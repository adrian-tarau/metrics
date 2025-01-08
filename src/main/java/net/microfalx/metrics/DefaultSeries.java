package net.microfalx.metrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.microfalx.lang.CollectionUtils.toList;

/**
 * A default implementation which holds all values in a list.
 */
@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
class DefaultSeries extends AbstractSeries {

    private final List<Value> values;

    private volatile OptionalDouble average;
    private volatile OptionalDouble minimum;
    private volatile OptionalDouble maximum;

    private volatile double weight = Double.MIN_VALUE;

    static Series random(String name, LocalDateTime start, Duration interval, int count, float min, float max) {
        Random random = ThreadLocalRandom.current();
        List<Value> values = new ArrayList<>();
        float range = max - min;
        for (int i = 0; i < count; i++) {
            values.add(Value.create(start, min + range * random.nextFloat()));
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
        rlock.lock();
        try {
            return new ArrayList<>(values);
        } finally {
            rlock.unlock();
        }
    }

    public Value get(int index) {
        rlock.lock();
        try {
            return values.get(index);
        } finally {
            rlock.unlock();
        }
    }

    public int getCount() {
        rlock.lock();
        try {
            return values.size();
        } finally {
            rlock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        rlock.lock();
        try {
            return values.isEmpty();
        } finally {
            rlock.unlock();
        }
    }

    public Optional<Value> getFirst() {
        rlock.lock();
        try {
            return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
        } finally {
            rlock.unlock();
        }
    }

    public Optional<Value> getLast() {
        rlock.lock();
        try {
            return values.isEmpty() ? Optional.empty() : Optional.of(values.get(values.size() - 1));
        } finally {
            rlock.unlock();
        }
    }

    public OptionalDouble getAverage() {
        if (average == null) {
            rlock.lock();
            try {
                average = values.stream().mapToDouble(Value::asDouble).average();
            } finally {
                rlock.unlock();
            }
        }
        return average;
    }

    public OptionalDouble getMinimum() {
        if (minimum == null) {
            rlock.lock();
            try {
                minimum = values.stream().mapToDouble(Value::asDouble).min();
            } finally {
                rlock.unlock();
            }
        }
        return minimum;
    }

    public OptionalDouble getMaximum() {
        if (maximum == null) {
            rlock.lock();
            try {
                maximum = values.stream().mapToDouble(Value::asDouble).max();
            } finally {
                rlock.unlock();
            }
        }
        return maximum;
    }

    public double getWeight() {
        if (weight == Double.MIN_VALUE) {
            rlock.lock();
            try {
                weight = getMaximum().orElse(0) / values.size();
            } finally {
                rlock.unlock();
            }
        }
        return weight;
    }

    @Override
    public Series add(Value value) {
        wlock.lock();
        try {
            values.add(value);
        } finally {
            wlock.unlock();
        }
        return this;
    }

    @Override
    public Series compact() {
        byte[] data;
        rlock.lock();
        try {
            data = CompactSeries.compact(values);
        } finally {
            rlock.unlock();
        }
        return new CompactSeries(getName(), data);
    }
}
