package me.jetby.treexclans.gui;

import me.jetby.treexclans.gui.requirements.ViewRequirement;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record Button(

        String id,
        String displayName,
        List<String> lore,
        int slot,
        int amount,
        int customModelData,
        boolean enchanted,
        boolean freeSlot,
        ItemStack itemStack,
        List<ViewRequirement> viewRequirements,
        List<Command> commands,
        int priority,
        String type


) {
}
