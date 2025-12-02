package com.bekvon.bukkit.residence.containers;

public enum GenMessageType {
    ActionBar, TitleBar, ChatBox;

    public static GenMessageType getByName(String name) {
        for (GenMessageType one : GenMessageType.values()) {
            if (one.toString().equalsIgnoreCase(name))
                return one;
        }
        return null;
    }

    public static String getAllValuesAsString() {
        String v = "";
        for (GenMessageType one : GenMessageType.values()) {
            if (!v.isEmpty())
                v += ", ";
            v += one.toString();
        }
        return v;
    }
}
