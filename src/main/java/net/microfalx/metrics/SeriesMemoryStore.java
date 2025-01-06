package net.microfalx.metrics;

import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A store which holds all series in memory.
 */
@Provider
@Order(Order.AFTER)
public class SeriesMemoryStore implements SeriesStore {

    private final Map<Metric, Series> series = new ConcurrentHashMap<>();

    @Override
    public Series get(Metric metric) {
        requireNonNull(metric);
        synchronized (this) {
            return getOrCreate(metric);
        }
    }

    @Override
    public Series get(Metric metric, LocalDateTime from, LocalDateTime to) {
        requireNonNull(metric);
        Series series = get(metric);
        Collection<Value> newValues = new ArrayList<>();
        for (Value value : series.getValues()) {
            LocalDateTime dateTime = value.atDateTime();
            if (dateTime.isBefore(from) || dateTime.isAfter(to)) continue;
            newValues.add(value);
        }
        return Series.create(series.getName(), newValues);
    }

    @Override
    public void add(Metric metric, Value value) {
        requireNonNull(metric);
        synchronized (this) {
            Series series = getOrCreate(metric);
            if (series instanceof DefaultSeries defaultSeries) {
                defaultSeries.doAdd(value);
            } else {
                series = series.add(value);
                this.series.put(metric, series);
            }
        }
    }

    private Series getOrCreate(Metric metric) {
        return this.series.computeIfAbsent(metric, m -> Series.create(metric.getName()));
    }
}
