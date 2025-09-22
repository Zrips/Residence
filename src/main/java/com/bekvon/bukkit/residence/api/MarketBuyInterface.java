package com.bekvon.bukkit.residence.api;

import java.util.Map;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public interface MarketBuyInterface {

    public Map<String, Integer> getBuyableResidences();


    public void buyPlot(String areaname, Player player, boolean resadmin);

    public void removeFromSale(String areaname);

    public boolean isForSale(String areaname);

    public int getSaleAmount(String name);

    @Deprecated
    public boolean putForSale(String areaname, int amount);
    
    public boolean putForSale(ClaimedResidence res, int amount); 
}
