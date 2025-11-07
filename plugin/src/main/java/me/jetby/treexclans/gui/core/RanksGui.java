package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.InstanceFactory;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.api.gui.Button;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.gui.GuiFactoryImpl;
import me.jetby.treexclans.api.gui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class RanksGui extends Gui {
    public RanksGui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clanImpl) {
        super(plugin, menu, player, clanImpl);
        registerButtons();

        setupRanksPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "all_ranks": {
                break;
            }
            case "leader_rank": {
                Rank rank = getClanImpl().getLeader().getRank();
                if (rank == null) {
                    break;
                }
                replaceMemberPlaceholders(rank);

                ItemWrapper wrapper = new ItemWrapper(button.itemStack());
                wrapper.displayName(applyDefaultPlaceholders(button.displayName()));
                wrapper.lore(button.lore().stream()
                        .map(this::applyDefaultPlaceholders)
                        .collect(Collectors.toList()));

                wrapper.customModelData(button.customModelData());
                wrapper.enchanted(button.enchanted());
                wrapper.update();

                ItemMeta itemMeta = button.itemStack().getItemMeta();
                if (itemMeta != null) {
                    itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                    button.itemStack().setItemMeta(itemMeta);
                }

                builder.defaultItem(wrapper);
                break;
            }
            case "next_page": {
                builder.defaultClickHandler((e, gui) -> {
                    e.setCancelled(true);
                    nextPage();
                });
                break;
            }
            case "prev_page": {
                builder.defaultClickHandler((e, gui) -> {
                    e.setCancelled(true);
                    previousPage();
                });
                break;
            }
        }
    }

    @Override
    public boolean cancelRegistration(Player player, @Nullable Button button) {
        if (button != null) {
            return button.type().equals("all_ranks");
        }
        return false;
    }

    private void setupRanksPagination() {
        List<Button> buttons = getMenu().buttons().stream()
                .filter(b -> "all_ranks".equals(b.type()))
                .toList();

        List<Integer> sortedRankSlots = buttons.stream()
                .map(Button::slot)
                .distinct()
                .sorted()
                .toList();

        if (buttons.isEmpty()) return;

        int itemsPerPage = sortedRankSlots.size();

        Map<String, Rank> ranks = new HashMap<>(getClanImpl().getRanks());
        ranks.remove(getClanImpl().getLeader().getRank().id());
        List<String> ranksStr = new ArrayList<>(ranks.keySet());

        int totalPages = (int) Math.ceil((double) ranksStr.size() / itemsPerPage);
        Button button = buttons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, ranksStr.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int rankIndex = start + i;
                final int slot = sortedRankSlots.get(i);

                if (rankIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                final String rankId = ranksStr.get(rankIndex);
                final Rank rank = ranks.get(rankId);

                consumers[i] = builder -> {
                    replaceMemberPlaceholders(rank);
                    ItemStack clonedItem = button.itemStack().clone();
                    ItemWrapper wrapper = new ItemWrapper(clonedItem);

                    wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                    List<String> processedLore = button.lore().stream()
                            .map(this::applyDefaultPlaceholders)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(button.customModelData());
                    wrapper.enchanted(button.enchanted());
                    wrapper.update();

                    builder.slots(slot);
                    builder.defaultItem(wrapper);

                    builder.defaultClickHandler((event, ctrl) -> {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                InstanceFactory.GUI_FACTORY.create(
                                                getPlugin(),
                                                getPlugin().getGuiLoader().getMenus().get(button.openGui()),
                                                getPlayer(), getClanImpl(), rank)
                                        .open(getPlayer()), 1L);
                    });
                };
            }

            if (consumers[page] == null) continue;
            addPage(consumers);
        }
    }

    private void replaceMemberPlaceholders(Rank rank) {
        setCustomPlaceholder("%invite_status%", getStatus(rank.perms().contains(RankPerms.INVITE)));
        setCustomPlaceholder("%kick_status%", getStatus(rank.perms().contains(RankPerms.KICK)));
        setCustomPlaceholder("%base_status%", getStatus(rank.perms().contains(RankPerms.BASE)));
        setCustomPlaceholder("%setrank_status%", getStatus(rank.perms().contains(RankPerms.SETRANK)));
        setCustomPlaceholder("%setbase_status%", getStatus(rank.perms().contains(RankPerms.SETBASE)));
        setCustomPlaceholder("%deposit_status%", getStatus(rank.perms().contains(RankPerms.DEPOSIT)));
        setCustomPlaceholder("%withdraw_status%", getStatus(rank.perms().contains(RankPerms.WITHDRAW)));
        setCustomPlaceholder("%pvp_status%", getStatus(rank.perms().contains(RankPerms.PVP)));
        setCustomPlaceholder("%setslogan_status%", getStatus(rank.perms().contains(RankPerms.SETSLOGAN)));
        setCustomPlaceholder("%setprefix_status%", getStatus(rank.perms().contains(RankPerms.SETPREFIX)));
        setCustomPlaceholder("%rank%", rank.name());
    }

    private String getStatus(boolean status) {
        if (status) {
            return getPlugin().getLang().getMessage("rank-perm-yes");
        } else {
            return getPlugin().getLang().getMessage("rank-perm-no");
        }
    }

    public TreexClans getPlugin() {
        return (TreexClans) super.getPlugin();
    }
}
