package me.jetby.xClans;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.tools.LogInitialize;
import me.jetby.treex.tools.log.Logger;
import me.jetby.xClans.commands.clan.ClanCommand;
import me.jetby.xClans.commands.xclan.XClanCommand;
import me.jetby.xClans.configurations.Config;
import me.jetby.xClans.configurations.Lang;
import me.jetby.xClans.functions.ClanGlow;
import me.jetby.xClans.listeners.UserLoader;
import me.jetby.xClans.storage.Storage;
import me.jetby.xClans.storage.YAML;
import me.jetby.xClans.tools.FormatTime;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TreexClans extends JavaPlugin {

    private static TreexClans INSTANCE;

    public static TreexClans getInstance() {
        return INSTANCE;
    }
    private Economy economy;

    private Config cfg;
    @Setter
    public Lang lang;
    private FormatTime formatTime;

    private ClanGlow clanGlow;

    private ClanManager clanManager;
    private Storage storage;

    private boolean packetInit = true;

    private Logger LOGGER;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = LogInitialize.getLogger(this);

        setupEconomy();

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
            ClanCommand cmd = new ClanCommand(this);
            clanCommand.setExecutor(cmd);
            clanCommand.setTabCompleter(cmd);
        }

        try {
            PacketEvents.getAPI().init();
        } catch (Exception e) {
            packetInit = false;
        }


        clanManager = new ClanManager(this);

        clanGlow = new ClanGlow();

        storage = new YAML(this);
        storage.load();

        getServer().getPluginManager().registerEvents(new UserLoader(this), this);

    }

    @Override
    public void onDisable() {
        if (storage!=null) storage.save();
        PacketEvents.getAPI().terminate();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            LOGGER.error("Vault was not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LOGGER.error("Vault economy plugin was not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        this.economy = rsp.getProvider();
        return true;
    }
}
