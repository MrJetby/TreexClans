package me.jetby.treexclans.api.addons.listener;

import me.jetby.treexclans.api.addons.JavaAddon;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a unified interface for managing event listeners across addons.
 *
 * <p>
 * Supports registration and unregistration both per-addon and globally.
 * </p>
 */
public interface EventRegistrar {

    /**
     * Registers one or more listeners for the given addon.
     *
     * @param addon     the addon that owns these listeners
     * @param listeners one or more listener instances
     */
    void register(@NotNull JavaAddon addon, @NotNull Listener... listeners);

    /**
     * Unregisters all listeners associated with the given addon.
     *
     * @param addon the addon whose listeners should be removed
     */
    void unregister(@NotNull JavaAddon addon);

    /**
     * Unregisters all listeners for all addons.
     */
    void unregisterAll();
}
