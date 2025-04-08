package com.bekvon.bukkit.residence.containers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ResidencePlayerMaxValues {

    private static ConcurrentHashMap<UUID, ResidencePlayerMaxValues> data = new ConcurrentHashMap<UUID, ResidencePlayerMaxValues>();

    public static ResidencePlayerMaxValues get(UUID uuid) {
        return data.computeIfAbsent(uuid, k -> new ResidencePlayerMaxValues());
    }

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;
    private int maxSubzoneDepth = -1;
    private int maxX = -1;
    private int maxZ = -1;

    public int getMaxRes() {
        return maxRes;
    }

    public void setMaxRes(int maxRes) {
        this.maxRes = maxRes;
    }

    public int getMaxRents() {
        return maxRents;
    }

    public void setMaxRents(int maxRents) {
        this.maxRents = maxRents;
    }

    public int getMaxSubzones() {
        return maxSubzones;
    }

    public void setMaxSubzones(int maxSubzones) {
        this.maxSubzones = maxSubzones;
    }

    public int getMaxSubzoneDepth() {
        return maxSubzoneDepth;
    }

    public void setMaxSubzoneDepth(int maxSubzoneDepth) {
        this.maxSubzoneDepth = maxSubzoneDepth;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

}
