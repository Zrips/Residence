package com.bekvon.bukkit.residence.economy;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResidenceBank {
    Double storedMoney;
    ClaimedResidence res;

    public ResidenceBank(ClaimedResidence parent) {
        storedMoney = 0D;
        res = parent;
    }

    @Deprecated
    public int getStoredMoney() {
        return storedMoney.intValue();
    }

    public void setStoredMoney(double amount) {
        storedMoney = amount;
    }

    public Double getStoredMoneyD() {
        return storedMoney;
    }

    public String getStoredMoneyFormated() {
        try {
            return Residence.getInstance().getEconomyManager().format(storedMoney);
        } catch (Exception e) {
            return String.valueOf(this.storedMoney);
        }
    }

    public void add(double amount) {
        storedMoney = storedMoney + amount;
    }

    public boolean hasEnough(double amount) {
        if (storedMoney >= amount)
            return true;
        return false;
    }

    public void subtract(double amount) {
        storedMoney = storedMoney - amount;
        if (storedMoney < 0)
            storedMoney = 0D;
    }

    @Deprecated
    public void withdraw(CommandSender sender, int amount, boolean resadmin) {
        withdraw(sender, (double) amount, resadmin);
    }

    public void withdraw(CommandSender sender, double amount, boolean resadmin) {
        if (!(sender instanceof Player))
            return;
        Player player = (Player) sender;
        if (!Residence.getInstance().getConfigManager().enableEconomy()) {
            Residence.getInstance().msg(sender, lm.Economy_MarketDisabled);
        }
        if (!resadmin && !res.getPermissions().playerHas(player, Flags.bank, FlagCombo.OnlyTrue)) {
            Residence.getInstance().msg(sender, lm.Bank_NoAccess);
            return;
        }
        if (!hasEnough(amount)) {
            Residence.getInstance().msg(sender, lm.Bank_NoMoney);
            return;
        }

        if (!resadmin && res.isRented() && !res.getRentedLand().player.equalsIgnoreCase(sender.getName())) {
            Residence.getInstance().msg(sender, lm.Bank_rentedWithdraw, res.getName());
            return;
        }

        if (sender instanceof Player && Residence.getInstance().getEconomyManager().add(sender.getName(), amount) || !(sender instanceof Player)) {
            this.subtract(amount);
            Residence.getInstance().msg(sender, lm.Bank_Withdraw, String.format("%.2f", amount));
        }
    }

    @Deprecated
    public void deposit(CommandSender sender, int amount, boolean resadmin) {
        deposit(sender, (double) amount, resadmin);
    }

    public void deposit(CommandSender sender, double amount, boolean resadmin) {
        if (!(sender instanceof Player))
            return;
        Player player = (Player) sender;
        if (!Residence.getInstance().getConfigManager().enableEconomy()) {
            Residence.getInstance().msg(sender, lm.Economy_MarketDisabled);
        }
        if (!resadmin && !res.getPermissions().playerHas(player, Flags.bank, FlagCombo.OnlyTrue)) {
            Residence.getInstance().msg(sender, lm.Bank_NoAccess);
            return;
        }
        if (sender instanceof Player && !Residence.getInstance().getEconomyManager().canAfford(sender.getName(), amount)) {
            Residence.getInstance().msg(sender, lm.Economy_NotEnoughMoney);
            return;
        }
        if (sender instanceof Player && Residence.getInstance().getEconomyManager().subtract(sender.getName(), amount) || !(sender instanceof Player)) {
            this.add(amount);
            Residence.getInstance().msg(sender, lm.Bank_Deposit, Residence.getInstance().getEconomyManager().format(amount));
        }
    }
}
