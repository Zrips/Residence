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
	String v = "";
	for (ELMessageType one : ELMessageType.values()) {
	    if (!v.isEmpty())
		v += ", ";
	    v += one.toString();
	}
	return v;
    }
}
