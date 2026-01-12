package com.bekvon.bukkit.residence.protection;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.Colors.CMIChatColor;

public class PermissionListManager {

    private final Map<String, Map<String, FlagPermissions>> lists;
    private Residence plugin;

    public PermissionListManager(Residence residence) {
        this.plugin = residence;
        lists = Collections.synchronizedMap(new HashMap<String, Map<String, FlagPermissions>>());
    }

    public FlagPermissions getList(String player, String listname) {
        Map<String, FlagPermissions> get = lists.get(player);
        if (get == null) {
            return null;
        }
        return get.get(listname);
    }

    public void makeList(Player player, String listname) {
        Map<String, FlagPermissions> get = lists.get(player.getName());
        if (get == null) {
            get = new HashMap<>();
            lists.put(player.getName(), get);
        }
        FlagPermissions perms = get.get(listname);
        if (perms == null) {
            perms = new FlagPermissions();
            get.put(listname, perms);
            lm.General_ListCreate.sendMessage(player, listname);
        } else {
            lm.General_ListExists.sendMessage(player);
        }
    }

    public void removeList(Player player, String listname) {
        Map<String, FlagPermissions> get = lists.get(player.getName());
        if (get == null) {
            lm.Invalid_List.sendMessage(player);
            return;
        }
        FlagPermissions list = get.get(listname);
        if (list == null) {
            lm.Invalid_List.sendMessage(player);
            return;
        }
        get.remove(listname);
        lm.General_ListRemoved.sendMessage(player);
    }

    public void applyListToResidence(Player player, String listname, String areaname, boolean resadmin) {
        FlagPermissions list = this.getList(player.getName(), listname);
        if (list == null) {
            lm.Invalid_List.sendMessage(player);
            return;
        }
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }
        res.getPermissions().applyTemplate(player, list, resadmin);
    }

    public void printList(Player player, String listname) {
        FlagPermissions list = this.getList(player.getName(), listname);
        if (list == null) {
            lm.Invalid_List.sendMessage(player);
            return;
        }
        player.sendMessage(CMIChatColor.LIGHT_PURPLE + "------Permission Template------");
        lm.General_Name.sendMessage(player, listname);
        list.printFlags(player);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, Object> save() {
        Map root = new LinkedHashMap<>();
        for (Entry<String, Map<String, FlagPermissions>> players : lists.entrySet()) {
            Map saveMap = new LinkedHashMap<>();
            Map<String, FlagPermissions> map = players.getValue();
            for (Entry<String, FlagPermissions> list : map.entrySet()) {
                saveMap.put(list.getKey(), list.getValue().save(null));
            }
            root.put(players.getKey(), saveMap);
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    public PermissionListManager load(Map<String, Object> root) {

        PermissionListManager p = new PermissionListManager(plugin);
        if (root != null) {
            for (Entry<String, Object> players : root.entrySet()) {
                try {
                    Map<String, Object> value = (Map<String, Object>) players.getValue();
                    Map<String, FlagPermissions> loadedMap = Collections.synchronizedMap(new HashMap<String, FlagPermissions>());
                    for (Entry<String, Object> list : value.entrySet()) {
                        loadedMap.put(list.getKey(), FlagPermissions.load((Map<String, Object>) list.getValue()));
                    }
                    p.lists.put(players.getKey(), loadedMap);
                } catch (Exception ex) {
                    System.out.println("[Residence] - Failed to load permission lists for player: " + players.getKey());
                }
            }
        }
        return p;
    }

    public void printLists(Player player) {
        StringBuilder sbuild = new StringBuilder();
        Map<String, FlagPermissions> get = lists.get(player.getName());
        sbuild.append(lm.General_Lists.getMessage());
        if (get != null) {
            for (Entry<String, FlagPermissions> thislist : get.entrySet()) {
                sbuild.append(thislist.getKey()).append(" ");
            }
        }
        player.sendMessage(sbuild.toString());
    }
}
