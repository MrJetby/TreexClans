package me.jetby.xClans.functions;

import lombok.RequiredArgsConstructor;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.records.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static me.jetby.xClans.TreexClans.LOGGER;

@RequiredArgsConstructor
public class ClanListeners implements Listener {

    private final TreexClans plugin;

    @Deprecated
    @EventHandler
    public void clanChat(AsyncPlayerChatEvent e) {
        if (!plugin.getClanManager().isInClan(e.getPlayer().getUniqueId())) return;
        Clan clan = plugin.getClanManager().getClanByMember(e.getPlayer().getUniqueId());
        if (clan == null) return;
        if (!clan.getMember(e.getPlayer().getUniqueId()).isChat()) return;
        plugin.getClanManager().sendChat(clan, e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }

    @EventHandler
    public void clanPvp(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            if (!plugin.getClanManager().isInClan(player.getUniqueId())) return;
            Clan clan = plugin.getClanManager().getClanByMember(player.getUniqueId());
            if (clan == null) return;
            if (clan.isPvp()) return;
            if (e.getEntity() instanceof Player target) {
                if (plugin.getClanManager().getClanByMember(target.getUniqueId())!=null && plugin.getClanManager().getClanByMember(target.getUniqueId()).equals(clan)) {
                    plugin.getLang().sendMessage(player, clan, "pvp-disabled");
                    e.setCancelled(true);
                }
            }
        }
    }
}
