package com.bekvon.bukkit.residence.containers;

import net.Zrips.CMILib.Locale.LC;

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
        StringBuilder v = new StringBuilder();
        for (GenMessageType one : GenMessageType.values()) {
            if (!v.toString().isEmpty())
                v.append(LC.info_ListSpliter);
            v.append(one.toString());
        }
        return v.toString();
    }
}
