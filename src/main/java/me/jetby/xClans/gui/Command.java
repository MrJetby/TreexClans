package me.jetby.xClans.gui;

import me.jetby.xClans.gui.requirements.ClickRequirement;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public record Command(
        boolean anyClick,
        ClickType clickType,
        List<String> actions,
        List<ClickRequirement> clickRequirements

) {
}
