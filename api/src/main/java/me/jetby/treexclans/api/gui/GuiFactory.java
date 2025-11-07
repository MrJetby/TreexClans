package me.jetby.treexclans.api.gui;

import me.jetby.treexclans.api.TreexClansAPI;
import me.jetby.treexclans.api.service.clan.Clan;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a high-level factory responsible for creating {@link Gui} instances.
 * <p>
 * This interface defines how GUIs are created — both built-in and custom ones.
 * Implementations may delegate creation to a {@link GuiService} or handle
 * built-in GUI types internally.
 * <p>
 * The factory pattern allows decoupling the GUI creation logic from
 * business logic, improving modularity and plugin extensibility.
 *
 * <pre>{@code
 * public class DefaultGuiFactory implements GuiFactory {
 *
 *     private final GuiService guiService;
 *
 *     public DefaultGuiFactory(GuiService guiService) {
 *         this.guiService = guiService;
 *     }
 *
 *     @Override
 *     public Gui create(@NotNull TreexClans plugin,
 *                       @NotNull Menu menu,
 *                       @NotNull Player player,
 *                       @NotNull Clan clan,
 *                       Object... args) {
 *         return guiService.createGui(menu.type(), plugin, menu, player, clan, args);
 *     }
 * }
 * }</pre>
 */
public interface GuiFactory {

    GuiService getGuiService();

    /**
     * Creates a new GUI instance based on the provided menu type.
     * <p>
     * Implementations should decide how to handle built-in GUI types
     * versus dynamically registered GUI types.
     *
     * @param plugin        the plugin instance
     * @param menu          the menu configuration
     * @param player        the player who opens the GUI
     * @param clan          the player's clan (may be {@code null})
     * @param customObjects optional additional arguments or context
     * @return a new {@link Gui} instance, or {@code null} if the GUI type is unknown
     */
    @Nullable
    Gui create(@NotNull JavaPlugin plugin,
               @NotNull Menu menu,
               @NotNull Player player,
               @Nullable Clan clan,
               Object... customObjects);
}
