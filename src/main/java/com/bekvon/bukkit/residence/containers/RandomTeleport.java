package com.bekvon.bukkit.residence.containers;

import org.bukkit.Location;
import org.bukkit.World;

public class RandomTeleport {
    int MaxCord;
    int MinCord;
    Location loc;

    public RandomTeleport(World world, int MaxCord, int MinCord, int centerX, int centerZ) {
	this.loc = new Location(world, centerX, world.getMaxHeight() / 2, centerZ);
	this.MaxCord = MaxCord;
	this.MinCord = MinCord;
    }

    public Location getCenter() {
	return this.loc;
    }

    public int getMaxCord() {
	return this.MaxCord;
    }

    public int getMinCord() {
	return this.MinCord;
    }
}
