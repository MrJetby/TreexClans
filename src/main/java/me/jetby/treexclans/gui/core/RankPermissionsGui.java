package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.clan.rank.RankPerms;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class RankPermissionsGui extends Gui {

    private final Rank rank;

    public RankPermissionsGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan, Rank rank) {
        super(plugin, menu, player, clan);
        this.rank = rank;

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        if (!button.type().startsWith("perm-")) return;
        build(button, builder, RankPerms.valueOf(button.type().replace("perm-", "").toUpperCase()));
    }

    @Override
    protected void onClick(Player player, Button button, InventoryClickEvent event, GuiItemController controller) {
        String perm = button.type().replace("perm-", "").toUpperCase();

        controller.updateItems(wrapper -> {
            if (rank.perms().contains(RankPerms.valueOf(perm))) {
                wrapper = ItemWrapper.builder(Material.LIME_DYE).build();
            } else {
                wrapper = ItemWrapper.builder(Material.RED_DYE).build();
            }
            String rawDisplayName = button.displayName();
            String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, perm);
            processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
            wrapper.displayName(Colorize.text(processedDisplayName));

            List<String> rawLore = button.lore();
            List<String> processedLore = rawLore.stream()
                    .map(l -> replaceMemberPlaceholders(l, perm))
                    .map(l -> Papi.setPapi(getPlayer(), l))
                    .map(Colorize::text)
                    .collect(Collectors.toList());
            wrapper.lore(processedLore);

            wrapper.customModelData(button.customModelData());
            wrapper.enchanted(button.enchanted());
            wrapper.update();
        });
    }

    private void build(Button button, GuiItemController.Builder builder, RankPerms perm) {
        ItemWrapper wrapper;
        if (rank.perms().contains(perm)) {
            wrapper = ItemWrapper.builder(Material.LIME_DYE).build();
        } else {
            wrapper = ItemWrapper.builder(Material.RED_DYE).build();
        }

        String rawDisplayName = button.displayName();
        String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, rank.name());
        processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
        wrapper.displayName(Colorize.text(processedDisplayName));

        List<String> rawLore = button.lore();
        List<String> processedLore = rawLore.stream()
                .map(l -> replaceMemberPlaceholders(l, rank.name()))
                .map(l -> Papi.setPapi(getPlayer(), l))
                .map(Colorize::text)
                .collect(Collectors.toList());
        wrapper.lore(processedLore);

        wrapper.customModelData(button.customModelData());
        wrapper.enchanted(button.enchanted());
        wrapper.update();

        ItemMeta itemMeta = button.itemStack().getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
            button.itemStack().setItemMeta(itemMeta);
        }

        builder.defaultItem(wrapper);
    }

    private String replaceMemberPlaceholders(String text, String rankName) {
        Rank rank = getClan().getRanks().get(rankName);
        if (rank == null) return text;

        Set<RankPerms> perms = rank.perms();

        text = text.replace("%invite_status%", getStatus(perms.contains(RankPerms.INVITE)));
        text = text.replace("%kick_status%", getStatus(perms.contains(RankPerms.KICK)));
        text = text.replace("%base_status%", getStatus(perms.contains(RankPerms.BASE)));
        text = text.replace("%setrank_status%", getStatus(perms.contains(RankPerms.SETRANK)));
        text = text.replace("%setbase_status%", getStatus(perms.contains(RankPerms.SETBASE)));
        text = text.replace("%deposit_status%", getStatus(perms.contains(RankPerms.DEPOSIT)));
        text = text.replace("%withdraw_status%", getStatus(perms.contains(RankPerms.WITHDRAW)));
        text = text.replace("%pvp_status%", getStatus(perms.contains(RankPerms.PVP)));
        text = text.replace("%rank%", rank.name());
        return text;
    }

    private String getStatus(boolean status) {
        return getPlugin().getLang().getMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }


}

