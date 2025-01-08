package net.microfalx.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SqliteSeriesStoreTest {

    private SeriesStore store;
    private final Metric metric1 = Metric.create("m1");
    private final Metric metric2 = Metric.create("m1");

    @BeforeEach
    void setup() {
        store = new SqliteSeriesStore("metrics_" + System.currentTimeMillis());
    }

    @Test
    void init() {
        assertNotNull(store.get(metric1));
    }

}