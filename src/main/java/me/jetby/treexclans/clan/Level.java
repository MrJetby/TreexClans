package me.jetby.treexclans.clan;

import java.util.List;

public record Level(
        String id,
        int minExp,
        int maxMembers,
        int maxBalance,
        int chest,
        List<String> quests,
        List<String> levelUpActions
) {
}
