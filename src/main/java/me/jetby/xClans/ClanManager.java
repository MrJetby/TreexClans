package me.jetby.xClans;

import lombok.RequiredArgsConstructor;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Level;
import me.jetby.xClans.records.Member;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

@RequiredArgsConstructor
public class ClanManager {
    private final TreexClans plugin;

    public boolean isInClan(@NotNull UUID uuid) {
        return plugin.getClansLoader().getClans().values().stream()
                .anyMatch(clan -> (clan.getLeader() != null && clan.getLeader().uuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.uuid().equals(uuid)));
    }

    public boolean isInClan(@NotNull String playerName) {
        UUID uuid = UUID.fromString(playerName);
        return plugin.getClansLoader().getClans().values().stream()
                .anyMatch(clan -> (clan.getLeader() != null && clan.getLeader().uuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.uuid().equals(uuid)));
    }

    public boolean clanExists(@NotNull String clanName) {
        return plugin.getClansLoader().getClans().containsKey(clanName);
    }

    public void createClan(@NotNull String clanName, @NotNull Clan clan) {
        if (!clanExists(clanName)) plugin.getClansLoader().getClans().put(clanName, clan);

    }

    public void createClan(@NotNull String clanName, @NotNull Member leader) {
        if (!clanExists(clanName)) {
            Clan clan = new Clan(clanName, null, leader, new HashSet<>(), plugin.getClansLoader().getDefaultRanks(), null, new Level(1), 0.0);
            plugin.getClansLoader().getClans().put(clanName, clan);
        }

    }

    public Clan getClan(@NotNull String clanName) {
        return plugin.getClansLoader().getClans().get(clanName);
    }

    public Clan getClanByMember(@NotNull UUID uuid) {
        return plugin.getClansLoader().getClans().values().stream()
                .filter(clan -> (clan.getLeader() != null && clan.getLeader().uuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.uuid().equals(uuid)))
                .findFirst()
                .orElse(null);
    }

    public Clan getClanByMember(@NotNull String playerName) {
        UUID uuid = UUID.fromString(playerName);
        return plugin.getClansLoader().getClans().values().stream()
                .filter(clan -> (clan.getLeader() != null && clan.getLeader().uuid().equals(uuid))
                        || clan.getMembers().stream()
                        .anyMatch(member -> member.uuid().equals(uuid)))
                .findFirst()
                .orElse(null);
    }
}
