package me.jetby.xClans.records;

import org.bukkit.inventory.ItemStack;

public record Item(
        String inv,
        int slot,
        ItemStack itemStack
) {
}
