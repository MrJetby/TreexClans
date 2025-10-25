package me.jetby.xClans;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.jetby.xClans.commands.clan.ClanCommand;
import me.jetby.xClans.commands.xclan.XClanCommand;
import me.jetby.xClans.configurations.ClansLoader;
import me.jetby.xClans.configurations.Config;
import me.jetby.xClans.functions.ClanGlow;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;


public final class TreexClans extends JavaPlugin {

    private static TreexClans INSTANCE;

    public static TreexClans getInstance() {
        return INSTANCE;
    }

    @Getter
    private Config cfg;

    @Getter
    private ClanGlow clanGlow;

    @Getter
    private ClansLoader clansLoader;
    @Getter
    private ClanManager clanManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        cfg = new Config();
        cfg.load();

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


        clansLoader = new ClansLoader();
        clansLoader.load();

        clanManager = new ClanManager(this);

        clanGlow = new ClanGlow();


    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}
