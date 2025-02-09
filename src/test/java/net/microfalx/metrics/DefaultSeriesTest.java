package net.microfalx.metrics;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefaultSeriesTest {

    private static final long START_TIMESTAMP = System.currentTimeMillis();

    @Test
    void create() {
        Series series = new DefaultSeries("test", List.of(Value.create(1, 1), Value.create(2, 2)));
        assertEquals(2, series.getCount());
        assertFalse(series.isEmpty());
    }

    @Test
    void random() {
        Series series = DefaultSeries.random("test", LocalDateTime.now(), Duration.ofSeconds(60), 10, 0, 10);
        assertEquals(10, series.getCount());
    }

    @Test
    void values() {
        Series series = createDefault();
        assertEquals(1, series.getFirst().get().getValue());
        assertEquals(10, series.getLast().get().getValue());
        assertEquals(1, series.get(0).getValue());
        assertEquals(5, series.get(2).getValue());
    }

    @Test
    void valuesEmpty() {
        Series series = new DefaultSeries("test", List.of());
        assertEquals(true, series.getFirst().isEmpty());
        assertEquals(true, series.getLast().isEmpty());
    }

    @Test
    void metrics() {
        Series series = createDefault();
        assertEquals(4, series.getCount());
        assertEquals(1, series.getMinimum().getAsDouble(), 0.001);
        assertEquals(1, series.getMinimum().getAsDouble(), 0.001);
        assertEquals(4.5, series.getAverage().getAsDouble(), 0.001);
        assertEquals(4.5, series.getAverage().getAsDouble(), 0.001);
        assertEquals(10, series.getMaximum().getAsDouble(), 0.001);
        assertEquals(10, series.getMaximum().getAsDouble(), 0.001);
    }

    @Test
    public void add() {
        Series series = createDefault();
        assertEquals(4, series.getCount());
        series = series.add(Value.create(5, 15));
        assertEquals(5, series.getCount());
        assertEquals(1, series.getMinimum().getAsDouble(), 0.001);
        assertEquals(6.6, series.getAverage().getAsDouble(), 0.001);
        assertEquals(15, series.getMaximum().getAsDouble(), 0.001);
    }

    @Test
    public void addSeries() {
        Series target = createDefault();
        assertEquals(4, target.getCount());
        Series source = createDefault();
        assertEquals(4.5, source.getAverage().getAsDouble(), 0.001);
        target.add(source);
        assertEquals(8, target.getCount());
        assertEquals(4.5, target.getAverage().getAsDouble(), 0.001);
    }

    @Test
    public void addAverageSeries() {
        Series target = createDefault();
        assertEquals(4, target.getCount());
        Series source = createDefault();
        assertEquals(4.5, source.getAverage().getAsDouble(), 0.001);
        target.addAverage(source);
        assertEquals(5, target.getCount());
        assertEquals(4.5, target.getAverage().getAsDouble(), 0.001);
    }

    @Test
    public void weight() {
        Series series = createDefault();
        assertEquals(2.5, series.getWeight(), 0.001);
    }

    @Test
    public void compact() {
        Series series = createDefault();
        series = series.compact();
        assertEquals(4, series.getCount());
    }

    private DefaultSeries createDefault() {
        return new DefaultSeries("test", List.of(Value.create(START_TIMESTAMP, 1),
                Value.create(START_TIMESTAMP + 2, 2), Value.create(START_TIMESTAMP + 3, 5),
                Value.create(START_TIMESTAMP + 4, 10)));
    }

}