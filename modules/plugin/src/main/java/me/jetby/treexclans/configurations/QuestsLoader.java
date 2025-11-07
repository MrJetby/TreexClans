package me.jetby.treexclans.configurations;

import lombok.Getter;
import me.jetby.treexclans.functions.quests.Quest;
import me.jetby.treexclans.functions.quests.QuestProgressType;
import me.jetby.treexclans.functions.quests.QuestType;
import me.jetby.treexclans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class QuestsLoader {


    private final FileConfiguration configuration = FileLoader.getFileConfiguration("quests.yml");


    @Getter
    private final Map<String, Set<Quest>> categories = new LinkedHashMap<>();
    @Getter
    private final Map<String, Quest> quests = new HashMap<>();

    public void load() {
        quests.clear();
        categories.clear();

        for (String key : configuration.getKeys(false)) {
            if (key.equals("category")) continue;
            ConfigurationSection quest = configuration.getConfigurationSection(key);
            if (quest != null) {
                String type = quest.getString("type");
                if (type == null) {
                    LOGGER.warn("Quest " + key + " has wrong type.");
                    continue;
                }
                String[] questArgs = type.split(";");
                QuestType questType = QuestType.valueOf(questArgs[0].toUpperCase());
                String questProperty = null;
                if (questArgs.length == 2) {
                    questProperty = questArgs[1].toUpperCase();
                }

                String name = quest.getString("name");
                String description = quest.getString("description");
                List<String> rewardsDescription = quest.getStringList("rewards-description");
                QuestProgressType progressType = QuestProgressType.valueOf(quest.getString("progress", "GLOBAL").toUpperCase());
                int target = quest.getInt("target");
                List<String> globalRewards = quest.getStringList("global-rewards");
                List<String> rewards = quest.getStringList("rewards");
                List<String> disabledWorlds = quest.getStringList("disabled-worlds");

                quests.put(key, new Quest(key, name, description, rewardsDescription, progressType, questType, questProperty, target, globalRewards, rewards, disabledWorlds));

            }
        }

        ConfigurationSection categories = configuration.getConfigurationSection("category");
        if (categories != null) {
            for (String key : categories.getKeys(false)) {
                List<String> questIds = categories.getStringList(key);
                if (questIds.isEmpty()) continue;
                Set<Quest> questList = new LinkedHashSet<>();
                for (String questId : questIds) {
                    Quest getData = quests.get(questId);
                    if (getData != null) {
                        questList.add(getData);
                    } else {
                        LOGGER.warn("Quest '" + questId + "' in category '" + key + "' not found!");
                    }
                }
                if (!questList.isEmpty()) {
                    this.categories.put(key, questList);
                }
            }
        }

    }

}
