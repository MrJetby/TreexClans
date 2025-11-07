package me.jetby.treexclans.gui;

import lombok.Getter;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.gui.Button;
import me.jetby.treexclans.api.gui.ButtonCommand;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.api.gui.Menu;
import me.jetby.treexclans.functions.glow.Equipment;
import me.jetby.treexclans.api.gui.requirements.ClickRequirement;
import me.jetby.treexclans.api.gui.requirements.ViewRequirement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.io.File;
import java.util.*;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class GuiLoader {

    @Getter
    private final Map<String, Menu> menus = new HashMap<>();
    @Getter
    private final Map<UUID, Gui> guis = new HashMap<>();

    private final TreexClans plugin;
    private final File file;
    private final boolean debug;

    public GuiLoader(TreexClans plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        this.debug = plugin.getCfg().isDebug();
    }

    public void load() {

        menus.clear();

        File folder = new File(file, "Menu");


        if (!folder.exists() && folder.mkdirs()) {
            String[] defaults = {
                    "main.yml", "quests.yml", "members.yml", "choose-player-color.yml",
                    "glow-color.yml", "rank-perms.yml", "ranks.yml", "storage.yml", "top-clans.yml", "shop.yml"
            };

            for (String name : defaults) {
                File target = new File(folder, name);

                if (!target.exists()) {
                    plugin.saveResource("Menu/" + name, false);
                }

                FileConfiguration config = YamlConfiguration.loadConfiguration(target);
                loadMenu(config.getString("id"), target);
            }
            return;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().endsWith(".yml")) continue;
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                loadMenu(config.getString("id", file.getName().replace(".yml", "")), file);
                if (debug) LOGGER.info(file.getName() + " (id: " + config.getString("id") + ")" + " loaded");
            }
        }
        LOGGER.success(menus.size() + " menus has been founded");
    }

    private void loadMenu(String menuId, File file) {

        if (menus.containsKey(menuId)) {
            LOGGER.error("A duplicate of " + menuId + " was found");
            return;
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String title = config.getString("title");
            String type = config.getString("listen", "default").toUpperCase();
            int size = config.getInt("size", 27);
            String permission = config.getString("open_permission");
            InventoryType inventoryType = InventoryType.valueOf(config.getString("inventory", "CHEST"));
            List<String> openCommands = config.getStringList("open_commands");
            List<String> openArgs = config.getStringList("open_args");
            List<Button> buttons = loadButtons(config);

            menus.put(menuId, new Menu(menuId, title, type, size, inventoryType, permission, openCommands, openArgs, buttons));
        } catch (Exception e) {
            LOGGER.error("Error trying to load menu: " + e.getMessage());
        }
    }

    private List<Button> loadButtons(FileConfiguration config) {
        List<Button> buttons = new ArrayList<>();
        ConfigurationSection itemsSection = config.getConfigurationSection("Items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    String displayName = itemSection.getString("display_name");
                    List<String> lore = itemSection.getStringList("lore");
                    List<Integer> slots = parseSlots(itemSection.get("slot"));
                    int amount = itemSection.getInt("amount", 1);
                    int customModelData = itemSection.getInt("custom-model-data", 0);
                    boolean enchanted = itemSection.getBoolean("enchanted", false);
                    boolean freeSlot = itemSection.getBoolean("free-slot", false);
                    boolean hideAttributes = itemSection.getBoolean("hide_attributes", false);
                    int priority = itemSection.getInt("priority", 0);
                    String type = itemSection.getString("type", "default");
                    String defaultMaterial;
                    if (freeSlot) {
                        defaultMaterial = "AIR";
                    } else {
                        defaultMaterial = "STONE";
                    }

                    String rgb = itemSection.getString("color", "WHITE");
                    String openGui = itemSection.getString("open-gui");
                    String material = Papi.setPapi(null, itemSection.getString("material", defaultMaterial));
                    ItemStack itemStack;
                    if (material.startsWith("basehead-")) {
                        try {
                            itemStack = SkullCreator.itemFromBase64(material.replace("basehead-", ""));
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("Error creating custom skull: " + e.getMessage());
                            itemStack = new ItemStack(SkullCreator.createSkull());
                        }
                    } else {
                        itemStack = new ItemStack(Material.valueOf(material));
                    }

                    itemStack.setAmount(amount);
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        if (hideAttributes) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        meta.addItemFlags(ItemFlag.HIDE_DYE);
                        meta.setDisplayName(displayName);
                        meta.setLore(lore);
                        meta.setCustomModelData(customModelData);
                        if (meta instanceof LeatherArmorMeta lam) {
                            lam.setColor(Equipment.getColorByName(rgb));
                        }
                        itemStack.setItemMeta(meta);
                    }

                    for (Integer slot : slots) {

                        buttons.add(new Button(key, displayName, rgb,
                                openGui, lore, slot, amount, customModelData,
                                enchanted, freeSlot, hideAttributes, itemStack,
                                requirements(itemSection, slot),
                                loadCommands(itemSection),
                                priority, type));
                    }
                }
            }
        }
        return buttons;
    }

    private List<ButtonCommand> loadCommands(ConfigurationSection itemSection) {
        List<ButtonCommand> buttonCommands = new ArrayList<>();

        if (itemSection.contains("left_click_commands")) {

            buttonCommands.add(new ButtonCommand(false, ClickType.LEFT, itemSection.getStringList("left_click_commands"),
                    requirements(itemSection, "left_click_requirements", ClickType.LEFT, false)));

        }
        if (itemSection.contains("right_click_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.RIGHT, itemSection.getStringList("right_click_commands"),
                    requirements(itemSection, "right_click_requirements", ClickType.RIGHT, false)));

        }
        if (itemSection.contains("shift_left_click_commands")) {

            buttonCommands.add(new ButtonCommand(false, ClickType.SHIFT_LEFT, itemSection.getStringList("shift_left_click_commands"),
                    requirements(itemSection, "shift_left_click_requirements", ClickType.SHIFT_LEFT, false)));

        }
        if (itemSection.contains("shift_right_click_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.SHIFT_RIGHT, itemSection.getStringList("shift_right_click_commands"),
                    requirements(itemSection, "shift_right_click_requirements", ClickType.SHIFT_RIGHT, false)));

        }
        if (itemSection.contains("click_commands")) {
            buttonCommands.add(new ButtonCommand(true, ClickType.UNKNOWN, itemSection.getStringList("click_commands"),
                    requirements(itemSection, "click_requirements", ClickType.UNKNOWN, true)));

        }
        if (itemSection.contains("drop_commands")) {
            buttonCommands.add(new ButtonCommand(false, ClickType.DROP, itemSection.getStringList("drop_commands"),
                    requirements(itemSection, "drop_requirements", ClickType.DROP, false)));
        }

        return buttonCommands;
    }

    private List<ViewRequirement> requirements(ConfigurationSection itemSection, int slot) {
        List<ViewRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection("view_requirements");
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;

            String input = section.getString("input");

            String permission = section.getString("permission");

            requirements.add(new ViewRequirement(
                    section.getString("type"),
                    input,
                    section.getString("output"),
                    section.getBoolean("free-slot", false),
                    permission));
        }
        return requirements;
    }

    private List<ClickRequirement> requirements(ConfigurationSection itemSection, String name, ClickType clickType, boolean anyClick) {
        List<ClickRequirement> requirements = new ArrayList<>();
        ConfigurationSection requirementsSection = itemSection.getConfigurationSection(name);
        if (requirementsSection == null) {
            return requirements;
        }
        for (String key : requirementsSection.getKeys(false)) {
            ConfigurationSection section = requirementsSection.getConfigurationSection(key);
            if (section == null) continue;
            requirements.add(new ClickRequirement(anyClick, clickType,
                    section.getString("type"),
                    section.getString("input"),
                    section.getString("output"),
                    section.getString("permission"),
                    section.getStringList("deny_commands")));
        }
        return requirements;
    }

    private List<Integer> parseSlots(Object slotObject) {
        List<Integer> slots = new ArrayList<>();

        if (slotObject instanceof Integer) {
            slots.add((Integer) slotObject);
        } else if (slotObject instanceof String) {
            String slotString = ((String) slotObject).trim();
            slots.addAll(parseSlotString(slotString));
        } else if (slotObject instanceof List<?>) {
            for (Object obj : (List<?>) slotObject) {
                if (obj instanceof Integer) {
                    slots.add((Integer) obj);
                } else if (obj instanceof String) {
                    slots.addAll(parseSlotString((String) obj));
                }
            }
        } else {
            Bukkit.getLogger().warning("Unknown slot format: " + slotObject);
        }

        return slots;
    }

    private List<Integer> parseSlotString(String slotString) {
        List<Integer> slots = new ArrayList<>();
        if (slotString.contains("-")) {
            try {
                String[] range = slotString.split("-");
                int start = Integer.parseInt(range[0].trim());
                int end = Integer.parseInt(range[1].trim());
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Error parsing slot range: " + slotString);
            }
        } else {
            try {
                slots.add(Integer.parseInt(slotString));
            } catch (NumberFormatException e) {
                Bukkit.getLogger().warning("Error parsing single slot: " + slotString);
            }
        }
        return slots;
    }

}

