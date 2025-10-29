package me.jetby.treexclans.clan.rank;

import java.util.Set;

public record Rank(
        String id,
        String name,
        Set<RankPerms> perms
) {}