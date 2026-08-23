package net.havoccasino.economy;

public enum CurrencyType {
    MONEY,
    RUBIES;

    public static CurrencyType fromString(String raw, CurrencyType fallback) {
        if (raw == null) {
            return fallback;
        }
        String s = raw.trim().toLowerCase();
        switch (s) {
            case "money":
            case "cash":
            case "coins":
            case "coin":
                return MONEY;
            case "rubies":
            case "ruby":
            case "gems":
            case "gem":
                return RUBIES;
            default:
                return fallback;
        }
    }
}
