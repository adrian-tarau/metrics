package net.microfalx.metrics;

/**
 * Various utilities around metrics.
 */
public class MetricsUtils {

    public static final String GROUP_SEPARATOR = " - ";
    public static final String ID_SEPARATOR = ".";

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
}
