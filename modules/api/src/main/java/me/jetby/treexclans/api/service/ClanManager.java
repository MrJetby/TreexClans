package me.jetby.treexclans.api.service;

import me.jetby.treexclans.api.service.clan.Clan;
import me.jetby.treexclans.api.service.clan.member.Member;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Central API for interacting with clans in TreexClans.
 * <p>
 * Provides access to all clan-related operations:
 * creation, deletion, messaging, color management,
 * member lookups, validation and economy features.
 */
public interface ClanManager {

    /**
     * @return access to clan creation and deletion logic.
     */
    @NotNull Lifecycle lifecycle();

    /**
     * @return access to validation (names, prefixes, regex).
     */
    @NotNull Validation validation();

    /**
     * @return access to chat and messaging functionality.
     */
    @NotNull Chat chat();

    /**
     * @return access to balance (economy) operations.
     */
    @NotNull Economy economy();

    /**
     * @return access to glow color customization.
     */
    @NotNull Colors colors();

    /**
     * @return access to clan lookup utilities.
     */
    @NotNull Lookup lookup();

    /* ----------------------------------------------------
     * SUB-INTERFACES
     * ---------------------------------------------------- */

    /** Clan creation and deletion lifecycle. */
    interface Lifecycle {

        boolean createClan(@NotNull String name, @NotNull Clan clan);

        boolean createClan(@NotNull String name, @NotNull Player leader);

        void deleteClan(@NotNull Clan clan, @Nullable Player initiator);

        boolean deleteClan(@NotNull String name);

        boolean clanExists(@NotNull String name);
    }

    /** Validation and naming rules for clans and prefixes. */
    interface Validation {

        boolean isAllowedName(@NotNull Player player, @NotNull String clanName);

        boolean isAllowedPrefix(@NotNull Player player, @NotNull String prefix);

        boolean isAllowedRegex(@NotNull String text, @NotNull String regex);
    }

    /** Handles sending messages and clan chat formatting. */
    interface Chat {

        void sendMessage(@NotNull Clan clan, @NotNull String message);

        void sendChat(@NotNull Clan clan, @NotNull Player sender, @NotNull String message);
    }

    /** Clan economy operations: balance add, take, get. */
    interface Economy {

        void addBalance(double amount, @NotNull Clan clan);

        void takeBalance(double amount, @NotNull Clan clan);

        double getBalance(@NotNull Clan clan);
    }

    /** Glow color customization between clan members. */
    interface Colors {

        void setColor(@NotNull Clan clan, @NotNull Member member, @NotNull Color color);

        void setColor(@NotNull Member member, @NotNull Set<Member> members, @NotNull Color color);

        void setColor(@NotNull Member member, @NotNull Member target, @NotNull Color color);
    }

    /** Clan and member lookup utilities. */
    interface Lookup {

        boolean isInClan(@NotNull UUID uuid);

        boolean isInClan(@NotNull String uuidString);

        @Nullable Clan getClan(@NotNull String name);

        @Nullable Clan getClanByMember(@NotNull UUID uuid);

        @Nullable Clan getClanByMember(@NotNull String uuidString);

        @Nullable Clan getClanByMember(@NotNull Member member);

        @NotNull String getLastOnlineFormatted(@NotNull UUID uuid);

        @NotNull String getLastOnlineFormatted(@NotNull Member member);
    }
}
