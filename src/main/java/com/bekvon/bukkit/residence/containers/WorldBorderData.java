package com.bekvon.bukkit.residence.containers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;

import com.bekvon.bukkit.residence.protection.CuboidArea;

import net.Zrips.CMILib.Version.Version;

public class WorldBorderData {

    public double centerX = 0;
    public double centerZ = 0;
    public double size = Double.MAX_VALUE;
    public double minX = Double.MIN_VALUE;
    public double maxX = Double.MAX_VALUE;
    public double minZ = Double.MIN_VALUE;
    public double maxZ = Double.MAX_VALUE;

    public WorldBorderData(World world) {

        if (Version.isCurrentEqualOrLower(Version.v1_7_10)) {
            return;
        }

        WorldBorder border = world.getWorldBorder();

        Location center = border.getCenter();
        size = border.getSize();

        double radius = size / 2.0;

        minX = center.getX() - radius;
        maxX = center.getX() + radius - 1;
        minZ = center.getZ() - radius;
        maxZ = center.getZ() + radius - 1;
    }

    public boolean isInside(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public WorldBorderData clampCuboidToBorder(CuboidArea cuboid) {

        double minX = Math.max(cuboid.getLowVector().getX(), this.minX);
        double maxX = Math.min(cuboid.getHighVector().getX(), this.maxX);

        double minZ = Math.max(cuboid.getLowVector().getZ(), this.minZ);
        double maxZ = Math.min(cuboid.getHighVector().getZ(), this.maxZ);

        // check invalid intersection (fully outside or inverted)
        if (minX > maxX || minZ > maxZ) {
            cuboid.getLowVector().setX(0);
            cuboid.getLowVector().setY(0);
            cuboid.getLowVector().setZ(0);

            cuboid.getHighVector().setX(0);
            cuboid.getHighVector().setY(0);
            cuboid.getHighVector().setZ(0);
            return this;
        }

        cuboid.getLowVector().setX(minX);
        cuboid.getHighVector().setX(maxX);

        cuboid.getLowVector().setZ(minZ);
        cuboid.getHighVector().setZ(maxZ);
        return this;
    }
}
