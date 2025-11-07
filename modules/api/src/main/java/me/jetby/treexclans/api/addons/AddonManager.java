package me.jetby.treexclans.api.addons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

/**
 * Public API for interacting with TreexClans Addon System.
 * <p>
 * Provides methods for loading, enabling, disabling, and unloading addons dynamically.
 */
public interface AddonManager {

    /**
     * Loads all addon JARs from the addons folder.
     */
    void loadAddons();

    /**
     * Loads a single addon JAR (does not enable it immediately).
     *
     * @param jarFile The path to the addon JAR file.
     * @throws Exception If loading or scanning fails.
     */
    void loadAddon(@NotNull File jarFile) throws Exception;

    /**
     * Enables all loaded addons in dependency order.
     */
    void enableAll();

    /**
     * Enables a specific addon by ID.
     *
     * @param addonId The addon ID.
     * @return true if successfully enabled.
     */
    boolean enableAddon(@NotNull String addonId);

    /**
     * Disables and unloads all addons (reverse order).
     */
    void unloadAll();

    /**
     * Disables a specific addon (invokes onDisable).
     *
     * @param addon The addon instance to disable.
     * @return true if successfully disabled.
     */
    boolean disable(@NotNull JavaAddon addon);

    /**
     * Unloads a specific addon by its ID.
     *
     * @param addonId The addon ID.
     * @return true if successfully unloaded.
     */
    boolean unloadAddon(@NotNull String addonId);

    /**
     * Retrieves a loaded addon by its ID.
     *
     * @param addonId The addon ID.
     * @return The addon instance or null if not found.
     */
    @Nullable
    JavaAddon getAddon(@NotNull String addonId);

    /**
     * Checks if an addon with the specified ID is currently loaded.
     *
     * @param addonId The addon ID.
     * @return true if the addon is loaded.
     */
    boolean isLoaded(@NotNull String addonId);

    /**
     * @return An immutable list of all loaded addon IDs.
     */
    @NotNull
    List<String> getAddonIds();
}
