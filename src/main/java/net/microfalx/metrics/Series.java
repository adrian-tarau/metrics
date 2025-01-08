package net.microfalx.metrics;

import net.microfalx.lang.Identifiable;
import net.microfalx.lang.Nameable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * A collection of points in time for a named series.
 */
public interface Series extends Identifiable<String>, Nameable {

    /**
     * Creates an empty series for a given metric and its values.
     *
     * @param name the name of the series
     * @return a non-null instance
     */
    static Series create(String name) {
        return new DefaultSeries(name, null);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param values the iterable
     * @return a non-null instance
     */
    static Series create(String name, Iterable<Value> values) {
        return new DefaultSeries(name, values);
    }

    /**
     * Creates a series from a list of values.
     *
     * @param name   the name of the series
     * @param values the values
     * @return a non-null instance
     */
    static Series create(String name, Value... values) {
        return new DefaultSeries(name, Arrays.asList(values));
    }

    /**
     * Creates a series with random values.
     *
     * @param name     the name of the series.
     * @param start    the start time
     * @param interval the interval
     * @param count    the number of values to add
     * @param min      the minimum value
     * @param max      the maximum value
     * @return the series
     */
    static Series random(String name, LocalDateTime start, Duration interval, int count, float min, float max) {
        return DefaultSeries.random(name, start, interval, count, min, max);
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
    Series setRetention(Duration retention);

    /**
     * Return a list of values.
     *
     * @return a non-null instance
     */
    List<Value> getValues();

    /**
     * Returns the value at a given index.
     *
     * @param index the index
     * @return the value
     */
    Value get(int index);

    /**
     * Returns the number of points this series has.
     *
     * @return a positive integer
     */
    int getCount();

    /**
     * Returns whether the series is empty,
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();

    /**
     * Returns first value in the series.
     *
     * @return an optional value
     */
    Optional<Value> getFirst();

    /**
     * Returns last (most recent) value in the series.
     *
     * @return the value, null if there are no values available
     */
    Optional<Value> getLast();

    /**
     * Returns the average across in the series.
     *
     * @return an optional average
     */
    OptionalDouble getAverage();

    /**
     * Returns the minimum across in the series.
     *
     * @return an optional average
     */
    OptionalDouble getMinimum();

    /**
     * Returns the maximum across in the series.
     *
     * @return an optional average
     */
    OptionalDouble getMaximum();

    /**
     * Returns the weight of this series.
     * <p>
     * The weight is useful to compare which series has more "stuff" and can be displayed first (or kept).
     *
     * @return a positive number
     */
    double getWeight();

    /**
     * Adds a new value to the series.
     *
     * @param value the new value
     * @return a new series with a new value
     */
    Series add(Value value);

    /**
     * Compacts the series.
     *
     * @return a non-null instance
     */
    Series compact();
}
