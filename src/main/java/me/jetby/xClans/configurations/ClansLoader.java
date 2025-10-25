package me.jetby.xClans.configurations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.jetby.xClans.records.Clan;
import me.jetby.xClans.records.Level;
import me.jetby.xClans.records.rank.Rank;
import me.jetby.xClans.records.rank.RankPermissions;
import me.jetby.xClans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class ClansLoader {

    private final FileConfiguration configuration = FileLoader.getFileConfiguration("storage.yml");

    private final Set<Level> levels = new HashSet<>();
    private final Map<String, Clan> clans = new HashMap<>();


    public void load() {

    }
}
