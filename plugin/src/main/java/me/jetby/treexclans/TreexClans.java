package me.jetby.treexclans;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.jodexindustries.jguiwrapper.common.JGuiInitializer;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.tools.LogInitialize;
import me.jetby.treex.tools.log.Logger;
import me.jetby.treexclans.addon.AddonManagerImpl;
import me.jetby.treexclans.api.TreexClansAPI;
import me.jetby.treexclans.api.addons.AddonManager;
import me.jetby.treexclans.api.addons.commands.CommandService;
import me.jetby.treexclans.api.gui.GuiFactory;
import me.jetby.treexclans.api.service.ClanManager;
import me.jetby.treexclans.api.service.leaderboard.LeaderboardService;
import me.jetby.treexclans.commands.CommandServiceImpl;
import me.jetby.treexclans.commands.admin.AdminCommand;
import me.jetby.treexclans.commands.clan.ClanCommand;
import me.jetby.treexclans.configurations.*;
import me.jetby.treexclans.functions.glow.Glow;
import me.jetby.treexclans.functions.quests.QuestManager;
import me.jetby.treexclans.functions.tops.LeaderboardServiceImpl;
import me.jetby.treexclans.gui.CommandRegistrar;
import me.jetby.treexclans.gui.GuiFactoryImpl;
import me.jetby.treexclans.gui.GuiLoader;
import me.jetby.treexclans.hooks.ClanPlaceholder;
import me.jetby.treexclans.hooks.TreexAutoDownload;
import me.jetby.treexclans.hooks.Vault;
import me.jetby.treexclans.listeners.ClanListeners;
import me.jetby.treexclans.listeners.QuestsListeners;
import me.jetby.treexclans.storage.Storage;
import me.jetby.treexclans.storage.YAML;
import me.jetby.treexclans.tools.FormatTime;
import me.jetby.treexclans.tools.bStats;
import me.jetby.treexclans.tools.customactions.Actions;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashSet;

@Getter
public final class TreexClans extends JavaPlugin implements TreexClansAPI {

    private static TreexClans INSTANCE;
    private CommandService commandService;
    private GuiFactory guiFactory;

    public static TreexClans getInstance() {
        return INSTANCE;
    }

    private JavaPlugin plugin;
    private Economy economy;
    @Setter
    private Config cfg;
    @Setter
    public Lang lang;
    private FormatTime formatTime;
    @Setter
    private Glow glow;
    private ClanManager clanManager;
    private LeaderboardService leaderboardService;
    private Storage storage;

    public static Logger LOGGER;
    public static NamespacedKey NAMESPACED_KEY;
    @Setter
    private GuiLoader guiLoader;

    @Setter
    private QuestsLoader questsLoader;
    private QuestManager questManager;
    private ClanPlaceholder clanPlaceholder;

    private Modules modules;

    private AddonManager addonManagerImpl;

    @Override
    public void onLoad() {
        PacketEvents.getAPI().getEventManager().registerListener(
                glow = new Glow(this), PacketListenerPriority.NORMAL);
    }

    @Override
    public void onEnable() {
        this.plugin = this;
        INSTANCE = this;
        this.guiFactory = new GuiFactoryImpl();
        InstanceFactory.GUI_FACTORY = guiFactory;
        InstanceFactory.ITEM_KEY = new NamespacedKey("treexclans", "item");
        NAMESPACED_KEY = InstanceFactory.ITEM_KEY;

        try {
            new TreexAutoDownload(this);
            new Actions().registerCustomActions();
        } catch (IOException ex) {
            getLogger().warning("Failed to initialize Treex: " + ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        LOGGER = LogInitialize.getLogger(this);

        clanPlaceholder = new ClanPlaceholder(this);
        if (clanPlaceholder.isPapi()) {
            clanPlaceholder.register();
        }

        economy = new Vault().getEconomy();

        new ConfigUpdater(getConfig().getInt("config-version", 1));

        cfg = new Config(this);
        cfg.load();

        formatTime = new FormatTime(this);

        modules = new Modules();
        modules.load();

        clanManager = new ClanManagerImpl(this);

        JGuiInitializer.init(this, false);

        guiLoader = new GuiLoader(this, getDataFolder());
        guiLoader.load();
        CommandRegistrar.createCommands(this);


        this.commandService = new CommandServiceImpl();

        PluginCommand xClanCommand = this.getCommand("xclan");
        if (xClanCommand != null) {
            AdminCommand cmd = new AdminCommand(commandService);
            xClanCommand.setExecutor(cmd);
            xClanCommand.setTabCompleter(cmd);
        }
        clanCommand = this.getCommand("clan");
        if (clanCommand != null) {
            ClanCommand cmd = new ClanCommand(this);
            clanCommand.setExecutor(cmd);
            clanCommand.setTabCompleter(cmd);
        }


        questsLoader = new QuestsLoader();
        questsLoader.load();

        questManager = new QuestManager(this);

        storage = new YAML(this);
        storage.load();

        leaderboardService = new LeaderboardServiceImpl(this);

        new bStats(this, 27749);

        getServer().getPluginManager().registerEvents(new ClanListeners(this), this);
        getServer().getPluginManager().registerEvents(new QuestsListeners(this), this);

        getServer().getServicesManager().register(
                TreexClansAPI.class,            // интерфейс
                this,    // реализация
                this,                           // владелец (плагин)
                ServicePriority.Normal
        );

        addonManagerImpl = new AddonManagerImpl(this, true);
        addonManagerImpl.loadAddons();
    }

    private PluginCommand clanCommand;

    @Override
    public void onDisable() {
        if (addonManagerImpl != null) {
            addonManagerImpl.unloadAll();
        }
        getServer().getServicesManager().unregister(TreexClansAPI.class);
        if (storage != null) storage.save();
        disableGlowForAll();
        if (clanPlaceholder != null) {
            if (clanPlaceholder.isPapi()) {
                clanPlaceholder.unregister();
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getOpenInventory().close();
        }
    }

    private void disableGlowForAll() {
        for (var clan : cfg.getClans().values()) {
            var memberImpls = new HashSet<>(clan.getMembers());
            memberImpls.add(clan.getLeader());
            for (var memberImpl : memberImpls) {
                Player player = Bukkit.getPlayer(memberImpl.getUuid());
                if (player != null) {
                    if (glow.hasObserver(player)) {
                        glow.removeObserver(player);
                    }
                }
            }
        }
    }
}
