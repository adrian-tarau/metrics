package net.microfalx.metrics;

import net.microfalx.lang.CollectionUtils;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A matrix of values for a metric.
 */
public final class Matrix {

    private final Metric metric;
    private final List<Value> values;

    /**
     * Creates a matrix for a given metrics with no values.
     *
     * @param metric the metrics
     * @return a non-null instance
     */
    public static Matrix empty(Metric metric) {
        return create(metric, Collections.emptyList());
    }

    /**
     * Creates a matrix for a given metric and its values.
     *
     * @param metric the metric
     * @param values the values
     * @return a non-null instance
     */
    public static Matrix create(Metric metric, Iterator<Value> values) {
        return new Matrix(metric, CollectionUtils.toIterable(values));
    }

    /**
     * Creates a matrix for a given metric and its values.
     *
     * @param metric the metric
     * @param values the values
     * @return a non-null instance
     */
    public static Matrix create(Metric metric, Iterable<Value> values) {
        return new Matrix(metric, values);
    }

    Matrix(Metric metric, Iterable<Value> values) {
        requireNonNull(metric);
        this.metric = metric;
        this.values = CollectionUtils.toList(values);
        this.values.sort(Comparator.comparing(Value::getTimestamp));
    }

    /**
     * Returns the metric.
     *
     * @return a non-null instance
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * Returns the values.
     *
     * @return a non-nll instance
     */
    public List<Value> getValues() {
        return unmodifiableList(values);
    }

    /**
     * Returns the number of points this matrix has.
     *
     * @return a positive integer
     */
    public int getCount() {
        return values.size();
    }

    /**
     * Returns first value in the matrix.
     *
     * @return an optional value
     */
    public Optional<Value> getFirst() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    /**
     * Returns last (most recent) value in the matrix.
     *
     * @return the value, null if there are no values available
     */
    public Optional<Value> getLast() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(values.size() - 1));
    }

    /**
     * Returns the average across in the matrix.
     *
     * @return an optional average
     */
    public OptionalDouble getAverage() {
        return values.stream().mapToDouble(Value::asDouble).average();
    }

    /**
     * Returns the minimum across in the matrix.
     *
     * @return an optional average
     */
    public OptionalDouble getMinimum() {
        return values.stream().mapToDouble(Value::asDouble).min();
    }

    /**
     * Returns the maximum across in the matrix.
     *
     * @return an optional average
     */
    public OptionalDouble getMaximum() {
        return values.stream().mapToDouble(Value::asDouble).max();
    }

    /**
     * Returns the sum across in the matrix.
     *
     * @return the sum
     */
    public double getSum() {
        return values.stream().mapToDouble(Value::asDouble).sum();
    }

    /**
     * Creates a series out of a matrix.
     *
     * @return a non-null instance
     */
    public Series toSeries() {
        return Series.create(String.join(" / ", metric.getLabels()), values);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Matrix.class.getSimpleName() + "[", "]")
                .add("metric=" + metric)
                .add("values=" + values.size())
                .toString();
    }
}
