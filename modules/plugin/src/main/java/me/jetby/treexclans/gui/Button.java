package me.jetby.treexclans.gui;

import me.jetby.treexclans.gui.requirements.ViewRequirement;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record Button(

        String id,
        String displayName,
        String rgb,
        String openGui,
        List<String> lore,
        int slot,
        int amount,
        int customModelData,
        boolean enchanted,
        boolean freeSlot,
        boolean hideAttributes,
        ItemStack itemStack,
        List<ViewRequirement> viewRequirements,
        List<ButtonCommand> buttonCommands,
        int priority,
        String type


) {
}
