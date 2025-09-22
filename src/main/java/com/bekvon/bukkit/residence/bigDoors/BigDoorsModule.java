package com.bekvon.bukkit.residence.bigDoors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import nl.pim16aap2.bigDoors.compatibility.IProtectionCompat;

public class BigDoorsModule implements IProtectionCompat {

    @Override
    public boolean canBreakBlock(Player player, Location loc) {
        return ResidenceBlockListener.canBreakBlock(player, loc, false);
    }

    @Override
    public boolean canBreakBlocksBetweenLocs(Player player, Location loc1, Location loc2) {

        CuboidArea area = new CuboidArea(loc1, loc2);

        Vector min = area.getLowVector();
        Vector max = area.getHighVector();

        for (int xPos = min.getBlockX(); xPos <= max.getBlockX(); xPos++) {
            for (int yPos = min.getBlockY(); yPos <= max.getBlockY(); yPos++) {
                for (int zPos = min.getBlockZ(); zPos <= max.getBlockZ(); zPos++) {
                    if (!ResidenceBlockListener.canBreakBlock(player, new Location(loc1.getWorld(), xPos, yPos, zPos), false)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "Residence";
    }

    @Override
    public boolean success() {
        return true;
    }
}
