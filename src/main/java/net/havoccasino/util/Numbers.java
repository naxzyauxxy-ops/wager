package net.havoccasino.util;

public final class Numbers {

    private Numbers() {
    }

    /** Parses a strictly positive amount, rounded to 2 dp, or null if invalid. */
    public static Double parsePositive(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(raw.replace(",", ""));
            if (value <= 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return Math.round(value * 100.0) / 100.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Formats a multiplier compactly (e.g. 12, 1.5). */
    public static String trim(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }
}
