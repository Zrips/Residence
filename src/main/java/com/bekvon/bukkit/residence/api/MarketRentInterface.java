package com.bekvon.bukkit.residence.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public interface MarketRentInterface {
    public Set<ClaimedResidence> getRentableResidences();

    public Set<ClaimedResidence> getCurrentlyRentedResidences();

    public RentedLand getRentedLand(String landName);

    public List<String> getRentedLands(String playerName);

    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean resadmin);

    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean resadmin);

    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean AllowAutoPay, boolean resadmin);

    public void rent(Player player, String landName, boolean repeat, boolean resadmin);

    public void removeFromForRent(Player player, String landName, boolean resadmin);

    public void unrent(Player player, String landName, boolean resadmin);

    public void removeFromRent(String landName);

    public void removeRentable(String landName);

    public boolean isForRent(String landName);

    public boolean isRented(String landName);

    public String getRentingPlayer(String landName);

    public int getCostOfRent(String landName);

    public boolean getRentableRepeatable(String landName);

    public boolean getRentedAutoRepeats(String landName);

    public int getRentDays(String landName);

    public void checkCurrentRents();

    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin);

    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin);

    @Deprecated
    public int getRentCount(String player);

    @Deprecated
    public int getRentableCount(String player);

    public int getRentableCount(UUID playerUUID);

    public int getRentCount(UUID playerUUID);
}
