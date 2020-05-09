package com.bekvon.bukkit.residence.Placeholders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.GetTime;

import me.clip.placeholderapi.PlaceholderAPI;

public class Placeholder {

    private Residence plugin;

    public Placeholder(Residence plugin) {
	this.plugin = plugin;
    }

    public enum CMIPlaceHolders {
	residence_user_amount,
	residence_user_group,
	residence_user_admin,
	residence_user_cancreate,
	residence_user_maxres,
	residence_user_maxew,
	residence_user_maxns,
	residence_user_maxud,
	residence_user_maxsub,
	residence_user_maxsubdepth,
	residence_user_maxrents,
	residence_user_maxrentdays,
	residence_user_blockcost,
	residence_user_blocksell,
	residence_user_current_owner,
	residence_user_current_res,
	residence_user_current_bank,
	residence_user_current_qsize,
	residence_user_current_ssize,
	residence_user_current_forsale,
	residence_user_current_saleprice,
	residence_user_current_forrent,
	residence_user_current_rentprice,
	residence_user_current_rentedby,
	residence_user_current_rentdays,
	residence_user_current_rentends,
	;

	public static CMIPlaceHolders getByName(String name) {
	    name = name.replace("_", "");
	    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
		String n = one.name().replace("_", "");
		if (n.equalsIgnoreCase(name)) {
		    return one;
		}
	    }
	    name = "residence" + name;
	    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
		String n = one.name().replace("_", "");
		if (n.equalsIgnoreCase(name)) {
		    return one;
		}
	    }
	    return null;
	}

	public static CMIPlaceHolders getByNameExact(String name) {
	    name = name.toLowerCase();
	    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
		String n = one.name();
		if (n.equals(name))
		    return one;
	    }
	    return null;
	}

	public String getFull() {
	    return "%" + this.name() + "%";
	}
    }

    public List<String> updatePlaceHolders(Player player, List<String> messages) {
	List<String> ms = new ArrayList<String>(messages);
	for (int i = 0, l = messages.size(); i < l; ++i) {
	    ms.set(i, updatePlaceHolders(player, messages.get(i)));
	}
	return ms;
    }

    public String updatePlaceHolders(Player player, String message) {
	if (message == null)
	    return null;
	if (message.contains("%"))
	    message = translateOwnPlaceHolder(player, message);
	if (!plugin.isPlaceholderAPIEnabled())
	    return message;
	if (message.contains("%"))
	    message = PlaceholderAPI.setPlaceholders(player, message);
	return message;
    }

    Pattern placeholderPatern = Pattern.compile("(%)([^\"^%]*)(%)");

    private String translateOwnPlaceHolder(Player player, String message) {
	if (message == null)
	    return null;

	if (message.contains("%")) {
	    Matcher match = placeholderPatern.matcher(message);
	    while (match.find()) {
		String cmd = match.group(2);
		if (!message.contains("%"))
		    break;
		CMIPlaceHolders place = CMIPlaceHolders.getByNameExact(cmd);
		String with = this.getValue(player, place);
		if (with == null)
		    continue;
		message = message.replace(place.getFull(), with);
	    }
	}

	return message;
    }

    public String getValue(Player player, CMIPlaceHolders placeHolder) {
	ResidencePlayer user = plugin.getPlayerManager().getResidencePlayer(player);
	if (placeHolder == null)
	    return null;
	if (user != null) {
	    PermissionGroup group = user.getGroup();
	    switch (placeHolder) {
	    case residence_user_admin:
		return variable(Residence.getInstance().getPermissionManager().isResidenceAdmin(player));
	    case residence_user_amount:
		return String.valueOf(user.getResAmount());
	    case residence_user_blockcost:
		if (Residence.getInstance().getEconomyManager() != null) {
		    return String.valueOf(group.getCostperarea());
		}
		break;
	    case residence_user_blocksell:
		if (Residence.getInstance().getEconomyManager() != null) {
		    return String.valueOf(group.getSellperarea());
		}
		break;
	    case residence_user_cancreate:
		return variable(group.canCreateResidences());
	    case residence_user_group:
		return group.getGroupName();
	    case residence_user_maxew:
		return group.getXmin() + "-" + group.getXmax();
	    case residence_user_maxns:
		return group.getZmin() + "-" + group.getZmax();
	    case residence_user_maxrentdays:
		return String.valueOf(group.getMaxRentDays());
	    case residence_user_maxrents:
		return String.valueOf(user.getMaxRents());
	    case residence_user_maxres:
		return String.valueOf(user.getMaxRes());
	    case residence_user_maxsub:
		return String.valueOf(user.getMaxSubzones());
	    case residence_user_maxsubdepth:
		return String.valueOf(user.getMaxSubzoneDepth());
	    case residence_user_maxud:
		return group.getYmin() + "-" + group.getYmax();
	    case residence_user_current_owner:
		ClaimedResidence res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "" : res.getOwner();
	    case residence_user_current_res:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "" : res.getName();
	    case residence_user_current_bank:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "0" : res.getBank().getStoredMoneyFormated();
	    case residence_user_current_qsize:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "0" : String.valueOf(res.getTotalSize());
	    case residence_user_current_ssize:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "0" : String.valueOf(res.getXZSize());
	    case residence_user_current_forsale:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "" : String.valueOf(res.isForSell());
	    case residence_user_current_saleprice:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null || !res.isForSell() ? "" : String.valueOf(res.getSellPrice());
	    case residence_user_current_rentprice:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null || !res.isForRent() ? "" : String.valueOf(res.getRentable().cost);
	    case residence_user_current_rentdays:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null || !res.isForRent() ? "" : String.valueOf(res.getRentable().days);
	    case residence_user_current_rentedby:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null || !res.isForRent() || res.getRentedLand() == null || res.getRentedLand().player == null ? "" : res.getRentedLand().player;
	    case residence_user_current_rentends:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null || !res.isForRent() || res.getRentedLand() == null || res.getRentedLand().player == null ? "" : GetTime.getTime(res.getRentedLand().endTime, true);
	    case residence_user_current_forrent:
		res = plugin.getResidenceManager().getByLoc(user.getPlayer().getLocation());
		return res == null ? "" : String.valueOf(res.isForRent());
	    default:
		break;
	    }
	}

	switch (placeHolder) {
	default:
	    break;
	}

	return null;
    }

    private String variable(Boolean state) {
	return state ? plugin.getLM().getMessage(lm.General_True) : plugin.getLM().getMessage(lm.General_False);
    }
}