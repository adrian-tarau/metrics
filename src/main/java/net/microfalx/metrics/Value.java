package net.microfalx.metrics;

import com.esotericsoftware.kryo.DefaultSerializer;
import net.microfalx.lang.TimeUtils;
import net.microfalx.lang.Timestampable;

import java.time.*;
import java.time.temporal.Temporal;

import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochMilli;

/**
 * A class which holds a numeric value at a point in time.
 */
@DefaultSerializer(ValueSerializer.class)
public class Value implements Timestampable<Instant> {

    final long timestamp;
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
     * @param value  the value as a float
     * @return non-null instance
     */
    public static Value create(float value) {
        return create(currentTimeMillis(), value);
    }

    /**
     * Creates an instance with the current timestamp with a given value.
     *
     * @param value the value as a double
     * @return non-null instance
     */
    public static Value create(double value) {
        return create(currentTimeMillis(), (float) value);
    }

    /**
     * Creates an instance with a timestamp and a value.
     *
     * @param timestamp the timestamp
     * @param value    the value
     * @return non-null instance
     */
    public static Value create(LocalDateTime timestamp, float value) {
        return new Value(TimeUtils.toMillis(timestamp), value);
    }

    /**
     * Creates an instance with a timestamp and a value.
     * @param timestamp the timestamp
     * @param value    the value
     * @return non-null instance
     */
    public static Value create(long timestamp, float value) {
        return new Value(timestamp, value);
    }

    /**
     * Creates an instance with a timestamp and a value.
     *
     * @return non-null instance
     */
    public static Value create(long timestamp, double value) {
        return new Value(timestamp, (float) value);
    }

    protected Value() {
        this(currentTimeMillis(), 0);
    }

    Value(long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public Instant getCreatedAt() {
        return ofEpochMilli(getTimestamp());
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
        return ofEpochMilli(getTimestamp());
    }

    /**
     * Returns the timestamp as a local date/time.
     *
     * @return a non-null instance
     */
    public LocalDateTime atLocalTime() {
        return atInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Returns the timestamp as a zoned date/time.
     *
     * @return a non-null instance
     */
    public ZonedDateTime atZonedTime() {
        return atInstant().atZone(ZoneOffset.UTC);
    }

    /**
     * Returns whether the value is withing a range.
     *
     * @param from the start of the interval, can be null
     * @param to   the end of the interval, can be null
     * @return {@code true} if within interval, {@code false} otherwise
     */
    public boolean isWithin(Temporal from, Temporal to) {
        long timestamp1 = getTimestamp();
        if (from != null && TimeUtils.toMillis(from) > timestamp1) return false;
        if (to != null && TimeUtils.toMillis(to) < timestamp1) return false;
        return true;
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
        return value;
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
