package me.jetby.xClans.records.rank;

public record Rank(
        String id,
        String name,
        RankPermissions rankPermissions
) {}