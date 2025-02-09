package net.microfalx.metrics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.ImmutableCollectionsSerializers;
import com.esotericsoftware.kryo.serializers.OptionalSerializers;
import com.esotericsoftware.kryo.serializers.VersionFieldSerializer;
import net.microfalx.lang.annotation.Order;
import net.microfalx.lang.annotation.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SeriesMemoryStoreTest {

    private static final LocalDateTime START = LocalDateTime.now();
    private static final LocalDateTime END = START.plusMinutes(5);
    private static final LocalDateTime MIDDLE = START.plusMinutes(2).plusSeconds(30);

    private static final int STORE_COUNT = 5;
    private static final int SERIALIZATION_ID = 1000;

    private SeriesStore store;
    private final Metric metric1 = Metric.create("g1");
    private final Metric metric2 = Metric.create("g2");
    private final Metric metric3 = Metric.create("c1").withType(Metric.Type.COUNTER);

    @BeforeEach
    void setup() {
        store = SeriesStore.create();
    }

    @Test
    void create() {
        assertSame(TestSeriesStore.class, SeriesStore.create().getClass());
    }

    @Test
    void get() {
        store.add(metric1, Value.create(LocalDateTime.now(), 1));
        Series series = store.get(metric1);
        assertEquals(1, series.getCount());
        series = store.get(metric2);
        assertEquals(0, series.getCount());
    }

    @Test
    void getWithInterval() {
        generateMetric1();
        Series series = store.get(metric1);
        assertEquals(30, series.getCount());
        series = store.get(metric1, START, MIDDLE);
        assertEquals(16, series.getCount());
    }

    @Test
    void addWithCounter() {
        store.add(metric3, Value.create(LocalDateTime.now(), 1));
        store.add(metric3, Value.create(LocalDateTime.now(), 5));
        store.add(metric3, Value.create(LocalDateTime.now(), 8));
        store.add(metric3, Value.create(LocalDateTime.now(), 3));
        store.add(metric3, Value.create(LocalDateTime.now(), 5));
        store.add(metric3, Value.create(LocalDateTime.now(), 11));
        double value = store.getAverage(metric3, Duration.ofHours(1)).orElse(0);
        assertEquals(3.75, value, 0.001);
    }

    @Test
    void add() {
        store.add(Collections.emptyList(), false);
        assertEquals(0, store.get(metric1).getCount());
        store.add(getStores(), false);
        Series series = store.get(metric1);
        assertEquals(15, series.getCount());
        assertEquals(2.33, series.getAverage().getAsDouble(), 0.01);
    }

    @Test
    void addAverage() {
        store.add(getStores(), true);
        Series series = store.get(metric1);
        assertEquals(5, series.getCount());
        assertEquals(2, series.getAverage().getAsDouble(), 0.01);
    }

    @Test
    void serialize() {
        store = new SeriesMemoryStore();
        generateMetric1();
        Kryo kryo = createKryo();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Output output = new Output(buffer);
        kryo.writeObject(output, store);
        output.close();
        kryo.readObject(new Input(new ByteArrayInputStream(buffer.toByteArray())), SeriesMemoryStore.class);
        assertEquals(1, store.getMetrics().size());
        assertEquals(30, store.get(metric1).getCount());
        assertEquals(15.5, store.get(metric1).getAverage().getAsDouble(), 0.001);
    }

    private void generateMetric1() {
        LocalDateTime current = START;
        int value = 1;
        while (current.isBefore(END)) {
            store.add(metric1, Value.create(current, value++));
            current = current.plusSeconds(10);
        }
    }

    private Collection<SeriesStore> getStores() {
        Collection<SeriesStore> stores = new ArrayList<>();
        for (int i = 1; i <= STORE_COUNT; i++) {
            SeriesStore memory = SeriesStore.memory();
            stores.add(memory);
            for (int j = 0; j < i; j++) {
                memory.add(metric1, Value.create(START.plusSeconds(j), j + 1));
            }
        }
        return stores;
    }

    private Kryo createKryo() {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(VersionFieldSerializer.class);
        kryo.register(Metric.class, SERIALIZATION_ID + 50);
        kryo.register(Metric.Type.class, SERIALIZATION_ID + 51);
        kryo.register(Value.class, SERIALIZATION_ID + 52);
        kryo.register(SeriesMemoryStore.class, SERIALIZATION_ID + 53);
        kryo.register(DefaultSeries.class, SERIALIZATION_ID + 54);

        kryo.addDefaultSerializer(AtomicInteger.class, new DefaultSerializers.AtomicIntegerSerializer());
        kryo.addDefaultSerializer(AtomicLong.class, new DefaultSerializers.AtomicLongSerializer());
        kryo.addDefaultSerializer(URI.class, new DefaultSerializers.URISerializer());
        kryo.addDefaultSerializer(Optional.class, new OptionalSerializers.OptionalSerializer());
        kryo.addDefaultSerializer(OptionalDouble.class, new OptionalSerializers.OptionalDoubleSerializer());
        kryo.addDefaultSerializer(ConcurrentSkipListMap.class, new DefaultSerializers.ConcurrentSkipListMapSerializer());

        ImmutableCollectionsSerializers.addDefaultSerializers(kryo);
        ImmutableCollectionsSerializers.addDefaultSerializers(kryo);

        kryo.register(Duration.class, SERIALIZATION_ID + 100);
        kryo.register(ZonedDateTime.class, SERIALIZATION_ID + 101);
        kryo.register(URI.class, SERIALIZATION_ID + 102);
        kryo.register(Optional.class, SERIALIZATION_ID + 103);
        kryo.register(OptionalDouble.class, SERIALIZATION_ID + 104);

        kryo.register(ArrayList.class, SERIALIZATION_ID + 110);
        kryo.register(HashSet.class, SERIALIZATION_ID + 111);
        kryo.register(HashMap.class, SERIALIZATION_ID + 112);
        kryo.register(ConcurrentSkipListMap.class, SERIALIZATION_ID + 113);
        kryo.register(CopyOnWriteArrayList.class, SERIALIZATION_ID + 114);
        kryo.register(CopyOnWriteArraySet.class, SERIALIZATION_ID + 115);

        return kryo;
    }

    @Provider
    @Order
    public static class TestSeriesStore extends SeriesMemoryStore {

    }

}