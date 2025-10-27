package me.jetby.treexclans.clan.rank;

public record Rank(
        String id,
        String name,
        RankPermissions rankPermissions
) {}