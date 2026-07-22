package com.bekvon.bukkit.residence.webmap;

import java.awt.Color;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.Colors.CMIChatColor;
import xyz.jpenilla.squaremap.api.BukkitAdapter;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.LayerProvider;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.Registry;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;
import xyz.jpenilla.squaremap.api.marker.Rectangle;

public class SquareWebMap extends WebMap {

    private static final Key LAYER_KEY = Key.of("residence.residences");
    private static final String LAYER_LABEL = "Residence";

    private final Map<MapWorld, SimpleLayerProvider> providers = new HashMap<MapWorld, SimpleLayerProvider>();
    private Squaremap api;

    @Override
    protected void handleResidenceAdd(ClaimedResidence res, int depth) {
        if (res == null || api == null)
            return;

        String owner = res.getOwner();
        if ((owner != null && getSettings().getHiddenPlayerResidences().contains(owner.toLowerCase()))
                || (res.getPermissions().has(Flags.hidden, false) && getSettings().isHideHidden())
                || !isVisible(res.getName(), res.getPermissions().getWorldName())) {
            handleResidenceRemove(res.getName(), res, depth);
            return;
        }

        World world = Bukkit.getWorld(res.getPermissions().getWorldName());
        if (world == null)
            return;

        SimpleLayerProvider provider = getProvider(world);
        if (provider == null)
            return;

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {
            String resName = res.getName();
            if (res.getAreaMap().size() > 1)
                resName = res.getName() + " (" + oneArea.getKey() + ")";

            Location low = oneArea.getValue().getLowLocation();
            Location high = oneArea.getValue().getHighLocation();

            Rectangle marker = Marker.rectangle(
                    Point.of(low.getBlockX(), low.getBlockZ()),
                    Point.of(high.getBlockX() + 1D, high.getBlockZ() + 1D)
            );

            CMIChatColor border = getSettings().getBorderColor();
            CMIChatColor fill = fillColor(res);

            MarkerOptions.Builder options = MarkerOptions.builder()
                    .strokeColor(toColor(border))
                    .strokeWeight(getSettings().getBorderWeight())
                    .strokeOpacity(toOpacity(border))
                    .fillColor(toColor(fill))
                    .fillOpacity(toOpacity(fill))
                    .clickTooltip(formatInfoWindow(res, resName));

            marker.markerOptions(options);
            provider.addMarker(markerKey(oneArea.getKey(), res.getName()), marker);
        }

        if (depth <= getSettings().getLayerSubZoneDepth()) {
            List<ClaimedResidence> subzones = res.getSubzones();
            for (ClaimedResidence subzone : subzones) {
                try {
                    handleResidenceAdd(subzone, depth + 1);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void handleResidenceRemove(String resid, ClaimedResidence res, int depth) {
        if (resid == null || res == null || api == null)
            return;

        World world = Bukkit.getWorld(res.getPermissions().getWorldName());
        if (world == null)
            return;

        SimpleLayerProvider provider = getProvider(world);
        if (provider == null)
            return;

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {
            provider.removeMarker(markerKey(oneArea.getKey(), resid));
        }

        if (depth <= getSettings().getLayerSubZoneDepth() + 1) {
            List<ClaimedResidence> subzones = res.getSubzones();
            for (ClaimedResidence subzone : subzones)
                handleResidenceRemove(subzone.getName(), subzone, depth + 1);
        }
    }

    private SimpleLayerProvider getProvider(World world) {
        MapWorld mapWorld = getMapWorld(world);
        if (mapWorld == null)
            return null;

        SimpleLayerProvider provider = providers.get(mapWorld);
        if (provider != null)
            return provider;

        Registry<LayerProvider> registry = mapWorld.layerRegistry();
        if (registry.hasEntry(LAYER_KEY))
            registry.unregister(LAYER_KEY);

        provider = SimpleLayerProvider.builder(LAYER_LABEL)
                .defaultHidden(getSettings().isHideByDefault())
                .build();
        registry.register(LAYER_KEY, provider);

        providers.put(mapWorld, provider);
        return provider;
    }

    private MapWorld getMapWorld(World world) {
        for (MapWorld mapWorld : api.mapWorlds()) {
            try {
                if (world.equals(BukkitAdapter.bukkitWorld(mapWorld)))
                    return mapWorld;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static Key markerKey(String areaName, String residenceName) {
        String key = "residence." + areaName + "." + residenceName;
        try {
            return Key.of(key);
        } catch (IllegalArgumentException ignored) {
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    (areaName + "\0" + residenceName).getBytes(StandardCharsets.UTF_8)
            );
            return Key.of("residence." + encoded);
        }
    }

    private static Color toColor(CMIChatColor color) {
        int argb = color.getARGB();
        return new Color((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF);
    }

    private static double toOpacity(CMIChatColor color) {
        return ((color.getARGB() >>> 24) & 0xFF) / 255D;
    }

    @Override
    public void activate() {
        api = SquaremapProvider.get();
        lm.consoleMessage("squaremap residence activated!");
        postActivated();
    }

    @Override
    public void onDisable() {
        if (api == null)
            return;

        for (Entry<MapWorld, SimpleLayerProvider> entry : providers.entrySet()) {
            Registry<LayerProvider> registry = entry.getKey().layerRegistry();
            if (registry.hasEntry(LAYER_KEY) && registry.get(LAYER_KEY) == entry.getValue())
                registry.unregister(LAYER_KEY);
        }
        providers.clear();
    }
}
