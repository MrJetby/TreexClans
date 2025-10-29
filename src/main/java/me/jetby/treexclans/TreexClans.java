package me.jetby.treexclans;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.jodexindustries.jguiwrapper.common.JGuiInitializer;
import lombok.Getter;
import lombok.Setter;
import me.jetby.treex.tools.LogInitialize;
import me.jetby.treex.tools.log.Logger;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import me.jetby.treexclans.commands.clan.ClanCommand;
import me.jetby.treexclans.commands.admin.AdminCommand;
import me.jetby.treexclans.configurations.Config;
import me.jetby.treexclans.configurations.Lang;
import me.jetby.treexclans.configurations.QuestsLoader;
import me.jetby.treexclans.functions.glow.Glow;
import me.jetby.treexclans.functions.quests.QuestManager;
import me.jetby.treexclans.gui.CommandRegistrar;
import me.jetby.treexclans.gui.GuiLoader;
import me.jetby.treexclans.listeners.ClanListeners;
import me.jetby.treexclans.listeners.QuestsListeners;
import me.jetby.treexclans.storage.Storage;
import me.jetby.treexclans.storage.YAML;
import me.jetby.treexclans.tools.FormatTime;
import me.jetby.treexclans.tools.TreexInitializer;
import me.jetby.treexclans.tools.customactions.Actions;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

    private GuiLoader guiLoader;

    private QuestsLoader questsLoader;
    private QuestManager questManager;


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

        JGuiInitializer.init(this, false);
        guiLoader = new GuiLoader(this, getDataFolder());
        guiLoader.load();
        CommandRegistrar.createCommands(this);

        PluginCommand xClanCommand = this.getCommand("xclan");
        if (xClanCommand != null) {
            AdminCommand cmd = new AdminCommand();
            xClanCommand.setExecutor(cmd);
            xClanCommand.setTabCompleter(cmd);
        }
        PluginCommand clanCommand = this.getCommand("clan");
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

        getServer().getPluginManager().registerEvents(new ClanListeners(this), this);
        getServer().getPluginManager().registerEvents(new QuestsListeners(this), this);

    }
    @Override
    public void onDisable() {
        if (storage != null) storage.save();
        disableGlowForAll();
    }

    private void disableGlowForAll() {
        for (Clan clan : cfg.getClans().values()) {
            Set<Member> members = new HashSet<>(clan.getMembers());
            members.add(clan.getLeader());
            for (Member member : members) {
                Player player = Bukkit.getPlayer(member.getUuid());
                if (player != null) {
                    if (glow.hasObserver(player)) {
                        glow.removeObserver(player);
                    }
                }
            }
        }
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            LOGGER.error("Vault was not found!");
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LOGGER.error("Vault economy plugin was not found!");
            return;
        }

        this.economy = rsp.getProvider();
    }
}
