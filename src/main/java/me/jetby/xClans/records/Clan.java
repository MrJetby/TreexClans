package me.jetby.xClans.records;

import me.jetby.xClans.records.rank.Rank;

import java.util.Set;

public record Clan(
        Member leader,
        Set<Member> members,
        Set<Rank> ranks,
        Chest chest
) {
}
