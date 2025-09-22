package com.bekvon.bukkit.residence.utils;

import java.util.HashMap;
import java.util.UUID;

import com.bekvon.bukkit.residence.containers.DelayTeleport;

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
}
