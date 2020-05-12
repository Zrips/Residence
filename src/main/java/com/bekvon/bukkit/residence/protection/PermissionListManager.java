package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
            plugin.msg(player, lm.General_ListCreate, listname);
        } else {
            plugin.msg(player, lm.General_ListExists);
        }
    }

    public void removeList(Player player, String listname) {
        Map<String, FlagPermissions> get = lists.get(player.getName());
        if (get == null) {
            plugin.msg(player, lm.Invalid_List);
            return;
        }
        FlagPermissions list = get.get(listname);
        if (list == null) {
            plugin.msg(player, lm.Invalid_List);
            return;
        }
        get.remove(listname);
        plugin.msg(player, lm.General_ListRemoved);
    }

    public void applyListToResidence(Player player, String listname, String areaname, boolean resadmin) {
        FlagPermissions list = this.getList(player.getName(), listname);
        if (list == null) {
            plugin.msg(player, lm.Invalid_List);
            return;
        }
        ClaimedResidence res = plugin.getResidenceManager().getByName(areaname);
        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }
        res.getPermissions().applyTemplate(player, list, resadmin);
    }

    public void printList(Player player, String listname) {
        FlagPermissions list = this.getList(player.getName(), listname);
        if (list == null) {
            plugin.msg(player, lm.Invalid_List);
            return;
        }
        player.sendMessage(ChatColor.LIGHT_PURPLE + "------Permission Template------");
        plugin.msg(player, lm.General_Name, listname);
        list.printFlags(player);
    }


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
        sbuild.append(plugin.msg(lm.General_Lists));
        if (get != null) {
            for (Entry<String, FlagPermissions> thislist : get.entrySet()) {
                sbuild.append(thislist.getKey()).append(" ");
            }
        }
        player.sendMessage(sbuild.toString());
    }
}
