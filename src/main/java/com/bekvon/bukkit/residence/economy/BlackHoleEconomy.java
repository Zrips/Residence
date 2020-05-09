package com.bekvon.bukkit.residence.economy;

import org.bukkit.entity.Player;

public class BlackHoleEconomy implements EconomyInterface {

    public BlackHoleEconomy() {
    }

    @Override
    public double getBalance(Player player) {
        return 0D;
    }

    @Override
    public double getBalance(String playerName) {
        return 0D;
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        return false;
    }

    @Override
    public String getName() {
        return "BlackHoleEconomy";
    }

    @Override
    public String format(double amount) {
        return String.valueOf(Math.round(amount * 100) / 100D);
    }

}