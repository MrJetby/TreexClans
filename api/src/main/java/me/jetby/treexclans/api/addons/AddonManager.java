package me.jetby.treexclans.api.addons;

import me.jetby.treexclans.api.addons.exception.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Public API for interacting with the TreexClans Addon System.
 * <p>
 * Provides methods for dynamically loading, enabling, disabling,
 * and managing addons at runtime.
 */
public interface AddonManager {

    /**
     * Loads a single addon JAR without enabling it.
     * <p>
     * This method scans the JAR, validates metadata, and prepares
     * an addon instance for future activation.
     *
     * @param jarFile The addon JAR file to load.
     * @return The loaded {@link JavaAddon} instance.
     * @throws AddonLoadException         If the addon could not be read or parsed.
     * @throws DuplicateAddonIdException  If another addon with the same ID already exists.
     * @throws MissingDependencyException If required dependencies are missing.
     */
    JavaAddon loadAddon(@NotNull File jarFile)
            throws AddonLoadException,
            DuplicateAddonIdException,
            MissingDependencyException,
            AddonNotFoundException;

    /**
     * Enables a specific addon instance.
     * <p>
     * Invokes {@link JavaAddon#onEnable()} and marks the addon as active.
     *
     * @param addon The addon to enable.
     * @return true if successfully enabled.
     * @throws AddonEnableException If the addon failed to initialize.
     */
    boolean enableAddon(@NotNull JavaAddon addon) throws AddonEnableException;

    /**
     * Disables all currently loaded addons in reverse load order.
     * <p>
     * Invokes {@link JavaAddon#onDisable()} for each addon and
     * performs any necessary cleanup.
     */
    void disableAddons();

    /**
     * Disables a specific addon instance.
     * <p>
     * Invokes the {@link JavaAddon#onDisable()} lifecycle method.
     *
     * @param addon The addon instance to disable.
     * @return {@code true} if successfully disabled, otherwise {@code false}.
     */
    boolean disableAddon(@NotNull JavaAddon addon);

    /**
     * Retrieves a loaded addon by its registered ID.
     *
     * @param addonId The unique addon ID.
     * @return The corresponding {@link JavaAddon} instance, or {@code null} if not found.
     */
    @Nullable
    JavaAddon getAddon(@NotNull String addonId);

    /**
     * Checks whether an addon with the specified ID is currently enabled.
     *
     * @param addonId The addon ID to check.
     * @return {@code true} if the addon is enabled, otherwise {@code false}.
     */
    boolean isAddonEnabled(@NotNull String addonId);

    /**
     * Checks whether the specified addon instance is currently enabled.
     *
     * @param addon The addon instance to check.
     * @return {@code true} if the addon is enabled, otherwise {@code false}.
     */
    boolean isAddonEnabled(@NotNull JavaAddon addon);

    /**
     * Returns an immutable list of all loaded addons.
     *
     * @return A list of currently loaded {@link JavaAddon} instances.
     */
    @NotNull
    List<JavaAddon> getAddons();
}
