package me.jetby.treexclans.gui;

import me.jetby.treexclans.gui.requirements.ClickRequirement;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public record ButtonCommand(
        boolean anyClick,
        ClickType clickType,
        List<String> actions,
        List<ClickRequirement> clickRequirements
) {
}
