package net.microfalx.metrics;

/**
 * A base interface for all meters.
 */
public interface Meter {

    /**
     * Returns the name of the meter.
     *
     * @return a non-null instance
     */
    String getName();
}
