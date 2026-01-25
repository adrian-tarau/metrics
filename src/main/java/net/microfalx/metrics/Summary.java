package net.microfalx.metrics;

/**
 * A timer with percentiles support.
 */
public interface Summary extends Meter {

    /**
     * Returns the value for the given percentile.
     *
     * @param percentile the percentile
     * @return a long value
     */
    double getPercentile(Percentile percentile);

    /**
     * Returns all percentiles.
     *
     * @return a non-null array with 3 values: p50, p95, p99
     */
    double[] getPercentiles();

}
