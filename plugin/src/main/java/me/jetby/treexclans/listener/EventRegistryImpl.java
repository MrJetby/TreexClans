package me.jetby.treexclans.listener;

import me.jetby.treexclans.api.addons.JavaAddon;
import me.jetby.treexclans.api.addons.listener.EventRegistrar;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of {@link EventRegistrar}.
 *
 * <p>
 * Handles automatic registration and cleanup of multiple event listeners,
 * keeping track of which addon owns which listener. This allows safe
 * re-registration and unloading per addon basis.
 * </p>
 */
public class EventRegistryImpl implements EventRegistrar {

    /** Stores all listeners grouped by owning addon. */
    private final Map<JavaAddon, Set<Listener>> addonListeners = new HashMap<>();

    /**
     * Registers one or more listeners for the specified addon.
     *
     * <p>
     * Each addon should register its own listeners through this method.
     * They are tracked internally to allow safe cleanup later.
     * </p>
     *
     * @param addon     the addon owning the listeners
     * @param listeners the listener instances to register
     */
    @Override
    public void register(@NotNull JavaAddon addon, Listener... listeners) {
        var manager = Bukkit.getPluginManager();
        var set = addonListeners.computeIfAbsent(addon, a -> new HashSet<>());

        for (Listener listener : listeners) {
            manager.registerEvents(listener, addon.getServiceManager().getPlugin());
            set.add(listener);
        }
    }

    /**
     * Unregisters all listeners that belong to the given addon.
     *
     * <p>
     * This is typically called when the addon is disabled or reloaded.
     * </p>
     *
     * @param addon the addon whose listeners should be unregistered
     */
    @Override
    public void unregister(@NotNull JavaAddon addon) {
        var set = addonListeners.remove(addon);
        if (set == null || set.isEmpty()) return;

        for (Listener listener : set) {
            HandlerList.unregisterAll(listener);
        }
    }

    /**
     * Unregisters all listeners for all addons.
     *
     * <p>
     * Useful when shutting down the entire system, e.g. during
     * plugin disable or server stop.
     * </p>
     */
    @Override
    public void unregisterAll() {
        for (var set : addonListeners.values()) {
            for (Listener listener : set) {
                HandlerList.unregisterAll(listener);
            }
        }
        addonListeners.clear();
    }
}
