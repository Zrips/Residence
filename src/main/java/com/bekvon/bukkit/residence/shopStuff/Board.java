package com.bekvon.bukkit.residence.shopStuff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class Board {

    private Location TopLoc = null;
    private Location BottomLoc = null;

    int startPlace = 0;

    List<Location> Locations = new ArrayList<Location>();
    HashMap<ClaimedResidence, Location> signLocations = new HashMap<ClaimedResidence, Location>();

    public void clearSignLoc() {
        signLocations.clear();
    }

    @Deprecated
    public void addSignLoc(String resName, Location loc) {
        ClaimedResidence res = ClaimedResidence.getByName(resName);
        if (res == null)
            return;
        addSignLoc(res, loc);
    }

    public void addSignLoc(ClaimedResidence res, Location loc) {
        signLocations.put(res, loc);
    }

    public HashMap<ClaimedResidence, Location> getSignLocations() {
        return signLocations;
    }

    public Location getSignLocByName(String resName) {
        ClaimedResidence res = ClaimedResidence.getByName(resName);
        if (res == null)
            return null;
        return signLocations.get(res);
    }

    public String getResNameByLoc(Location location) {
        ClaimedResidence res = getResByLoc(location);
        return res == null ? null : res.getName();
    }

    public ClaimedResidence getResByLoc(Location location) {
        for (Entry<ClaimedResidence, Location> one : signLocations.entrySet()) {
            Location loc = one.getValue();
            if (!loc.getWorld().getName().equalsIgnoreCase(location.getWorld().getName()))
                continue;
            if (loc.getBlockX() != location.getBlockX())
                continue;
            if (loc.getBlockY() != location.getBlockY())
                continue;
            if (loc.getBlockZ() != location.getBlockZ())
                continue;
            return one.getKey();
        }
        return null;
    }

    public List<Location> getLocations() {
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
        this.startPlace = StartPlace;
    }

    public int getStartPlace() {
        return this.startPlace == 0 ? 0 : (startPlace - 1);
    }

    public String getWorld() {
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
