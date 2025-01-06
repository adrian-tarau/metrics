package net.microfalx.metrics;

import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SeriesMemoryStoreTest {

    private static final LocalDateTime START = LocalDateTime.now();
    private static final LocalDateTime END = START.plusMinutes(5);
    private static final LocalDateTime MIDDLE = END.minusMinutes(2);

    private SeriesStore store;
    private Metric metric1 = Metric.create("m1");
    private Metric metric2 = Metric.create("m2");

    @BeforeEach
    void setup() {
        store = SeriesStore.create();
    }

    @Test
    void create() {
        assertSame(TestSeriesStore.class, SeriesStore.create().getClass());
    }

    @Test
    void get() {
        store.add(metric1, Value.create(LocalDateTime.now(), 1));
        Series series = store.get(metric1);
        assertEquals(1, series.getCount());
        series = store.get(metric2);
        assertEquals(0, series.getCount());
    }

    @Test
    void getWithInterval() {
        generateMetric1();
        Series series = store.get(metric1);
        assertEquals(30, series.getCount());
        series = store.get(metric1, START, MIDDLE);
        assertEquals(0, series.getCount());
    }

    private void generateMetric1() {
        LocalDateTime current = START;
        while (current.isBefore(END)) {
            store.add(metric1, Value.create(current, 1));
            current = current.plusSeconds(10);
        }
    }

    @Provider
    @Order
    public static class TestSeriesStore extends SeriesMemoryStore {

    }

}