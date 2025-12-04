package com.bekvon.bukkit.residence.webmap;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.utils.GetTime;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public abstract class WebMap {

    MapSettings settings = new MapSettings();

    public WebMap() {
        Bukkit.getServer().getPluginManager().registerEvents(new WebMapListeners(Residence.getInstance()), Residence.getInstance());
    }

    public void fireUpdateAdd(final ClaimedResidence res, final int deep) {

        if (res == null)
            return;

        CMIScheduler.runTaskLater(Residence.getInstance(), () -> handleResidenceAdd(res, deep), 10L);
    }

    public void fireUpdateRemove(final ClaimedResidence res, final int deep) {
        if (res == null)
            return;

        handleResidenceRemove(res.getName(), res, deep);
    }

    protected String formatInfoWindow(ClaimedResidence res, String resName) {
        if (res == null)
            return null;
        if (res.getName() == null)
            return null;
        if (res.getOwner() == null)
            return null;
        String v = "<div class=\"regioninfo\"><div class=\"infowindow\"><span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br /> "
                + CMIChatColor.stripColor(lm.General_Owner.getMessage("")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />";

        if (getSettings().isShowFlags()) {

            ResidencePermissions residencePermissions = res.getPermissions();
            FlagPermissions gRD = Residence.getInstance().getConfigManager().getGlobalResidenceDefaultFlags();

            StringBuilder flgs = new StringBuilder();
            for (Entry<String, Boolean> one : residencePermissions.getFlags().entrySet()) {
                if (getSettings().isExcludeDefaultFlags() && gRD.isSet(one.getKey()) && gRD.getFlags().get(one.getKey()).equals(one.getValue())) {
                    continue;
                }
                if (!flgs.toString().isEmpty())
                    flgs.append("<br/>");
                flgs.append(one.getKey() + ": " + one.getValue());
            }

            if (!flgs.toString().isEmpty()) {
                v += CMIChatColor.stripColor(lm.General_ResidenceFlags.getMessage("")) + "<br /><span style=\"font-weight:bold;\">%flags%</span>";
                v = v.replace("%flags%", flgs.toString());
            }
        }

        v += "</div></div>";

        if (Residence.getInstance().getRentManager().isForRent(res))
            v = "<div class=\"regioninfo\"><div class=\"infowindow\">"
                    + CMIChatColor.stripColor(lm.Rentable_Land.getMessage("")) + "<span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br />"
                    + CMIChatColor.stripColor(lm.General_Owner.getMessage("")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
                    + CMIChatColor.stripColor(lm.Residence_RentedBy.getMessage("")) + "<span style=\"font-weight:bold;\">%renter%</span><br /> "
                    + CMIChatColor.stripColor(lm.General_LandCost.getMessage("")) + "<span style=\"font-weight:bold;\">%rent%</span><br /> "
                    + CMIChatColor.stripColor(lm.Rent_Days.getMessage("")) + "<span style=\"font-weight:bold;\">%rentdays%</span><br /> "
                    + CMIChatColor.stripColor(lm.Rentable_AllowRenewing.getMessage("")) + "<span style=\"font-weight:bold;\">%renew%</span><br /> "
                    + CMIChatColor.stripColor(lm.Rent_Expire.getMessage("")) + "<span style=\"font-weight:bold;\">%expire%</span></div></div>";

        if (Residence.getInstance().getTransactionManager().isForSale(res))
            v = "<div class=\"regioninfo\"><div class=\"infowindow\">"
                    + CMIChatColor.stripColor(lm.Economy_LandForSale.getMessage(" "))
                    + "<span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br /> "
                    + CMIChatColor.stripColor(lm.General_Owner.getMessage("")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
                    + CMIChatColor.stripColor(lm.Economy_SellAmount.getMessage("")) + "<span style=\"font-weight:bold;\">%price%</span><br /></div></div>";

        v = v.replace("%regionname%", resName);
        v = v.replace("%playerowners%", res.getOwner());
        String m = res.getEnterMessage();
        v = v.replace("%entermsg%", (m != null) ? m : "");
        m = res.getLeaveMessage();
        v = v.replace("%leavemsg%", (m != null) ? m : "");

        RentManager rentmgr = Residence.getInstance().getRentManager();
        TransactionManager transmgr = Residence.getInstance().getTransactionManager();

        if (rentmgr.isForRent(res)) {
            boolean isrented = rentmgr.isRented(res);
            v = v.replace("%isrented%", Boolean.toString(isrented));
            String id = "";
            if (isrented)
                id = rentmgr.getRentingPlayer(res);
            v = v.replace("%renter%", id);

            v = v.replace("%rent%", rentmgr.getCostOfRent(res) + "");
            v = v.replace("%rentdays%", rentmgr.getRentDays(res) + "");
            boolean renew = rentmgr.getRentableRepeatable(res);
            v = v.replace("%renew%", renew + "");
            String expire = "";
            if (isrented) {
                long time = rentmgr.getRentedLand(res).endTime;
                if (time != 0L)
                    expire = GetTime.getTime(time);
            }
            v = v.replace("%expire%", expire);
        }

        if (transmgr.isForSale(res)) {
            boolean forsale = transmgr.isForSale(res);
            v = v.replace("%isforsale%", Boolean.toString(transmgr.isForSale(res)));
            String price = "";
            if (forsale)
                price = Integer.toString(transmgr.getSaleAmount(res));
            v = v.replace("%price%", price);
        }

        return v;
    }

    protected boolean isVisible(String id, String worldname) {
        List<String> visible = getSettings().getVisibleRegions();
        List<String> hidden = getSettings().getHiddenRegions();
        if (visible != null && !visible.isEmpty() && !visible.contains(id) && !visible.contains("world:" + worldname))
            return false;

        if (hidden != null && !hidden.isEmpty() && (hidden.contains(id) || hidden.contains("world:" + worldname)))
            return false;

        return true;
    }

    protected CMIChatColor fillColor(ClaimedResidence resid) {
        if (Residence.getInstance().getRentManager().isForRent(resid) && !Residence.getInstance().getRentManager().isRented(resid))
            return getSettings().getFillForRent();
        else if (Residence.getInstance().getRentManager().isForRent(resid) && Residence.getInstance().getRentManager().isRented(resid))
            return getSettings().getFillRented();
        else if (Residence.getInstance().getTransactionManager().isForSale(resid))
            return getSettings().getFillForSale();
        else if (resid.isServerLand())
            return getSettings().getFillServerLand();
        return getSettings().getFillColor();
    }

    protected abstract void handleResidenceAdd(ClaimedResidence res, int depth);

    public abstract void handleResidenceRemove(String resid, ClaimedResidence res, int depth);

    public abstract void activate();

    public abstract void onDisable();

    public MapSettings getSettings() {
        return settings;
    }

    protected void postActivated() {
        for (Entry<String, ClaimedResidence> one : Residence.getInstance().getResidenceManager().getResidences().entrySet()) {
            try {
                handleResidenceAdd(one.getValue(), one.getValue().getSubzoneDeep());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
