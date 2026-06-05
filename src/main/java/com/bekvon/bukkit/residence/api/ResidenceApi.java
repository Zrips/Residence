package com.bekvon.bukkit.residence.api;

import com.bekvon.bukkit.residence.Residence;

/**
 * Main static entry point for the Residence API.
 * Provides access to all manager interfaces for market, rent, player, chat, and residence operations.
 */
public class ResidenceApi {

    /**
     * Get market buy manager
     *
     * @return MarketBuyInterface instance
     */
    public static MarketBuyInterface getMarketBuyManager() {
        return Residence.getInstance().getMarketBuyManagerAPI();
    }

    /**
     * Get market rent manager
     *
     * @return MarketRentInterface instance
     */
    public static MarketRentInterface getMarketRentManager() {
        return Residence.getInstance().getMarketRentManagerAPI();
    }

    /**
     * Get player manager
     *
     * @return ResidencePlayerInterface instance
     */
    public static ResidencePlayerInterface getPlayerManager() {
        return Residence.getInstance().getPlayerManagerAPI();
    }

    /**
     * Get residence chat manager
     *
     * @return ChatInterface instance
     */
    public static ChatInterface getChatManager() {
        return Residence.getInstance().getResidenceChatAPI();
    }

    /**
     * Get residence manager
     *
     * @return ResidenceInterface instance
     */
    public static ResidenceInterface getResidenceManager() {
        return Residence.getInstance().getResidenceManagerAPI();
    }
}
