package me.jetby.treexclans.api.service.clan.member;

import me.jetby.treexclans.api.service.clan.member.rank.Rank;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a clan member within TreexClans.
 * Provides access to player identity, stats, and preferences.
 */
public interface Member {

    /** @return unique player UUID. */
    @NotNull UUID getUuid();

    /** @return current clan rank of the member. */
    @NotNull Rank getRank();
    void setRank(@NotNull Rank rank);

    /** @return timestamp (millis) when member joined the clan. */
    long getJoinedAt();
    void setJoinedAt(long joinedAt);

    /** @return timestamp (millis) when member was last online. */
    long getLastOnline();
    void setLastOnline(long lastOnline);

    /** @return whether clan glow (visual aura) is enabled for this member. */
    boolean isClanGlow();
    void setClanGlow(boolean clanGlow);

    /** @return whether member has clan chat enabled. */
    boolean isChat();
    void setChat(boolean chat);

    /** @return current amount of clan coins the member has. */
    int getCoin();
    void setCoin(int coin);

    /** @return member's current experience points. */
    int getExp();
    void setExp(int exp);

    /** Adds coins to this member’s balance. */
    void addCoin(int amount);

    /** Removes coins from this member’s balance. */
    void takeCoin(int amount);

    /** @return per-player glow color mapping. */
    @NotNull Map<UUID, Color> getGlowColors();
    void setGlowColors(@NotNull Map<UUID, Color> colors);

    /** @return number of kills made by the member. */
    int getKills();
    void setKills(int kills);

    /** @return number of deaths of the member. */
    int getDeaths();
    void setDeaths(int deaths);
}
