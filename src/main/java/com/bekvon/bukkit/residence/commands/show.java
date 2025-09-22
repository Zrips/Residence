package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class show implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        ClaimedResidence res = null;

        if (args.length == 1) {
            res = plugin.getResidenceManager().getByName(args[0]);
        } else {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        Visualizer v = new Visualizer(player);
        v.setAreas(res.getAreaArray());
        plugin.getSelectionManager().showBounds(player, v);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Show residence boundaries");
        c.get("Info", Arrays.asList("&eUsage: &6/res show <residence>"));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
