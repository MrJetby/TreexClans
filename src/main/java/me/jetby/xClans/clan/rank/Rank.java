package me.jetby.xClans.clan.rank;

public record Rank(
        String id,
        String name,
        RankPermissions rankPermissions
) {}