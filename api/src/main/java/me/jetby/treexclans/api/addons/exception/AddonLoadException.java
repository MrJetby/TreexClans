package me.jetby.treexclans.api.addons.exception;

/**
 * Thrown when an addon JAR cannot be read or parsed.
 */
public class AddonLoadException extends AddonException {
    public AddonLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
