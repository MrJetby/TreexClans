package me.jetby.treexclans.gui.requirements;

/**
 * Базовый интерфейс требования для GUI.
 *
 * <p>Поддерживаемые типы требований:</p>
 * <ul>
 *     <li><b>"has permission"</b> — игрок имеет указанное разрешение.</li>
 *     <li><b>"!has permission"</b> — игрок <u>не</u> имеет указанного разрешения.</li>
 *     <li><b>"string equals"</b> — input == output (игнор регистра, поддерживаются плейсхолдеры).</li>
 *     <li><b>"!string equals"</b> — input != output (игнор регистра, поддерживаются плейсхолдеры).</li>
 *     <li><b>"javascript"</b> / <b>"math"</b> — проверка выражений (> >= == != <= <), плейсхолдеры поддерживаются.</li>
 * </ul>
 *
 * <p>Примеры:</p>
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
     * Тип проверки.
     *
     * <p>Возможные значения:</p>
     * <ul>
     *     <li><b>"has permission"</b> — проверяет наличие разрешения.</li>
     *     <li><b>"!has permission"</b> — проверяет отсутствие разрешения.</li>
     *     <li><b>"string equals"</b> — сравнивает input и output (регистр игнорируется, плейсхолдеры разрешены).</li>
     *     <li><b>"!string equals"</b> — сравнивает input и output на неравенство.</li>
     *     <li><b>"javascript"</b> / <b>"math"</b> — выполняет математическое выражение (> >= == != <= <).</li>
     * </ul>
     *
     * @return тип требования
     */
    String type();

    /**
     * Разрешение, используемое при проверке прав.
     *
     * <p>Применяется только для типов:</p>
     * <ul>
     *     <li><b>"has permission"</b></li>
     *     <li><b>"!has permission"</b></li>
     * </ul>
     *
     * @return строка разрешения (например, {@code treexclans.admin})
     */
    String permission();

    /**
     * Входное значение для проверки.
     *
     * <p>Используется при следующих типах:</p>
     * <ul>
     *     <li><b>"string equals"</b> / <b>"!string equals"</b> — сравнение с {@link #output()}</li>
     *     <li><b>"javascript"</b> / <b>"math"</b> — выражение вида {@code %player_level% >= 10}</li>
     * </ul>
     *
     * <p>Поддерживаются плейсхолдеры PlaceholderAPI.</p>
     *
     * @return входное выражение
     */
    String input();

    /**
     * Выходное значение для сравнения.
     *
     * <p>Используется только при типах:</p>
     * <ul>
     *     <li><b>"string equals"</b></li>
     *     <li><b>"!string equals"</b></li>
     * </ul>
     *
     * <p>Поддерживаются плейсхолдеры PlaceholderAPI.</p>
     *
     * @return значение для сравнения
     */
    String output();
}
