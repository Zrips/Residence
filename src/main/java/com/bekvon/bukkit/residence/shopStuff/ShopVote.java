package com.bekvon.bukkit.residence.shopStuff;

import java.util.UUID;

public class ShopVote {

    String name = null;
    int vote = -1;
    long time = 0L;
    private UUID uuid = null;

    public ShopVote(String name, UUID uuid, int vote, long time) {
        this.name = name;
        this.uuid = uuid;
        this.vote = vote;
        this.time = time;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
