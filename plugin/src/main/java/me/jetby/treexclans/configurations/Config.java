package me.jetby.treexclans.configurations;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.text.Colorize;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.level.Level;
import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import me.jetby.treexclans.api.service.clan.member.rank.RankPerms;
import me.jetby.treexclans.api.gui.requirements.SimpleRequirement;
import me.jetby.treexclans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.*;

@Getter
public class Config {
    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration;
    @Getter(AccessLevel.NONE)
    private final File file;
    @Getter(AccessLevel.NONE)
    private final FileConfiguration language;
    @Getter(AccessLevel.NONE)
    private final String lang;
    @Getter(AccessLevel.NONE)
    private final FileConfiguration level;

    private final Map<Integer, Level> levels = new LinkedHashMap<>();
    @Setter
    private Map<String, Clan> clans = new HashMap<>();

    private final Map<String, Rank> defaultRanks = new HashMap<>();
    private Rank defaultRank;
    private Rank leaderRank;

    private boolean debug;

    private String chatFormat;

    private String prefixPlaceholder_hasPrefix;
    private String prefixPlaceholder_noPrefix;
    private String prefixPlaceholder_noClan;
    private int prefixMinLength;
    private int prefixMaxLength;
    private String prefixRegex;
    private List<String> lengthIgnoredSymbols;

    private String tagPlaceholder_hasClan;
    private String tagPlaceholder_noClan;

    private String formattedTimeFormat;

    private int minTagLength;
    private int maxTagLength;
    private String regex;
    private List<String> blockedTags;
    private final List<SimpleRequirement> requirements = new ArrayList<>();

    private boolean gradualQuest;

    public Config(TreexClans plugin) {
        this.configuration = FileLoader.getFileConfiguration("config.yml");
        this.level = FileLoader.getFileConfiguration("levels.yml");
        this.file = FileLoader.getFile("config.yml");

        lang = configuration.getString("lang", "en");
        Lang lang = new Lang(plugin, this.lang);
        plugin.setLang(lang);
        this.language = lang.getConfig();
    }

    public void load() {
        requirements.clear();
        blockedTags = null;
        defaultRanks.clear();
        levels.clear();

        debug = configuration.getBoolean("debug", false);

        ConfigurationSection prefix = configuration.getConfigurationSection("prefix");
        if (prefix == null) prefix = configuration.createSection("prefix");
        prefixMinLength = prefix.getInt("min-clan-prefix-length", 3);
        prefixMaxLength = prefix.getInt("max-clan-prefix-length", 16);
        prefixRegex = prefix.getString("regex", "^[A-Za-z0-9]+$");
        lengthIgnoredSymbols = prefix.getStringList("length-ignored-symbols");

        ConfigurationSection prefixPlaceholder = prefix.getConfigurationSection("placeholder");
        if (prefixPlaceholder == null) prefixPlaceholder = prefix.createSection("placeholder");
        prefixPlaceholder_hasPrefix = Colorize.text(prefixPlaceholder.getString("has_prefix", ""));
        prefixPlaceholder_noPrefix = Colorize.text(prefixPlaceholder.getString("no_prefix", ""));
        prefixPlaceholder_noClan = Colorize.text(prefixPlaceholder.getString("no_clan", ""));


        ConfigurationSection tag = configuration.getConfigurationSection("tag-placeholder");
        if (tag == null) tag = configuration.createSection("tag-placeholder");
        tagPlaceholder_hasClan = Colorize.text(tag.getString("has_clan"));
        tagPlaceholder_noClan = Colorize.text(tag.getString("no_clan"));


        formattedTimeFormat = configuration.getString("placeholder-show-format", "%weeks% %days% %hours% %minutes% %seconds%");

        ConfigurationSection ranks = configuration.getConfigurationSection("ranks");
        if (ranks != null) {
            for (String key : ranks.getKeys(false)) {
                ConfigurationSection rank = ranks.getConfigurationSection(key);
                if (rank == null) continue;
                String name = rank.getString("display-name");
                ConfigurationSection permission = rank.getConfigurationSection("permissions");
                Set<RankPerms> perms = new HashSet<>();
                if (permission != null) {
                    for (String perm : permission.getKeys(false)) {
                        switch (perm.toLowerCase()) {
                            case "invite" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.INVITE);
                            }
                            case "kick" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.KICK);
                            }
                            case "base" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.BASE);
                            }
                            case "setbase" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETBASE);
                            }
                            case "setrank" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETRANK);
                            }
                            case "deposit" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.DEPOSIT);
                            }
                            case "withdraw" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.WITHDRAW);
                            }
                            case "pvp" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.PVP);
                            }
                            case "setslogan" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETSLOGAN);
                            }
                            case "setprefix" -> {
                                if (permission.getBoolean(perm)) perms.add(RankPerms.SETPREFIX);
                            }
                        }

                    }

                }
                defaultRanks.put(key.toLowerCase(), new Rank(key.toLowerCase(), name, perms));
            }
        }


        ConfigurationSection clanCreate = configuration.getConfigurationSection("clan-create");
        if (clanCreate != null) {
            ConfigurationSection requirements = clanCreate.getConfigurationSection("requirements");
            if (requirements != null) {
                for (String key : requirements.getKeys(false)) {
                    ConfigurationSection req = requirements.getConfigurationSection(key);
                    if (req == null) continue;
                    String type = req.getString("type");
                    String input = req.getString("input");
                    String output = req.getString("output");
                    String permission = req.getString("permission");
                    List<String> actions = req.getStringList("actions");
                    List<String> deny_actions = req.getStringList("deny_actions");
                    this.requirements.add(new SimpleRequirement(type, input, output, permission, actions, deny_actions));
                }
            }


            defaultRank = defaultRanks.get(clanCreate.getString("member-rank", "member").toLowerCase());
            leaderRank = defaultRanks.get(clanCreate.getString("leader-rank", "leader").toLowerCase());
            minTagLength = clanCreate.getInt("min-clan-tag-length", 3);
            maxTagLength = clanCreate.getInt("max-clan-tag-length", 6);
            blockedTags = clanCreate.getStringList("blocked-tags");
            regex = clanCreate.getString("regex", "^[A-Za-z0-9]+$");
        }

        for (String id : level.getKeys(false)) {
            ConfigurationSection lSection = level.getConfigurationSection(id);
            if (lSection == null) continue;
            int exp = lSection.getInt("exp", 0);
            int chest = lSection.getInt("chest", 10);
            int maxMembers = lSection.getInt("max-members", 1);
            int maxBalance = lSection.getInt("max-balance", 0);
            List<String> quests = lSection.getStringList("quests");
            List<String> levelUpActions = lSection.getStringList("level-up-actions");
            levels.put(Integer.parseInt(id), new Level(id, exp, maxMembers, maxBalance, chest, quests, levelUpActions));
        }

        gradualQuest = configuration.getBoolean("gradual-quest", false);
        chatFormat = configuration.getString("chat-format", "<#FFE259>&l[TreexClans]</#FFA751> &e&l{player} &7▶ &f{message}");
    }

}
