package me.jetby.xClans.clan;

import org.bukkit.inventory.ItemStack;

public record Item(
        String inv,
        int slot,
        ItemStack itemStack
) {
}
