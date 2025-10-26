package me.jetby.xClans.functions;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.records.Member;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ClanGlow implements PacketListener {

    private final Map<UUID, Set<Member>> observersToTargets = new HashMap<>();
    private BukkitRunnable refreshTask;

    public ClanGlow() {
        startRefreshTask();
    }

    public void addObserver(Player observer, Set<Member> targets) {
        if (!observer.isOnline()) return;
        observersToTargets.put(observer.getUniqueId(), new HashSet<>(targets));

        applyGlowForObserver(observer);
    }

    public void removeObserver(Player observer) {
        observersToTargets.remove(observer.getUniqueId());
        resetGlowForObserver(observer);

    }

    public boolean hasObserver(Player observer) {
        return observersToTargets.containsKey(observer.getUniqueId());
    }

    public void cleanupAll() {
        for (UUID uuid : new HashSet<>(observersToTargets.keySet())) {
            Player observer = Bukkit.getPlayer(uuid);
            if (observer == null) continue;
            removeObserver(observer);
        }
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    private void applyGlowForObserver(Player observer) {
        Set<Member> targets = observersToTargets.get(observer.getUniqueId());
        if (targets == null) return;
        for (Member member : targets) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player != null) {
                sendGlowEquipment(observer, player, member.getGlowColors().get(member));
            }
        }
    }

    private void resetGlowForObserver(Player observer) {
        if (!observer.isOnline()) return;
        Set<Member> targets = observersToTargets.get(observer.getUniqueId());
        if (targets == null) return;
        for (Member member : targets) {
            Player player = Bukkit.getPlayer(member.getUuid());
            if (player != null) {
                sendResetEquipment(observer, player);
            }
        }
    }

    private void sendGlowEquipment(Player observer, Player target, Color color) {
        List<Equipment> equipmentList = createGreenEquipmentList(color);
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(
                target.getEntityId(), equipmentList
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
    }

    private void sendResetEquipment(Player observer, Player target) {
        List<Equipment> emptyList = new ArrayList<>();
        WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(
                target.getEntityId(), emptyList
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
    }

    private List<Equipment> createGreenEquipmentList(Color color) {
        List<Equipment> equipmentList = new ArrayList<>();
        ItemStack[] armorPieces = {
                createColoredLeather(Material.LEATHER_HELMET, color),
                createColoredLeather(Material.LEATHER_CHESTPLATE, color),
                createColoredLeather(Material.LEATHER_LEGGINGS, color),
                createColoredLeather(Material.LEATHER_BOOTS, color)
        };
        equipmentList.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(armorPieces[0])));
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(armorPieces[1])));
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(armorPieces[2])));
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(armorPieces[3])));
        return equipmentList;
    }

    private ItemStack createColoredLeather(Material material, Color color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof LeatherArmorMeta lam) {
            lam.setColor(color);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void startRefreshTask() {
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {

                for (Map.Entry<UUID, Set<Member>> entry : new HashSet<>(observersToTargets.entrySet())) {
                    Player observer = Bukkit.getPlayer(entry.getKey());
                    if (observer == null || !observer.isOnline()) continue;

                    applyGlowForObserver(observer);
                }
            }
        };
        refreshTask.runTaskTimerAsynchronously(TreexClans.getInstance(), 100L, 100L); // 5 сек
    }
}