package net.microfalx.metrics;

/**
 * Base class for all metrics exceptions.
 */
public class MetricException extends RuntimeException{

    public MetricException(String message) {
        super(message);
    }

    public MetricException(String message, Throwable cause) {
        super(message, cause);
    }
}
