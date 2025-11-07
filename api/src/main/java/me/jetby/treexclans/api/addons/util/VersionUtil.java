package me.jetby.treexclans.api.addons.util;

/**
 * Утилита проверки версий.
 * <p>Поддерживает стандартные сравнения и диапазоны версий ({@code >=}, {@code <=}, {@code ^}, {@code .x}).</p>
 */
public final class VersionUtil {

    private VersionUtil() {}

    /**
     * Проверяет, удовлетворяет ли версия {@code have} требованию {@code require}.
     *
     * @param have    фактическая версия (например, {@code 1.5.2})
     * @param require требуемая версия (например, {@code >=1.4}, {@code ^1.5}, {@code 1.x})
     * @return true, если версия соответствует требованию
     */
    public static boolean isSatisfied(String have, String require) {
        if (require == null || require.isBlank()) return true;
        require = require.trim();

        if (require.endsWith(".x")) {
            String prefix = require.substring(0, require.length() - 2);
            return have.startsWith(prefix + ".") || have.equals(prefix);
        }

        if (require.startsWith("^")) {
            String base = require.substring(1);
            String major = split(base, 0);
            return split(have, 0).equals(major) && compare(have, base) >= 0;
        }

        if (require.startsWith(">=")) return compare(have, require.substring(2)) >= 0;
        if (require.startsWith("<=")) return compare(have, require.substring(2)) <= 0;
        if (require.startsWith(">"))  return compare(have, require.substring(1)) > 0;
        if (require.startsWith("<"))  return compare(have, require.substring(1)) < 0;
        if (require.startsWith("==")) return compare(have, require.substring(2)) == 0;

        return compare(have, require) == 0;
    }

    private static int compare(String a, String b) {
        String[] A = a.split("\\.");
        String[] B = b.split("\\.");
        for (int i = 0; i < Math.max(A.length, B.length); i++) {
            int ai = i < A.length ? parse(A[i]) : 0;
            int bi = i < B.length ? parse(B[i]) : 0;
            if (ai != bi) return Integer.compare(ai, bi);
        }
        return 0;
    }

    private static int parse(String s) {
        int n = 0;
        for (int i = 0; i < s.length() && Character.isDigit(s.charAt(i)); i++) {
            n = n * 10 + (s.charAt(i) - '0');
        }
        return n;
    }

    private static String split(String s, int idx) {
        String[] parts = s.split("\\.");
        return idx < parts.length ? parts[idx] : "0";
    }
}
