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
        String l = "";
        for (EconomyType one : EconomyType.values()) {
            if (!l.isEmpty())
                l += ", ";
            l += one.toString();
        }
        return l;
    }
}
