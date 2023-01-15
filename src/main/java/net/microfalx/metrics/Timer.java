package net.microfalx.metrics;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An interface for a timer.
 */
public interface Timer extends Meter, AutoCloseable {

    /**
     * Returns the type of the timer.
     *
     * @return a non-null enum
     */
    Type getType();

    /**
     * Starts the timer.
     *
     * @return self
     */
    Timer start();

    /**
     * Stops the timer.
     *
     * @return self
     */
    Timer stop();

    /**
     * Times a block of code.
     *
     * @param supplier the supplier
     */
    <T> T record(Supplier<T> supplier);

    /**
     * Times a block of code.
     *
     * @param consumer the consumer
     */
    void record(Consumer<Timer> consumer);

    /**
     * Times a block of code.
     *
     * @param consumer the consumer
     * @param value    the value passed to the consumer
     */
    <T> void record(Consumer<T> consumer, T value);

    /**
     * Times a block of code.
     *
     * @param callable the callable
     */
    <T> T record(Callable<T> callable);

    /**
     * Times a block of code.
     *
     * @param runnable the runnable
     */
    void record(Runnable runnable);

    /**
     * Returns the total duration.
     *
     * @return a non-null instance
     */
    Duration getDuration();

    @Override
    default void close() {
        stop();
    }

    enum Type {
        SHORT,
        LONG
    }
}
