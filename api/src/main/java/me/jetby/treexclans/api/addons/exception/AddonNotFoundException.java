package me.jetby.treexclans.api.addons.exception;

/**
 * Thrown when an addon cannot be located or identified within the JAR file.
 * <p>
 * For example, if the JAR does not contain a valid main class or @ClanAddon annotation.
 */
public class AddonNotFoundException extends AddonException {

    public AddonNotFoundException(String message) {
        super(message);
    }

    public AddonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
