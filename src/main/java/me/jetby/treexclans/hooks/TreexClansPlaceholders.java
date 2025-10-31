package me.jetby.treexclans.hooks;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreexClansPlaceholders extends PlaceholderExpansion {
    private final TreexClans plugin;
    @Getter
    private final boolean papi;

    public TreexClansPlaceholders(TreexClans plugin) {
        this.plugin = plugin;
        this.papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "treexclans";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.valueOf(plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String identifier) {
        String[] args = identifier.split("_");
        Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());

        return switch (args[0].toLowerCase()) {
            case "coin" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getMember(player.getUniqueId()).getCoin());
            }
            case "balance" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getBalance());
            }
            case "level" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getLevel());
            }
            case "clan_exp" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getExp());
            }
            case "exp" -> {
                if (!plugin.getClanManager().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clan.getMember(player.getUniqueId()).getExp());
            }
            default -> null;
        };
    }
}
