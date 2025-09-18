package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.utils.PlayerCache;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class limits implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player) && args.length < 1)
            return false;

        if (args.length != 0 && args.length != 1)
            return false;
        final String[] tempArgs = args;
        UUID target;
        boolean rsadm = false;
        if (tempArgs.length == 0) {
            target = ((Player) sender).getUniqueId();
            rsadm = true;
        } else
            target = PlayerCache.getUUID(tempArgs[0]);

        if (target == null)
            return false;

        if (!PlayerCache.getSenderUUID(sender).equals(target) && !ResPerm.command_$1_others.hasPermission(sender, this.getClass().getSimpleName()))
            return true;

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(target);
        rPlayer.getGroup().printLimits(sender, target, rsadm);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Show your limits.");
        c.get("Info", Arrays.asList("&eUsage: &6/res limits (playerName)", "Shows the limitations you have on creating and managing residences."));
        LocaleManager.addTabCompleteMain(this, "[playername]");
    }
}
