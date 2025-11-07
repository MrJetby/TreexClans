package me.jetby.treexclans.api.addons;

import lombok.Getter;
import me.jetby.treexclans.api.TreexClansAPI;
import me.jetby.treexclans.api.addons.annotations.ClanAddon;
import me.jetby.treexclans.api.addons.service.ServiceManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

@Getter
public abstract class JavaAddon {

    private ClanAddon info;
    private ServiceManager serviceManager;
    private File dataFolder;
    private Logger logger;

    public final void initialize(@NotNull AddonContext context) {
        this.info = getClass().getAnnotation(ClanAddon.class);
        this.serviceManager = context.serviceManager();
        var pluginClan = serviceManager.getPlugin().getServer().getServicesManager().load(TreexClansAPI.class);
        if (info == null)
            throw new IllegalStateException("Класс " + getClass().getName() + " не имеет аннотации @TreexAddonInfo");

        var parent = pluginClan.getPlugin().getLogger();
        this.logger = new Logger("AddonLogger-" + info.id(), null) {
            @Override
            public void log(java.util.logging.Level level, String msg) {
                String prefix = "[" + info.id() + "] ";
                parent.log(level, prefix + msg);
            }

            @Override
            public void log(java.util.logging.Level level, String msg, Throwable thrown) {
                String prefix = "[" + info.id() + "] ";
                parent.log(level, prefix + msg, thrown);
            }
        };
        this.dataFolder = serviceManager.getDataFolder();
    }

    public abstract void onEnable();
    public abstract void onDisable();

    public FileConfiguration getConfig() {
        return serviceManager.getServiceConfiguration().getConfig();
    }
    public void saveConfig() {
        serviceManager.getServiceConfiguration().saveConfig();
    }
    public File getFile(String fileName) {
        return serviceManager.getServiceConfiguration().getFile(fileName);
    }
    public FileConfiguration getConfiguration(String fileName) {
        return serviceManager.getServiceConfiguration().getFileConfiguration(fileName);
    }

}