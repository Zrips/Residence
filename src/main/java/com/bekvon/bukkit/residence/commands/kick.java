package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class kick implements cmd {

    @Override
    @CommandAnnotation(priority = 2200)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        if (args.length != 1)
            return false;

        Player targetplayer = Bukkit.getPlayer(args[0]);
        if (targetplayer == null || !player.canSee(targetplayer)) {
            lm.General_NotOnline.sendMessage(sender);
            return true;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        PermissionGroup group = rPlayer.getGroup();

        if (!group.hasKickAccess() && !resadmin) {
            lm.General_NoPermission.sendMessage(sender);
            return true;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(targetplayer.getLocation());

        if (res == null || !res.isOwner(player) && !resadmin && !res.getPermissions().playerHas(player, Flags.admin, false)) {
            lm.Residence_PlayerNotIn.sendMessage(sender);
            return true;
        }

        if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.admin, false))
            return false;

        if (res.getRaid().isRaidInitialized()) {
            lm.Raid_cantDo.sendMessage(sender);
            return true;
        }

        if (!res.getPlayersInResidence().contains(targetplayer))
            return false;

        if (ResPerm.command_kick_bypass.hasPermission(targetplayer)) {
            lm.Residence_CantKick.sendMessage(sender);
            return true;
        }
        res.kickFromResidence(targetplayer);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Kicks player from residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res kick <player>", "You must be the owner or an admin to do this.", "Player should be online."));
        LocaleManager.addTabCompleteMain(this, "[playername]");
    }
}
