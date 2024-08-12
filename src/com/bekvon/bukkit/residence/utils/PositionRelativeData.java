package com.bekvon.bukkit.residence.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;

import net.Zrips.CMILib.Container.CMIWorld;

public class PositionRelativeData {

    private int localX = 0;
    private int localY = 0;
    private int localZ = 0;

    private int maxWorldY = 0;
    private int minWorldY = 0;

    public PositionRelativeData(World world, int localX, int localY, int localZ) {
        this.localX = localX;
        this.localY = localY;
        this.localZ = localZ;
        maxWorldY = CMIWorld.getMaxHeight(world); 
        minWorldY = CMIWorld.getMinHeight(world);

        if (world.getEnvironment().equals(Environment.NETHER) && maxWorldY < 200) {
            maxWorldY = 255;
        }
    }

    public PositionRelativeData(Location loc) {
        this(loc.getWorld(), loc.getBlockX() & 0xF, loc.getBlockY(), loc.getBlockZ() & 0xF);
    }

    public int getLocalX() {
        return localX;
    }

    public int getLocalZ() {
        return localZ;
    }

    public int getMaxWorldY() {
        return maxWorldY;
    }

    public int getMinWorldY() {
        return minWorldY;
    }

    public int getLocalY() {
        return localY;
    }
}
