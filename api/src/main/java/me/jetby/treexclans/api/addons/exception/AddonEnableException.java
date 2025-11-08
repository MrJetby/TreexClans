package me.jetby.treexclans.api.addons.exception;

/**
 * Thrown when an addon fails to initialize (for example, onEnable throws an exception).
 */
public class AddonEnableException extends AddonException {
    public AddonEnableException(String message, Throwable cause) {
        super(message, cause);
    }
}
