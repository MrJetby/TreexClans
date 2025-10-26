package me.jetby.xClans.functions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class QuestsListeners implements Listener {

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();

    }
}
