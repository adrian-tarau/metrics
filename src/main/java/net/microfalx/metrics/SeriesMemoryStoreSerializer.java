package net.microfalx.metrics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Map;

import static java.time.Duration.ofSeconds;

public class SeriesMemoryStoreSerializer extends Serializer<SeriesMemoryStore> {

    @Override
    public void write(Kryo kryo, Output output, SeriesMemoryStore object) {
        output.writeInt((int) object.getRetention().toSeconds());
        for (Map.Entry<Metric, Series> entry : object.series.entrySet()) {
            output.writeBoolean(true);
            kryo.writeObject(output, entry.getKey());
            kryo.writeObject(output, entry.getValue());
        }
        output.writeBoolean(false);
    }

    @Override
    public SeriesMemoryStore read(Kryo kryo, Input input, Class<? extends SeriesMemoryStore> type) {
        SeriesMemoryStore store = new SeriesMemoryStore();
        store.setRetention(ofSeconds(input.readInt()));
        while (input.readBoolean()) {
            Metric metric = kryo.readObject(input, Metric.class);
            Series series =  kryo.readObject(input, DefaultSeries.class);
            store.series.put(metric, series);
        }
        return store;
    }

}
