package com.bekvon.bukkit.residence.itemlist;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ResidenceItemList extends ItemList {
    ClaimedResidence res;
    private Residence plugin;

    public ResidenceItemList(Residence plugin, ClaimedResidence parent, ListType type) {
	super(type);
	this.plugin = plugin;
	res = parent;
    }

    private ResidenceItemList(Residence plugin) {
	this.plugin = plugin;
    }

    public void playerListChange(Player player, Material mat, boolean resadmin) {

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (resadmin || (res.getPermissions().hasResidencePermission(player, true) && group.itemListAccess())) {
	    if (super.toggle(mat))
		plugin.msg(player, lm.General_ListMaterialAdd, mat.toString(), type.toString().toLowerCase());
	    else
		plugin.msg(player, lm.General_ListMaterialRemove, mat.toString(), type.toString().toLowerCase());
	} else {
	    plugin.msg(player, lm.General_NoPermission);
	}
    }

    public static ResidenceItemList load(Residence plugin, ClaimedResidence parent, Map<String, Object> map) {
	ResidenceItemList newlist = new ResidenceItemList(plugin);
	newlist.res = parent;
	return (ResidenceItemList) ItemList.load(map, newlist);
    }
}
