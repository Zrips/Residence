package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

public interface WorldGuardInterface {

    ProtectedRegion getRegion(Player player, CuboidArea area);

    boolean isSelectionInArea(Player player);

}
