package com.bekvon.bukkit.residence.containers;

public enum ELMessageType {
    ActionBar, TitleBar, ChatBox;

    public static ELMessageType getByName(String name) {
        for (ELMessageType one : ELMessageType.values()) {
            if (one.toString().equalsIgnoreCase(name))
                return one;
        }
        return null;
    }

    public static String getAllValuesAsString() {
        StringBuilder v = new StringBuilder();
        for (ELMessageType one : ELMessageType.values()) {
            if (v.length() > 0)
                v.append(", ");
            v.append(one.toString());
        }
        return v.toString();
    }
}
