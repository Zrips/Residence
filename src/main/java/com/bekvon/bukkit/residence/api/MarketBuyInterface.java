package com.bekvon.bukkit.residence.api;

import java.util.Map;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Interface for managing the residence market buy/sell system.
 * Obtain an instance via {@link ResidenceApi#getMarketBuyManager()}.
 */
public interface MarketBuyInterface {

    /**
     * Get all residences currently listed for sale.
     *
     * @return map of residence names to their sale prices
     */
    public Map<String, Integer> getBuyableResidences();

    /**
     * Buy a residence for a player.
     *
     * @param areaname Residence name
     * @param player   Buyer
     * @param resadmin Whether in admin mode
     */
    public void buyPlot(String areaname, Player player, boolean resadmin);

    /**
     * Remove a residence from the market.
     *
     * @param areaname Residence name
     */
    public void removeFromSale(String areaname);

    /**
     * Check whether a residence is currently for sale.
     *
     * @param areaname Residence name
     * @return true if the residence is for sale
     */
    public boolean isForSale(String areaname);

    /**
     * Get the sale price of a residence.
     *
     * @param name Residence name
     * @return sale price, or 0 if not for sale
     */
    public int getSaleAmount(String name);

    /**
     * Put a residence up for sale by name.
     *
     * @param areaname Residence name
     * @param amount   Sale price
     * @return true if the residence was successfully listed
     * @deprecated Use {@link #putForSale(ClaimedResidence, int)} instead.
     */
    @Deprecated
    public boolean putForSale(String areaname, int amount);

    /**
     * Put a residence up for sale.
     *
     * @param res    The residence to sell
     * @param amount Sale price
     * @return true if the residence was successfully listed
     */
    public boolean putForSale(ClaimedResidence res, int amount);
}
