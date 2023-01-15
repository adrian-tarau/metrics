package net.microfalx.metrics;

import java.lang.module.ResolutionException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.microfalx.metrics.MetricsUtils.requireNonNull;

/**
 * An abstraction to track various metrics.
 * <p>
 * It also carries a group name (namespace) so all metrics are covering the same group of metrics without the new to
 * keep adding the same namespace.
 * <p>
 * It also abstracts counters, gauges, timers and histograms with simple method calls.
 */
public abstract class Metrics implements Cloneable {

    private static final String METRICS_IMPLEMENTATION_CLASS = "net.microfalx.metrics.MicrometerMetrics";
    private static final String GROUP_SEPARATOR = ".";

    private String group;
    protected Map<String, String> tags = new HashMap<>();
    protected String[] tagsArray = new String[0];

    /**
     * Returns an instance of a metric group.
     *
     * @return a non-null instance
     */
    public static Metrics of(String group) {
        return doCreate(group);
    }

    /**
     * Creates an instance with a given group (namespace)
     *
     * @param group the group name
     */
    public Metrics(String group) {
        requireNonNull(group);
        this.group = group;
    }

    /**
     * Returns the final name of a metrics within this group.
     *
     * @param name the name
     * @return the final name
     */
    public String getName(String name) {
        return finalName(name);
    }

    /**
     * Returns the group name (namespace) associated with this instance of metrics.
     *
     * @return a non-null instance
     */
    public final String getGroup() {
        return group;
    }

    /**
     * Returns the tags associated with this metrics.
     *
     * @return a non-null instance
     */
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    /**
     * Registers a common tag.
     *
     * @param key   the key
     * @param value the value
     */
    public void addTag(String key, String value) {
        requireNonNull(key);
        requireNonNull(value);

        tags.put(key, value);
        Collection<String> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            values.add(entry.getKey());
            values.add(entry.getValue());
        }
        this.tagsArray = values.toArray(new String[0]);
    }

    /**
     * Creates a copy of the current metrics and adds a new subgroup.
     *
     * @param name the name of the group
     * @return a new instance
     */
    public Metrics withGroup(String name) {
        requireNonNull(name);
        Metrics copy = copy();
        copy.group += GROUP_SEPARATOR + name;
        return copy;
    }

    /**
     * Creates a copy of the current metrics and adds a new tag.
     *
     * @param key   the key
     * @param value the value
     * @return a new instance
     */
    public Metrics withTag(String key, String value) {
        Metrics copy = copy();
        copy.addTag(key, value);
        return copy;
    }

    /**
     * Updates metrics specific tags.
     */
    protected void updateTagsCache() {
        // empty on purpose
    }

    /**
     * Increments a counter within a group.
     *
     * @param name the name of the counter
     */
    public long count(String name) {
        return getCounter(name).increment();
    }

    /**
     * Increases a gauge by one.
     *
     * @param name the name of the counter
     */
    public long increment(String name) {
        return getGauge(name).increment();
    }

    /**
     * Decreases a gauge by one.
     *
     * @param name the name of the counter
     */
    public long decrement(String name) {
        return getGauge(name).decrement();
    }

    /**
     * Times a block of code.
     *
     * @param name     the name of the timer
     * @param supplier the supplier
     */
    public <T> T time(String name, Supplier<T> supplier) {
        return getTimer(name, Timer.Type.SHORT).record(supplier);
    }

    /**
     * Times a block of code.
     *
     * @param name     the name of the timer
     * @param callable the callable
     */
    public <T> T time(String name, Callable<T> callable) {
        return getTimer(name, Timer.Type.SHORT).record(callable);
    }

    /**
     * Times a block of code.
     *
     * @param name     the name of the timer
     * @param consumer the consumer
     */
    public void time(String name, Consumer<Timer> consumer) {
        getTimer(name, Timer.Type.SHORT).record(consumer);
    }

    /**
     * Times a block of code.
     *
     * @param name     the name of the timer
     * @param consumer the consumer
     * @param value    the value passed to the consumer
     */
    public <T> void time(String name, Consumer<T> consumer, T value) {
        getTimer(name, Timer.Type.SHORT).record(consumer, value);
    }

    /**
     * Returns a counter within a group.
     *
     * @param name the name of the counter
     */
    public abstract Counter getCounter(String name);

    /**
     * Returns a gauge within a group.
     *
     * @param name the name of the counter
     */
    public abstract Gauge getGauge(String name);

    /**
     * Registers a gauge which extracts the value from a supplier.
     *
     * @param name the name of the counter
     */
    public abstract Gauge getGauge(String name, Supplier<Double> supplier);

    /**
     * Returns a timer to time a block of code.
     * <p>
     * The method returns an auto-closable resource and this should be called in a "try with resource" pattern.
     *
     * @param name the name of the timer
     * @return the resource to stop the timer
     */
    public Timer getTimer(String name) {
        return getTimer(name, Timer.Type.SHORT);
    }

    /**
     * Returns a timer to time a block of code.
     * <p>
     * The method returns an auto-closable resource and this should be called in a "try with resource" pattern.
     *
     * @param name the name of the timer
     * @param type the type of the timer
     * @return the resource to stop the timer
     */
    public abstract Timer getTimer(String name, Timer.Type type);

    /**
     * Starts a timer to time a block of code.
     * <p>
     * The method returns an auto-closable resource and this should be called in a "try with resource" pattern.
     *
     * @param name the name of the timer
     * @return the resource to stop the timer
     */
    public Timer startTimer(String name) {
        return getTimer(name).start();
    }

    /**
     * Creates the first available implementation of metrics.
     *
     * @param group the group name
     * @return the instance
     */
    private static Metrics doCreate(String group) {
        try {
            Class<?> clazz = Metrics.class.getClassLoader().loadClass(METRICS_IMPLEMENTATION_CLASS);
            return (Metrics) clazz.getConstructor(String.class).newInstance(group);
        } catch (Throwable e) {
            return new NoMetrics(group);
        }
    }

    /**
     * Creates a deep copy of the metrics.
     *
     * @return a new copy
     */
    private Metrics copy() {
        try {
            Metrics metrics = (Metrics) clone();
            metrics.tags = new HashMap<>(tags);
            metrics.tagsArray = null;
            return metrics;
        } catch (CloneNotSupportedException e) {
            throw new ResolutionException("Cannot clone ", e);
        }
    }

    protected final String finalName(String name) {
        return normalize(group) + GROUP_SEPARATOR + normalize(name);
    }

    private static String normalize(String name) {
        if (name == null) return "na";
        name = name.toLowerCase();
        name = name.replace(' ', '_');
        return name;
    }


    /**
     * Default implementation in case there is no other present.
     */
    static class NoMetrics extends Metrics {

        public NoMetrics(String group) {
            super(group);
        }

        @Override
        public Counter getCounter(String name) {
            return new NoCounter(name);
        }

        @Override
        public Gauge getGauge(String name) {
            return new NoGauge(name);
        }

        @Override
        public Gauge getGauge(String name, Supplier<Double> supplier) {
            return new NoGauge(name);
        }

        @Override
        public Timer getTimer(String name, Timer.Type type) {
            return new NoTimer(name);
        }
    }

    static class AbstractMeter implements Meter {

        private final String name;

        public AbstractMeter(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    static class NoCounter extends AbstractMeter implements Counter {

        public NoCounter(String name) {
            super(name);
        }

        @Override
        public long getValue() {
            return 0;
        }

        @Override
        public long increment() {
            return 0;
        }
    }

    static class NoGauge extends AbstractMeter implements Gauge {

        public NoGauge(String name) {
            super(name);
        }

        @Override
        public long increment() {
            return 0;
        }

        @Override
        public long decrement() {
            return 0;
        }

        @Override
        public long getValue() {
            return 0;
        }
    }

    static class NoTimer extends AbstractMeter implements Timer {

        public NoTimer(String name) {
            super(name);
        }

        @Override
        public Type getType() {
            return Type.SHORT;
        }

        @Override
        public Timer start() {
            return this;
        }

        @Override
        public Timer stop() {
            return this;
        }

        @Override
        public <T> T record(Supplier<T> supplier) {
            return supplier.get();
        }

        @Override
        public void record(Consumer<Timer> consumer) {
            consumer.accept(this);
        }

        @Override
        public <T> void record(Consumer<T> consumer, T value) {
            consumer.accept(value);
        }

        @Override
        public <T> T record(Callable<T> callable) {
            try {
                return callable.call();
            } catch (Exception e) {
                return MetricsUtils.throwException(e);
            }
        }

        @Override
        public void record(Runnable runnable) {
            runnable.run();
        }

        @Override
        public Duration getDuration() {
            return Duration.ZERO;
        }
    }
}
