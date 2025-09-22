package com.bekvon.bukkit.residence.shopStuff;

import java.util.UUID;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;

public class ShopVote {

    private UUID uuid = null;
    int vote = -1;
    long time = 0L;

    public ShopVote(UUID uuid, int vote, long time) {
        this.uuid = uuid;
        this.vote = vote;
        this.time = time;
    }

    public String getName() {
        return ResidencePlayer.getName(uuid);
    }

    public int getVote() {
        return this.vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public long getTime() {
        if (this.time == 0)
            this.time = System.currentTimeMillis();
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
