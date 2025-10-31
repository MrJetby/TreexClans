package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import me.jetby.treexclans.tools.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TopClansGui extends Gui {

    public TopClansGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        registerButtons();
        setupMembersPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "clans": {
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
    public boolean cancelRegistration(Player player, @javax.annotation.Nullable Button button) {
        return button!=null && button.type().equals("clans");
    }

    private void setupMembersPagination() {
        List<Button> clanButtons = getMenu().buttons().stream()
                .filter(b -> "clans".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedClansSlots = clanButtons.stream().map(Button::slot).toList();
        if (clanButtons.isEmpty()) return;

        int itemsPerPage = sortedClansSlots.size();

        List<Clan> clans = getPlugin().getCfg().getClans().values().stream().toList();

        int totalPages = (int) Math.ceil((double) clans.size() / itemsPerPage);

        Button button = clanButtons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, clans.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int memberIndex = start + i;
                int slot = sortedClansSlots.get(i);

                if (memberIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                final Clan clan = clans.get(memberIndex);

                consumers[i] = builder -> {
                    ItemWrapper wrapper = new ItemWrapper(button.itemStack().clone());

                    String processedDisplayName = setPlaceholders(
                            applyDefaultPlaceholders(button.displayName()),
                            clan
                    );
                    processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
                    wrapper.displayName(Colorize.text(processedDisplayName));

                    List<String> processedLore = button.lore().stream()
                            .map(l -> applyDefaultPlaceholders(l))
                            .map(l -> setPlaceholders(l, clan))
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
            if (consumers[page] == null) continue;
            addPage(consumers);
        }
    }

    private String setPlaceholders(String text, Clan clan) {
        if (text == null) return null;

        text = text.replace("%level%", clan.getLevel().id());

        int kills = 0;
        int deaths = 0;
        for (Member member : clan.getMembersWithLeader()) {
            kills += member.getKills();
            deaths += member.getDeaths();
        }

        if (clan.getPrefix() != null) {
            text = text.replace("%prefix%", clan.getPrefix());
        } else {
            text = text.replace("%prefix%", clan.getId().toUpperCase());
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(clan.getLeader().getUuid());
        String leaderName = leader.getName() != null ? leader.getName() : "Unknown";
        text = text.replace("%leader_name%", leaderName);
        text = text.replace("%kills%", String.valueOf(kills));
        text = text.replace("%deaths%", String.valueOf(deaths));
        text = text.replace("%kd%", calculateKD(kills, deaths));
        text = text.replace("%balance%", String.valueOf(clan.getBalance()));

        return text;
    }

    private String calculateKD(int kills, int deaths) {
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }

}
