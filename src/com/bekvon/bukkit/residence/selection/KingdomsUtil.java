package com.bekvon.bukkit.residence.selection;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.kingdoms.constants.land.Land;
import org.kingdoms.constants.land.SimpleChunkLocation;
import org.kingdoms.manager.game.GameManagement;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class KingdomsUtil {
    private Residence plugin;

    public KingdomsUtil(Residence residence) {
        this.plugin = residence;
    }

    private Land getRegion(CuboidArea area) {

        if (plugin.getKingdomsManager() == null)
            return null;

        if (area == null)
            return null;

        Chunk loc1 = area.getLowLocation().getChunk();
        Chunk loc2 = area.getHighLocation().getChunk();

        World world = loc1.getWorld();

        for (int x = loc1.getX(); x <= loc2.getX(); x++) {
            for (int z = loc1.getZ(); z <= loc2.getZ(); z++) {
                Chunk tchunk = world.getChunkAt(x, z);
                SimpleChunkLocation sChunk = new SimpleChunkLocation(tchunk);
                Land land = GameManagement.getLandManager().getOrLoadLand(sChunk);
                if (land != null && land.getOwner() != null)
                    return land;
            }
        }

        return null;
    }

    public boolean isSelectionInArea(Player player) {

        if (plugin.getKingdomsManager() == null)
            return false;

        Land land = getRegion(plugin.getSelectionManager().getSelectionCuboid(player));
        if (land == null)
            return false;

        lm.Select_KingdomsOverlap.sendMessage(player, land.getOwner());

        SimpleChunkLocation sl = land.getLoc();

        World world = Bukkit.getWorld(sl.getWorld());

        Location lowLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player).getWorld(), sl.getX() * 16, 0, sl.getZ() * 16);
        Location highLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player).getWorld(), sl.getX() * 16 + 16, world.getMaxHeight(), sl.getZ() * 16 + 16);
        Visualizer v = new Visualizer(player);
        v.setAreas(plugin.getSelectionManager().getSelectionCuboid(player));
        v.setErrorAreas(new CuboidArea(lowLoc, highLoc));
        plugin.getSelectionManager().showBounds(player, v);
        return true;
    }
}
