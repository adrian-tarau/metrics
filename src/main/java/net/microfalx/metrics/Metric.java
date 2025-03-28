package net.microfalx.metrics;

import com.esotericsoftware.kryo.DefaultSerializer;
import net.microfalx.lang.Hashing;
import net.microfalx.lang.NamedIdentityAware;
import net.microfalx.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableCollection;
import static net.microfalx.lang.ArgumentUtils.requireNonNull;
import static net.microfalx.lang.ArgumentUtils.requireNotEmpty;
import static net.microfalx.lang.StringUtils.capitalizeWords;
import static net.microfalx.lang.StringUtils.toIdentifier;

/**
 * A metric and its labels (dimensions).
 */
@DefaultSerializer(MetricSerializer.class)
public class Metric extends NamedIdentityAware<String> implements Comparable<Metric> {

    public static String UNNAMED = "UNNAMED";
    private static final int MAX_CACHE_SIZE = 10_000;

    private String group;
    private String displayName;
    private final String hash;
    final Map<String, String> labels;
    private Type type = Type.GAUGE;

    private static final Map<String, Metric> METRIC_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates an unnamed metric with no labels.
     *
     * @return a non-null instance
     */
    public static Metric create() {
        return new Metric(UNNAMED, emptyMap(), null);
    }

    /**
     * Creates a metric without any labels.
     *
     * @param name the metric name
     * @return a non-null instance
     */
    public static Metric create(String name) {
        return new Metric(name, emptyMap(), null);
    }

    /**
     * Creates a metric with one label.
     *
     * @param name   the metric name
     * @param label1 the label name
     * @param value1 the label value
     * @return a non-null instance
     */
    public static Metric create(String name, String label1, String value1) {
        return new Metric(name, Map.of(label1, value1), null);
    }

    /**
     * Creates a metric with two label.
     *
     * @param name   the metric name
     * @param label1 the label name
     * @param value1 the label value
     * @param label2 the label name
     * @param value2 the label value
     * @return a non-null instance
     */
    public static Metric create(String name, String label1, String value1, String label2, String value2) {
        return new Metric(name, Map.of(label1, value1, label2, value2), null);
    }

    /**
     * Creates a metric with multiple labels.
     *
     * @param name   the metric name
     * @param labels the labels
     * @return a non-null instance
     */
    public static Metric create(String name, Map<String, String> labels) {
        String hash = calculateHash(name, labels);
        return new Metric(name, labels, hash);
    }

    /**
     * Creates a metric.
     * <p>
     * The method returns a cached metric if one is available.
     *
     * @param name the metric name
     * @return a non-null instance
     */
    public static Metric get(String name) {
        return get(name, emptyMap());
    }

    /**
     * Creates a metric with multiple labels.
     * <p>
     * The method returns a cached metric if one is available.
     *
     * @param name   the metric name
     * @param labels the labels
     * @return a non-null instance
     */
    public static Metric get(String name, Map<String, String> labels) {
        String hash = calculateHash(name, labels);
        if (METRIC_CACHE.size() > MAX_CACHE_SIZE) METRIC_CACHE.clear();
        return METRIC_CACHE.computeIfAbsent(hash, s -> new Metric(name, labels, hash));
    }

    protected Metric() {
        hash = calculateHash(UNNAMED, emptyMap());
        labels = emptyMap();
    }

    Metric(String name, Map<String, String> labels, String hash) {
        requireNotEmpty(name);
        setId(toIdentifier(name));
        setName(name);
        this.displayName = capitalizeWords(name);
        this.hash = hash != null ? hash : calculateHash(name, labels);
        this.labels = labels != null ? new HashMap<>(labels) : Map.of();
    }

    /**
     * Returns the type of metrics.
     *
     * @return a non-null instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Creates a new metrics with a different type.
     *
     * @param type the type
     * @return a non-null instance
     */
    public Metric withType(Type type) {
        requireNonNull(type);
        Metric copy = (Metric) copy();
        copy.type = type;
        return copy;
    }

    /**
     * Returns the group for the metric.
     *
     * @return the group, null if not group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Creates a new metrics with a different group.
     *
     * @param group the display name
     * @return a non-null instance
     */
    public Metric withGroup(String group) {
        requireNonNull(group);
        Metric copy = (Metric) copy();
        copy.group = group;
        return copy;
    }

    /**
     * Returns the display name.
     *
     * @return a non-null instance
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the full display name.
     *
     * @return a non-null instance
     */
    public String getFullDisplayName() {
        return StringUtils.isNotEmpty(group) ? group + " / " + displayName : displayName;
    }

    /**
     * Creates a new metrics with a different display name.
     *
     * @param displayName the display name
     * @return a non-null instance
     */
    public Metric withDisplayName(String displayName) {
        requireNonNull(displayName);
        Metric copy = (Metric) copy();
        copy.displayName = displayName;
        return copy;
    }

    /**
     * Returns the hash which uniquely identifies a metric and its labels.
     *
     * @return a non-null instance
     */
    public String getHash() {
        return hash;
    }

    /**
     * Returns the labels available with this metric.
     *
     * @return a non-null instance
     */
    public Collection<String> getLabels() {
        return unmodifiableCollection(labels.keySet());
    }

    /**
     * Returns a label with a given name from this metric.
     *
     * @param name the name of the label
     * @return the label value, null if such label does not exist
     */
    public String getLabel(String name) {
        requireNonNull(name);
        return labels.get(name);
    }

    /**
     * Returns whether the label exists with this metric.
     *
     * @param name the name of the label
     * @return <code>true</code> if exists, <code>false</code> otherwise
     */
    public boolean hasLabel(String name) {
        requireNonNull(name);
        return labels.containsKey(name);
    }

    @Override
    public int compareTo(Metric o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return Objects.equals(hash, metric.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Metric.class.getSimpleName() + "[", "]")
                .add("id='" + getId() + "'")
                .add("name='" + getName() + "'")
                .add("group='" + group + "'")
                .add("displayName='" + displayName + "'")
                .add("hash='" + hash + "'")
                .add("labels=" + labels)
                .toString();
    }

    void update(Type type, String group, String displayName, String description) {
        this.type = type;
        setDescription(description);
        this.group = group;
        this.displayName = displayName;
    }

    private static String calculateHash(String name, Map<String, String> labels) {
        Hashing hashing = Hashing.create();
        String id = toIdentifier(name);
        hashing.update(id);
        hashing.update(labels);
        return id + "_" + hashing.asString();
    }

    public enum Type {
        COUNTER,
        GAUGE
    }
}
