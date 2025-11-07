package me.jetby.treexclans.tools;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.TreexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

@UtilityClass
public class FileLoader {

    public FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(TreexClans.getInstance().getDataFolder().getAbsolutePath(), fileName);
        if (!file.exists()) {
            TreexClans.getInstance().saveResource(fileName, false);

        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String fileName) {
        File file = new File(TreexClans.getInstance().getDataFolder().getAbsoluteFile(), fileName);
        if (!file.exists()) {
            TreexClans.getInstance().saveResource(fileName, false);
        }
        return file;
    }

}
