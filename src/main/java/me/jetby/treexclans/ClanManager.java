package me.jetby.treexclans;

import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treexclans.api.events.OnClanCreate;
import me.jetby.treexclans.api.events.OnClanDelete;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Level;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.gui.requirements.Requirements;
import me.jetby.treexclans.gui.requirements.SimpleRequirement;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record ClanManager(TreexClans plugin) implements Listener {
    public ClanManager(TreexClans plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

    }

    /**
     * Checks if a player (by UUID) is currently in any clan.
     *
     * @param uuid the player's unique identifier
     * @return true if the player belongs to any clan, false otherwise
     */
    public boolean isInClan(@NotNull UUID uuid) {
        return plugin.getCfg().getClans().values().stream()
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
        return plugin.getCfg().getClans().values().stream()
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
        return plugin.getCfg().getClans().containsKey(clanName);
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
            plugin.getCfg().getClans().put(clanName, clan);
            Bukkit.getPluginManager().callEvent(new OnClanCreate(clan));
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
            Clan clan = new Clan(clanName, null, leader, new HashSet<>(), plugin.getCfg().getDefaultRanks(),
                    new Level(1, 0, 0, 1, new ArrayList<>()), 0.0, null, 0, false, new HashMap<>(), new ArrayList<>(), new ArrayList<>());
            plugin.getCfg().getClans().put(clanName, clan);
            Bukkit.getPluginManager().callEvent(new OnClanCreate(clan));
            return true;
        }
        return false;
    }

    public boolean isAllowedName(Player player, String clanName) {

        if (clanName.length() < plugin.getCfg().getMinTagLength()) {
            plugin.getLang().sendMessage(player, null, "clan-tag-too-short", new Lang.ReplaceString("{min_length}", String.valueOf(plugin.getCfg().getMinTagLength())));
            return false;
        }
        if (clanName.length() > plugin.getCfg().getMaxTagLength()) {
            plugin.getLang().sendMessage(player, null, "clan-tag-too-long", new Lang.ReplaceString("{max_length}", String.valueOf(plugin.getCfg().getMaxTagLength())));
            return false;
        }

        if (plugin.getCfg().getBlockedTags().contains(clanName)) {
            plugin.getLang().sendMessage(player, null, "clan-tag-blocked");
            return false;
        }

        // TODO: проверить что никаких символов и только англо буквы
//        plugin.getLang().sendMessage(player, null, "clan-tag-invalid-characters");

        for (SimpleRequirement requirement : plugin.getCfg().getRequirements()) {
            if (!Requirements.check(player, requirement)) {
                Requirements.runDenyCommands(player, requirement.denyActions());
                return false;
            } else {
                ActionExecutor.execute(new ActionContext(player), ActionRegistry.transform(requirement.actions()));
            }
        }
        return true;
    }

    public void deleteClan(@NotNull Clan clan) {
//        for (Member member : clan.getMembers()) {
//            Player player = Bukkit.getPlayer(member.getUuid());
//            if (player != null) {
//                player.sendMessage("Your clan was disbanded by clan leader");
//            }
//        }
        plugin.getCfg().getClans().remove(clan.getId());
        Bukkit.getPluginManager().callEvent(new OnClanDelete(clan));
    }

    public boolean deleteClan(@NotNull String clanName) {
        Clan clan = getClan(clanName);
        if (clan == null) {
            return false;
        }
        for (Member member : clan.getMembers()) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player != null) {
                player.sendMessage("Your clan was disbanded by clan leader");
            }
        }
        plugin.getCfg().getClans().remove(clan.getId());
        Bukkit.getPluginManager().callEvent(new OnClanDelete(clan));
        return true;
    }


    public void addBalance(double a, @NotNull Clan clan) {
        clan.setBalance(clan.getBalance() + a);
    }

    public double getBalance(@NotNull Clan clan) {
        return clan.getBalance();
    }

    public void takeBalance(double a, @NotNull Clan clan) {
        clan.setBalance(clan.getBalance() - a);
    }

    /**
     * Retrieves a clan by its name.
     *
     * @param clanName the name of the clan
     * @return the {@link Clan} instance, or null if not found
     */
    public Clan getClan(@NotNull String clanName) {
        return plugin.getCfg().getClans().get(clanName);
    }

    /**
     * Retrieves the clan to which the player (by UUID) belongs.
     *
     * @param uuid the player's unique identifier
     * @return the player's {@link Clan}, or null if none found
     */
    public Clan getClanByMember(@NotNull UUID uuid) {
        return plugin.getCfg().getClans().values().stream()
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
        return plugin.getCfg().getClans().values().stream()
                .filter(clan -> (clan.getLeader() != null && clan.getLeader().getUuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.getUuid().equals(uuid)))
                .findFirst()
                .orElse(null);
    }

    public Clan getClanByMember(@NotNull Member member) {
        return plugin.getCfg().getClans().values().stream()
                .filter(clan -> (clan.getLeader() != null && clan.getLeader().equals(member))
                        || clan.getMembers().stream()
                        .anyMatch(player -> player.equals(member)))
                .findFirst()
                .orElse(null);
    }

    public String getLastOnlineFormatted(@NotNull UUID uuid) {
        if (isInClan(uuid)) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline()) {
                getClanByMember(uuid).getMember(uuid).setLastOnline(System.currentTimeMillis());
                return "В сети";
            } else {
                return plugin.getFormatTime().stringFormat(System.currentTimeMillis() - getClanByMember(uuid).getMember(uuid).getLastOnline());
            }
        }
        return "-1";
    }

    public String getLastOnlineFormatted(@NotNull Member member) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
        if (offlinePlayer.isOnline()) {
            member.setLastOnline(System.currentTimeMillis());
            return "В сети";
        } else {
            return plugin.getFormatTime().stringFormat(System.currentTimeMillis() - member.getLastOnline());
        }
    }


    public void sendMessage(@NotNull Clan clan, String message) {
        for (Member member : clan.getMembers()) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player == null) continue;
            player.sendMessage(message);
        }
        Member leader = clan.getLeader();
        Player player = Bukkit.getPlayer(leader.getUuid());
        if (player == null) return;
        player.sendMessage(message);
    }

    public void sendChat(@NotNull Clan clan, Player sender, String message) {
        for (Member member : clan.getMembers()) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player == null) continue;
            player.sendMessage(Colorize.text(plugin.getCfg().getChatFormat()
                    .replace("{player}", sender.getName())
                    .replace("{message}", message)));
        }
        Member leader = clan.getLeader();
        Player player = Bukkit.getPlayer(leader.getUuid());
        if (player == null) return;
        player.sendMessage(Colorize.text(plugin.getCfg().getChatFormat()
                .replace("{player}", sender.getName())
                .replace("{message}", message)));
    }

    public void setColor(@NotNull Clan clan, @NotNull Member member, @NotNull Color color) {
        Map<UUID, Color> colors = member.getGlowColors();
        Set<Member> members = new HashSet<>(clan.getMembers());
        if (!clan.getLeader().equals(member)) members.add(clan.getLeader());
        for (Member target : members) {
            if (target.equals(member)) continue;
            colors.put(target.getUuid(), color);
        }
        member.setGlowColors(colors);
    }

    public void setColor(@NotNull Member member, @NotNull Set<Member> members, @NotNull Color color) {
        Map<UUID, Color> colors = member.getGlowColors();
        for (Member target : members) {
            if (target.equals(member)) continue;
            colors.put(target.getUuid(), color);
        }
        member.setGlowColors(colors);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Clan clan = getClanByMember(uuid);
            if (clan == null) return;
            getClanByMember(uuid).getMember(uuid).setLastOnline(System.currentTimeMillis());
        });
    }
}
