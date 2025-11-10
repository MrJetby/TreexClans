package me.jetby.treexclans.hooks;

import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
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

            case "top" -> {
                if (args[1].equalsIgnoreCase("kills")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        var clan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.KILLS, num);
                        if (clan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield clan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((int) plugin.getLeaderboardService().getTopProgress(clan, LeaderboardService.TopType.KILLS));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("deaths")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        var clan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.DEATHS, num);
                        if (clan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield clan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((int) plugin.getLeaderboardService().getTopProgress(clan, LeaderboardService.TopType.DEATHS));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("kd")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        var clan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.KD, num);
                        if (clan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield clan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((double) plugin.getLeaderboardService().getTopProgress(clan, LeaderboardService.TopType.KD));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("balance")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        var clan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.BALANCE, num);
                        if (clan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield clan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((double) plugin.getLeaderboardService().getTopProgress(clan, LeaderboardService.TopType.BALANCE));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("level")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        var clan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.LEVEL, num);
                        if (clan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield clan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf(plugin.getLeaderboardService().getTopProgress(clan, LeaderboardService.TopType.LEVEL));
                        }
                    }
                }
                if (args[1].equalsIgnoreCase("members")) {
                    if (args.length==4)  {
                        int num = Integer.parseInt(args[2]);
                        var clan = plugin.getLeaderboardService().getTopClan(LeaderboardService.TopType.MEMBERS, num);
                        if (clan==null) yield "none";
                        String type = args[3];

                        if (type.equalsIgnoreCase("name")) {
                            yield clan.getId();
                        } else if (type.equalsIgnoreCase("progress")) {
                            yield String.valueOf((int) plugin.getLeaderboardService().getTopProgress(clan, LeaderboardService.TopType.MEMBERS));
                        }
                    }
                }
                yield null;
            }

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
                yield clanImpl.getLevel().id();
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
