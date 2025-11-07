package me.jetby.treexclans.listeners;

import me.jetby.treexclans.TreexClans;
import me.jetby.treexclans.functions.quests.QuestManager;
import me.jetby.treexclans.functions.quests.QuestType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class QuestsListeners implements Listener {

    private final TreexClans plugin;
    private final QuestManager questManager;

    public QuestsListeners(TreexClans plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getQuestManager();
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Player killer = player.getKiller();

        if (killer != null) {
            run(killer, QuestType.PLAYER_KILL, null);
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        Player killer = e.getEntity().getKiller();

        if (killer != null) {
            run(killer, QuestType.ENTITY_KILL, entity.getType().name());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        run(player, QuestType.BLOCK_PLACE, e.getBlock().getType().name());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        run(player, QuestType.BLOCK_BREAK, e.getBlock().getType().name());
    }

    public void run(Player player, QuestType questType, String property) {
        if (plugin.getClanManager().lookup().isInClan(player.getUniqueId())) {
            var clanImpl = plugin.getClanManager().lookup().getClanByMember(player.getUniqueId());
            questManager.addProgressViaChecks(player, clanImpl.getMember(player.getUniqueId()), questType, property, 1);
        }
    }
}
