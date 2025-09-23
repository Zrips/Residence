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

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class give implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        boolean includeSubzones = false;
        boolean confirmed = false;

        if (args.length != 2 && args.length != 3 && args.length != 4)
            return false;

        for (String one : args) {
            if (one.equalsIgnoreCase("-s"))
                includeSubzones = true;
            if (one.equalsIgnoreCase("-confirmed"))
                confirmed = true;
        }
        if (!confirmed) {

            RawMessage rm = new RawMessage();

            ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return false;
            }

            rm.addText(lm.Residence_GiveConfirm.getMessage(args[0], res.getOwner(), args[1]))
                .addHover(LC.info_Click.getLocale())
                .addCommand((resadmin ? "resadmin" : "res") + " give " + args[0] + " " + args[1] + (includeSubzones ? " -s" : "") + " -confirmed");
            rm.show(sender);

            return true;
        }
        plugin.getResidenceManager().giveResidence(player, args[1], args[0], resadmin, includeSubzones);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Give residence to player.");
        c.get("Info", Arrays.asList("&eUsage: &6/res give <residence name> [player] <-s>", "Gives your owned residence to target player"));
        LocaleManager.addTabCompleteMain(this, "[residence]", "[playername]");
    }
}
