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
 * 表示一个基于两个角点构成的长方体领地区域。
 * <p>
 * Residence 的物理领地范围由低点 {@link #lowPoints} 和高点 {@link #highPoints}
 * 共同描述。构造时会自动把传入的两个 Bukkit 坐标转换为最小坐标点和最大坐标点，
 * 因此调用方不需要关心两个角点的先后顺序。
 * </p>
 */
public class CuboidArea {
    /**
     * 区域最大坐标点，分别保存 X、Y、Z 三个方向的最大方块坐标。
     */
    private Vector highPoints;
    /**
     * 区域最小坐标点，分别保存 X、Y、Z 三个方向的最小方块坐标。
     */
    private Vector lowPoints;
    /**
     * 区域所在世界名称，用于在世界对象尚未加载或无法直接保存 World 时保留归属信息。
     */
    protected String worldName;
    /**
     * 区域所在 Bukkit 世界对象。
     */
    protected World world;

    /**
     * 克隆当前区域。
     * <p>
     * 坐标向量会复制为新的 {@link Vector} 实例，世界对象和世界名称保持原值。
     * </p>
     *
     * @return 当前区域的副本
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
     * 根据两个角点创建长方体区域。
     * <p>
     * 方法会比较两个点的 X、Y、Z 坐标，自动计算出低点和高点。
     * 如果任一坐标为空，则对象会保持未初始化状态，后续调用方需要自行避免使用空坐标。
     * </p>
     *
     * @param startLoc 区域的一个角点
     * @param endLoc 区域的另一个角点
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
     * 创建空区域实例。
     * <p>
     * 主要用于反序列化、克隆或后续通过 setter 填充坐标。
     * </p>
     */
    public CuboidArea() {
    }

    /**
     * 判断传入区域是否完整位于当前区域内部。
     * <p>
     * 只要传入区域的高点和低点都在当前区域内，就认为该区域被当前区域包含。
     * </p>
     *
     * @param area 要检查的区域
     * @return 完整位于当前区域内返回 {@code true}，否则返回 {@code false}
     */
    public boolean isAreaWithinArea(CuboidArea area) {
        return (this.containsLoc(area.highPoints, area.getWorldName()) && this.containsLoc(area.lowPoints, area.getWorldName()));
    }

    /**
     * 判断 Bukkit 坐标是否位于当前区域内。
     *
     * @param loc 要检查的位置
     * @return 位置在区域内返回 {@code true}；位置为空或不在区域内返回 {@code false}
     */
    public boolean containsLoc(Location loc) {
        if (loc == null)
            return false;
        return containsLoc(loc.toVector(), loc.getWorld().getName());
    }

    /**
     * 判断向量坐标是否位于当前区域内。
     * <p>
     * 检查会同时比较世界名称和 X、Y、Z 三个方向的边界，边界方块本身也算在区域内。
     * </p>
     *
     * @param loc 要检查的方块坐标向量
     * @param world 坐标所在世界名称
     * @return 坐标位于当前区域内返回 {@code true}，否则返回 {@code false}
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
     * 判断当前区域是否与另一个区域发生重叠。
     * <p>
     * 不同世界的区域不会被认为发生碰撞。同一世界下会先通过角点包含关系快速判断，
     * 再通过三轴范围交叉检查处理边与面相交的情况。
     * </p>
     *
     * @param area 要检查的另一个区域
     * @return 两个区域有重叠返回 {@code true}，否则返回 {@code false}
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
     * 通过三轴投影区间交叉判断两个长方体是否相交。
     * <p>
     * 只有 X、Y、Z 三个方向的坐标区间都存在交集时，两个区域才算发生碰撞。
     * </p>
     *
     * @param A1High 第一个区域的高点
     * @param A1Low 第一个区域的低点
     * @param A2High 第二个区域的高点
     * @param A2Low 第二个区域的低点
     * @return 两个区域相交返回 {@code true}，否则返回 {@code false}
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
     * 获取区域计费体积或面积。
     * <p>
     * 当配置启用 Y 轴方块计费时，返回 X * Y * Z 的体积；
     * 当配置不按 Y 轴计费时，返回 X * Z 的水平面积。
     * </p>
     *
     * @return 当前区域大小
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
     * 获取 X 轴方向长度。
     *
     * @return X 轴包含的方块数量
     */
    public int getXSize() {
        return (highPoints.getBlockX() - lowPoints.getBlockX()) + 1;
    }

    /**
     * 获取 Y 轴方向长度。
     *
     * @return Y 轴包含的方块数量
     */
    public int getYSize() {
        return (highPoints.getBlockY() - lowPoints.getBlockY()) + 1;
    }

    /**
     * 获取 Z 轴方向长度。
     *
     * @return Z 轴包含的方块数量
     */
    public int getZSize() {
        return (highPoints.getBlockZ() - lowPoints.getBlockZ()) + 1;
    }

    /**
     * 获取区域高点向量。
     *
     * @return 区域最大坐标点
     */
    public Vector getHighVector() {
        return highPoints;
    }

    /**
     * 获取区域低点向量。
     *
     * @return 区域最小坐标点
     */
    public Vector getLowVector() {
        return lowPoints;
    }

    /**
     * 获取区域高点位置。
     *
     * @return 区域最大坐标点对应的 Bukkit 位置
     * @deprecated 请使用 {@link #getHighLocation()}
     */
    @Deprecated
    public Location getHighLoc() {
        return getHighLocation();
    }

    /**
     * 获取区域低点位置。
     *
     * @return 区域最小坐标点对应的 Bukkit 位置
     * @deprecated 请使用 {@link #getLowLocation()}
     */
    @Deprecated
    public Location getLowLoc() {
        return getLowLocation();
    }

    /**
     * 获取区域高点位置。
     *
     * @return 区域最大坐标点对应的 Bukkit 位置
     */
    public Location getHighLocation() {
        return highPoints.toLocation(getWorld());
    }

    /**
     * 获取区域低点位置。
     *
     * @return 区域最小坐标点对应的 Bukkit 位置
     */
    public Location getLowLocation() {
        return lowPoints.toLocation(getWorld());
    }

    /**
     * 获取区域所在世界对象。
     * <p>
     * 如果当前未缓存世界对象，但保存了世界名称，会通过 Bukkit 世界管理器尝试重新获取。
     * </p>
     *
     * @return 区域所在世界；世界未加载或名称为空时可能返回 {@code null}
     */
    public World getWorld() {
        if (world == null && worldName != null)
            world = Bukkit.getWorld(worldName);
        return world;
    }

    /**
     * 获取区域所在世界名称。
     *
     * @return 世界名称；世界对象和保存名称都为空时返回 {@code null}
     */
    public String getWorldName() {
        return world != null ? world.getName() : worldName;
    }

    /**
     * 将区域坐标保存为紧凑字符串。
     * <p>
     * 保存格式为 {@code lowX:lowY:lowZ:highX:highY:highZ}。
     * </p>
     *
     * @return 可用于 {@link #newLoad(String, String)} 读取的坐标字符串
     */
    public String newSave() {
        return lowPoints.getBlockX() + ":" + lowPoints.getBlockY() + ":" + lowPoints.getBlockZ() + ":" + highPoints.getBlockX() + ":" + highPoints.getBlockY() + ":" + highPoints.getBlockZ();
    }

    /**
     * 从紧凑字符串读取区域坐标。
     *
     * @param root 坐标字符串，格式为 {@code lowX:lowY:lowZ:highX:highY:highZ}
     * @param world 区域所在世界名称
     * @return 读取出的区域对象
     * @throws Exception 坐标字符串为空、格式不正确或坐标无法转换为整数时抛出
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
     * 从旧版 Map 数据读取区域坐标。
     * <p>
     * 数据中应包含 {@code X1}、{@code Y1}、{@code Z1}、{@code X2}、{@code Y2}、{@code Z2}。
     * </p>
     *
     * @param root 保存区域坐标的 Map 数据
     * @param world 区域所在世界名称
     * @return 读取出的区域对象
     * @throws Exception 数据为空或坐标数据不完整时抛出
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
     * 获取当前区域覆盖的全部区块引用。
     * <p>
     * 该方法只按 X/Z 平面计算区块，Y 轴不会影响区块列表。
     * </p>
     *
     * @return 当前区域覆盖到的区块引用列表
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
     * 设置区域高点位置。
     *
     * @param highLocation 新的区域最大坐标点
     */
    public void setHighLocation(Location highLocation) {
        this.highPoints = highLocation.toVector();
        this.world = highLocation.getWorld();
    }

    /**
     * 设置区域高点向量。
     *
     * @param highLocation 新的区域最大坐标点
     */
    public void setHighVector(Vector highLocation) {
        this.highPoints = highLocation;
    }

    /**
     * 设置区域低点位置。
     *
     * @param lowLocation 新的区域最小坐标点
     */
    public void setLowLocation(Location lowLocation) {
        this.lowPoints = lowLocation.toVector();
        this.world = lowLocation.getWorld();
    }

    /**
     * 设置区域低点向量。
     *
     * @param lowLocation 新的区域最小坐标点
     */
    public void setLowVector(Vector lowLocation) {
        this.lowPoints = lowLocation;
    }

    /**
     * 根据权限组配置计算当前区域价格。
     *
     * @param group 用于读取单方块价格的权限组
     * @return 当前区域价格，保留到两位小数
     */
    public double getCost(PermissionGroup group) {
        return (long) (getSize() * group.getCostPerBlock() * 100L) / 100D;
    }
}
