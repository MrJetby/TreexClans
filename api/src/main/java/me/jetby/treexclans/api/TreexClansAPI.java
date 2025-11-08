package me.jetby.treexclans.api;

import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.gui.GuiFactory;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Member;
import java.util.Set;
import java.util.UUID;

/**
 * Public API interface for interacting with TreexClans.
 * <p>
 * External plugins should access it via:
 * {@code TreexClansAPI api = TreexClans.getAPI();}
 */
public interface TreexClansAPI {


    Economy getEconomy();

    CommandService getCommandService();

    GuiFactory getGuiFactory();

    /**
     * @return the main plugin instance
     */
    JavaPlugin getPlugin();

    @NotNull ClanManager getClanManager();
    @NotNull LeaderboardService getLeaderboardService();

}
