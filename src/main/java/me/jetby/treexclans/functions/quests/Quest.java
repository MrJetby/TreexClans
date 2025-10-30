package me.jetby.treexclans.functions.quests;

import javax.annotation.Nullable;
import java.util.List;

public record Quest(
        String id,
        String name,
        String description,
        QuestProgressType progressType,
        QuestType questType,
        @Nullable String questProperty,
        int target,
        List<String> globalRewards,
        List<String> rewards,
        List<String> disabledWorlds

) {
}
