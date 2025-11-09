package me.jetby.treexclans.api.gui.requirements;

/**
 * Base interface for GUI requirement checks.
 * <p>
 * A requirement defines a logical condition that must be met
 * before a GUI action or button becomes available to a player.
 * </p>
 *
 * <p>Supported requirement types:</p>
 * <ul>
 *     <li><b>"has permission"</b> — player has the specified permission.</li>
 *     <li><b>"!has permission"</b> — player <u>does not</u> have the specified permission.</li>
 *     <li><b>"string equals"</b> — compares {@code input == output} (case-insensitive, placeholders supported).</li>
 *     <li><b>"!string equals"</b> — compares {@code input != output} (case-insensitive, placeholders supported).</li>
 *     <li><b>"javascript"</b> / <b>"math"</b> — evaluates an expression (>, >=, ==, !=, <=, <), placeholders supported.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <pre>
 * type: "has permission"
 * permission: "treexclans.admin"
 *
 * type: "string equals"
 * input: "%player_name%"
 * output: "JetBy"
 *
 * type: "math"
 * input: "%player_level% >= 10"
 * </pre>
 */
public interface Requirement {

    /**
     * Defines the type of requirement.
     * <p>
     * Available values:
     * </p>
     * <ul>
     *     <li><b>"has permission"</b> — checks if a player has the specified permission.</li>
     *     <li><b>"!has permission"</b> — checks if a player lacks the specified permission.</li>
     *     <li><b>"string equals"</b> — compares {@code input} and {@code output} (case-insensitive, placeholders allowed).</li>
     *     <li><b>"!string equals"</b> — checks if {@code input} and {@code output} are not equal.</li>
     *     <li><b>"javascript"</b> / <b>"math"</b> — evaluates a mathematical or logical expression (>, >=, ==, !=, <=, <).</li>
     * </ul>
     *
     * @return the type of requirement.
     */
    String type();

    /**
     * The permission node to check.
     * <p>
     * Used only with:
     * </p>
     * <ul>
     *     <li><b>"has permission"</b></li>
     *     <li><b>"!has permission"</b></li>
     * </ul>
     *
     * @return the permission string (e.g. {@code treexclans.admin}).
     */
    String permission();

    /**
     * The input value or expression for validation.
     * <p>
     * Used with:
     * </p>
     * <ul>
     *     <li><b>"string equals"</b> / <b>"!string equals"</b> — compared with {@link #output()}.</li>
     *     <li><b>"javascript"</b> / <b>"math"</b> — evaluated expression (e.g. {@code %player_level% >= 10}).</li>
     * </ul>
     *
     * <p>
     * PlaceholderAPI placeholders are supported.
     * </p>
     *
     * @return the input expression.
     */
    String input();

    /**
     * The comparison target for {@link #input()}.
     * <p>
     * Used only with:
     * </p>
     * <ul>
     *     <li><b>"string equals"</b></li>
     *     <li><b>"!string equals"</b></li>
     * </ul>
     *
     * <p>
     * PlaceholderAPI placeholders are supported.
     * </p>
     *
     * @return the output or comparison value.
     */
    String output();
}
