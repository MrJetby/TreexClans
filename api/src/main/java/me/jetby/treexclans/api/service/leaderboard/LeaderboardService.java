package me.jetby.treexclans.api.service.leaderboard;


import me.jetby.treexclans.api.service.clan.Clan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Provides access to clan leaderboard functionality.
 * <p>
 * Allows retrieving top clans by various criteria such as kills,
 * deaths, balance, level, or member count.
 */
public interface LeaderboardService {

    /**
     * Represents different leaderboard types.
     */
    enum TopType {
        /**
         * Clan kill count (total kills by members).
         */
        KILLS,

        /**
         * Clan death count (total deaths by members).
         */
        DEATHS,

        /**
         * Kill/death ratio (K/D).
         */
        KD,

        /**
         * Total clan balance.
         */
        BALANCE,

        /**
         * Clan level.
         */
        LEVEL,

        /**
         * Total number of clan members (including leader).
         */
        MEMBERS
    }

    /**
     * Retrieves a clan from the specified top list by its rank.
     *
     * @param type     leaderboard type (KILLS, KD, LEVEL, etc.)
     * @param position rank position (1-based)
     * @return the clan at the given position, or null if none
     */
    @Nullable
    Clan getTopClan(@NotNull TopType type, int position);

    Object getTopProgress(@NotNull Clan clan, TopType type);

    /**
     * Gets the full sorted leaderboard of the specified type.
     *
     * @param type leaderboard type
     * @return ordered list of clans, best to worst (may be empty)
     */
    @NotNull
    List<Clan> getTopList(@NotNull TopType type);
}