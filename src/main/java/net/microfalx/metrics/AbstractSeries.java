package net.microfalx.metrics;

import net.microfalx.lang.NamedIdentityAware;

import java.time.Duration;
import java.util.OptionalDouble;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.time.Duration.ofMinutes;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.StringUtils.EMPTY_STRING;
import static net.microfalx.lang.StringUtils.defaultIfNull;

/**
 * Base class for all series.
 */
abstract class AbstractSeries extends NamedIdentityAware<String> implements Series {

    private volatile Duration retention = ofMinutes(15);

    protected final transient ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final transient Lock rlock = lock.readLock();
    protected final transient Lock wlock = lock.writeLock();

    public AbstractSeries(String name) {
        setId(MetricUtils.nextId("series"));
        name = defaultIfNull(name, EMPTY_STRING);
        setName(name);
    }

    @Override
    public final Duration getRetention() {
        return retention;
    }

    @Override
    public final Series setRetention(Duration retention) {
        requireNonNull(retention);
        this.retention = retention;
        return this;
    }

    @Override
    public Series add(Series series) {
        requireNonNull(series);
        for (Value value : series.getValues()) {
            add(value);
        }
        afterChange();
        return this;
    }

    @Override
    public Series addAverage(Series series) {
        requireNonNull(series);
        OptionalDouble average = series.getAverage();
        if (average.isPresent()) {
            long timestamp = series.getFirst().get().getTimestamp();
            add(Value.create(timestamp, (float) average.getAsDouble()));
        }
        afterChange();
        return this;
    }

    protected void afterChange() {
        // empty by design
    }
}
