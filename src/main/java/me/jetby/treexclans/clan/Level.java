package me.jetby.treexclans.clan;

import java.util.List;

public record Level(
        int id,
        int minExp,
        int maxMembers,
        int chest,
        List<String> quests
) {
}
