package com.bekvon.bukkit.residence.protection;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;

public class WorldFlagManager {
    protected Map<String, Map<String, FlagPermissions>> groupperms;
    protected Map<String, FlagPermissions> worldperms;
    protected FlagPermissions globaldefaults;
    private Residence plugin;

    public WorldFlagManager(Residence plugin) {
        this.plugin = plugin;
    }

    public void load() {
        globaldefaults = new FlagPermissions();
        worldperms = new HashMap<>();
        groupperms = new HashMap<>();
        this.parsePerms();
    }

    public FlagPermissions getPerms(Player player) {
        ResidencePlayer resPlayer = ResidencePlayer.get(player);
        return this.getPerms(player.getWorld().getName(), resPlayer.getGroup().getGroupName());
    }

    public FlagPermissions getPerms(String world, String group) {
        world = world.toLowerCase();
        group = group.toLowerCase();
        Map<String, FlagPermissions> groupworldperms = groupperms.get(group);
        if (groupworldperms == null) {
            return this.getPerms(world);
        }
        FlagPermissions list = groupworldperms.get(world);
        if (list == null) {
            list = groupworldperms.get("global." + world);
            if (list == null) {
                list = groupworldperms.get("global");
            }
            if (list == null) {
                return this.getPerms(world);
            }
        }
        return list;
    }

    public FlagPermissions getPerms(World world) {
        return this.getPerms(world.getName());
    }

    public FlagPermissions getPerms(String world) {
        world = world.toLowerCase();
        FlagPermissions list = worldperms.get(world);
        if (list == null) {
            if (globaldefaults == null)
                return new FlagPermissions();
            return globaldefaults;
        }
        return list;
    }

    public final void parsePerms() {
        try {
            FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "flags.yml"));
            FileConfiguration groups = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "groups.yml"));

            if (flags.isConfigurationSection("Global.Flags")) {
                Set<String> keys = flags.getConfigurationSection("Global.Flags").getKeys(false);
                if (keys != null) {
                    for (String key : keys) {
                        if (key.equalsIgnoreCase("Global")) {
                            globaldefaults = FlagPermissions.parseFromConfigNode(key, flags.getConfigurationSection("Global.Flags"));
                        } else {
                            worldperms.put(key.toLowerCase(), FlagPermissions.parseFromConfigNode(key, flags.getConfigurationSection("Global.Flags")));
                        }
                    }
                }
            }

            try {
                if (flags.isConfigurationSection("Global.CommandLimits")) {
                    Set<String> keys = flags.getConfigurationSection("Global.CommandLimits").getKeys(false);
                    if (keys != null) {
                        for (String key : keys) {
                            if (key.equalsIgnoreCase("Global") && globaldefaults != null) {
                                globaldefaults.parseCommandLimits(flags.getConfigurationSection("Global.CommandLimits." + key));
                            } else {
                                FlagPermissions existing = worldperms.get(key.toLowerCase());
                                if (existing != null)
                                    existing.parseCommandLimits(flags.getConfigurationSection("Global.CommandLimits." + key));
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            for (Entry<String, FlagPermissions> entry : worldperms.entrySet()) {
                entry.getValue().setParent(globaldefaults);
            }

            if (!groups.isConfigurationSection("Groups")) {
                lm.consoleMessage(ChatColor.RED + "Your groups.yml file is incorrect!");
                return;
            }

            if (flags.isConfigurationSection("Groups")) {
                Set<String> keys = groups.getConfigurationSection("Groups").getKeys(false);
                if (keys != null) {
                    for (String key : keys) {
                        if (!groups.contains("Groups." + key + ".Flags"))
                            continue;
                        if (!groups.contains("Groups." + key + ".Flags.World"))
                            continue;
                        if (key == null)
                            continue;
                        Set<String> worldkeys = groups.getConfigurationSection("Groups." + key + ".Flags.World").getKeys(false);

                        if (worldkeys == null)
                            continue;

                        Map<String, FlagPermissions> perms = new HashMap<>();
                        for (String wkey : worldkeys) {
                            FlagPermissions list = FlagPermissions.parseFromConfigNode(wkey, groups.getConfigurationSection("Groups." + key + ".Flags.World"));
                            if (wkey.equalsIgnoreCase("global")) {
                                list.setParent(globaldefaults);
                                perms.put(wkey.toLowerCase(), list);
                                for (Entry<String, FlagPermissions> worldperm : worldperms.entrySet()) {
                                    list = FlagPermissions.parseFromConfigNode(wkey, groups.getConfigurationSection("Groups." + key + ".Flags.World"));
                                    list.setParent(worldperm.getValue());
                                    perms.put("global." + worldperm.getKey().toLowerCase(), list);
                                }
                            } else {
                                perms.put(wkey.toLowerCase(), list);
                            }
                        }
                        for (Entry<String, FlagPermissions> entry : perms.entrySet()) {
                            String wkey = entry.getKey();
                            FlagPermissions list = entry.getValue();
                            if (!wkey.startsWith("global.")) {
                                list.setParent(perms.get("global." + wkey));
                                if (list.getParent() == null) {
                                    list.setParent(worldperms.get(wkey));
                                }
                                if (list.getParent() == null) {
                                    list.setParent(globaldefaults);
                                }
                            }
                        }
                        groupperms.put(key.toLowerCase(), perms);

                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(WorldFlagManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
