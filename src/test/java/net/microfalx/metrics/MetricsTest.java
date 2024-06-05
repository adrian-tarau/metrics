package net.microfalx.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricsTest {

    private Metrics metrics = Metrics.of("Test");

    @BeforeAll
    static void registerRegistry() {
        io.micrometer.core.instrument.Metrics.globalRegistry.clear();
        io.micrometer.core.instrument.Metrics.addRegistry(new SimpleMeterRegistry());
    }

    @Test
    void addTag() {
        metrics.addTag("tag1", "1");
        assertEquals("1", metrics.getTags().get("tag1"));
        assertEquals("tag1", metrics.getTags().keySet().iterator().next());
    }

    @Test
    void withGroup() {
        assertThat(metrics.withGroup("group1").getGroup()).contains("group1");
    }

    @Test
    void withTag() {
        Metrics metricWithNewTag = metrics.withTag("tag1", "1");
        assertEquals("1", metricWithNewTag.getTags().get("tag1"));
        assertEquals("tag1", metricWithNewTag.getTags().keySet().iterator().next());
    }

    @Test
    void countWithCounterName() {
        assertEquals(1, metrics.count("test.c1"));
        assertEquals(2, metrics.count("test.c1"));
    }

    @Test
    void countWithCounterNameAndDelta() {
        assertEquals(3, metrics.count("test.c1", 1));
        assertEquals(5, metrics.count("test.c1", 2));
    }

    @Test
    void getGauge() {
        assertEquals(5, metrics.getGauge("test.g1", () -> 5d).getValue());
        assertEquals(1, metrics.getGauge("test.g2").increment());
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
        org.assertj.core.api.Assertions.assertThat(timer.getDuration().toMillis()).isGreaterThan(150).
                isLessThan(500);
    }


    @Test
    void startTimer() {
        Timer timer = metrics.startTimer("test.t1");
        assertEquals(Timer.Type.LONG, timer.getType());
        assertEquals("Test", timer.getGroup());
        assertNotNull(metrics.getTimer("test.t1").getFirstAccess());
        assertNotNull(metrics.getTimer("test.t1").getLastAccess());
        assertEquals("test.t1", metrics.getTimer("test.t1").getName());
    }


    @Test
    void touch() {
        Timer meter = metrics.getTimer("test.t1");
        assertNotNull(metrics.touch(meter).getFirstAccess());
        assertNotNull(metrics.touch(meter).getLastAccess());
        assertEquals("Test", metrics.touch(meter).getGroup());
        assertEquals("test.t1", metrics.touch(meter).getName());
    }

    private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}