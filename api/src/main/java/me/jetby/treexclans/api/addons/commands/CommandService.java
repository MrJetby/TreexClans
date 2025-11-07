package me.jetby.treexclans.api.addons.commands;

import me.jetby.treexclans.api.command.Subcommand;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents a unified service responsible for managing and handling registered subcommands
 * within a specific command context (e.g. clan or admin commands).
 * <p>
 * Implementations of this interface should ensure thread safety when registering
 * or unregistering commands, as these operations may be invoked dynamically at runtime
 * (e.g. by addons or external modules).
 */
public interface CommandService {

    /**
     * Returns an immutable view of all currently registered subcommands.
     *
     * @return a map containing all registered subcommands mapped by their names.
     */
    @NotNull
    Map<String, Subcommand> getCommands();

    /**
     * Registers a new subcommand for this command service.
     * <p>
     * If a subcommand with the same name already exists, it should be replaced or rejected,
     * depending on the implementation policy.
     *
     * @param name       the command name (identifier).
     * @param subcommand the subcommand instance to register.
     * @throws IllegalArgumentException if the name or subcommand is null or invalid.
     */
    void registerCommand(@NotNull String name, @NotNull Subcommand subcommand);

    /**
     * Unregisters a subcommand by its name.
     * <p>
     * If no subcommand is found under that name, this operation should safely do nothing.
     *
     * @param name the name of the command to remove.
     */
    void unregisterCommand(@NotNull String name);

    /**
     * Defines available types of command categories managed by the service.
     */
    enum CommandType {
        /**
         * Represents player-level commands, e.g. "/clan create", "/clan join", etc.
         */
        CLAN,

        /**
         * Represents administrator-level commands, e.g. "/clan admin reload", "/clan admin give", etc.
         */
        ADMIN
    }
}
