package com.bekvon.bukkit.residence.api;

import com.bekvon.bukkit.residence.Residence;

public class ResidenceApi {

    public static MarketBuyInterface getMarketBuyManager() {
	return Residence.getInstance().getMarketBuyManagerAPI();
    }

    public static MarketRentInterface getMarketRentManager() {
	return Residence.getInstance().getMarketRentManagerAPI();
    }

    public static ResidencePlayerInterface getPlayerManager() {
	return Residence.getInstance().getPlayerManagerAPI();
    }

    public static ChatInterface getChatManager() {
	return Residence.getInstance().getResidenceChatAPI();
    }

    public static ResidenceInterface getResidenceManager() {
	return Residence.getInstance().getResidenceManagerAPI();
    }
}
