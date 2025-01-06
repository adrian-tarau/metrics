package net.microfalx.metrics;

import net.microfalx.lang.StringUtils;

/**
 * Base class for all series.
 */
abstract class AbstractSeries implements Series {

    private final String id = MetricUtils.nextId("series");
    private final String name;

    public AbstractSeries(String name) {
        name = StringUtils.defaultIfNull(name, StringUtils.EMPTY_STRING);
        this.name = name;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }
}
