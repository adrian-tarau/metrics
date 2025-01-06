package net.microfalx.metrics;

import org.atteo.classindex.IndexSubclasses;

import java.time.LocalDateTime;

/**
 * An abstraction of a store for {@link Series}.
 */
@IndexSubclasses
public interface SeriesStore {

    /**
     * Creates a series store.
     *
     * @return a non-null instance
     */
    static SeriesStore create() {
        return MetricUtils.create();
    }

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
    Series get(Metric metric, LocalDateTime from, LocalDateTime to);

    /**
     * Adds a new value to a series.
     *
     * @param metric the metric
     * @param value  the value
     */
    void add(Metric metric, Value value);
}
