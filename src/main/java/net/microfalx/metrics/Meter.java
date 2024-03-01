package net.microfalx.metrics;

import net.microfalx.lang.Identifiable;

import java.time.LocalDateTime;

/**
 * A base interface for all meters.
 */
public interface Meter extends Identifiable<String> {

    /**
     * Returns the name of the meter.
     *
     * @return a non-null instance
     */
    String getName();

    /**
     * Returns the group which owns this meter.
     *
     * @return a non-null instance
     */
    String getGroup();

    /**
     * Returns the time when the meter was used first time.
     *
     * @return a non-null instance
     */
    LocalDateTime getFirstAccess();

    /**
     * Returns the time when the metter was used last time.
     *
     * @return a non-null instance
     */
    LocalDateTime getLastAccess();
}
