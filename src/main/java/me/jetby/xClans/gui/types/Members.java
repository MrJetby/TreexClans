package me.jetby.xClans.gui.types;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.gui.*;
import me.jetby.xClans.gui.requirements.ClickRequirement;
import me.jetby.xClans.gui.requirements.Requirements;
import me.jetby.xClans.gui.requirements.ViewRequirement;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Member;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.xClans.TreexClans.NAMESPACED_KEY;

public class Members extends TGui {
    private final List<Integer> freeSlots = new ArrayList<>();

    private final Menu menu;
    private final Player player;
    private final Clan clan;

    public Members(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        this.menu = menu;
        this.player = player;
        this.clan = clan;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        size(menu.size());
        type(menu.inventoryType());
        title(Papi.setPapi(player, menu.title()));

        onDrag(event -> event.setCancelled(true));

        registerItem("prev_page", b -> b.slots(45)
                .defaultItem(ItemWrapper.builder(Material.ARROW).build())
                .defaultClickHandler((e, gui) -> {
                    e.setCancelled(true);
                    previousPage();
                }));

        registerItem("next_page", b -> b.slots(53)
                .defaultItem(ItemWrapper.builder(Material.ARROW).build())
                .defaultClickHandler((e, gui) -> {
                    e.setCancelled(true);
                    nextPage();
                }));

        registerStaticButtons();

        setupMembersPagination();

        openPage(0);
    }

    private void registerStaticButtons() {
        Map<Integer, List<Button>> buttonsBySlot = new HashMap<>();
        for (Button button : menu.buttons()) {
            if ("members".equals(button.type())) continue;
            buttonsBySlot.computeIfAbsent(button.slot(), k -> new ArrayList<>()).add(button);
        }

        for (Map.Entry<Integer, List<Button>> entry : buttonsBySlot.entrySet()) {
            int slot = entry.getKey();
            List<Button> slotButtons = entry.getValue();

            slotButtons.sort(Comparator.comparingInt(Button::priority).reversed());

            Button selectedButton = null;

            for (Button button : slotButtons) {
                boolean visible = true;

                if (!button.viewRequirements().isEmpty()) {
                    for (ViewRequirement requirement : button.viewRequirements()) {
                        boolean passed = Requirements.check(player, requirement);
                        if (!passed) {
                            visible = false;
                            break;
                        }
                    }
                }

                if (visible) {
                    selectedButton = button;
                    break;
                }
            }

            if (selectedButton != null) {
                Button finalSelectedButton = selectedButton;
                registerItem(finalSelectedButton.id() + finalSelectedButton.slot(), builder -> {
                    builder.slots(finalSelectedButton.slot());

                    ItemStack itemStack;
                    ItemWrapper wrapper;

                    if ("leader".equals(finalSelectedButton.type())) {
                        Member leaderMember = clan.getLeader();
                        if (leaderMember == null) return;

                        OfflinePlayer target = Bukkit.getOfflinePlayer(leaderMember.getUuid());
                        itemStack = SkullCreator.itemFromName(target.getName());
                        wrapper = new ItemWrapper(itemStack);

                        String rawDisplayName = finalSelectedButton.displayName();
                        String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, leaderMember, target);
                        processedDisplayName = Papi.setPapi(player, processedDisplayName);
                        wrapper.displayName(Colorize.text(processedDisplayName));

                        List<String> rawLore = finalSelectedButton.lore();
                        List<String> processedLore = rawLore.stream()
                                .map(l -> replaceMemberPlaceholders(l, leaderMember, target))
                                .map(l -> Papi.setPapi(player, l))
                                .map(Colorize::text)
                                .collect(Collectors.toList());
                        wrapper.lore(processedLore);
                    } else {
                        itemStack = finalSelectedButton.itemStack().clone();
                        wrapper = new ItemWrapper(itemStack);

                        String rawDisplayName = finalSelectedButton.displayName();
                        String processedDisplayName = Papi.setPapi(player, rawDisplayName);
                        wrapper.displayName(Colorize.text(processedDisplayName));

                        List<String> rawLore = finalSelectedButton.lore();
                        List<String> processedLore = rawLore.stream()
                                .map(l -> Papi.setPapi(player, l))
                                .map(Colorize::text)
                                .collect(Collectors.toList());
                        wrapper.lore(processedLore);
                    }

                    wrapper.customModelData(finalSelectedButton.customModelData());
                    wrapper.enchanted(finalSelectedButton.enchanted());
                    wrapper.update();

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                    itemStack.setItemMeta(itemMeta);

                    builder.defaultItem(wrapper);

                    builder.defaultClickHandler((event, controller) -> {
                        event.setCancelled(true);
                        ClickType clickType = event.getClick();

                        for (Command cmd : finalSelectedButton.commands()) {
                            if (cmd.clickType() == clickType || cmd.anyClick()) {
                                boolean allRequirementsPassed = true;
                                if (!cmd.clickRequirements().isEmpty()) {
                                    for (ClickRequirement clickRequirement : cmd.clickRequirements()) {
                                        if ((clickRequirement.anyClick() || clickRequirement.clickType() == clickType)) {
                                            if (!Requirements.check(player, clickRequirement)) {
                                                Requirements.runDenyCommands(player, clickRequirement.deny_commands(), finalSelectedButton);
                                                allRequirementsPassed = false;
                                                break;
                                            }
                                        }
                                    }
                                }

                                if (allRequirementsPassed) {
                                    ActionContext ctx = new ActionContext(player);
                                    ctx.put("button", finalSelectedButton);
                                    ActionExecutor.execute(ctx, ActionRegistry.transform(cmd.actions()));
                                    break;
                                }
                            }
                        }
                    });
                });
            }
        }
    }

    private void setupMembersPagination() {
        List<Button> memberButtons = menu.buttons().stream()
                .filter(b -> "members".equals(b.type()))
                .collect(Collectors.toList());

        if (memberButtons.isEmpty()) return;

        List<Integer> memberSlots = memberButtons.stream()
                .map(Button::slot)
                .sorted()
                .collect(Collectors.toList());

        int itemsPerPage = memberSlots.size();

        List<Member> members = clan.getMembers().stream()
                .filter(m -> !m.equals(clan.getLeader()))
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) members.size() / itemsPerPage);

        Button memberButton = memberButtons.get(0); // Assume all have same config

        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, members.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int memberIndex = start + i;
                int slot = memberSlots.get(i);

                if (memberIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }

                Member member = members.get(memberIndex);
                OfflinePlayer target = Bukkit.getOfflinePlayer(member.getUuid());

                consumers[i] = builder -> {
                    ItemStack itemStack = SkullCreator.itemFromName(target.getName());
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    String rawDisplayName = memberButton.displayName();
                    String processedDisplayName = replaceMemberPlaceholders(rawDisplayName, member, target);
                    processedDisplayName = Papi.setPapi(player, processedDisplayName);
                    wrapper.displayName(Colorize.text(processedDisplayName));

                    List<String> rawLore = memberButton.lore();
                    List<String> processedLore = rawLore.stream()
                            .map(l -> replaceMemberPlaceholders(l, member, target))
                            .map(l -> Papi.setPapi(player, l))
                            .map(Colorize::text)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);

                    wrapper.customModelData(memberButton.customModelData());
                    wrapper.enchanted(memberButton.enchanted());
                    wrapper.update();

                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                };
            }

            addPage(consumers);
        }
    }

    private String replaceMemberPlaceholders(String text, Member member, OfflinePlayer target) {
        text = text.replace("%player_name%", target.getName());
        text = text.replace("%rank%", member.getRank().name());
//        text = text.replace("%kills%", String.valueOf(member.getKills() != null ? member.getKills() : 0));
//        text = text.replace("%deaths%", String.valueOf(member.getDeaths() != null ? member.getDeaths() : 0));
//        text = text.replace("%kd%", String.valueOf(calculateKD(member)));
//        text = text.replace("%war_wins%", String.valueOf(member.getWarWins() != null ? member.getWarWins() : 0));
//        text = text.replace("%war_participated%", String.valueOf(member.getWarParticipated() != null ? member.getWarParticipated() : 0));
//        text = text.replace("%war_loses%", String.valueOf(member.getWarLoses() != null ? member.getWarLoses() : 0));
//        text = text.replace("%exp%", String.valueOf(member.getExp() != null ? member.getExp() : 0));
//        text = text.replace("%coin%", String.valueOf(member.getCoin() != null ? member.getCoin() : 0));
        return text;
    }

//    private double calculateKD(Member member) {
//        int kills = member.getKills() != null ? member.getKills() : 0;
//        int deaths = member.getDeaths() != null ? member.getDeaths() : 0;
//        return deaths == 0 ? kills : (double) kills / deaths;
//    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Inventory topInventory = p.getOpenInventory().getTopInventory();
        Inventory clickedInv = e.getClickedInventory();
        int rawSlot = e.getRawSlot();
        ClickType click = e.getClick();

        if (holder().getInventory() == null || !holder().getInventory().equals(topInventory)) return;

        if (clickedInv != null && clickedInv.equals(topInventory)) {
            if (!freeSlots.contains(rawSlot)) {
                if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
                } else {
                    e.setCancelled(true);
                }
                return;
            }
        }

        if ((click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT)
                && (clickedInv == null || clickedInv.equals(p.getInventory()))) {
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) {
                return;
            }

            int remaining = clicked.getAmount();

            for (int slot : freeSlots) {
                ItemStack slotItem = holder().getInventory().getItem(slot);

                if (slotItem == null || slotItem.getType().isAir()) {
                    ItemStack toPut = clicked.clone();
                    int putAmount = Math.min(remaining, toPut.getMaxStackSize());
                    toPut.setAmount(putAmount);
                    holder().getInventory().setItem(slot, toPut);
                    remaining -= putAmount;
                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    } else {
                        continue;
                    }
                }

                if (slotItem.isSimilar(clicked) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                    int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                    int toAdd = Math.min(space, remaining);
                    slotItem.setAmount(slotItem.getAmount() + toAdd);
                    holder().getInventory().setItem(slot, slotItem);
                    remaining -= toAdd;
                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    }
                }
            }

            if (remaining > 0) {
                ItemStack left = clicked.clone();
                left.setAmount(remaining);
                e.setCurrentItem(left);
            } else {
                e.setCurrentItem(null);
            }

            return;
        }

        if (rawSlot < holder().getInventory().getSize() && !freeSlots.contains(rawSlot)) {
            e.setCancelled(true);
            return;
        }
    }

    @Override
    public GuiType guiType() {
        return GuiType.MEMBERS;
    }
}