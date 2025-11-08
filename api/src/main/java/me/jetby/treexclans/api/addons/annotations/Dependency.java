package me.jetby.treexclans.api.addons.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a dependency reference for a TreexClans addon.
 * <p>
 * Each dependency specifies another addon that must (or may)
 * be loaded before the current one. This ensures proper
 * initialization order and integration between addons.
 * </p>
 *
 * <pre>{@code
 * @ClanAddon(
 *     id = "example-addon",
 *     depends = {
 *         @Dependency(id = "core-api"),
 *         @Dependency(id = "database")
 *     }
 * )
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Dependency {

    /**
     * Identifier of the target addon dependency.
     * <p>
     * Must exactly match the {@code id} value of another
     * addon annotated with {@link ClanAddon}.
     * </p>
     */
    String id();
}
