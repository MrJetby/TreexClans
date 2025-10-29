package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.clan.rank.Rank;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import me.jetby.treexclans.tools.NumberUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class RanksGui extends Gui {
    public RanksGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);

        setupRanksPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "all_rank": {
                break;
            }
            case "leader_rank": {
                Rank rank = getClan().getLeader().getRank();
                if (rank == null) {
                    break;
                }

                ItemWrapper wrapper = new ItemWrapper(button.itemStack());

                String rawDisplayName = button.displayName();
                String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, rank);
                processedDisplayName = Papi.setPapi(player, processedDisplayName);
                wrapper.displayName(Colorize.text(processedDisplayName));

                List<String> rawLore = button.lore();
                List<String> processedLore = rawLore.stream()
                        .map(l -> replaceMemberPlaceholders(l, rank))
                        .map(l -> Papi.setPapi(player, l))
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
        if (button!=null) {
            return button.type().equals("all_ranks");
        }
        return false;
    }

    private void setupRanksPagination() {

        List<Button> buttons = getMenu().buttons().stream()
                .filter(b -> "all_ranks".equals(b.type()))
                .toList();

        List<Integer> sortedRankSlots = buttons.stream().map(Button::slot).toList();
        if (buttons.isEmpty()) return;

        int itemsPerPage = sortedRankSlots.size();

        Map<String, Rank> ranks = getClan().getRanks();
        List<String> ranksStr = ranks.keySet().stream().toList();


        int totalPages = (int) Math.ceil((double) ranksStr.size() / itemsPerPage);

        Button button = buttons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, ranksStr.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int memberIndex = start + i;
                int slot = sortedRankSlots.get(i);

                if (memberIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                Rank rank = ranks.get(ranksStr.get(memberIndex));

                consumers[i] = builder -> {
                    ItemWrapper wrapper = new ItemWrapper(button.itemStack());

                    String rawDisplayName = button.displayName();
                    String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, rank);
                    processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
                    wrapper.displayName(Colorize.text(processedDisplayName));

                    List<String> rawLore = button.lore();
                    List<String> processedLore = rawLore.stream()
                            .map(l -> replaceMemberPlaceholders(l, rank))
                            .map(l -> Papi.setPapi(getPlayer(), l))
                            .map(Colorize::text)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(button.customModelData());
                    wrapper.enchanted(button.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                };
            }

            addPage(consumers);
        }
    }

    private String replaceMemberPlaceholders(String text, Rank rank) {
        text = text.replace("%invite_status%", getStatus(rank.rankPermissions().invite()));
        text = text.replace("%kick_status%", getStatus(rank.rankPermissions().kick()));
        text = text.replace("%base_status%", getStatus(rank.rankPermissions().base()));
        text = text.replace("%setrank_status%", getStatus(rank.rankPermissions().setrank()));
        text = text.replace("%setbase_status%", getStatus(rank.rankPermissions().setbase()));
        text = text.replace("%deposit_status%", getStatus(rank.rankPermissions().deposit()));
        text = text.replace("%withdraw_status%", getStatus(rank.rankPermissions().withdraw()));
        text = text.replace("%pvp_status%", getStatus(rank.rankPermissions().pvp()));
        text = text.replace("%rank%", rank.name());
        return text;
    }

    private String getStatus(boolean status) {
        if (status) {
            return getPlugin().getLang().getMessage("rank-perm-yes");
        } else {
            return getPlugin().getLang().getMessage("rank-perm-no");
        }
    }

}
