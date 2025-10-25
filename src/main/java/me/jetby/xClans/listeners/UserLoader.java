package me.jetby.xClans.listeners;

import me.jetby.xClans.TreexClans;
import me.jetby.xClans.records.Clan;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class UserLoader implements Listener {

    private final TreexClans plugin;

    public UserLoader(TreexClans plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
            Clan clan = plugin.getClanManager().getClanByMember(uuid);
            if (clan==null) return;
            plugin.getClanManager().getClanByMember(uuid).getMember(uuid).setLastOnline(System.currentTimeMillis());
        });
    }
}
