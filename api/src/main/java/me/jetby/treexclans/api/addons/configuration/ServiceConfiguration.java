package me.jetby.treexclans.api.addons.configuration;

import org.bukkit.configuration.file.FileConfiguration;

public interface ServiceConfiguration {

    FileConfiguration getConfig();
    void saveConfig();

}
