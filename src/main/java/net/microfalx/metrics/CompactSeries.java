package net.microfalx.metrics;

import net.microfalx.lang.ArgumentUtils;
import net.microfalx.lang.ExceptionUtils;

import java.io.*;
import java.util.*;

import static net.microfalx.lang.ArgumentUtils.requireBounded;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "OptionalAssignedToNull"})
public class CompactSeries extends AbstractSeries {

    private static final int HEADER_SIZE = 4;
    private static final int VALUE_SIZE = 8;
    private static final byte[] SIGNATURE = {(byte) 0xA3, (byte) 0x98};

    private final byte[] data;

    private OptionalDouble average;
    private OptionalDouble minimum;
    private OptionalDouble maximum;

    private double weight = Double.MIN_VALUE;

    CompactSeries(String name, Collection<Value> values) {
        this(name, compact(values));
    }

    CompactSeries(String name, byte[] data) {
        super(name);
        ArgumentUtils.requireNonNull(data);
        this.data = data;
    }

    @Override
    public List<Value> getValues() {
        return new ListWrapper();
    }

    @Override
    public Value get(int index) {
        requireBounded(index, 0, getCount());
        int pos = HEADER_SIZE + index * VALUE_SIZE;
        byte[] data = new byte[VALUE_SIZE];
        System.arraycopy(CompactSeries.this.data, pos, data, 0, data.length);
        InputStream is = new ByteArrayInputStream(data);
        DataInputStream din = new DataInputStream(is);
        try {
            return new Value(din.readInt(), din.readFloat());
        } catch (IOException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    @Override
    public int getCount() {
        return (data.length - HEADER_SIZE) / VALUE_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return data.length <= HEADER_SIZE;
    }

    @Override
    public Optional<Value> getFirst() {
        return isEmpty() ? Optional.empty() : Optional.of(get(0));
    }

    @Override
    public Optional<Value> getLast() {
        return isEmpty() ? Optional.empty() : Optional.of(get(getCount() - 1));

    }

    @Override
    public OptionalDouble getAverage() {
        if (average == null) {
            average = getValues().stream().mapToDouble(Value::asDouble).average();
        }
        return average;
    }

    public OptionalDouble getMinimum() {
        if (minimum == null) {
            minimum = getValues().stream().mapToDouble(Value::asDouble).min();
        }
        return minimum;
    }

    public OptionalDouble getMaximum() {
        if (maximum == null) {
            maximum = getValues().stream().mapToDouble(Value::asDouble).max();
        }
        return maximum;
    }

    @Override
    public double getWeight() {
        if (weight == Double.MIN_VALUE) {
            weight = getMaximum().orElse(0) / getCount();
        }
        return weight;
    }

    @Override
    public Series add(Value value) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(buffer);
        try {
            buffer.write(data);
            write(dout, value);
            return new CompactSeries(getName(), buffer.toByteArray());
        } catch (IOException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    @Override
    public Series compact() {
        return this;
    }

    static byte[] compact(Collection<Value> values) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(buffer);
        try {
            dout.write(SIGNATURE);
            dout.writeShort(values.size());
            for (Value value : values) {
                write(dout, value);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            return ExceptionUtils.throwException(e);
        }
    }

    static void write(DataOutputStream dout, Value value) throws IOException {
        dout.writeInt(value.timestamp);
        dout.writeFloat(value.value);
    }

    class ListWrapper extends AbstractList<Value> {

        @Override
        public Value get(int index) {
            return CompactSeries.this.get(index);
        }

        @Override
        public int size() {
            return CompactSeries.this.getCount();
        }
    }
}
