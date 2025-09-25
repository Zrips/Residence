package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;

public class PlayerGroup {

    private static ConcurrentHashMap<UUID, PlayerGroup> playerGroups = new ConcurrentHashMap<UUID, PlayerGroup>();

    public static @Nullable PlayerGroup getPlayerGroup(@Nonnull UUID uuid) {
        return playerGroups.get(uuid);
    }

    public static void removePlayerGroup(@Nonnull UUID uuid) {
        playerGroups.remove(uuid);
    }

    public static void addPlayerGroup(@Nonnull UUID uuid, @Nonnull PlayerGroup group) {
        playerGroups.put(uuid, group);
    }

    ResidencePlayer resPlayer;
    long lastCheck = 0L;
    HashMap<String, PermissionGroup> rGroups = new HashMap<String, PermissionGroup>();
    HashMap<String, String> pGroups = new HashMap<String, String>();

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
        rGroups.put(world.toLowerCase(), group);
    }

    public @Nullable PermissionGroup getGroup(String world) {
        updateGroup(world, false);
        return this.rGroups.get(world.toLowerCase());
    }

    public @Nullable String getPermissionGroup(String world) {
        updateGroup(world, false);
        return this.pGroups.get(world.toLowerCase());
    }

    public void addPermissionGroup(String world, String group) {
        pGroups.put(world.toLowerCase(), group);
    }

    public void updateGroup(String world, boolean force) {
        if (!force && this.lastCheck != 0L && System.currentTimeMillis() - this.lastCheck < 60 * 1000)
            return;

        this.lastCheck = System.currentTimeMillis();

        if (world == null)
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

        addPermissionGroup(world, group);

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

    private PermissionGroup getPermissionGroup() {
        Player player = resPlayer.getPlayer();
        PermissionGroup group = Residence.getInstance().getPermissionManager().getDefaultGroup();
        for (Entry<String, PermissionGroup> one : Residence.getInstance().getPermissionManager().getGroups().entrySet()) {
            if (player != null && ResPerm.group_$1.hasPermission(player, one.getKey())) {
                group = one.getValue();
            }
        }
        return group;
    }

    private boolean isDefault(PermissionGroup group) {
        return Residence.getInstance().getPermissionManager().getDefaultGroup().equals(group);
    }

    private boolean isDefault(String group) {
        return Residence.getInstance().getPermissionManager().getDefaultGroup().getGroupName().equalsIgnoreCase(group);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> groups = new HashMap<String, Object>();

        for (Entry<String, PermissionGroup> one : rGroups.entrySet()) {
            if (!isDefault(one.getValue()))
                groups.put("RGroup." + one.getKey(), one.getValue().getGroupName());
        }

        for (Entry<String, String> one : pGroups.entrySet()) {
            if (!isDefault(one.getValue()))
                groups.put("PGroup." + one.getKey(), one.getValue());
        }

        return groups;
    }

    public static @Nullable PlayerGroup deserialize(ResidencePlayer rplayer, Map<String, Object> map) {

        if (!map.containsKey("PGroup") && !map.containsKey("RGroup"))
            return null;

        PlayerGroup group = new PlayerGroup(rplayer);

        Object pgroups = map.get("PGroup");

        if (pgroups instanceof Map) {
            for (Entry<String, Object> one : ((Map<String, Object>) pgroups).entrySet()) {
                if (!(one.getValue() instanceof String))
                    continue;
                group.addPermissionGroup(one.getKey(), (String) one.getValue());
            }
        }

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

        return null;
    }

}
