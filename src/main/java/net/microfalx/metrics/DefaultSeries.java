package net.microfalx.metrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Collections.unmodifiableList;
import static net.microfalx.lang.CollectionUtils.toList;

/**
 * A default implementation which holds all values in a list.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class DefaultSeries extends AbstractSeries {

    private final List<Value> values;

    private OptionalDouble average;
    private OptionalDouble minimum;
    private OptionalDouble maximum;

    private double weight = Double.MIN_VALUE;

    static Series random(String name, LocalDateTime start, Duration interval, int count, double min, double max) {
        Random random = ThreadLocalRandom.current();
        List<Value> values = new ArrayList<>();
        double range = max - min;
        for (int i = 0; i < count; i++) {
            values.add(Value.create(start, (float) (min + random.nextDouble(range))));
            start = start.plus(interval);
        }
        return Series.create(name, values);
    }

    DefaultSeries(String name, Iterable<Value> values) {
        super(name);
        this.values = toList(values);
        this.values.sort(Comparator.comparing(Value::getTimestamp));
    }

    public List<Value> getValues() {
        return unmodifiableList(values);
    }

    public Value get(int index) {
        return values.get(index);
    }

    public int getCount() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Optional<Value> getFirst() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }

    public Optional<Value> getLast() {
        return values.isEmpty() ? Optional.empty() : Optional.of(values.get(values.size() - 1));
    }

    public OptionalDouble getAverage() {
        if (average == null) {
            average = values.stream().mapToDouble(Value::asDouble).average();
        }
        return average;
    }

    public OptionalDouble getMinimum() {
        if (minimum == null) {
            minimum = values.stream().mapToDouble(Value::asDouble).min();
        }
        return minimum;
    }

    public OptionalDouble getMaximum() {
        if (maximum == null) {
            maximum = values.stream().mapToDouble(Value::asDouble).max();
        }
        return maximum;
    }

    public double getWeight() {
        if (weight == Double.MIN_VALUE) {
            weight = getMaximum().orElse(0) / values.size();
        }
        return weight;
    }

    @Override
    public Series add(Value value) {
        List<Value> newValues = new ArrayList<>(values);
        newValues.add(value);
        return new DefaultSeries(getName(), newValues);
    }

    void doAdd(Value value) {
        this.values.add(value);
    }

    @Override
    public Series compact() {
        byte[] data = CompactSeries.compact(values);
        return new CompactSeries(getName(), data);
    }
}
