package net.microfalx.metrics;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public class CompositeSeries extends AbstractSeries {

    public CompositeSeries(String name) {
        super(name);
    }

    @Override
    public List<Value> getValues() {
        return List.of();
    }

    @Override
    public Value get(int index) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public Optional<Value> getFirst() {
        return Optional.empty();
    }

    @Override
    public Optional<Value> getLast() {
        return Optional.empty();
    }

    @Override
    public OptionalDouble getAverage() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMinimum() {
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble getMaximum() {
        return OptionalDouble.empty();
    }

    @Override
    public double getWeight() {
        return 0;
    }

    @Override
    public Series add(Value value) {
        return null;
    }

    @Override
    public Series compact() {
        return null;
    }
}
