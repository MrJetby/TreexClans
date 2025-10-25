package me.jetby.xClans;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import me.jetby.xClans.commands.clan.ClanCommand;
import me.jetby.xClans.commands.xclan.XClanCommand;
import me.jetby.xClans.configurations.ClansLoader;
import me.jetby.xClans.configurations.Config;
import me.jetby.xClans.configurations.Lang;
import me.jetby.xClans.functions.ClanGlow;
import me.jetby.xClans.listeners.UserLoader;
import me.jetby.xClans.storage.Storage;
import me.jetby.xClans.storage.YAML;
import me.jetby.xClans.tools.FormatTime;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TreexClans extends JavaPlugin {

    private static TreexClans INSTANCE;

    public static TreexClans getInstance() {
        return INSTANCE;
    }

    private Config cfg;
    @Setter
    public Lang lang;
    private FormatTime formatTime;

    private ClanGlow clanGlow;

    private ClansLoader clansLoader;
    private ClanManager clanManager;
    private Storage storage;

    private boolean packetInit = true;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        cfg = new Config(this);
        cfg.load();

        formatTime = new FormatTime(this);

        PluginCommand xClanCommand = this.getCommand("xclan");
        if (xClanCommand != null) {
            XClanCommand cmd = new XClanCommand();
            xClanCommand.setExecutor(cmd);
            xClanCommand.setTabCompleter(cmd);
        }
        PluginCommand clanCommand = this.getCommand("clan");
        if (clanCommand != null) {
            ClanCommand cmd = new ClanCommand();
            clanCommand.setExecutor(cmd);
            clanCommand.setTabCompleter(cmd);
        }

        try {
            PacketEvents.getAPI().init();
        } catch (Exception e) {
            packetInit = false;
        }

        clansLoader = new ClansLoader();
        clansLoader.load();

        clanManager = new ClanManager(this);

        clanGlow = new ClanGlow();

        storage = new YAML(this);
        storage.load();

        getServer().getPluginManager().registerEvents(new UserLoader(this), this);

    }

    @Override
    public void onDisable() {
        storage.save();
        PacketEvents.getAPI().terminate();
    }
}
