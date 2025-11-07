package me.jetby.treexclans.functions.tops;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the LeaderboardService.
 * Handles clan ranking by kills, deaths, level, balance, etc.
 */
public final class LeaderboardServiceImpl implements LeaderboardService {

    private final Map<String, Clan> clans;

    public LeaderboardServiceImpl(@NotNull TreexClans plugin) {
        this.clans = plugin.getCfg().getClans();
    }

    @Override
    public @Nullable Clan getTopClan(@NotNull TopType type, int position) {
        List<Clan> list = getTopList(type);
        return (position > 0 && position <= list.size()) ? list.get(position - 1) : null;
    }

    @Override
    public @NotNull List<Clan> getTopList(@NotNull TopType type) {
        Comparator<Clan> comparator = switch (type) {
            case KILLS -> Comparator.comparingInt(this::getTotalKills).reversed();
            case DEATHS -> Comparator.comparingInt(this::getTotalDeaths).reversed();
            case KD -> Comparator.<Clan>comparingDouble(c -> calculateKd(getTotalKills(c), getTotalDeaths(c))).reversed();
            case BALANCE -> Comparator.comparingDouble(Clan::getBalance).reversed();
            case LEVEL -> Comparator.<Clan>comparingInt(c -> Integer.parseInt(c.getLevel().id())).reversed();
            case MEMBERS -> Comparator.<Clan>comparingInt(c -> c.getMembersWithLeader().size()).reversed();
        };

        return clans.values().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private double calculateKd(int kills, int deaths) {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    private int getTotalKills(@NotNull Clan clan) {
        return clan.getMembersWithLeader().stream()
                .mapToInt(Member::getKills)
                .sum();
    }

    private int getTotalDeaths(@NotNull Clan clan) {
        return clan.getMembersWithLeader().stream()
                .mapToInt(Member::getDeaths)
                .sum();
    }
}
