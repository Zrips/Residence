package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class setallowner implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (args.length < 2)
            return false;

        if (!resadmin) {
            lm.General_NoPermission.sendMessage(sender);
            return true;
        }

        String oldOwner = args[0];
        String newOwner = args[1];

        boolean keepFlags = false;
        if (args.length > 2 && args[2].equalsIgnoreCase("-keepflags"))
            keepFlags = true;

        if (!plugin.getPlayerManager().isPlayerExist(sender, newOwner, true)) {
            return null;
        }

        List<ClaimedResidence> playerResidences = new ArrayList<>();
        for (ClaimedResidence res : plugin.getResidenceManager().getResidences().values()) {
            if (res.getOwner().equalsIgnoreCase(oldOwner)) {
                playerResidences.add(res);
            }
        }

        if (playerResidences.isEmpty()) {
            lm.Residence_NotOwned.sendMessage(sender, oldOwner);
            lm.Residence_OwnerChange.sendMessage(sender, args[0], args[1]);
            return true;
        }

        int[] counts = {0, 0}; // counts[0] = transferred, counts[1] = skipped

        for (ClaimedResidence area : playerResidences) {
            processResidenceAndSubzones(area, oldOwner, newOwner, keepFlags, resadmin, plugin, counts);
        }

        lm.Residence_OwnerChangeAll.sendMessage(sender, counts[0], oldOwner, newOwner);

        if (counts[1] > 0) {
            lm.Residence_OwnerChangeAllSkipped.sendMessage(sender, counts[1]);
        }

        return true;
    }

    private void processResidenceAndSubzones(ClaimedResidence area, String oldOwner, String newOwner, boolean keepFlags, boolean resadmin, Residence plugin, int[] counts) {
        if (area.getOwner().equalsIgnoreCase(oldOwner)) {
            if (area.getRaid().isRaidInitialized() && !resadmin) {
                counts[1]++;
            } else {
                area.getPermissions().setOwner(newOwner, !keepFlags);

                if (plugin.getRentManager().isForRent(area))
                    plugin.getRentManager().removeRentable(area);
                if (plugin.getTransactionManager().isForSale(area))
                    plugin.getTransactionManager().removeFromSale(area);

                if (!keepFlags)
                    area.getPermissions().applyDefaultFlags();

                plugin.getSignUtil().updateSignResName(area);
                counts[0]++;
            }
        }

        for (ClaimedResidence subzone : area.getSubzones()) {
            processResidenceAndSubzones(subzone, oldOwner, newOwner, keepFlags, resadmin, plugin, counts);
        }
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Change owner of all residences from one player to another.");
        c.get("Info", Arrays.asList("&eUsage: &6/resadmin setallowner [oldowner] [newowner] (-keepflags)"));
        LocaleManager.addTabCompleteMain(this, "[playername]", "[playername]", "-keepflags");
    }

}
