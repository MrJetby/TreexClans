package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.BetterColorize;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.gui.Button;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.api.gui.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class ChestGui extends Gui {
    private static final Map<String, Set<ChestGui>> ACTIVE_CHESTS = new HashMap<>();
    private final Map<Integer, Integer> slotToGlobalIndex = new HashMap<>();
    private int currentPage = 0;
    private BukkitTask autoSaveTask;
    private boolean isInitialized = false;
    private Button blockedSlot;

    public ChestGui(JavaPlugin plugin, @Nullable Menu menu, Player player, Clan clanImpl) {
        super(plugin, menu, player, clanImpl);
        registerButtons();
        onClose(event -> {
            if (autoSaveTask != null) {
                autoSaveTask.cancel();
            }
            saveToCloudData();
            unregisterChest();
        });

        registerToActiveChests();
        setupItemsPages();

        autoSaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::saveToCloudData, 100L, 100L);

        openPage(0);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            loadPageFromCloudData();
            isInitialized = true;
        }, 1L);

    }

    private void registerToActiveChests() {
        ACTIVE_CHESTS.computeIfAbsent(getClanImpl().getId(), k -> new HashSet<>()).add(this);
    }

    private void unregisterChest() {
        Set<ChestGui> chests = ACTIVE_CHESTS.get(getClanImpl().getId());
        if (chests != null) {
            chests.remove(this);
            if (chests.isEmpty()) {
                ACTIVE_CHESTS.remove(getClanImpl().getId());
            }
        }
    }
    private void setupItemsPages() {
        List<Button> itemButtons = getMenu().buttons().stream()
                .filter(b -> "item".equals(b.type()) || "chest".equals(b.type()))
                .toList();

        if (itemButtons.isEmpty()) return;

        List<Integer> configSlots = itemButtons.stream()
                .map(Button::slot)
                .sorted()
                .distinct()
                .toList();

        int slotsPerPage = configSlots.size();
        int maxChestSlots = getClanImpl().getLevel().chest();
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
                        ItemWrapper barrier = ItemWrapper.builder(blockedSlot.itemStack().getType())
                                .displayName(BetterColorize.component(blockedSlot.displayName()))
                                .lore(BetterColorize.component((blockedSlot.lore())))
                                .build();

                        ItemMeta meta = barrier.itemStack().getItemMeta();
                        if (meta != null) {
                            meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "blocked_slot");
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

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        String type = button.type().toLowerCase();

        if ("item".equals(type) || "chest".equals(type)) {
            return;
        }
        if (button.type().equalsIgnoreCase("blocked-slot")) {
            blockedSlot = button;
            return;
        }

        if ("next_page".equals(type)) {
            builder.defaultClickHandler((e, gui) -> {
                e.setCancelled(true);
                if (currentPage < getTotalPages() - 1) {
                    saveToCloudData();
                    currentPage++;
                    nextPage();
                    Bukkit.getScheduler().runTaskLater(getPlugin(), this::loadPageFromCloudData, 1L);
                }
            });
            return;
        }

        if ("prev_page".equals(type)) {
            builder.defaultClickHandler((e, gui) -> {
                e.setCancelled(true);
                if (currentPage > 0) {
                    saveToCloudData();
                    currentPage--;
                    previousPage();
                    Bukkit.getScheduler().runTaskLater(getPlugin(), this::loadPageFromCloudData, 1L);
                }
            });
        }
    }

    private int getTotalPages() {
        List<Button> itemButtons = getMenu().buttons().stream()
                .filter(b -> "item".equals(b.type()) || "chest".equals(b.type()))
                .toList();

        if (itemButtons.isEmpty()) return 1;

        int slotsPerPage = (int) itemButtons.stream().map(Button::slot).distinct().count();
        int maxChestSlots = getClanImpl().getLevel().chest();
        return (int) Math.ceil((double) maxChestSlots / slotsPerPage);
    }

    private void saveToCloudData() {
        if (!isInitialized) return;

        Inventory inv = holder().getInventory();

        List<ItemStack> chestData = getClanImpl().getChest();

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

        List<Button> itemButtons = getMenu().buttons().stream()
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

            if (globalIndex < getClanImpl().getLevel().chest()) {
                slotToGlobalIndex.put(guiSlot, globalIndex);
            }
        }
    }

    private void loadPageFromCloudData() {
        Inventory inv = holder().getInventory();

        List<ItemStack> chestData = getClanImpl().getChest();

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
        Set<ChestGui> chests = ACTIVE_CHESTS.get(getClanImpl().getId());
        if (chests == null) return;

        for (ChestGui chest : chests) {
            if (chest != this && chest.currentPage == this.currentPage) {
                Bukkit.getScheduler().runTask(getPlugin(), chest::loadPageFromCloudData);
            }
        }
    }

    @Override
    protected boolean cancelRegistration(Player player, @Nullable Button button) {
        if (button != null) {
            return button.type().equals("item") || button.type().equals("chest");
        }
        return false;
    }

    @Override
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!p.equals(getPlayer())) return;

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

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                saveToCloudData();
                notifyOtherViewers();
            }, 1L);
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

            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                saveToCloudData();
                notifyOtherViewers();
            }, 1L);
        }
    }

}
