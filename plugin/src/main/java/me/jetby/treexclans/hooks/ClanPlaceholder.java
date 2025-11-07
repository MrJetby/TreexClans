package me.jetby.treexclans.hooks;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.treexclans.TreexClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanPlaceholder extends PlaceholderExpansion {
    private final TreexClans plugin;
    @Getter
    private final boolean papi;

    public ClanPlaceholder(TreexClans plugin) {
        this.plugin = plugin;
        this.papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clan";
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
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());

        return switch (args[0].toLowerCase()) {
            case "tag" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId()))
                    yield plugin.getCfg().getTagPlaceholder_noClan();

                yield plugin.getCfg().getTagPlaceholder_hasClan()
                        .replace("{tag}", clanImpl.getId())
                        .replace("{prefix}", clanImpl.getPrefix() == null ? "" : clanImpl.getPrefix());
            }
            case "prefix" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId()))
                    yield plugin.getCfg().getPrefixPlaceholder_noClan();
                if (clanImpl.getPrefix() == null) yield plugin.getCfg().getPrefixPlaceholder_noPrefix()
                        .replace("{tag}", clanImpl.getId());

                yield plugin.getCfg().getPrefixPlaceholder_hasPrefix()
                        .replace("{tag}", clanImpl.getId())
                        .replace("{prefix}", clanImpl.getPrefix());
            }
            case "coin" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clanImpl.getMember(player.getUniqueId()).getCoin());
            }
            case "slogan" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "";
                yield clanImpl.getSlogan();
            }
            case "balance" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clanImpl.getBalance());
            }
            case "level" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clanImpl.getLevel());
            }
            case "clan" -> {
                if (args[1].equalsIgnoreCase("exp")) {
                    if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                    yield String.valueOf(clanImpl.getExp());
                }
                yield "";
            }
            case "exp" -> {
                if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) yield "0";
                yield String.valueOf(clanImpl.getMember(player.getUniqueId()).getExp());
            }
            default -> null;
        };
    }
}
