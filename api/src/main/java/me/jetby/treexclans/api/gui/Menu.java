package me.jetby.treexclans.api.gui;

import org.bukkit.event.inventory.InventoryType;

import java.util.List;

public record Menu(
        String id,
        String title,
        String type,
        int size,
        InventoryType inventoryType,
        String permission,
        List<String> openCommands,
        List<String> openArgs,
        List<Button> buttons
) {
}
