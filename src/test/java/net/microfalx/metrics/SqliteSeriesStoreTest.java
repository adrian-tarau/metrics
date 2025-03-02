package net.microfalx.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;

import static net.microfalx.lang.TimeUtils.fromMillis;
import static net.microfalx.lang.TimeUtils.toLocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SqliteSeriesStoreTest {

    private static final int BATCH_SIZE = 10;

    private SeriesStore store;
    private final Metric metric1 = Metric.create("m1");
    private final Metric metric2 = Metric.create("m2");

    @BeforeEach
    void setup() {
        store = new SqliteSeriesStore("metrics_" + System.currentTimeMillis());
    }

    @Test
    void init() {
        assertNotNull(store.get(metric1));
    }

    @Test
    void add() {
        store.add(metric1, Value.create(0));
    }

    @Test
    void getMetrics() {
        addBatch();
        Set<Metric> metrics = store.getMetrics();
        assertEquals(2, metrics.size());
    }

    @Test
    void get() {
        long timestamp = addBatch();
        Series series = store.get(metric1);
        assertNotNull(series);
        assertEquals(10, series.getCount());
        assertEquals(0, series.getMinimum().orElse(0), 0.1);
        assertEquals(9, series.getMaximum().orElse(0), 0.1);
        assertEquals(4.5, series.getAverage().orElse(0), 0.1);
        assertEquals(timestamp, series.getFirst().orElse(null).getTimestamp());
    }

    @Test
    void getInterval() {
        long timestamp = addBatch();
        Series series = store.get(metric1, fromMillis(timestamp + 5), fromMillis(timestamp + 20));
        assertNotNull(series);
        assertEquals(5, series.getCount());
        assertEquals(5, series.getMinimum().orElse(0), 0.1);
        assertEquals(9, series.getMaximum().orElse(0), 0.1);
        assertEquals(7, series.getAverage().orElse(0), 0.1);
        assertEquals(timestamp + 5, series.getFirst().orElse(null).getTimestamp());
    }

    @Test
    void getAverage() {
        long timestamp = addBatch();
        OptionalDouble average = store.getAverage(metric1, fromMillis(timestamp + 5), fromMillis(timestamp + 20));
        assertEquals(7, average.orElse(0));
    }

    @Test
    void getEarliestTimestamp() {
        long timestamp = addBatch();
        Optional<LocalDateTime> earliestTimestamp = store.getEarliestTimestamp(metric1);
        assertEquals(toLocalDateTime(timestamp), earliestTimestamp.orElse(null));
    }

    @Test
    void getLatestTimestamp() {
        long timestamp = addBatch();
        Optional<LocalDateTime> latestTimestamp = store.getLatestTimestamp(metric1);
        assertEquals(toLocalDateTime(timestamp + BATCH_SIZE - 1), latestTimestamp.orElse(null));
    }

    @Test
    void batch() {
        addBatch();
    }

    @Test
    void clear() {
        addBatch(5);
        store.clear();
    }

    private long addBatch() {
        return addBatch(BATCH_SIZE);
    }

    private long addBatch(int size) {
        long initialTimestamp = System.currentTimeMillis();
        long timestamp = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            Batch batch = Batch.create(timestamp);
            batch.add(metric1, i);
            batch.add(metric2, i);
            store.add(batch);
            timestamp++;
        }
        return initialTimestamp;
    }

}