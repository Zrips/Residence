package com.bekvon.bukkit.residence.webmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.Colors.CMIChatColor;

public class DynWebMap extends WebMap {

    public DynmapAPI api;
    MarkerAPI markerapi;
    MarkerSet set;
    private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();

    private void addStyle(ClaimedResidence res, AreaMarker m) {
        MapSettings as = new MapSettings();
        CMIChatColor fc = fillColor(res);
        m.setLineStyle(getSettings().getBorderWeight(), getAlpha(getSettings().getBorderColor()), getRGB(getSettings().getBorderColor()));
        m.setFillStyle(getAlpha(fc), getRGB(fc));
        m.setRangeY(as.getY(), as.getY());
    }

    private int getRGB(CMIChatColor color) {
        return color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
    }

    private double getAlpha(CMIChatColor color) {
        try {
            return color.getAlpha() / 255.0;
        } catch (Exception e) {
        }
        return 0.25;
    }

    @Override
    protected void handleResidenceAdd(ClaimedResidence res, int depth) {

        if (res == null)
            return;

        if (getSettings().getHiddenPlayerResidences().contains(res.getOwner().toLowerCase()))
            return;

        boolean hidden = res.getPermissions().has(Flags.hidden, false);
        if (hidden && getSettings().isHideHidden()) {
            fireUpdateRemove(res, depth);
            return;
        }

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {

            String id = oneArea.getKey() + "." + res.getName();

            String name = res.getName();
            double[] x = new double[4];
            double[] z = new double[4];

            String resName = res.getName();

            if (res.getAreaMap().size() > 1) {
                resName = res.getName() + " (" + oneArea.getKey() + ")";
            }

            String desc = formatInfoWindow(res, resName);

            if (!isVisible(res.getName(), res.getWorldName()))
                return;

            Location l0 = oneArea.getValue().getLowLocation();
            Location l1 = oneArea.getValue().getHighLocation();

            x[0] = l0.getX();
            z[0] = l0.getZ();
            x[1] = l0.getX();
            z[1] = l1.getZ() + 1.0;
            x[2] = l1.getX() + 1.0;
            z[2] = l1.getZ() + 1.0;
            x[3] = l1.getX() + 1.0;
            z[3] = l0.getZ();

            AreaMarker marker = null;

            if (resareas.containsKey(id)) {
                marker = resareas.get(id);
                resareas.remove(id);
                marker.deleteMarker();
            }

            marker = set.createAreaMarker(id, name, true, res.getWorldName(), x, z, true);
            if (marker == null)
                return;

            marker.setRangeY(l1.getY(), l0.getY());

            marker.setDescription(desc);
            addStyle(res, marker);
            resareas.put(id, marker);

            if (depth <= getSettings().getLayerSubZoneDepth()) {
                List<ClaimedResidence> subids = res.getSubzones();
                for (ClaimedResidence one : subids) {
                    handleResidenceAdd(one, depth + 1);
                }
            }
        }
    }

    @Override
    public void handleResidenceRemove(String resid, ClaimedResidence res, int depth) {

        if (markerapi == null)
            return;

        if (resid == null)
            return;

        if (res == null)
            return;

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {
            String id = oneArea.getKey() + "." + resid;
            if (resareas.containsKey(id)) {
                AreaMarker marker = resareas.remove(id);
                marker.deleteMarker();
            }
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
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmap == null)
            return;
        api = (DynmapAPI) dynmap;

        try {
            markerapi = api.getMarkerAPI();
        } catch (Exception e) {
        }
        if (markerapi == null) {
            lm.consoleMessage("Error loading dynmap marker API!");
            return;
        }

        if (set != null) {
            set.deleteMarkerSet();
            set = null;
        }
        set = markerapi.getMarkerSet("residence.markerset");
        if (set == null)
            set = markerapi.createMarkerSet("residence.markerset", "Residence", null, false);
        else
            set.setMarkerSetLabel("Residence");

        if (set == null) {
            lm.consoleMessage("Error creating marker set");
            return;
        }
        set.setLayerPriority(1);
        set.setHideByDefault(getSettings().isHideByDefault());

        lm.consoleMessage("DynMap residence activated!");

        postActivated();
    }

    @Override
    public void onDisable() {
        if (set != null)
            set.deleteMarkerSet();
    }
}
