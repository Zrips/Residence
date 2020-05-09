package com.bekvon.bukkit.residence.economy;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketBuyInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.text.help.PageInfo;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

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
            plugin.msg(player, lm.Economy_MarketDisabled);
            return false;
        }
        if (!econ.canAfford(player.getName(), chargeamount)) {
            plugin.msg(player, lm.Economy_NotEnoughMoney);
            return false;
        }
        econ.subtract(player.getName(), chargeamount);
        try {
            if (chargeamount != 0D)
                plugin.msg(player, lm.Economy_MoneyCharged, plugin.getEconomyManager().format(chargeamount), econ.getName());
        } catch (Exception e) {
        }
        return true;
    }

    public boolean giveEconomyMoney(Player player, double amount) {
        if (player == null)
            return false;
        if (amount == 0)
            return true;
        EconomyInterface econ = plugin.getEconomyManager();
        if (econ == null) {
            plugin.msg(player, lm.Economy_MarketDisabled);
            return false;
        }

        econ.add(player.getName(), amount);
        plugin.msg(player, lm.Economy_MoneyAdded, plugin.getEconomyManager().format(amount), econ.getName());
        return true;
    }

    @Deprecated
    public boolean giveEconomyMoney(String playerName, double amount) {
        if (playerName == null)
            return false;
        if (amount == 0)
            return true;
        EconomyInterface econ = plugin.getEconomyManager();
        if (econ == null) {
            return false;
        }
        econ.add(playerName, amount);
        return true;
    }

    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        putForSale(res, player, amount, resadmin);
    }

    public void putForSale(ClaimedResidence res, Player player, int amount, boolean resadmin) {

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        if (plugin.getConfigManager().enabledRentSystem()) {
            if (!resadmin) {
                if (res.isForRent()) {
                    plugin.msg(player, lm.Economy_RentSellFail);
                    return;
                }
                if (res.isSubzoneForRent()) {
                    plugin.msg(player, lm.Economy_SubzoneRentSellFail);
                    return;
                }
                if (res.isParentForRent()) {
                    plugin.msg(player, lm.Economy_ParentRentSellFail);
                    return;
                }
            }
        }

        if (!plugin.getConfigManager().isSellSubzone()) {
            if (res.isSubzone()) {
                plugin.msg(player, lm.Economy_SubzoneSellFail);
                return;
            }
        }

        if (!resadmin) {
            if (!plugin.getConfigManager().enableEconomy() || plugin.getEconomyManager() == null) {
                plugin.msg(player, lm.Economy_MarketDisabled);
                return;
            }

            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

            if (!resadmin && !(rPlayer.getGroup().canSellLand() || ResPerm.sell.hasPermission(player))) {
                plugin.msg(player, lm.General_NoPermission);
                return;
            }
            if (amount <= 0) {
                plugin.msg(player, lm.Invalid_Amount);
                return;
            }
        }

        if (!res.isOwner(player) && !resadmin) {
            plugin.msg(player, lm.General_NoPermission);
            return;
        }
        if (sellAmount.contains(res)) {
            plugin.msg(player, lm.Economy_AlreadySellFail);
            return;
        }
        res.setSellPrice(amount);
        sellAmount.add(res);
        plugin.getSignUtil().CheckSign(res);
        plugin.msg(player, lm.Residence_ForSale, res.getName(), amount);
    }

    @Override
    public boolean putForSale(String areaname, int amount) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return putForSale(res, amount);
    }

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
    public void buyPlot(String areaname, Player player, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        buyPlot(res, player, resadmin);
    }

    public void buyPlot(ClaimedResidence res, Player player, boolean resadmin) {
        if (res == null || !res.isForSell()) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (!resadmin) {
            if (!plugin.getConfigManager().enableEconomy() || plugin.getEconomyManager() == null) {
                plugin.msg(player, lm.Economy_MarketDisabled);
                return;
            }
            boolean canbuy = group.canBuyLand() || ResPerm.buy.hasPermission(player);
            if (!canbuy && !resadmin) {
                plugin.msg(player, lm.General_NoPermission);
                return;
            }
        }

        if (res.getPermissions().getOwner().equals(player.getName())) {
            plugin.msg(player, lm.Economy_OwnerBuyFail);
            return;
        }
        if (plugin.getResidenceManager().getOwnedZoneCount(player.getName()) >= rPlayer.getMaxRes() && !resadmin) {
            plugin.msg(player, lm.Residence_TooMany);
            return;
        }
        Server serv = plugin.getServ();
        int amount = res.getSellPrice();

        if (!resadmin && !group.buyLandIgnoreLimits()) {
            CuboidArea[] areas = res.getAreaArray();
            for (CuboidArea thisarea : areas) {
                if (!res.isSubzone() && !res.isSmallerThanMax(player, thisarea, resadmin) || res.isSubzone() && !res.isSmallerThanMaxSubzone(player, thisarea,
                        resadmin)) {
                    plugin.msg(player, lm.Residence_BuyTooBig);
                    return;
                }
            }
        }

        EconomyInterface econ = plugin.getEconomyManager();
        if (econ == null) {
            plugin.msg(player, lm.Economy_MarketDisabled);
            return;
        }

        String buyerName = player.getName();
        String sellerName = res.getPermissions().getOwner();
        Player sellerNameFix = plugin.getServ().getPlayer(sellerName);
        if (sellerNameFix != null) {
            sellerName = sellerNameFix.getName();
        }

        if (econ.canAfford(buyerName, amount)) {
            if (!econ.transfer(buyerName, sellerName, amount)) {
                player.sendMessage(ChatColor.RED + "Error, could not transfer " + amount + " from " + buyerName + " to " + sellerName);
                return;
            }
            res.getPermissions().setOwner(player, true);
            res.getPermissions().applyDefaultFlags();
            removeFromSale(res);

            if (plugin.getConfigManager().isRemoveLwcOnBuy())
                plugin.getResidenceManager().removeLwcFromResidence(player, res);

            plugin.getSignUtil().CheckSign(res);

            Visualizer v = new Visualizer(player);
            v.setAreas(res);
            plugin.getSelectionManager().showBounds(player, v);

            plugin.msg(player, lm.Economy_MoneyCharged, plugin.getEconomyManager().format(amount), econ.getName());
            plugin.msg(player, lm.Residence_Bought, res.getResidenceName());
            Player seller = serv.getPlayer(sellerName);
            if (seller != null && seller.isOnline()) {
                seller.sendMessage(plugin.msg(lm.Residence_Buy, player.getName(), res.getResidenceName()));
                seller.sendMessage(plugin.msg(lm.Economy_MoneyCredit, plugin.getEconomyManager().format(amount), econ.getName()));
            }
        } else {
            plugin.msg(player, lm.Economy_NotEnoughMoney);
        }

    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        removeFromSale(player, res, resadmin);
    }

    public void removeFromSale(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            plugin.msg(player, lm.Invalid_Area);
            return;
        }

        if (!res.isForSell()) {
            plugin.msg(player, lm.Residence_NotForSale);
            return;
        }
        if (res.isOwner(player) || resadmin) {
            removeFromSale(res);
            plugin.getSignUtil().CheckSign(res);
            plugin.msg(player, lm.Residence_StopSelling);
        } else {
            plugin.msg(player, lm.General_NoPermission);
        }
    }

    @Override
    public void removeFromSale(String areaname) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        removeFromSale(res);
    }

    public void removeFromSale(ClaimedResidence res) {
        if (res == null)
            return;
        sellAmount.remove(res);
        plugin.getSignUtil().removeSign(res);
    }

    @Override
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

        plugin.msg(player, lm.General_Separator);
        plugin.msg(player, lm.Area_Name, res.getName());
        plugin.msg(player, lm.Economy_SellAmount, res.getSellPrice());
        if (plugin.getConfigManager().useLeases()) {
            String etime = plugin.getLeaseManager().getExpireTime(res);
            if (etime != null) {
                plugin.msg(player, lm.Economy_LeaseExpire, etime);
            }
        }
        plugin.msg(player, lm.General_Separator);
        return true;
    }

    public void printForSaleResidences(Player player, int page) {
        List<ClaimedResidence> toRemove = new ArrayList<ClaimedResidence>();
        plugin.msg(player, lm.Economy_LandForSale);
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
            plugin.msg(player, lm.Economy_SellList, pi.getPositionForOutput(position), res.getName(), res.getSellPrice(), res.getOwner());
        }

        for (ClaimedResidence one : toRemove) {
            sellAmount.remove(one);
        }
        plugin.getInfoPageManager().ShowPagination(player, pi, "/res market list sell");
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