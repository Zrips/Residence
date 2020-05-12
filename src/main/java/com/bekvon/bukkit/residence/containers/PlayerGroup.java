package com.bekvon.bukkit.residence.containers;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class PlayerGroup {

    ResidencePlayer resPlayer;
    long lastCheck = 0L;
    HashMap<String, PermissionGroup> groups = new HashMap<String, PermissionGroup>();

    public PlayerGroup(ResidencePlayer resPlayer) {
        this.resPlayer = resPlayer;
        Player player = resPlayer.getPlayer();
        if (player != null)
            updateGroup(player.getWorld().getName(), true);
    }

    public void setLastCkeck(Long time) {
        this.lastCheck = time;
    }

    public void addGroup(String world, PermissionGroup group) {
        groups.put(world, group);
    }

    public PermissionGroup getGroup(String world) {
        updateGroup(world, false);
        return this.groups.get(world);
    }

    public void updateGroup(String world, boolean force) {
        if (!force && this.lastCheck != 0L && System.currentTimeMillis() - this.lastCheck < 60 * 1000)
            return;

        this.lastCheck = System.currentTimeMillis();
        List<PermissionGroup> posibleGroups = new ArrayList<PermissionGroup>();
        String group;
        if (Residence.getInstance().getPermissionManager().getPlayersGroups().containsKey(resPlayer.getName().toLowerCase())) {
            group = Residence.getInstance().getPermissionManager().getPlayersGroups().get(resPlayer.getName().toLowerCase());
            if (group != null) {
                group = group.toLowerCase();
                if (group != null && Residence.getInstance().getPermissionManager().getGroups().containsKey(group)) {
                    PermissionGroup g = Residence.getInstance().getPermissionManager().getGroups().get(group);
                    posibleGroups.add(g);
                    this.groups.put(world, g);
                }
            }
        }

        posibleGroups.add(getPermissionGroup());

        group = Residence.getInstance().getPermissionManager().getPermissionsGroup(resPlayer.getName(), world);

        PermissionGroup g = Residence.getInstance().getPermissionManager().getGroupByName(group);

        if (g != null)
            posibleGroups.add(g);

        PermissionGroup finalGroup = null;
        if (posibleGroups.size() == 1)
            finalGroup = posibleGroups.get(0);

        for (int i = 0; i < posibleGroups.size(); i++) {
            if (finalGroup == null) {
                finalGroup = posibleGroups.get(i);
                continue;
            }

            if (finalGroup.getPriority() < posibleGroups.get(i).getPriority())
                finalGroup = posibleGroups.get(i);
        }

        if (finalGroup == null || !Residence.getInstance().getPermissionManager().getGroups().containsValue(finalGroup)) {
            this.groups.put(world, Residence.getInstance().getPermissionManager().getDefaultGroup());
        } else {
            this.groups.put(world, finalGroup);
        }
    }

    private PermissionGroup getPermissionGroup() {
        Player player = resPlayer.getPlayer();
        PermissionGroup group = Residence.getInstance().getPermissionManager().getDefaultGroup();
        for (Entry<String, PermissionGroup> one : Residence.getInstance().getPermissionManager().getGroups().entrySet()) {
            if (player != null) {
                if (ResPerm.group_$1.hasPermission(player, one.getKey())) {
                    group = one.getValue();
                }
            } else {
                OfflinePlayer offlineP = Residence.getInstance().getOfflinePlayer(resPlayer.getPlayerName());
                if (offlineP != null)
                    if (ResidenceVaultAdapter.hasPermission(offlineP, ResPerm.group_$1.getPermission(one.getKey()), Residence.getInstance().getConfigManager().getDefaultWorld()))
                        group = one.getValue();
            }
        }
        return group;
    }

}
