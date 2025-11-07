package me.jetby.treexclans.api;

import me.jetby.treexclans.api.addons.commands.CommandService;
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

    /**
     * @return the main plugin instance
     */
    JavaPlugin getPlugin();

    @NotNull ClanManager getClanManager();
    @NotNull LeaderboardService getLeaderboardService();

//    /**
//     * @return configuration manager
//     */
//    @NotNull Config getConfigManager();
//
//    /**
//     * @return the clan manager
//     */
//    @NotNull ClanManager getClanManager();
//
//    /**
//     * @return the top manager
//     */
//    @NotNull TopManager getTopManager();
//
//    /**
//     * @return the quest manager
//     */
//    @NotNull QuestManager getQuestManager();
//
//    /**
//     * @return the glow manager
//     */
//    @Nullable Glow getGlow();
//
//    /**
//     * Gets a clan by name.
//     */
//    @Nullable Clan getClan(@NotNull String name);
//
//    /**
//     * Gets a clan by member UUID.
//     */
//    @Nullable Clan getClanByMember(@NotNull UUID uuid);
//
//    /**
//     * Gets a member by UUID.
//     */
//    @Nullable Member getMember(@NotNull UUID uuid);
//
//    /**
//     * @return all registered clans
//     */
//    @NotNull Set<Clan> getAllClans();

    /**
     * Reloads configuration and internal caches.
     */
//    void reload();

}
