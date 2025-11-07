package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;


public class RankPermissionsGui extends Gui {
    private final Rank rank;

    public RankPermissionsGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clanImpl, Rank rank) {
        super(plugin, menu, player, clanImpl);
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

        if (!getClanImpl().getMember(player.getUniqueId()).getRank().perms().contains(RankPerms.SETRANK)) return;
        if (getClanImpl().getMember(player.getUniqueId()).getRank().equals(rank)) return;
        if (!getClanImpl().getLeader().getRank().perms().contains(perm)) return;
        if (!getClanImpl().getMember(player.getUniqueId()).getRank().perms().contains(perm)) return;

        if (rank.perms().contains(perm)) {
            rank.perms().remove(perm);
        } else {
            rank.perms().add(perm);
        }

        controller.updateItems(wrapper -> {
            Material material = rank.perms().contains(perm) ? Material.LIME_DYE : Material.RED_DYE;
            wrapper.material(material);

            replaceMemberPlaceholders(rank.id());

            wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

            wrapper.lore(button.lore().stream()
                    .map(this::applyDefaultPlaceholders)
                    .collect(Collectors.toList()));

            wrapper.customModelData(button.customModelData());
            wrapper.enchanted(button.enchanted());
            wrapper.update((HumanEntity) player);
        });
    }

    private void build(Button button, GuiItemController.Builder builder, RankPerms perm) {
        Material material = rank.perms().contains(perm) ? Material.LIME_DYE : Material.RED_DYE;
        ItemWrapper wrapper = ItemWrapper.builder(material).build();

        replaceMemberPlaceholders(rank.id());

        wrapper.displayName(applyDefaultPlaceholders(button.displayName()));
        wrapper.lore(button.lore().stream()
                .map(this::applyDefaultPlaceholders)
                .collect(Collectors.toList()));

        wrapper.customModelData(button.customModelData());
        wrapper.enchanted(button.enchanted());
        wrapper.update((HumanEntity) getPlayer());

        builder.defaultItem(wrapper);
    }

    private void replaceMemberPlaceholders(String rankName) {
        Rank rank = getClanImpl().getRanks().get(rankName);
        if (rank == null) return;

        Set<RankPerms> perms = EnumSet.allOf(RankPerms.class);
        for (RankPerms perm : perms) {
            setCustomPlaceholder("%" + perm.name().toLowerCase() + "_status%", getStatus(rank.perms().contains(perm)));
        }
        setCustomPlaceholder("%rank%", rank.name());

    }

    private String getStatus(boolean status) {
        return getPlugin().getLang().getMessage(status ? "rank-perm-yes" : "rank-perm-no");
    }


}

