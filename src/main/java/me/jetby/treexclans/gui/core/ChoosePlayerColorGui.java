package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.gui.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChoosePlayerColorGui extends Gui {
    public ChoosePlayerColorGui(TreexClans plugin, @Nullable Menu menu, Player player, Clan clan, Member target) {
        super(plugin, menu, player, clan);
        registerButtons();

        size(menu.size());
        type(menu.inventoryType());
        title(Papi.setPapi(player, menu.title()));

        setupMembersPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
            case "players": {
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
        return button!=null && button.type().equals("players");
    }

    private void setupMembersPagination() {

        List<Button> memberButtons = getMenu().buttons().stream()
                .filter(b -> "players".equalsIgnoreCase(b.type()))
                .toList();

        List<Integer> sortedMemberSlots = memberButtons.stream().map(Button::slot).toList();
        if (memberButtons.isEmpty()) return;

        int itemsPerPage = sortedMemberSlots.size();

        List<Member> members = new ArrayList<>(getClan().getMembers());
        members.add(getClan().getLeader());
        members.removeIf(m -> m.getUuid().equals(getPlayer().getUniqueId()));


        int totalPages = (int) Math.ceil((double) members.size() / itemsPerPage);

        Button memberButton = memberButtons.get(0);

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, members.size());

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

                Member member = members.get(memberIndex);
                if (member.equals(getClan().getMember(getPlayer().getUniqueId()))) continue;

                OfflinePlayer target = Bukkit.getOfflinePlayer(member.getUuid());

                consumers[i] = builder -> {
                    ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    String rawDisplayName = memberButton.displayName();
                    String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, member);
                    processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
                    wrapper.displayName(Colorize.text(processedDisplayName));

                    List<String> rawLore = memberButton.lore();
                    List<String> processedLore = rawLore.stream()
                            .map(l -> replaceMemberPlaceholders(l, member))
                            .map(l -> Papi.setPapi(getPlayer(), l))
                            .map(Colorize::text)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(memberButton.customModelData());
                    wrapper.enchanted(memberButton.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) ->  {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                GuiFactory.create(
                                                getPlugin(),
                                                getPlugin().getGuiLoader().getMenus().get(memberButton.openGui()),
                                                getPlayer(), getClan(), null, member, null)
                                        .open(getPlayer()), 1L);
                    });
                };
            }
            addPage(consumers);
        }
    }

    private String replaceMemberPlaceholders(String text, Member member) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(member.getUuid());
        text = text.replace("%joined-at%", getPlugin().getFormatTime().stringFormat(System.currentTimeMillis() - member.getJoinedAt()));
        text = text.replace("%last-online%", getPlugin().getClanManager().getLastOnlineFormatted(member));
        text = text.replace("%player_name%", offlinePlayer.getName());
        text = text.replace("%rank%", member.getRank().name());
        text = text.replace("%kills%", String.valueOf(member.getKills()));
        text = text.replace("%deaths%", String.valueOf(member.getDeaths()));
        text = text.replace("%war_wins%", String.valueOf(member.getWarWins()));
        text = text.replace("%war_participated%", String.valueOf(member.getWarParticipated()));
        text = text.replace("%war_loses%", String.valueOf(member.getWarLoses()));
        text = text.replace("%exp%", String.valueOf(member.getExp()));
        text = text.replace("%coin%", String.valueOf(member.getCoin()));
        return text;
    }

}