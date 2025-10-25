package me.jetby.xClans.configurations;

import lombok.Getter;
import me.jetby.xClans.TreexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;


public class Lang {

    @Getter
    private FileConfiguration config;

    public Lang(TreexClans plugin, String lang) {
        File langFolder = new File(plugin.getDataFolder(), "lang");


        File[] files = langFolder.listFiles();

        String[] defaults = {"ru.yml", "en.yml", "es.yml", "de.yml", "zh.yml", "uk.yml"};

        for (String name : defaults) {
            File target = new File(langFolder, name);

            if (!target.exists()) {
                plugin.saveResource("lang/" + name, false);
                FileConfiguration configuration = YamlConfiguration.loadConfiguration(target);
                String foundedLang = configuration.getString("lang");
                if (foundedLang == null) continue;
                if (!foundedLang.equalsIgnoreCase(lang)) continue;
                this.config = configuration;
                break;
            }

        }

        if (files == null) return;

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            String foundedLang = configuration.getString("lang");
            if (foundedLang == null) continue;
            if (!foundedLang.equalsIgnoreCase(lang)) continue;
            this.config = configuration;
            break;
        }
    }
    public String getMessage(String path) {
        return config.getString(path);
    }
    public List<String> getMessageList(String path) {
        return config.getStringList(path);
    }
}
