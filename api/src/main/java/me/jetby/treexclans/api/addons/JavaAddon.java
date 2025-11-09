package me.jetby.treexclans.api.addons;

import lombok.Getter;
import me.jetby.treexclans.api.TreexClansAPI;
import me.jetby.treexclans.api.addons.annotations.ClanAddon;
import me.jetby.treexclans.api.addons.service.ServiceManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

/**
 * Base class for all TreexClans addons.
 * <p>
 * Every addon must extend this class and be annotated with
 * {@link ClanAddon}. It provides access to essential services
 * such as configuration, logging, and the main plugin API.
 * </p>
 *
 * <p>
 * The lifecycle methods {@link #onEnable()} and {@link #onDisable()}
 * are automatically called when the addon is enabled or disabled.
 * </p>
 */
@Getter
public abstract class JavaAddon {

    private ClanAddon info;
    private ServiceManager serviceManager;
    private File dataFolder;
    private Logger logger;
    protected boolean enabled = false;

    /**
     * Initializes the addon context.
     * <p>
     * This method is automatically called by the AddonManager and should
     * not be invoked manually. It binds the addon to its runtime context,
     * sets up the logger, and initializes its data folder.
     * </p>
     *
     * @param context The initialization context containing
     *                the {@link ServiceManager} and {@link Logger}.
     */
    public final void initialize(@NotNull AddonContext context) {
        this.info = getClass().getAnnotation(ClanAddon.class);
        this.serviceManager = context.serviceManager();

        var pluginClan = serviceManager.getPlugin().getServer()
                .getServicesManager()
                .load(TreexClansAPI.class);

        if (info == null) {
            throw new IllegalStateException("Class " + getClass().getName() + " does not have the @ClanAddon annotation");
        }

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

    private void enable() {
        if (!enabled) {
            this.enabled = true;
            this.onEnable();
        }
    }

    private void disable() {
        if (enabled) {
            this.enabled = false;
            this.onDisable();
            this.getServiceManager().getEventRegistrar().unregister(this);
        }
    }

    /**
     * Called when the addon is enabled.
     * <p>
     * Override this method to initialize your addon’s
     * logic, register listeners, or load resources.
     * </p>
     */
    protected abstract void onEnable();

    /**
     * Called when the addon is disabled.
     * <p>
     * Override this method to safely unload your addon,
     * save data, and clean up any registered handlers.
     * </p>
     */
    protected abstract void onDisable();

    /**
     * Gets the main configuration file of this addon.
     *
     * @return The root configuration ({@code config.yml}) instance.
     */
    public FileConfiguration getConfig() {
        return serviceManager.getServiceConfiguration().getConfig();
    }

    /**
     * Saves the current configuration to disk.
     * <p>
     * Use this after modifying configuration values
     * to ensure that changes are persisted.
     * </p>
     */
    public void saveConfig() {
        serviceManager.getServiceConfiguration().saveConfig();
    }

    /**
     * Gets a reference to a file within the addon's folder.
     *
     * @param fileName The name or relative path of the file.
     * @return The corresponding {@link File} instance.
     */
    public File getFile(String fileName) {
        return serviceManager.getServiceConfiguration().getFile(fileName);
    }

    /**
     * Loads or retrieves a secondary configuration file.
     *
     * @param fileName The name or relative path of the YAML file.
     * @return The {@link FileConfiguration} instance for that file.
     */
    public FileConfiguration getConfiguration(String fileName) {
        return serviceManager.getServiceConfiguration().getFileConfiguration(fileName);
    }
}
