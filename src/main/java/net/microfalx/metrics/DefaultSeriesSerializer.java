package net.microfalx.metrics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import static java.time.Duration.ofSeconds;

public class DefaultSeriesSerializer extends Serializer<DefaultSeries> {

    @Override
    public void write(Kryo kryo, Output output, DefaultSeries object) {
        output.writeString(object.getName());
        output.writeInt((int) object.getRetention().toSeconds());
        for (Value value : object.getValues()) {
            output.writeBoolean(true);
            kryo.writeObject(output, value);
        }
        output.writeBoolean(false);
    }

    @Override
    public DefaultSeries read(Kryo kryo, Input input, Class<? extends DefaultSeries> type) {
        DefaultSeries defaultSeries = new DefaultSeries(input.readString());
        defaultSeries.setRetention(ofSeconds(input.readInt()));
        while (input.readBoolean()) {
            defaultSeries.values.add(kryo.readObject(input, Value.class));
        }
        return defaultSeries;
    }

}
