package com.bekvon.bukkit.residence.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketBuyInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listeners.ResidenceLWCListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.Container.PageInfo;

public class TransactionManager implements MarketBuyInterface {
    private Set<ClaimedResidence> sellAmount;
    private Residence plugin;

    public TransactionManager(Residence plugin) {
        this.plugin = plugin;
        sellAmount = new HashSet<ClaimedResidence>();
    }

    public boolean chargeEconomyMoney(Player player, double chargeamount) {
        EconomyInterface econ = plugin.getEconomyManager();
        if (econ == null) {
            lm.Economy_MarketDisabled.sendMessage(player);
            return false;
        }
        if (!econ.canAfford(player, chargeamount)) {
            lm.Economy_NotEnoughMoney.sendMessage(player);
            return false;
        }
        econ.subtract(player, chargeamount);
        try {
            if (chargeamount != 0D)
                lm.Economy_MoneyCharged.sendMessage(player, plugin.getEconomyManager().format(chargeamount), econ.getName());
        } catch (Exception e) {
        }
        return true;
    }

    public boolean giveEconomyMoney(Player player, double amount) {
        if (player == null)
            return false;

        if (giveEconomyMoney(player.getUniqueId(), amount)) {

            EconomyInterface econ = plugin.getEconomyManager();
            lm.Economy_MoneyAdded.sendMessage(player, plugin.getEconomyManager().format(amount), econ.getName());
            return true;
        }

        lm.Economy_MarketDisabled.sendMessage(player);
        return false;
    }

    @Deprecated
    public boolean giveEconomyMoney(String playerName, double amount) {
        return giveEconomyMoney(ResidencePlayer.getUUID(playerName), amount);
    }

    public boolean giveEconomyMoney(UUID uuid, double amount) {
        if (uuid == null)
            return false;

        if (amount == 0)
            return true;
        EconomyInterface econ = plugin.getEconomyManager();
        if (econ == null)
            return false;
        econ.add(uuid, amount);
        return true;
    }

    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
        ClaimedResidence res = ClaimedResidence.getByName(areaname);
        putForSale(res, player, amount, resadmin);
    }

    public void putForSale(ClaimedResidence res, Player player, int amount, boolean resadmin) {

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (plugin.getConfigManager().enabledRentSystem()) {
            if (!resadmin) {
                if (res.isForRent()) {
                    lm.Economy_RentSellFail.sendMessage(player);
                    return;
                }
                if (res.isSubzoneForRent()) {
                    lm.Economy_SubzoneRentSellFail.sendMessage(player);
                    return;
                }
                if (res.isParentForRent()) {
                    lm.Economy_ParentRentSellFail.sendMessage(player);
                    return;
                }
            }
        }

        if (!plugin.getConfigManager().isSellSubzone()) {
            if (res.isSubzone()) {
                lm.Economy_SubzoneSellFail.sendMessage(player);
                return;
            }
        }

        if (!resadmin) {
            if (!plugin.getConfigManager().enableEconomy() || plugin.getEconomyManager() == null) {
                lm.Economy_MarketDisabled.sendMessage(player);
                return;
            }

            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

            if (!resadmin && !(rPlayer.getGroup().canSellLand() || ResPerm.sell.hasPermission(player))) {
                lm.General_NoPermission.sendMessage(player);
                return;
            }
            if (amount < 0) {
                lm.Invalid_Amount.sendMessage(player);
                return;
            }
        }

        if (!res.isOwner(player) && !resadmin) {
            lm.General_NoPermission.sendMessage(player);
            return;
        }
        if (sellAmount.contains(res)) {
            lm.Economy_AlreadySellFail.sendMessage(player);
            return;
        }
        res.setSellPrice(amount);
        sellAmount.add(res);
        plugin.getSignUtil().checkSign(res);
        lm.Residence_ForSale.sendMessage(player, res.getName(), amount);
    }

    @Override
    @Deprecated
    public boolean putForSale(String areaname, int amount) {
        return putForSale(ClaimedResidence.getByName(areaname), amount);
    }

    @Override
    public boolean putForSale(ClaimedResidence res, int amount) {
        if (res == null)
            return false;

        if (plugin.getConfigManager().enabledRentSystem() && (res.isForRent() || res.isSubzoneForRent() || res.isParentForRent()))
            return false;

        if (sellAmount.contains(res))
            return false;

        res.setSellPrice(amount);
        sellAmount.add(res);
        return true;
    }

    @Override
    @Deprecated
    public void buyPlot(String areaname, Player player, boolean resadmin) {
        buyPlot(ClaimedResidence.getByName(areaname), player, resadmin);
    }

    public void buyPlot(ClaimedResidence res, Player player, boolean resadmin) {
        if (res == null || !res.isForSell()) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        ResidencePlayer rPlayer = ResidencePlayer.get(player);
        PermissionGroup group = rPlayer.getGroup();
        if (!resadmin) {
            if (!plugin.getConfigManager().enableEconomy() || plugin.getEconomyManager() == null) {
                lm.Economy_MarketDisabled.sendMessage(player);
                return;
            }
            boolean canbuy = group.canBuyLand() || ResPerm.buy.hasPermission(player);
            if (!canbuy && !resadmin) {
                lm.General_NoPermission.sendMessage(player);
                return;
            }
        }

        if (res.getPermissions().getOwner().equals(player.getName())) {
            lm.Economy_OwnerBuyFail.sendMessage(player);
            return;
        }
        if (plugin.getResidenceManager().getOwnedZoneCount(player.getUniqueId()) >= rPlayer.getMaxRes() && !resadmin && !group.buyLandIgnoreLimits()) {
            lm.Residence_TooMany.sendMessage(player);
            return;
        }
        Server serv = plugin.getServ();
        int amount = res.getSellPrice();

        if (!resadmin && !group.buyLandIgnoreLimits()) {
            CuboidArea[] areas = res.getAreaArray();
            for (CuboidArea thisarea : areas) {
                if (!res.isSubzone() && !res.isSmallerThanMax(player, thisarea, resadmin) || res.isSubzone() && !res.isSmallerThanMaxSubzone(player, thisarea,
                        resadmin)) {
                    lm.Residence_BuyTooBig.sendMessage(player);
                    return;
                }
            }
        }

        EconomyInterface econ = plugin.getEconomyManager();
        if (econ == null) {
            lm.Economy_MarketDisabled.sendMessage(player);
            return;
        }

        String buyerName = player.getName();
        String sellerName = res.getPermissions().getOwner();
        Player sellerNameFix = plugin.getServ().getPlayer(sellerName);
        if (sellerNameFix != null) {
            sellerName = sellerNameFix.getName();
        }

        if (econ.canAfford(res.getPermissions().getOwnerUUID(), amount)) {
            if (!econ.transfer(player.getUniqueId(), res.getPermissions().getOwnerUUID(), amount)) {
                player.sendMessage(ChatColor.RED + "Error, could not transfer " + amount + " from " + buyerName + " to " + sellerName);
                return;
            }
            res.getPermissions().setOwner(player, true);
            res.getPermissions().applyDefaultFlags();
            removeFromSale(res);

            if (plugin.getConfigManager().isRemoveLwcOnBuy() && plugin.isLwcPresent())
                ResidenceLWCListener.removeLwcFromResidence(player, res);

            plugin.getSignUtil().checkSign(res);

            Visualizer v = new Visualizer(player);
            v.setAreas(res);
            plugin.getSelectionManager().showBounds(player, v);

            lm.Economy_MoneyCharged.sendMessage(player, plugin.getEconomyManager().format(amount), econ.getName());
            lm.Residence_Bought.sendMessage(player, res.getResidenceName());
            Player seller = serv.getPlayer(sellerName);
            if (seller != null && seller.isOnline()) {
                lm.Residence_Buy.sendMessage(seller, player.getName(), res.getResidenceName());
                lm.Economy_MoneyCredit.sendMessage(seller, plugin.getEconomyManager().format(amount), econ.getName());
            }
        } else {
            lm.Economy_NotEnoughMoney.sendMessage(player);
        }

    }

    @Deprecated
    public void removeFromSale(Player player, String areaname, boolean resadmin) {
        removeFromSale(player, ClaimedResidence.getByName(areaname), resadmin);
    }

    public void removeFromSale(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            lm.Invalid_Area.sendMessage(player);
            return;
        }

        if (!res.isForSell()) {
            lm.Residence_NotForSale.sendMessage(player);
            return;
        }
        if (res.isOwner(player) || resadmin) {
            removeFromSale(res);
            plugin.getSignUtil().checkSign(res);
            lm.Residence_StopSelling.sendMessage(player);
        } else {
            lm.General_NoPermission.sendMessage(player);
        }
    }

    @Override
    @Deprecated
    public void removeFromSale(String areaname) {
        removeFromSale(ClaimedResidence.getByName(areaname));
    }

    public void removeFromSale(ClaimedResidence res) {
        removeFromSale(res, true);
    }

    public void removeFromSale(ClaimedResidence res, boolean removeSigns) {
        if (res == null)
            return;
        sellAmount.remove(res);
        if (removeSigns)
            plugin.getSignUtil().removeSign(res);
    }

    @Override
    @Deprecated
    public boolean isForSale(String areaname) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return isForSale(res);
    }

    public boolean isForSale(ClaimedResidence res) {
        if (res == null)
            return false;
        return sellAmount.contains(res);
    }

    public boolean viewSaleInfo(String areaname, Player player) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return viewSaleInfo(res, player);
    }

    public boolean viewSaleInfo(ClaimedResidence res, Player player) {

        if (res == null || !res.isForSell()) {
            return false;
        }

        if (!sellAmount.contains(res))
            return false;

        lm.General_Separator.sendMessage(player);
        lm.Area_Name.sendMessage(player, res.getName());
        lm.Economy_SellAmount.sendMessage(player, res.getSellPrice());
        if (plugin.getConfigManager().useLeases()) {
            String etime = plugin.getLeaseManager().getExpireTime(res);
            if (etime != null) {
                lm.Economy_LeaseExpire.sendMessage(player, etime);
            }
        }
        lm.General_Separator.sendMessage(player);
        return true;
    }

    public void printForSaleResidences(Player player, int page) {
        List<ClaimedResidence> toRemove = new ArrayList<ClaimedResidence>();
        lm.Economy_LandForSale.sendMessage(player);
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(ChatColor.GREEN);

        PageInfo pi = new PageInfo(10, sellAmount.size(), page);

        int position = -1;
        for (ClaimedResidence res : sellAmount) {
            position++;
            if (position > pi.getEnd())
                break;
            if (!pi.isInRange(position))
                continue;

            if (res == null) {
                toRemove.add(res);
                continue;
            }
            lm.Economy_SellList.sendMessage(player, pi.getPositionForOutput(position), res.getName(), res.getSellPrice(), res.getOwner());
        }

        for (ClaimedResidence one : toRemove) {
            sellAmount.remove(one);
        }
        pi.autoPagination(player, "/res market list sell");
    }

    public void clearSales() {
        for (ClaimedResidence res : sellAmount) {
            if (res == null)
                continue;
            res.setSellPrice(-1);
        }
        sellAmount.clear();
        System.out.println("[Residence] - ReInit land selling.");
    }

    @Override
    public int getSaleAmount(String areaname) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return getSaleAmount(res);
    }

    public int getSaleAmount(ClaimedResidence res) {
        if (res == null)
            return -1;
        return res.getSellPrice();
    }

    public void load(Map<String, Integer> root) {
        if (root == null)
            return;

        for (Entry<String, Integer> one : root.entrySet()) {
            ClaimedResidence res = plugin.getResidenceManager().getByName(one.getKey());
            if (res == null)
                continue;
            res.setSellPrice(one.getValue());
            sellAmount.add(res);
        }
    }

    @Override
    public Map<String, Integer> getBuyableResidences() {
        Map<String, Integer> list = new HashMap<String, Integer>();
        for (ClaimedResidence res : sellAmount) {
            if (res == null)
                continue;
            list.put(res.getName(), res.getSellPrice());
        }
        return list;
    }

    public Map<String, Integer> save() {
        return getBuyableResidences();
    }
}