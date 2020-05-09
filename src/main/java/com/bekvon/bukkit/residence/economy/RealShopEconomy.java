package com.bekvon.bukkit.residence.economy;

import org.bukkit.entity.Player;

import fr.crafter.tickleman.realeconomy.RealEconomy;

public class RealShopEconomy implements EconomyInterface {

    RealEconomy plugin;

    public RealShopEconomy(RealEconomy e) {
	plugin = e;
    }

    @Override
    public double getBalance(Player player) {
	return plugin.getBalance(player.getName());
    }

    @Override
    public double getBalance(String playerName) {
	return plugin.getBalance(playerName);
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
	if (plugin.getBalance(playerName) >= amount) {
	    return true;
	}
	return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
	plugin.setBalance(playerName, plugin.getBalance(playerName) + amount);
	return true;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
	if (!canAfford(playerName, amount)) {
	    return false;
	}
	plugin.setBalance(playerName, plugin.getBalance(playerName) - amount);
	return true;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
	if (!canAfford(playerFrom, amount)) {
	    return false;
	}
	if (subtract(playerFrom, amount)) {
	    if (!add(playerTo, amount)) {
		add(playerFrom, amount);
		return false;
	    }
	    return true;
	}
	return false;
    }

    @Override
    public String getName() {
	return "RealShopEconomy";
    }

    @Override
    public String format(double amount) {
	return plugin.format(amount);
    }
}
