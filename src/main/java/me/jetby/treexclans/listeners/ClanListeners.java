package me.jetby.treexclans.listeners;

import me.jetby.treexclans.ClanManager;
import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.clan.Clan;
import me.jetby.treexclans.clan.Member;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
        if (!plugin.getClanManager().isInClan(e.getPlayer().getUniqueId())) return;
        Clan clan = plugin.getClanManager().getClanByMember(e.getPlayer().getUniqueId());
        if (clan == null) return;
        if (!clan.getMember(e.getPlayer().getUniqueId()).isChat()) return;
        plugin.getClanManager().sendChat(clan, e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }

    @EventHandler
    public void onTeamDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) return;
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (clan == null) return;
            if (clan.isPvp()) return;
            if (e.getEntity() instanceof Player target) {
                if (plugin.getClanManager().getClanByMember(target.getUniqueId()) != null && plugin.getClanManager().getClanByMember(target.getUniqueId()).equals(clan)) {
                    plugin.getLang().sendMessage(player, clan, "pvp-disabled");
                    e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onTeamDamageByProjectile(ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) return;
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (clan == null) return;
            if (clan.isPvp()) return;
            if (e.getHitEntity() instanceof Player target) {
                if (plugin.getClanManager().getClanByMember(target.getUniqueId()) != null && plugin.getClanManager().getClanByMember(target.getUniqueId()).equals(clan)) {
                    plugin.getLang().sendMessage(player, clan, "pvp-disabled");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onClanKillsOrDeaths(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();
        if (manager.isInClan(player.getUniqueId())) {
            Clan clan = manager.getClanByMember(player.getUniqueId());
            Member member = clan.getMember(player.getUniqueId());
            member.setDeaths(member.getDeaths() + 1);
        }
        if (killer != null) {
            if (manager.isInClan(killer.getUniqueId())) {
                Clan clan = manager.getClanByMember(killer.getUniqueId());
                Member member = clan.getMember(killer.getUniqueId());
                member.setKills(member.getKills() + 1);
            }
        }
    }
}
