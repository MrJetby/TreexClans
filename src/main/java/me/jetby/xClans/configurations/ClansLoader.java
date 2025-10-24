package me.jetby.xClans.configurations;

import lombok.Getter;
import me.jetby.xClans.records.Level;
import me.jetby.xClans.records.rank.Rank;

import java.util.HashSet;
import java.util.Set;

public class ClansLoader {

    @Getter
    private final Set<Rank> defaultRanks = new HashSet<>();
    @Getter
    private final Set<Level> levels = new HashSet<>();



}
