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
     * Returns the last started timer for the current thread.
     * <p>
     * If a timer does not exist, one will be returned with duration 0.
     *
     * @return a non-null instance
     */
    static Timer last() {
        Timer timer = Metrics.LAST.get();
        if (timer == null) Metrics.SYSTEM.getTimer("na");
        return timer;
    }

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
    <T> T recordCallable(Callable<T> callable);

    /**
     * Times a block of code.
     *
     * @param runnable the runnable
     */
    void record(Runnable runnable);

    /**
     * Wrap a {@link Runnable} so that it is timed when invoked.
     *
     * @param f the Runnable to time when it is invoked.
     * @return The wrapped Runnable.
     */
    Runnable wrap(Runnable f);

    /**
     * Wrap a {@link Callable} so that it is timed when invoked.
     *
     * @param f   The Callable to time when it is invoked.
     * @param <T> The return type of the callable.
     * @return The wrapped callable.
     */
    <T> Callable<T> wrap(Callable<T> f);

    /**
     * Wrap a {@link Supplier} so that it is timed when invoked.
     *
     * @param f   The {@code Supplier} to time when it is invoked.
     * @param <T> The return type of the {@code Supplier} result.
     * @return The wrapped supplier.
     * @since 1.2.0
     */
    <T> Supplier<T> wrap(Supplier<T> f);

    /**
     * Returns the total duration.
     *
     * @return a non-null instance
     */
    Duration getDuration();

    /**
     * Returns the number of times this timer was invoked.
     *
     * @return a positive long
     */
    long getCount();

    /**
     * Returns the average duration across invocations.
     *
     * @return a non-null instance
     */
    Duration getAverageDuration();

    /**
     * Returns the minimum duration across invocations.
     *
     * @return a non-null instance
     */
    Duration getMinimumDuration();

    /**
     * Returns the maximum duration across invocations.
     *
     * @return a non-null instance
     */
    Duration getMaximumDuration();

    @Override
    void close();

    enum Type {
        SHORT,
        LONG
    }
}
