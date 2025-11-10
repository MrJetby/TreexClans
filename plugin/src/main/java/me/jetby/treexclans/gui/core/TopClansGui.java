package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.InstanceFactory;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.gui.Button;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.api.gui.Menu;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import me.jetby.treexclans.gui.*;
import me.jetby.treexclans.tools.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class TopClansGui extends Gui {

    private final LeaderboardService.TopType currentSort;
    private int s;

    public TopClansGui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clanImpl, LeaderboardService.TopType topType, int s) {
        super(plugin, menu, player, clanImpl);
        this.s = s;
        this.currentSort = Objects.requireNonNullElse(topType, LeaderboardService.TopType.KILLS);
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
            case "filter": {
                if (s + 1 > getTops(button).size()) s = 0;
                builder.defaultItem(ItemWrapper.builder(button.itemStack().getType())
                        .displayName(applyDefaultPlaceholders(button.displayName()))
                        .lore(button.lore()
                                .stream()
                                .map(this::getCurrentSort)
                                .map(this::applyDefaultPlaceholders)
                                .toList())
                        .build());
                builder.defaultClickHandler((event, controller) -> {
                    close(player);
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                        Gui gui = InstanceFactory.GUI_FACTORY.create(getPlugin(), getMenu(), player, getClanImpl(), getTops(button).get(s), s + 1);
                        gui.open(player);
                    }, 1L);
                });
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
        return button != null && (button.type().equalsIgnoreCase("clans"));
    }

    private void setupMembersPagination() {
        List<Button> clanButtons = getMenu().buttons().stream()
                .filter(b -> "clans".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedClansSlots = clanButtons.stream().map(Button::slot).toList();
        if (clanButtons.isEmpty()) return;

        int itemsPerPage = sortedClansSlots.size();

        List<Clan> allClanImpls = new ArrayList<>();
        int maxClansToShow = 10000;

        for (int i = 1; i <= maxClansToShow; i++) {
            Clan clanImpl = getPlugin().getLeaderboardService().getTopClan(currentSort, i);
            if (clanImpl != null) {
                allClanImpls.add(clanImpl);
            } else {
                break;
            }
        }

        if (allClanImpls.isEmpty()) {
            Bukkit.getLogger().warning("Top clans list is empty!");
            return;
        }

        int totalPages = (int) Math.ceil((double) allClanImpls.size() / itemsPerPage);

        Button button = clanButtons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, allClanImpls.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int clanIndex = start + i;
                int slot = sortedClansSlots.get(i);
                int topNum = clanIndex + 1;

                if (clanIndex >= end || clanIndex >= allClanImpls.size()) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                final Clan clanImpl = allClanImpls.get(clanIndex);
                if (clanImpl == null) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                int finalTopNum = topNum;

                consumers[i] = builder -> {
                    setPlaceholders(clanImpl);
                    ItemStack itemStack = SkullCreator.itemFromUuid(clanImpl.getLeader().getUuid());
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "clans");
                    itemStack.setItemMeta(meta);
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    setCustomPlaceholder("%top_num%", String.valueOf(finalTopNum));
                    wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                    List<String> processedLore = button.lore().stream()
                            .map(this::applyDefaultPlaceholders)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(button.customModelData());
                    wrapper.enchanted(button.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                };
            }

            addPage(consumers);
        }
    }

    private List<LeaderboardService.TopType> getTops(Button button) {
        List<LeaderboardService.TopType> list = new ArrayList<>();
        for (String s : button.lore()) {
            if (s.contains("%top_kills_set%")) {
                list.add(LeaderboardService.TopType.KILLS);
                continue;
            }
            if (s.contains("%top_deaths_set%")) {
                list.add(LeaderboardService.TopType.DEATHS);
                continue;
            }
            if (s.contains("%top_kd_set%")) {
                list.add(LeaderboardService.TopType.KD);
                continue;
            }
            if (s.contains("%top_balance_set%")) {
                list.add(LeaderboardService.TopType.BALANCE);
                continue;
            }
            if (s.contains("%top_level_set%")) {
                list.add(LeaderboardService.TopType.LEVEL);
                continue;
            }
            if (s.contains("%top_members_set%")) {
                list.add(LeaderboardService.TopType.MEMBERS);
            }

        }

        return list;
    }

    private void setPlaceholders(Clan clanImpl) {

        int kills = 0;
        int deaths = 0;
        for (var memberImpl : clanImpl.getMembersWithLeader()) {
            kills += memberImpl.getKills();
            deaths += memberImpl.getDeaths();
        }

        if (clanImpl.getPrefix() != null) {
            setCustomPlaceholder("%prefix%", clanImpl.getPrefix());
        } else {
            setCustomPlaceholder("%prefix%", clanImpl.getId().toUpperCase());
        }

        OfflinePlayer leader = Bukkit.getOfflinePlayer(clanImpl.getLeader().getUuid());
        String leaderName = leader.getName() != null ? leader.getName() : "Unknown";
        setCustomPlaceholder("%slogan%", clanImpl.getSlogan());
        setCustomPlaceholder("%tag%", clanImpl.getId());
        setCustomPlaceholder("%level%", clanImpl.getLevel().id());
        setCustomPlaceholder("%leader_name%", leaderName);
        setCustomPlaceholder("%kills%", String.valueOf(kills));
        setCustomPlaceholder("%deaths%", String.valueOf(deaths));
        setCustomPlaceholder("%kd%", calculateKD(kills, deaths));
        setCustomPlaceholder("%balance%", String.valueOf(clanImpl.getBalance()));
    }

    private String getCurrentSort(String text) {
        if (text == null) return null;
        switch (currentSort) {
            case KILLS ->
                    text = text.replace("%top_kills_set%", getPlugin().getMessages().getMessage("gui.tops.kills.set"));
            case DEATHS ->
                    text = text.replace("%top_deaths_set%", getPlugin().getMessages().getMessage("gui.tops.deaths.set"));
            case KD -> text = text.replace("%top_kd_set%", getPlugin().getMessages().getMessage("gui.tops.kd.set"));
            case BALANCE ->
                    text = text.replace("%top_balance_set%", getPlugin().getMessages().getMessage("gui.tops.balance.set"));
            case LEVEL ->
                    text = text.replace("%top_level_set%", getPlugin().getMessages().getMessage("gui.tops.level.set"));
            case MEMBERS ->
                    text = text.replace("%top_members_set%", getPlugin().getMessages().getMessage("gui.tops.members.set"));
        }
        text = text.replace("%top_kills_set%", getPlugin().getMessages().getMessage("gui.tops.kills.unset"));
        text = text.replace("%top_deaths_set%", getPlugin().getMessages().getMessage("gui.tops.deaths.unset"));
        text = text.replace("%top_kd_set%", getPlugin().getMessages().getMessage("gui.tops.kd.unset"));
        text = text.replace("%top_balance_set%", getPlugin().getMessages().getMessage("gui.tops.balance.unset"));
        text = text.replace("%top_level_set%", getPlugin().getMessages().getMessage("gui.tops.level.unset"));
        text = text.replace("%top_members_set%", getPlugin().getMessages().getMessage("gui.tops.members.unset"));
        return text;
    }

    private String calculateKD(int kills, int deaths) {
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }

    public TreexClans getPlugin() {
        return (TreexClans) super.getPlugin();
    }
}