package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class rc implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	if (!(sender instanceof Player))
	    return true;
	Player player = (Player) sender;
	String pname = player.getName();
	if (!plugin.getConfigManager().chatEnabled()) {
	    plugin.msg(player, lm.Residence_ChatDisabled);
	    return false;
	}

	if (args.length == 0) {
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    if (res == null) {
		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);
		if (chat != null) {
		    plugin.getChatManager().removeFromChannel(pname);
		    plugin.getPlayerListener().removePlayerResidenceChat(player);
		    return true;
		}
		plugin.msg(player, lm.Residence_NotIn);
		return true;
	    }
	    ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);
	    if (chat != null && chat.getChannelName().equals(res.getName())) {
		plugin.getChatManager().removeFromChannel(pname);
		plugin.getPlayerListener().removePlayerResidenceChat(player);
		return true;
	    }
	    if (!res.getPermissions().playerHas(player.getName(), Flags.chat, true) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		plugin.msg(player, lm.Residence_FlagDeny, Flags.chat, res.getName());
		return false;
	    }

	    plugin.getPlayerListener().tooglePlayerResidenceChat(player, res.getName());
	    plugin.getChatManager().setChannel(pname, res);
	    return true;
	} else if (args.length == 1) {
	    if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("leave")) {
		plugin.getChatManager().removeFromChannel(pname);
		plugin.getPlayerListener().removePlayerResidenceChat(player);
		return true;
	    }
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
	    if (res == null) {
		plugin.msg(player, lm.Chat_InvalidChannel);
		return true;
	    }

	    if (!res.getPermissions().playerHas(player.getName(), Flags.chat, true) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		plugin.msg(player, lm.Residence_FlagDeny, Flags.chat, res.getName());
		return false;
	    }
	    plugin.getPlayerListener().tooglePlayerResidenceChat(player, res.getName());
	    plugin.getChatManager().setChannel(pname, res);

	    return true;
	} else if (args.length == 2) {
	    if (args[0].equalsIgnoreCase("setcolor")) {

		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    plugin.msg(player, lm.Chat_JoinFirst);
		    return true;
		}

		ClaimedResidence res = plugin.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.isOwner(player) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}

		if (!ResPerm.chatcolor.hasPermission(player))
		    return true;

		String possibleColor = args[1];

		if (possibleColor.length() == 1 && !possibleColor.contains("&"))
		    possibleColor = "&" + possibleColor;

		CMIChatColor color = CMIChatColor.getColor(possibleColor);

		if (color == null && possibleColor.length() > 2 && !possibleColor.startsWith(CMIChatColor.colorCodePrefix) && !possibleColor.endsWith(CMIChatColor.colorCodeSuffix))
		    possibleColor = CMIChatColor.colorCodePrefix + possibleColor + CMIChatColor.colorCodeSuffix;
 
		color = CMIChatColor.getColor(possibleColor);

		if (color == null) {
		    plugin.msg(player, lm.Chat_InvalidColor);
		    return true;
		}

		res.setChannelColor(color);
		chat.setChannelColor(color);
		plugin.msg(player, lm.Chat_ChangedColor, color.getName());
		return true;
	    } else if (args[0].equalsIgnoreCase("setprefix")) {
		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    plugin.msg(player, lm.Chat_JoinFirst);
		    return true;
		}

		ClaimedResidence res = plugin.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.isOwner(player) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}

		if (!ResPerm.chatprefix.hasPermission(player))
		    return true;

		String prefix = args[1];

		if (prefix.length() > plugin.getConfigManager().getChatPrefixLength()) {
		    plugin.msg(player, lm.Chat_InvalidPrefixLength, plugin.getConfigManager()
			.getChatPrefixLength());
		    return true;
		}

		res.setChatPrefix(prefix);
		chat.setChatPrefix(prefix);
		plugin.msg(player, lm.Chat_ChangedPrefix, CMIChatColor.translate(prefix));
		return true;
	    } else if (args[0].equalsIgnoreCase("kick")) {
		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    plugin.msg(player, lm.Chat_JoinFirst);
		    return true;
		}

		ClaimedResidence res = plugin.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.getOwner().equals(player.getName()) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}

		if (!ResPerm.chatkick.hasPermission(player))
		    return true;

		String targetName = args[1];
		if (!chat.hasMember(targetName)) {
		    plugin.msg(player, lm.Chat_NotInChannel);
		    return false;
		}

		chat.leave(targetName);
		plugin.getPlayerListener().removePlayerResidenceChat(targetName);
		plugin.msg(player, lm.Chat_Kicked, targetName, chat.getChannelName());
		return true;
	    }
	}
	return true;
    }

    @Override
    public void getLocale() {
	ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
	c.get("Description", "Joins current or defined residence chat channel");
	c.get("Info", Arrays.asList("&eUsage: &6/res rc (residence)", "Join residence chat channel."));
	LocaleManager.addTabCompleteMain(this, "[residence]");

	c.setFullPath(c.getPath() + "SubCommands.");
	c.get("leave.Description", "Leaves current residence chat channel");
	c.get("leave.Info", Arrays.asList("&eUsage: &6/res rc leave", "If you are in residence chat channel then you will leave it"));
	LocaleManager.addTabCompleteSub(this, "leave");

	c.get("setcolor.Description", "Sets residence chat channel text color");
	c.get("setcolor.Info", Arrays.asList("&eUsage: &6/res rc setcolor [colorCode]", "Sets residence chat channel text color"));
	LocaleManager.addTabCompleteSub(this, "setcolor");

	c.get("setprefix.Description", "Sets residence chat channel prefix");
	c.get("setprefix.Info", Arrays.asList("&eUsage: &6/res rc setprefix [newName]", "Sets residence chat channel prefix"));
	LocaleManager.addTabCompleteSub(this, "setprefix");

	c.get("kick.Description", "Kicks player from channel");
	c.get("kick.Info", Arrays.asList("&eUsage: &6/res rc kick [player]", "Kicks player from channel"));
	LocaleManager.addTabCompleteSub(this, "kick", "[playername]");
    }
}
