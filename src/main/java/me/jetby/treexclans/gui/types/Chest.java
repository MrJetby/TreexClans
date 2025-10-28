package me.jetby.treexclans.gui.types;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import lombok.Getter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.gui.*;
import me.jetby.treexclans.gui.requirements.ClickRequirement;
import me.jetby.treexclans.gui.requirements.Requirements;
import me.jetby.treexclans.gui.requirements.ViewRequirement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class Chest extends TGui {
    private final List<Integer> freeSlots = new ArrayList<>();

    private final Menu menu;
    private final Player player;
    private final Clan clan;

    public Chest(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        this.menu = menu;
        this.player = player;
        this.clan = clan;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        size(menu.size());
        type(menu.inventoryType());
        title(Papi.setPapi(player, menu.title()));

        onDrag(event -> event.setCancelled(true));

        registerStaticButtons();

        setupItemsPagination();

        openPage(0);
    }

    private void registerStaticButtons() {
        Map<Integer, List<Button>> buttonsBySlot = new HashMap<>();
        for (Button button : menu.buttons()) {
            switch (button.type().toLowerCase()) {
                case "members":
                    continue;
                case "next_page": {
                    ItemWrapper item = new ItemWrapper(button.itemStack());
                    item.enchanted(button.enchanted());
                    registerItem("next_page", b -> b.slots(button.slot())
                            .defaultItem(item)
                            .defaultClickHandler((e, gui) -> {
                                e.setCancelled(true);
                                nextPage();
                            }));
                    continue;
                }
                case "prev_page": {
                    ItemWrapper item = new ItemWrapper(button.itemStack());
                    item.enchanted(button.enchanted());
                    registerItem("prev_page", b -> b.slots(button.slot())
                            .defaultItem(item)
                            .defaultClickHandler((e, gui) -> {
                                e.setCancelled(true);
                                previousPage();
                            }));
                    continue;
                }
            }
            buttonsBySlot.computeIfAbsent(button.slot(), k -> new ArrayList<>()).add(button);
        }

        for (Map.Entry<Integer, List<Button>> entry : buttonsBySlot.entrySet()) {
            int slot = entry.getKey();
            List<Button> slotButtons = entry.getValue();

            slotButtons.sort(Comparator.comparingInt(Button::priority).reversed());

            Button selectedButton = null;
            boolean anyFreeSlot = false;

            for (Button button : slotButtons) {
                boolean visible = true;
                boolean freeSlotFromRequirements = false;

                if (!button.viewRequirements().isEmpty()) {
                    for (ViewRequirement requirement : button.viewRequirements()) {
                        boolean passed = Requirements.check(player, requirement);
                        if (!passed) {
                            if (requirement.freeSlot()) {
                                freeSlotFromRequirements = true;
                            } else {
                                visible = false;
                                break;
                            }
                        }
                    }
                }

                if (visible) {
                    selectedButton = button;
                    break;
                } else if (freeSlotFromRequirements) {
                    anyFreeSlot = true;
                }
            }
            if (selectedButton == null && anyFreeSlot) {
                freeSlots.add(slot);
                registerItem("free_slot_" + slot, builder -> {
                    builder.slots(slot);
                    builder.defaultClickHandler((event, controller) -> {
                        event.setCancelled(false);
                    });
                });
                continue;
            }

            if (selectedButton != null) {
                Button finalSelectedButton = selectedButton;
                registerItem(finalSelectedButton.id() + finalSelectedButton.slot(), builder -> {
                    builder.slots(finalSelectedButton.slot());

                    ItemStack itemStack;
                    ItemWrapper wrapper;


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


                    wrapper.customModelData(finalSelectedButton.customModelData());
                    wrapper.enchanted(finalSelectedButton.enchanted());
                    wrapper.update();

                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta!=null) {
                        itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                        itemStack.setItemMeta(itemMeta);
                    }

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

    private void setupItemsPagination() {
        List<Button> itemButtons = menu.buttons().stream()
                .filter(b -> "item".equals(b.type()))
                .toList();

        if (itemButtons.isEmpty()) return;

        List<Integer> itemSlots = itemButtons.stream()
                .map(Button::slot)
                .sorted()
                .toList();

        int itemsPerPage = itemSlots.size();

        List<ItemStack> items = clan.getChest();

        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);


        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, items.size());

            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];

            for (int i = 0; i < itemsPerPage; i++) {
                int itemIndex = start + i;
                int slot = itemSlots.get(i);

                if (itemIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.BARRIER).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }


                consumers[i] = builder -> {
                    builder.slots(slot);
                    builder.defaultItem(new ItemWrapper(items.get(itemIndex)));
                    builder.defaultClickHandler((event, ctrl) -> event.setCancelled(false));
                };
            }

            addPage(consumers);
        }
    }




    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Inventory topInventory = p.getOpenInventory().getTopInventory();
        Inventory clickedInv = e.getClickedInventory();
        int rawSlot = e.getRawSlot();
        ClickType click = e.getClick();

        if (!holder().getInventory().equals(topInventory)) return;

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