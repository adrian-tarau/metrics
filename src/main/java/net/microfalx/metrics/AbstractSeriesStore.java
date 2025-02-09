package net.microfalx.metrics;

import net.microfalx.lang.ObjectUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all series stores.
 */
public abstract class AbstractSeriesStore implements SeriesStore {

    private volatile Duration retention = ofMinutes(15);
    private final Map<Metric, Value> lastValues = new ConcurrentSkipListMap<>();

    private volatile Optional<LocalDateTime> earliestTimestamp;

    @Override
    public final Duration getRetention() {
        return retention;
    }

    @Override
    public final SeriesStore setRetention(Duration retention) {
        requireNonNull(retention);
        this.retention = retention;
        return this;
    }

    @Override
    public OptionalDouble getAverage(Metric metric, Duration interval) {
        requireNonNull(interval);
        LocalDateTime now = LocalDateTime.now();
        return getAverage(metric, now.minus(interval), now);
    }

    @Override
    public OptionalDouble getAverage(Metric metric) {
        return getAverage(metric, Duration.ofDays(1));
    }

    @Override
    public Optional<LocalDateTime> getEarliestTimestamp() {
        if (earliestTimestamp == null) {
            LocalDateTime earliest = null;
            for (Metric metric : getMetrics()) {
                Optional<LocalDateTime> timestamp = getEarliestTimestamp(metric);
                if (timestamp.isPresent()) {
                    earliest = ObjectUtils.compare(timestamp.get(), earliest) > 0 ? timestamp.get() : earliest;
                }
            }
            earliestTimestamp = Optional.ofNullable(earliest);
        }
        return earliestTimestamp;
    }

    @Override
    public Optional<LocalDateTime> getEarliestTimestamp(Metric metric) {
        requireNonNull(metric);
        return get(metric).getFirst().map(Value::atLocalTime);
    }

    @Override
    public Optional<LocalDateTime> getLatestTimestamp() {
        LocalDateTime earliest = null;
        for (Metric metric : getMetrics()) {
            Optional<LocalDateTime> timestamp = getEarliestTimestamp(metric);
            if (timestamp.isPresent()) {
                earliest = ObjectUtils.compare(timestamp.get(), earliest) < 0 ? timestamp.get() : earliest;
            }
        }
        return Optional.ofNullable(earliest);
    }

    @Override
    public Optional<LocalDateTime> getLatestTimestamp(Metric metric) {
        requireNonNull(metric);
        return get(metric).getLast().map(Value::atLocalTime);
    }

    @Override
    public void add(SeriesStore store) {
        requireNonNull(store);
        add(Collections.singleton(store), false);
    }

    @Override
    public void add(Collection<SeriesStore> seriesStores, boolean average) {
        requireNonNull(seriesStores);
        Set<Metric> metrics = seriesStores.isEmpty() ? Collections.emptySet() : seriesStores.iterator().next().getMetrics();
        if (metrics.isEmpty()) return;
        List<SeriesStore> sortedSeriesStores = new ArrayList<>(seriesStores);
        sortedSeriesStores.sort(Comparator.naturalOrder());
        for (Metric metric : metrics) {
            Series targetSeries = get(metric);
            for (SeriesStore sortedSeriesStore : sortedSeriesStores) {
                Series sourceSeries = sortedSeriesStore.get(metric);
                if (average) {
                    targetSeries.addAverage(sourceSeries);
                } else {
                    targetSeries.add(sourceSeries);
                }
            }
        }
    }

    @Override
    public int compareTo(SeriesStore o) {
        return ObjectUtils.compare(getEarliestTimestamp(), o.getEarliestTimestamp());
    }

    protected final Value adaptValue(Metric metric, Value value) {
        if (metric.getType() == Metric.Type.COUNTER) {
            Value previousValue = lastValues.get(metric);
            try {
                if (previousValue == null) return null;
                float newValue = value.asFloat();
                float prevValue = previousValue.asFloat();
                if (newValue >= prevValue) {
                    return Value.create(value.getTimestamp(), newValue - prevValue);
                } else {
                    return null;
                }
            } finally {
                lastValues.put(metric, value);
            }
        } else {
            return value;
        }
    }

}
