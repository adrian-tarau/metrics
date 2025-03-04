package net.microfalx.metrics;

import com.esotericsoftware.kryo.DefaultSerializer;
import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;
import org.apache.commons.lang3.tuple.Pair;

import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.util.Collections.unmodifiableSet;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A store which holds all series in memory.
 */
@Provider
@Order(Order.AFTER)
@DefaultSerializer(SeriesMemoryStoreSerializer.class)
public class SeriesMemoryStore extends AbstractSeriesStore {

    final Map<Metric, Series> series = new ConcurrentSkipListMap<>();

    protected SeriesMemoryStore() {
    }

    @Override
    public Set<Metric> getMetrics() {
        return unmodifiableSet(series.keySet());
    }

    @Override
    public Series get(Metric metric) {
        requireNonNull(metric);
        return getOrCreate(metric);
    }

    @Override
    public Series get(Metric metric, Temporal from, Temporal to) {
        requireNonNull(metric);
        Series series = get(metric);
        Collection<Value> newValues = new ArrayList<>();
        for (Value value : series.getValues()) {
            if (value.isWithin(from, to)) newValues.add(value);
        }
        return Series.create(series.getName(), newValues);
    }

    @Override
    public OptionalDouble getAverage(Metric metric, Temporal from, Temporal to) {
        return get(metric, from, to).getAverage();
    }

    @Override
    public void add(Metric metric, Value value) {
        requireNonNull(metric);
        Series series = getOrCreate(metric);
        value = adaptValue(metric, value);
        if (value != null) series.add(value);
    }

    @Override
    public void add(Batch batch) {
        requireNonNull(batch);
        for (Pair<Metric, Value> value : batch) {
            Metric metric = value.getKey();
            Series series = getOrCreate(metric);
            Value adaptedValue = adaptValue(metric, value.getValue());
            if (adaptedValue != null) series.add(adaptedValue);
        }
    }

    @Override
    public void clear() {
        series.clear();
    }

    private Series getOrCreate(Metric metric) {
        return this.series.computeIfAbsent(metric, m -> Series.create(metric.getName()).setRetention(getRetention()));
    }
}
