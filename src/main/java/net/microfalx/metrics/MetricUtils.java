package net.microfalx.metrics;

import net.microfalx.lang.ClassUtils;
import net.microfalx.lang.IdGenerator;

import java.time.Duration;
import java.util.Collection;

import static java.time.Duration.ofSeconds;

/**
 * Various utilities around metrics.
 */
public class MetricUtils {

    public static final String GROUP_SEPARATOR = " - ";
    public static final String ID_SEPARATOR = ".";

    private static volatile SeriesStore seriesStore;

    static SeriesStore create() {
        Collection<SeriesStore> seriesStores = ClassUtils.resolveProviderInstances(SeriesStore.class);
        if (seriesStores.isEmpty()) throw new IllegalStateException("A series store implementation cannot be provided");
        return seriesStores.iterator().next();
    }

    /**
     * Returns the identifier for a metric.
     *
     * @param group the group name
     * @param name  the name
     * @return a non-null instance
     */
    public static String computeId(String group, String name) {
        return normalize(group) + ID_SEPARATOR + normalize(name);
    }

    /**
     * Normalizes a name/identifier.
     *
     * @param name the name
     * @return a normalize identifier
     */
    public static String normalize(String name) {
        if (name == null) return "na";
        name = name.toLowerCase();
        name = name.replace(' ', '_');
        return name;
    }

    /**
     * Rounds the duration at 5s, 60s or 5min, depending on the value.
     *
     * @param duration the original duration
     * @return the rounded duration
     */
    public static Duration round(Duration duration) {
        long seconds = duration.toSeconds();
        if (seconds < 60) {
            seconds = (seconds / 5) * 5;
        } else if (seconds < 300) {
            seconds = (seconds / 60) * 60;
        } else {
            seconds = (seconds / 300) * 300;
        }
        return ofSeconds(seconds);
    }

    /**
     * Returns the id generator for metrics objects.
     *
     * @return a non-null instance
     */
    static IdGenerator getIdGenerator() {
        return IdGenerator.get("metrics");
    }

    /**
     * Returns the next identifier for metrics objects.
     *
     * @param prefix the prefix
     * @return a non-null string
     */
    static String nextId(String prefix) {
        return prefix + "_" + getIdGenerator().nextAsString();
    }
}
