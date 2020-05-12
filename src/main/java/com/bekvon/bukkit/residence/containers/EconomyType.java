package com.bekvon.bukkit.residence.containers;

public enum EconomyType {

    Vault, None;

    public static EconomyType getByName(String string) {
        for (EconomyType one : EconomyType.values()) {
            if (one.toString().equalsIgnoreCase(string))
                return one;
        }
        return null;
    }

    public static String toStringLine() {
        StringBuilder l = new StringBuilder();
        for (EconomyType one : EconomyType.values()) {
            if (l.length() > 0)
                l.append(", ");
            l.append(one.toString());
        }
        return l.toString();
    }
}
