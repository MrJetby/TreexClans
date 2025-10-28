package me.jetby.treexclans.gui.types;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.actions.ActionContext;
import me.jetby.treex.actions.ActionExecutor;
import me.jetby.treex.actions.ActionRegistry;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class Chest extends TGui {
    private static final Map<String, Set<Chest>> ACTIVE_CHESTS = new HashMap<>();

    private final TreexClans plugin;
    private final Menu menu;
    private final Player player;
    private final Clan clan;

    private final Map<Integer, Integer> slotToGlobalIndex = new HashMap<>();

    private int currentPage = 0;
    private BukkitTask autoSaveTask;
    private boolean isInitialized = false;

    public Chest(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);
        this.plugin = plugin;
        this.menu = menu;
        this.player = player;
        this.clan = clan;

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        size(menu.size());
        type(menu.inventoryType());
        title(Papi.setPapi(player, menu.title()));

        onDrag(event -> event.setCancelled(true));

        onClose(event -> {
            if (autoSaveTask != null) {
                autoSaveTask.cancel();
            }
            saveToCloudData();
            unregisterChest();
        });

        registerToActiveChests();
        registerStaticButtons();
        setupItemsPages();

        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::saveToCloudData, 100L, 100L);

        openPage(0);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            loadPageFromCloudData();
            isInitialized = true;
        }, 3L);
    }

    private void registerToActiveChests() {
        ACTIVE_CHESTS.computeIfAbsent(clan.getId(), k -> new HashSet<>()).add(this);
    }

    private void unregisterChest() {
        Set<Chest> chests = ACTIVE_CHESTS.get(clan.getId());
        if (chests != null) {
            chests.remove(this);
            if (chests.isEmpty()) {
                ACTIVE_CHESTS.remove(clan.getId());
            }
        }
    }

    private void registerStaticButtons() {
        Map<Integer, List<Button>> buttonsBySlot = new HashMap<>();

        for (Button button : menu.buttons()) {
            String type = button.type().toLowerCase();

            if ("item".equals(type) || "chest".equals(type)) {
                continue;
            }

            if ("next_page".equals(type)) {
                registerItem("next_page", builder -> {
                    builder.slots(button.slot());
                    ItemWrapper wrapper = new ItemWrapper(button.itemStack().clone());
                    wrapper.displayName(Colorize.text(Papi.setPapi(player, button.displayName())));
                    wrapper.enchanted(button.enchanted());
                    builder.defaultItem(wrapper);
                    builder.defaultClickHandler((e, gui) -> {
                        e.setCancelled(true);
                        if (currentPage < getTotalPages() - 1) {
                            saveToCloudData();
                            currentPage++;
                            nextPage();
                            Bukkit.getScheduler().runTaskLater(plugin, this::loadPageFromCloudData, 1L);
                        }
                    });
                });
                continue;
            }

            if ("prev_page".equals(type)) {
                registerItem("prev_page", builder -> {
                    builder.slots(button.slot());
                    ItemWrapper wrapper = new ItemWrapper(button.itemStack().clone());
                    wrapper.displayName(Colorize.text(Papi.setPapi(player, button.displayName())));
                    wrapper.enchanted(button.enchanted());
                    builder.defaultItem(wrapper);
                    builder.defaultClickHandler((e, gui) -> {
                        e.setCancelled(true);
                        if (currentPage > 0) {
                            saveToCloudData();
                            currentPage--;
                            previousPage();
                            Bukkit.getScheduler().runTaskLater(plugin, this::loadPageFromCloudData, 1L);
                        }
                    });
                });
                continue;
            }

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
                        if (!Requirements.check(player, requirement)) {
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
                Button finalButton = selectedButton;
                registerItem(finalButton.id() + "_" + slot, builder -> {
                    builder.slots(slot);

                    ItemStack itemStack = finalButton.itemStack().clone();
                    ItemWrapper wrapper = new ItemWrapper(itemStack);
                    wrapper.displayName(Colorize.text(Papi.setPapi(player, finalButton.displayName())));

                    List<String> lore = finalButton.lore().stream()
                            .map(l -> Colorize.text(Papi.setPapi(player, l)))
                            .collect(Collectors.toList());
                    wrapper.lore(lore);
                    wrapper.customModelData(finalButton.customModelData());
                    wrapper.enchanted(finalButton.enchanted());
                    wrapper.update();

                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "static_button");
                        itemStack.setItemMeta(meta);
                    }

                    builder.defaultItem(wrapper);
                    builder.defaultClickHandler((event, controller) -> {
                        event.setCancelled(true);

                        ClickType clickType = event.getClick();
                        for (Command cmd : finalButton.commands()) {
                            if (cmd.clickType() == clickType || cmd.anyClick()) {
                                boolean allPassed = true;

                                for (ClickRequirement req : cmd.clickRequirements()) {
                                    if (req.anyClick() || req.clickType() == clickType) {
                                        if (!Requirements.check(player, req)) {
                                            Requirements.runDenyCommands(player, req.deny_commands(), finalButton);
                                            allPassed = false;
                                            break;
                                        }
                                    }
                                }

                                if (allPassed) {
                                    ActionContext ctx = new ActionContext(player);
                                    ctx.put("button", finalButton);
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

    private void setupItemsPages() {
        List<Button> itemButtons = menu.buttons().stream()
                .filter(b -> "item".equals(b.type()) || "chest".equals(b.type()))
                .toList();

        if (itemButtons.isEmpty()) return;

        List<Integer> configSlots = itemButtons.stream()
                .map(Button::slot)
                .sorted()
                .distinct()
                .toList();

        int slotsPerPage = configSlots.size();
        int maxChestSlots = clan.getLevel().chest();
        int totalPages = (int) Math.ceil((double) maxChestSlots / slotsPerPage);
        if (totalPages == 0) totalPages = 1;

        for (int page = 0; page < totalPages; page++) {
            Consumer<GuiItemController.Builder>[] consumers = new Consumer[slotsPerPage];

            for (int i = 0; i < slotsPerPage; i++) {
                int globalIndex = page * slotsPerPage + i;
                int guiSlot = configSlots.get(i);

                if (globalIndex >= maxChestSlots) {
                    consumers[i] = builder -> {
                        builder.slots(guiSlot);
                        ItemWrapper barrier = ItemWrapper.builder(Material.BARRIER)
                                .displayName("§c§lСлот заблокирован")
                                .lore(Arrays.asList(
                                        "§7Этот слот недоступен.",
                                        "§7Повысьте уровень клана,",
                                        "§7чтобы разблокировать больше слотов."
                                ))
                                .build();

                        ItemMeta meta = barrier.itemStack().getItemMeta();
                        if (meta != null) {
                            meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "locked_slot");
                            barrier.itemStack().setItemMeta(meta);
                        }

                        builder.defaultItem(new ItemWrapper(barrier.itemStack()));
                        builder.defaultClickHandler((e, ctrl) -> e.setCancelled(true));
                    };
                } else {
                    consumers[i] = builder -> {
                        builder.slots(guiSlot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((e, ctrl) -> {
                            e.setCancelled(false);
                        });
                    };
                }
            }

            addPage(consumers);
        }
    }

    private int getTotalPages() {
        List<Button> itemButtons = menu.buttons().stream()
                .filter(b -> "item".equals(b.type()) || "chest".equals(b.type()))
                .toList();

        if (itemButtons.isEmpty()) return 1;

        int slotsPerPage = (int) itemButtons.stream().map(Button::slot).distinct().count();
        int maxChestSlots = clan.getLevel().chest();
        return (int) Math.ceil((double) maxChestSlots / slotsPerPage);
    }

    private void saveToCloudData() {
        if (!isInitialized) return;

        Inventory inv = holder().getInventory();

        List<ItemStack> chestData = clan.getChest();

        updateSlotMapping();

        int maxIndex = slotToGlobalIndex.values().stream()
                .max(Integer::compare)
                .orElse(-1);

        while (chestData.size() <= maxIndex) {
            chestData.add(null);
        }

        for (Map.Entry<Integer, Integer> entry : slotToGlobalIndex.entrySet()) {
            int guiSlot = entry.getKey();
            int globalIndex = entry.getValue();

            ItemStack item = inv.getItem(guiSlot);

            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) {
                    continue;
                }
            }

            if (item == null || item.getType() == Material.AIR) {
                chestData.set(globalIndex, null);
            } else {
                chestData.set(globalIndex, item.clone());
            }
        }

        while (!chestData.isEmpty() && chestData.get(chestData.size() - 1) == null) {
            chestData.remove(chestData.size() - 1);
        }

        notifyOtherViewers();
    }

    private void updateSlotMapping() {
        slotToGlobalIndex.clear();

        List<Button> itemButtons = menu.buttons().stream()
                .filter(b -> "item".equals(b.type()) || "chest".equals(b.type()))
                .toList();

        if (itemButtons.isEmpty()) return;

        List<Integer> configSlots = itemButtons.stream()
                .map(Button::slot)
                .sorted()
                .distinct()
                .toList();

        int slotsPerPage = configSlots.size();

        for (int i = 0; i < slotsPerPage; i++) {
            int globalIndex = currentPage * slotsPerPage + i;
            int guiSlot = configSlots.get(i);

            if (globalIndex < clan.getLevel().chest()) {
                slotToGlobalIndex.put(guiSlot, globalIndex);
            }
        }
    }

    private void loadPageFromCloudData() {
        Inventory inv = holder().getInventory();

        List<ItemStack> chestData = clan.getChest();

        updateSlotMapping();

        for (Map.Entry<Integer, Integer> entry : slotToGlobalIndex.entrySet()) {
            int guiSlot = entry.getKey();
            int globalIndex = entry.getValue();

            ItemStack item = globalIndex < chestData.size() ? chestData.get(globalIndex) : null;

            if (item == null || item.getType() == Material.AIR) {
                inv.setItem(guiSlot, null);
            } else {
                inv.setItem(guiSlot, item.clone());
            }
        }
    }

    private void notifyOtherViewers() {
        Set<Chest> chests = ACTIVE_CHESTS.get(clan.getId());
        if (chests == null) return;

        for (Chest chest : chests) {
            if (chest != this && chest.currentPage == this.currentPage) {
                Bukkit.getScheduler().runTask(plugin, chest::loadPageFromCloudData);
            }
        }
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.equals(player)) return;

        Inventory topInv = e.getView().getTopInventory();
        if (!topInv.equals(holder().getInventory())) return;

        Inventory clickedInv = e.getClickedInventory();
        int rawSlot = e.getRawSlot();
        ClickType click = e.getClick();

        if (clickedInv != null && clickedInv.equals(topInv)) {
            if (!slotToGlobalIndex.containsKey(rawSlot)) {
                e.setCancelled(true);
                return;
            }

            ItemStack cursor = e.getCursor();
            if (cursor != null && cursor.hasItemMeta()) {
                ItemMeta meta = cursor.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) {
                    e.setCancelled(true);
                    return;
                }
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                saveToCloudData();
                notifyOtherViewers();
            }, 2L);
            return;
        }

        if ((click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT)
                && clickedInv != null && clickedInv.equals(p.getInventory())) {

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }

            if (clicked.hasItemMeta()) {
                ItemMeta meta = clicked.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer().has(NAMESPACED_KEY, PersistentDataType.STRING)) {
                    e.setCancelled(true);
                    return;
                }
            }

            e.setCancelled(true);

            int remaining = clicked.getAmount();
            List<Integer> availableSlots = new ArrayList<>(slotToGlobalIndex.keySet());
            availableSlots.sort(Integer::compare);

            for (int guiSlot : availableSlots) {
                ItemStack slotItem = topInv.getItem(guiSlot);

                if (slotItem == null || slotItem.getType() == Material.AIR) {
                    int toPlace = Math.min(remaining, clicked.getMaxStackSize());
                    ItemStack toSet = clicked.clone();
                    toSet.setAmount(toPlace);
                    topInv.setItem(guiSlot, toSet);
                    remaining -= toPlace;

                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    }
                    continue;
                }

                if (slotItem.isSimilar(clicked) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                    int space = slotItem.getMaxStackSize() - slotItem.getAmount();
                    int toAdd = Math.min(space, remaining);
                    slotItem.setAmount(slotItem.getAmount() + toAdd);
                    remaining -= toAdd;

                    if (remaining <= 0) {
                        e.setCurrentItem(null);
                        break;
                    }
                }
            }

            if (remaining > 0 && remaining < clicked.getAmount()) {
                ItemStack leftover = clicked.clone();
                leftover.setAmount(remaining);
                e.setCurrentItem(leftover);
            }

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                saveToCloudData();
                notifyOtherViewers();
            }, 2L);
        }
    }

    @Override
    public GuiType guiType() {
        return GuiType.CHEST;
    }
}