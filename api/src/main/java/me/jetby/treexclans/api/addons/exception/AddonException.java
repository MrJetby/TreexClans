package me.jetby.treexclans.api.addons.exception;

/**
 * Base class for all addon-related exceptions.
 */
public class AddonException extends Exception {
    public AddonException(String message) {
        super(message);
    }

    public AddonException(String message, Throwable cause) {
        super(message, cause);
    }
}
