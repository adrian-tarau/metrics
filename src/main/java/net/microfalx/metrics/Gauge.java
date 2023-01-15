package net.microfalx.metrics;

/**
 * An interface for a meter which goes up and down
 */
public interface Gauge extends Meter {

    /**
     * Increments the value associated with the meter.
     *
     * @return the current value
     */
    long increment();

    /**
     * Decrements the value associated with the meter.
     *
     * @return the current value
     */
    long decrement();

    /**
     * Returns the value of the gauge.
     *
     * @return a positive long
     */
    long getValue();
}
