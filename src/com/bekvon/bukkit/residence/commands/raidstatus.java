package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.UUID;

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
import com.bekvon.bukkit.residence.raid.RaidAttacker;
import com.bekvon.bukkit.residence.raid.RaidDefender;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.utils.PlayerCache;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class raidstatus implements cmd {

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
        if (args.length > 0)
            res = plugin.getResidenceManager().getByName(args[0]);

        if (res == null) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        }

        if (res == null) {
            UUID offp = PlayerCache.getUUID(args[0]);
            if (offp != null) {
                ResidencePlayer resp = plugin.getPlayerManager().getResidencePlayer(offp);
                res = resp.getCurrentlyRaidedResidence();
            }
        }

        if (res == null) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        ResidenceRaid raid = res.getRaid();

        lm.Raid_status_title.sendMessage(sender, res.getName(), res.getOwner());
        if (res.getRaid().isImmune()) {
            lm.Raid_status_immune.sendMessage(sender, Utils.to24hourShort(raid.getImmunityUntil() - System.currentTimeMillis() + 1000L));
        } else if (res.getRaid().isInPreRaid()) {
            lm.Raid_status_starts.sendMessage(sender, Utils.to24hourShort(raid.getStartsAt() - System.currentTimeMillis()));
            RawMessage rm = new RawMessage();
            rm.addText(plugin.getLM().getMessage(lm.Raid_status_attackers, raid.getAttackers().size())).addHover(getAttackers(raid));
            rm.show(sender);
            rm = new RawMessage();
            rm.addText(plugin.getLM().getMessage(lm.Raid_status_defenders, raid.getDefenders().size())).addHover(getDefenders(raid));
            rm.show(sender);
        } else if (res.getRaid().isUnderRaid()) {
            lm.Raid_status_ends.sendMessage(sender, Utils.to24hourShort(raid.getEndsAt() - System.currentTimeMillis()));
            RawMessage rm = new RawMessage();
            rm.addText(plugin.getLM().getMessage(lm.Raid_status_attackers, raid.getAttackers().size())).addHover(getAttackers(raid));
            rm.show(sender);
            rm = new RawMessage();
            rm.addText(plugin.getLM().getMessage(lm.Raid_status_defenders, raid.getDefenders().size())).addHover(getDefenders(raid));
            rm.show(sender);
        } else {
            if (raid.getCooldownEnd() < System.currentTimeMillis())
                lm.Raid_status_canraid.sendMessage(sender);
            else
                lm.Raid_status_raidin.sendMessage(sender, Utils.to24hourShort(raid.getCooldownEnd() - System.currentTimeMillis() + 1000L));
        }

        return true;
    }

    private static String getAttackers(ResidenceRaid raid) {
        String r = "";
        int i = 0;
        for (Entry<UUID, RaidAttacker> one : raid.getAttackers().entrySet()) {
            if (!one.getValue().getPlayer().isOnline())
                continue;
            i++;
            if (i >= 5)
                r += " \n";
            if (!r.isEmpty())
                r += ", ";
            if (one.getValue().getPlayer().isOnline())
                r += one.getValue().getPlayer().getPlayer().getDisplayName();
        }
        return r;
    }

    private static String getDefenders(ResidenceRaid raid) {
        String r = "";
        int i = 0;
        for (Entry<UUID, RaidDefender> one : raid.getDefenders().entrySet()) {
            if (!one.getValue().getPlayer().isOnline())
                continue;
            i++;
            if (i >= 5)
                r += " \n";
            if (!r.isEmpty())
                r += ", ";
            if (one.getValue().getPlayer().isOnline())
                r += one.getValue().getPlayer().getPlayer().getDisplayName();
        }
        return r;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Check raid status for a residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res raidstatus (resName/playerName)"));
        LocaleManager.addTabCompleteMain(this, "[cresidence]%%[playername]");
    }

}
