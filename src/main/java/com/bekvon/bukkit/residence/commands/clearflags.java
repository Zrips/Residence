package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class clearflags implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 3600, regVar = { 2, 3 }, consoleVar = { 666 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (!resadmin) {
            lm.General_NoPermission.sendMessage(sender);
            return null;
        }

        ClaimedResidence area = ClaimedResidence.getByName(args[0]);
        if (area == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return null;
        }

        if (area.getRaid().isRaidInitialized()) {
            lm.Raid_cantDo.sendMessage(sender);
            return null;
        }
        area.getPermissions().clearFlags();
        lm.Flag_Cleared.sendMessage(sender);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Remove all flags from residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res clearflags <residence>"));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
