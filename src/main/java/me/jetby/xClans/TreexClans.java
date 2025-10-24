package me.jetby.xClans;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.jetby.xClans.commands.clan.ClanCommand;
import me.jetby.xClans.commands.xclan.XClanCommand;
import me.jetby.xClans.configurations.ClansLoader;
import me.jetby.xClans.functions.ClanGlow;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class TreexClans extends JavaPlugin {

    private static TreexClans INSTANCE;

    public static TreexClans getInstance() {
        return INSTANCE;
    }

    @Getter
    private final ClansLoader clansLoader = new ClansLoader();
    @Getter
    private ClanManager clanManager = new ClanManager(this);

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        clansLoader.load();
        // register /xclan command
        PluginCommand xClanCommand = this.getCommand("xclans");
        if (xClanCommand != null) {
            XClanCommand cmd = new XClanCommand();
            xClanCommand.setExecutor(cmd);
            xClanCommand.setTabCompleter(cmd);
        }
        // register /clan command
        PluginCommand clanCommand = this.getCommand("clan");
        if (clanCommand != null) {
            ClanCommand cmd = new ClanCommand();
            clanCommand.setExecutor(cmd);
            clanCommand.setTabCompleter(cmd);
        }

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        PacketEvents.getAPI().getEventManager().registerListener(new ClanGlow());


    }

    @Override
    public void onDisable() {

    }
}
