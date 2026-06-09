package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ResidenceManager.ChunkRef;

import net.Zrips.CMILib.Container.CMINumber;

/**
 * Represents a cuboid residence area built from two corner points.
 * <p>
 * A physical Residence area is described by the low point {@link #lowPoints}
 * and the high point {@link #highPoints}. The constructor normalizes the two
 * Bukkit locations into minimum and maximum block coordinates, so callers do
 * not need to pass the corners in a specific order.
 * </p>
 */
public class CuboidArea {
    /**
     * Maximum block coordinate of this area on the X, Y, and Z axes.
     */
    private Vector highPoints;
    /**
     * Minimum block coordinate of this area on the X, Y, and Z axes.
     */
    private Vector lowPoints;
    /**
     * Name of the world this area belongs to.
     * <p>
     * This keeps ownership information when the Bukkit {@link World} object is
     * not loaded yet or cannot be stored directly.
     * </p>
     */
    protected String worldName;
    /**
     * Bukkit world object this area belongs to.
     */
    protected World world;

    /**
     * Clones this area.
     * <p>
     * Coordinate vectors are copied into new {@link Vector} instances. The world
     * object and world name keep their original values.
     * </p>
     *
     * @return a copy of this area
     */
    public CuboidArea clone() {
        CuboidArea newArea = new CuboidArea();
        newArea.highPoints = highPoints.clone();
        newArea.lowPoints = lowPoints.clone();
        newArea.worldName = worldName;
        newArea.world = world;
        return newArea;
    }

    /**
     * Creates a cuboid area from two corner locations.
     * <p>
     * The constructor compares the X, Y, and Z coordinates of both locations and
     * calculates the low and high points automatically. If either location is
     * {@code null}, this instance remains uninitialized and callers must avoid
     * using its coordinate-dependent methods.
     * </p>
     *
     * @param startLoc one corner of the area
     * @param endLoc another corner of the area
     */
    public CuboidArea(Location startLoc, Location endLoc) {

        if (startLoc == null || endLoc == null)
            return;

        int highx;
        int highy;
        int highz;
        int lowx;
        int lowy;
        int lowz;

        if (startLoc.getBlockX() > endLoc.getBlockX()) {
            highx = startLoc.getBlockX();
            lowx = endLoc.getBlockX();
        } else {
            highx = endLoc.getBlockX();
            lowx = startLoc.getBlockX();
        }
        if (startLoc.getBlockY() > endLoc.getBlockY()) {
            highy = startLoc.getBlockY();
            lowy = endLoc.getBlockY();
        } else {
            highy = endLoc.getBlockY();
            lowy = startLoc.getBlockY();
        }
        if (startLoc.getBlockZ() > endLoc.getBlockZ()) {
            highz = startLoc.getBlockZ();
            lowz = endLoc.getBlockZ();
        } else {
            highz = endLoc.getBlockZ();
            lowz = startLoc.getBlockZ();
        }

        highPoints = new Vector(highx, highy, highz);
        lowPoints = new Vector(lowx, lowy, lowz);

        world = startLoc.getWorld() != null ? startLoc.getWorld() : startLoc.getWorld() != null ? startLoc.getWorld() : null;

        worldName = world != null ? world.getName() : null;
    }

    /**
     * Creates an empty area instance.
     * <p>
     * This is mainly used for deserialization, cloning, or later coordinate
     * population through setters.
     * </p>
     */
    public CuboidArea() {
    }

    /**
     * Checks whether another area is fully inside this area.
     * <p>
     * The area is considered contained when both its high point and low point are
     * inside this area.
     * </p>
     *
     * @param area area to check
     * @return {@code true} if the area is fully inside this area
     */
    public boolean isAreaWithinArea(CuboidArea area) {
        return (this.containsLoc(area.highPoints, area.getWorldName()) && this.containsLoc(area.lowPoints, area.getWorldName()));
    }

    /**
     * Checks whether a Bukkit location is inside this area.
     *
     * @param loc location to check
     * @return {@code true} if the location is inside this area; {@code false} if
     *         the location is {@code null} or outside this area
     */
    public boolean containsLoc(Location loc) {
        if (loc == null)
            return false;
        return containsLoc(loc.toVector(), loc.getWorld().getName());
    }

    /**
     * Checks whether a vector coordinate is inside this area.
     * <p>
     * The check compares the world name and the X, Y, and Z boundaries. Boundary
     * blocks are included in the area.
     * </p>
     *
     * @param loc block coordinate vector to check
     * @param world world name of the coordinate
     * @return {@code true} if the coordinate is inside this area
     */
    public boolean containsLoc(Vector loc, String world) {
        if (loc == null)
            return false;

        if (!world.equals(worldName))
            return false;

        if (lowPoints.getBlockX() > loc.getBlockX())
            return false;

        if (highPoints.getBlockX() < loc.getBlockX())
            return false;

        if (lowPoints.getBlockZ() > loc.getBlockZ())
            return false;

        if (highPoints.getBlockZ() < loc.getBlockZ())
            return false;

        if (lowPoints.getBlockY() > loc.getBlockY())
            return false;

        if (highPoints.getBlockY() < loc.getBlockY())
            return false;

        return true;
    }

    /**
     * Checks whether this area overlaps another area.
     * <p>
     * Areas in different worlds are not considered colliding. For areas in the
     * same world, the check first uses corner containment as a fast path, then
     * uses axis range intersection to cover edge and face intersections.
     * </p>
     *
     * @param area another area to check
     * @return {@code true} if the two areas overlap
     */
    public boolean checkCollision(CuboidArea area) {
        if (!area.getWorld().equals(this.getWorld())) {
            return false;
        }
        if (area.containsLoc(lowPoints, getWorldName()) || area.containsLoc(highPoints, getWorldName()) || this.containsLoc(area.highPoints, getWorldName()) || this.containsLoc(area.lowPoints,
                getWorldName())) {
            return true;
        }
        return advCuboidCheckCollision(highPoints, lowPoints, area.highPoints, area.lowPoints);
    }

    /**
     * Checks whether two cuboids intersect by comparing their axis projections.
     * <p>
     * Two areas collide only when their coordinate ranges intersect on all three
     * axes: X, Y, and Z.
     * </p>
     *
     * @param A1High high point of the first area
     * @param A1Low low point of the first area
     * @param A2High high point of the second area
     * @param A2Low low point of the second area
     * @return {@code true} if the two areas intersect
     */
    private static boolean advCuboidCheckCollision(Vector A1High, Vector A1Low, Vector A2High, Vector A2Low) {
        int A1HX = A1High.getBlockX();
        int A1LX = A1Low.getBlockX();
        int A2HX = A2High.getBlockX();
        int A2LX = A2Low.getBlockX();
        if ((A1HX >= A2LX && A1HX <= A2HX) || (A1LX >= A2LX && A1LX <= A2HX) || (A2HX >= A1LX && A2HX <= A1HX) || (A2LX >= A1LX && A2LX <= A1HX)) {
            int A1HY = A1High.getBlockY();
            int A1LY = A1Low.getBlockY();
            int A2HY = A2High.getBlockY();
            int A2LY = A2Low.getBlockY();
            if ((A1HY >= A2LY && A1HY <= A2HY) || (A1LY >= A2LY && A1LY <= A2HY) || (A2HY >= A1LY && A2HY <= A1HY) || (A2LY >= A1LY && A2LY <= A1HY)) {
                int A1HZ = A1High.getBlockZ();
                int A1LZ = A1Low.getBlockZ();
                int A2HZ = A2High.getBlockZ();
                int A2LZ = A2Low.getBlockZ();
                if ((A1HZ >= A2LZ && A1HZ <= A2HZ) || (A1LZ >= A2LZ && A1LZ <= A2HZ) || (A2HZ >= A1LZ && A2HZ <= A1HZ) || (A2LZ >= A1LZ && A2LZ <= A1HZ)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the billable volume or area.
     * <p>
     * When Y blocks are included in cost calculation, this returns X * Y * Z.
     * Otherwise, it returns the horizontal X * Z area.
     * </p>
     *
     * @return size of this area
     */
    public long getSize() {
        long xsize = (highPoints.getBlockX() - lowPoints.getBlockX()) + 1;
        long zsize = (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
        if (!Residence.getInstance().getConfigManager().isNoCostForYBlocks()) {
            long ysize = (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
            return CMINumber.abs(xsize * ysize * zsize);
        }
        return CMINumber.abs(xsize * zsize);
    }

    /**
     * Gets the length on the X axis.
     *
     * @return number of blocks on the X axis
     */
    public int getXSize() {
        return (highPoints.getBlockX() - lowPoints.getBlockX()) + 1;
    }

    /**
     * Gets the length on the Y axis.
     *
     * @return number of blocks on the Y axis
     */
    public int getYSize() {
        return (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
    }

    /**
     * Gets the length on the Z axis.
     *
     * @return number of blocks on the Z axis
     */
    public int getZSize() {
        return (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
    }

    /**
     * Gets the high point vector.
     *
     * @return maximum coordinate point of this area
     */
    public Vector getHighVector() {
        return highPoints;
    }

    /**
     * Gets the low point vector.
     *
     * @return minimum coordinate point of this area
     */
    public Vector getLowVector() {
        return lowPoints;
    }

    /**
     * Gets the high point location.
     *
     * @return Bukkit location for the maximum coordinate point
     * @deprecated use {@link #getHighLocation()}
     */
    @Deprecated
    public Location getHighLoc() {
        return getHighLocation();
    }

    /**
     * Gets the low point location.
     *
     * @return Bukkit location for the minimum coordinate point
     * @deprecated use {@link #getLowLocation()}
     */
    @Deprecated
    public Location getLowLoc() {
        return getLowLocation();
    }

    /**
     * Gets the high point location.
     *
     * @return Bukkit location for the maximum coordinate point
     */
    public Location getHighLocation() {
        return highPoints.toLocation(getWorld());
    }

    /**
     * Gets the low point location.
     *
     * @return Bukkit location for the minimum coordinate point
     */
    public Location getLowLocation() {
        return lowPoints.toLocation(getWorld());
    }

    /**
     * Gets the world object this area belongs to.
     * <p>
     * If the world object is not currently cached but the world name is known,
     * this method attempts to resolve it from Bukkit.
     * </p>
     *
     * @return world this area belongs to; may be {@code null} when the world is
     *         not loaded or the name is unknown
     */
    public World getWorld() {
        if (world == null && worldName != null)
            world = Bukkit.getWorld(worldName);
        return world;
    }

    /**
     * Gets the world name this area belongs to.
     *
     * @return world name; {@code null} if both the world object and stored name
     *         are unavailable
     */
    public String getWorldName() {
        return world != null ? world.getName() : worldName;
    }

    /**
     * Saves this area's coordinates as a compact string.
     * <p>
     * Format: {@code lowX:lowY:lowZ:highX:highY:highZ}.
     * </p>
     *
     * @return coordinate string readable by {@link #newLoad(String, String)}
     */
    public String newSave() {
        return lowPoints.getBlockX() + ":" + lowPoints.getBlockY() + ":" + lowPoints.getBlockZ() + ":" + highPoints.getBlockX() + ":" + highPoints.getBlockY() + ":" + highPoints.getBlockZ();
    }

    /**
     * Loads area coordinates from a compact string.
     *
     * @param root coordinate string in {@code lowX:lowY:lowZ:highX:highY:highZ}
     *        format
     * @param world world name this area belongs to
     * @return loaded area
     * @throws Exception when the coordinate string is empty, malformed, or cannot
     *         be parsed as integers
     */
    public static CuboidArea newLoad(String root, String world) throws Exception {
        if (root == null || !root.contains(":")) {
            throw new Exception("Invalid residence physical location...");
        }
        CuboidArea newArea = new CuboidArea();
        String[] split = root.split(":");
        try {
            int x1 = Integer.parseInt(split[0]);
            int y1 = Integer.parseInt(split[1]);
            int z1 = Integer.parseInt(split[2]);
            int x2 = Integer.parseInt(split[3]);
            int y2 = Integer.parseInt(split[4]);
            int z2 = Integer.parseInt(split[5]);
            newArea.lowPoints = new Vector(x1, y1, z1);
            newArea.highPoints = new Vector(x2, y2, z2);
            newArea.worldName = world;
//            newArea.world = Bukkit.getWorld(newArea.worldName);
        } catch (Exception e) {
            throw new Exception("Invalid residence physical location...");
        }

        return newArea;
    }

    /**
     * Loads area coordinates from legacy map data.
     * <p>
     * The data should contain {@code X1}, {@code Y1}, {@code Z1}, {@code X2},
     * {@code Y2}, and {@code Z2}.
     * </p>
     *
     * @param root map data containing area coordinates
     * @param world world name this area belongs to
     * @return loaded area
     * @throws Exception when the data is empty or coordinate data is incomplete
     */
    public static CuboidArea load(Map<String, Object> root, String world) throws Exception {
        if (root == null) {
            throw new Exception("Invalid residence physical location...");
        }
        CuboidArea newArea = new CuboidArea();
        int x1 = (Integer) root.get("X1");
        int y1 = (Integer) root.get("Y1");
        int z1 = (Integer) root.get("Z1");
        int x2 = (Integer) root.get("X2");
        int y2 = (Integer) root.get("Y2");
        int z2 = (Integer) root.get("Z2");
        newArea.highPoints = new Vector(x1, y1, z1);
        newArea.lowPoints = new Vector(x2, y2, z2);
        newArea.worldName = world;
        newArea.world = Bukkit.getWorld(newArea.worldName);
        return newArea;
    }

    /**
     * Gets all chunk references covered by this area.
     * <p>
     * Chunks are calculated only on the X/Z plane. The Y axis does not affect the
     * chunk list.
     * </p>
     *
     * @return chunk references covered by this area
     */
    public List<ChunkRef> getChunks() {
        List<ChunkRef> chunks = new ArrayList<>();
        Vector high = this.highPoints;
        Vector low = this.lowPoints;
        int lowX = ChunkRef.getChunkCoord(low.getBlockX());
        int lowZ = ChunkRef.getChunkCoord(low.getBlockZ());
        int highX = ChunkRef.getChunkCoord(high.getBlockX());
        int highZ = ChunkRef.getChunkCoord(high.getBlockZ());

        for (int x = lowX; x <= highX; x++) {
            for (int z = lowZ; z <= highZ; z++) {
                chunks.add(new ChunkRef(x, z));
            }
        }
        return chunks;
    }

    /**
     * Sets the high point location.
     *
     * @param highLocation new maximum coordinate point
     */
    public void setHighLocation(Location highLocation) {
        this.highPoints = highLocation.toVector();
        this.world = highLocation.getWorld();
    }

    /**
     * Sets the high point vector.
     *
     * @param highLocation new maximum coordinate point
     */
    public void setHighVector(Vector highLocation) {
        this.highPoints = highLocation;
    }

    /**
     * Sets the low point location.
     *
     * @param lowLocation new minimum coordinate point
     */
    public void setLowLocation(Location lowLocation) {
        this.lowPoints = lowLocation.toVector();
        this.world = lowLocation.getWorld();
    }

    /**
     * Sets the low point vector.
     *
     * @param lowLocation new minimum coordinate point
     */
    public void setLowVector(Vector lowLocation) {
        this.lowPoints = lowLocation;
    }

    /**
     * Calculates the price of this area by permission group settings.
     *
     * @param group permission group used to read the per-block price
     * @return price of this area, rounded down to two decimal places
     */
    public double getCost(PermissionGroup group) {
        return (long) (getSize() * group.getCostPerBlock() * 100L) / 100D;
    }
}
