package com.bekvon.bukkit.residence.permissions;

import com.bekvon.bukkit.residence.Residence;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import org.bukkit.entity.Player;

import java.util.List;

public class PermissionsBukkitAdapter implements PermissionsInterface {

    PermissionsPlugin newperms;

    public PermissionsBukkitAdapter(PermissionsPlugin p) {
        newperms = p;
    }

    @Override
    public String getPlayerGroup(Player player) {
        return this.getPlayerGroup(player.getName(), player.getWorld().getName());
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        PermissionManager pmanager = Residence.getInstance().getPermissionManager();
        List<Group> groups = newperms.getGroups(player);
        for (Group group : groups) {
            String name = group.getName().toLowerCase();
            if (pmanager.hasGroup(name)) {
                return name;
            }
        }
        if (groups.size() > 0) {
            return groups.get(0).getName().toLowerCase();
        }
        return null;
    }

}
