package me.jetby.xClans.functions.glow;


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EquipmentUtil {


    private enum Equip {
        HELMET(EquipmentSlot.HELMET, org.bukkit.inventory.EquipmentSlot.HEAD),
        CHEST_PLATE(EquipmentSlot.CHEST_PLATE, org.bukkit.inventory.EquipmentSlot.CHEST),
        LEGGINGS(EquipmentSlot.LEGGINGS, org.bukkit.inventory.EquipmentSlot.LEGS),
        BOOTS(EquipmentSlot.BOOTS, org.bukkit.inventory.EquipmentSlot.FEET);

        public static final Equip[] VALUES = values();

        public final EquipmentSlot packet;
        public final org.bukkit.inventory.EquipmentSlot bukkit;

        Equip(EquipmentSlot packet, org.bukkit.inventory.EquipmentSlot bukkit) {
            this.packet = packet;
            this.bukkit = bukkit;
        }
    }

    public static void sendDefaultEquipment(Player source, Player target) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(source, packetFromPlayer(target));
    }

    @Contract("_ -> new")
    public static @NotNull WrapperPlayServerEntityEquipment packetFromPlayer(Player source) {
        return new WrapperPlayServerEntityEquipment(source.getEntityId(), equipmentFromPlayer(source));
    }

    public static @NotNull List<Equipment> equipmentFromPlayer(Player player) {
        List<Equipment> list = new ArrayList<>();

        EntityEquipment equipment = player.getEquipment();
        for (var slot : Equip.VALUES) {
            ItemStack item = equipment.getItem(slot.bukkit);

            list.add(new Equipment(slot.packet, SpigotConversionUtil.fromBukkitItemStack(item)));
        }

        return list;
    }

    /**
     * Creates a list of {@link Equipment} objects from the provided {@link ItemStack}s.
     * <p>
     * Only the first four elements are considered, corresponding to the equipment slots:
     * <b>helmet</b>, <b>chestplate</b>, <b>leggings</b>, and <b>boots</b>.
     * Any additional {@code ItemStack}s beyond the fourth one are ignored.
     * </p>
     *
     * @param itemStacks an array of {@link ItemStack}s representing the player's armor items
     *                   (in order: helmet, chestplate, leggings, boots)
     * @return a list of {@link Equipment} objects mapped to their respective {@link EquipmentSlot}s
     */
    public static @NotNull List<Equipment> withItemStacks(Color color, ItemStack... itemStacks) {
        List<Equipment> list = new ArrayList<>();
        for (int i = 0; i < Math.min(itemStacks.length, 4); i++) {
            ItemStack itemStack = itemStacks[i];
            ItemMeta meta = itemStack.getItemMeta();
            if (meta instanceof LeatherArmorMeta lam) {
                lam.setColor(color);
            }
            itemStack.setItemMeta(meta);
            list.add(new Equipment(Equip.VALUES[i].packet, SpigotConversionUtil.fromBukkitItemStack(itemStack)));
        }
        return list;
    }
}
