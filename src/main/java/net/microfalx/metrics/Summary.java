package net.microfalx.metrics;

import java.time.Duration;

/**
 * A timer with percentiles support.
 */
public interface Summary extends Timer {

    /**
     * Returns the duration for the given percentile.
     *
     * @param percentile the percentile
     * @return a long value
     */
    Duration getPercentile(Percentile percentile);

    /**
     * Returns all percentiles durations.
     *
     * @return a non-null array with 3 values: p50, p95, p99
     */
    Duration[] getPercentiles();

}
