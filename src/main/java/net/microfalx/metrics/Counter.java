package net.microfalx.metrics;

/**
 * An interface for a counter
 */
public interface Counter extends Meter {

    /**
     * Returns the current value of the counter.
     *
     * @return a positive long
     */
    long getValue();

    /**
     * Increments the counter.
     *
     * @return a positive long
     */
    long increment();

    /**
     * Increments the counter by a delta.
     *
     * @param delta the delta
     * @return the counter
     */
    long increment(int delta);
}
