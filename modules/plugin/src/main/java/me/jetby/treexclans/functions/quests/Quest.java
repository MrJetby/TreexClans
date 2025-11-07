package me.jetby.treexclans.functions.quests;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public record Quest(
        String id,
        String name,
        String description,
        List<String> rewardsDescription,
        QuestProgressType progressType,
        QuestType questType,
        @Nullable String questProperty,
        int target,
        List<String> globalRewards,
        List<String> rewards,
        List<String> disabledWorlds

) {
}
