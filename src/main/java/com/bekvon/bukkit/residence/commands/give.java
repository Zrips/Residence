package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

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
                plugin.msg(sender, lm.Invalid_Residence);
                return false;
            }

            rm.add(plugin.getLM().getMessage(lm.Residence_GiveConfirm, args[0], res.getOwner(), args[1]), plugin.getLM().getMessage(lm.info_click), (resadmin ? "resadmin" : "res") + " give " + args[0]
                    + " " + args[1] + (includeSubzones ? " -s" : "") + " -confirmed");
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
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]", "[playername]"));
    }
}
