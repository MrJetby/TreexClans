package me.jetby.xClans;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.jodexindustries.jguiwrapper.common.JGuiInitializer;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.tools.LogInitialize;
import me.jetby.treex.tools.log.Logger;
import me.jetby.xClans.commands.clan.ClanCommand;
import me.jetby.xClans.commands.xclan.XClanCommand;
import me.jetby.xClans.configurations.Config;
import me.jetby.xClans.configurations.Lang;
import me.jetby.xClans.listeners.ClanListeners;
import me.jetby.xClans.functions.glow.Glow;
import me.jetby.xClans.gui.CommandRegistrar;
import me.jetby.xClans.gui.Loader;
import me.jetby.xClans.storage.Storage;
import me.jetby.xClans.storage.YAML;
import me.jetby.xClans.tools.FormatTime;
import me.jetby.xClans.tools.TreexInitializer;
import me.jetby.xClans.tools.customactions.Actions;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

@Getter
public final class TreexClans extends JavaPlugin {

    private static TreexClans INSTANCE;

    public static TreexClans getInstance() {
        return INSTANCE;
    }
    private Economy economy = null;

    private Config cfg;
    @Setter
    public Lang lang;
    private FormatTime formatTime;

    private Glow glow;

    private ClanManager clanManager;
    private Storage storage;


    public static Logger LOGGER;
    public static final NamespacedKey NAMESPACED_KEY = new NamespacedKey("treexclans", "item");

    private Loader menuLoader;


    @Override
    public void onLoad() {
        PacketEvents.getAPI().getEventManager().registerListener(
               glow = new Glow(this), PacketListenerPriority.NORMAL);
    }
    @Override
    public void onEnable() {
        INSTANCE = this;

        try {
            new TreexInitializer(this);
            new Actions().registerCustomActions();
        } catch (IOException ex) {
            getLogger().warning("Failed to initialize Treex: " + ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }


        LOGGER = LogInitialize.getLogger(this);

        setupEconomy();

        cfg = new Config(this);
        cfg.load();

        formatTime = new FormatTime(this);


        clanManager = new ClanManager(this);

        storage = new YAML(this);
        storage.load();

        JGuiInitializer.init(this, false);
        menuLoader = new Loader(this, getDataFolder());
        menuLoader.load();
        CommandRegistrar.createCommands(this);

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


        getServer().getPluginManager().registerEvents(new ClanListeners(this), this);

    }

    @Override
    public void onDisable() {
        if (storage!=null) storage.save();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            LOGGER.error("Vault was not found!");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LOGGER.error("Vault economy plugin was not found!");
            return false;
        }

        this.economy = rsp.getProvider();
        return true;
    }
}
