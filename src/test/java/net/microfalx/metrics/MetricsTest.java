package net.microfalx.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsTest {

    private Metrics metrics = Metrics.of("Test");

    @BeforeAll
    static void registerRegistry() {
        io.micrometer.core.instrument.Metrics.globalRegistry.clear();
        io.micrometer.core.instrument.Metrics.addRegistry(new SimpleMeterRegistry());
    }

    @Test
    void count() {
        metrics.count("c1");
        assertEquals(1, (long) io.micrometer.core.instrument.Metrics.counter("test.c1").count());
        metrics.count("c1");
        assertEquals(2, (long) io.micrometer.core.instrument.Metrics.counter("metrics.c1").count());
    }

    @Test
    void gauge() {
        Gauge g1 = metrics.getGauge("g1");
        g1.increment();
        assertEquals(1, g1.getValue());
        g1.decrement();
        assertEquals(0, g1.getValue());
    }

    @Test
    void timeShort() {
        Timer timer = metrics.getTimer("test.t1");
        metrics.time("t1", (t) -> sleep(200));
        //org.assertj.core.api.Assertions.assertThat(timer.getDuration().toMillis()).isGreaterThan(150).isLessThan(300);
    }

    private void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}