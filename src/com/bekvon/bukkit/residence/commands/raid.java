package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.utils.TimeModifier;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Time.CMITimeManager;

public class raid implements cmd {

    enum States {
        start, stop, immunity, kick;

        public static States getState(String name) {
            for (States one : States.values()) {
                if (one.toString().equalsIgnoreCase(name))
                    return one;
            }
            return null;
        }

    }

    @Override
    @CommandAnnotation(simple = true, priority = 3100, regVar = { 1, 2, 3, 4 }, consoleVar = { 2, 3, 4 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (!ConfigManager.RaidEnabled) {
            lm.Raid_NotEnabled.sendMessage(sender);
            return true;
        }

        if (!resadmin && !ResAdmin.isResAdmin(sender)) {
            lm.General_NoPermission.sendMessage(sender);
            return null;
        }

        States state = States.getState(args[0]);

        if (state == null) {
            return false;
        }

        switch (state) {
        case immunity:

            ClaimedResidence res = null;

            if (args.length > 2)
                res = plugin.getResidenceManager().getByName(args[2]);
            if (res == null && sender instanceof Player)
                res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return null;
            }

            Long time = null;
            if (args.length > 3)
                time = TimeModifier.getTimeRangeFromString(args[3]);

            if (args.length < 2)
                return false;

            if (time == null && args.length > 2)
                time = TimeModifier.getTimeRangeFromString(args[2]);

            switch (args[1].toLowerCase()) {
            case "add":
                if (time == null)
                    return false;
                Long immune = res.getRaid().getImmunityUntil();
                immune = immune == null || immune < System.currentTimeMillis() ? System.currentTimeMillis() : immune;
                immune += (time * 1000L);
                res.getRaid().setImmunityUntil(immune);
                lm.Raid_immune.sendMessage(sender, CMITimeManager.to24hourShort(immune - System.currentTimeMillis()));
                return true;
            case "take":
                if (time == null)
                    return false;
                immune = res.getRaid().getImmunityUntil();
                immune = immune == null || immune < System.currentTimeMillis() ? System.currentTimeMillis() : immune;
                immune -= (time * 1000L);
                res.getRaid().setImmunityUntil(immune);

                if (res.getRaid().isImmune())
                    lm.Raid_immune.sendMessage(sender, CMITimeManager.to24hourShort(immune - System.currentTimeMillis()));
                else
                    lm.Raid_notImmune.sendMessage(sender);
                return true;
            case "set":
                if (time == null)
                    return false;
                immune = System.currentTimeMillis() + (time * 1000L);
                res.getRaid().setImmunityUntil(immune);
                lm.Raid_immune.sendMessage(sender, CMITimeManager.to24hourShort(immune - System.currentTimeMillis()));

                return true;
            case "clear":
                res.getRaid().setImmunityUntil(null);
                res.getRaid().setEndsAt(0L);
                lm.Raid_notImmune.sendMessage(sender);

                return true;
            }

            break;
        case kick:

            if (args.length < 2)
                return false;

            ResidencePlayer rplayer = plugin.getPlayerManager().getResidencePlayer(args[1]);

            if (rplayer == null) {
                lm.Invalid_Player.sendMessage(sender);
                return null;
            }

            if (rplayer.getJoinedRaid() == null || rplayer.getJoinedRaid().isEnded()) {
                lm.Raid_notInRaid.sendMessage(sender);
                return null;
            }

            ResidenceRaid raid = rplayer.getJoinedRaid();
            if (raid == null || !raid.isUnderRaid() && !raid.isInPreRaid()) {
                lm.Raid_NotIn.sendMessage(sender);
                return true;
            }

            if (raid.getRes().isOwner(rplayer.getUniqueId())) {
                lm.Raid_CantKick.sendMessage(sender, raid.getRes().getName());
                return true;
            }

            raid.removeAttacker(rplayer);
            raid.removeDefender(rplayer);
            raid.getRes().kickFromResidence(rplayer.getPlayer());

            lm.Raid_Kicked.sendMessage(sender, rplayer.getName(), raid.getRes().getName());

            return true;
        case start:

            res = null;

            if (args.length > 1)
                res = plugin.getResidenceManager().getByName(args[1]);
            if (res == null && sender instanceof Player)
                res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return null;
            }

            if (res.getRaid().isUnderRaid() || res.getRaid().isInPreRaid()) {
                return null;
            }

            res.getRaid().endRaid();
            res.getRaid().setEndsAt(0L);
            res.getRPlayer().setLastRaidDefendTimer(0L);

            boolean started = res.getRaid().preStartRaid(null);

            if (started) {
                res.getRaid().startRaid();
                return true;
            }

            break;
        case stop:

            res = null;

            if (args.length > 1)
                res = plugin.getResidenceManager().getByName(args[1]);
            if (res == null && sender instanceof Player)
                res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return null;
            }

            if (!res.getRaid().isUnderRaid() && !res.getRaid().isInPreRaid()) {
                lm.Raid_defend_notRaided.sendMessage(sender);
                return null;
            }

            res.getRaid().endRaid();
            res.getRaid().setEndsAt(0L);

            lm.Raid_stopped.sendMessage(sender, res.getName());
            return true;
        default:
            break;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manage raid in residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res raid start [resname] (playerName)", "&6/res raid stop [resname]", "&6/res raid kick [playerName]",
            "&6/res raid immunity [add/take/set/clear] [resname/currentres] [time]"));

        LocaleManager.addTabCompleteSub(this, "start", "[residence]");
        LocaleManager.addTabCompleteSub(this, "stop", "[residence]");
        LocaleManager.addTabCompleteSub(this, "kick", "[playername]");
        LocaleManager.addTabCompleteSub(this, "immunity", "add%%take%%set%%clear", "[residence]");
    }

}
