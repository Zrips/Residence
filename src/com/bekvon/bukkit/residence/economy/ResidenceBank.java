package com.bekvon.bukkit.residence.economy;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

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

    public void setStoredMoney(double amount) {
        storedMoney = amount;
    }

    public void add(double amount) {
        storedMoney = storedMoney + amount;
    }

    public boolean hasEnough(double amount) {
        return storedMoney >= amount;
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
            lm.Economy_MarketDisabled.sendMessage(sender);
        }
        if (!resadmin && !res.getPermissions().playerHas(player, Flags.bank, FlagCombo.OnlyTrue)) {
            lm.Bank_NoAccess.sendMessage(sender);
            return;
        }
        if (!hasEnough(amount)) {
            lm.Bank_NoMoney.sendMessage(sender);
            return;
        }

        if (!resadmin && res.isRented() && !res.getRentedLand().isRenter(player)) {
            lm.Bank_rentedWithdraw.sendMessage(sender, res.getName());
            return;
        }

        if (sender instanceof Player && Residence.getInstance().getEconomyManager().add(player.getUniqueId(), amount) || !(sender instanceof Player)) {
            this.subtract(amount);
            lm.Bank_Withdraw.sendMessage(sender, String.format("%.2f", amount));
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
            lm.Economy_MarketDisabled.sendMessage(sender);
        }
        if (!resadmin && !res.getPermissions().playerHas(player, Flags.bank, FlagCombo.OnlyTrue)) {
            lm.Bank_NoAccess.sendMessage(sender);
            return;
        }
        if (!Residence.getInstance().getEconomyManager().canAfford(player, amount)) {
            lm.Economy_NotEnoughMoney.sendMessage(sender);
            return;
        }

        if (Residence.getInstance().getConfigManager().BankCapacity > 0 && this.getStoredMoneyD() + amount > Residence.getInstance().getConfigManager().BankCapacity) {
            amount = Residence.getInstance().getConfigManager().BankCapacity - this.getStoredMoneyD();
            lm.Bank_full.sendMessage(sender);
            if (amount < 0) {
                return;
            }
        }

        if (Residence.getInstance().getEconomyManager().subtract(player, amount)) {
            this.add(amount);
            lm.Bank_Deposit.sendMessage(sender, Residence.getInstance().getEconomyManager().format(amount));
        }
    }

    public void showBalance(CommandSender sender, boolean resadmin) {

        if (!Residence.getInstance().getConfigManager().enableEconomy()) {
            lm.Economy_MarketDisabled.sendMessage(sender);
        }

        if (sender instanceof Player && !resadmin && !res.isOwner(sender) && !res.getPermissions().playerHas((Player) sender, Flags.bank, FlagCombo.OnlyTrue)) {
            lm.Bank_NoAccess.sendMessage(sender);
            return;
        }

        lm.Residence_Balance.sendMessage(sender, Residence.getInstance().getEconomyManager().format(getStoredMoneyD()));

    }
}
