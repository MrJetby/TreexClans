package me.jetby.treexclans.api;

import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.addons.listener.EventRegistrar;
import me.jetby.treexclans.api.gui.GuiFactory;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * 🧩 The main public API interface for interacting with TreexClans.
 *
 * <p>
 * Provides access to all major systems of the TreexClans framework —
 * including clans, leaderboards, commands, GUIs, and Vault integration.
 * </p>
 *
 * <p>
 * To obtain an instance of this API, use the Bukkit
 * {@link org.bukkit.plugin.ServicesManager}:
 * </p>
 *
 * <pre>{@code
 * TreexClansAPI api = Bukkit.getServicesManager().load(TreexClansAPI.class);
 * }</pre>
 *
 * <p>
 * Once loaded, you can access all TreexClans subsystems safely
 * without directly depending on the main plugin class.
 * </p>
 */

public interface TreexClansAPI {

    /**
     * Provides access to the Vault economy integration.
     *
     * <p>
     * Used for handling currency operations such as clan balance,
     * purchases, rewards, and other economy-related features.
     * </p>
     *
     * @return the active Vault {@link Economy} instance
     */
    Economy getEconomy();

    /**
     * Returns the global command service.
     *
     * <p>
     * This service is responsible for command registration,
     * execution, and dynamic management of addon subcommands.
     * </p>
     *
     * @return the {@link CommandService} instance
     */
    CommandService getCommandService();

    /**
     * Provides access to GUI and menu creation utilities.
     *
     * <p>
     * The {@link GuiFactory} simplifies building interactive
     * interfaces and menus within TreexClans.
     * </p>
     *
     * @return the global {@link GuiFactory}
     */
    GuiFactory getGuiFactory();

    /**
     * Returns the main TreexClans plugin instance.
     *
     * <p>
     * Useful for accessing Bukkit’s plugin context,
     * scheduling tasks, or registering listeners.
     * </p>
     *
     * @return the {@link JavaPlugin} instance representing TreexClans
     */
    JavaPlugin getPlugin();

    /**
     * Provides a unified registrar for event listeners.
     *
     * <p>
     * The {@link EventRegistrar} allows addons to easily
     * register and unregister their listeners in a safe and
     * organized manner.
     * </p>
     *
     * @return the global {@link EventRegistrar} instance
     */
    EventRegistrar getEventRegistrar();

    /**
     * Gives access to the core ClanManager system.
     *
     * <p>
     * The {@link ClanManager} provides APIs for creating, deleting,
     * validating, and managing clans, as well as accessing chat,
     * economy, color, and lookup services.
     * </p>
     *
     * @return the {@link ClanManager} API
     */
    @NotNull ClanManager getClanManager();

    /**
     * Returns the leaderboard service.
     *
     * <p>
     * The {@link LeaderboardService} handles ranking logic,
     * top statistics, and performance tracking for clans.
     * </p>
     *
     * @return the {@link LeaderboardService} instance
     */
    @NotNull LeaderboardService getLeaderboardService();
}
