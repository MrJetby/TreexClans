package me.jetby.xClans.configurations;

import lombok.Getter;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Level;
import me.jetby.xClans.records.rank.Rank;
import me.jetby.xClans.records.rank.RankPermissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class ClansLoader {

    private final Set<Rank> defaultRanks = new HashSet<>();
    private final Set<Level> levels = new HashSet<>();
    private final Map<String, Clan> clans = new HashMap<>();

    private Rank leaderRank;

    public void load() {
        leaderRank = new Rank("leader", "Leader", new RankPermissions(
                true, true, true, true, true, true, true, true));
    }
}
