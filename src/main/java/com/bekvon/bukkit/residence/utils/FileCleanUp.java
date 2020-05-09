package com.bekvon.bukkit.residence.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class FileCleanUp {

    private Residence plugin;

    public FileCleanUp(Residence plugin) {
        this.plugin = plugin;
    }

    public void cleanFiles() {

        Map<String, ClaimedResidence> resNameList = new HashMap<String, ClaimedResidence>(plugin.getResidenceManager().getResidences());
        int i = 0;

        OfflinePlayer[] offplayer = Bukkit.getOfflinePlayers();

        HashMap<UUID, OfflinePlayer> playerMapUUID = new HashMap<UUID, OfflinePlayer>();
        HashMap<String, OfflinePlayer> playerMapNane = new HashMap<String, OfflinePlayer>();

        for (OfflinePlayer one : offplayer) {
            playerMapUUID.put(one.getUniqueId(), one);
            playerMapNane.put(one.getName(), one);
        }

        int interval = plugin.getConfigManager().getResidenceFileCleanDays();
        long time = System.currentTimeMillis();

        Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Starting auto CleanUp (" + playerMapUUID.size() + "/" + resNameList.size() + ")!");

        for (Entry<String, ClaimedResidence> oneName : resNameList.entrySet()) {
            ClaimedResidence res = oneName.getValue();
            if (res == null)
                continue;

            OfflinePlayer player = playerMapUUID.get(res.getOwnerUUID());

            if (player == null)
                player = playerMapNane.get(res.getOwner());

            if (player == null)
                continue;

            if (!plugin.getConfigManager().getCleanWorlds().contains(res.getWorld()))
                continue;

            if (res.getOwner().equalsIgnoreCase("server land") || res.getOwner().equalsIgnoreCase(plugin.getServerLandName()))
                continue;

            long lastPlayed = player.getLastPlayed();
            int dif = (int) ((time - lastPlayed) / 1000 / 60 / 60 / 24);
            if (dif < interval)
                continue;

            if (ResidenceVaultAdapter.hasPermission(player, ResPerm.cleanbypass.getPermission(), res.getWorld()))
                continue;

            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player.getUniqueId());

            plugin.getResidenceManager().removeResidence(rPlayer, oneName.getValue(), true, plugin.getConfigManager().isAutoCleanUpRegenerate());
            i++;
        }
        Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Auto CleanUp deleted " + i + " residences!");
    }
}
