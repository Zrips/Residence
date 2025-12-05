package com.bekvon.bukkit.residence.webmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;

public class BlueWebMap extends WebMap {

    public BlueMapAPI api;
    private HashMap<World, MarkerSet> markerSetMap = new HashMap<>();
    private String label = "Residences";

    @Override
    protected void handleResidenceAdd(ClaimedResidence res, int depth) {

        if (res == null)
            return;

        if (getSettings().getHiddenPlayerResidences().contains(res.getOwner().toLowerCase()))
            return;

        if (res.getPermissions().has(Flags.hidden, false) && getSettings().isHideHidden()) {
            fireUpdateRemove(res, depth);
            return;
        }

        World world = Bukkit.getWorld(res.getPermissions().getWorldName());

        if (world == null)
            return;

        Optional<BlueMapWorld> wo = api.getWorld(world);

        if (!wo.isPresent())
            return;

        BlueMapWorld bmw = wo.get();

        MarkerSet markerSet = markerSetMap.computeIfAbsent(world, k -> MarkerSet.builder().label(label).defaultHidden(false).build());

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {

            String id = oneArea.getKey() + "." + res.getName();

            String resName = res.getName();

            if (res.getAreaMap().size() > 1) {
                resName = res.getName() + " (" + oneArea.getKey() + ")";
            }

            String desc = formatInfoWindow(res, resName);

            if (!isVisible(res.getName(), res.getPermissions().getWorldName()))
                return;

            Location l1 = oneArea.getValue().getLowLocation();
            Location l2 = oneArea.getValue().getHighLocation();

            Shape rect = Shape.createRect(l1.getBlockX(), l1.getBlockZ(), l2.getBlockX(), l2.getBlockZ());
            Color color = new Color(fillColor(res).getARGB());

            ExtrudeMarker shape = ExtrudeMarker.builder()
                    .label(resName)
                    .shape(rect, l1.getBlockY(), l2.getBlockY())
                    .detail(desc)
                    .fillColor(color)
                    .lineColor(new Color(getSettings().getBorderColor().getARGB()))
                    .lineWidth(getSettings().getBorderWeight())
                    .depthTestEnabled(false)
                    .build();

            markerSet.getMarkers().put(id, shape);

            for (BlueMapMap map : bmw.getMaps()) {
                map.getMarkerSets().put(label, markerSet);
            }

            if (depth <= getSettings().getLayerSubZoneDepth()) {
                List<ClaimedResidence> subids = res.getSubzones();
                for (ClaimedResidence one : subids) {
                    try {
                        handleResidenceAdd(one, depth + 1);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void handleResidenceRemove(String resid, ClaimedResidence res, int depth) {

        if (resid == null)
            return;
        if (res == null)
            return;

        World world = Bukkit.getWorld(res.getPermissions().getWorldName());

        MarkerSet markerSet = markerSetMap.computeIfAbsent(world, k -> MarkerSet.builder().label(label).defaultHidden(false).build());

        if (markerSet == null)
            return;

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {
            String id = oneArea.getKey() + "." + resid;
            markerSet.remove(id);
            if (depth <= getSettings().getLayerSubZoneDepth() + 1) {
                List<ClaimedResidence> subids = res.getSubzones();
                for (ClaimedResidence one : subids) {
                    handleResidenceRemove(one.getName(), one, depth + 1);
                }
            }
        }
    }

    @Override
    public void activate() {

        BlueMapAPI.onEnable(a -> {
            api = a;
            lm.consoleMessage("BlueMap residence activated!");
            postActivated();
        });
    }

    @Override
    public void onDisable() {

    }
}
