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
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



public class RankPermissionsGui extends Gui {
    private final Rank rank;

    public RankPermissionsGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan, Rank rank) {
        super(plugin, menu, player, clan);
        this.rank = rank;
        registerButtons();

        openPage(0);
    }

    @Override
    public void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        if (!button.type().startsWith("perm-")) return;
        build(button, builder, RankPerms.valueOf(button.type().replace("perm-", "").toUpperCase()));
    }

    @Override
    public void onClick(Player player, Button button, GuiItemController controller) {

        String permName = button.type().replace("perm-", "").toUpperCase();
        RankPerms perm = RankPerms.valueOf(permName);

        if (!getClan().getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.SETRANK)) return;
        if (!getClan().getLeader().getRank().perms().contains(perm)) return;
        if (!getClan().getMember(player.getUniqueId()).getRank().perms().contains(perm)) return;

        if (rank.perms().contains(perm)) {
            rank.perms().remove(perm);
        } else {
            rank.perms().add(perm);
        }

        controller.updateItems(wrapper -> {
            Material material = rank.perms().contains(perm) ? Material.LIME_DYE : Material.RED_DYE;
            wrapper.material(material);

            String rawDisplayName = button.displayName();
            String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, rank.id());
            processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
            wrapper.displayName(Colorize.text(processedDisplayName));

            List<String> rawLore = button.lore();
            List<String> processedLore = rawLore.stream()
                    .map(l -> replaceMemberPlaceholders(l, rank.id()))
                    .map(l -> Papi.setPapi(getPlayer(), l))
                    .map(Colorize::text)
                    .collect(Collectors.toList());
            wrapper.lore(processedLore);

            wrapper.customModelData(button.customModelData());
            wrapper.enchanted(button.enchanted());
            wrapper.update((HumanEntity) player);
        });
    }

    private void build(Button button, GuiItemController.Builder builder, RankPerms perm) {
        Material material = rank.perms().contains(perm) ? Material.LIME_DYE : Material.RED_DYE;
        ItemWrapper wrapper = ItemWrapper.builder(material).build();

        String rawDisplayName = button.displayName();
        String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, rank.id());
        processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
        wrapper.displayName(Colorize.text(processedDisplayName));

        List<String> rawLore = button.lore();
        List<String> processedLore = rawLore.stream()
                .map(l -> replaceMemberPlaceholders(l, rank.id()))
                .map(l -> Papi.setPapi(getPlayer(), l))
                .map(Colorize::text)
                .collect(Collectors.toList());
        wrapper.lore(processedLore);

        wrapper.customModelData(button.customModelData());
        wrapper.enchanted(button.enchanted());
        wrapper.update((HumanEntity) getPlayer());

        builder.defaultItem(wrapper);
    }

    private String replaceMemberPlaceholders(String text, String rankName) {
        Rank rank = getClan().getRanks().get(rankName);
        if (rank == null) return text;

        Set<RankPerms> perms = EnumSet.allOf(RankPerms.class);
        for (RankPerms perm : perms) {
            text = text.replace("%" + perm.name().toLowerCase() + "_status%", getStatus(rank.perms().contains(perm)));
        }
        text = text.replace("%rank%", rank.name());
        return text;
    }

    private String getStatus(boolean status) {
        return getPlugin().getLang().getMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }


}

