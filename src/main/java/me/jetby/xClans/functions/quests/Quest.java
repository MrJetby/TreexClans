package me.jetby.xClans.functions.quests;

import java.util.List;

public record Quest(
        String id,
        String name,
        String description,
        QuestType questType,
        String questProperty,
        String target,
        List<String> rewards

) {
}
