package com.bekvon.bukkit.residence.selectionVisuals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.selection.VisualizerConfig;

import net.Zrips.CMILib.Container.CMINumber;
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

    int range = VisualizerConfig.getRange();
    int gridSize = VisualizerConfig.getGridSize();
    double lineThickness = VisualizerConfig.getLineThickness();

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

    public int getGridSize() {
        return gridSize;
    }

    public CuboidDisplay setGridSize(int size) {
        gridSize = size;
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

    private void init() {

        edgeDisplays = new List[12];

        for (int i = 0; i < edgeDisplays.length; i++)
            edgeDisplays[i] = new ArrayList<>();

        faceDisplays = new List[6];

        for (int i = 0; i < faceDisplays.length; i++)
            faceDisplays[i] = new ArrayList<>();
    }

    public void recalculate() {

        if (scheduler == null || player == null || !player.isOnline())
            return;

        if (edgeDisplays == null)
            init();

        double minX = area.getLowPoint().getX();
        double minY = area.getLowPoint().getY();
        double minZ = area.getLowPoint().getZ();

        double maxX = area.getHighPoint().getX() + 1;
        double maxY = area.getHighPoint().getY() + 1;
        double maxZ = area.getHighPoint().getZ() + 1;

        // Bottom
        updateHorizontalEdge(0, minX, maxX, minZ, minY, true);
        updateHorizontalEdge(1, minX, maxX, maxZ, minY, true);
        updateHorizontalEdge(2, minZ, maxZ, minX, minY, false);
        updateHorizontalEdge(3, minZ, maxZ, maxX, minY, false);

        // Top
        updateHorizontalEdge(4, minX, maxX, minZ, maxY, true);
        updateHorizontalEdge(5, minX, maxX, maxZ, maxY, true);
        updateHorizontalEdge(6, minZ, maxZ, minX, maxY, false);
        updateHorizontalEdge(7, minZ, maxZ, maxX, maxY, false);

        // Vertical
        updateVerticalEdge(8, minX, minZ, minY, maxY);
        updateVerticalEdge(9, minX, maxZ, minY, maxY);
        updateVerticalEdge(10, maxX, minZ, minY, maxY);
        updateVerticalEdge(11, maxX, maxZ, minY, maxY);

        updateBottomGrid(minX, maxX, minY, minZ, maxZ);
        updateTopGrid(minX, maxX, maxY, minZ, maxZ);
        updateXFaceGrid(2, minX, minY, maxY, minZ, maxZ);
        updateXFaceGrid(3, maxX, minY, maxY, minZ, maxZ);
        updateZFaceGrid(4, minZ, minX, maxX, minY, maxY);
        updateZFaceGrid(5, maxZ, minX, maxX, minY, maxY);
    }

    private void updateHorizontalEdge(int edgeIndex, double start, double end, double fixed, double y, boolean alongX) {

        Location playerLocation = player.getEyeLocation();

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
            if (alongX)
                display = createDisplay(new Location(playerLocation.getWorld(), visibleStart, y - thickness / 2.0, fixed - thickness / 2.0), edgeMaterial.getMaterial());
            else
                display = createDisplay(new Location(playerLocation.getWorld(), fixed - thickness / 2.0, y - thickness / 2.0, visibleStart), edgeMaterial.getMaterial());

            displays.add(display);
        }

        display = displays.get(0);
        display.show(player);

        if (alongX) {
            display.setVisual(visibleStart, y - thickness / 2.0, fixed - thickness / 2.0, (float) length, (float) thickness, (float) thickness);
        } else {
            display.setVisual(fixed - thickness / 2.0, y - thickness / 2.0, visibleStart, (float) thickness, (float) thickness, (float) length);
        }
    }

    private void updateVerticalEdge(int edgeIndex, double x, double z, double minY, double maxY) {

        Location playerLocation = player.getEyeLocation();

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
            display = createDisplay(new Location(playerLocation.getWorld(), x - thickness / 2.0, visibleStart, z - thickness / 2.0), edgeMaterial.getMaterial());
            displays.add(display);
        }

        display = displays.get(0);
        display.show(player);

        display.setVisual(x - thickness / 2.0, visibleStart, z - thickness / 2.0, (float) thickness, (float) length, (float) thickness);
    }

    private void updateBottomGrid(double minX, double maxX, double y, double minZ, double maxZ) {

        List<CMIBlockDisplay> displays = faceDisplays[0];

        List<GridLine> lines = new ArrayList<>();

        int gridSize = getGridSize();

        /*
         * Lines running along X.
         */
        for (double z = firstGridLine(minZ, maxZ); z <= maxZ; z += gridSize) {

            double[] visible = getVisibleInterval(minX, maxX, z, y, true);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], z, true));
        }

        /*
         * Lines running along Z.
         */
        for (double x = firstGridLine(minX, maxX); x <= maxX; x += gridSize) {

            double[] visible = getVisibleInterval(minZ, maxZ, x, y, false);

            if (visible != null)
                lines.add(new GridLine(visible[0], visible[1], x, false));
        }

        updateGridDisplays(displays, lines, y);
    }

    private void updateTopGrid(double minX, double maxX, double y, double minZ, double maxZ) {

        List<CMIBlockDisplay> displays = faceDisplays[1];

        List<GridLine> lines = new ArrayList<>();

        int gridSize = getGridSize();
        if (maxZ - minZ > gridSize) {
            for (double z = firstGridLine(minZ, maxZ); z < maxZ; z += gridSize) {

                double[] visible = getVisibleInterval(minX, maxX, z, y, true);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], z, true));
            }
        }

        if (maxX - minX > gridSize) {
            for (double x = firstGridLine(minX, maxX); x < maxX; x += gridSize) {

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

        int gridSize = getGridSize();
        /*
         * Vertical lines along Y.
         */
        if (maxZ - minZ > gridSize) {
            for (double z = firstGridLine(minZ, maxZ); z < maxZ; z += gridSize) {

                double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], z, true));
            }
        }

        /*
         * Horizontal lines along Z.
         */
        if (maxY - minY > gridSize) {
            for (double y = firstGridLine(minY, maxY); y < maxY; y += gridSize) {

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

        int gridSize = getGridSize();
        /*
         * Vertical lines along Y.
         */
        if (maxX - minX > gridSize) {
            for (double x = firstGridLine(minX, maxX); x < maxX; x += gridSize) {

                double[] visible = getVisibleVerticalInterval(x, z, minY, maxY);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], x, true));
            }
        }

        /*
         * Horizontal lines along X.
         */
        if (maxY - minY > gridSize) {
            for (double y = firstGridLine(minY, maxY); y < maxY; y += gridSize) {

                double[] visible = getVisibleInterval(minX, maxX, z, y, true);

                if (visible != null)
                    lines.add(new GridLine(visible[0], visible[1], y, false));
            }
        }

        updateZFaceDisplays(displays, lines, z);
    }

    private void updateGridDisplays(CMIBlockDisplay display, double scaleX, double scaleY, double scaleZ, double locationX, double locationY, double locationZ) {
        CMIScheduler.runAtEntity(Residence.getInstance(), display.getEntity(), () -> {
            display.setVisual(
                    locationX - getLineThickness() / 2.0,
                    locationY - getLineThickness() / 2.0,
                    locationZ - getLineThickness() / 2.0,
                    (float) scaleX,
                    (float) scaleY,
                    (float) scaleZ);
        });
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

            CMIBlockDisplay display = createDisplay(location, material);
            display.show(player);
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

            CMIBlockDisplay display = createDisplay(location, material);
            display.show(player);
            displays.add(display);
        }
    }

    private CMIBlockDisplay createDisplay(Location location, Material material) {
        return new CMIBlockDisplay(location, material);
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

            CMIBlockDisplay display = createDisplay(location, material);
            display.show(player);
            displays.add(display);
        }
    }

    private double[] getVisibleInterval(double start, double end, double fixed, double y, boolean alongX) {

        Location playerLocation = player.getEyeLocation();

        double playerAxis = alongX ? playerLocation.getX() : playerLocation.getZ();
        double playerFixed = alongX ? playerLocation.getZ() : playerLocation.getX();

        double dy = playerLocation.getY() - y;
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

        Location playerLocation = player.getEyeLocation();

        double dx = playerLocation.getX() - x;
        double dz = playerLocation.getZ() - z;

        double horizontalSquared = dx * dx + dz * dz;

        double radiusSquared = getRange() * getRange();

        if (horizontalSquared > radiusSquared)
            return null;

        double verticalDistance = Math.sqrt(radiusSquared - horizontalSquared);

        double visibleStart = Math.max(minY, playerLocation.getY() - verticalDistance);

        double visibleEnd = Math.min(maxY, playerLocation.getY() + verticalDistance);

        if (visibleStart >= visibleEnd)
            return null;

        return new double[] {
                visibleStart,
                visibleEnd
        };
    }

    private double firstGridLine(double min, double max) {
        return (Math.floor(min / getGridSize()) + 1) * getGridSize();
    }

    public void show() {

        if (edgeDisplays == null)
            recalculate();

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

        /*
         * For horizontal faces: true = X direction false = Z direction
         *
         * For vertical faces the interpretation depends on the face update method.
         */
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