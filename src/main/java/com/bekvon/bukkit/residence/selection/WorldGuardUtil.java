package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardUtil implements WorldGuardInterface {
    private Residence plugin;

    public WorldGuardUtil(Residence residence) {
        this.plugin = residence;
    }

    @Override
    public ProtectedRegion getRegion(Player player, CuboidArea area) {
        // TODO world guard has an API do not use Reflection
//
//        if (area == null)
//            return null;
//
//        if (plugin.getWorldGuard() == null)
//            return null;
//
//        if (plugin.getWorldEdit() == null)
//            return null;
//
//        Location loc1 = area.getLowLoc();
//        Location loc2 = area.getHighLoc();
//
//        String id = "icp__tempregion";
//        try {
//
//            BlockVector min = new BlockVector(loc1.getX(), loc1.getY(), loc1.getZ());
//            BlockVector max = new BlockVector(loc2.getX(), loc2.getY(), loc2.getZ());
//            //ProtectedRegion region = ProtectedCuboidRegion.class.getConstructor(String.class, BlockVector.class, BlockVector.class).newInstance(id, min, max);
//
//            Method methd = plugin.getWorldGuard().getClass().getMethod("getRegionManager", loc1.getWorld().getClass());
//
//            RegionManager mgr = (RegionManager) methd.invoke(plugin.getWorldGuard(), loc1.getWorld());
//
//            ApplicableRegionSet regions = mgr.getApplicableRegions(region);
//
//            for (ProtectedRegion one : regions.getRegions()) {
//                if (!ResPerm.worldguard_$1.hasPermission(player, one.getId()))
//                    return one;
//            }
//        } catch (Exception | IncompatibleClassChangeError e) {
//        }
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
        Location lowLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMinimumPoint().getBlockX(),
                Region.getMinimumPoint().getBlockY(), Region.getMinimumPoint().getBlockZ());
        Location highLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMaximumPoint().getBlockX(),
                Region.getMaximumPoint().getBlockY(), Region.getMaximumPoint().getBlockZ());
        Visualizer v = new Visualizer(player);
        v.setAreas(plugin.getSelectionManager().getSelectionCuboid(player));
        v.setErrorAreas(new CuboidArea(lowLoc, highLoc));
        plugin.getSelectionManager().showBounds(player, v);
        return true;
    }
}
