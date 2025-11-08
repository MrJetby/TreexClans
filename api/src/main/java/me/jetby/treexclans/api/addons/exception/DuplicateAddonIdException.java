package me.jetby.treexclans.api.addons.exception;

/**
 * Thrown when another addon with the same ID is already loaded.
 */
public class DuplicateAddonIdException extends AddonException {
    public DuplicateAddonIdException(String message) {
        super(message);
    }
}
