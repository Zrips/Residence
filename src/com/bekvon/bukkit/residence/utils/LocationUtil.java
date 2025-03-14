package com.bekvon.bukkit.residence.utils;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.RandomLoc;
import com.bekvon.bukkit.residence.containers.playerPersistentData;
import com.bekvon.bukkit.residence.containers.playerTempData;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.PaperMethods.CMIChunkSnapShot;
import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class LocationUtil {
    static Method getBlockTypeId = null;

    private static CMIMaterial getBlockType(ChunkSnapshot snap, World world, int localX, int localY, int localZ) {
        return getBlockType(snap, new PositionRelativeData(world, localX, localY, localZ));
    }

    private static CMIMaterial getBlockType(ChunkSnapshot snap, Location loc) {
        return getBlockType(snap, new PositionRelativeData(loc));
    }

    private static CMIMaterial getBlockType(ChunkSnapshot snap, PositionRelativeData data) {
        return getBlockType(snap, data.getLocalY(), data);
    }

    @SuppressWarnings("deprecation")
    private static CMIMaterial getBlockType(ChunkSnapshot snap, int localY, PositionRelativeData data) {
        if (localY > data.getMaxWorldY() || localY < data.getMinWorldY())
            return CMIMaterial.AIR;

        if (snap == null)
            return CMIMaterial.AIR;

        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {

            if (localY >= data.getMaxWorldY())
                return CMIMaterial.AIR;

            return CMIMaterial.get(snap.getBlockType(data.getLocalX(), localY, data.getLocalZ()));
        }

        if (snap.getHighestBlockYAt(data.getLocalX(), data.getLocalZ()) < localY)
            return CMIMaterial.AIR;

        if (getBlockTypeId == null)
            try {
                getBlockTypeId = snap.getClass().getMethod("getBlockTypeId", int.class, int.class, int.class);
            } catch (Throwable e) {
                e.printStackTrace();
            }

        try {
            return CMIMaterial.get((int) getBlockTypeId.invoke(snap, data.getLocalX(), localY, data.getLocalZ()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return CMIMaterial.AIR;
    }

    private static boolean isEmptyBlock(CMIChunkSnapShot chunk, Location loc) {

        if (chunk == null)
            return false;

        CMIMaterial material = getBlockType(chunk.getSnapshot(), loc);

        return isEmptyBlock(material);
    }

    private static boolean isEmptyBlock(Material material) {
        return isEmptyBlock(CMIMaterial.get(material));
    }

    private static boolean isEmptyBlock(CMIMaterial material) {
        return material.containsCriteria(CMIMC.NOCOLLISIONBOX);
    }

    private static boolean isValidLocation(CMIChunkSnapShot chunk, Location loc) {

        if (chunk == null)
            return false;

        int chunkX = loc.getBlockX() & 0xF;
        int chunkZ = loc.getBlockZ() & 0xF;

        CMIMaterial material = getBlockType(chunk.getSnapshot(), loc.getWorld(), chunkX, loc.getBlockY(), chunkZ);

        if (!isEmptyBlock(material))
            return false;

        CMIMaterial material1 = getBlockType(chunk.getSnapshot(), loc.getWorld(), chunkX, loc.getBlockY() + 1, chunkZ);

        if (!isEmptyBlock(material1))
            return false;

        CMIMaterial material2 = getBlockType(chunk.getSnapshot(), loc.getWorld(), chunkX, loc.getBlockY() - 1, chunkZ);

        if (material2 == CMIMaterial.LAVA)
            return false;

        if (isEmptyBlock(material2))
            return false;

        return true;
    }

    public static CompletableFuture<Location> getOutsideFreeLocASYNC(ClaimedResidence res, Player player, boolean toSpawnOnFail) {
        return getOutsideFreeLocASYNC(res, player.getLocation(), player, toSpawnOnFail);
    }

    public static CompletableFuture<Location> getOutsideFreeLocASYNC(ClaimedResidence res, Location insideLoc, Player player, boolean toSpawnOnFail) {
        return CompletableFuture.supplyAsync(() -> getOutsideFreeLoc(res, insideLoc, player, toSpawnOnFail));
    }

    static Random ran = new Random(System.currentTimeMillis());

    private static RandomLoc getRandomEdge(CuboidArea area, int it) {
        switch (it % 4) {
        case 0:
            return new RandomLoc(area.getLowVector().getX() - 1, area.getLowVector().getZ() - 1 + ran.nextInt(area.getZSize() + 2));
        case 1:
            return new RandomLoc(area.getHighVector().getX() + 1, area.getLowVector().getZ() - 1 + ran.nextInt(area.getZSize() + 2));
        case 2:
            return new RandomLoc(area.getLowVector().getX() + ran.nextInt(area.getXSize()), area.getLowVector().getZ() - 1);
        case 3:
        default:
            return new RandomLoc(area.getLowVector().getX() + ran.nextInt(area.getXSize()), area.getHighVector().getZ() + 1);
        }
    }

    private static Location getEdgeLocation(Player player, CuboidArea area) {

        int maxIt = 15;

        Location loc = new Location(area.getWorld(), 0, 0, 0);

        boolean admin = ResPerm.admin_tp.hasPermission(player);

        boolean found = false;
        int it = 0;

        while (!found && it < maxIt) {

            it++;

            RandomLoc place = getRandomEdge(area, it);

            double x = place.getX();
            double z = place.getZ();

            loc.setX(x);
            loc.setZ(z);
            loc.setY(area.getHighVector().getBlockY());

            int max = area.getHighVector().getBlockY();
            max = loc.getWorld().getEnvironment() == Environment.NETHER ? 100 : max;

            CompletableFuture<CMIChunkSnapShot> cs = getSnapshot(loc, true, false);

            CMIChunkSnapShot chunk = cs.join();

            if (chunk.getSnapshot() == null)
                continue;

            for (int i = max; i > area.getLowVector().getY(); i--) {
                loc.setY(i);
                try {
                    if (isValidLocation(chunk, loc)) {
                        break;
                    }
                    if (!isEmptyBlock(chunk, loc)) {
                        loc.setY(area.getLowVector().getY());
                        break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            if (loc.getY() - 1 <= area.getLowVector().getY())
                continue;

            LocationCheck permissionCheck = new LocationCheck();

            CMIScheduler.runTask(Residence.getInstance(), () -> {
                ClaimedResidence tres = Residence.getInstance().getResidenceManager().getByLoc(loc);
                if (tres != null && player != null && (!tres.getPermissions().playerHas(player, Flags.tp, FlagCombo.TrueOrNone) ||
                    !tres.getPermissions().playerHas(player, Flags.move, FlagCombo.TrueOrNone)) && !admin) {
                    permissionCheck.setPermissionPass(false);
                }
            }).join();

            if (!permissionCheck.isPermissionPass())
                continue;

            found = true;
            loc.add(0.5, 0.1, 0.5);

            break;
        }
        return found ? loc : null;
    }

    private static Location fallBackLocation(ClaimedResidence res, Player player, boolean toSpawnOnFail) {
        if (Residence.getInstance().getConfigManager().getKickLocation() != null)
            return Residence.getInstance().getConfigManager().getKickLocation();

        if (!toSpawnOnFail)
            return null;

        World bw = res.getPermissions().getBukkitWorld();

        if (bw == null)
            return player.getWorld().getSpawnLocation();

        return bw.getSpawnLocation();
    }

    private static Location getOutsideFreeLoc(ClaimedResidence res, Location insideLoc, Player player, boolean toSpawnOnFail) {

        CMIDebug.d("get");

        playerTempData tempData = playerTempData.get(player);

        CompletableFuture<Location> futureLocation = new CompletableFuture<>();

        Location outsideLocation = null;
        try {
            CMIScheduler.runTask(Residence.getInstance(), () -> {
                futureLocation.complete(tempData.getLastValidLocation(player));
            });
        } catch (Throwable e) {
            e.printStackTrace();            
        }
        
        try {
//             outsideLocation = 
            futureLocation.get();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        
        CMIDebug.d("get outside free", outsideLocation != null);

        if (outsideLocation != null)
            return outsideLocation;

        CuboidArea area = res.getAreaByLoc(insideLoc);

        CMIDebug.d("get edge", area != null);
        if (area == null)
            return fallBackLocation(res, player, toSpawnOnFail);

        CMIDebug.d("get edge");

        Location loc = getEdgeLocation(player, area);

        if (loc == null)
            return fallBackLocation(res, player, toSpawnOnFail);

        if (player != null) {
            loc.setPitch(player.getLocation().getPitch());
            loc.setYaw(player.getLocation().getYaw());
        }

        return loc;
    }

    public static CompletableFuture<Location> getMiddleFreeLocASYNC(ClaimedResidence res, Player player, boolean toSpawnOnFail) {
        return getMiddleFreeLocASYNC(res, player.getLocation(), player, toSpawnOnFail);
    }

    public static CompletableFuture<Location> getMiddleFreeLocASYNC(ClaimedResidence res, Location insideLoc, Player player, boolean toSpawnOnFail) {
        return CompletableFuture.supplyAsync(() -> getMiddleFreeLoc(res, insideLoc, player, toSpawnOnFail));
    }

    private static Location getMiddleFreeLoc(ClaimedResidence res, Location insideLoc, Player player, boolean toSpawnOnFail) {

        if (insideLoc == null)
            return null;

        CuboidArea area = res.getAreaByLoc(insideLoc);
        if (area == null) {
            return insideLoc;
        }

        int y = area.getHighVector().getBlockY();
        int lowY = area.getLowVector().getBlockY();

        int x = area.getLowVector().getBlockX() + area.getXSize() / 2;
        int z = area.getLowVector().getBlockZ() + area.getZSize() / 2;

        Location newLoc = new Location(area.getWorld(), x + 0.5, y, z + 0.5);

        int it = 1;
        int maxIt = y - 2;

        CompletableFuture<CMIChunkSnapShot> cs = PaperLib.getSnapshot(newLoc, false, false);

        CMIChunkSnapShot chunk = cs.join();

        if (chunk == null)
            return null;

        while (it < maxIt) {
            it++;

            if (newLoc.getBlockY() < lowY)
                break;

            newLoc.add(0, -1, 0);

            try {
                if (isValidLocation(chunk, newLoc)) {
                    if (player != null) {
                        newLoc.setPitch(player.getLocation().getPitch());
                        newLoc.setYaw(player.getLocation().getYaw());
                    }
                    return newLoc;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return getOutsideFreeLoc(res, insideLoc, player, toSpawnOnFail);
    }

    public static CompletableFuture<Location> getTeleportLocationASYNC(ClaimedResidence res, Player player, boolean toSpawnOnFail) {
        return CompletableFuture.supplyAsync(() -> getTeleportLocation(res, player, toSpawnOnFail));
    }

    public static Location getTeleportLocation(ClaimedResidence res, Player player, boolean toSpawnOnFail) {

        if (res.tpLoc == null || res.getMainArea() != null && !res.containsLoc(new Location(res.getMainArea().getWorld(), res.tpLoc.getX(), res.tpLoc.getY(), res.tpLoc.getZ()))) {

            if (res.getMainArea() == null)
                return null;

            Vector low = res.getMainArea().getLowVector();
            Vector high = res.getMainArea().getHighVector();

            Location t = new Location(res.getMainArea().getWorld(), (low.getBlockX() + high.getBlockX()) / 2,
                (low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high.getBlockZ()) / 2);

            t = getMiddleFreeLoc(res, t, player, toSpawnOnFail);

            if (t == null)
                return null;

            res.tpLoc = t.toVector();
        }

        if (res.tpLoc == null)
            return null;

        Location loc = res.tpLoc.toLocation(res.getMainArea().getLowLocation().getWorld());
        if (res.PitchYaw != null) {
            loc.setPitch((float) res.PitchYaw.getX());
            loc.setYaw((float) res.PitchYaw.getY());
        }
        return loc;
    }

    public static CompletableFuture<LocationCheck> isSafeTeleportASYNC(ClaimedResidence res, Player player) {
        return CompletableFuture.supplyAsync(() -> isSafeTp(res, player));
    }

    public static LocationCheck isSafeTp(ClaimedResidence res, Player player) {

        LocationCheck validity = new LocationCheck();

        if (player.getAllowFlight())
            return validity;

        if (player.getGameMode() == GameMode.CREATIVE)
            return validity;

        if (Utils.isSpectator(player.getGameMode()))
            return validity;

        if (res.tpLoc == null)
            return validity;

        Location tempLoc = getTeleportLocation(res, player, false);

        if (tempLoc == null)
            return validity;

        CompletableFuture<CMIChunkSnapShot> cs = PaperLib.getSnapshot(tempLoc, false, false);

        CMIChunkSnapShot chunk = cs.join();

        if (chunk == null)
            return validity;

        int fallDistance = 0;
        int minY = CMIWorld.getMinHeight(tempLoc.getWorld());
        for (int i = (int) tempLoc.getY(); i >= minY; i--) {
            if (i <= minY) {
                validity.setValidity(LocationValidity.Void);
                break;
            }

            tempLoc.setY(i);

            int chunkX = tempLoc.getBlockX() & 0xF;
            int chunkZ = tempLoc.getBlockZ() & 0xF;

            CMIMaterial material = getBlockType(chunk.getSnapshot(), tempLoc.getWorld(), chunkX, tempLoc.getBlockY(), chunkZ);

            if (isEmptyBlock(material)) {
                fallDistance++;
            } else {
                if (material.isLava() || material.equals(CMIMaterial.MAGMA_BLOCK) || material.equals(CMIMaterial.FIRE)) {
                    validity.setValidity(LocationValidity.Lava);
                }
                break;
            }
        }
        validity.setFallDistance(fallDistance);
        return validity;
    }

    public static CompletableFuture<CMIChunkSnapShot> getSnapshot(Location loc, boolean generate, boolean biomeData) {
        return getSnapshot(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4, generate, biomeData);
    }

    public static CompletableFuture<CMIChunkSnapShot> getSnapshot(World world, int chunkX, int chunkZ, boolean generate, boolean biomeData) {
        if (world == null)
            return CompletableFuture.completedFuture(null);

        CompletableFuture<Chunk> future = null;
        try {
            if (!Version.isPaperBranch()) {
                return CompletableFuture.supplyAsync(() -> {
                    CMIChunkSnapShot cmiChunkSnapshot = new CMIChunkSnapShot(world);
                    try {

                        CompletableFuture<Void> t = CMIScheduler.runAtLocation(Residence.getInstance(), new Location(world, chunkX * 16, 0, chunkZ * 16), () -> cmiChunkSnapshot.setSnapshot(world
                            .getChunkAt(chunkX, chunkZ)
                            .getChunkSnapshot(true, biomeData, false)));

                        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
                            t = t.orTimeout(10, TimeUnit.SECONDS);

                        t = t.exceptionally(ex -> {
                            CMIMessages.consoleMessage("Could not get chunk snapshot for " + world + " " + (chunkX * 16) + ":" + (chunkZ * 16));
                            ex.printStackTrace();
                            return null;
                        });
                        t.get();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                    return cmiChunkSnapshot;
                });
            }
            future = PaperLib.getChunkAtAsync(world, chunkX, chunkZ, generate);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (future == null)
            return CompletableFuture.completedFuture(null);

        return future.thenComposeAsync(chunk -> CompletableFuture.supplyAsync(() -> {
            CMIChunkSnapShot cmiChunkSnapshot = new CMIChunkSnapShot(world);

            if (chunk == null)
                return cmiChunkSnapshot;

            try {
                CompletableFuture<Void> t = CMIScheduler.runAtLocation(Residence.getInstance(), new Location(world, chunkX * 16, 0, chunkZ * 16), () -> cmiChunkSnapshot.setSnapshot(chunk.getChunkSnapshot(
                    true, biomeData, false)));

                if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
                    t = t.orTimeout(10, TimeUnit.SECONDS);

                t = t.exceptionally(ex -> {
                    CMIMessages.consoleMessage("Unable to get chunk snapshot for " + world + " " + (chunkX * 16) + ":" + (chunkZ * 16));
                    ex.printStackTrace();
                    return null;
                });
                t.get();
            } catch (Throwable e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            return cmiChunkSnapshot;
        }));
    }
}
