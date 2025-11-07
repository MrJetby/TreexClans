package me.jetby.treexclans.api.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.gui.Gui;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * API for registering and managing custom GUI types.
 * <p>
 * Example usage:
 * <pre>
 * public class MyCustomGui extends Gui {
 *     public MyCustomGui(TreexClans plugin, Menu menu, Player player, Clan clan) {
 *         super(plugin, menu, player, clan);
 *         registerButtons();
 *     }
 * }
 *
 * // In your plugin initialization class:
 * GuiApi.registerGui("my_gui_type", (plugin, menu, player, clan, objects) ->
 *     new MyCustomGui(plugin, menu, player, clan)
 * );
 * </pre>
 */
@UtilityClass
public class GuiApi {

    private final Map<String, IGuiFactory> registeredGuis = new HashMap<>();

    /**
     * Registers a new custom GUI type.
     *
     * @param guiType the GUI type (unique identifier)
     * @param factory the factory used to create GUI instances
     *                <p>
     *                Example:
     *                <pre>
     *                GuiApi.registerGui("my_custom", (plugin, menu, player, clan, args) ->
     *                    new MyCustomGui(plugin, menu, player, clan)
     *                );
     *                </pre>
     */
    public void registerGui(@NotNull String guiType, @NotNull IGuiFactory factory) {
        if (registeredGuis.containsKey(guiType.toUpperCase())) {
            return;
        }
        registeredGuis.put(guiType.toUpperCase(), factory);
    }

    /**
     * Unregisters a custom GUI type.
     *
     * @param guiType the GUI type to remove
     */
    public void unregisterGui(@NotNull String guiType) {
        registeredGuis.remove(guiType.toUpperCase());
    }

    /**
     * Checks whether a GUI type is registered.
     *
     * @param guiType the GUI type
     * @return true if the type is registered
     */
    public boolean isGuiRegistered(@NotNull String guiType) {
        return registeredGuis.containsKey(guiType.toUpperCase());
    }

    /**
     * Gets the factory used to create a GUI by type.
     *
     * @param guiType the GUI type
     * @return the factory, or null if not found
     */
    public IGuiFactory getGuiFactory(@NotNull String guiType) {
        return registeredGuis.get(guiType.toUpperCase());
    }

    /**
     * Creates a GUI instance by type.
     *
     * @param guiType       the GUI type
     * @param plugin        the plugin instance
     * @param menu          the menu configuration
     * @param player        the player
     * @param clanImpl          the player's clan
     * @param customObjects additional objects to pass (optional)
     * @return the GUI instance, or null if the type is not registered
     */
    public Gui createGui(@NotNull String guiType,
                         TreexClans plugin,
                         me.jetby.treexclans.gui.Menu menu,
                         Player player,
                         Clan clanImpl,
                         Object... customObjects) {
        IGuiFactory factory = getGuiFactory(guiType);
        if (factory == null) {
            return null;
        }
        return factory.create(plugin, menu, player, clanImpl, customObjects);
    }

    /**
     * Gets the number of registered GUI types.
     *
     * @return the number of registered types
     */
    public int getRegisteredGuiCount() {
        return registeredGuis.size();
    }

    /**
     * Factory interface for creating custom GUI instances.
     */
    @FunctionalInterface
    public interface IGuiFactory {
        /**
         * Creates a new GUI instance.
         *
         * @param plugin        the plugin instance
         * @param menu          the menu configuration
         * @param player        the player
         * @param clanImpl          the player's clan
         * @param customObjects additional objects (optional)
         * @return the new GUI instance
         */
        Gui create(TreexClans plugin,
                   me.jetby.treexclans.gui.Menu menu,
                   Player player,
                   Clan clanImpl,
                   Object... customObjects);
    }
}
