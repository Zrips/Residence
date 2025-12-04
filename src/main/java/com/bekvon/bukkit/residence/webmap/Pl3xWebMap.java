package com.bekvon.bukkit.residence.webmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.Layer;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Rectangle;
import net.pl3x.map.core.markers.option.Fill;
import net.pl3x.map.core.markers.option.Fill.Type;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Stroke;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.core.registry.Registry;

public class Pl3xWebMap extends WebMap {

    public Pl3xMap api;

    HashMap<String, SimpleLayer> providers = new HashMap<String, SimpleLayer>();

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

        net.pl3x.map.core.world.World mWorld = api.getWorldRegistry().get(world.getName());

        if (mWorld == null) {
            return;
        }

        Registry<Layer> registry = mWorld.getLayerRegistry();
        SimpleLayer provider = providers.get(res.getPermissions().getWorldName());
        if (registry.has("Residence")) {
            provider = (SimpleLayer) registry.get("Residence");
            providers.put(res.getPermissions().getWorldName(), provider);
        }

        if (provider == null) {
            Pl3xMapLayer prov = new Pl3xMapLayer(mWorld);
            prov.setDefaultHidden(getSettings().isHideByDefault());

            mWorld.getLayerRegistry().register("Residence", prov);
            providers.put(res.getPermissions().getWorldName(), prov);
        }

        provider = providers.get(res.getPermissions().getWorldName());

        if (provider == null) {
            return;
        }

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {

            String id = oneArea.getKey() + "." + res.getName();

            String resName = res.getName();

            if (res.getAreaMap().size() > 1) {
                resName = res.getName() + " (" + oneArea.getKey() + ")";
            }

            String desc = formatInfoWindow(res, resName);

            if (!isVisible(res.getName(), res.getPermissions().getWorldName()))
                return;

            Location l0 = oneArea.getValue().getLowLocation();
            Location l1 = oneArea.getValue().getHighLocation();

            Point p1 = Point.of(l0.getX(), l0.getZ());
            Point p2 = Point.of(l1.getX() + 1, l1.getZ() + 1);

            Rectangle marker = Marker.rectangle(id, p1, p2);

            Options options = new Options();

            Tooltip tooltip = new Tooltip();

            tooltip.setContent(desc);

            options.setTooltip(tooltip);

            Fill fill = new Fill();
            fill.setColor(fillColor(res).getARGB());
            fill.setType(Type.NONZERO);
            options.setFill(fill);

            Stroke stroke = new Stroke();
            stroke.setColor(getSettings().getBorderColor().getARGB());
            stroke.setWeight(getSettings().getBorderWeight());
            options.setStroke(stroke);

            marker.setOptions(options);

            provider.addMarker(marker);

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

        net.pl3x.map.core.world.World mWorld = api.getWorldRegistry().get(world.getName());

        if (mWorld == null)
            return;

        Registry<Layer> registry = mWorld.getLayerRegistry();
        SimpleLayer provider = providers.get(res.getPermissions().getWorldName());
        if (registry.has("Residence")) {
            provider = (SimpleLayer) registry.get("Residence");
            providers.put(res.getPermissions().getWorldName(), provider);
        }

        if (provider == null) {
            return;
        }

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {
            String id = oneArea.getKey() + "." + resid;
            provider.removeMarker(id);
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

        api = Pl3xMap.api();
        lm.consoleMessage("Pl3xMap residence activated!");
        
        postActivated();
    }

    @Override
    public void onDisable() {

        
    }
}
