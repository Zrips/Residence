package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.FileHandler.ConfigReader;

public class bank implements cmd {

    private enum Action {
        deposit, withdraw, balance;

        public static Action get(String name) {
            for (Action one : Action.values()) {
                if (one.name().equalsIgnoreCase(name))
                    return one;
            }
            return null;
        }
    }

    @Override
    @CommandAnnotation(simple = true, priority = 3400, regVar = { 1, 2, 3 }, consoleVar = { 2, 3 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        String resName = null;
        ClaimedResidence res = null;
        Action action = null;
        double amount = 0D;

        for (String one : args) {

            if (action == null) {
                action = Action.get(one);
                continue;
            }

            if (resName == null) {
                resName = one;
                continue;
            }

            try {
                if (amount <= 0)
                    amount = Double.parseDouble(one);
            } catch (Exception ex) {
            }
        }

        if (action == null)
            action = Action.balance;

        if (!action.equals(Action.balance) && amount == 0) {
            try {
                amount = Double.parseDouble(resName);
            } catch (Exception ex) {
            }
            resName = null;
        }

        amount = CMINumber.clamp(amount, 0, Double.MAX_VALUE);

        if (!action.equals(Action.balance) && amount == 0) {
            lm.Invalid_Amount.sendMessage(sender);
            return null;
        }
        if (resName != null) {
            res = plugin.getResidenceManager().getByName(resName);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return null;
            }
        } else if (sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }

        if (res == null) {
            lm.Residence_NotIn.sendMessage(sender);
            return null;
        }

        switch (action) {
        case deposit:
            res.getBank().deposit(sender, amount, resadmin);
            return true;
        case withdraw:
            res.getBank().withdraw(sender, amount, resadmin);
            return true;
        case balance:
            res.getBank().showBalance(sender, resadmin);
            return true;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manage money in a Residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res bank [deposit/withdraw/balance] <residence> <amount>",
                "You must be standing in a Residence or provide residence name",
                "You must have the +bank flag."));
        LocaleManager.addTabCompleteMain(this, "deposit%%withdraw%%balance", "[residence]");
    }
}
