package net.microfalx.metrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all series stores.
 */
public abstract class AbstractSeriesStore implements SeriesStore {

    private volatile Duration retention = ofMinutes(15);
    private final Map<Metric, Value> lastValues = new ConcurrentSkipListMap<>();

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
