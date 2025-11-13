package me.jetby.treexclans.configurations;

import me.jetby.treexclans.tools.FileLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigUpdater {

    private final File file = FileLoader.getFile("config.yml");
    private final FileConfiguration configuration;

    public ConfigUpdater(int version) {
        this.configuration = YamlConfiguration.loadConfiguration(file);
        switch (version) {
            case 1 -> loadV1();
            case 2 -> loadV2();
            case 3 -> loadV3();
        }
    }
    public void loadV3() {
        try {
            configuration.set("config-version", 4);

            configuration.set("clan-storage.filter.type", "BLACKLIST");
            configuration.set("clan-storage.filter.materials", List.of("BEDROCK", "BARRIER"));

            configuration.save(file);
        } catch (IOException ignored) {}
    }
    public void loadV2() {
        try {
            configuration.set("config-version", 3);
            configuration.set("prefix.length-ignored-symbols", List.of("&", "#"));
            configuration.save(file);
        } catch (IOException ignored) {}
    }
    public void loadV1() {
        try {

            ConfigurationSection ranks = configuration.getConfigurationSection("ranks");
            for (String key : ranks.getKeys(false)) {
                configuration.set("ranks."+key+".permissions.setslogan", false);
                configuration.set("ranks."+key+".permissions.setprefix", false);
            }
            configuration.set("config-version", 2);

            configuration.set("prefix.min-clan-prefix-length", 3);
            configuration.set("prefix.max-clan-prefix-length", 16);
            configuration.set("prefix.regex", "^[A-Za-z0-9]+$");

            configuration.set("prefix.placeholder.has_prefix", "&7[&6{prefix}&7]");
            configuration.set("prefix.placeholder.no_prefix", "&8[&e{tag}&8]");
            configuration.set("prefix.placeholder.no_clan", "");

            configuration.set("tag-placeholder.has_clan", "&8[&e{tag}&8]");
            configuration.set("tag-placeholder.no_clan", "");

            configuration.save(file);
        } catch (IOException ignored) {}
    }
}
