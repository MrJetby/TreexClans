package me.jetby.treexclans.hooks;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import static me.jetby.treexclans.TreexClans.LOGGER;

public class Vault {
    @Getter
    private final Economy economy;

    public Vault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            LOGGER.error("Vault was not found!");
            this.economy = null;
            return;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            LOGGER.error("Vault economy plugin was not found!");
            this.economy = null;
            return;
        }
        this.economy = rsp.getProvider();
    }
}
