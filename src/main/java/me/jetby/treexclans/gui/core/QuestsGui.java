package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Colorize;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.functions.quests.Quest;
import me.jetby.treexclans.gui.*;
import me.jetby.treexclans.gui.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class QuestsGui extends Gui {


    public QuestsGui(TreexClans plugin, Menu menu, Player player, Clan clan) {
        super(plugin, menu, player, clan);

        registerButtons();

        size(menu.size());
        type(menu.inventoryType());
        title(Papi.setPapi(player, menu.title()));


        setupQuestsPagination();

        openPage(0);
    }

    @Override
    public boolean cancelRegistration(Player player, @Nullable Button button) {
        if (button!=null) {
            return button.type().equals("all_quests") || button.type().startsWith("category-");
        }
        return false;
    }

    private void setupQuestsPagination() {
        List<Button> questButtons = getMenu().buttons().stream()
                .filter(b -> b.type().equals("all_quests") || b.type().startsWith("category-"))
                .toList();
        if (questButtons.isEmpty()) return;
        List<Integer> questSlots = questButtons.stream()
                .map(Button::slot)
                .toList();
        Button questButton = questButtons.get(0);
        int itemsPerPage = questSlots.size();
        List<Quest> questsList = new ArrayList<>();
        if ("all_quests".equals(questButton.type())) {
            for (Set<Quest> quests : getPlugin().getQuestsLoader().getCategories().values()) {
                questsList.addAll(quests);
            }

        } else {
            String catId = questButton.type().substring(9);
            Set<Quest> cat = getPlugin().getQuestsLoader().getCategories().get(catId);
            if (cat == null) return;
            questsList = cat.stream().toList();
        }
        int totalPages = (int) Math.ceil((double) questsList.size() / itemsPerPage);
        for (int page = 0; page < totalPages; page++) {
            int start = page * itemsPerPage;
            int end = Math.min(start + itemsPerPage, questsList.size());
            Consumer<GuiItemController.Builder>[] consumers = new Consumer[itemsPerPage];
            for (int i = 0; i < itemsPerPage; i++) {
                int questIndex = start + i;
                int slot = questSlots.get(i);
                if (questIndex >= end) {
                    consumers[i] = builder -> {
                        builder.slots(slot);
                        builder.defaultItem(ItemWrapper.builder(Material.AIR).build());
                        builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                    };
                    continue;
                }
                Quest quest = questsList.get(questIndex);
                consumers[i] = builder -> {
                    ItemStack itemStack = questButton.itemStack().clone();
                    ItemWrapper wrapper = new ItemWrapper(itemStack);
                    String rawDisplayName = questButton.displayName();
                    String processedDisplayName = replaceQuestPlaceholders(rawDisplayName, quest, getClan().getMember(getPlayer().getUniqueId()));
                    processedDisplayName = Papi.setPapi(getPlayer(), processedDisplayName);
                    wrapper.displayName(Colorize.text(processedDisplayName));
                    List<String> rawLore = questButton.lore();
                    List<String> processedLore = rawLore.stream()
                            .map(l -> replaceQuestPlaceholders(l, quest, getClan().getMember(getPlayer().getUniqueId())))
                            .map(l -> Papi.setPapi(getPlayer(), l))
                            .map(Colorize::text)
                            .collect(Collectors.toList());
                    wrapper.lore(processedLore);
                    wrapper.customModelData(questButton.customModelData());
                    wrapper.enchanted(questButton.enchanted());
                    wrapper.update();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                    itemStack.setItemMeta(itemMeta);
                    builder.defaultItem(wrapper);
                    builder.slots(slot);
                    builder.defaultClickHandler((event, ctrl) -> event.setCancelled(true));
                };
            }
            addPage(consumers);
        }
    }

    private String replaceQuestPlaceholders(String text, Quest quest, Member member) {
        int progress = getPlugin().getQuestManager().getProgress(member, quest);
        text = text.replace("%status%", status(member, quest));
        text = text.replace("%quest_name%", quest.name());
        text = text.replace("%quest_description%", quest.description());
        text = text.replace("%quest_progress%", String.valueOf(progress));
        text = text.replace("%quest_target%", String.valueOf(quest.target()));
        return text;
    }

    private String status(Member member, Quest quest) {
        if (getPlugin().getQuestManager().isQuestCompleted(member, quest)) {
            return getPlugin().getLang().getMessage("quest-status-completed");
        } else {
            return getPlugin().getLang().getMessage("quest-status-uncompleted");
        }
    }

}