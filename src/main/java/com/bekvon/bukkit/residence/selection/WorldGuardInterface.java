package com.bekvon.bukkit.residence.selection;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface WorldGuardInterface {

    ProtectedRegion getRegion(Player player, CuboidArea area);

    boolean isSelectionInArea(Player player);

}
