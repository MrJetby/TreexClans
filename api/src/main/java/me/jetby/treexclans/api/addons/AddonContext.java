package me.jetby.treexclans.api.addons;

import me.jetby.treexclans.api.addons.service.ServiceManager;

import java.util.logging.Logger;

/**
 * Контекст инициализации аддона.
 * <p>Передаётся только при вызове {@link JavaAddon#initialize(AddonContext)}.</p>
 */
public record AddonContext(
        ServiceManager serviceManager,
        Logger logger
) {}
