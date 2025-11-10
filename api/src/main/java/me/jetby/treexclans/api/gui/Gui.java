package me.jetby.treexclans.api.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.api.text.SerializerType;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import com.jodexindustries.jguiwrapper.gui.advanced.PaginatedAdvancedGui;
import lombok.Getter;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.InstanceFactory;
import me.jetby.treexclans.api.gui.requirements.ClickRequirement;
import me.jetby.treexclans.api.gui.requirements.Requirements;
import me.jetby.treexclans.api.gui.requirements.ViewRequirement;
import me.jetby.treexclans.api.service.clan.Clan;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class Gui extends PaginatedAdvancedGui implements Listener {

    @Getter
    private final Inventory inventory;
    @Getter
    private final List<Integer> freeSlots = new ArrayList<>();
    @Getter
    private final Menu menu;
    @Getter
    private final Player player;
    @Getter
    private final Clan clanImpl;
    @Getter
    private final JavaPlugin plugin;

    public Gui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clanImpl) {
        super(menu.size(), menu.title());
        this.menu = menu;
        this.player = player;
        this.inventory = holder().getInventory();
        this.clanImpl = clanImpl;
        this.plugin = plugin;
        defaultSerializer = SerializerType.PLAIN;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);


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
    }

    public String applyDefaultPlaceholders(String text) {
        if (text == null) return null;
        if (clanImpl != null) {
            Player leader = Bukkit.getPlayer(clanImpl.getLeader().getUuid());
            if (leader != null) {
                text = text.replace("%clan_leader_name%", leader.getName());
            } else {
                OfflinePlayer offlineLeader = Bukkit.getOfflinePlayer(clanImpl.getLeader().getUuid());
                text = text.replace("%clan_leader_name%", offlineLeader.getName());
            }
            if (getClanImpl().getMember(player.getUniqueId()) != null) {
                text = text.replace("%clan_coin%", String.valueOf(getClanImpl().getMember(player.getUniqueId()).getCoin()));
            }
        }
        for (Map.Entry<String, String> entry : customPlaceholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private final Map<String, String> customPlaceholders = new HashMap<>();

    public Map<String, String> setCustomPlaceholder(String target, String replacement) {
        customPlaceholders.put(target, replacement);
        return customPlaceholders;
    }

    protected void registerButtons() {
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
                if (!cancelRegistration(player, null)) {
                    registerItem("free_slot_" + slot, builder -> {
                        builder.slots(slot);
                        builder.defaultClickHandler((event, controller) -> {
                            event.setCancelled(false);
                            onClick(player, null, controller);
                        });

                        onRegister(player, null, builder);
                    });
                }
                continue;
            }

            if (selectedButton != null) {
                Button finalSelectedButton = selectedButton;
                if (!cancelRegistration(player, selectedButton)) {

                    registerItem(selectedButton.id() + selectedButton.slot(), builder -> {
                        if (finalSelectedButton.slot() != -1) {
                            builder.slots(finalSelectedButton.slot());
                        }

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
                                onClick(player, finalSelectedButton, controller);
                            });
                            return;
                        }

                        ItemStack itemStack = finalSelectedButton.itemStack().clone();
                        ItemWrapper wrapper = new ItemWrapper(itemStack);

                        wrapper.displayName(Colorize.text(Papi.setPapi(player, applyDefaultPlaceholders(finalSelectedButton.displayName()))));
                        List<String> processedLore = finalSelectedButton.lore().stream()
                                .map(this::applyDefaultPlaceholders)
                                .map(l -> Papi.setPapi(player, l))
                                .toList();
                        wrapper.lore(Papi.setPapi(player, Colorize.list(processedLore)));
                        wrapper.customModelData(finalSelectedButton.customModelData());
                        wrapper.enchanted(finalSelectedButton.enchanted());
                        wrapper.update();

                        ItemMeta itemMeta = itemStack.getItemMeta();
                        itemMeta.getPersistentDataContainer().set(InstanceFactory.ITEM_KEY, PersistentDataType.STRING, "menu_item");
                        itemStack.setItemMeta(itemMeta);

                        builder.defaultItem(wrapper);

                        builder.defaultClickHandler((event, controller) -> {
                            event.setCancelled(true);
                            ClickType clickType = event.getClick();

                            for (ButtonCommand cmd : finalSelectedButton.buttonCommands()) {
                                if (cmd.clickType() == clickType || cmd.anyClick()) {
                                    boolean allRequirementsPassed = true;
                                    if (!cmd.clickRequirements().isEmpty()) {
                                        for (ClickRequirement clickRequirement : cmd.clickRequirements()) {
                                            if ((clickRequirement.anyClick() || clickRequirement.clickType() == clickType)) {
                                                if (!Requirements.check(player, clickRequirement)) {
                                                    ActionContext ctx = new ActionContext(player);
                                                    ctx.put("button", finalSelectedButton);
                                                    ctx.put("clan", clanImpl);
                                                    List<String> commands = new ArrayList<>(clickRequirement.deny_commands());
                                                    commands = commands.stream()
                                                            .map(this::applyDefaultPlaceholders)
                                                            .map(l -> Papi.setPapi(player, l))
                                                            .map(Colorize::text)
                                                            .toList();
                                                    ActionExecutor.execute(ctx, ActionRegistry.transform(commands));
                                                    allRequirementsPassed = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    if (allRequirementsPassed) {
                                        ActionContext ctx = new ActionContext(player);
                                        ctx.put("button", finalSelectedButton);
                                        ctx.put("clan", clanImpl);
                                        List<String> commands = new ArrayList<>(cmd.actions());
                                        commands = commands.stream()
                                                .map(this::applyDefaultPlaceholders)
                                                .map(l -> Papi.setPapi(player, l))
                                                .map(Colorize::text)
                                                .toList();
                                        ActionExecutor.execute(ctx, ActionRegistry.transform(commands));
                                        break;
                                    }
                                }
                            }
                            onClick(player, finalSelectedButton, controller);

                        });
                        onRegister(player, finalSelectedButton, builder);
                    });
                }

            }
        }
    }

    protected void onClick(Player player, Button button, GuiItemController controller) {
    }

    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
    }

    protected boolean cancelRegistration(Player player, @Nullable Button button) {
        return false;
    }

    protected void onInventoryClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Inventory topInventory = p.getOpenInventory().getTopInventory();
        Inventory clickedInv = e.getClickedInventory();
        int rawSlot = e.getRawSlot();
        ClickType click = e.getClick();

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
                && (clickedInv == null || clickedInv.equals(p.getInventory()))) {
            e.setCancelled(true);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) {
                return;
            }

            int remaining = clicked.getAmount();

            for (int slot : freeSlots) {
                ItemStack slotItem = inventory.getItem(slot);

                if (slotItem == null || slotItem.getType().isAir()) {
                    ItemStack toPut = clicked.clone();
                    int putAmount = Math.min(remaining, toPut.getMaxStackSize());
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

                if (slotItem.isSimilar(clicked) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                    int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                    int toAdd = Math.min(space, remaining);
                    slotItem.setAmount(slotItem.getAmount() + toAdd);
                    inventory.setItem(slot, slotItem);
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

        if (rawSlot < inventory.getSize() && !freeSlots.contains(rawSlot)) {
            e.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        onInventoryClickEvent(e);
    }
}
