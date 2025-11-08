package me.jetby.treexclans.api.addons.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents metadata that defines a TreexClans addon.
 * <p>
 * This annotation provides essential information about the addon,
 * such as its ID, version, authors, dependencies, and load order.
 * It must be applied to the main class of every addon.
 * </p>
 *
 * <pre>{@code
 * @ClanAddon(
 *     id = "clan-shop",
 *     version = "2.2.0",
 *     authors = {"JetBy"},
 *     description = "Clan shop addon.",
 *     depends = {
 *         @Dependency(id = "core-api"),
 *         @Dependency(id = "database")
 *     },
 *     softDepends = {
 *         @Dependency(id = "economy")
 *     },
 *     loadBefore = {"promo-system"},
 *     loadAfter = {"core-api"}
 * )
 * public final class ClanShopAddon extends TreexAddon { ... }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClanAddon {

    /**
     * Unique identifier of the addon.
     * <p>
     * Must be lowercase, alphanumeric, and distinct
     * across all loaded addons.
     * </p>
     */
    String id();

    /**
     * Current version of the addon.
     * <p>
     * Used to track compatibility and update status.
     * Example: {@code "1.0.3"} or {@code "2.2.0-beta"}.
     * </p>
     */
    String version();

    /**
     * List of addon authors or maintainers.
     * <p>
     * Helps identify contributors for support
     * or credit display in logs and UI.
     * </p>
     */
    String[] authors() default {};

    /**
     * Short, human-readable description.
     * <p>
     * Appears in addon listings and may be used
     * by the in-game or web documentation system.
     * </p>
     */
    String description() default "";

    /**
     * Hard (required) dependencies.
     * <p>
     * The addon will not be enabled if any of
     * these dependencies are missing or invalid.
     * </p>
     */
    Dependency[] depends() default {};

    /**
     * Soft (optional) dependencies.
     * <p>
     * The addon can load without these, but if
     * present, integration features may be enabled.
     * </p>
     */
    Dependency[] softDepends() default {};

    /**
     * Defines addons that should load after this one.
     * <p>
     * Useful when this addon provides base functionality
     * that others extend or rely on.
     * </p>
     */
    String[] loadBefore() default {};

    /**
     * Defines addons that should load before this one.
     * <p>
     * Ensures that dependencies or shared services are
     * initialized first, preventing startup conflicts.
     * </p>
     */
    String[] loadAfter() default {};
}
