package me.jetby.treexclans.api.addons.exception;

/**
 * Thrown when an addon declares missing dependencies.
 */
public class MissingDependencyException extends AddonException {
    public MissingDependencyException(String message) {
        super(message);
    }
}
