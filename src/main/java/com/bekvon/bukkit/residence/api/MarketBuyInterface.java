package com.bekvon.bukkit.residence.api;

import org.bukkit.entity.Player;

import java.util.Map;

public interface MarketBuyInterface {

    public Map<String, Integer> getBuyableResidences();

    public boolean putForSale(String areaname, int amount);

    public void buyPlot(String areaname, Player player, boolean resadmin);

    public void removeFromSale(String areaname);

    public boolean isForSale(String areaname);

    public int getSaleAmount(String name);
}
