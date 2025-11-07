package me.jetby.treexclans.api.gui;

import me.jetby.treexclans.api.TreexClansAPI;
import me.jetby.treexclans.api.service.clan.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Provides a registration and management API for custom GUI types.
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 *     <li>Registering and unregistering GUI factories</li>
 *     <li>Creating GUI instances by their unique type identifiers</li>
 *     <li>Providing access to the internal registry of all GUI types</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyCustomGui extends Gui {
 *     public MyCustomGui(TreexClans plugin, Menu menu, Player player, Clan clan) {
 *         super(plugin, menu, player, clan);
 *         registerButtons();
 *     }
 * }
 *
 * guiService.registerGui("MY_GUI", (plugin, menu, player, clan, args) ->
 *     new MyCustomGui(plugin, menu, player, clan)
 * );
 * }</pre>
 */
public interface GuiService {

    /**
     * Registers a new custom GUI type factory.
     *
     * @param guiType the GUI type identifier (case-insensitive, must be unique)
     * @param factory the {@link IGuiFactory} responsible for creating instances of that GUI type
     * @throws IllegalArgumentException if a GUI type with the same name already exists
     */
    void registerGui(@NotNull String guiType, @NotNull IGuiFactory factory);

    /**
     * Removes a previously registered GUI type.
     *
     * @param guiType the type identifier to remove
     */
    void unregisterGui(@NotNull String guiType);

    /**
     * Checks whether a GUI type is registered.
     *
     * @param guiType the GUI type identifier
     * @return {@code true} if the type is registered, {@code false} otherwise
     */
    boolean isGuiRegistered(@NotNull String guiType);

    /**
     * Retrieves the factory associated with a given GUI type.
     *
     * @param guiType the GUI type identifier
     * @return the associated {@link IGuiFactory}, or {@code null} if none exists
     */
    @Nullable
    IGuiFactory getGuiFactory(@NotNull String guiType);

    /**
     * Creates a GUI instance from a registered factory.
     *
     * @param guiType       the GUI type identifier
     * @param plugin        the plugin instance
     * @param menu          the menu configuration
     * @param player        the player viewing the GUI
     * @param clan          the player's clan (nullable if not in a clan)
     * @param customObjects optional additional context objects
     * @return a new {@link Gui} instance, or {@code null} if the type is not registered
     */
    @Nullable
    Gui createGui(@NotNull String guiType,
                  @NotNull JavaPlugin plugin,
                  @NotNull Menu menu,
                  @NotNull Player player,
                  @Nullable Clan clan,
                  Object... customObjects);

    /**
     * Returns an immutable map of all currently registered GUI types and their factories.
     *
     * @return a map of GUI type → factory
     */
    @NotNull
    Map<String, IGuiFactory> getRegisteredGuis();

    /**
     * Returns the total number of registered GUI types.
     *
     * @return the number of registered GUI types
     */
    int getRegisteredGuiCount();

    /**
     * Represents a factory for creating {@link Gui} instances.
     * <p>
     * Each GUI type should define its own factory that describes
     * how to construct a new instance of its interface.
     */
    @FunctionalInterface
    interface IGuiFactory {

        /**
         * Creates a new {@link Gui} instance.
         *
         * @param plugin        the plugin instance
         * @param menu          the menu configuration
         * @param player        the player viewing the GUI
         * @param clan          the player's clan (nullable)
         * @param customObjects additional custom arguments or context
         * @return a new {@link Gui} instance
         */
        @NotNull
        Gui create(@NotNull JavaPlugin plugin,
                   @NotNull Menu menu,
                   @NotNull Player player,
                   @Nullable Clan clan,
                   Object... customObjects);
    }
}
