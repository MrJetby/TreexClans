package me.jetby.xClans.records;


import me.jetby.xClans.records.rank.Rank;
import org.bukkit.Color;

import java.util.UUID;

public record Member(
        UUID uuid,
        Rank rank,
        int joinedAt,
        int lastOnline,
        boolean clanGlow,
        Color glowColor
) {
}
