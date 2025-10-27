package me.jetby.xClans.configurations;


import lombok.AccessLevel;
import lombok.Getter;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.gui.requirements.SimpleRequirement;
import me.jetby.xClans.clan.Clan;
import me.jetby.xClans.clan.Level;
import me.jetby.xClans.clan.rank.Rank;
import me.jetby.xClans.clan.rank.RankPermissions;
import me.jetby.xClans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

@Getter
public class Config {
    @Getter(AccessLevel.NONE)
    final FileConfiguration configuration;
    final File file;
    private final FileConfiguration language;
    final String lang;

    private final Set<Level> levels = new HashSet<>();
    private final Map<String, Clan> clans = new HashMap<>();

    private final Map<String, Rank> defaultRanks = new HashMap<>();
    private Rank defaultRank;
    private Rank leaderRank;

    private String chatFormat;

    private String formattedTimeFormat;

    private int minTagLength;
    private int maxTagLength;
    private List<String> blockedTags;
    private final List<SimpleRequirement> requirements = new ArrayList<>();

    public Config(TreexClans plugin) {
        this.configuration = FileLoader.getFileConfiguration("config.yml");
        this.file = FileLoader.getFile("config.yml");

        lang = configuration.getString("lang", "en");
        Lang lang = new Lang(plugin, this.lang);
        plugin.setLang(lang);
        this.language = lang.getConfig();
    }

    public void load() {

        formattedTimeFormat = configuration.getString("formattedTime.show-format", "%weeks% %days% %hours% %minutes% %seconds%");

        ConfigurationSection ranks = configuration.getConfigurationSection("ranks");
        if (ranks != null) {
            for (String key : ranks.getKeys(false)) {
                ConfigurationSection rank = ranks.getConfigurationSection(key);
                if (rank == null) continue;
                String name = rank.getString("display-name");
                ConfigurationSection permission = rank.getConfigurationSection("permissions");
                RankPermissions rankPermissions = null;
                if (permission != null) {
                    rankPermissions = new RankPermissions(
                            permission.getBoolean("invite", false),
                            permission.getBoolean("kick", false),
                            permission.getBoolean("base", false),
                            permission.getBoolean("setbase", false),
                            permission.getBoolean("setrank", false),
                            permission.getBoolean("deposit", false),
                            permission.getBoolean("withdraw", false),
                            permission.getBoolean("pvp", false)
                    );
                }
                defaultRanks.put(key, new Rank(key, name, rankPermissions));
            }
        }


        ConfigurationSection clanCreate = configuration.getConfigurationSection("clan-create");
        if (clanCreate != null) {
            ConfigurationSection requirements = configuration.getConfigurationSection("requirements");
            if (requirements!=null) {
                for (String key : requirements.getKeys(false)) {
                    ConfigurationSection req = requirements.getConfigurationSection(key);
                    if (req==null) continue;
                    String type = req.getString("type");
                    String input = req.getString("input");
                    String output = req.getString("output");
                    String permission = req.getString("permission");
                    List<String> actions = req.getStringList("actions");
                    List<String> deny_actions = req.getStringList("deny_actions");
                    this.requirements.add(new SimpleRequirement(type, input, output, permission, actions, deny_actions));
                }
            }



            defaultRank = defaultRanks.get(clanCreate.getString("member-rank", "MEMBER"));
            leaderRank = defaultRanks.get(clanCreate.getString("leader-rank", "LEADER"));
            minTagLength = clanCreate.getInt("min-clan-tag-length", 3);
            maxTagLength = clanCreate.getInt("max-clan-tag-length", 6);
            blockedTags = clanCreate.getStringList("blocked-tags");
        }

        chatFormat = configuration.getString("chat-format", "<#FFE259>&l[TreexClans]</#FFA751> &e&l{player} &7▶ &f{message}");
    }
}
