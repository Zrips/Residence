package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class gset implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        String residenceName = null;
        String flagGroup = null;
        Flags flag = null;
        FlagState state = null;

        for (String one : args) {

            if (flagGroup == null && plugin.getPermissionManager().hasGroup(one)) {
                flagGroup = one;
                continue;
            }

            if (flag == null) {
                flag = Flags.getFlag(one);
                if (flag != null)
                    continue;
            }

            if (state == null) {
                FlagState s = FlagPermissions.stringToFlagState(one);
                if (!s.equals(FlagState.INVALID)) {
                    state = s;
                    continue;
                }
            }

            if (residenceName == null)
                residenceName = one;
        }

        ClaimedResidence area = null;
        Player player = (Player) sender;

        if (residenceName != null)
            area = plugin.getResidenceManager().getByName(args[0]);
        else
            area = plugin.getResidenceManager().getByLoc(player.getLocation());

        if (area == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (flagGroup == null) {
            lm.Invalid_Group.sendMessage(sender);
            return true;
        }

        area.getPermissions().setGroupFlag(player, flagGroup, flag, state, resadmin);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set flags on a specific group for a Residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res gset <residence> [group] [flag] [true/false/remove]", "To see a list of flags, use /res flags ?"));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
