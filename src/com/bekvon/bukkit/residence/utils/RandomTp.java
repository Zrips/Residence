package com.bekvon.bukkit.residence.utils;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.DelayTeleport;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.containers.ValidLocation;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;
import net.Zrips.CMILib.Version.Teleporters.CMITeleporter;

public class RandomTp {

    static int miny = 63;

    private Residence plugin;

    public RandomTp(Residence plugin) {
        this.plugin = plugin;
    }

    public boolean isDefinedRnadomTp(World world) {
        for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
            if (!one.getCenter().getWorld().equals(world))
                continue;
            return true;
        }
        return false;
    }

    @Deprecated
    public Location getRandomlocation(World world) {

        if (world == null)
            return null;

        Random randomX = new Random(System.currentTimeMillis());
        Random randomZ = new Random(System.nanoTime());

        boolean ok = false;
        double x = 0;
        double z = 0;

        int tries = 0;

        RandomTeleport rtloc = null;

        for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {

            if (!one.getCenter().getWorld().equals(world))
                continue;

            rtloc = one;
            break;
        }

        if (rtloc == null)
            return null;

        int inerrange = rtloc.getMinCord();
        int outerrange = rtloc.getMaxCord();
        if (outerrange < 1)
            outerrange = 1;
        int maxtries = plugin.getConfigManager().getrtMaxTries();

        int centerX = rtloc.getCenter().getBlockX();
        int centerY = rtloc.getCenter().getBlockZ();

        Location loc = null;

        c: while (!ok) {
            tries++;
            if (tries > maxtries)
                return null;

            x = randomX.nextInt(outerrange * 2) - outerrange + 0.5 + centerX;

            if (x > inerrange * -1 && x < inerrange)
                continue;

            z = randomZ.nextInt(outerrange * 2) - outerrange + 0.5 + centerY;
            if (z > inerrange * -1 && z < inerrange)
                continue;

            loc = new Location(world, x, world.getMaxHeight(), z);

            int max = loc.getWorld().getMaxHeight();
            max = loc.getWorld().getEnvironment().equals(Environment.NETHER) ? 100 : world.getHighestBlockAt(loc).getY() + 1;

            for (int i = max; i > 0; i--) {
                loc.setY(i);
                Block block = loc.getBlock();
                Block block2 = loc.clone().add(0, 1, 0).getBlock();
                Block block3 = loc.clone().add(0, -1, 0).getBlock();
                if (!ResidencePlayerListener.isEmptyBlock(block3) && ResidencePlayerListener.isEmptyBlock(block) && ResidencePlayerListener.isEmptyBlock(block2)) {
                    break;
                }
                if (i <= 3) {
                    loc = null;
                    continue c;
                }
            }

            if (!ResidencePlayerListener.isEmptyBlock(loc.getBlock()))
                continue;

            if (loc.clone().add(0, -1, 0).getBlock().getType().equals(Material.LAVA))
                continue;

            if (loc.clone().add(0, -1, 0).getBlock().getType().equals(Material.WATER))
                continue;

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);

            if (res != null)
                continue;

            loc.setY(loc.getY() + 2);
            break;
        }

        if (loc != null) {
            int dir = randomZ.nextInt(359);
            loc.setYaw(dir);
        }

        return loc;
    }

    Random randomX = new Random(System.currentTimeMillis());
    Random randomZ = new Random(System.nanoTime());

    public Location getRandomlocationSync(World world) {
        return get(world);
    }

    public CompletableFuture<Location> getRandomlocationAsync(World world) {
        return CompletableFuture.supplyAsync(() -> get(world));
    }

    private RandomTeleport getRandomTeleport(World world) {
        if (world == null)
            return null;
        for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
            if (!one.getCenter().getWorld().equals(world))
                continue;
            return one;
        }
        return null;
    }

    private Location get(World world) {

        if (world == null)
            return null;

        boolean ok = false;
        double x = 0;
        double z = 0;

        int tries = 0;

        RandomTeleport rtloc = null;

        for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {

            if (!one.getCenter().getWorld().equals(world))
                continue;

            rtloc = one;
            break;
        }

        if (rtloc == null)
            return null;

        int inerrange = rtloc.getMinCord();
        int outerrange = rtloc.getMaxCord();
        if (outerrange < 1)
            outerrange = 1;
        int maxtries = plugin.getConfigManager().getrtMaxTries();

        int centerX = rtloc.getCenter().getBlockX();
        int centerY = rtloc.getCenter().getBlockZ();

        Location loc = null;

        while (!ok) {
            tries++;

            if (tries > maxtries)
                return null;

            try {
                x = randomX.nextInt(outerrange * 2) - outerrange + 0.5 + centerX;

                if (x > inerrange * -1 && x < inerrange)
                    continue;

                z = randomZ.nextInt(outerrange * 2) - outerrange + 0.5 + centerY;
                if (z > inerrange * -1 && z < inerrange)
                    continue;

                loc = new Location(world, x, world.getMaxHeight(), z);

                loc = getDownLocationSimple(loc);

                if (loc == null) {
                    continue;
                }

                if (loc.getY() < CMIWorld.getMinHeight(loc.getWorld()) + 4) {

                    if (loc.getWorld().getEnvironment().equals(Environment.NETHER)) {
                        loc.setY(CMIWorld.getMaxHeight(loc.getWorld()) / 2);
                    } else {
                        if (Version.isFolia()) {

                            Location location = loc;

                            CompletableFuture<Void> fut = CMIScheduler.runAtLocation(plugin, loc, () -> {
                                Chunk chunk = location.getChunk();
                                int y = chunk.getChunkSnapshot().getHighestBlockYAt(location.getBlockX() & 0xF, location.getBlockZ() & 0xF) - 1;
                                location.setY(y);
                            });
                            fut.get();

                            loc = location;

                        } else if (Version.isPaper() && Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
                            CompletableFuture<Chunk> chunkFuture = PaperLib.getChunkAtAsync(loc, false);
                            Chunk chunk = chunkFuture.get();

                            if (chunk == null)
                                continue;

                            int y = chunk.getChunkSnapshot().getHighestBlockYAt(loc.getBlockX() & 0xF, loc.getBlockZ() & 0xF) - 1;

                            loc.setY(y);
                        } else {
                            loc.setY(loc.getWorld().getHighestBlockYAt(loc));
                        }
                    }
                }

                if (loc.getWorld().getEnvironment().equals(Environment.NETHER)) {
                    if (loc.getY() > 128)
                        continue;
                }

                ValidLocation empty = new ValidLocation();

                if (Version.isFolia()) {
                    Location location = loc;
                    CompletableFuture<Void> fut = CMIScheduler.runAtLocation(plugin, loc, () -> {
                        empty.valid = ResidencePlayerListener.isEmptyBlock(location.getBlock());

                        if (!empty.valid)
                            return;

                        empty.valid = !location.clone().add(0, -1, 0).getBlock().getType().equals(Material.LAVA);

                        if (!empty.valid)
                            return;

                        empty.valid = !location.clone().add(0, -1, 0).getBlock().getType().equals(Material.WATER);
                    });
                    fut.get();
                } else {
                    empty.valid = ResidencePlayerListener.isEmptyBlock(loc.getBlock());
                }

                if (!empty.valid)
                    continue;

                if (!Version.isFolia()) {
                    if (loc.clone().add(0, -1, 0).getBlock().getType().equals(Material.LAVA))
                        continue;

                    if (loc.clone().add(0, -1, 0).getBlock().getType().equals(Material.WATER))
                        continue;
                }

                ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);

                if (res != null)
                    continue;

                loc.setY(loc.getY() + 2);
                break;

            } catch (Exception | Error e) {
            }
        }

        if (loc != null) {
            int dir = randomZ.nextInt(359);
            loc.setYaw(dir);
        }

        return loc;
    }

    private static Location getDownLocationSimple(Location oloc) {
        try {
            if (oloc == null)
                return oloc;
            Location loc = oloc.clone();

            loc.setY(loc.getBlockY());

            if (!oloc.getWorld().getEnvironment().equals(Environment.NETHER)) {
                if (Version.isFolia()) {

                    Location location = loc.clone();

                    CompletableFuture<Void> fut = CMIScheduler.runAtLocation(Residence.getInstance(), loc, () -> {
                        Chunk chunk = location.getChunk();
                        int y = chunk.getChunkSnapshot().getHighestBlockYAt(location.getBlockX() & 0xF, location.getBlockZ() & 0xF) - 1;
                        location.setY(y + 1);
                    });
                    fut.get();

                    loc = location;

                } else if (Version.isPaper() && Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
                    CompletableFuture<Chunk> chunkFuture = PaperLib.getChunkAtAsync(loc, true);

                    Chunk chunk = chunkFuture.get();

                    if (chunk == null)
                        return null;

                    int y = chunk.getChunkSnapshot().getHighestBlockYAt(loc.getBlockX() & 0xF, loc.getBlockZ() & 0xF) - 1;

                    if (loc.getY() < y) {
                        return null;
                    }
                    loc.setY(y + 1);
                } else {
                    int y = loc.getWorld().getHighestBlockYAt(loc);
                    if (loc.getY() < y) {
                        return null;
                    }
                    loc.setY(y + 1);
                }
                if (oloc.getWorld().getEnvironment().equals(Environment.THE_END) && loc.getY() < 5)
                    return null;

                return loc.add(0, 1, 0);
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void performDelaydTp(final Location loc, final Player targetPlayer) {

        DelayTeleport tpDelayRecord = Teleporting.getOrCreateTeleportDelay(targetPlayer.getUniqueId());
        if (tpDelayRecord.getTeleportTask() != null)
            tpDelayRecord.getTeleportTask().cancel();

        CMITask task = CMIScheduler.runAtLocationLater(plugin, loc, () -> {
            if (!Teleporting.isUnderTeleportDelay(targetPlayer.getUniqueId()) && plugin.getConfigManager().getTeleportDelay() > 0)
                return;

            Teleporting.cancelTeleportDelay(targetPlayer.getUniqueId());

            targetPlayer.closeInventory();
            CMITeleporter.teleportAsync(targetPlayer, loc);
            plugin.msg(targetPlayer, lm.RandomTeleport_TeleportSuccess, loc.getX(), loc.getY(), loc.getZ());
        }, plugin.getConfigManager().getTeleportDelay() * 20L);

        tpDelayRecord.setRemainingTime(plugin.getConfigManager().getTeleportDelay());
        tpDelayRecord.setTeleportTask(task);
    }

    public void performInstantTp(Location loc, Player targetPlayer) {
        targetPlayer.closeInventory();
        CMITeleporter.teleportAsync(targetPlayer, loc);

        plugin.msg(targetPlayer, lm.RandomTeleport_TeleportSuccess, loc.getX(), loc.getY(), loc.getZ());
    }
}
