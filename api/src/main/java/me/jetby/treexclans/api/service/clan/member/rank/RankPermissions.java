package me.jetby.treexclans.api.service.clan.member.rank;

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
