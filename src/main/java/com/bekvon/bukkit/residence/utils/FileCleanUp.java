package com.bekvon.bukkit.residence.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.LuckPerms5Adapter;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Logs.CMIDebug;

public class FileCleanUp {

    private Residence plugin;

    public FileCleanUp(Residence plugin) {
        this.plugin = plugin;
    }

    public void cleanOldResidence() {

        Map<String, ClaimedResidence> resNameList = new HashMap<String, ClaimedResidence>(plugin.getResidenceManager().getResidences());
        int i = 0;

        OfflinePlayer[] offplayer = Bukkit.getOfflinePlayers();

        HashMap<UUID, OfflinePlayer> playerMapUUID = new HashMap<UUID, OfflinePlayer>();

        boolean lp = plugin.getPermissionManager().getPermissionsPlugin() instanceof LuckPerms5Adapter;

        for (OfflinePlayer one : offplayer) {
            playerMapUUID.put(one.getUniqueId(), one);
            if (lp) {
                LuckPerms5Adapter.loadUser(one.getUniqueId());
            }
        }

        int interval = plugin.getConfigManager().getResidenceFileCleanDays();
        long time = System.currentTimeMillis();

        Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Starting auto CleanUp (" + playerMapUUID.size() + "/" + resNameList.size() + ")!");

        int skipped = 0;
        try {
            for (Entry<String, ClaimedResidence> oneName : resNameList.entrySet()) {
                ClaimedResidence res = oneName.getValue();
                if (res == null)
                    continue;

                OfflinePlayer player = playerMapUUID.get(res.getOwnerUUID());

                if (player == null) {
                    skipped++;
                    if (plugin.getConfigManager().isAutoCleanDetailsOnUnknown())
                        Bukkit.getConsoleSender().sendMessage("Skipping " + res.getName() + " residence owned by " + res.getOwner() + " (" + res.getOwnerUUID() + ")");
                    continue;
                }

                if (!plugin.getConfigManager().getAutoCleanUpWorlds().contains(res.getPermissions().getWorldName().toLowerCase()))
                    continue;

                if (res.getOwner().equalsIgnoreCase("server land") || res.getOwner().equalsIgnoreCase(plugin.getServerLandName()))
                    continue;

                if (res.getOwner().equalsIgnoreCase(plugin.getConfigManager().getAutoCleanUserName()))
                    continue;

                long lastPlayed = player.getLastPlayed();
                int dif = (int) ((time - lastPlayed) / 1000 / 60 / 60 / 24);
                if (dif < interval)
                    continue;

                if (plugin.getPermissionManager().getPermissionsPlugin().hasPermission(player, ResPerm.cleanbypass.getPermission(), res.getPermissions().getWorldName().toLowerCase()))
                    continue;

                ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player.getUniqueId());

                if (plugin.getConfigManager().isAutoCleanTrasnferToUser() && !res.getOwner().equalsIgnoreCase(plugin.getConfigManager().getAutoCleanUserName())) {
                    res.getPermissions().setOwner(plugin.getConfigManager().getAutoCleanUserName(), true);
                    if (plugin.getRentManager().isForRent(res))
                        plugin.getRentManager().removeRentable(res);
                    if (plugin.getTransactionManager().isForSale(res))
                        plugin.getTransactionManager().removeFromSale(res);
                } else
                    plugin.getResidenceManager().removeResidence(rPlayer, oneName.getValue(), true, plugin.getConfigManager().isAutoCleanUpRegenerate());
                i++;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Auto CleanUp deleted " + i + " residences!");
        if (skipped > 0)
            Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Skipped " + skipped + " residences due to inability to determine residence owner.");
    }
}
