package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayManager;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayType;

import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class Visualizer {
    private Player player;
    private long start;

    private HashMap<CuboidDisplayType, List<CMIBlockWorldArea>> areas = new HashMap<CuboidDisplayType, List<CMIBlockWorldArea>>();
    private HashMap<CuboidDisplayType, List<Location>> locationsPrimary = new HashMap<CuboidDisplayType, List<Location>>();
    private HashMap<CuboidDisplayType, List<Location>> locationsSecondary = new HashMap<CuboidDisplayType, List<Location>>();

    private CMITask mainSchduler = null;
    private boolean once = false;
    private int starting = 0;
    private int currentSkip = 0;

    private Location loc = null;

    public Visualizer(Player player) {
        this.player = player;
        this.start = System.currentTimeMillis();
    }

    public void cancelAll() {
        if (mainSchduler != null) {
            mainSchduler.cancel();
            mainSchduler = null;
        }
    }

    public void clearRecords() {
        cancelAll();
        
        areas.clear();
        locationsPrimary.clear();
        locationsSecondary.clear();
    }

    public boolean isSameLoc() {
        if (loc == null)
            return false;
        if (loc.getWorld() != player.getWorld())
            return false;

        if (!areas.getOrDefault(CuboidDisplayType.ERROR, new ArrayList<CMIBlockWorldArea>()).isEmpty() && locationsPrimary.getOrDefault(CuboidDisplayType.ERROR, new ArrayList<Location>()).isEmpty())
            return false;

        if (loc.distance(player.getLocation()) > 1)
            return false;

        return true;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Deprecated
    public List<CuboidArea> getAreas() {
        List<CuboidArea> areas = new ArrayList<CuboidArea>();

        List<CMIBlockWorldArea> temp = this.areas.get(CuboidDisplayType.SELECTION);

        if (temp == null)
            return areas;

        for (CMIBlockWorldArea area : temp) {
            areas.add(new CuboidArea(area.getLowPoint().toLocation(area.getWorld().getWorld()), area.getHighPoint().toLocation(area.getWorld().getWorld())));
        }

        return areas;
    }

    public List<CMIBlockWorldArea> getAreas(CuboidDisplayType type) {
        return this.areas.getOrDefault(type, new ArrayList<CMIBlockWorldArea>());
    }

    @Deprecated
    public void setAreas(ClaimedResidence res) {
        if (res == null)
            return;

        setAreas(new ArrayList<CuboidArea>(Arrays.asList(res.getAreaArray())));
    }

    @Deprecated
    public void setAreas(CuboidArea[] areas) {
        setAreas(new ArrayList<CuboidArea>(Arrays.asList(areas)));
    }

    @Deprecated
    public void setAreas(CuboidArea area) {
        setAreas(new ArrayList<CuboidArea>(Arrays.asList(area)));
    }

    @Deprecated
    public void setAreas(ArrayList<CuboidArea> areas) {

        List<CMIBlockWorldArea> temp = this.areas.computeIfAbsent(CuboidDisplayType.SELECTION, k -> new ArrayList<CMIBlockWorldArea>());
        temp.clear();

        for (CuboidArea area : areas) {
            temp.add(new CMIBlockWorldArea(area.getLowLocation(), area.getHighLocation()));
        }
    }

    public void setAreas(CuboidDisplayType type, CMIBlockWorldArea area) {
        this.areas.put(type, new ArrayList<CMIBlockWorldArea>(Arrays.asList(area)));
    }

    public void setAreas(CuboidDisplayType type, ArrayList<CMIBlockWorldArea> areas) {
        this.areas.put(type, areas);
    }

    @Deprecated
    public List<CuboidArea> getErrorAreas() {

        List<CuboidArea> errorAreas = new ArrayList<CuboidArea>();

        List<CMIBlockWorldArea> temp = this.areas.computeIfAbsent(CuboidDisplayType.ERROR, k -> new ArrayList<CMIBlockWorldArea>());

        if (temp == null)
            return errorAreas;

        for (CMIBlockWorldArea area : temp) {
            errorAreas.add(new CuboidArea(area.getLowPoint().toLocation(area.getWorld().getWorld()), area.getHighPoint().toLocation(area.getWorld().getWorld())));
        }

        return errorAreas;
    }

    @Deprecated
    public void setErrorAreas(ClaimedResidence res) {
        if (res == null)
            return;

        setErrorAreas(new ArrayList<CuboidArea>(Arrays.asList(res.getAreaArray())));
    }

    @Deprecated
    public void setErrorAreas(CuboidArea[] errorAreas) {

        setErrorAreas(new ArrayList<CuboidArea>(Arrays.asList(errorAreas)));
    }

    @Deprecated
    public void setErrorAreas(ArrayList<CuboidArea> errorAreas) {
        List<CMIBlockWorldArea> temp = this.areas.computeIfAbsent(CuboidDisplayType.ERROR, k -> new ArrayList<CMIBlockWorldArea>());
        temp.clear();

        for (CuboidArea area : errorAreas) {
            temp.add(new CMIBlockWorldArea(area.getLowLocation(), area.getHighLocation()));
        }
    }

    @Deprecated
    public void setErrorAreas(CuboidArea errorArea) {
        setErrorAreas(new ArrayList<CuboidArea>(Arrays.asList(errorArea)));
    }

    public CMITask getScheduler() {
        return mainSchduler;
    }

    public void setScheduler(CMITask scheduler) {
        this.mainSchduler = scheduler;
    }

    public boolean isOnce() {
        return once;
    }

    public void setOnce(boolean once) {
        this.once = once;
    }

    public List<Location> getPrimaryLocations(CuboidDisplayType type) {
        return locationsPrimary.get(type);
    }

    @Deprecated
    public List<Location> getLocations() {
        return getPrimaryLocations(CuboidDisplayType.SELECTION);
    }

    public void setPrimaryLocations(CuboidDisplayType type, List<Location> locations) {
        this.locationsPrimary.put(type, locations);
    }

    @Deprecated
    public void setLocations(List<Location> locations) {
        setPrimaryLocations(CuboidDisplayType.SELECTION, locations);
    }

    public List<Location> getSecondaryLocations(CuboidDisplayType type) {
        return locationsSecondary.get(type);
    }

    public void setSecondaryLocations(CuboidDisplayType type, List<Location> locations) {
        this.locationsSecondary.put(type, locations);
    }

    @Deprecated
    public List<Location> getLocations2() {
        return getSecondaryLocations(CuboidDisplayType.SELECTION);
    }

    @Deprecated
    public void setLocations2(List<Location> locations) {
        setSecondaryLocations(CuboidDisplayType.SELECTION, locations);
    }

    @Deprecated
    public List<Location> getErrorLocations() {
        return getPrimaryLocations(CuboidDisplayType.ERROR);
    }

    @Deprecated
    public void setErrorLocations(List<Location> errorLocations) {
        setPrimaryLocations(CuboidDisplayType.ERROR, errorLocations);
    }

    @Deprecated
    public List<Location> getErrorLocations2() {
        return getSecondaryLocations(CuboidDisplayType.ERROR);
    }

    @Deprecated
    public void setErrorLocations2(List<Location> errorLocations) {
        setSecondaryLocations(CuboidDisplayType.ERROR, errorLocations);
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public int getStarting() {
        return starting;
    }

    public void setStarting(int starting) {
        this.starting = starting;
    }

    public int getCurrentSkip() {
        return currentSkip;
    }

    public void setCurrentSkip(int currentSkip) {
        this.currentSkip = currentSkip;
    }

    public void addCurrentSkip() {
        this.currentSkip++;
    }
}
