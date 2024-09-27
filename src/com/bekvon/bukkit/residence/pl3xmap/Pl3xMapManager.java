package com.bekvon.bukkit.residence.pl3xmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.utils.GetTime;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;
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

public class Pl3xMapManager {
    Residence plugin;

    public Pl3xMap api;

    private CMITask scheduler = null;
    HashMap<String, SimpleLayer> providers = new HashMap<String, SimpleLayer>();

    public Pl3xMapManager(Residence plugin) {
        this.plugin = plugin;
    }

    public void fireUpdateAdd(final ClaimedResidence res, final int deep) {
        if (api == null)
            return;
        if (res == null)
            return;

        if (scheduler != null)
            scheduler.cancel();

        scheduler = CMIScheduler.runTaskLater(plugin, () -> handleResidenceAdd(res.getName(), res, deep), 10L);
    }

    public void fireUpdateRemove(final ClaimedResidence res, final int deep) {
        if (api == null)
            return;
        if (res == null)
            return;

        handleResidenceRemove(res.getName(), res, deep);
    }

    private String formatInfoWindow(String resid, ClaimedResidence res, String resName) {
        if (res == null)
            return null;
        if (res.getName() == null)
            return null;
        if (res.getOwner() == null)
            return null;
        String v =
            "<div class=\"regioninfo\"><div class=\"infowindow\"><span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br /> "
                + CMIChatColor.stripColor(plugin.msg(lm.General_Owner, "")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />";

        if (plugin.getConfigManager().Pl3xMapShowFlags) {

            ResidencePermissions residencePermissions = res.getPermissions();
            FlagPermissions gRD = Residence.getInstance().getConfigManager().getGlobalResidenceDefaultFlags();

            StringBuilder flgs = new StringBuilder();
            for (Entry<String, Boolean> one : residencePermissions.getFlags().entrySet()) {
                if (Residence.getInstance().getConfigManager().Pl3xMapExcludeDefaultFlags && gRD.isSet(one.getKey()) && gRD.getFlags().get(one.getKey()).equals(one.getValue())) {
                    continue;
                }
                if (!flgs.toString().isEmpty())
                    flgs.append("<br/>");
                flgs.append(one.getKey() + ": " + one.getValue());
            }

            if (!flgs.toString().isEmpty()) {
                v += CMIChatColor.stripColor(plugin.msg(lm.General_ResidenceFlags, "")) + "<br /><span style=\"font-weight:bold;\">%flags%</span>";
                v = v.replace("%flags%", flgs.toString());
            }
        }

        v += "</div></div>";

        if (plugin.getRentManager().isForRent(res.getName()))
            v = "<div class=\"regioninfo\"><div class=\"infowindow\">"
                + CMIChatColor.stripColor(plugin.msg(lm.Rentable_Land, "")) + "<span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br />"
                + CMIChatColor.stripColor(plugin.msg(lm.General_Owner, "")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
                + CMIChatColor.stripColor(plugin.msg(lm.Residence_RentedBy, "")) + "<span style=\"font-weight:bold;\">%renter%</span><br /> "
                + CMIChatColor.stripColor(plugin.msg(lm.General_LandCost, "")) + "<span style=\"font-weight:bold;\">%rent%</span><br /> "
                + CMIChatColor.stripColor(plugin.msg(lm.Rent_Days, "")) + "<span style=\"font-weight:bold;\">%rentdays%</span><br /> "
                + CMIChatColor.stripColor(plugin.msg(lm.Rentable_AllowRenewing, "")) + "<span style=\"font-weight:bold;\">%renew%</span><br /> "
                + CMIChatColor.stripColor(plugin.msg(lm.Rent_Expire, "")) + "<span style=\"font-weight:bold;\">%expire%</span></div></div>";

        if (plugin.getTransactionManager().isForSale(res.getName()))
            v = "<div class=\"regioninfo\"><div class=\"infowindow\">"
                + CMIChatColor.stripColor(plugin.msg(lm.Economy_LandForSale, " "))
                + "<span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br /> "
                + CMIChatColor.stripColor(plugin.msg(lm.General_Owner, "")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
                + CMIChatColor.stripColor(plugin.msg(lm.Economy_SellAmount, "")) + "<span style=\"font-weight:bold;\">%price%</span><br /></div></div>";

        v = v.replace("%regionname%", resName);
        v = v.replace("%playerowners%", res.getOwner());
        String m = res.getEnterMessage();
        v = v.replace("%entermsg%", (m != null) ? m : "");
        m = res.getLeaveMessage();
        v = v.replace("%leavemsg%", (m != null) ? m : "");

        RentManager rentmgr = plugin.getRentManager();
        TransactionManager transmgr = plugin.getTransactionManager();

        if (rentmgr.isForRent(res.getName())) {
            boolean isrented = rentmgr.isRented(resid);
            v = v.replace("%isrented%", Boolean.toString(isrented));
            String id = "";
            if (isrented)
                id = rentmgr.getRentingPlayer(resid);
            v = v.replace("%renter%", id);

            v = v.replace("%rent%", rentmgr.getCostOfRent(resid) + "");
            v = v.replace("%rentdays%", rentmgr.getRentDays(resid) + "");
            boolean renew = rentmgr.getRentableRepeatable(resid);
            v = v.replace("%renew%", renew + "");
            String expire = "";
            if (isrented) {
                long time = rentmgr.getRentedLand(resid).endTime;
                if (time != 0L)
                    expire = GetTime.getTime(time);
            }
            v = v.replace("%expire%", expire);
        }

        if (transmgr.isForSale(res.getName())) {
            boolean forsale = transmgr.isForSale(resid);
            v = v.replace("%isforsale%", Boolean.toString(transmgr.isForSale(resid)));
            String price = "";
            if (forsale)
                price = Integer.toString(transmgr.getSaleAmount(resid));
            v = v.replace("%price%", price);
        }

        return v;
    }

    private boolean isVisible(String id, String worldname) {
        List<String> visible = plugin.getConfigManager().Pl3xMapVisibleRegions;
        List<String> hidden = plugin.getConfigManager().Pl3xMapHiddenRegions;
        if (visible != null && visible.size() > 0) {
            if ((visible.contains(id) == false) && (visible.contains("world:" + worldname) == false)) {
                return false;
            }
        }
        if (hidden != null && hidden.size() > 0) {
            if (hidden.contains(id) || hidden.contains("world:" + worldname))
                return false;
        }
        return true;
    }

    private int fillColor(String resid) {
        if (plugin.getRentManager().isForRent(resid) && !plugin.getRentManager().isRented(resid))
            return plugin.getConfigManager().Pl3xMapFillForRent;
        else if (plugin.getRentManager().isForRent(resid) && plugin.getRentManager().isRented(resid))
            return plugin.getConfigManager().Pl3xMapFillRented;
        else if (plugin.getTransactionManager().isForSale(resid))
            return plugin.getConfigManager().Pl3xMapFillForSale;
        return plugin.getConfigManager().Pl3xFillColor;
    }

    private void handleResidenceAdd(String resid, ClaimedResidence res, int depth) {

        if (res == null)
            return;

        if (res.getPermissions().has("hidden", false) && plugin.getConfigManager().Pl3xMapHideHidden) {
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
            mWorld.getLayerRegistry().register("Residence", prov);
            providers.put(res.getPermissions().getWorldName(), prov);
        }

        provider = providers.get(res.getPermissions().getWorldName());

        if (provider == null) {
            return;
        }

        for (Entry<String, CuboidArea> oneArea : res.getAreaMap().entrySet()) {

            String id = oneArea.getKey() + "." + resid;

            String resName = res.getName();

            if (res.getAreaMap().size() > 1) {
                resName = res.getName() + " (" + oneArea.getKey() + ")";
            }

            String desc = formatInfoWindow(resid, res, resName);

            if (!isVisible(resid, res.getPermissions().getWorldName()))
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
            fill.setColor(fillColor(resid));
            fill.setType(Type.NONZERO);
            options.setFill(fill);

            Stroke stroke = new Stroke();
            stroke.setColor(plugin.getConfigManager().Pl3xBorderColor);
            stroke.setWeight(plugin.getConfigManager().Pl3xMapBorderWeight);
            options.setStroke(stroke);

            marker.setOptions(options);

            provider.addMarker(marker);

            if (depth <= plugin.getConfigManager().Pl3xMapLayerSubZoneDepth) {
                List<ClaimedResidence> subids = res.getSubzones();
                for (ClaimedResidence one : subids) {
                    try {
                        handleResidenceAdd(one.getName(), one, depth + 1);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

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

            if (depth <= plugin.getConfigManager().Pl3xMapLayerSubZoneDepth + 1) {
                List<ClaimedResidence> subids = res.getSubzones();
                for (ClaimedResidence one : subids) {
                    handleResidenceRemove(one.getName(), one, depth + 1);
                }
            }
        }
    }

    public void activate() {

        CMIMessages.consoleMessage(Residence.getInstance().getPrefix() + " Pl3xMap residence activated!");

        for (Entry<String, ClaimedResidence> one : plugin.getResidenceManager().getResidences().entrySet()) {
            fireUpdateAdd(one.getValue(), one.getValue().getSubzoneDeep());
            try {
                handleResidenceAdd(one.getValue().getName(), one.getValue(), one.getValue().getSubzoneDeep());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
