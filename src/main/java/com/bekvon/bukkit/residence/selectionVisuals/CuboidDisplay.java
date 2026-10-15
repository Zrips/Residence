package com.bekvon.bukkit.residence.selectionVisuals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.selection.VisualizerConfig;

import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Container.CMIVector3D;
import net.Zrips.CMILib.Cuboids.CMIBlockArea;
import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class CuboidDisplay {

    private final CMIBlockWorldArea area;
    private final Player player;

    private CMIMaterial edgeMaterial = CMIMaterial.BLUE_WOOL;
    private CMIMaterial sideMaterial = CMIMaterial.GREEN_WOOL;

    private CMITask scheduler;

    private int range = VisualizerConfig.getRange();
    private double gridSize = CuboidDisplayType.SELECTION.getGridSize();
    private double lineThickness = CuboidDisplayType.SELECTION.getLineThickness();
    private double edgeLineThickness = CuboidDisplayType.SELECTION.getEdgeThickness();

    private static final double CALCULATION_BUFFER = 32.0;
    private static final double CALCULATION_REBUILD_DISTANCE = 1.0;

    private static final byte ORIENTATION_X = 0;
    private static final byte ORIENTATION_Y = 1;
    private static final byte ORIENTATION_Z = 2;

    private CMIBlockArea calculation;
    private CMIVector3D calculationCenter;
    private World calculationWorld;

    private boolean calculationBoundsInitialized;

    private final Map<DisplayKey, CMIBlockDisplay> edgeDisplays = new ConcurrentHashMap<>();
    private final Map<DisplayKey, CMIBlockDisplay> faceDisplays = new ConcurrentHashMap<>();

    private static final class DisplayKey {

        private final long x;
        private final long y;
        private final long z;
        private final byte orientation;

        private DisplayKey(double x, double y, double z, byte orientation) {
            this.x = Double.doubleToLongBits(x);
            this.y = Double.doubleToLongBits(y);
            this.z = Double.doubleToLongBits(z);
            this.orientation = orientation;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, orientation);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;

            if (!(obj instanceof DisplayKey))
                return false;

            DisplayKey other = (DisplayKey) obj;

            return x == other.x && y == other.y && z == other.z && orientation == other.orientation;
        }
    }

    private static final class GridLine {

        private final double start;
        private final double end;
        private final double fixed;
        private final boolean alongX;

        private GridLine(double start, double end, double fixed, boolean alongX) {

            this.start = start;
            this.end = end;
            this.fixed = fixed;
            this.alongX = alongX;
        }
    }

    public CuboidDisplay(Player player, CMIBlockWorldArea area) {
        this.player = player;
        this.area = area;
    }

    public double getLineThickness() {
        return lineThickness;
    }

    public CuboidDisplay setLineThickness(double thickness) {
        lineThickness = thickness;
        return this;
    }

    public double getEdgeThickness() {
        return edgeLineThickness;
    }

    public CuboidDisplay setEdgeThickness(double thickness) {
        edgeLineThickness = thickness;
        return this;
    }

    public double getGridSize() {
        return gridSize;
    }

    public CuboidDisplay setGridSize(double size) {
        gridSize = CMINumber.clamp(size, 0.1);
        return this;
    }

    public int getRange() {
        return range;
    }

    public CuboidDisplay setRange(int range) {
        this.range = CMINumber.clamp(range, 1);
        return this;
    }

    public CMIBlockWorldArea getArea() {
        return area;
    }

    public World getWorld() {
        return area.getWorld().getWorld();
    }

    public CMIMaterial getEdgeMaterial() {
        return edgeMaterial;
    }

    public CuboidDisplay setEdgeMaterial(CMIMaterial edgeMaterial) {
        this.edgeMaterial = edgeMaterial;
        return this;
    }

    public CMIMaterial getSideMaterial() {
        return sideMaterial;
    }

    public CuboidDisplay setSideMaterial(CMIMaterial sideMaterial) {
        this.sideMaterial = sideMaterial;
        return this;
    }

    public CuboidDisplay removeAfter(double seconds) {
        callScheduler((long) (seconds * 20));
        return this;
    }

    private void callScheduler(long ticks) {
        ticks = CMINumber.clamp(ticks, 1L);

        if (scheduler != null)
            scheduler.cancel();

        scheduler = CMIScheduler.runTaskLater(Residence.getInstance(), () -> {
            clear();
            CuboidDisplayManager.remove(player, area);
            scheduler = null;
        }, ticks);
    }

    private CMIBlockDisplay createDisplay(Location location, Material material) {

        CMIBlockDisplay display = new CMIBlockDisplay();

        CMIScheduler.runAtLocation(Residence.getInstance(), location, () -> {
            display.init(location, material);
            display.show(player);
        });

        return display;
    }

    private CMIBlockDisplay getOrCreateDisplay(Map<DisplayKey, CMIBlockDisplay> cache, DisplayKey key, Location spawnLocation, Material material) {

        CMIBlockDisplay display = cache.get(key);

        if (display != null)
            return display;

        Iterator<Map.Entry<DisplayKey, CMIBlockDisplay>> iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<DisplayKey, CMIBlockDisplay> entry = iterator.next();
            DisplayKey existingKey = entry.getKey();

            if (existingKey.orientation != key.orientation)
                continue;

            if (existingKey.x != key.x || existingKey.y != key.y || existingKey.z != key.z)
                continue;

            display = entry.getValue();
            iterator.remove();
            cache.put(key, display);
            return display;
        }

        display = createDisplay(spawnLocation, material);
        cache.put(key, display);

        return display;
    }

    private void updateDisplay(Map<DisplayKey, CMIBlockDisplay> cache, DisplayKey key, Location spawnLocation, Material material, double x, double y, double z, double scaleX, double scaleY, double scaleZ) {

        CMIBlockDisplay display = getOrCreateDisplay(cache, key, spawnLocation, material);

        if (display.setVisual(x, y, z, (float) scaleX, (float) scaleY, (float) scaleZ))
            display.show(player);
    }

    private void hideUnusedDisplays(Map<DisplayKey, CMIBlockDisplay> cache, Set<DisplayKey> used) {
        for (Map.Entry<DisplayKey, CMIBlockDisplay> entry : cache.entrySet()) {
            if (!used.contains(entry.getKey())) {
                entry.getValue().hide(player);
            }
        }
    }

    private void updateCalculationBounds(boolean force) {
        if (!force && !needsCalculationBoundsUpdate())
            return;

        Location location = player.getEyeLocation();

        double radius = getRange() + CALCULATION_BUFFER;

        double areaMinX = area.getLowPoint().getX();
        double areaMinY = area.getLowPoint().getY();
        double areaMinZ = area.getLowPoint().getZ();

        double areaMaxX = area.getHighPoint().getX() + 1;
        double areaMaxY = area.getHighPoint().getY() + 1;
        double areaMaxZ = area.getHighPoint().getZ() + 1;

        double calculationMinX = Math.max(areaMinX, location.getX() - radius);
        double calculationMaxX = Math.min(areaMaxX, location.getX() + radius);
        double calculationMinY = Math.max(areaMinY, location.getY() - radius);
        double calculationMaxY = Math.min(areaMaxY, location.getY() + radius);
        double calculationMinZ = Math.max(areaMinZ, location.getZ() - radius);
        double calculationMaxZ = Math.min(areaMaxZ, location.getZ() + radius);

        calculation = new CMIBlockArea(new Vector(calculationMinX, calculationMinY, calculationMinZ), new Vector(calculationMaxX, calculationMaxY, calculationMaxZ));

        calculationCenter = new CMIVector3D(location.toVector());
        calculationWorld = location.getWorld();

        calculationBoundsInitialized = true;
    }

    private boolean needsCalculationBoundsUpdate() {
        if (!calculationBoundsInitialized)
            return true;

        Location location = player.getEyeLocation();

        if (location.getWorld() != calculationWorld)
            return true;

        double dx = location.getX() - calculationCenter.getX();
        double dy = location.getY() - calculationCenter.getY();
        double dz = location.getZ() - calculationCenter.getZ();

        return dx * dx + dy * dy + dz * dz >= CALCULATION_REBUILD_DISTANCE * CALCULATION_REBUILD_DISTANCE;
    }

    public void recalculate(boolean force) {
        if (player == null || !player.isOnline())
            return;

        updateCalculationBounds(force);

        double minX = area.getLowPoint().getX();
        double minY = area.getLowPoint().getY();
        double minZ = area.getLowPoint().getZ();

        double maxX = area.getHighPoint().getX() + 1;
        double maxY = area.getHighPoint().getY() + 1;
        double maxZ = area.getHighPoint().getZ() + 1;

        /*
         * Keep the logical edges independent of their visible lengths.
         */
        Set<DisplayKey> usedEdgeDisplays = new HashSet<>();

        updateHorizontalEdge(minX, maxX, minZ, minY, true, usedEdgeDisplays);
        updateHorizontalEdge(minX, maxX, maxZ, minY, true, usedEdgeDisplays);
        updateHorizontalEdge(minZ, maxZ, minX, minY, false, usedEdgeDisplays);
        updateHorizontalEdge(minZ, maxZ, maxX, minY, false, usedEdgeDisplays);
        updateHorizontalEdge(minX, maxX, minZ, maxY, true, usedEdgeDisplays);
        updateHorizontalEdge(minX, maxX, maxZ, maxY, true, usedEdgeDisplays);
        updateHorizontalEdge(minZ, maxZ, minX, maxY, false, usedEdgeDisplays);
        updateHorizontalEdge(minZ, maxZ, maxX, maxY, false, usedEdgeDisplays);

        updateVerticalEdge(minX, minZ, minY, maxY, usedEdgeDisplays);
        updateVerticalEdge(minX, maxZ, minY, maxY, usedEdgeDisplays);
        updateVerticalEdge(maxX, minZ, minY, maxY, usedEdgeDisplays);
        updateVerticalEdge(maxX, maxZ, minY, maxY, usedEdgeDisplays);

        /*
         * Mark every face-grid display used during this recalculation. Anything not
         * marked will be hidden after all six faces update.
         */
        Set<DisplayKey> usedFaceDisplays = new HashSet<>();

        updateBottomGrid(minX, maxX, minY, minZ, maxZ, usedFaceDisplays);
        updateTopGrid(minX, maxX, maxY, minZ, maxZ, usedFaceDisplays);
        updateXFaceGrid(minX, minY, maxY, minZ, maxZ, usedFaceDisplays);
        updateXFaceGrid(maxX, minY, maxY, minZ, maxZ, usedFaceDisplays);
        updateZFaceGrid(minZ, minX, maxX, minY, maxY, usedFaceDisplays);
        updateZFaceGrid(maxZ, minX, maxX, minY, maxY, usedFaceDisplays);

        hideUnusedDisplays(faceDisplays, usedFaceDisplays);
        hideUnusedDisplays(edgeDisplays, usedEdgeDisplays);
    }

    /*
     * Horizontal edge.
     *
     * The key uses the original edge start, not visibleStart.
     */
    private void updateHorizontalEdge(double start, double end, double fixed, double y, boolean alongX, Set<DisplayKey> used) {

        double[] visible = getVisibleInterval(start, end, fixed, y, alongX);

        DisplayKey key = alongX ? new DisplayKey(start, y, fixed, ORIENTATION_X) : new DisplayKey(fixed, y, start, ORIENTATION_Z);

        CMIBlockDisplay existing = edgeDisplays.get(key);

        if (visible == null) {
            if (existing != null)
                existing.hide(player);
            return;
        }

        used.add(key);

        double visibleStart = visible[0];
        double visibleEnd = visible[1];
        double length = visibleEnd - visibleStart;

        double thickness = getEdgeThickness();

        Location spawnLocation;

        double visualX;
        double visualY = y - thickness / 2.0;
        double visualZ;
        double scaleX;
        double scaleY = thickness;
        double scaleZ;

        if (alongX) {
            spawnLocation = new Location(calculationWorld, start, visualY, fixed - thickness / 2.0);

            visualX = visibleStart;
            visualZ = fixed - thickness / 2.0;

            scaleX = length;
            scaleZ = thickness;
        } else {
            spawnLocation = new Location(calculationWorld, fixed - thickness / 2.0, visualY, start);

            visualX = fixed - thickness / 2.0;
            visualZ = visibleStart;

            scaleX = thickness;
            scaleZ = length;
        }

        updateDisplay(edgeDisplays, key, spawnLocation, edgeMaterial.getMaterial(), visualX, visualY, visualZ, scaleX, scaleY, scaleZ);
    }

    /*
     * Vertical edge.
     *
     * The canonical origin is (x, minY, z).
     */
    private void updateVerticalEdge(double x, double z, double minY, double maxY, Set<DisplayKey> used) {

        double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

        DisplayKey key = new DisplayKey(x, minY, z, ORIENTATION_Y);

        CMIBlockDisplay existing = edgeDisplays.get(key);

        if (visible == null) {
            if (existing != null)
                existing.hide(player);

            return;
        }

        used.add(key);

        double visibleStart = visible[0];
        double visibleEnd = visible[1];
        double length = visibleEnd - visibleStart;

        double thickness = getEdgeThickness();

        Location spawnLocation = new Location(calculationWorld, x - thickness / 2.0, minY, z - thickness / 2.0);

        updateDisplay(edgeDisplays, key, spawnLocation, edgeMaterial.getMaterial(), x - thickness / 2.0, visibleStart, z - thickness / 2.0, thickness, length, thickness);
    }

    private void updateBottomGrid(double minX, double maxX, double y, double minZ, double maxZ, Set<DisplayKey> used) {

        List<GridLine> lines = new ArrayList<>();

        double size = getGridSize();

        double firstZ = firstGridLine(minZ, calculation.getLowPoint().getZ(), calculation.getHighPoint().getZ());

        for (double z = firstZ; z < calculation.getHighPoint().getZ()
                && z < maxZ; z += size) {

            if (z < minZ)
                continue;

            double[] visible = getVisibleInterval(minX, maxX, z, y, true);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], z, true));
        }

        double firstX = firstGridLine(minX, calculation.getLowPoint().getX(), calculation.getHighPoint().getX());

        for (double x = firstX; x < calculation.getHighPoint().getX()
                && x < maxX; x += size) {

            if (x < minX)
                continue;

            double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], x, false));
        }

        updateHorizontalGridDisplays(lines, y, used);
    }

    private void updateTopGrid(double minX, double maxX, double y, double minZ, double maxZ, Set<DisplayKey> used) {

        List<GridLine> lines = new ArrayList<>();

        double size = getGridSize();

        double firstZ = firstGridLine(minZ, calculation.getLowPoint().getZ(), calculation.getHighPoint().getZ());

        for (double z = firstZ; z < calculation.getHighPoint().getZ() && z < maxZ; z += size) {

            if (z < minZ)
                continue;

            double[] visible = getVisibleInterval(minX, maxX, z, y, true);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], z, true));
        }

        double firstX = firstGridLine(minX, calculation.getLowPoint().getX(), calculation.getHighPoint().getX());

        for (double x = firstX; x < calculation.getHighPoint().getX()
                && x < maxX; x += size) {

            if (x < minX)
                continue;

            double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], x, false));
        }

        updateHorizontalGridDisplays(lines, y, used);
    }

    /*
     * Grid on an X-facing plane.
     *
     * alongY is used for vertical grid lines. alongZ is used for horizontal grid
     * lines.
     */
    private void updateXFaceGrid(double x, double minY, double maxY, double minZ, double maxZ, Set<DisplayKey> used) {

        List<GridLine> lines = new ArrayList<>();

        double size = getGridSize();

        double firstZ = firstGridLine(minZ, calculation.getLowPoint().getZ(), calculation.getHighPoint().getZ());

        for (double z = firstZ; z < calculation.getHighPoint().getZ()
                && z < maxZ; z += size) {

            if (z < minZ)
                continue;

            double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], z, true));
        }

        double firstY = firstGridLine(minY, calculation.getLowPoint().getY(), calculation.getHighPoint().getY());

        for (double y = firstY; y < calculation.getHighPoint().getY()
                && y < maxY; y += size) {

            if (y < minY)
                continue;

            double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], y, false));
        }

        updateXFaceDisplays(lines, x, minY, minZ, used);
    }

    /*
     * Grid on a Z-facing plane.
     */
    private void updateZFaceGrid(double z, double minX, double maxX, double minY, double maxY, Set<DisplayKey> used) {

        List<GridLine> lines = new ArrayList<>();

        double size = getGridSize();

        double firstX = firstGridLine(minX, calculation.getLowPoint().getX(), calculation.getHighPoint().getX());

        for (double x = firstX; x < calculation.getHighPoint().getX()
                && x < maxX; x += size) {

            if (x < minX)
                continue;

            double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], x, true));
        }

        double firstY = firstGridLine(minY, calculation.getLowPoint().getY(), calculation.getHighPoint().getY());

        for (double y = firstY; y < calculation.getHighPoint().getY()
                && y < maxY; y += size) {

            if (y < minY)
                continue;

            double[] visible = getVisibleInterval(minX, maxX, z, y, true);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], y, false));
        }

        updateZFaceDisplays(lines, z, minX, minY, used);
    }

    /*
     * Bottom and top face lines.
     *
     * X-directed line key: (minX, planeY, fixedZ, X) Z-directed line key: (fixedX,
     * planeY, minZ, Z)
     */
    private void updateHorizontalGridDisplays(List<GridLine> lines, double y, Set<DisplayKey> used) {

        double thickness = getLineThickness();
        Material material = sideMaterial.getMaterial();

        for (GridLine line : lines) {
            DisplayKey key;
            Location spawnLocation;

            double x;
            double visualY = y - thickness / 2.0;
            double z;
            double scaleX;
            double scaleY = thickness;
            double scaleZ;

            if (line.alongX) {
                key = new DisplayKey(area.getLowPoint().getX(), y, line.fixed, ORIENTATION_X);

                spawnLocation = new Location(calculationWorld, area.getLowPoint().getX(), visualY, line.fixed - thickness / 2.0);

                x = line.start;
                z = line.fixed - thickness / 2.0;
                scaleX = line.end - line.start;
                scaleZ = thickness;
            } else {
                key = new DisplayKey(line.fixed, y, area.getLowPoint().getZ(), ORIENTATION_Z);

                spawnLocation = new Location(calculationWorld, line.fixed - thickness / 2.0, visualY, area.getLowPoint().getZ());

                x = line.fixed - thickness / 2.0;
                z = line.start;
                scaleX = thickness;
                scaleZ = line.end - line.start;
            }

            used.add(key);

            updateDisplay(faceDisplays, key, spawnLocation, material, x, visualY, z, scaleX, scaleY, scaleZ);
        }
    }

    /*
     * X-facing plane lines.
     *
     * Vertical line key: (faceX, minY, fixedZ, Y) Z-directed line key: (faceX,
     * fixedY, minZ, Z)
     */
    private void updateXFaceDisplays(List<GridLine> lines, double x, double minY, double minZ, Set<DisplayKey> used) {

        double thickness = getLineThickness();
        Material material = sideMaterial.getMaterial();

        for (GridLine line : lines) {
            DisplayKey key;
            Location spawnLocation;

            double visualX = x - thickness / 2.0;
            double y;
            double z;
            double scaleX = thickness;
            double scaleY;
            double scaleZ;

            if (line.alongX) {
                // This GridLine represents a vertical Y-directed line.
                key = new DisplayKey(x, minY, line.fixed, ORIENTATION_Y);

                spawnLocation = new Location(calculationWorld, visualX, minY, line.fixed - thickness / 2.0);

                y = line.start;
                z = line.fixed - thickness / 2.0;
                scaleY = line.end - line.start;
                scaleZ = thickness;
            } else {
                // This GridLine represents a Z-directed line.
                key = new DisplayKey(x, line.fixed, minZ, ORIENTATION_Z);

                spawnLocation = new Location(calculationWorld, visualX, line.fixed - thickness / 2.0, minZ);

                y = line.fixed - thickness / 2.0;
                z = line.start;
                scaleY = thickness;
                scaleZ = line.end - line.start;
            }

            used.add(key);

            updateDisplay(faceDisplays, key, spawnLocation, material, visualX, y, z, scaleX, scaleY, scaleZ);
        }
    }

    /*
     * Z-facing plane lines.
     *
     * Vertical line key: (fixedX, minY, faceZ, Y) X-directed line key: (minX,
     * fixedY, faceZ, X)
     */
    private void updateZFaceDisplays(List<GridLine> lines, double z, double minX, double minY, Set<DisplayKey> used) {

        double thickness = getLineThickness();
        Material material = sideMaterial.getMaterial();

        for (GridLine line : lines) {
            DisplayKey key;
            Location spawnLocation;

            double x;
            double y;
            double visualZ = z - thickness / 2.0;
            double scaleX;
            double scaleY;
            double scaleZ = thickness;

            if (line.alongX) {
                // This GridLine represents a vertical Y-directed line.
                key = new DisplayKey(line.fixed, minY, z, ORIENTATION_Y);

                spawnLocation = new Location(calculationWorld, line.fixed - thickness / 2.0, minY, visualZ);

                x = line.fixed - thickness / 2.0;
                y = line.start;
                scaleX = thickness;
                scaleY = line.end - line.start;
            } else {
                // This GridLine represents an X-directed line.
                key = new DisplayKey(minX, line.fixed, z, ORIENTATION_X);

                spawnLocation = new Location(calculationWorld, minX, line.fixed - thickness / 2.0, visualZ);

                x = line.start;
                y = line.fixed - thickness / 2.0;
                scaleX = line.end - line.start;
                scaleY = thickness;
            }

            used.add(key);

            updateDisplay(faceDisplays, key, spawnLocation, material, x, y, visualZ, scaleX, scaleY, scaleZ);
        }
    }

    private double[] getVisibleInterval(double start, double end, double fixed, double y, boolean alongX) {

        double playerAxis = alongX ? calculationCenter.getX() : calculationCenter.getZ();

        double playerFixed = alongX ? calculationCenter.getZ() : calculationCenter.getX();

        double dy = calculationCenter.getY() - y;
        double df = playerFixed - fixed;

        double perpendicularSquared = dy * dy + df * df;
        double radiusSquared = (double) getRange() * getRange();

        if (perpendicularSquared > radiusSquared)
            return null;

        double alongDistance = Math.sqrt(radiusSquared - perpendicularSquared);
        double visibleStart = Math.max(start, playerAxis - alongDistance);
        double visibleEnd = Math.min(end, playerAxis + alongDistance);

        if (visibleStart >= visibleEnd)
            return null;

        return new double[] { visibleStart, visibleEnd };
    }

    private double[] getVisibleVerticalInterval(double x, double z, double minY, double maxY) {

        double dx = calculationCenter.getX() - x;
        double dz = calculationCenter.getZ() - z;

        double horizontalSquared = dx * dx + dz * dz;
        double radiusSquared = (double) getRange() * getRange();

        if (horizontalSquared > radiusSquared)
            return null;

        double verticalDistance = Math.sqrt(radiusSquared - horizontalSquared);
        double visibleStart = Math.max(minY, calculationCenter.getY() - verticalDistance);
        double visibleEnd = Math.min(maxY, calculationCenter.getY() + verticalDistance);

        if (visibleStart >= visibleEnd)
            return null;

        return new double[] { visibleStart, visibleEnd };
    }

    private double firstGridLine(double originalMin, double calculationMin, double calculationMax) {

        double size = getGridSize();

        double first = (Math.floor(originalMin / size) + 1) * size;

        if (first < calculationMin) {
            double steps = Math.ceil((calculationMin - first) / size);
            first += steps * size;
        }

        return first;
    }

    public void show() {

        for (CMIBlockDisplay display : edgeDisplays.values())
            display.show(player);

        for (CMIBlockDisplay display : faceDisplays.values())
            display.show(player);
    }

    public void hide(Player player) {
        for (CMIBlockDisplay display : edgeDisplays.values())
            display.hide(player);

        for (CMIBlockDisplay display : faceDisplays.values())
            display.hide(player);
    }

    public void clear() {
        for (CMIBlockDisplay display : edgeDisplays.values())
            display.remove();

        for (CMIBlockDisplay display : faceDisplays.values())
            display.remove();

        edgeDisplays.clear();
        faceDisplays.clear();

        if (scheduler != null) {
            scheduler.cancel();
            scheduler = null;
        }

        calculation = null;
        calculationCenter = null;
        calculationWorld = null;
        calculationBoundsInitialized = false;
    }
}