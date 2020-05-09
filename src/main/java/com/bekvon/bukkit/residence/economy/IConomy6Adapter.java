package com.bekvon.bukkit.residence.economy;

import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import org.bukkit.entity.Player;

public class IConomy6Adapter implements EconomyInterface {

    iConomy icon;

    public IConomy6Adapter(iConomy iconomy) {
        icon = iconomy;
    }

    private static void checkExist(String playerName) {
        Accounts acc = new Accounts();
        if (!acc.exists(playerName)) {
            acc.create(playerName);
        }
    }

    @Override
    public double getBalance(Player player) {
        checkExist(player.getName());
        return new Accounts().get(player.getName()).getHoldings().getBalance();
    }

    @Override
    public double getBalance(String playerName) {
        checkExist(playerName);
        return new Accounts().get(playerName).getHoldings().getBalance();
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
        checkExist(playerName);
        double holdings = this.getBalance(playerName);
        if (holdings >= amount) {
            return true;
        }
        return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
        checkExist(playerName);
        new Accounts().get(playerName).getHoldings().add(amount);
        return true;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
        checkExist(playerName);
        if (this.canAfford(playerName, amount)) {
            new Accounts().get(playerName).getHoldings().subtract(amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
        checkExist(playerTo);
        checkExist(playerFrom);
        if (this.canAfford(playerFrom, amount)) {
            Account p1 = new Accounts().get(playerFrom);
            Account p2 = new Accounts().get(playerTo);
            p1.getHoldings().subtract(amount);
            p2.getHoldings().add(amount);
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
