package com.bekvon.bukkit.residence.api;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public interface ResidenceInterface {
    public ClaimedResidence getByLoc(Location loc);

    public ClaimedResidence getByName(String name);

    public String getSubzoneNameByRes(ClaimedResidence res);

    public void addShop(ClaimedResidence res);

    public void addShop(String res);

    public void removeShop(ClaimedResidence res);

    public void removeShop(String res);

    public List<ClaimedResidence> getShops();

    public boolean addResidence(String name, Location loc1, Location loc2);

    public boolean addResidence(String name, String owner, Location loc1, Location loc2);

    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin);
    
    public boolean addResidence(Player player, String name, boolean resadmin);
}
