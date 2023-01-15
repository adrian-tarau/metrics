package net.microfalx.metrics;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.microfalx.metrics.MetricsUtils.requireNonNull;

public class MicrometerMetrics extends Metrics {

    private final MeterRegistry registry = io.micrometer.core.instrument.Metrics.globalRegistry;
    private Iterable<Tag> mtags;

    public MicrometerMetrics(String group) {
        super(group);
    }

    @Override
    public Counter getCounter(String name) {
        requireNonNull(name);
        io.micrometer.core.instrument.Counter counter = registry.counter(finalName(name), tagsArray);
        return new CounterImpl(counter);
    }

    @Override
    public Gauge getGauge(String name, Supplier<Double> supplier) {
        registry.gauge(finalName(name), mtags, null, value -> supplier.get());
        return new GaugeImpl(registry.find(name).gauge(), null, supplier);
    }

    @Override
    public Gauge getGauge(String name) {
        AtomicLong gauge = registry.gauge(finalName(name), new AtomicLong());
        return new GaugeImpl(registry.find(name).gauge(), gauge, null);
    }


    @Override
    public Timer getTimer(String name, Timer.Type type) {
        io.micrometer.core.instrument.Meter timer;
        if (type == Timer.Type.SHORT) {
            timer = registry.timer(finalName(name), mtags);
        } else {
            timer = registry.more().longTaskTimer(finalName(name), mtags);
        }
        return new TimerImpl(timer, type);
    }

    @Override
    protected void updateTagsCache() {
        mtags = Tags.of(tagsArray);
    }

    static class MeterImpl implements Meter {

        protected final io.micrometer.core.instrument.Meter meter;

        public MeterImpl(io.micrometer.core.instrument.Meter meter) {
            this.meter = meter;
        }

        @Override
        public String getName() {
            return meter.getId().getName();
        }
    }

    static class CounterImpl extends MeterImpl implements Counter {

        public CounterImpl(io.micrometer.core.instrument.Meter meter) {
            super(meter);
        }


        @Override
        public long getValue() {
            return (long) ((io.micrometer.core.instrument.Counter) meter).count();
        }

        @Override
        public long increment() {
            ((io.micrometer.core.instrument.Counter) meter).increment();
            return getValue();
        }
    }

    static class GaugeImpl extends MeterImpl implements Gauge {

        private AtomicLong value;
        private Supplier<Double> supplier;

        public GaugeImpl(io.micrometer.core.instrument.Meter meter, AtomicLong value, Supplier<Double> supplier) {
            super(meter);
            this.value = value;
            this.supplier = supplier;
        }

        @Override
        public long increment() {
            return value != null ? value.incrementAndGet() : 0;
        }

        @Override
        public long decrement() {
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

        public TimerImpl(io.micrometer.core.instrument.Meter meter, Type type) {
            super(meter);
            this.type = type;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Timer start() {
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
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).record(supplier);
            } else {
                return ((io.micrometer.core.instrument.LongTaskTimer) meter).record(supplier);
            }
        }

        @Override
        public void record(Consumer<Timer> consumer) {
            if (type == Type.SHORT) {
                ((io.micrometer.core.instrument.Timer) meter).record(() -> consumer.accept(this));
            } else {
                ((io.micrometer.core.instrument.LongTaskTimer) meter).record(() -> consumer.accept(this));
            }
        }

        @Override
        public <T> void record(Consumer<T> consumer, T value) {
            if (type == Type.SHORT) {
                ((io.micrometer.core.instrument.Timer) meter).record(() -> consumer.accept(value));
            } else {
                ((io.micrometer.core.instrument.LongTaskTimer) meter).record(() -> consumer.accept(value));
            }
        }

        @Override
        public <T> T record(Callable<T> callable) {
            if (type == Type.SHORT) {
                return ((io.micrometer.core.instrument.Timer) meter).record(() -> {
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        return MetricsUtils.throwException(e);
                    }
                });
            } else {
                return ((io.micrometer.core.instrument.LongTaskTimer) meter).record(() -> {
                    try {
                        return callable.call();
                    } catch (Exception e) {
                        return MetricsUtils.throwException(e);
                    }
                });
            }
        }

        @Override
        public void record(Runnable runnable) {

        }

        @Override
        public Duration getDuration() {
            if (type == Type.SHORT) {
                return Duration.ofMillis((long) ((io.micrometer.core.instrument.Timer) meter).totalTime(TimeUnit.MILLISECONDS));
            } else {
                return Duration.ofMillis((long) ((LongTaskTimer) meter).duration(TimeUnit.MILLISECONDS));
            }
        }
    }
}
