package me.jetby.xClans.functions.glow;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.jetby.xClans.TreexClans;
import me.jetby.xClans.clan.Clan;
import me.jetby.xClans.clan.Member;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public record Glow(TreexClans plugin) implements PacketListener {

    private static final Map<UUID, Set<Member>> observersToTargets = new HashMap<>();

    public void addObserver(Player observer, Set<Member> targets) {
        if (!observer.isOnline()) return;
        Clan clan = plugin.getClanManager().getClanByMember(observer.getUniqueId());
        if (clan==null) return;
        Member m = clan.getMember(observer.getUniqueId());
        observersToTargets.put(observer.getUniqueId(), new HashSet<>(targets));
        for (Member member : targets) {
            Player target = Bukkit.getPlayer(member.getUuid());
            if (target!=null) {
                Color color = m.getGlowColors().getOrDefault(target.getUniqueId(), Color.LIME);
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                        PacketEvents.getAPI().getPlayerManager().sendPacket(observer, createPacket(target.getEntityId(), color)));
            }
        }

    }

    public void removeObserver(Player observer) {
        for (Map.Entry<UUID, Set<Member>> entry : observersToTargets.entrySet()) {
            for (Member member : entry.getValue()) {
                Player target = Bukkit.getPlayer(member.getUuid());
                if (target!=null) {
                    EquipmentUtil.sendDefaultEquipment(observer, target);
                }
            }
        }
        observersToTargets.remove(observer.getUniqueId());
    }

    public boolean hasObserver(Player observer) {
        return observersToTargets.containsKey(observer.getUniqueId());
    }

    private static List<Equipment> getEquipment(Color color) {
        return EquipmentUtil.withItemStacks(
                color,
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_BOOTS)
        );
    }

    @Override
    public void onPacketSend(@NotNull PacketSendEvent event) {
        PacketTypeCommon packetCommon = event.getPacketType();

        if (!(packetCommon instanceof Server type)) return;

        if (type != Server.SPAWN_ENTITY && type != Server.SPAWN_PLAYER && type != Server.ENTITY_EQUIPMENT) {
            return;
        }

        int entityId = getEntityId(packetCommon, event);

        if (entityId == -1) return;

        Player player = event.getPlayer();

        UUID observerUUID = event.getUser().getUUID();
        Set<Member> targets = observersToTargets.get(observerUUID);
        if (targets == null) return;

        Entity entity = SpigotConversionUtil.getEntityById(player.getWorld(), entityId);
        if (!(entity instanceof Player target)) return;

        boolean isTarget = targets.stream().anyMatch(m -> m.getUuid().equals(entity.getUniqueId()));
        if (!isTarget) return;

        Clan clan = plugin.getClanManager().getClanByMember(observerUUID);
        if (clan==null) return;
        Member m = clan.getMember(observerUUID);

        if (type == Server.ENTITY_EQUIPMENT) {
            Object buffer = createBuffer(event, m.getGlowColors().getOrDefault(target.getUniqueId(), Color.LIME));
            event.setByteBuf(buffer);
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> event.getUser().sendPacket(createPacket(entityId, m.getGlowColors().getOrDefault(target.getUniqueId(), Color.LIME))));
        }
    }

    private int getEntityId(@NotNull PacketTypeCommon common, @NotNull PacketSendEvent event) {
        Class<? extends PacketWrapper<?>> wrapperClass = common.getWrapperClass();
        if (wrapperClass == null) return -1;

        try {
            Constructor<? extends PacketWrapper<?>> constructor = wrapperClass.getDeclaredConstructor(PacketSendEvent.class);

            PacketWrapper<?> packetWrapper = constructor.newInstance(event);

            Method getEntityId = wrapperClass.getDeclaredMethod("getEntityId");
            return (int) getEntityId.invoke(packetWrapper);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error with getting entity id", e);
            return -1;
        }
    }

    private @NotNull WrapperPlayServerEntityEquipment createPacket(int entityId, Color color) {
        return new WrapperPlayServerEntityEquipment(entityId, getEquipment(color));
    }

    private Object createBuffer(PacketSendEvent event, Color color) {
        WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);
        wrapper.setEquipment(getEquipment(color));
        wrapper.write();

        return wrapper.buffer;
    }
}
