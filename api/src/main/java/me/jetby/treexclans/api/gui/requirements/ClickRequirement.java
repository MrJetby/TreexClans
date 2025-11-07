package me.jetby.treexclans.api.gui.requirements;

import org.bukkit.event.inventory.ClickType;

import java.util.List;

public record ClickRequirement(
        boolean anyClick,
        ClickType clickType,

        String type,
        String input,
        String output,
        String permission,
        List<String> deny_commands
) implements Requirement { }
