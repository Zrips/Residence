package com.bekvon.bukkit.residence.utils;

import java.util.ArrayDeque;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.playerTempData;

import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class PlayerLocationChecker {

    private final ArrayDeque<UUID> queue = new ArrayDeque<>();

    public PlayerLocationChecker() {
    }

    public void start() {
        CMIScheduler.scheduleSyncRepeatingTask(Residence.getInstance(), () -> tick(), 1L, 1L);
    }

    private void tick() {
        if (queue.isEmpty()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                queue.add(p.getUniqueId());
            }
        }

        // Increase cycle count if we have more players online to avoid delays between
        // checks, which should happen no less than once every second for each player
        int cycles = ((queue.size() - 1) / 20) + 1;

        for (int i = 0; i < cycles; i++)
            poolNextPlayer();

    }

    private boolean poolNextPlayer() { 

        if (queue.isEmpty())
            return false;

        UUID uuid = queue.poll();
        if (uuid == null)
            return true;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            if (player != null)
                playerTempData.get(player).setLastLocation(null);
            return true;
        }

        playerTempData playerData = playerTempData.get(player);

        Long time = playerData.getLastCheck();

        if (time + 1000L > System.currentTimeMillis())
            return true;

        playerData.setLastCheck(System.currentTimeMillis());

        CMIScheduler.runAtLocation(Residence.getInstance(), player.getLocation(), () -> {

            if (player == null || !player.isOnline())
                return;

            Location current = player.getLocation();
            Location previous = playerData.getLastLocation(player.getLocation());

            if (previous == null || hasChanged(previous, current)) {
                onLocationChange(player, previous, current);
            }

            playerData.setLastLocation(current);
        });
        return true;
    }

    private boolean hasChanged(Location from, Location to) {
        if (!from.getWorld().equals(to.getWorld()))
            return true;
        if (from.getBlockX() != to.getBlockX())
            return true;
        if (from.getBlockY() != to.getBlockY())
            return true;
        if (from.getBlockZ() != to.getBlockZ())
            return true;
        return false;
    }

    private void onLocationChange(Player player, Location from, Location to) {
        Residence.getInstance().getPlayerListener().handleNewLocation(player, to, true);
    }
}
