package me.jetby.treexclans.gui.core;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.treex.text.Papi;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import me.jetby.treexclans.functions.quests.Quest;
import me.jetby.treexclans.functions.quests.QuestProgressType;
import me.jetby.treexclans.api.gui.Button;
import me.jetby.treexclans.api.gui.Gui;
import me.jetby.treexclans.api.gui.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static me.jetby.treexclans.TreexClans.NAMESPACED_KEY;

public class QuestsGui extends Gui {


    public QuestsGui(JavaPlugin plugin, Menu menu, Player player, Clan clanImpl) {
        super(plugin, menu, player, clanImpl);

        registerButtons();

        size(menu.size());
        type(menu.inventoryType());
        title(Papi.setPapi(player, menu.title()));


        setupQuestsPagination();

        openPage(0);
    }

    @Override
    protected void onRegister(Player player, Button button, GuiItemController.Builder builder) {
        if (button == null) return;
        switch (button.type().toLowerCase()) {
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
        if (button != null) {
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
        Button button = questButtons.get(0);
        int itemsPerPage = questSlots.size();
        List<Quest> questsList = new ArrayList<>();
        if ("all_quests".equals(button.type())) {
            for (Set<Quest> quests : getPlugin().getQuestsLoader().getCategories().values()) {
                questsList.addAll(quests);
            }

        } else {
            String catId = button.type().substring(9);
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
                var memberImpl = getClanImpl().getMember(getPlayer().getUniqueId());
                int progress = getPlugin().getQuestManager().getProgress(memberImpl, quest);
                consumers[i] = builder -> {

                    setCustomPlaceholder("%status%", status(memberImpl, quest));
                    setCustomPlaceholder("%quest_name%", quest.name());
                    setCustomPlaceholder("%quest_description%", quest.description());
                    setCustomPlaceholder("%quest_progress%", String.valueOf(progress));
                    setCustomPlaceholder("%quest_target%", String.valueOf(quest.target()));
                    setCustomPlaceholder("%quest_progress_type%", progressType(quest));

                    ItemStack itemStack = button.itemStack().clone();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, "menu_item");
                    itemStack.setItemMeta(itemMeta);

                    ItemWrapper wrapper = new ItemWrapper(itemStack);

                    wrapper.displayName(applyDefaultPlaceholders(button.displayName()));

                    List<String> lore = new ArrayList<>(button.lore());
                    lore.addAll(quest.rewardsDescription());
                    wrapper.lore(lore.stream()
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

    private String status(Member memberImpl, Quest quest) {
        if (getPlugin().getQuestManager().isQuestCompleted(memberImpl, quest)) {
            return getPlugin().getLang().getMessage("quest-status-completed");
        } else {
            return getPlugin().getLang().getMessage("quest-status-uncompleted");
        }
    }

    private String progressType(Quest quest) {
        if (quest.progressType().equals(QuestProgressType.INDIVIDUAL)) {
            return getPlugin().getLang().getMessage("quest-progress-type-individual");
        } else {
            return getPlugin().getLang().getMessage("quest-progress-type-global");
        }
    }

    public TreexClans getPlugin() {
        return (TreexClans) super.getPlugin();
    }

}