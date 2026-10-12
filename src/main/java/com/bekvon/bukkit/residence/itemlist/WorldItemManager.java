package com.bekvon.bukkit.residence.itemlist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.Items.CMIMaterial;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

public class WorldItemManager {
    protected List<WorldItemList> lists = new ArrayList<WorldItemList>();
    private Residence plugin;

    public WorldItemManager(Residence plugin) {
        this.plugin = plugin;
    }

    public boolean isAllowed(Material mat, Player player) {
        if (!CMIMaterial.isValidItem(mat)) {
            return true;
        }
        PermissionGroup group = plugin.getPlayerManager().getResidencePlayer(player).getGroup();
        if (group == null) {
            return true;
        }
        String worldName = player.getWorld().getName();
        String groupName = group.getGroupName();

        for (WorldItemList list : lists) {
            if (!list.isAllowed(mat, worldName, groupName)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIgnored(Player player, Material mat, World world) {
        PermissionGroup group = plugin.getPlayerManager().getResidencePlayer(player).getGroup();
        if (group == null) {
            return false;
        }
        String worldName = world.getName();
        String groupName = group.getGroupName();

        for (WorldItemList list : lists) {
            if (list.isIgnored(mat, worldName, groupName)) {
                return true;
            }
        }
        return false;
    }

    public void load() {
        lists.clear();
        loadLists();
    }

    private void loadLists() {
        FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "flags.yml"));
        if (!flags.isConfigurationSection("ItemList"))
            return;
        Set<String> keys = flags.getConfigurationSection("ItemList").getKeys(false);
        if (keys == null)
            return;

        for (String key : keys) {
            try {
                WorldItemList list = WorldItemList.readList(flags.getConfigurationSection("ItemList." + key));
                lists.add(list);
                // System.out.println("Debug: read list " + key + " world: " + list.getWorld() +
                // " group: " + list.getGroup() + " itemcount:" + list.getListSize());
            } catch (Exception ex) {
                System.out.println("Failed to load item list:" + key);
            }
        }
    }
}
