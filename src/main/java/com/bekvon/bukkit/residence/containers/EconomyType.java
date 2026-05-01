package com.bekvon.bukkit.residence.containers;

import net.Zrips.CMILib.Locale.LC;

public enum EconomyType {

    Vault,
    Essentials,
    CMIEconomy,
    Auto;

    public static EconomyType getByName(String string) {
        if (string.equalsIgnoreCase("none"))
			return Auto;
        for (EconomyType one : EconomyType.values()) {
            if (one.toString().equalsIgnoreCase(string))
                return one;
        }
        return null;
    }

    public static String toStringLine() {
        StringBuilder v = new StringBuilder();
        for (EconomyType one : EconomyType.values()) {
            if (!v.toString().isEmpty())
                v.append(LC.info_ListSpliter.getLocale());
            v.append(one.toString());
        }
        return v.toString();
    }
}
