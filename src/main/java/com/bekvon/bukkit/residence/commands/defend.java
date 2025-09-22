package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.RaidDefender;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class defend implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        final Player player = (Player) sender;

        if (args.length != 0 && args.length != 1)
            return false;

        if (!ConfigManager.RaidEnabled) {
            lm.Raid_NotEnabled.sendMessage(sender);
            return true;
        }

        ClaimedResidence res = null;
        ResidencePlayer target = null;
        if (args.length == 1) {
            UUID targetP = ResidencePlayer.getUUID(args[0]);
            if (targetP != null) {
                if (Bukkit.getPlayer(targetP) == null) {
                    lm.Invalid_PlayerOffline.sendMessage(sender);
                    return true;
                }
                target = plugin.getPlayerManager().getResidencePlayer(targetP);
            } else
                res = plugin.getResidenceManager().getByName(args[0]);
        } else
            res = plugin.getResidenceManager().getByLoc(player.getLocation());

        if (res == null) {
            ResidencePlayer owner = plugin.getPlayerManager().getResidencePlayer(player);
            res = owner.getCurrentlyRaidedResidence();
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        if (resPlayer.getJoinedRaid() != null) {
            lm.Raid_attack_alreadyInAnother.sendMessage(sender, resPlayer.getJoinedRaid().getRes().getName());
            return true;
        }

        if (res.isOwner(player)) {
            if (target != null) {
                ClaimedResidence req = joinRequests.get(target.getUniqueId());

                if (req != null && req.equals(res)) {
                    lm.Raid_defend_Joined.sendMessage(target.getPlayer(), res.getName());
                    for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
                        if (!target.getUniqueId().equals(one.getKey())) {
                            lm.Raid_defend_JoinedDef.sendMessage(Bukkit.getPlayer(one.getKey()), target.getPlayer().getDisplayName());
                        }
                    }
                    res.getRaid().addDefender(target);
                    joinRequests.remove(target.getUniqueId());
                    ownerJoinRequests.remove(target.getUniqueId());
                    return true;
                }

                ownerJoinRequests.put(target.getUniqueId(), res);
                RawMessage rm = new RawMessage();
                rm.addText(plugin.getLM().getMessage(lm.Raid_defend_Invitation, res.getName())).addHover(res.getName()).addCommand("res defend " + res.getName());
                rm.show(target.getPlayer());
                return true;
            }
            lm.Raid_defend_noSelf.sendMessage(sender);
            return true;
        }

        final ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(res.getOwnerUUID());
        if (!rPlayer.isOnline()) {
            lm.Raid_defend_IsOffline.sendMessage(sender);
            return true;
        }

        if (!res.getRaid().isInPreRaid() && !res.getRaid().isUnderRaid()) {
            lm.Raid_defend_notRaided.sendMessage(sender);
            return true;
        }

        if (res.getRaid().isUnderRaid() || res.getRaid().isInPreRaid()) {

            ClaimedResidence req = ownerJoinRequests.get(player.getUniqueId());
            if (req != null && req.equals(res)) {
                lm.Raid_defend_Joined.sendMessage(sender, res.getName());
                for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
                    if (!player.getUniqueId().equals(one.getKey())) {
                        lm.Raid_defend_JoinedDef.sendMessage(Bukkit.getPlayer(one.getKey()), player.getDisplayName());
                    }
                }
                res.getRaid().addDefender(player);
                joinRequests.remove(player.getUniqueId());
                ownerJoinRequests.remove(player.getUniqueId());
                return true;
            }

            joinRequests.put(player.getUniqueId(), res);
            RawMessage rm = new RawMessage();
            lm.Raid_defend_Sent.sendMessage(sender, res.getName());
            rm.addText(plugin.getLM().getMessage(lm.Raid_defend_Invitation, player.getDisplayName())).addHover(player.getName()).addCommand("res defend " + rPlayer.getPlayer().getName());
            rm.show(rPlayer.getPlayer());
            return true;
        }

        lm.Raid_cantJoin.sendMessage(sender);

        return false;
    }

    static HashMap<UUID, ClaimedResidence> ownerJoinRequests = new HashMap<UUID, ClaimedResidence>();
    static HashMap<UUID, ClaimedResidence> joinRequests = new HashMap<UUID, ClaimedResidence>();

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Join raid defence on residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res defend [resName] (playerName)"));
        LocaleManager.addTabCompleteMain(this, "[cresidence]%%[playername]", "[playername]");
    }

}
