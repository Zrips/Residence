package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;

public class rt implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (args.length != 0 && args.length != 1 && args.length != 2) {
	    return false;
	}

	if (!resadmin && !ResPerm.randomtp.hasPermission(sender))
	    return true;

	World wname = null;

	Player tPlayer = null;

	if (args.length > 0) {
	    c: for (int i = 0; i < args.length; i++) {
		for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
		    if (!one.getCenter().getWorld().getName().equalsIgnoreCase(args[i]))
			continue;
		    wname = one.getCenter().getWorld();
		    continue c;
		}
		Player p = Bukkit.getPlayer(args[i]);
		if (p != null)
		    tPlayer = p;
	    }
	}

	if (args.length > 0 && wname == null && tPlayer == null) {
	    plugin.msg(sender, lm.Invalid_World);
	    String worlds = "";
	    for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
		if (!worlds.isEmpty())
		    worlds += ", ";
		worlds += one.getCenter().getWorld().getName();
		break;
	    }
	    plugin.msg(sender, lm.RandomTeleport_WorldList, worlds);
	    return true;
	}

	if (tPlayer == null && sender instanceof Player)
	    tPlayer = (Player) sender;

	if (wname == null && tPlayer != null)
	    wname = tPlayer.getLocation().getWorld();

	if (wname == null && tPlayer == null) {
	    plugin.msg(sender, lm.Invalid_World);
	    String worlds = "";
	    for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
		if (!worlds.isEmpty())
		    worlds += ", ";
		worlds += one.getCenter().getWorld().getName();
		break;
	    }
	    plugin.msg(sender, lm.RandomTeleport_WorldList, worlds);
	    return true;
	}

	if (tPlayer == null)
	    return false;

	if (!sender.getName().equalsIgnoreCase(tPlayer.getName()) && !ResPerm.randomtp_admin.hasPermission(sender))
	    return false;

	int sec = plugin.getConfigManager().getrtCooldown();
	if (plugin.getRandomTeleportMap().containsKey(tPlayer.getName()) && !resadmin && !ResPerm.randomtp_cooldownbypass.hasPermission(sender, false)) {
	    if (plugin.getRandomTeleportMap().get(tPlayer.getName()) + (sec * 1000) > System.currentTimeMillis()) {
		int left = (int) (sec - ((System.currentTimeMillis() - plugin.getRandomTeleportMap().get(tPlayer.getName())) / 1000));
		plugin.msg(tPlayer, lm.RandomTeleport_TpLimit, left);
		return true;
	    }
	}
	if (!plugin.getRandomTpManager().isDefinedRnadomTp(wname)) {
	    plugin.msg(sender, lm.RandomTeleport_Disabled);
	    return true;
	}

	Location loc = plugin.getRandomTpManager().getRandomlocation(wname);
	plugin.getRandomTeleportMap().put(tPlayer.getName(), System.currentTimeMillis());

	if (loc == null) {
	    plugin.msg(sender, lm.RandomTeleport_IncorrectLocation, sec);
	    return true;
	}

	if (plugin.getConfigManager().getTeleportDelay() > 0 && !resadmin && !ResPerm.randomtp_delaybypass.hasPermission(sender, false)) {
	    plugin.msg(tPlayer, lm.RandomTeleport_TeleportStarted, loc.getX(), loc.getY(), loc
		.getZ(), plugin.getConfigManager().getTeleportDelay());
	    plugin.getTeleportDelayMap().add(tPlayer.getName());
	    plugin.getRandomTpManager().performDelaydTp(loc, tPlayer);
	} else
	    plugin.getRandomTpManager().performInstantTp(loc, tPlayer);

	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Teleports to random location in world");
	c.get("Info", Arrays.asList("&eUsage: &6/res rt (worldname) (playerName)", "Teleports you to random location in defined world."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[worldname]", "[playername]"));
    }
}
