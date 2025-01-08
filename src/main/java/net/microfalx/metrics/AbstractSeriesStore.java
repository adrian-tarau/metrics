package net.microfalx.metrics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.OptionalDouble;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * Base class for all series stores.
 */
public abstract class AbstractSeriesStore implements SeriesStore {

    private volatile Duration retention = ofMinutes(15);

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

}
