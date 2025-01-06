package net.microfalx.metrics;

import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochMilli;

/**
 * A class which holds a numeric value at a point in time.
 */
public class Value implements Timestampable<Instant> {

    private static final long MILLIS_SINCE_2020 = LocalDateTime.of(2024, 1, 1, 0, 0)
            .toInstant(ZoneOffset.UTC).toEpochMilli();

    final int timestamp;
    final float value;

    /**
     * Creates an instance with the current timestamp and a value of zero.
     *
     * @return non-null instance
     */
    public static Value zero() {
        return create(currentTimeMillis(), 0);
    }

    /**
     * Creates an instance with the current timestamp with a given value.
     *
     * @return non-null instance
     */
    public static Value create(float value) {
        return create(currentTimeMillis(), value);
    }

    /**
     * Creates an instance with a timestamp and a value.
     *
     * @return non-null instance
     */
    public static Value create(LocalDateTime timestamp, float value) {
        return new Value(TimeUtils.toMillis(timestamp), value);
    }

    /**
     * Creates an instance with a timestamp and a value.
     *
     * @return non-null instance
     */
    public static Value create(long timestamp, float value) {
        return new Value(timestamp, value);
    }

    Value(long timestamp, float value) {
        this.timestamp = timestamp > MILLIS_SINCE_2020 ? (int) (timestamp - MILLIS_SINCE_2020) : (int) timestamp;
        this.value = value;
    }

    Value(int timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public Instant getCreatedAt() {
        return Instant.ofEpochMilli(getTimestamp());
    }

    /**
     * Returns the timestamp.
     *
     * @return millis since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * Returns the timestamp as an instance.
     *
     * @return a non-null instance
     */
    public Instant atInstant() {
        return ofEpochMilli(timestamp);
    }

    /**
     * Returns the timestamp as a date/time.
     *
     * @return a non-null instance
     */
    public LocalDateTime atDateTime() {
        return TimeUtils.toLocalDateTime(timestamp);
    }

    /**
     * Returns the value as a double type.
     *
     * @return the value
     */
    public double asDouble() {
        return value;
    }

    /**
     * Returns the value as a float type.
     *
     * @return the value
     */
    public float asFloat() {
        return (float) value;
    }

    /**
     * Returns the value as a long type.
     *
     * @return the value
     */
    public long asLong() {
        return (long) value;
    }

    /**
     * Returns the value as an int type.
     *
     * @return the value
     */
    public int asInt() {
        return (int) value;
    }

    /**
     * Creates a new value object and adds the value.
     *
     * @param value the value to add
     * @return a new instance
     */
    public Value add(float value) {
        return new Value(timestamp, this.value + value);
    }

    @Override
    public String toString() {
        return TimeUtils.toZonedDateTime(timestamp) + "=" + value;
    }
}
