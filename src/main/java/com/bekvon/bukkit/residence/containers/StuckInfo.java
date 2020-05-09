package com.bekvon.bukkit.residence.containers;

import org.bukkit.entity.Player;

public class StuckInfo {

    private Player player;
    private int times = 0;
    private Long lastTp = 0L;

    public StuckInfo(Player player) {
        this.player = player;
        times++;
        lastTp = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public int getTimesTeleported() {
        return times;
    }

    public void addTimeTeleported() {
        this.times++;
    }

    public Long getLastTp() {
        return lastTp;
    }

    public void updateLastTp() {
        if (System.currentTimeMillis() - this.lastTp > 1000) {
            this.times = 0;
        }
        addTimeTeleported();
        this.lastTp = System.currentTimeMillis();
    }

}
