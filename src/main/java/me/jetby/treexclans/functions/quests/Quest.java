package me.jetby.treexclans.functions.quests;

import me.jetby.treex.actions.ActionRegistry;

import javax.annotation.Nullable;
import java.util.List;

public record Quest(
        String id,
        String name,
        String description,
        QuestType questType,
        @Nullable String questProperty,
        int target,
        List<ActionRegistry.RegistryActionEntry> rewards,
        List<String> disabledWorlds

) {
}
