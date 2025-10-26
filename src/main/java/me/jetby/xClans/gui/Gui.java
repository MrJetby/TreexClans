package me.jetby.xClans.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import lombok.Getter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Papi;
import me.jetby.xClans.gui.requirements.ClickRequirement;
import me.jetby.xClans.gui.requirements.Requirements;
import me.jetby.xClans.gui.requirements.ViewRequirement;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Item;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static me.jetby.xClans.TreexClans.NAMESPACED_KEY;

public class Gui extends AdvancedGui implements Listener {
    @Getter
    private final List<Integer> freeSlots = new ArrayList<>();

    private final Menu menu;
    private final Player player;
    private final Inventory inventory;
    private final Clan clan;

    public Gui(JavaPlugin plugin, Menu menu, Player player, Clan clan) {
        super(menu.title());
        this.menu = menu;
        this.player = player;
        this.inventory = holder().getInventory();
        this.clan = clan;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        size(menu.size());
        type(menu.inventoryType());


        registerButtons();

        onDrag(event -> {
            int topSize = inventory.getSize();
            for (int rawSlot : event.getRawSlots()) {
                if (rawSlot >= topSize) continue;

                if (!freeSlots.contains(rawSlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
            event.setCancelled(false);
        });

        onClose(event -> {
            List<Item> items = new ArrayList<>();

            for (int slot = 0; slot < event.getInventory().getSize(); slot++) {
                if (!freeSlots.contains(slot)) continue;
                ItemStack itemStack = event.getInventory().getItem(slot);
                if (itemStack == null) {
                    continue;
                } else {
                    if (itemStack.getItemMeta().getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING))
                        continue;
                }
                items.add(new Item(null, slot, itemStack));
            }
            clan.setItems(items);
        });

    }


    private void registerButtons() {
        freeSlots.clear();

        Map<Integer, List<Button>> buttonsBySlot = new HashMap<>();
        for (Button button : menu.buttons()) {
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
                registerItem(selectedButton.id() + selectedButton.slot(), builder -> {
                    builder.slots(finalSelectedButton.slot());

                    boolean isFreeSlot = finalSelectedButton.freeSlot();
                    if (!isFreeSlot) {
                        for (ViewRequirement requirement : finalSelectedButton.viewRequirements()) {
                            boolean passed = Requirements.check(player, requirement);
                            if (!passed && requirement.freeSlot()) {
                                isFreeSlot = true;
                                break;
                            }
                        }
                    }

                    if (isFreeSlot) {
                        freeSlots.add(finalSelectedButton.slot());
                        builder.defaultClickHandler((event, controller) -> {
                            event.setCancelled(false);
                        });
                        return;
                    }

                    ItemStack itemStack = finalSelectedButton.itemStack().clone();
                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    wrapper.displayName(Papi.setPapi(player, finalSelectedButton.displayName()));
                    wrapper.lore(Papi.setPapi(player, finalSelectedButton.lore()));
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

        for (Item item : clan.getItems()) {

            if (item == null) continue;
            if (!freeSlots.contains(item.slot())) continue;

            if (inventory.getItem(item.slot()) == null) {
                inventory.setItem(item.slot(), item.itemStack());
            }
        }
    }



    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked( ) instanceof Player p)) return;

        Inventory topInventory = p.getOpenInventory( ).getTopInventory( );
        Inventory clickedInv = e.getClickedInventory( );
        int rawSlot = e.getRawSlot( );
        ClickType click = e.getClick( );

        if (inventory == null || !inventory.equals(topInventory)) return;

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
                && (clickedInv == null || clickedInv.equals(p.getInventory( )))) {
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem( );
            if (clicked == null || clicked.getType( ).isAir( )) {
                return;
            }

            int remaining = clicked.getAmount( );

            for (int slot : freeSlots) {
                ItemStack slotItem = inventory.getItem(slot);

                if (slotItem == null || slotItem.getType( ).isAir( )) {
                    ItemStack toPut = clicked.clone( );
                    int putAmount = Math.min(remaining, toPut.getMaxStackSize( ));
                    toPut.setAmount(putAmount);
                    inventory.setItem(slot, toPut);
                    remaining -= putAmount;
                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    } else {
                        continue;
                    }
                }

                if (slotItem.isSimilar(clicked) && slotItem.getAmount( ) < slotItem.getMaxStackSize( )) {
                    int space = slotItem.getMaxStackSize( ) - slotItem.getAmount( );
                    int toAdd = Math.min(space, remaining);
                    slotItem.setAmount(slotItem.getAmount( ) + toAdd);
                    inventory.setItem(slot, slotItem);
                    remaining -= toAdd;
                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    }
                }
            }

            if (remaining > 0) {
                ItemStack left = clicked.clone( );
                left.setAmount(remaining);
                e.setCurrentItem(left);
            } else {
                e.setCurrentItem(null);
            }

            return;
        }

        if (rawSlot < inventory.getSize( ) && !freeSlots.contains(rawSlot)) {
            e.setCancelled(true);
            return;
        }

    }
}
