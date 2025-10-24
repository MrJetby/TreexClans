package me.jetby.xClans.storage;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.configurations.ClansLoader;
import me.jetby.xClans.records.Clan;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class YAML implements Storage{

    private final TreexClans plugin;
    private final FileConfiguration configuration;
    private final ClansLoader clansLoader;

    public YAML(TreexClans plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();
        this.clansLoader = plugin.getClansLoader();
    }

    @Override
    public void load() {


    }

    @Override
    public void save() {

        for (String clanId : clansLoader.getClans().keySet()) {
            Clan clan = clansLoader.getClans().get(clanId);
            ConfigurationSection section = configuration.getConfigurationSection(clanId);
            if (section==null) configuration.createSection(clanId);
            section.set("balance", clan.getBalance());
            section.set("level", clan.getLevel());
            section.set("members", clan.getLevel());
        }
    }
}
