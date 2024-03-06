package net.microfalx.metrics;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.time.Duration;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ExceptionUtils.throwException;
import static net.microfalx.metrics.MetricsUtils.computeId;

public class MicrometerMetrics extends Metrics {

    private final MeterRegistry registry = io.micrometer.core.instrument.Metrics.globalRegistry;

    public MicrometerMetrics(String group) {
        super(group);
    }

    @Override
    public Counter getCounter(String name) {
        requireNonNull(name);
        String id = computeId(getGroup(), name);
        return counters.computeIfAbsent(id, s -> {
            io.micrometer.core.instrument.Counter counter = registry.counter(id, getTags(name));
            return new CounterImpl(this, name, counter);
        });
    }

    @Override
    public Gauge getGauge(String name, Supplier<Double> supplier) {
        String id = computeId(getGroup(), name);
        return gauges.computeIfAbsent(id, s -> {
            registry.gauge(id, getTags(name), null, value -> supplier.get());
            return new GaugeImpl(this, name, registry.find(name).gauge(), null, supplier);
        });
    }

    @Override
    public Gauge getGauge(String name) {
        String id = computeId(getGroup(), name);
        return gauges.computeIfAbsent(id, s -> {
            AtomicLong gauge = registry.gauge(id, new AtomicLong());
            return new GaugeImpl(this, name, registry.find(name).gauge(), gauge, null);
        });
    }

    @Override
    public Timer getTimer(String name, Timer.Type type) {
        String id = computeId(getGroup(), name);
        Timer finalTimer = timers.computeIfAbsent(id, s -> {
            io.micrometer.core.instrument.Meter timer;
            if (type == Timer.Type.SHORT) {
                timer = registry.timer(id, getTags(name));
            } else {
                timer = registry.more().longTaskTimer(id, getTags(name));
            }
            return new TimerImpl(this, name, timer, type);
        });
        Metrics.LAST.set(finalTimer);
        return finalTimer;
    }

    private List<Tag> doGetTags() {
        return this.getTags().entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    private Tags getTags(String name) {
        List<Tag> tags = doGetTags();
        tags.add(Tag.of(NAME_TAG, name));
        return Tags.of(tags);
    }

    static class MeterImpl extends AbstractMeter {

        private final Metrics metrics;
        private final String name;
        protected final io.micrometer.core.instrument.Meter meter;

        public MeterImpl(Metrics metrics, String name, io.micrometer.core.instrument.Meter meter) {
            super(metrics.getGroup(), name);
            requireNonNull(metrics);
            requireNonNull(name);
            requireNonNull(meter);
            this.metrics = metrics;
            this.name = name;
            this.meter = meter;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                    .add("metrics=" + metrics)
                    .add("name='" + name + "'")
                    .add("meter=" + meter)
                    .toString();
        }
    }

    static class CounterImpl extends MeterImpl implements Counter {

        public CounterImpl(Metrics metrics, String name, io.micrometer.core.instrument.Meter meter) {
            super(metrics, name, meter);
        }

        @Override
        public long getValue() {
            return (long) ((io.micrometer.core.instrument.Counter) meter).count();
        }

        @Override
        public long increment() {
            touch();
            ((io.micrometer.core.instrument.Counter) meter).increment();
            return getValue();
        }

        @Override
        public long increment(int delta) {
            touch();
            ((io.micrometer.core.instrument.Counter) meter).increment(delta);
            return getValue();
        }
    }

    static class GaugeImpl extends MeterImpl implements Gauge {

        private AtomicLong value;
        private Supplier<Double> supplier;

        public GaugeImpl(Metrics metrics, String name, io.micrometer.core.instrument.Meter meter, AtomicLong value, Supplier<Double> supplier) {
            super(metrics, name, meter);
            this.value = value;
            this.supplier = supplier;
        }

        @Override
        public long increment() {
            touch();
            return value != null ? value.incrementAndGet() : 0;
        }

        @Override
        public long decrement() {
            touch();
            return value != null ? value.decrementAndGet() : 0;
        }

        @Override
        public long getValue() {
            return value != null ? value.get() : supplier.get().longValue();
        }
    }

    static class TimerImpl extends MeterImpl implements Timer {

        private final Type type;
        private LongTaskTimer.Sample sample;

        public TimerImpl(Metrics metrics, String name, io.micrometer.core.instrument.Meter meter, Type type) {
            super(metrics, name, meter);
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Timer start() {
            touch();
            if (type != Type.LONG) throw new IllegalStateException("A short timer cannot be started manually");
            sample = ((io.micrometer.core.instrument.LongTaskTimer) meter).start();
            return this;
        }

        @Override
        public Timer stop() {
            if (sample != null) sample.stop();
            return this;
        }

        @Override
        public <T> T record(Supplier<T> supplier) {
            touch();
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).record(supplier);
            } else {
                return ((io.micrometer.core.instrument.LongTaskTimer) meter).record(supplier);
            }
        }

        @Override
        public void record(Consumer<Timer> consumer) {
            touch();
            if (type == Type.SHORT) {
                ((io.micrometer.core.instrument.Timer) meter).record(() -> consumer.accept(this));
            } else {
                ((io.micrometer.core.instrument.LongTaskTimer) meter).record(() -> consumer.accept(this));
            }
        }

        @Override
        public <T> void record(Consumer<T> consumer, T value) {
            touch();
            if (type == Type.SHORT) {
                ((io.micrometer.core.instrument.Timer) meter).record(() -> consumer.accept(value));
            } else {
                ((io.micrometer.core.instrument.LongTaskTimer) meter).record(() -> consumer.accept(value));
            }
        }

        @Override
        public <T> T recordCallable(Callable<T> callable) {
            touch();
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).record(() -> {
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        return throwException(e);
                    }
                });
            } else {
                return ((io.micrometer.core.instrument.LongTaskTimer) meter).record(() -> {
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        return throwException(e);
                    }
                });
            }
        }

        @Override
        public void record(Runnable runnable) {
            touch();
            if (meter instanceof LongTaskTimer) {
                ((LongTaskTimer) meter).record(runnable);
            } else {
                ((io.micrometer.core.instrument.Timer) meter).record(runnable);
            }
        }

        @Override
        public Runnable wrap(Runnable f) {
            touch();
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).wrap(f);
            } else {
                return throwWrappersNotSupported();
            }
        }

        @Override
        public <T> Callable<T> wrap(Callable<T> f) {
            touch();
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).wrap(f);
            } else {
                return throwWrappersNotSupported();
            }
        }

        @Override
        public <T> Supplier<T> wrap(Supplier<T> f) {
            touch();
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).wrap(f);
            } else {
                return throwWrappersNotSupported();
            }
        }

        @Override
        public Duration getDuration() {
            if (meter instanceof LongTaskTimer) {
                return Duration.ofMillis((long) ((LongTaskTimer) meter).duration(TimeUnit.MILLISECONDS));
            } else {
                return Duration.ofMillis((long) ((io.micrometer.core.instrument.Timer) meter).totalTime(TimeUnit.MILLISECONDS));
            }
        }

        @Override
        public long getCount() {
            if (meter instanceof LongTaskTimer) {
                return -1;
            } else {
                return ((io.micrometer.core.instrument.Timer) meter).count();
            }
        }

        @Override
        public Duration getAverageDuration() {
            if (meter instanceof LongTaskTimer) {
                return Duration.ofMillis((long) ((LongTaskTimer) meter).mean(TimeUnit.MILLISECONDS));
            } else {
                return Duration.ofMillis((long) ((io.micrometer.core.instrument.Timer) meter).mean(TimeUnit.MILLISECONDS));
            }
        }

        @Override
        public Duration getMinimumDuration() {
            return Duration.ofMillis(-1);
        }

        @Override
        public Duration getMaximumDuration() {
            if (meter instanceof LongTaskTimer) {
                return Duration.ofMillis((long) ((LongTaskTimer) meter).max(TimeUnit.MILLISECONDS));
            } else {
                return Duration.ofMillis((long) ((io.micrometer.core.instrument.Timer) meter).max(TimeUnit.MILLISECONDS));
            }
        }

        @SuppressWarnings("resource")
        @Override
        public void close() {
            stop();
        }

        private <T> T throwWrappersNotSupported() {
            return thrownUnsupported("Long timers do not support wrappers");
        }

        private <T> T thrownUnsupported(String name) {
            throw new UnsupportedOperationException(name);
        }
    }
}
