package net.microfalx.metrics;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NoMetricsTest {

    private final Metrics.NoMetrics metrics = new Metrics.NoMetrics("Test");

    @Test
    void count() {
        assertEquals(0, metrics.count("test.c1"));
    }

    @Test
    void getGauge() {
        assertEquals(0, metrics.getGauge("test.g1", () -> 5d).getValue());
        assertEquals(0, metrics.getGauge("test.g2").increment());
        assertEquals(0, metrics.getGauge("test.g2").decrement());
    }

    @Test
    void timeSupplier() {
        assertEquals(7, metrics.time("time1", () -> 7));
    }

    @Test
    void timeCallable() {
        assertEquals(7, metrics.timeCallable("time1", () -> 7));
    }

    @Test
    void timeConsumer() {
        Timer timer = metrics.getTimer("test.t1");
        metrics.time("test.t1", (t) -> sleep(200));
        assertEquals(Duration.ZERO, timer.getDuration());
    }


    @Test
    void startTimer() {
        Timer timer = metrics.startTimer("test.t1");
        assertEquals(Timer.Type.SHORT, timer.getType());
        assertEquals("Test", timer.getGroup());
        assertNotNull(metrics.getTimer("test.t1").getFirstAccess());
        assertNotNull(metrics.getTimer("test.t1").getLastAccess());
        assertEquals("test.t1", metrics.getTimer("test.t1").getName());
    }

    private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}