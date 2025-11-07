package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.gui.Button;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.Menu;
import me.jetby.treexclans.gui.SkullCreator;
import me.jetby.treexclans.tools.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class MembersGui extends Gui {


    public MembersGui(TreexClans plugin, Menu menu, Player player, Clan clanImpl) {
        super(plugin, menu, player, clanImpl);
        registerButtons();

        setupMembersPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "members": {
                break;
            }
            case "leader": {
                var leaderMemberImpl = getClanImpl().getLeader();
                if (leaderMemberImpl == null) {
                    builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                    break;
                }
                replaceMemberPlaceholders(leaderMemberImpl);

                OfflinePlayer target = Bukkit.getOfflinePlayer(leaderMemberImpl.getUuid());
                ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                ItemWrapper wrapper = new ItemWrapper(itemStack);

                wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                wrapper.lore(button.lore().stream()
                        .map(this::applyDefaultPlaceholders)
                        .collect(Collectors.toList()));

                wrapper.customModelData(button.customModelData());
                wrapper.enchanted(button.enchanted());
                wrapper.update();

                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                    itemStack.setItemMeta(itemMeta);
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
        return button != null && button.type().equals("members");
    }

    private void setupMembersPagination() {

        List<Button> memberButtons = getMenu().buttons().stream()
                .filter(b -> "members".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedMemberSlots = memberButtons.stream().map(Button::slot).toList();
        if (memberButtons.isEmpty()) return;

        int itemsPerPage = sortedMemberSlots.size();

        List<Member> memberImpls = getClanImpl().getMembers().stream()
                .filter(m -> !m.equals(getClanImpl().getLeader()))
                .toList();

        int totalPages = (int) Math.ceil((double) memberImpls.size() / itemsPerPage);

        Button button = memberButtons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, memberImpls.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int memberIndex = start + i;
                int slot = sortedMemberSlots.get(i);

                if (memberIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                var memberImpl = memberImpls.get(memberIndex);
                OfflinePlayer target = Bukkit.getOfflinePlayer(memberImpl.getUuid());

                consumers[i] = builder -> {
                    replaceMemberPlaceholders(memberImpl);
                    ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                    wrapper.lore(button.lore().stream()
                            .map(this::applyDefaultPlaceholders)
                            .collect(Collectors.toList()));

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

    private void replaceMemberPlaceholders(Member memberImpl) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberImpl.getUuid());
        setCustomPlaceholder("%joined-at%", getPlugin().getFormatTime().stringFormat(System.currentTimeMillis() - memberImpl.getJoinedAt()));
        setCustomPlaceholder("%last-online%", getPlugin().getClanManager().lookup().getLastOnlineFormatted(memberImpl));
        setCustomPlaceholder("%target_name%", offlinePlayer.getName());
        setCustomPlaceholder("%rank%", memberImpl.getRank().name());
        setCustomPlaceholder("%kills%", String.valueOf(memberImpl.getKills()));
        setCustomPlaceholder("%deaths%", String.valueOf(memberImpl.getDeaths()));
        setCustomPlaceholder("%kd%", calculateKD(memberImpl));
        setCustomPlaceholder("%exp%", String.valueOf(memberImpl.getExp()));
        setCustomPlaceholder("%coin%", String.valueOf(memberImpl.getCoin()));
    }

    private String calculateKD(Member memberImpl) {
        int kills = memberImpl.getKills();
        int deaths = memberImpl.getDeaths();
        return deaths == 0 ? kills + "" : NumberUtils.formatWithCommas((double) kills / deaths);
    }
}