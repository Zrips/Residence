package com.bekvon.bukkit.residence.signsStuff;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.utils.Utils;

public class SignInfo {

    HashMap<String, Signs> AllSigns = new HashMap<String, Signs>();

    public SignInfo() {
    }

    public void setAllSigns(ConcurrentHashMap<String, Signs> AllSigns) {
	this.AllSigns.clear();
	this.AllSigns.putAll(AllSigns);
    }

    public HashMap<String, Signs> GetAllSigns() {
	return this.AllSigns;
    }

    public Signs getResSign(Location loc) {
	if (this.AllSigns.isEmpty())
	    return null;
	String l = Utils.convertLocToStringShort(loc);
	if (l == null)
	    return null;
	return this.AllSigns.get(l);
    }

    public void removeSign(Signs sign) {
	this.AllSigns.remove(Utils.convertLocToStringShort(sign.getLocation()));
    }

    public void addSign(Signs sign) {
	String loc = Utils.convertLocToStringShort(sign.getLocation());
	if (loc == null)
	    return;
	this.AllSigns.put(loc, sign);
    }
}
