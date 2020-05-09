package com.bekvon.bukkit.residence.shopStuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;

public class Board {

    private Location TopLoc = null;
    private Location BottomLoc = null;

    int StartPlace = 0;

    List<Location> Locations = new ArrayList<Location>();
    HashMap<String, Location> SignLocations = new HashMap<String, Location>();

    public void clearSignLoc() {
	SignLocations.clear();
    }

    public void addSignLoc(String resName, Location loc) {
	SignLocations.put(resName, loc);
    }

    public HashMap<String, Location> getSignLocations() {
	return SignLocations;
    }

    public Location getSignLocByName(String resName) {
	return SignLocations.get(resName);
    }

    public String getResNameByLoc(Location location) {
	for (Entry<String, Location> One : SignLocations.entrySet()) {
	    Location loc = One.getValue();
	    if (!loc.getWorld().getName().equalsIgnoreCase(location.getWorld().getName()))
		continue;
	    if (loc.getBlockX() != location.getBlockX())
		continue;
	    if (loc.getBlockY() != location.getBlockY())
		continue;
	    if (loc.getBlockZ() != location.getBlockZ())
		continue;
	    return One.getKey();
	}
	return null;
    }

    public List<Location> GetLocations() {
	Locations.clear();

	if (TopLoc == null || BottomLoc == null)
	    return null;

	if (TopLoc.getWorld() == null)
	    return null;

	int xLength = TopLoc.getBlockX() - BottomLoc.getBlockX();
	int yLength = TopLoc.getBlockY() - BottomLoc.getBlockY();
	int zLength = TopLoc.getBlockZ() - BottomLoc.getBlockZ();

	if (xLength < 0)
	    xLength = xLength * -1;
	if (zLength < 0)
	    zLength = zLength * -1;

	for (int y = 0; y <= yLength; y++) {
	    for (int x = 0; x <= xLength; x++) {
		for (int z = 0; z <= zLength; z++) {

		    int tempx = 0;
		    int tempz = 0;

		    if (TopLoc.getBlockX() > BottomLoc.getBlockX())
			tempx = TopLoc.getBlockX() - x;
		    else
			tempx = TopLoc.getBlockX() + x;

		    if (TopLoc.getBlockZ() > BottomLoc.getBlockZ())
			tempz = TopLoc.getBlockZ() - z;
		    else
			tempz = TopLoc.getBlockZ() + z;

		    Locations.add(new Location(TopLoc.getWorld(), tempx, TopLoc.getBlockY() - y, tempz));
		}
	    }
	}

	return this.Locations;
    }

    public void setStartPlace(int StartPlace) {
	this.StartPlace = StartPlace;
    }

    public int GetStartPlace() {
	return this.StartPlace == 0 ? 0 : (StartPlace - 1);
    }

    public String GetWorld() {
	return this.TopLoc.getWorld().getName();
    }

    public Location getTopLoc() {
	return TopLoc;
    }

    public void setTopLoc(Location topLoc) {
	TopLoc = topLoc;
    }

    public Location getBottomLoc() {
	return BottomLoc;
    }

    public void setBottomLoc(Location bottomLoc) {
	BottomLoc = bottomLoc;
    }
}
