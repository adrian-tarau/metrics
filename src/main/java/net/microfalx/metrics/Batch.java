package net.microfalx.metrics;

import net.microfalx.lang.TimeUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A batch of metrics collected at the same time and processed together.
 */
public class Batch implements Iterable<Pair<Metric, Value>> {

    private final List<Pair<Metric, Value>> metrics = new ArrayList<>();

    private final long timestamp;

    public static Batch create(long timestamp) {
        return new Batch(timestamp);
    }

    public static Batch create(LocalDateTime timestamp) {
        return new Batch(TimeUtils.toMillis(timestamp));
    }

    private Batch(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Iterator<Pair<Metric, Value>> iterator() {
        return metrics.iterator();
    }

    /**
     * Adds a new metric value.
     *
     * @param metric the metric
     * @param value  the value
     */
    public void add(Metric metric, float value) {
        requireNonNull(metric);
        requireNonNull(value);
        metrics.add(Pair.of(metric, Value.create(timestamp, value)));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Batch.class.getSimpleName() + "[", "]")
                .add("metrics=" + metrics)
                .toString();
    }
}
