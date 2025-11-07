package me.jetby.treexclans.listeners;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.api.service.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;


public class ClanListeners implements Listener {

    private final TreexClans plugin;
    private final ClanManager manager;

    public ClanListeners(TreexClans plugin) {
        this.plugin = plugin;
        this.manager = plugin.getClanManager();
    }

    @Deprecated
    @EventHandler
    public void onClanChat(AsyncPlayerChatEvent e) {
        if (!plugin.getClanManager().lookup().isInClan(e.getPlayer().getUniqueId())) return;
        var clanImpl = plugin.getClanManager().lookup().getClanByMember(e.getPlayer().getUniqueId());
        if (clanImpl == null) return;
        if (!clanImpl.getMember(e.getPlayer().getUniqueId()).isChat()) return;
        plugin.getClanManager().chat().sendChat(clanImpl, e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }

    @EventHandler
    public void onTeamDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) return;
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (clanImpl == null) return;
            if (clanImpl.isPvp()) return;
            if (e.getEntity() instanceof Player target) {
                if (plugin.getClanManager().lookup().getClanByMember(target.getUniqueId()) != null && plugin.getClanManager().lookup().getClanByMember(target.getUniqueId()).equals(clanImpl)) {
                    plugin.getLang().sendMessage(player, clanImpl, "pvp-disabled");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTeamDamageByProjectile(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player player) {
            if (!plugin.getClanManager().lookup().isInClan(player.getUniqueId())) return;
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            if (clanImpl == null) return;
            if (clanImpl.isPvp()) return;
            if (e.getHitEntity() instanceof Player target) {
                if (plugin.getClanManager().lookup().getClanByMember(target.getUniqueId()) != null && plugin.getClanManager().lookup().getClanByMember(target.getUniqueId()).equals(clanImpl)) {
                    plugin.getLang().sendMessage(player, clanImpl, "pvp-disabled");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onClanKillsOrDeaths(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();
        if (manager.lookup().isInClan(player.getUniqueId())) {
            var clanImpl = manager.lookup().getClanByMember(player.getUniqueId());
            var memberImpl = clanImpl.getMember(player.getUniqueId());
            memberImpl.setDeaths(memberImpl.getDeaths() + 1);
        }
        if (killer != null) {
            if (manager.lookup().isInClan(killer.getUniqueId())) {
                var clanImpl = manager.lookup().getClanByMember(killer.getUniqueId());
                var memberImpl = clanImpl.getMember(killer.getUniqueId());
                memberImpl.setKills(memberImpl.getKills() + 1);
            }
        }
    }
}
