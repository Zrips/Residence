package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MinimizeFlags {

    private Map<String, Boolean> flags = new HashMap<String, Boolean>();

    private int id = 0;

    public MinimizeFlags(int id, Map<String, Boolean> flags) {
	this.id = id;
	this.flags = flags;
    }

    public boolean same(Map<String, Boolean> flags) {
	if (flags.size() != this.flags.size())
	    return false;
	for (Entry<String, Boolean> one : flags.entrySet()) {
	    if (!this.flags.containsKey(one.getKey()))
		return false;
	    if (this.flags.get(one.getKey()) != one.getValue())
		return false;
	}
	return true;
    }

    public boolean add(Map<String, Boolean> flags) {
	if (!same(flags))
	    return false;
	this.flags = flags;
	return true;
    }

    public int getId() {
	return id;
    }

    public Map<String, Boolean> getFlags() {
	return flags;
    }

}
