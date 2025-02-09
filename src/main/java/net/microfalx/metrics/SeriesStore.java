package net.microfalx.metrics;

import org.atteo.classindex.IndexSubclasses;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * An abstraction of a store for {@link Series}.
 */
@IndexSubclasses
public interface SeriesStore extends Comparable<SeriesStore> {

    /**
     * Creates a series store.
     *
     * @return a non-null instance
     */
    static SeriesStore create() {
        return MetricUtils.create();
    }

    /**
     * Creates a memory store.
     *
     * @return a non-null instance
     */
    static SeriesStore get() {
        return MetricUtils.getDefault();
    }

    /**
     * Creates a memory store.
     *
     * @return a non-null instance
     */
    static SeriesStore memory() {
        return new SeriesMemoryStore();
    }

    /**
     * Creates a series store which stores metrics on disk.
     *
     * @param name the name on disk
     * @return a non-null instance
     */
    static SeriesStore disk(String name) {
        return new SqliteSeriesStore(name);
    }

    /**
     * Returns the retention of this store.
     *
     * @return a non-null instance
     */
    Duration getRetention();

    /**
     * Changes the retention of this store.
     *
     * @param retention the new retention
     * @return a new instance
     */
    SeriesStore setRetention(Duration retention);

    /**
     * Returns the stored metrics.
     *
     * @return a non-null instance
     */
    Set<Metric> getMetrics();

    /**
     * Returns the earliest timestamp available in the store.
     *
     * @return a non-null instance
     */
    Optional<LocalDateTime> getEarliestTimestamp();

    /**
     * Returns the earliest timestamp available in the store for a given metric.
     *
     * @param metric the metric
     * @return a non-null instance
     */
    Optional<LocalDateTime> getEarliestTimestamp(Metric metric);

    /**
     * Returns the latest timestamp available in the store.
     *
     * @return a non-null instance
     */
    Optional<LocalDateTime> getLatestTimestamp();

    /**
     * Returns the latest timestamp available in the store for a given metric.
     *
     * @return a non-null instance
     */
    Optional<LocalDateTime> getLatestTimestamp(Metric metric);

    /**
     * Returns the series for a given metric.
     *
     * @param metric the metric
     * @return the series
     */
    Series get(Metric metric);

    /**
     * Returns the series for a given metric within a time interval
     *
     * @param metric the metric
     * @param from   the start of the interval
     * @param to     the end of the interval
     * @return the series
     */
    Series get(Metric metric, Temporal from, Temporal to);

    /**
     * Returns the average for a given metric.
     *
     * @param metric the metric
     * @param from   the start of the interval
     * @param to     the end of the interval
     * @return the average
     */
    OptionalDouble getAverage(Metric metric, Temporal from, Temporal to);

    /**
     * Returns the average for a given metric.
     *
     * @param metric   the metric
     * @param interval the interval up to now
     * @return the average
     */
    OptionalDouble getAverage(Metric metric, Duration interval);

    /**
     * Returns the average for a given metric.
     *
     * @param metric the metric
     * @return the average
     */
    OptionalDouble getAverage(Metric metric);

    /**
     * Adds a new value to this series.
     *
     * @param metric the metric
     * @param value  the value
     */
    void add(Metric metric, Value value);

    /**
     * Adds a batch of metrics to this store.
     *
     * @param batch the batch
     */
    void add(Batch batch);

    /**
     * Clears the store.
     */
    void clear();

    /**
     * Adds all metrics from a given store to this store.
     *
     * @param store the other store
     */
    void add(SeriesStore store);

    /**
     * Adds all metrics from a given series into this series.
     *
     * @param average {@code true} to add the average of the metrics, {@code false} all values
     * @param series  the series
     */
    void add(Collection<SeriesStore> series, boolean average);
}
