package me.jetby.xClans.records;


import me.jetby.xClans.records.rank.Rank;

import java.util.UUID;

public record Member(
        UUID uuid,
        Rank rank,
        Level level
) {
}
