package me.jetby.xClans.configurations;


import lombok.AccessLevel;
import lombok.Getter;
import me.jetby.xClans.records.rank.Rank;
import me.jetby.xClans.records.rank.RankPermissions;
import me.jetby.xClans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Config {
    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration = FileLoader.getFileConfiguration("config.yml");
    private final Map<String, Rank> defaultRanks = new HashMap<>();

    private Rank defaultRank;
    private Rank leaderRank;


    public void load() {

        ConfigurationSection ranks = configuration.getConfigurationSection("ranks");
        if (ranks!=null) {
            for (String key : ranks.getKeys(false)) {
                ConfigurationSection rank = ranks.getConfigurationSection(key);
                if (rank==null) continue;
                String name = rank.getString("name");
                RankPermissions rankPermissions = new RankPermissions(
                        rank.getBoolean("invite", false),
                        rank.getBoolean("kick", false),
                        rank.getBoolean("base", false),
                        rank.getBoolean("setbase", false),
                        rank.getBoolean("setrank", false),
                        rank.getBoolean("deposit", false),
                        rank.getBoolean("withdraw", false),
                        rank.getBoolean("pvp", false)
                );
                defaultRanks.put(key, new Rank(key, name, rankPermissions));
            }
        }


        ConfigurationSection clanCreate = configuration.getConfigurationSection("clan-create");
        if (clanCreate!=null) {
            defaultRank = defaultRanks.get(clanCreate.getString("member-rank", "MEMBER"));
            leaderRank = defaultRanks.get(clanCreate.getString("leader-rank", "LEADER"));
        }
    }
}
