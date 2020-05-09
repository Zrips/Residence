package com.bekvon.bukkit.residence.economy;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import org.bukkit.entity.Player;

public class IConomy5Adapter implements EconomyInterface {

    @Override
    public double getBalance(Player player) {
        Account acc = iConomy.getAccount(player.getName());
        return (acc == null) ? 0 : acc.getHoldings().balance();
    }

    @Override
    public double getBalance(String playerName) {
        Account acc = iConomy.getAccount(playerName);
        return (acc == null) ? 0 : acc.getHoldings().balance();
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        if (amount == 0) {
            return true;
        }
        Account acc = iConomy.getAccount(playerName);
        return (acc == null) ? false : acc.getHoldings().hasEnough(amount);
    }

    @Override
    public boolean add(String playerName, double amount) {
        Account acc = iConomy.getAccount(playerName);
        if (acc != null) {
            acc.getHoldings().add(amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        Account acc = iConomy.getAccount(playerName);
        if (acc != null) {
            acc.getHoldings().subtract(amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        Account accFrom = iConomy.getAccount(playerFrom);
        Account accTo = iConomy.getAccount(playerTo);
        if (accFrom != null && accTo != null) {
            accFrom.getHoldings().subtract(amount);
            accTo.getHoldings().add(amount);
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "iConomy";
    }

    @Override
    public String format(double amount) {
        return iConomy.format(amount);
    }
}
