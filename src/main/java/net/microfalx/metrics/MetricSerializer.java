package net.microfalx.metrics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import net.microfalx.lang.EnumUtils;

import java.util.HashMap;
import java.util.Map;

public class MetricSerializer extends Serializer<Metric> {

    @Override
    public void write(Kryo kryo, Output output, Metric object) {
        output.writeString(object.getName());
        output.writeString(object.getDescription());
        output.writeString(object.getGroup());
        output.writeString(object.getDisplayName());
        output.writeString(object.getType().name());
        kryo.writeObject(output, object.labels);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Metric read(Kryo kryo, Input input, Class<? extends Metric> type) {
        String name = input.readString();
        String description = input.readString();
        String group = input.readString();
        String displayName = input.readString();
        Metric.Type mtype = EnumUtils.fromName(Metric.Type.class, input.readString(), Metric.Type.COUNTER);
        Map<String, String> labels = kryo.readObject(input, HashMap.class);
        Metric metric = new Metric(name, labels, null);
        metric.update(mtype, group, displayName, description);
        return metric;
    }
}
