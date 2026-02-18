package com.bekvon.bukkit.residence.containers;

import net.Zrips.CMILib.Locale.LC;

public enum EconomyType {

    Vault, @Deprecated
    iConomy, Essentials, @Deprecated
    RealEconomy, CMIEconomy, None;

    public static EconomyType getByName(String string) {
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
                v.append(LC.info_ListSpliter);
            v.append(one.toString());
        }
        return v.toString();
    }
}
