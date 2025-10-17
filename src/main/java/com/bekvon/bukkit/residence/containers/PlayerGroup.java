package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;

public class PlayerGroup {

    private ResidencePlayer resPlayer;
    private long lastCheck = 0L;
    private HashMap<String, PermissionGroup> rGroups = new HashMap<String, PermissionGroup>();

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
        if (group == null)
            return;
        rGroups.put(world.toLowerCase(), group);
    }

    public @Nullable PermissionGroup getGroup(boolean force) {
        String world = resPlayer.getPlayer() != null ? resPlayer.getPlayer().getWorld().getName() : resPlayer.getLastKnownWorld() != null ? resPlayer.getLastKnownWorld() : Residence.getInstance()
            .getConfigManager().getDefaultWorld();
        return getGroup(world, force);
    }

    public @Nullable PermissionGroup getGroup(String world) {
        return getGroup(world, false);
    }

    public @Nullable PermissionGroup getGroup(String world, boolean force) {
        updateGroup(world, force);
        PermissionGroup gr = this.rGroups.get(world.toLowerCase());
        return gr != null ? gr : Residence.getInstance().getPermissionManager().getDefaultGroup();
    }

    public void updateGroup(String world, boolean force) {
        if (!force && this.lastCheck != 0L && System.currentTimeMillis() - this.lastCheck < 60 * 1000)
            return;

        this.lastCheck = System.currentTimeMillis();

        if (world == null || resPlayer == null || !resPlayer.isOnline())
            return;

        List<PermissionGroup> possibleGroups = new ArrayList<PermissionGroup>();
        String group = Residence.getInstance().getPermissionManager().getPlayersGroups().get(resPlayer.getUniqueId());
        if (group != null) {
            group = group.toLowerCase();
            PermissionGroup g = Residence.getInstance().getPermissionManager().getGroupByName(group);
            if (g != null) {
                possibleGroups.add(g);
                addGroup(world, g);
            }
        }

        possibleGroups.add(getPermissionGroup());

        group = Residence.getInstance().getPermissionManager().getPermissionsGroup(resPlayer.getUniqueId(), world);

        PermissionGroup g = Residence.getInstance().getPermissionManager().getGroupByName(group);

        if (g != null)
            possibleGroups.add(g);

        PermissionGroup finalGroup = null;
        if (possibleGroups.size() == 1)
            finalGroup = possibleGroups.get(0);

        for (int i = 0; i < possibleGroups.size(); i++) {
            if (finalGroup == null) {
                finalGroup = possibleGroups.get(i);
                continue;
            }

            if (finalGroup.getPriority() < possibleGroups.get(i).getPriority())
                finalGroup = possibleGroups.get(i);
        }

        if (finalGroup == null || !Residence.getInstance().getPermissionManager().getGroups().containsValue(finalGroup)) {
            addGroup(world, Residence.getInstance().getPermissionManager().getDefaultGroup());
        } else {
            addGroup(world, finalGroup);
        }
    }

    public PermissionGroup getPermissionGroup() {

        if (resPlayer.isOnline()) {
            Player player = resPlayer.getPlayer();
            PermissionGroup group = Residence.getInstance().getPermissionManager().getDefaultGroup();
            for (Entry<String, PermissionGroup> one : Residence.getInstance().getPermissionManager().getGroups().entrySet()) {
                if (player != null && ResPerm.group_$1.hasPermission(player, one.getKey())) {
                    group = one.getValue();
                }
            }

            return group;
        }

        PermissionGroup pg = getGroup(resPlayer.getLastKnownWorld() != null ? resPlayer.getLastKnownWorld() : Residence.getInstance().getConfigManager().getDefaultWorld());
        return pg == null ? Residence.getInstance().getPermissionManager().getDefaultGroup() : pg;
    }

    private static boolean isDefault(PermissionGroup group) {
        return Residence.getInstance().getPermissionManager().getDefaultGroup().equals(group);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> groups = new HashMap<String, Object>();

        for (Entry<String, PermissionGroup> one : rGroups.entrySet()) {
            if (!isDefault(one.getValue()))
                groups.put("RGroup." + one.getKey(), one.getValue().getGroupName());
        }

        return groups;
    }

    public static @Nullable PlayerGroup deserialize(ResidencePlayer rplayer, Map<String, Object> map) {

        if (!map.containsKey("PGroup") && !map.containsKey("RGroup"))
            return null;

        PlayerGroup group = rplayer.getGroups() != null ? rplayer.getGroups() : new PlayerGroup(rplayer);

        Object rgroups = map.get("RGroup");
        if (rgroups instanceof Map) {
            for (Entry<String, Object> one : ((Map<String, Object>) rgroups).entrySet()) {

                if (!(one.getValue() instanceof String))
                    continue;

                PermissionGroup g = Residence.getInstance().getPermissionManager().getGroupByName((String) one.getValue());
                if (g != null)
                    group.addGroup(one.getKey(), g);
            }
        }

        group.setLastCkeck(System.currentTimeMillis());

        return group;
    }

}
