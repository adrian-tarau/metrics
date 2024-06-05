package net.microfalx.metrics;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MicrometerMetricsTest {

    private static MicrometerMetrics micrometerMetrics;

    @BeforeAll
    static void initializeMicrometerMetrics() {
        micrometerMetrics = new MicrometerMetrics("Test");
    }

    @Test
    void getCounter() {
        Counter counter = micrometerMetrics.getCounter("test2.c1");
        assertEquals(1, counter.increment());
        assertEquals("test2.c1", counter.getName());
        assertEquals("Test", counter.getGroup());
        assertEquals("test.test2.c1", counter.getId());
        assertNotNull(counter.getLastAccess());
        assertNotNull(counter.getFirstAccess());
    }

    @Test
    void getGaugeWithName() {
        Gauge gauge = micrometerMetrics.getGauge("test2.g1");
        assertEquals(1, gauge.increment());
        assertEquals(0, gauge.decrement());
        assertEquals(0, gauge.getValue());
        assertEquals("Test", gauge.getGroup());
        assertEquals("test.test2.g1", gauge.getId());
        assertNotNull(gauge.getLastAccess());
        assertNotNull(gauge.getFirstAccess());
        assertEquals("test2.g1", gauge.getName());
    }

    @Test
    void getGaugeWithNameAndSupplier() {
        Gauge gauge = micrometerMetrics.getGauge("test2.g2", () -> 7d);
        assertEquals(7, gauge.increment());
        assertEquals(7, gauge.decrement());
        assertEquals(7, gauge.getValue());
        assertEquals("Test", gauge.getGroup());
        assertEquals("test.test2.g2", gauge.getId());
        assertNotNull(gauge.getLastAccess());
        assertNotNull(gauge.getFirstAccess());
        assertEquals("test2.g2", gauge.getName());
    }

    @Test
    void getTimerWithName() {
        Timer timer = micrometerMetrics.getTimer("test2.t1");
        timer.record(() -> 7d);
        assertEquals("test.test2.t1", timer.getId());
        assertEquals(1, timer.getCount());
        assertEquals(Timer.Type.SHORT, timer.getType());
        assertNotNull(timer.getMinimumDuration());
        assertNotNull(timer.getMaximumDuration());
        assertNotNull(timer.getMinimumDuration());
        assertNotNull(timer.getDuration());
        assertNotNull(timer.getFirstAccess());
        assertNotNull(timer.getLastAccess());
        assertEquals("test2.t1", timer.getName());
        assertEquals(7d, timer.record(() -> 7d));
        assertEquals(7d, timer.recordCallable(() -> 7d));
    }

    @Test
    void getTimerWithNameAndSupplier() {
        Timer timer = micrometerMetrics.getTimer("test2.t2", Timer.Type.SHORT);
        micrometerMetrics.time("test2.t2", () -> 7d);
        assertNotNull("test2.test.t2", timer.getId());
        assertEquals(1, timer.getCount());
        assertEquals(Timer.Type.SHORT, timer.getType());
        assertNotNull(timer.getMinimumDuration());
        assertNotNull(timer.getMaximumDuration());
        assertNotNull(timer.getMinimumDuration());
        assertNotNull(timer.getDuration());
        assertNotNull(timer.getFirstAccess());
        assertNotNull(timer.getLastAccess());
        assertEquals("test2.t2", timer.getName());
        assertEquals(7d, timer.record(() -> 7d));
    }
}