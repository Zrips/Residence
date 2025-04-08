package com.bekvon.bukkit.residence.containers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bekvon.bukkit.residence.raid.ResidenceRaid;

public class ResidencePlayerRaidData {

    private static ConcurrentHashMap<UUID, ResidencePlayerRaidData> data = new ConcurrentHashMap<UUID, ResidencePlayerRaidData>();

    public static ResidencePlayerRaidData get(UUID uuid) {
        return data.computeIfAbsent(uuid, k -> new ResidencePlayerRaidData());
    }

    private long lastRaidAttackTimer = 0L;
    private long lastRaidDefendTimer = 0L;
    private ResidenceRaid raid = null;

    public Long getLastRaidAttackTimer() {
        return lastRaidAttackTimer;
    }

    public void setLastRaidAttackTimer(long lastRaidAttackTimer) {
        this.lastRaidAttackTimer = lastRaidAttackTimer;
    }

    public Long getLastRaidDefendTimer() {
        return lastRaidDefendTimer;
    }

    public void setLastRaidDefendTimer(long lastRaidDefendTimer) {
        this.lastRaidDefendTimer = lastRaidDefendTimer;
    }

    public ResidenceRaid getJoinedRaid() {
        return raid;
    }

    public void setJoinedRaid(ResidenceRaid raid) {
        this.raid = raid;
    }
}
