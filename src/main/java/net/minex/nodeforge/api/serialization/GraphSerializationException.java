package net.minex.nodeforge.api.serialization;

/**
 * Exception thrown when serializing, deserializing, or validating graph persistent data fails.
 */
public class GraphSerializationException extends RuntimeException {

    public GraphSerializationException(String message) {
        super(message);
    }

    public GraphSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
