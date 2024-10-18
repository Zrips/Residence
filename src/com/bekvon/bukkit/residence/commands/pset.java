package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Logs.CMIDebug;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

public class pset implements cmd {

    enum Action {
        removeall, pset;

        public static Action get(String name) {
            for (Action one : Action.values()) {
                if (one.name().equalsIgnoreCase(name))
                    return one;
            }
            return null;
        }
    }

    @Override
    @CommandAnnotation(simple = true, priority = 800)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Action action = null;
        FlagState state = FlagState.INVALID;
        ResidencePlayer rplayer = null;
        ClaimedResidence residence = null;
        Flags flag = null;
        String flagGroup = null;

        for (String one : args) {

            if (action == null) {
                action = Action.get(one);
                if (action != null)
                    continue;
            }

            if (state.equals(FlagState.INVALID)) {
                state = FlagPermissions.stringToFlagState(one);
                if (!state.equals(FlagState.INVALID))
                    continue;
            }

            if (residence == null) {
                residence = plugin.getResidenceManager().getByName(one);
                if (residence != null)
                    continue;
            }

            if (rplayer == null) {
                rplayer = Residence.getInstance().getPlayerManager().getResidencePlayerIfExist(one);
                if (rplayer != null) {
                    continue;
                }
            }

            if (flag == null) {
                flag = Flags.getFlag(one);
                if (flag != null)
                    continue;
            }
            if (flagGroup == null && FlagPermissions.flagGroupExists(one)) {
                flagGroup = one;
                continue;
            }
        }

        if (rplayer == null && residence != null) {
            rplayer = Residence.getInstance().getPlayerManager().getResidencePlayerIfExist(residence.getName());
            residence = null;
        }

        if (rplayer == null && sender instanceof Player) {
            rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer((Player) sender);
        }

        if (residence == null && sender instanceof Player) {
            residence = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }

        if (residence == null) {
            plugin.msg(sender, lm.Invalid_Residence);
            return null;
        }

        if (rplayer == null) {
            plugin.msg(sender, lm.Invalid_Player);
            return true;
        }

        if (action == null)
            action = Action.pset;

        switch (action) {
        case pset:

            if (!state.equals(FlagState.INVALID) && (flag != null || flagGroup != null)) {
                if (!residence.isOwner(sender) && !resadmin && !residence.getPermissions().playerHas(sender, Flags.admin, false)) {
                    plugin.msg(sender, lm.General_NoPermission);
                    return true;
                }
                residence.getPermissions().setPlayerFlag(sender, rplayer.getName(), flag != null ? flag.name() : flagGroup, state.getName(), resadmin, true);
                return true;
            }

            if (!(sender instanceof Player))
                return false;

            final Player player = (Player) sender;
            player.closeInventory();

            if (!residence.isOwner(sender) && !resadmin && !residence.getPermissions().playerHas(sender, Flags.admin, false)) {
                plugin.msg(sender, lm.General_NoPermission);
                return true;
            }

            plugin.getFlagUtilManager().openPsetFlagGui(player, rplayer.getName(), residence, resadmin, 1);

            return true;
        case removeall:
            residence.getPermissions().removeAllPlayerFlags(sender, rplayer.getName(), resadmin);
            return true;
        default:
            break;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set flags on a specific player for a Residence.");
        c.get("Info", Arrays.asList("&eUsage: &6/res pset <residence> [player] [flag] [true/false/remove]",
            "&eUsage: &6/res pset <residence> [player] removeall", "To see a list of flags, use /res flags ?"));
        LocaleManager.addTabCompleteMain(this, "[residence]%%[playername]", "[playername]%%[flag]", "[flag]%%true%%false%%remove", "true%%false%%remove");
    }
}
