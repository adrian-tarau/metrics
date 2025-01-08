package net.microfalx.metrics;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueTest {

    private static long millis = System.currentTimeMillis();
    private static Instant instant = Instant.ofEpochMilli(millis);

    @Test
    void timeReference() {
        Value value = Value.create(millis, 1);
        assertEquals(instant, value.atInstant());
        assertEquals(instant.atZone(ZoneId.systemDefault()).toLocalDateTime(), value.atLocalTime());
        assertEquals(instant.atZone(ZoneOffset.UTC), value.atZonedTime());
    }

    @Test
    void isWithin() {
        Value value = Value.create(millis, 1);
        assertTrue(value.isWithin(instant.minusSeconds(1),instant.plusSeconds(1)));
    }

}