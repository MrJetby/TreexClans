package me.jetby.treexclans.clan.rank;

public record RankPermissions(
        boolean invite,
        boolean kick,
        boolean base,
        boolean setbase,
        boolean setrank,
        boolean deposit,
        boolean withdraw,
        boolean pvp
) {
}
