package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class message implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1000)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        ClaimedResidence res = null;
        String message = null;
        Boolean enter = null;

        c: for (String one : args) {
            if (message == null)
                switch (one.toLowerCase()) {
                case "enter":
                    if (enter == null) {
                        enter = true;
                        continue;
                    }
                    break;
                case "leave":
                    if (enter == null) {
                        enter = false;
                        continue;
                    }
                    break;
                case "remove":
                    break c;
                }

            if (res == null && enter == null) {
                res = plugin.getResidenceManager().getByName(one);
                if (res != null)
                    continue;
            }

            if (message == null)
                message = "";
            if (!message.isEmpty())
                message += " ";
            message += one;
        }

        if (res == null && sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (enter == null) {
            lm.Invalid_MessageType.sendMessage(sender);
            return true;
        }

        if (enter && !ResPerm.command_message_enter.hasPermission(sender)) {
            return true;
        }

        if (!enter && !ResPerm.command_message_leave.hasPermission(sender)) {
            return true;
        }

        if (message == null && enter && !ResPerm.command_message_enter_remove.hasPermission(sender)) {
            return true;
        }

        if (message == null && !enter && !ResPerm.command_message_leave_remove.hasPermission(sender)) {
            return true;
        }

        if (message == null)
            message = "";

        res.setEnterLeaveMessage(sender, message, enter, resadmin);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manage residence enter / leave messages");
        c.get("Info", Arrays.asList("&eUsage: &6/res message <residence> [enter/leave] [message]",
                "Set the enter or leave message of a residence.", "&eUsage: &6/res message <residence> remove [enter/leave]", "Removes a enter or leave message."));
        LocaleManager.addTabCompleteMain(this, "[cresidence]", "enter%%leave%%remove", "[enter]%%[leave]");
    }
}
