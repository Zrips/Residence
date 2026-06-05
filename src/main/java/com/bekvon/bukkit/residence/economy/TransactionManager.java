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

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.PageInfo;

/**
 * Manages land buying, selling, and economy transactions for residences.
 * Implements {@link MarketBuyInterface} to provide marketplace functionality.
 * Get the instance via {@code ResidenceApi.getMarketBuyManager()}.
 */
public class TransactionManager implements MarketBuyInterface {
    private Set<ClaimedResidence> sellAmount;
    private Residence plugin;

    /**
     * Creates a new TransactionManager instance.
     *
     * @param plugin Residence plugin instance
     */
    public TransactionManager(Residence plugin) {
        this.plugin = plugin;
        sellAmount = new HashSet<ClaimedResidence>();
    }

    /**
     * Charges a player a specified amount of money.
     *
     * @param player       Player to charge
     * @param chargeamount Amount to charge
     * @return true if the charge was successful, false otherwise
     */
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

    /**
     * Gives money to an online player.
     *
     * @param player Player to receive money
     * @param amount Amount to give
     * @return true if the transfer was successful, false otherwise
     */
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

    /**
     * Gives money to a player by name.
     *
     * @param playerName Player name
     * @param amount     Amount to give
     * @return true if the transfer was successful, false otherwise
     * @deprecated Use {@link #giveEconomyMoney(UUID, double)} instead
     */
    @Deprecated
    public boolean giveEconomyMoney(String playerName, double amount) {
        return giveEconomyMoney(ResidencePlayer.getUUID(playerName), amount);
    }

    /**
     * Gives money to a player by UUID.
     *
     * @param uuid   Player UUID
     * @param amount Amount to give
     * @return true if the transfer was successful, false otherwise
     */
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

    /**
     * Puts a residence up for sale by name.
     *
     * @param areaname Residence name
     * @param player   Player listing the sale
     * @param amount   Sale price
     * @param resadmin Whether in admin mode
     */
    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
        ClaimedResidence res = ClaimedResidence.getByName(areaname);
        putForSale(res, player, amount, resadmin);
    }

    /**
     * Puts a residence up for sale.
     *
     * @param res      Residence to sell
     * @param player   Player listing the sale
     * @param amount   Sale price
     * @param resadmin Whether in admin mode
     */
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

    /**
     * Puts a residence up for sale by name (API method).
     *
     * @param areaname Residence name
     * @param amount   Sale price
     * @return true if the residence was put up for sale, false otherwise
     * @deprecated Use {@link #putForSale(ClaimedResidence, int)} instead
     */
    @Override
    @Deprecated
    public boolean putForSale(String areaname, int amount) {
        return putForSale(ClaimedResidence.getByName(areaname), amount);
    }

    /**
     * Puts a residence up for sale (API method).
     *
     * @param res    Residence to sell
     * @param amount Sale price
     * @return true if the residence was put up for sale, false otherwise
     */
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

    /**
     * Buys a plot for a player by residence name.
     *
     * @param areaname Residence name
     * @param player   Buyer
     * @param resadmin Whether in admin mode
     * @deprecated Use {@link #buyPlot(ClaimedResidence, Player, boolean)} instead
     */
    @Override
    @Deprecated
    public void buyPlot(String areaname, Player player, boolean resadmin) {
        buyPlot(ClaimedResidence.getByName(areaname), player, resadmin);
    }

    /**
     * Buys a plot for a player.
     *
     * @param res      Residence to buy
     * @param player   Buyer
     * @param resadmin Whether in admin mode
     */
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

        if (econ.canAfford(player.getUniqueId(), amount)) {
            if (!econ.transfer(player.getUniqueId(), res.getPermissions().getOwnerUUID(), amount)) {
                player.sendMessage(CMIChatColor.RED + "Error, could not transfer " + amount + " from " + buyerName + " to " + sellerName);
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

    /**
     * Removes a residence from sale by name.
     *
     * @param player   Player requesting removal
     * @param areaname Residence name
     * @param resadmin Whether in admin mode
     * @deprecated Use {@link #removeFromSale(Player, ClaimedResidence, boolean)} instead
     */
    @Deprecated
    public void removeFromSale(Player player, String areaname, boolean resadmin) {
        removeFromSale(player, ClaimedResidence.getByName(areaname), resadmin);
    }

    /**
     * Removes a residence from sale.
     *
     * @param player   Player requesting removal
     * @param res      Residence to remove from sale
     * @param resadmin Whether in admin mode
     */
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

    /**
     * Removes a residence from sale by name (API method).
     *
     * @param areaname Residence name
     * @deprecated Use {@link #removeFromSale(ClaimedResidence)} instead
     */
    @Override
    @Deprecated
    public void removeFromSale(String areaname) {
        removeFromSale(ClaimedResidence.getByName(areaname));
    }

    /**
     * Removes a residence from sale and updates signs.
     *
     * @param res Residence to remove from sale
     */
    public void removeFromSale(ClaimedResidence res) {
        removeFromSale(res, true);
    }

    /**
     * Removes a residence from sale.
     *
     * @param res        Residence to remove from sale
     * @param removeSigns Whether to remove associated signs
     */
    public void removeFromSale(ClaimedResidence res, boolean removeSigns) {
        if (res == null)
            return;
        sellAmount.remove(res);
        if (removeSigns)
            plugin.getSignUtil().removeSign(res);
    }

    /**
     * Checks if a residence is for sale by name.
     *
     * @param areaname Residence name
     * @return true if the residence is for sale, false otherwise
     * @deprecated Use {@link #isForSale(ClaimedResidence)} instead
     */
    @Override
    @Deprecated
    public boolean isForSale(String areaname) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return isForSale(res);
    }

    /**
     * Checks if a residence is for sale.
     *
     * @param res Residence to check
     * @return true if the residence is for sale, false otherwise
     */
    public boolean isForSale(ClaimedResidence res) {
        if (res == null)
            return false;
        return sellAmount.contains(res);
    }

    /**
     * Displays sale information for a residence by name.
     *
     * @param areaname Residence name
     * @param player   Player to display info to
     * @return true if sale info was displayed, false otherwise
     */
    public boolean viewSaleInfo(String areaname, Player player) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return viewSaleInfo(res, player);
    }

    /**
     * Displays sale information for a residence.
     *
     * @param res    Residence to display info for
     * @param player Player to display info to
     * @return true if sale info was displayed, false otherwise
     */
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

    /**
     * Prints a paginated list of residences for sale to a player.
     *
     * @param player Player to display the list to
     * @param page   Page number to display
     */
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

    /**
     * Clears all residences from sale and resets their sell prices.
     */
    public void clearSales() {
        for (ClaimedResidence res : sellAmount) {
            if (res == null)
                continue;
            res.setSellPrice(-1);
        }
        sellAmount.clear();
        System.out.println("[Residence] - ReInit land selling.");
    }

    /**
     * Gets the sale price of a residence by name.
     *
     * @param areaname Residence name
     * @return Sale price, or -1 if not found
     */
    @Override
    public int getSaleAmount(String areaname) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        return getSaleAmount(res);
    }

    /**
     * Gets the sale price of a residence.
     *
     * @param res Residence to check
     * @return Sale price, or -1 if not found
     */
    public int getSaleAmount(ClaimedResidence res) {
        if (res == null)
            return -1;
        return res.getSellPrice();
    }

    /**
     * Loads sale data from a map of residence names to sell prices.
     *
     * @param root Map of residence names to sell prices
     */
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

    /**
     * Gets all residences currently for sale with their prices.
     *
     * @return Map of residence names to sale prices
     */
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

    /**
     * Saves all sale data as a map of residence names to sell prices.
     *
     * @return Map of residence names to sale prices
     */
    public Map<String, Integer> save() {
        return getBuyableResidences();
    }
}