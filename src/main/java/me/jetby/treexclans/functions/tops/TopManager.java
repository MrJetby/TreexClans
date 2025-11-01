package me.jetby.treexclans.functions.tops;


import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class TopManager {

    private final Map<String, Clan> clans;

    public TopManager(TreexClans plugin) {
        this.clans = plugin.getCfg().getClans();
    }

    @Nullable
    public Clan getTopClan(TopType topType, int key) {
        return switch (topType) {
            case KILLS -> getTopKills(key);
            case DEATHS -> getTopDeaths(key);
            case KD -> getTopKd(key);
            case BALANCE -> getTopBalance(key);
            case LEVEL -> getTopLevel(key);
            case MEMBERS -> getTopAmountOfMembers(key);
        };
    }

    private Clan getTopKd(int key) {
        List<Map.Entry<String, Clan>> sortedClans = clans.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((c1, c2) -> {
                    double kd1 = calculateKd(getTotalKills(c1), getTotalDeaths(c1));
                    double kd2 = calculateKd(getTotalKills(c2), getTotalDeaths(c2));
                    return Double.compare(kd1, kd2);
                }))
                .limit(key)
                .toList();

        if (sortedClans.size() >= key) {
            return sortedClans.get(key - 1).getValue();
        }

        return null;
    }

    private double calculateKd(int kills, int deaths) {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    private Clan getTopKills(int key) {
        if (key < 1) return null;
        List<Map.Entry<String, Clan>> sortedClans = clans.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((c1, c2) ->
                        Integer.compare(getTotalKills(c2), getTotalKills(c1))))
                .limit(key)
                .toList();

        if (sortedClans.size() >= key) {
            return sortedClans.get(key - 1).getValue();
        }
        return null;
    }

    private Clan getTopAmountOfMembers(int key) {
        if (key < 1) return null;
        List<Map.Entry<String, Clan>> sortedClans = clans.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((c1, c2) ->
                        Integer.compare(c2.getMembersWithLeader().size(), c1.getMembersWithLeader().size())))
                .limit(key)
                .toList();

        if (sortedClans.size() >= key) {
            return sortedClans.get(key - 1).getValue();
        }
        return null;
    }
    private Clan getTopLevel(int key) {
        if (key < 1) return null;
        List<Map.Entry<String, Clan>> sortedClans = clans.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((c1, c2) ->
                        Integer.compare(Integer.parseInt(c2.getLevel().id()), Integer.parseInt(c1.getLevel().id()))))
                .limit(key)
                .toList();

        if (sortedClans.size() >= key) {
            return sortedClans.get(key - 1).getValue();
        }
        return null;
    }
    private Clan getTopBalance(int key) {
        if (key < 1) return null;
        List<Map.Entry<String, Clan>> sortedClans = clans.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((c1, c2) ->
                        Double.compare(c2.getBalance(), c1.getBalance())))
                .limit(key)
                .toList();

        if (sortedClans.size() >= key) {
            return sortedClans.get(key - 1).getValue();
        }
        return null;
    }
    private Clan getTopDeaths(int key) {
        if (key < 1) return null;
        List<Map.Entry<String, Clan>> sortedClans = clans.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((c1, c2) ->
                        Integer.compare(getTotalDeaths(c2), getTotalDeaths(c1))))
                .limit(key)
                .toList();

        if (sortedClans.size() >= key) {
            return sortedClans.get(key - 1).getValue();
        }
        return null;
    }

    private int getTotalDeaths(Clan clan) {
        int total = 0;
        for (Member member : clan.getMembersWithLeader()) {
            total += member.getDeaths();
        }
        return total;
    }

    private int getTotalKills(Clan clan) {
        int total = 0;
        for (Member member : clan.getMembersWithLeader()) {
            total += member.getKills();
        }
        return total;
    }
}
