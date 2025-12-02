package com.bekvon.bukkit.residence.utils;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.DelayTeleport;

import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class Teleporting {

    public static HashMap<UUID, DelayTeleport> teleportDelayMap = new HashMap<UUID, DelayTeleport>();

    public static HashMap<UUID, DelayTeleport> getTeleportDelayMap() {
        return teleportDelayMap;
    }

    public static void cancelTeleportDelay(UUID uuid) {

        DelayTeleport tpDelayRecord = teleportDelayMap.remove(uuid);

        if (tpDelayRecord == null)
            return;
        if (tpDelayRecord.getMessageTask() != null)
            tpDelayRecord.getMessageTask().cancel();
        if (tpDelayRecord.getTeleportTask() != null)
            tpDelayRecord.getTeleportTask().cancel();
    }

    public static DelayTeleport getTeleportDelay(UUID uuid) {
        return teleportDelayMap.get(uuid);
    }

    public static boolean isUnderTeleportDelay(UUID uuid) {
        return teleportDelayMap.containsKey(uuid);
    }

    public static DelayTeleport getOrCreateTeleportDelay(UUID uuid) {
        return teleportDelayMap.computeIfAbsent(uuid, k -> new DelayTeleport());
    }

    public static void addTeleportDelay(UUID uuid, DelayTeleport tpDelayRecord) {
        teleportDelayMap.put(uuid, tpDelayRecord);
    }

    public static CompletableFuture<Boolean> teleport(Player player, Location loc) {
        return teleport(player, loc, TeleportCause.PLUGIN);
    }

    public static CompletableFuture<Boolean> teleport(Player player, Location loc, TeleportCause cause) {

        if (player == null || !player.isOnline() || loc == null)
            return CompletableFuture.completedFuture(false);

        return teleport((Entity) player, loc, cause);
    }

    public static CompletableFuture<Boolean> teleport(Entity entity, Location loc) {
        return teleport(entity, loc, TeleportCause.PLUGIN);
    }

    public static CompletableFuture<Boolean> teleport(Entity entity, Location loc, TeleportCause cause) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            CMIScheduler.runTask(Residence.getInstance(), () -> {
                PaperLib.teleportAsync(entity, loc, cause)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                future.completeExceptionally(ex);
                            } else {
                                future.complete(result);
                            }
                        });
            });
        } catch (Throwable e) {
            future.completeExceptionally(e);
        }
        return future;
    }
}
