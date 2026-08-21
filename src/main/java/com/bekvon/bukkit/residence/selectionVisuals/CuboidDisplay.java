package com.bekvon.bukkit.residence.selectionVisuals;

import java.util.ArrayList;
import java.util.List;

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
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class CuboidDisplay {

    private final CMIBlockWorldArea area;

    private CMIMaterial edgeMaterial = CMIMaterial.BLUE_WOOL;
    private CMIMaterial sideMaterial = CMIMaterial.GREEN_WOOL;

    private final Player player;

    private CMITask scheduler = null;

    private int range = VisualizerConfig.getRange();
    private double gridSize = VisualizerConfig.getGridSize();
    private double lineThickness = VisualizerConfig.getLineThickness();

    private static final double CALCULATION_BUFFER = 32.0;
    private static final double CALCULATION_REBUILD_DISTANCE = 4.0;

    private CMIBlockArea calculation;

    private CMIVector3D calculationCenter;
    private World calculationWorld;

    private boolean calculationBoundsInitialized = false;

    /*
     * 0-3 = bottom horizontal edges 4-7 = top horizontal edges 8-11 = vertical
     * edges
     */
    private List<CMIBlockDisplay>[] edgeDisplays = null;

    /*
     * Face grid lines.
     *
     * 0 = bottom 1 = top 2 = min X 3 = max X 4 = min Z 5 = max Z
     */
    private List<CMIBlockDisplay>[] faceDisplays = null;

    public CuboidDisplay(Player player, CMIBlockWorldArea area) {
        this.area = area;
        this.player = player;
    }

    public double getLineThickness() {
        return lineThickness;
    }

    public CuboidDisplay setLineThickness(double thickness) {
        lineThickness = thickness;
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

    public CMIBlockWorldArea getArea() {
        return area;
    }

    public World getWorld() {
        return area.getWorld().getWorld();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        edgeDisplays = new List[12];

        for (int i = 0; i < edgeDisplays.length; i++)
            edgeDisplays[i] = new ArrayList<>();

        faceDisplays = new List[6];

        for (int i = 0; i < faceDisplays.length; i++)
            faceDisplays[i] = new ArrayList<>();
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

        double dx = location.getX() - calculationCenter.getX();
        double dy = location.getY() - calculationCenter.getY();
        double dz = location.getZ() - calculationCenter.getZ();

        return dx * dx + dy * dy + dz * dz >= CALCULATION_REBUILD_DISTANCE * CALCULATION_REBUILD_DISTANCE;
    }

    public void recalculate() {
        if (player == null || !player.isOnline())
            return;

        if (edgeDisplays == null)
            init();

        updateCalculationBounds(false);

        /*
         * Actual selection boundaries.
         *
         * These must remain the real selection boundaries because the edges and the
         * actual length of every grid line belong to the selection.
         */
        double minX = area.getLowPoint().getX();
        double minY = area.getLowPoint().getY();
        double minZ = area.getLowPoint().getZ();

        double maxX = area.getHighPoint().getX() + 1;
        double maxY = area.getHighPoint().getY() + 1;
        double maxZ = area.getHighPoint().getZ() + 1;

        /*
         * Bottom
         */
        updateHorizontalEdge(0, minX, maxX, minZ, minY, true);
        updateHorizontalEdge(1, minX, maxX, maxZ, minY, true);
        updateHorizontalEdge(2, minZ, maxZ, minX, minY, false);
        updateHorizontalEdge(3, minZ, maxZ, maxX, minY, false);

        /*
         * Top
         */
        updateHorizontalEdge(4, minX, maxX, minZ, maxY, true);
        updateHorizontalEdge(5, minX, maxX, maxZ, maxY, true);
        updateHorizontalEdge(6, minZ, maxZ, minX, maxY, false);
        updateHorizontalEdge(7, minZ, maxZ, maxX, maxY, false);

        /*
         * Vertical
         */
        updateVerticalEdge(8, minX, minZ, minY, maxY);
        updateVerticalEdge(9, minX, maxZ, minY, maxY);
        updateVerticalEdge(10, maxX, minZ, minY, maxY);
        updateVerticalEdge(11, maxX, maxZ, minY, maxY);

        /*
         * Grid calculations are restricted to the local calculation area.
         */
        updateBottomGrid(minX, maxX, minY, minZ, maxZ);
        updateTopGrid(minX, maxX, maxY, minZ, maxZ);

        updateXFaceGrid(2, minX, minY, maxY, minZ, maxZ);
        updateXFaceGrid(3, maxX, minY, maxY, minZ, maxZ);

        updateZFaceGrid(4, minZ, minX, maxX, minY, maxY);
        updateZFaceGrid(5, maxZ, minX, maxX, minY, maxY);
    }

    private void updateHorizontalEdge(int edgeIndex, double start, double end, double fixed, double y, boolean alongX) {

        double[] visible = getVisibleInterval(start, end, fixed, y, alongX);

        List<CMIBlockDisplay> displays = edgeDisplays[edgeIndex];

        if (visible == null) {
            for (CMIBlockDisplay display : displays)
                display.hide(player);

            return;
        }

        double visibleStart = visible[0];
        double visibleEnd = visible[1];
        double length = visibleEnd - visibleStart;

        CMIBlockDisplay display;

        double thickness = getLineThickness();

        if (displays.isEmpty()) {
            if (alongX) {
                display = createDisplay(player, new Location(calculationWorld, visibleStart, y - thickness / 2.0, fixed - thickness / 2.0), edgeMaterial.getMaterial());
            } else {
                display = createDisplay(player, new Location(calculationWorld, fixed - thickness / 2.0, y - thickness / 2.0, visibleStart), edgeMaterial.getMaterial());
            }

            displays.add(display);
        }

        display = displays.get(0);

        if (alongX) {
            display.setVisual(visibleStart, y - thickness / 2.0, fixed - thickness / 2.0, (float) length, (float) thickness, (float) thickness);
        } else {
            display.setVisual(fixed - thickness / 2.0, y - thickness / 2.0, visibleStart, (float) thickness, (float) thickness, (float) length);
        }
    }

    private void updateVerticalEdge(int edgeIndex, double x, double z, double minY, double maxY) {

        double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

        List<CMIBlockDisplay> displays = edgeDisplays[edgeIndex];

        if (visible == null) {
            for (CMIBlockDisplay display : displays)
                display.hide(player);

            return;
        }

        double visibleStart = visible[0];
        double visibleEnd = visible[1];
        double length = visibleEnd - visibleStart;

        CMIBlockDisplay display;

        double thickness = getLineThickness();

        if (displays.isEmpty()) {
            display = createDisplay(player, new Location(calculationWorld, x - thickness / 2.0, visibleStart, z - thickness / 2.0), edgeMaterial.getMaterial());
            displays.add(display);
        }

        display = displays.get(0);

        display.setVisual(x - thickness / 2.0, visibleStart, z - thickness / 2.0, (float) thickness, (float) length, (float) thickness);
    }

    private void updateBottomGrid(double minX, double maxX, double y, double minZ, double maxZ) {
        List<CMIBlockDisplay> displays = faceDisplays[0];

        List<GridLine> lines = new ArrayList<>();

        double gridSize = getGridSize();

        double firstZ = firstGridLine(minZ, calculation.getLowPoint().getZ(), calculation.getHighPoint().getZ());

        for (double z = firstZ; z <= calculation.getHighPoint().getZ() && z <= maxZ; z += gridSize) {
            if (z < minZ)
                continue;

            double[] visible = getVisibleInterval(minX, maxX, z, y, true);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], z, true));
        }

        double firstX = firstGridLine(minX, calculation.getLowPoint().getX(), calculation.getHighPoint().getX());

        for (double x = firstX; x <= calculation.getHighPoint().getX() && x <= maxX; x += gridSize) {
            if (x < minX)
                continue;

            double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], x, false));
        }

        updateGridDisplays(displays, lines, y);
    }

    private void updateTopGrid(double minX, double maxX, double y, double minZ, double maxZ) {
        List<CMIBlockDisplay> displays = faceDisplays[1];

        List<GridLine> lines = new ArrayList<>();

        double gridSize = getGridSize();

        if (maxZ - minZ > gridSize) {
            double firstZ = firstGridLine(minZ, calculation.getLowPoint().getZ(), calculation.getHighPoint().getZ());

            for (double z = firstZ; z < calculation.getHighPoint().getZ() && z < maxZ; z += gridSize) {
                if (z < minZ)
                    continue;

                double[] visible = getVisibleInterval(minX, maxX, z, y, true);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], z, true));
            }
        }

        if (maxX - minX > gridSize) {
            double firstX = firstGridLine(minX, calculation.getLowPoint().getX(), calculation.getHighPoint().getX());

            for (double x = firstX; x < calculation.getHighPoint().getX() && x < maxX; x += gridSize) {
                if (x < minX)
                    continue;

                double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], x, false));
            }
        }

        updateGridDisplays(displays, lines, y);
    }

    private void updateXFaceGrid(int faceIndex, double x, double minY, double maxY, double minZ, double maxZ) {
        List<CMIBlockDisplay> displays = faceDisplays[faceIndex];

        List<GridLine> lines = new ArrayList<>();

        double gridSize = getGridSize();

        if (maxZ - minZ > gridSize) {
            double firstZ = firstGridLine(minZ, calculation.getLowPoint().getZ(), calculation.getHighPoint().getZ());

            for (double z = firstZ; z < calculation.getHighPoint().getZ() && z < maxZ; z += gridSize) {
                if (z < minZ)
                    continue;

                double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], z, true));
            }
        }

        if (maxY - minY > gridSize) {
            double firstY = firstGridLine(minY, calculation.getLowPoint().getY(), calculation.getHighPoint().getY());

            for (double y = firstY; y < calculation.getHighPoint().getY() && y < maxY; y += gridSize) {
                if (y < minY)
                    continue;

                double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], y, false));
            }
        }

        updateXFaceDisplays(displays, lines, x);
    }

    private void updateZFaceGrid(int faceIndex, double z, double minX, double maxX, double minY, double maxY) {
        List<CMIBlockDisplay> displays = faceDisplays[faceIndex];

        List<GridLine> lines = new ArrayList<>();

        double gridSize = getGridSize();

        if (maxX - minX > gridSize) {
            double firstX = firstGridLine(minX, calculation.getLowPoint().getX(), calculation.getHighPoint().getX());

            for (double x = firstX; x < calculation.getHighPoint().getX() && x < maxX; x += gridSize) {
                if (x < minX)
                    continue;

                double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], x, true));
            }
        }

        if (maxY - minY > gridSize) {
            double firstY = firstGridLine(minY, calculation.getLowPoint().getY(), calculation.getHighPoint().getY());

            for (double y = firstY; y < calculation.getHighPoint().getY() && y < maxY; y += gridSize) {
                if (y < minY)
                    continue;

                double[] visible = getVisibleInterval(minX, maxX, z, y, true);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], y, false));
            }
        }

        updateZFaceDisplays(displays, lines, z);
    }

    private void updateGridDisplays(CMIBlockDisplay display, double scaleX, double scaleY, double scaleZ, double locationX, double locationY, double locationZ) {
        double thickness = getLineThickness();

        display.setVisual(
                locationX - thickness / 2.0,
                locationY - thickness / 2.0,
                locationZ - thickness / 2.0,
                (float) scaleX,
                (float) scaleY,
                (float) scaleZ);
    }

    private void updateGridDisplays(List<CMIBlockDisplay> displays, List<GridLine> lines, double y) {
        Material material = sideMaterial.getMaterial();

        resizeHorizontalGridDisplays(displays, lines, y, material);

        for (int i = 0; i < displays.size(); i++) {
            CMIBlockDisplay display = displays.get(i);

            if (i >= lines.size()) {
                display.hide(player);
                continue;
            }

            display.show(player);

            GridLine line = lines.get(i);

            double thickness = getLineThickness();

            if (line.alongX) {
                updateGridDisplays(display, line.end - line.start, thickness, thickness, line.start, y, line.fixed);
            } else {
                updateGridDisplays(display, thickness, thickness, line.end - line.start, line.fixed, y, line.start);
            }
        }
    }

    private void updateXFaceDisplays(List<CMIBlockDisplay> displays, List<GridLine> lines, double x) {
        Material material = sideMaterial.getMaterial();

        resizeXFaceDisplays(displays, lines, x, material);

        for (int i = 0; i < displays.size(); i++) {
            CMIBlockDisplay display = displays.get(i);

            if (i >= lines.size()) {
                display.hide(player);
                continue;
            }

            display.show(player);

            GridLine line = lines.get(i);

            double thickness = getLineThickness();

            if (line.alongX) {
                updateGridDisplays(display, thickness, line.end - line.start, thickness, x, line.start, line.fixed);
            } else {
                updateGridDisplays(display, thickness, thickness, line.end - line.start, x, line.fixed, line.start);
            }
        }
    }

    private void updateZFaceDisplays(List<CMIBlockDisplay> displays, List<GridLine> lines, double z) {
        Material material = sideMaterial.getMaterial();

        resizeZFaceDisplays(displays, lines, z, material);

        for (int i = 0; i < displays.size(); i++) {
            CMIBlockDisplay display = displays.get(i);

            if (i >= lines.size()) {
                display.hide(player);
                continue;
            }

            display.show(player);

            GridLine line = lines.get(i);

            double thickness = getLineThickness();

            if (line.alongX) {
                updateGridDisplays(display, thickness, line.end - line.start, thickness, line.fixed, line.start, z);
            } else {
                updateGridDisplays(display, line.end - line.start, thickness, thickness, line.start, line.fixed, z);
            }
        }
    }

    private void resizeHorizontalGridDisplays(List<CMIBlockDisplay> displays, List<GridLine> lines, double y, Material material) {
        while (displays.size() < lines.size()) {
            GridLine line = lines.get(displays.size());

            Location location;

            if (line.alongX) {
                location = new Location(player.getWorld(), line.start, y, line.fixed);
            } else {
                location = new Location(player.getWorld(), line.fixed, y, line.start);
            }

            CMIBlockDisplay display = createDisplay(player, location, material);
            displays.add(display);
        }
    }

    private void resizeXFaceDisplays(List<CMIBlockDisplay> displays, List<GridLine> lines, double x, Material material) {
        while (displays.size() < lines.size()) {
            GridLine line = lines.get(displays.size());

            Location location;

            if (line.alongX) {
                location = new Location(player.getWorld(), x, line.start, line.fixed);
            } else {
                location = new Location(player.getWorld(), x, line.fixed, line.start);
            }

            CMIBlockDisplay display = createDisplay(player, location, material);
            displays.add(display);
        }
    }

    private CMIBlockDisplay createDisplay(Player player, Location location, Material material) {
        CMIBlockDisplay display = new CMIBlockDisplay();

        CMIScheduler.runAtLocation(Residence.getInstance(), location, () -> {
            display.init(location, material);
            display.show(player);
        });

        return display;
    }

    private void resizeZFaceDisplays(List<CMIBlockDisplay> displays, List<GridLine> lines, double z, Material material) {
        while (displays.size() < lines.size()) {
            GridLine line = lines.get(displays.size());

            Location location;

            if (line.alongX) {
                location = new Location(player.getWorld(), line.fixed, line.start, z);
            } else {
                location = new Location(player.getWorld(), line.start, line.fixed, z);
            }

            CMIBlockDisplay display = createDisplay(player, location, material);
            displays.add(display);
        }
    }

    private double[] getVisibleInterval(double start, double end, double fixed, double y, boolean alongX) {

        double playerAxis = alongX ? calculationCenter.getX() : calculationCenter.getZ();
        double playerFixed = alongX ? calculationCenter.getZ() : calculationCenter.getX();

        double dy = calculationCenter.getY() - y;
        double df = playerFixed - fixed;

        double perpendicularSquared = dy * dy + df * df;
        double radiusSquared = getRange() * getRange();

        if (perpendicularSquared > radiusSquared)
            return null;

        double alongDistance = Math.sqrt(radiusSquared - perpendicularSquared);

        double visibleStart = Math.max(start, playerAxis - alongDistance);
        double visibleEnd = Math.min(end, playerAxis + alongDistance);

        if (visibleStart >= visibleEnd)
            return null;

        return new double[] {
                visibleStart,
                visibleEnd
        };
    }

    private double[] getVisibleVerticalInterval(double x, double z, double minY, double maxY) {

        double dx = calculationCenter.getX() - x;
        double dz = calculationCenter.getZ() - z;

        double horizontalSquared = dx * dx + dz * dz;
        double radiusSquared = getRange() * getRange();

        if (horizontalSquared > radiusSquared)
            return null;

        double verticalDistance = Math.sqrt(radiusSquared - horizontalSquared);

        double visibleStart = Math.max(minY, calculationCenter.getY() - verticalDistance);
        double visibleEnd = Math.min(maxY, calculationCenter.getY() + verticalDistance);

        if (visibleStart >= visibleEnd)
            return null;

        return new double[] {
                visibleStart,
                visibleEnd
        };
    }

    private double firstGridLine(double originalMin, double calculationMin, double calculationMax) {
        double first = (Math.floor(originalMin / getGridSize()) + 1) * getGridSize();

        if (first < calculationMin) {
            double steps = Math.ceil((calculationMin - first) / getGridSize());
            first += steps * getGridSize();
        }

        return first;
    }

    public void show() {
        if (edgeDisplays == null)
            init();

        if (!calculationBoundsInitialized)
            updateCalculationBounds(true);

        for (List<CMIBlockDisplay> displays : edgeDisplays) {
            for (CMIBlockDisplay display : displays) {
                display.show(player);
            }
        }

        for (List<CMIBlockDisplay> displays : faceDisplays) {
            for (CMIBlockDisplay display : displays) {
                display.show(player);
            }
        }
    }

    public void hide(Player player) {
        if (edgeDisplays == null)
            return;

        for (List<CMIBlockDisplay> displays : edgeDisplays) {
            for (CMIBlockDisplay display : displays)
                display.hide(player);
        }

        for (List<CMIBlockDisplay> displays : faceDisplays) {
            for (CMIBlockDisplay display : displays)
                display.hide(player);
        }
    }

    public void clear() {
        if (edgeDisplays == null)
            return;

        for (List<CMIBlockDisplay> displays : edgeDisplays) {
            for (CMIBlockDisplay display : displays)
                display.remove();

            displays.clear();
        }

        for (List<CMIBlockDisplay> displays : faceDisplays) {
            for (CMIBlockDisplay display : displays)
                display.remove();

            displays.clear();
        }

        if (scheduler != null) {
            scheduler.cancel();
            scheduler = null;
        }

        calculationBoundsInitialized = false;
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

    private static class GridLine {

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
}