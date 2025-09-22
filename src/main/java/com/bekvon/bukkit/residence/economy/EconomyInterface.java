package com.bekvon.bukkit.residence.economy;

import java.util.UUID;

import org.bukkit.entity.Player;

public interface EconomyInterface {
    public double getBalance(Player player);

    public double getBalance(UUID player);

    @Deprecated
    public double getBalance(String playerName);

    @Deprecated
    public boolean canAfford(String playerName, double amount);

    public boolean canAfford(Player player, double amount);

    public boolean canAfford(UUID player, double amount);

    @Deprecated
    public boolean add(String playerName, double amount);

    public boolean add(UUID playerName, double amount);

    @Deprecated
    public boolean subtract(String playerName, double amount);

    public boolean subtract(Player player, double amount);

    public boolean subtract(UUID playerName, double amount);

    @Deprecated
    public boolean transfer(String playerFrom, String playerTo, double amount);

    public boolean transfer(UUID playerFrom, UUID playerTo, double amount);

    public String getName();

    public String format(double amount);

}
