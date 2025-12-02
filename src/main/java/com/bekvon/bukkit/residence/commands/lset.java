package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class lset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 5000)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        ClaimedResidence res = null;
        Material mat = null;
        String listtype = null;
        boolean showinfo = false;
        if (args.length == 1 && args[0].equals("info")) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
            showinfo = true;
        } else if (args.length == 2 && args[1].equals("info")) {
            res = plugin.getResidenceManager().getByName(args[0]);
            showinfo = true;
        }
        if (showinfo) {
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            lm.General_Blacklist.sendMessage(sender);
            res.getItemBlacklist().printList(player);
            lm.General_Ignorelist.sendMessage(sender);
            res.getItemIgnoreList().printList(player);
            return true;
        } else if (args.length == 3) {
            res = plugin.getResidenceManager().getByName(args[0]);
            listtype = args[1];
            try {
                mat = Material.valueOf(args[2].toUpperCase());
            } catch (Exception ex) {
                lm.Invalid_Material.sendMessage(sender);
                return true;
            }
        } else if (args.length == 2) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
            listtype = args[0];
            try {
                mat = Material.valueOf(args[1].toUpperCase());
            } catch (Exception ex) {
                lm.Invalid_Material.sendMessage(sender);
                return true;
            }
        }
        if (res != null) {
            if (listtype != null && listtype.equalsIgnoreCase("blacklist")) {
                res.getItemBlacklist().playerListChange(player, mat, resadmin);
            } else if (listtype != null && listtype.equalsIgnoreCase("ignorelist")) {
                res.getItemIgnoreList().playerListChange(player, mat, resadmin);
            } else {
                lm.Invalid_List.sendMessage(sender);
            }
            return true;
        }
        lm.Invalid_Residence.sendMessage(sender);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Change blacklist and ignorelist options");
        c.get("Info", Arrays.asList("&eUsage: &6/res lset <residence> [blacklist/ignorelist] [material]",
                "&eUsage: &6/res lset <residence> Info",
                "Blacklisting a material prevents it from being placed in the residence.",
                "Ignorelist causes a specific material to not be protected by Residence."));
        LocaleManager.addTabCompleteMain(this, "[residence]%%blacklist%%ignorelist", "blacklist%%ignorelist%%[material]", "[material]");
    }
}
