package me.jetby.treexclans.api.addons;

import me.jetby.treexclans.api.addons.service.ServiceManager;

import java.util.logging.Logger;

/**
 * Represents the initialization context of a TreexClans addon.
 * <p>
 * This record is provided only during the {@link JavaAddon#initialize(AddonContext)}
 * call and contains essential environment data for the addon.
 * </p>
 *
 * <p>
 * It gives access to the shared {@link ServiceManager}, allowing
 * addons to interact with core systems such as clans, economy,
 * leaderboards, and GUI services. The {@link Logger} provides
 * a dedicated logging channel for the addon.
 * </p>
 */
public record AddonContext(
        ServiceManager serviceManager,
        Logger logger
) {}
