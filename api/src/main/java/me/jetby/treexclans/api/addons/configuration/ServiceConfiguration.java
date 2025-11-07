package me.jetby.treexclans.api.addons.configuration;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public interface ServiceConfiguration {

    FileConfiguration getConfig();
    void saveConfig();
    FileConfiguration getFileConfiguration(String child);
    File getFile(String child);
}
