package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class unstuck implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4000)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        if (!group.hasUnstuckAccess() && !ResAdmin.isResAdmin(player)) {
            plugin.msg(player, lm.General_NoPermission);
            return true;
        }
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

        if (res == null) {
            plugin.msg(player, lm.Residence_NotIn);
            return true;
        }

        res.kickFromResidence(player);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Teleports outside of residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res unstuck"));
        LocaleManager.addTabCompleteMain(this);
    }
}
