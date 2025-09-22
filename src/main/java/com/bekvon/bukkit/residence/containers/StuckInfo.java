package com.bekvon.bukkit.residence.containers;

public class StuckInfo {

    private int times = 0;
    private long lastTp = 0L;

    public StuckInfo() {
        times++;
        lastTp = System.currentTimeMillis();
    }

    public int getTimesTeleported() {
        return times;
    }

    public void addTimeTeleported() {
        this.times++;
    }

    public long getLastTp() {
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
