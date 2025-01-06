package net.microfalx.metrics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CompactSeriesTest {

    @Test
    void create() {
        CompactSeries series = createCompact();
        assertFalse(series.isEmpty());
        assertEquals(4, series.getCount());
    }

    @Test
    void values() {
        CompactSeries series = createCompact();
        assertEquals(1, series.getFirst().get().getValue());
        assertEquals(1, series.getFirst().get().getValue());
        assertEquals(10, series.getLast().get().getValue());
        assertEquals(10, series.getLast().get().getValue());
        assertEquals(1, series.get(0).getValue());
        assertEquals(1, series.getValues().get(0).getValue());
        assertEquals(5, series.get(2).getValue());
        assertEquals(5, series.getValues().get(2).getValue());
    }

    @Test
    void valuesEmpty() {
        Series series = new CompactSeries("test", List.of());
        assertEquals(true, series.getFirst().isEmpty());
        assertEquals(true, series.getLast().isEmpty());
    }

    @Test
    void metrics() {
        Series series = createCompact();
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
        Series series = createCompact();
        assertEquals(4, series.getCount());
        series = series.add(Value.create(5, 15));
        assertEquals(5, series.getCount());
        assertEquals(1, series.getMinimum().getAsDouble(), 0.001);
        assertEquals(6.6, series.getAverage().getAsDouble(), 0.001);
        assertEquals(15, series.getMaximum().getAsDouble(), 0.001);
    }

    @Test
    public void weight() {
        Series series = createCompact();
        assertEquals(2.5, series.getWeight(), 0.001);
    }

    @Test
    public void compact() {
        Series series = createCompact();
        series = series.compact();
        assertEquals(4, series.getCount());
    }

    private CompactSeries createCompact() {
        return new CompactSeries("test", createDefault().getValues());
    }

    private DefaultSeries createDefault() {
        return new DefaultSeries("test", List.of(Value.create(1, 1),
                Value.create(2, 2), Value.create(3, 5),
                Value.create(4, 10)));
    }

}