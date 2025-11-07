package me.jetby.treexclans.configurations;

import lombok.AccessLevel;
import lombok.Getter;
import me.jetby.treexclans.tools.FileLoader;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class Modules {

    @Getter(AccessLevel.NONE)
    private final FileConfiguration configuration = FileLoader.getFileConfiguration("modules.yml");

    private boolean glow;
    private boolean slogan;
    private boolean setprefix;

    public void load() {
        glow = configuration.getBoolean("glow", true);
        slogan = configuration.getBoolean("setslogan", true);
        setprefix = configuration.getBoolean("setprefix", true);
    }
}
