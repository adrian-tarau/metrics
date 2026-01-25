package net.microfalx.metrics;

/**
 * An enum representing supported percentiles.
 */
public enum Percentile {

    /**
     * The 50th percentile (median).
     */
    P50,

    /**
     * The 95th percentile (tail).
     */
    P95,

    /**
     * The 99th percentile (worse case).
     */
    P99
}
