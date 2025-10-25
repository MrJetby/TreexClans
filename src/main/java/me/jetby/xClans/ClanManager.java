package me.jetby.xClans;

import lombok.RequiredArgsConstructor;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Level;
import me.jetby.xClans.records.Member;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

@RequiredArgsConstructor
public class ClanManager {
    private final TreexClans plugin;

    /**
     * Checks if a player (by UUID) is currently in any clan.
     *
     * @param uuid the player's unique identifier
     * @return true if the player belongs to any clan, false otherwise
     */
    public boolean isInClan(@NotNull UUID uuid) {
        return plugin.getClansLoader().getClans().values().stream()
                .anyMatch(clan -> (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.getUuid().equals(uuid)));
    }
    /**
     * Checks if a player (by stringified UUID) is currently in any clan.
     *
     * @param playerName the player's UUID represented as a string
     * @return true if the player belongs to any clan, false otherwise
     */
    public boolean isInClan(@NotNull String playerName) {
        UUID uuid = UUID.fromString(playerName);
        return plugin.getClansLoader().getClans().values().stream()
                .anyMatch(clan -> (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.getUuid().equals(uuid)));
    }
    /**
     * Checks whether a clan with the given name already exists.
     *
     * @param clanName the name of the clan
     * @return true if a clan with that name exists, false otherwise
     */
    public boolean clanExists(@NotNull String clanName) {
        return plugin.getClansLoader().getClans().containsKey(clanName);
    }
    /**
     * Creates and registers a new clan if the name is not already in use.
     *
     * @param clanName the name of the new clan
     * @param clan     the clan instance to register
     * @return true if the clan was successfully created, false if it already exists
     */
    public boolean createClan(@NotNull String clanName, @NotNull Clan clan) {
        if (!clanExists(clanName)) {
            plugin.getClansLoader().getClans().put(clanName, clan);
            return true;
        }
        return false;
    }
    /**
     * Creates a new clan with the specified leader and default configuration.
     *
     * @param clanName the name of the new clan
     * @param leader   the leader of the clan
     * @return true if the clan was successfully created, false if it already exists
     */
    public boolean createClan(@NotNull String clanName, @NotNull Member leader) {
        if (!clanExists(clanName)) {
            Clan clan = new Clan(clanName, null, leader, new HashSet<>(), plugin.getCfg().getDefaultRanks(), null, new Level(1), 0.0);
            plugin.getClansLoader().getClans().put(clanName, clan);
            return true;
        }
        return false;
    }
    /**
     * Retrieves a clan by its name.
     *
     * @param clanName the name of the clan
     * @return the {@link Clan} instance, or null if not found
     */
    public Clan getClan(@NotNull String clanName) {
        return plugin.getClansLoader().getClans().get(clanName);
    }
    /**
     * Retrieves the clan to which the player (by UUID) belongs.
     *
     * @param uuid the player's unique identifier
     * @return the player's {@link Clan}, or null if none found
     */
    public Clan getClanByMember(@NotNull UUID uuid) {
        return plugin.getClansLoader().getClans().values().stream()
                .filter(clan -> (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.getUuid().equals(uuid)))
                .findFirst()
                .orElse(null);
    }
    /**
     * Retrieves the clan to which the player (by stringified UUID) belongs.
     *
     * @param playerName the player's UUID represented as a string
     * @return the player's {@link Clan}, or null if none found
     */
    public Clan getClanByMember(@NotNull String playerName) {
        UUID uuid = UUID.fromString(playerName);
        return plugin.getClansLoader().getClans().values().stream()
                .filter(clan -> (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.getUuid().equals(uuid)))
                .findFirst()
                .orElse(null);
    }
    /**
     * Retrieves the last online timestamp for a specific player.
     *
     * @param uuid the player's unique identifier
     * @return the timestamp of the player's last online moment, or -1 if not found
     */
    public long getLastOnline(@NotNull UUID uuid) {
        if (isInClan(uuid)) {
            return getClanByMember(uuid).getMember(uuid).getLastOnline();
        }
        return -1;
    }
    public String getLastOnlineFormatted(@NotNull UUID uuid) {
        if (isInClan(uuid)) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline()) {
                getClanByMember(uuid).getMember(uuid).setLastOnline(System.currentTimeMillis());
                return "В сети";
            } else {
                return plugin.getFormatTime().stringFormat(System.currentTimeMillis()-getClanByMember(uuid).getMember(uuid).getLastOnline());
            }
        }
        return "-1";
    }
}
