package net.microfalx.metrics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ValueSerializer extends Serializer<Value> {

    @Override
    public void write(Kryo kryo, Output output, Value object) {
        output.writeLong(object.getTimestamp());
        output.writeFloat(object.getValue());
    }

    @Override
    public Value read(Kryo kryo, Input input, Class<? extends Value> type) {
        return Value.create(input.readLong(), input.readFloat());
    }

}
