package me.jetby.treexclans.tools;

import lombok.experimental.UtilityClass;

import java.math.BigInteger;
import java.text.DecimalFormat;

@UtilityClass
public class NumberUtils {
    private static final DecimalFormat df = new DecimalFormat("#,##0.##");

    public static String formatWithCommas(long value) {
        return df.format(value);
    }

    public static String formatWithCommas(String value) {
        return df.format(Double.parseDouble(value));
    }

    public static String formatWithCommas(BigInteger value) {
        return df.format(value);
    }

    public static String formatWithCommas(double value) {
        return df.format(value);
    }
}
