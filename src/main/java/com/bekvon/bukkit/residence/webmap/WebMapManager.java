package com.bekvon.bukkit.residence.webmap;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.FileHandler.ConfigReader;

public class WebMapManager {

    WebMap webmap = null;
    private final List<WebMap> webmaps = new ArrayList<>();
    MapSettings settings = null;

    private boolean use;

    public WebMapManager() {

    }

    public void loadConfig(ConfigReader c) {

        settings = webmap != null ? webmap.getSettings() : new MapSettings();

        c.addComment("WebMap.Use", "Enables or disable DynMap, Pl3exMap or BlueMap support");
        use = c.get("WebMap.Use", true);
        c.addComment("WebMap.HideByDefault", "When set to true we will hide residence areas by default on WebMap window",
                "Residences can still be enabled throw provided WebMap option on left top side");
        settings.setHideByDefault(c.get("WebMap.HideByDefault", false));
        c.addComment("WebMap.ShowFlags", "Shows or hides residence flags");
        settings.setShowFlags(c.get("WebMap.ShowFlags", true));
        c.addComment("WebMap.ExcludeDefaultFlags", "When enabled default flags will not be included in residence overview");
        settings.setExcludeDefaultFlags(c.get("WebMap.ExcludeDefaultFlags", true));
        c.addComment("WebMap.HideHidden", "If set true, residence with hidden flag set to true will be hidden from WebMap");
        settings.setHideHidden(c.get("WebMap.HideHidden", true));

        c.addComment("WebMap.Layer.SubZoneDepth", "How deep to go into subzones to show");
        settings.setLayerSubZoneDepth(c.get("WebMap.Layer.SubZoneDepth", 2));

        c.addComment("WebMap.Border.Color", "Color of border. Pick color from this page https://rgbcolorpicker.com/",
                "Color code is defined with HEX color code including alpha value, so follow RGBA format",
                "If alpha is not provided then it will default to 0% opacity",
                "00 - 100% opacity, 40 - 75% opacity, 80 - 50% opacity, BF - 25% opacity, FF - 0% opacity");

        settings.setBorderColor(CMIChatColor.getColor(c.get("WebMap.Border.Color", "{#FF000040}")));
        c.addComment("WebMap.Border.Weight", "Border thickness");
        settings.setBorderWeight(c.get("WebMap.Border.Weight", 3));

        settings.setFillColor(CMIChatColor.getColor(c.get("WebMap.Fill.Color", "{#FF000040}")));
        settings.setFillForRent(CMIChatColor.getColor(c.get("WebMap.Fill.ForRent", "{#33cc3340}")));
        settings.setFillRented(CMIChatColor.getColor(c.get("WebMap.Fill.Rented", "{#99ff3340}")));
        settings.setFillForSale(CMIChatColor.getColor(c.get("WebMap.Fill.ForSale", "{#0066ff40}")));
        settings.setFillServerLand(CMIChatColor.getColor(c.get("WebMap.Fill.ServerLand", "{#8F8F8F40}")));

        c.addComment("WebMap.VisibleRegions", "Shows only regions on this list");
        settings.setVisibleRegions(c.get("WebMap.VisibleRegions", new ArrayList<String>()));
        c.addComment("WebMap.HiddenRegions", "Hides region on map even if its not hidden in game");
        settings.setHiddenRegions(c.get("WebMap.HiddenRegions", new ArrayList<String>()));

        c.addComment("WebMap.HiddenPlayerResidences", "List of player names whose residences should be hidden in DynMap, Pl3exMap or BlueMap independent of their settings");
        settings.setHiddenPlayerResidences(c.get("WebMap.HiddenPlayerResidences", new ArrayList<String>()));
    }

    public void activate() {

        if (!use)
            return;

        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        if (dynmap != null) {
            try {
                DynWebMap d = new DynWebMap();
                d.settings = settings;
                d.activate();
                webmaps.add(d);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

// Try Pl3xMap
        Plugin pl3xmap = Bukkit.getPluginManager().getPlugin("Pl3xMap");
        if (pl3xmap != null) {
            try {
                Pl3xWebMap p = new Pl3xWebMap();
                p.settings = settings;
                p.activate();
                webmaps.add(p);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

// Try BlueMap
        Plugin bluemap = Bukkit.getPluginManager().getPlugin("BlueMap");
        if (bluemap != null) {
            try {
                BlueWebMap b = new BlueWebMap();
                b.settings = settings;
                b.activate();
                webmaps.add(b);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        if (!webmaps.isEmpty()) {
            Bukkit.getServer().getPluginManager().registerEvents(
                    new WebMapListeners(Residence.getInstance()),
                    Residence.getInstance()
            );
        }
    }

    public void fireUpdateRemove(final ClaimedResidence res) {
        if (webmaps.isEmpty()) return;
        int depth = res.getSubzoneDeep();
        for (WebMap map : webmaps) {
            map.fireUpdateRemove(res, depth);
        }
    }

    public void fireUpdateAdd(final ClaimedResidence res) {
        if (webmaps.isEmpty()) return;
        int depth = res.getSubzoneDeep();
        for (WebMap map : webmaps) {
            map.fireUpdateAdd(res, depth);
        }
    }

    public void handleResidenceRemove(String resid, ClaimedResidence res) {
        if (webmaps.isEmpty()) return;
        int depth = res.getSubzoneDeep();
        for (WebMap map : webmaps) {
            map.handleResidenceRemove(resid, res, depth);
        }
    }
}
