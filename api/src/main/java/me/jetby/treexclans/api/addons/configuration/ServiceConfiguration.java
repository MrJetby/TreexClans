package me.jetby.treexclans.api.addons.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;

/**
 * Represents a configuration service used by TreexClans addons.
 * <p>
 * Provides a unified way to access, read, and save configuration
 * files associated with an addon or its submodules.
 * </p>
 *
 * <p>
 * Implementations are responsible for managing the lifecycle of
 * configuration files — including loading, saving, and providing
 * access to child configurations when needed.
 * </p>
 */
public interface ServiceConfiguration {

    /**
     * Gets the main configuration file of the addon.
     * <p>
     * This is typically the root {@code config.yml}
     * located inside the addon's data folder.
     * </p>
     *
     * @return The main configuration object.
     */
    FileConfiguration getConfig();

    /**
     * Saves the current configuration to disk.
     * <p>
     * Should be called after making any modifications
     * to ensure changes are persisted safely.
     * </p>
     */
    void saveConfig();

    /**
     * Retrieves a child configuration file.
     * <p>
     * Used for structured configurations where multiple
     * YAML files exist within the addon’s data folder.
     * </p>
     *
     * @param child The name or relative path of the file (e.g. "settings.yml").
     * @return The corresponding {@link FileConfiguration}.
     */
    FileConfiguration getFileConfiguration(String child);

    /**
     * Gets a reference to a file inside the addon's folder.
     * <p>
     * This method can be used to access or create additional
     * custom configuration or data files dynamically.
     * </p>
     *
     * @param child The name or relative path of the file.
     * @return A {@link File} object representing the requested file.
     */
    File getFile(String child);
}
