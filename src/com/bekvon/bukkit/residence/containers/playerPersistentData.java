package com.bekvon.bukkit.residence.containers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class playerPersistentData {

    protected static ConcurrentHashMap<UUID, playerPersistentData> playersData = new ConcurrentHashMap<UUID, playerPersistentData>();

    private boolean chatEnabled = true;
    private Location lastOutsideLoc = null;

    public Location getLastOutsideLoc() {
        return lastOutsideLoc;
    }

    public void setLastOutsideLoc(Location lastOutsideLoc) {
        this.lastOutsideLoc = lastOutsideLoc;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public static playerPersistentData get(Player player) {
        return get(player.getUniqueId());
    }

    public static playerPersistentData get(UUID uuid) {
        return playersData.computeIfAbsent(uuid, k -> new playerPersistentData());
    }

    public static void remove(UUID uuid) {
        playersData.remove(uuid);
    }
}
