package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class bank implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3400, regVar = {2, 3}, consoleVar = {2, 3})
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        ClaimedResidence res = null;

        if (args.length == 3) {
            res = plugin.getResidenceManager().getByName(args[1]);
            if (res == null) {
                plugin.msg(sender, lm.Invalid_Residence);
                return null;
            }
        } else if (sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }
        if (res == null) {
            plugin.msg(sender, lm.Residence_NotIn);
            return null;
        }
        double amount = 0D;
        try {
            if (args.length == 2)
                amount = Double.parseDouble(args[1]);
            else
                amount = Double.parseDouble(args[2]);
        } catch (Exception ex) {
            plugin.msg(sender, lm.Invalid_Amount);
            return null;
        }

        switch (args[0].toLowerCase()) {
            case "deposit":
                res.getBank().deposit(sender, amount, resadmin);
                return true;
            case "withdraw":
                res.getBank().withdraw(sender, amount, resadmin);
                return true;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manage money in a Residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res bank [deposit/withdraw] <residence> [amount]",
                "You must be standing in a Residence or provide residence name",
                "You must have the +bank flag."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Arrays.asList("deposit%%withdraw", "[residence]"));
    }
}
