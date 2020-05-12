package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.RaidAttacker;
import com.bekvon.bukkit.residence.raid.RaidDefender;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.utils.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.UUID;

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
            plugin.msg(player, lm.Raid_NotEnabled);
            return true;
        }

        ClaimedResidence res = null;
        if (args.length > 0)
            res = plugin.getResidenceManager().getByName(args[0]);

        if (res == null) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        }

        if (res == null) {
            OfflinePlayer offp = plugin.getOfflinePlayer(args[0]);
            if (offp != null) {
                ResidencePlayer resp = plugin.getPlayerManager().getResidencePlayer(offp.getUniqueId());
                res = resp.getCurrentlyRaidedResidence();
            }
        }

        if (res == null) {
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        }

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return true;
        }

        ResidenceRaid raid = res.getRaid();

        plugin.msg(sender, "&7----------- &f" + res.getName() + "(" + res.getOwner() + ") &7-----------");
        if (res.getRaid().isImmune()) {
            plugin.msg(sender, "&eImmune to raids for next: " + Utils.to24hourShort(raid.getImmunityUntil() - System.currentTimeMillis() + 1000L));
        } else if (res.isInPreRaid()) {
            plugin.msg(sender, "&7Raid starts in: " + Utils.to24hourShort(raid.getStartsAt() - System.currentTimeMillis()));
            RawMessage rm = new RawMessage();
            rm.add("&7Attackers: &4" + raid.getAttackers().size(), getAttackers(raid));
            rm.show(sender);
            rm = new RawMessage();
            rm.add("&7Defenders: &2" + raid.getDefenders().size(), getDefenders(raid));
            rm.show(sender);
        } else if (res.isUnderRaid()) {
            plugin.msg(sender, "&7Raid ends in: " + Utils.to24hourShort(raid.getEndsAt() - System.currentTimeMillis()));
            RawMessage rm = new RawMessage();
            rm.add("&7Attackers: &4" + raid.getAttackers().size(), getAttackers(raid));
            rm.show(sender);
            rm = new RawMessage();
            rm.add("&7Defenders: &2" + raid.getDefenders().size(), getDefenders(raid));
            rm.show(sender);
        } else {
            plugin.msg(sender, raid.getCooldownEnd() < System.currentTimeMillis() ? "&2Can be raided" : "&ePosible raid in: " + Utils.to24hourShort(raid.getCooldownEnd() - System.currentTimeMillis()
                    + 1000L));
        }

        return true;
    }

    private String getAttackers(ResidenceRaid raid) {
        StringBuilder r = new StringBuilder();
        int i = 0;
        for (Entry<UUID, RaidAttacker> one : raid.getAttackers().entrySet()) {
            if (!one.getValue().getPlayer().isOnline())
                continue;
            i++;
            if (i >= 5)
                r.append(" \n");
            if (r.length() > 0)
                r.append(", ");
            if (one.getValue().getPlayer().isOnline())
                r.append(one.getValue().getPlayer().getPlayer().getDisplayName());
        }
        return r.toString();
    }

    private String getDefenders(ResidenceRaid raid) {
        StringBuilder r = new StringBuilder();
        int i = 0;
        for (Entry<UUID, RaidDefender> one : raid.getDefenders().entrySet()) {
            if (!one.getValue().getPlayer().isOnline())
                continue;
            i++;
            if (i >= 5)
                r.append(" \n");
            if (r.length() > 0)
                r.append(", ");
            if (one.getValue().getPlayer().isOnline())
                r.append(one.getValue().getPlayer().getPlayer().getDisplayName());
        }
        return r.toString();
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Check raid status for a residence");
        c.get("Info", Collections.singletonList("&eUsage: &6/res raidstatus (resName/playerName)"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[cresidence]%%[playername]"));
    }

}
