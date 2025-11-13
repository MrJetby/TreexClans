package me.jetby.treexclans;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.api.gui.GuiFactory;
import org.bukkit.NamespacedKey;

/**
 * A global static registry (factory) of commonly used plugin instances and constants.
 * <p>
 * This class can hold any static objects — NamespacedKeys, managers,
 * configurations, singletons, or helper utilities.
 * <p>
 * Use it as a central place to store shared references initialized during plugin startup.
 */
@UtilityClass
public class InstanceFactory {

    /** Example: global NamespacedKey factory base */
    public NamespacedKey ITEM_KEY;
    public GuiFactory GUI_FACTORY;

}
