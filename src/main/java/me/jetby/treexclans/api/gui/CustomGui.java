package me.jetby.treexclans.api.gui;

import lombok.experimental.UtilityClass;
import me.jetby.treexclans.gui.Gui;
import me.jetby.treexclans.gui.GuiFactory;

@UtilityClass
public class CustomGui {

    public void register(String type, Gui gui) {
        GuiFactory.registerCustomGui(type.toUpperCase(), gui);
    }
    public void unregister(String type) {
        GuiFactory.unregisterCustomGui(type.toUpperCase());
    }

}
