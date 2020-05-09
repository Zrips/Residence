package com.bekvon.bukkit.residence.selection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuard7Util implements WorldGuardInterface {
    private Residence plugin;

    public WorldGuard7Util(Residence residence) {
	this.plugin = residence;
    }

    @Override
    public ProtectedRegion getRegion(Player player, CuboidArea area) {

	if (area == null)
	    return null;

	if (plugin.getWorldGuard() == null)
	    return null;

	if (plugin.getWorldEdit() == null)
	    return null;

	Location loc1 = area.getLowLoc();
	Location loc2 = area.getHighLoc();

	String id = "icp__tempregion";
	try {
	    BlockVector3 min = BlockVector3.at(loc1.getX(), loc1.getY(), loc1.getZ());
	    BlockVector3 max = BlockVector3.at(loc2.getX(), loc2.getY(), loc2.getZ());
	    ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);

	    RegionManager mgr = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc1.getWorld()));

	    ApplicableRegionSet regions = mgr.getApplicableRegions(region);

	    for (ProtectedRegion one : regions.getRegions()) {
		if (!ResPerm.worldguard_$1.hasPermission(player, one.getId()))
		    return one;
	    }
	} catch (Exception | IncompatibleClassChangeError e) {
	}
	return null;
    }

    @Override
    public boolean isSelectionInArea(Player player) {
	if (plugin.getWorldGuard() == null)
	    return false;

	ProtectedRegion Region = getRegion(player, plugin.getSelectionManager().getSelectionCuboid(player));
	if (Region == null)
	    return false;

	plugin.msg(player, lm.Select_WorldGuardOverlap, Region.getId());
	Location lowLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player).getWorld(), Region.getMinimumPoint().getBlockX(),
	    Region.getMinimumPoint().getBlockY(), Region.getMinimumPoint().getBlockZ());
	Location highLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player).getWorld(), Region.getMaximumPoint().getBlockX(),
	    Region.getMaximumPoint().getBlockY(), Region.getMaximumPoint().getBlockZ());
	Visualizer v = new Visualizer(player);
	v.setAreas(plugin.getSelectionManager().getSelectionCuboid(player));
	v.setErrorAreas(new CuboidArea(lowLoc, highLoc));
	plugin.getSelectionManager().showBounds(player, v);
	return true;
    }
}
