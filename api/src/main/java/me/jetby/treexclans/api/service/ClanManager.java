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
 * Provides access to all major clan-related features such as:
 * creation, deletion, economy, chat, validation, and visual customization.
 * </p>
 *
 * <p>
 * The {@link ClanManager} acts as the main entry point for addons and plugins
 * that need to interact with the TreexClans system.
 * </p>
 */
public interface ClanManager {

    /**
     * Provides access to the clan creation and deletion lifecycle.
     *
     * @return the lifecycle management API.
     */
    @NotNull Lifecycle lifecycle();

    /**
     * Provides access to name and prefix validation rules.
     *
     * @return the validation API.
     */
    @NotNull Validation validation();

    /**
     * Provides access to the clan chat and message broadcasting system.
     *
     * @return the chat API.
     */
    @NotNull Chat chat();

    /**
     * Provides access to clan balance and economy-related operations.
     *
     * @return the economy API.
     */
    @NotNull Economy economy();

    /**
     * Provides access to clan glow color customization utilities.
     *
     * @return the color management API.
     */
    @NotNull Colors colors();

    /**
     * Provides access to clan and member lookup utilities.
     *
     * @return the lookup API.
     */
    @NotNull Lookup lookup();

    // ------------------------------------------------------------------------
    // Nested interfaces
    // ------------------------------------------------------------------------

    /**
     * Clan creation and deletion lifecycle management.
     * <p>
     * Handles the creation, registration, and removal of clans.
     * This API ensures that all related data and members
     * are properly handled when clans are created or deleted.
     * </p>
     */
    interface Lifecycle {

        /**
         * Creates a new clan with the specified name and data.
         *
         * @param name  the clan name.
         * @param clan  the clan instance.
         * @return true if successfully created, false otherwise.
         */
        boolean createClan(@NotNull String name, @NotNull Clan clan);

        /**
         * Creates a new clan with the given name and leader.
         *
         * @param name    the clan name.
         * @param leader  the player who becomes the clan leader.
         * @return true if the clan was successfully created.
         */
        boolean createClan(@NotNull String name, @NotNull Player leader);

        /**
         * Deletes a clan and performs all necessary cleanup.
         *
         * @param clan       the clan to delete.
         * @param initiator  the player who initiated deletion (nullable).
         */
        void deleteClan(@NotNull Clan clan, @Nullable Player initiator);

        /**
         * Deletes a clan by its name.
         *
         * @param name the clan name.
         * @return true if deletion succeeded, false otherwise.
         */
        boolean deleteClan(@NotNull String name);

        /**
         * Checks whether a clan with the given name exists.
         *
         * @param name the clan name.
         * @return true if a clan with that name exists.
         */
        boolean clanExists(@NotNull String name);
    }

    /**
     * Validation and naming rules for clans and prefixes.
     * <p>
     * This interface ensures that all clan names, prefixes,
     * and input values meet the plugin’s configuration standards.
     * </p>
     */
    interface Validation {

        /**
         * Checks whether a clan name is allowed for creation.
         *
         * @param player   the player attempting to create the clan.
         * @param clanName the proposed clan name.
         * @return true if valid, false otherwise.
         */
        boolean isAllowedName(@NotNull Player player, @NotNull String clanName);

        /**
         * Checks whether a clan prefix is allowed.
         *
         * @param player the player attempting to set the prefix.
         * @param prefix the prefix to validate.
         * @return true if valid, false otherwise.
         */
        boolean isAllowedPrefix(@NotNull Player player, @NotNull String prefix);

        /**
         * Validates text using a custom regex pattern.
         *
         * @param text  the text to validate.
         * @param regex the regex rule.
         * @return true if the text matches the regex.
         */
        boolean isAllowedRegex(@NotNull String text, @NotNull String regex);
    }

    /**
     * Handles message broadcasting and clan chat communication.
     * <p>
     * Provides convenient methods for sending formatted messages
     * to entire clans or handling player chat within clan channels.
     * </p>
     */
    interface Chat {

        /**
         * Sends a message to all online members of the specified clan.
         *
         * @param clan    the target clan.
         * @param message the message to send.
         */
        void sendMessage(@NotNull Clan clan, @NotNull String message);

        /**
         * Sends a chat message from one player to all members of their clan.
         *
         * @param clan    the target clan.
         * @param sender  the player sending the message.
         * @param message the chat message content.
         */
        void sendChat(@NotNull Clan clan, @NotNull Player sender, @NotNull String message);
    }

    /**
     * Provides access to clan balance and economy management.
     * <p>
     * Supports adding, removing, and retrieving clan balance values,
     * typically synchronized with an external economy plugin (Vault).
     * </p>
     */
    interface Economy {

        /**
         * Adds balance to a clan’s account.
         *
         * @param amount the amount to add.
         * @param clan   the target clan.
         */
        void addBalance(double amount, @NotNull Clan clan);

        /**
         * Deducts balance from a clan’s account.
         *
         * @param amount the amount to deduct.
         * @param clan   the target clan.
         */
        void takeBalance(double amount, @NotNull Clan clan);

        /**
         * Retrieves the current balance of a clan.
         *
         * @param clan the target clan.
         * @return the clan’s current balance.
         */
        double getBalance(@NotNull Clan clan);
    }

    /**
     * Handles glow color customization between clan members.
     * <p>
     * This feature allows visual identification of teammates
     * or allies through colored glows in supported versions.
     * </p>
     */
    interface Colors {

        /**
         * Sets the glow color for a specific clan member.
         *
         * @param clan   the clan.
         * @param member the member to apply the color to.
         * @param color  the desired color.
         */
        void setColor(@NotNull Clan clan, @NotNull Member member, @NotNull Color color);

        /**
         * Sets a glow color for a member visible to multiple others.
         *
         * @param member  the target member.
         * @param members the members who will see the glow.
         * @param color   the color to apply.
         */
        void setColor(@NotNull Member member, @NotNull Set<Member> members, @NotNull Color color);

        /**
         * Sets a glow color between two individual members.
         *
         * @param member the source member.
         * @param target the target member.
         * @param color  the glow color.
         */
        void setColor(@NotNull Member member, @NotNull Member target, @NotNull Color color);
    }

    /**
     * Clan and member lookup utilities.
     * <p>
     * Provides fast, cache-aware access to clan and member
     * information based on UUID, name, or member object.
     * </p>
     */
    interface Lookup {

        /**
         * Checks whether a player with the given UUID is in a clan.
         *
         * @param uuid the player’s UUID.
         * @return true if the player belongs to a clan.
         */
        boolean isInClan(@NotNull UUID uuid);

        /**
         * Checks whether a player with the given UUID (as a string) is in a clan.
         *
         * @param uuidString the player’s UUID as a string.
         * @return true if the player belongs to a clan.
         */
        boolean isInClan(@NotNull String uuidString);

        /**
         * Retrieves a clan by its name.
         *
         * @param name the clan name.
         * @return the {@link Clan} instance or {@code null} if not found.
         */
        @Nullable Clan getClan(@NotNull String name);

        /**
         * Retrieves a clan by a member’s UUID.
         *
         * @param uuid the member’s UUID.
         * @return the {@link Clan} instance or {@code null} if not found.
         */
        @Nullable Clan getClanByMember(@NotNull UUID uuid);

        /**
         * Retrieves a clan by a member’s UUID string.
         *
         * @param uuidString the member’s UUID as a string.
         * @return the {@link Clan} instance or {@code null} if not found.
         */
        @Nullable Clan getClanByMember(@NotNull String uuidString);

        /**
         * Retrieves a clan by a {@link Member} instance.
         *
         * @param member the member.
         * @return the {@link Clan} instance or {@code null} if not found.
         */
        @Nullable Clan getClanByMember(@NotNull Member member);

        /**
         * Returns a formatted last online time for a player.
         *
         * @param uuid the player’s UUID.
         * @return a human-readable last online string.
         */
        @NotNull String getLastOnlineFormatted(@NotNull UUID uuid);

        /**
         * Returns a formatted last online time for a member.
         *
         * @param member the member instance.
         * @return a human-readable last online string.
         */
        @NotNull String getLastOnlineFormatted(@NotNull Member member);
    }
}
