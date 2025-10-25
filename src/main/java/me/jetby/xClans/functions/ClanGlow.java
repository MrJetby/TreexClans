package me.jetby.xClans.functions;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.*;
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
        if (observersToTargets.remove(observer.getUniqueId()) != null) {
            resetGlowForObserver(observer);
        }
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
            Player player = Bukkit.getPlayer(member.uuid());
            if (player!=null) {
                sendGlowEquipment(observer, player);
            }
        }
    }

    private void resetGlowForObserver(Player observer) {
        if (!observer.isOnline()) return;
        Set<Member> targets = observersToTargets.get(observer.getUniqueId());
        if (targets == null) return;
        for (Member member : targets) {
            Player player = Bukkit.getPlayer(member.uuid());
            if (player!=null) {
                sendResetEquipment(observer, player);
            }
        }
    }

    private void sendGlowEquipment(Player observer, Player target) {
        List<Equipment> equipmentList = createGreenEquipmentList();
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

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (observersToTargets.isEmpty()) return;
        Player eventObserver = event.getPlayer();
        Set<Member> targets = observersToTargets.get(eventObserver.getUniqueId());
        if (targets == null) return;

        PacketTypeCommon type = event.getPacketType();
        if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
            int entityId = wrapper.getEntityId();
            Player target = findTargetByEntityId(targets, entityId);
            if (target != null) {
                List<Equipment> equipmentList = createGreenEquipmentList();
                wrapper.setEquipment(equipmentList);
            }
        } else if (type == PacketType.Play.Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer spawnWrapper = new WrapperPlayServerSpawnPlayer(event);
            int entityId = spawnWrapper.getEntityId();
            Player target = findTargetByEntityId(targets, entityId);
            if (target != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (eventObserver.isOnline()) {
                            sendGlowEquipment(eventObserver, target);
                        }
                    }
                }.runTaskLater(TreexClans.getInstance(), 1L);
            }
        } else if (type == PacketType.Play.Server.ENTITY_METADATA) {
            WrapperPlayServerEntityMetadata metaWrapper = new WrapperPlayServerEntityMetadata(event);
            int entityId = metaWrapper.getEntityId();
            Player target = findTargetByEntityId(targets, entityId);
            if (target != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (eventObserver.isOnline()) {
                            sendGlowEquipment(eventObserver, target);
                        }
                    }
                }.runTaskLater(TreexClans.getInstance(), 1L);
            }
        }
    }

    private Player findTargetByEntityId(Set<Member> targets, int entityId) {
        for (Member member : targets) {
            Player p = Bukkit.getPlayer(member.uuid());
            if (p == null) continue;
            if (p.isOnline() && p.getEntityId() == entityId) {
                return p;
            }
        }
        return null;
    }

    private List<Equipment> createGreenEquipmentList() {
        List<Equipment> equipmentList = new ArrayList<>();
        ItemStack[] armorPieces = {
                createColoredLeather(Material.LEATHER_HELMET),
                createColoredLeather(Material.LEATHER_CHESTPLATE),
                createColoredLeather(Material.LEATHER_LEGGINGS),
                createColoredLeather(Material.LEATHER_BOOTS)
        };
        equipmentList.add(new Equipment(EquipmentSlot.HELMET, SpigotConversionUtil.fromBukkitItemStack(armorPieces[0])));
        equipmentList.add(new Equipment(EquipmentSlot.CHEST_PLATE, SpigotConversionUtil.fromBukkitItemStack(armorPieces[1])));
        equipmentList.add(new Equipment(EquipmentSlot.LEGGINGS, SpigotConversionUtil.fromBukkitItemStack(armorPieces[2])));
        equipmentList.add(new Equipment(EquipmentSlot.BOOTS, SpigotConversionUtil.fromBukkitItemStack(armorPieces[3])));
        return equipmentList;
    }

    private ItemStack createColoredLeather(Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof LeatherArmorMeta lam) {
            lam.setColor(Color.GREEN);
        }
        item.setItemMeta(meta);
        return item;
    }

    private void startRefreshTask() {
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                observersToTargets.keySet().removeIf(observer -> {
                    Player p = Bukkit.getPlayer(observer);
                    return p == null || !p.isOnline();
                });

                for (Map.Entry<UUID, Set<Member>> entry : new HashSet<>(observersToTargets.entrySet())) {
                    Player observer = Bukkit.getPlayer(entry.getKey());
                    if (observer == null || !observer.isOnline()) continue;

                    resetGlowForObserver(observer);
                    applyGlowForObserver(observer);
                }
            }
        };
        refreshTask.runTaskTimer(TreexClans.getInstance(), 100L, 100L); // 5 сек
    }
}