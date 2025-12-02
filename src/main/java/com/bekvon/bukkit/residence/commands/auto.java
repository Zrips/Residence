package com.bekvon.bukkit.residence.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selection.SelectionManager.Selection;

import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class auto implements cmd {

    private enum failReason {
        money, area, collision, none;
    }

    @Override
    @CommandAnnotation(simple = true, priority = 150, regVar = { 0, 1, 2 }, consoleVar = { 666 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;

        String resName = null;

        int length = -1;
        if (args.length == 1)
            try {
                length = Integer.parseInt(args[0]);
            } catch (Exception | Error e) {

            }

        if (args.length > 0 && length == -1)
            resName = args[0];
        else
            resName = player.getName();

        if (args.length == 2) {
            resName = args[0];
            try {
                length = Integer.parseInt(args[1]);
            } catch (Exception ex) {
            }
        }

        Residence.getInstance().getPlayerManager().getResidencePlayer(player).forceUpdateGroup();

        Location loc = player.getLocation();

        int minY = loc.getBlockY();
        int maxY = loc.getBlockY();

        if (plugin.getConfigManager().isSelectionIgnoreY()) {
            minY = plugin.getSelectionManager().getSelection(player).getMinYAllowed();
            maxY = plugin.getSelectionManager().getSelection(player).getMaxYAllowed();
        }

        loc.setY(minY);
        plugin.getSelectionManager().placeLoc1(player, loc.clone(), false);
        loc.setY(maxY);
        plugin.getSelectionManager().placeLoc2(player, loc.clone(), false);

        CuboidArea cuboid = plugin.getSelectionManager().getSelectionCuboid(player);

        failReason result = failReason.area;

        if (plugin.getConfigManager().isARCOldMethod())
            result = resize(plugin, player, cuboid, true, length);
        else
            result = optimizedResize(plugin, player, cuboid, true, length);

        if (!result.equals(failReason.none)) {
            return true;
        }

        plugin.getSelectionManager().afterSelectionUpdate(player, true);

        ClaimedResidence collision = Residence.getInstance().getResidenceManager().collidesWithResidence(plugin.getSelectionManager().getSelectionCuboid(player));

        if (collision != null) {
            lm.Area_Collision.sendMessage(player, collision.getResidenceName());
            return null;
        }

        if (plugin.getConfigManager().getAntiGreefRangeGaps(cuboid.getWorldName()) > 0) {
            cuboid = plugin.getSelectionManager().getSelectionCuboid(player);
            int gap = plugin.getConfigManager().getAntiGreefRangeGaps(cuboid.getWorldName());
            CuboidArea temp = new CuboidArea(cuboid.getLowLocation().clone().add(-gap, -gap, -gap), cuboid.getHighLocation().clone().add(gap, gap, gap));

            collision = Residence.getInstance().getResidenceManager().collidesWithResidence(temp);

            if (collision != null) {
                Visualizer v = new Visualizer(player);
                v.setErrorAreas(collision);
                Residence.getInstance().getSelectionManager().showBounds(player, v);
                lm.Area_TooClose.sendMessage(player, gap, collision.getName());
                return null;
            }
        }

        if (plugin.getResidenceManager().getByName(resName) != null) {
            for (int i = 1; i < 50; i++) {
                String tempName = resName + plugin.getConfigManager().ARCIncrementFormat().replace("[number]", String.valueOf(i));
                if (plugin.getResidenceManager().getByName(tempName) == null) {
                    resName = tempName;
                    break;
                }
            }
        }

        if (resName == null)
            resName = sender.getName() + plugin.getConfigManager().ARCIncrementFormat().replace("[number]", String.valueOf((new Random().nextInt(99950) + 50)));

        Selection selection = plugin.getSelectionManager().getSelection(player);

        String maxSide = "";
        String minSide = "";
        double maxRatio = 0;

        if (plugin.getConfigManager().isSelectionIgnoreY()) {

            double ratioX = getRatio(selection.getBaseArea().getXSize(), selection.getBaseArea().getXSize(), selection.getBaseArea().getZSize());
            double ratioZ = getRatio(selection.getBaseArea().getZSize(), selection.getBaseArea().getXSize(), selection.getBaseArea().getXSize());

            if (ratioX > maxRatio) {
                maxSide = "Z";
                minSide = "X";
                maxRatio = ratioX;
            }

            if (ratioZ > maxRatio) {
                maxSide = "X";
                minSide = "Z";
                maxRatio = ratioZ;
            }

        } else {

            double ratioX = getRatio(selection.getBaseArea().getXSize(), selection.getBaseArea().getYSize(), selection.getBaseArea().getZSize());
            double ratioY = getRatio(selection.getBaseArea().getYSize(), selection.getBaseArea().getZSize(), selection.getBaseArea().getXSize());
            double ratioZ = getRatio(selection.getBaseArea().getZSize(), selection.getBaseArea().getYSize(), selection.getBaseArea().getXSize());

            if (ratioX > maxRatio) {
                maxSide = "Z";
                minSide = "X";
                maxRatio = ratioX;
            }

            if (ratioZ > maxRatio) {
                maxSide = "X";
                minSide = "Z";
                maxRatio = ratioZ;
            }

            if (ratioY > maxRatio) {
                maxSide = "X";
                minSide = "Y";
                maxRatio = ratioY;
            }
        }

        if (maxRatio > plugin.getConfigManager().getARCRatioValue()) {
            if (plugin.getConfigManager().isARCRatioInform()) {
                lm.Area_WeirdShape.sendMessage(player, maxSide, (int) (maxRatio * 100) / 100D, minSide);
            }

            if (plugin.getConfigManager().isARCRatioConfirmation()) {
                RawMessage rm = new RawMessage();
                rm.addText(lm.info_clickToConfirm.getMessage());
                rm.addHover(lm.info_clickToConfirm.getMessage());
                rm.addCommand((resadmin ? "resadmin" : "res") + " create " + resName);
                rm.show(sender);
                return true;
            }
        }

        player.performCommand((resadmin ? "resadmin" : "res") + " create " + resName);

        return true;
    }

    private static double getRatio(int v1, int v2, int v3) {
        double ratio = v2 / (double) v1;
        if (v3 / v1 > ratio)
            ratio = v3 / (double) v1;
        return ratio;
    }

    private static int getMax(int max) {
        if (!Residence.getInstance().getConfigManager().isARCSizeEnabled())
            return max;
        int arcmin = Residence.getInstance().getConfigManager().getARCSizeMin();
        int arcmax = Residence.getInstance().getConfigManager().getARCSizeMax();
        int maxV = (int) (max * (Residence.getInstance().getConfigManager().getARCSizePercentage() / 100D));
        maxV = maxV < arcmin && arcmin < max ? arcmin : maxV;
        maxV = maxV > arcmax ? arcmax : maxV;
        return maxV;
    }

    public static failReason resize(Residence plugin, Player player, CuboidArea cuboid, boolean checkBalance, int max) {

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        double cost = cuboid.getCost(group);

        direction dir = direction.Top;

        List<direction> locked = new ArrayList<direction>();

        boolean checkCollision = plugin.getConfigManager().isARCCheckCollision();
        int skipped = 0;
        int done = 0;

        int groupMaxX = rPlayer.getMaxX();
        int groupMaxZ = rPlayer.getMaxZ();

        int maxX = getMax(groupMaxX);
        int maxY = getMax(group.getMaxYSize());
        int maxZ = getMax(groupMaxZ);

        if (maxX > max && max > 0)
            maxX = max;
        if (maxY > max && max > 0)
            maxY = max;
        if (maxZ > max && max > 0)
            maxZ = max;

        if (maxX <= 1)
            maxX = (rPlayer.getMaxX() - group.getMinX()) / 2 + group.getMinX();

        if (maxY <= 1)
            maxY = (group.getMaxYSize() - group.getMinYSize()) / 2 + group.getMinYSize();

        if (maxZ <= 1)
            maxZ = (rPlayer.getMaxZ() - group.getMinZ()) / 2 + group.getMinZ();

        int minY = CMIWorld.getMinHeight(cuboid.getWorld());

        while (true) {
            if (Residence.getInstance().getConfigManager().isSelectionIgnoreY() && (dir.equals(direction.Top) || dir.equals(direction.Bottom))) {
                dir = dir.getNext();
                continue;
            }
            done++;

            if (skipped >= 6) {
                break;
            }

            // fail safe if loop keeps going on
            if (done > 100000) {
                break;
            }

            if (locked.contains(dir)) {
                dir = dir.getNext();
                skipped++;
                continue;
            }

            CuboidArea c = new CuboidArea();
            c.setLowLocation(cuboid.getLowLocation().clone().add(-dir.getLow().getX(), -dir.getLow().getY(), -dir.getLow().getZ()));
            c.setHighLocation(cuboid.getHighLocation().clone().add(dir.getHigh().getX(), dir.getHigh().getY(), dir.getHigh().getZ()));

            if ((dir.equals(direction.Top) || dir.equals(direction.Bottom)) && c.getLowVector().getY() < minY) {
                c.getLowVector().setY(minY);
                locked.add(dir);
                dir = dir.getNext();
                if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
                    skipped++;
                }
                continue;
            }

            if ((dir.equals(direction.Top) || dir.equals(direction.Bottom)) && c.getHighVector().getY() >= c.getWorld().getMaxHeight()) {
                c.getHighVector().setY(c.getWorld().getMaxHeight() - 1);
                locked.add(dir);
                dir = dir.getNext();
                if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
                    skipped++;
                }
                continue;
            }

            if (checkCollision && plugin.getResidenceManager().collidesWithResidence(c) != null) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (maxX > 0 && maxX < c.getXSize() || c.getXSize() > groupMaxX) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY() && (maxY > 0 && maxY < c.getYSize() || c.getYSize() > group.getMaxYSize() + (-group.getMinYSize()))) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (maxZ > 0 && maxZ < c.getZSize() || c.getZSize() > groupMaxZ) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            skipped = 0;

            if (checkBalance && plugin.getConfigManager().enableEconomy()) {
                cost = c.getCost(group);

                if (!Residence.getInstance().getEconomyManager().canAfford(player, cost)) {
                    lm.Economy_NotEnoughMoney.sendMessage(player);
                    return failReason.money;
                }
            }

            cuboid.setLowLocation(c.getLowLocation());
            cuboid.setHighLocation(c.getHighLocation());

            dir = dir.getNext();
        }

        plugin.getSelectionManager().placeLoc1(player, cuboid.getLowLocation());
        plugin.getSelectionManager().placeLoc2(player, cuboid.getHighLocation());

        cuboid = plugin.getSelectionManager().getSelectionCuboid(player);

        if (cuboid.getXSize() > groupMaxX || cuboid.getYSize() > group.getMaxYSize() + (-group.getMinYSize()) || cuboid.getZSize() > groupMaxZ) {
            return failReason.area;
        }

        return failReason.none;
    }

    private static void fillMaps(HashMap<direction, Integer> directionMap, HashMap<direction, Integer> maxMap, direction dir, int max, int cubeSize) {
        int maxV = (int) ((max / 2D) - (cubeSize / 2D));
        directionMap.put(dir, maxV);
        maxMap.put(dir, maxV);
    }

    public static failReason optimizedResize(Residence plugin, Player player, CuboidArea cuboid, boolean checkBalance, int max) {

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        direction dir = direction.Top;

        List<direction> locked = new ArrayList<direction>();
        List<direction> permaLocked = new ArrayList<direction>();

        boolean checkCollision = plugin.getConfigManager().isARCCheckCollision();

        if (checkCollision && plugin.getResidenceManager().collidesWithResidence(cuboid) != null) {
            return failReason.collision;
        }

        int skipped = 0;
        int done = 0;

        int maxWorldY = group.getMaxYSize();

        int groupMaxX = rPlayer.getMaxX();
        int groupMaxZ = rPlayer.getMaxZ();
        int groupMaxY = group.getMaxYSize();

        int maxX = getMax(groupMaxX);
        int maxY = getMax(groupMaxY);

        int maxZ = getMax(groupMaxZ);

        if (maxX > max && max > 0)
            maxX = max;

        if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY() && maxY > max && max > 0) {
            maxY = max;
        } else if (Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
            maxY = Math.min(CMINumber.abs(CMIWorld.getMinHeight(cuboid.getWorld()) - CMIWorld.getMaxHeight(cuboid.getWorld())), maxY);
        }

        if (maxZ > max && max > 0)
            maxZ = max;

        if (maxX <= 1)
            maxX = (groupMaxX - group.getMinX()) / 2 + group.getMinX();

        if (maxY <= 1)
            maxY = (group.getMaxYSize() - group.getMinYSize()) / 2 + group.getMinYSize();

        if (maxZ <= 1)
            maxZ = (groupMaxZ - group.getMinZ()) / 2 + group.getMinZ();

        int gap = plugin.getConfigManager().getAntiGreefRangeGaps(cuboid.getWorldName());

        if (cuboid.getYSize() > maxY) {
            int dif = (cuboid.getYSize() - maxY) / 2;
            cuboid.getLowVector().add(new Vector(0, dif, 0));
            cuboid.getHighVector().add(new Vector(0, -dif, 0));
        }

        HashMap<direction, Integer> directionMap = new HashMap<direction, Integer>();
        HashMap<direction, Integer> maxMap = new HashMap<direction, Integer>();
        CuboidArea originalCuboid = new CuboidArea(cuboid.getLowLocation(), cuboid.getHighLocation());

        int smallestRange = maxX - cuboid.getXSize() < maxY - cuboid.getYSize() ? maxX - cuboid.getXSize() : maxY - cuboid.getYSize();
        smallestRange = smallestRange < maxZ - cuboid.getZSize() ? smallestRange : maxZ - cuboid.getZSize();
        smallestRange = smallestRange / 4;

        int worldMinY = CMIWorld.getMinHeight(cuboid.getWorld());

        int minYaltitude = worldMinY > group.getLowestYAllowed() ? worldMinY : group.getLowestYAllowed();
        int maxYaltitude = group.getHighestYAllowed();

        while (true) {

            done++;
            // fail safe if loop keeps going on
            if (done > 100) {
                break;
            }

            if (Math.abs(smallestRange) < 1) {
                break;
            }

            CuboidArea c = new CuboidArea();
            c.setLowLocation(cuboid.getLowLocation().clone().add(-smallestRange, -smallestRange, -smallestRange));
            c.setHighLocation(cuboid.getHighLocation().clone().add(smallestRange, smallestRange, smallestRange));

            if (c.getHighVector().getBlockY() > maxYaltitude) {
                c.setHighVector(c.getHighVector().setY(maxYaltitude));
            }

            if (c.getLowVector().getBlockY() > maxWorldY - 1) {
                c.setLowVector(c.getLowVector().setY(maxWorldY - 1));
            } else if (c.getLowVector().getBlockY() < minYaltitude) {
                c.setLowVector(c.getLowVector().setY(minYaltitude));
            }

            if (checkCollision) {

                if (gap > 0) {
                    CuboidArea temp = new CuboidArea(c.getLowLocation().clone().add(-gap, -gap, -gap), c.getHighLocation().clone().add(gap, gap, gap));

                    if (plugin.getResidenceManager().collidesWithResidence(temp) != null) {
                        smallestRange = (int) -(Math.ceil(Math.abs(smallestRange) / 2D));
                        cuboid.setLowLocation(c.getLowLocation());
                        cuboid.setHighLocation(c.getHighLocation());
                        continue;
                    }

                } else {
                    if (plugin.getResidenceManager().collidesWithResidence(c) != null) {
                        smallestRange = (int) -(Math.ceil(Math.abs(smallestRange) / 2D));
                        cuboid.setLowLocation(c.getLowLocation());
                        cuboid.setHighLocation(c.getHighLocation());
                        continue;
                    }
                }

                if (smallestRange == -1) {
                    cuboid.setLowLocation(cuboid.getLowLocation().clone().add(1, 1, 1));
                    cuboid.setHighLocation(cuboid.getHighLocation().clone().add(-1, -1, -1));
                    break;
                }
            }

            int sr = (int) Math.ceil(Math.abs(smallestRange) / 2D);

            if (maxX > 0 && maxX < c.getXSize() || c.getXSize() > groupMaxX) {
                break;
            }

            if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY() && (maxY > 0 && maxY < c.getYSize() || c.getYSize() > group.getMaxYSize() + (-group.getMinYSize()))) {
                break;
            }

            if (maxZ > 0 && maxZ < c.getZSize() || c.getZSize() > groupMaxZ) {
                break;
            }

            cuboid.setLowLocation(c.getLowLocation());
            cuboid.setHighLocation(c.getHighLocation());

            smallestRange = sr;
        }

        if (cuboid.getXSize() < 1) {
            int center = (int) (originalCuboid.getLowVector().getX() + ((originalCuboid.getHighVector().getX() - originalCuboid.getLowVector().getX()) / 2));
            cuboid.getLowVector().setX(center);
            cuboid.getHighVector().setX(center);
        }

        if (cuboid.getZSize() < 1) {
            int center = (int) (originalCuboid.getLowVector().getZ() + ((originalCuboid.getHighVector().getZ() - originalCuboid.getLowVector().getZ()) / 2));
            cuboid.getLowVector().setZ(center);
            cuboid.getHighVector().setZ(center);
        }

        if (cuboid.getYSize() < 1) {
            int center = (int) (originalCuboid.getLowVector().getY() + ((originalCuboid.getHighVector().getY() - originalCuboid.getLowVector().getY()) / 2));
            cuboid.getLowVector().setY(center);
            cuboid.getHighVector().setY(center);
        }

        fillMaps(directionMap, maxMap, direction.East, maxX, cuboid.getXSize());
        fillMaps(directionMap, maxMap, direction.West, maxX + 1, cuboid.getXSize());

        fillMaps(directionMap, maxMap, direction.South, maxZ, cuboid.getZSize());
        fillMaps(directionMap, maxMap, direction.North, maxZ + 1, cuboid.getZSize());

        fillMaps(directionMap, maxMap, direction.Top, maxY, cuboid.getYSize());
        fillMaps(directionMap, maxMap, direction.Bottom, maxY + 1, cuboid.getYSize());

        while (true) {
            if (Residence.getInstance().getConfigManager().isSelectionIgnoreY() && (dir.equals(direction.Top) || dir.equals(direction.Bottom))) {
                dir = dir.getNext();
                continue;
            }
            done++;

            if (skipped >= 6) {
                break;
            }

            // fail safe if loop keeps going on
            if (done > 100) {
                break;
            }

            if (locked.contains(dir)) {
                dir = dir.getNext();
                skipped++;
                continue;
            }

            skipped = 0;

            Integer offset = directionMap.get(dir);

            if (Math.abs(offset) == 0) {

                if (dir == direction.East && locked.contains(direction.West) && !permaLocked.contains(direction.West)) {
                    maxMap.put(direction.West, maxX - (player.getLocation().getBlockX() - cuboid.getHighVector().getBlockX()));
                    directionMap.put(direction.West, maxMap.get(direction.West) / 2);
                    locked.remove(direction.West);
                    permaLocked.add(dir);
                }

                if (dir == direction.West && locked.contains(direction.East) && !permaLocked.contains(direction.East)) {
                    maxMap.put(direction.East, maxX - (player.getLocation().getBlockX() - cuboid.getHighVector().getBlockX()));
                    directionMap.put(direction.East, maxMap.get(direction.East) / 2);
                    locked.remove(direction.East);
                    permaLocked.add(dir);
                }

                if (dir == direction.North && locked.contains(direction.South) && !permaLocked.contains(direction.South)) {
                    maxMap.put(direction.South, maxZ - (player.getLocation().getBlockZ() - cuboid.getHighVector().getBlockZ()));
                    directionMap.put(direction.South, maxMap.get(direction.South) / 2);
                    locked.remove(direction.South);
                    permaLocked.add(dir);
                }

                if (dir == direction.South && locked.contains(direction.North) && !permaLocked.contains(direction.North)) {
                    maxMap.put(direction.North, maxZ - (player.getLocation().getBlockZ() - cuboid.getHighVector().getBlockZ()));
                    directionMap.put(direction.North, maxMap.get(direction.North) / 2);
                    locked.remove(direction.North);
                    permaLocked.add(dir);
                }

                if (dir == direction.Top && locked.contains(direction.Bottom) && !permaLocked.contains(direction.Bottom)) {
                    maxMap.put(direction.Bottom, maxY - Math.abs(player.getLocation().getBlockY() - cuboid.getLowVector().getBlockY()));
                    directionMap.put(direction.Bottom, maxMap.get(direction.Bottom) / 2);
                    locked.remove(direction.Bottom);
                    permaLocked.add(dir);
                }

                if (dir == direction.Bottom && locked.contains(direction.Top) && !permaLocked.contains(direction.Top)) {
                    maxMap.put(direction.Top, maxY - Math.abs(player.getLocation().getBlockY() - cuboid.getHighVector().getBlockY()));
                    directionMap.put(direction.Top, maxMap.get(direction.Top) / 2);
                    locked.remove(direction.Top);
                    permaLocked.add(dir);
                }

                locked.add(dir);
                dir = dir.getNext();
                continue;
            }

            CuboidArea c = new CuboidArea();
            c.setLowLocation(cuboid.getLowLocation().clone().add(-dir.getLow().getX() * offset, -dir.getLow().getY() * offset, -dir.getLow().getZ() * offset));
            c.setHighLocation(cuboid.getHighLocation().clone().add(dir.getHigh().getX() * offset, dir.getHigh().getY() * offset, dir.getHigh().getZ() * offset));

            if (c.getHighVector().getBlockY() > maxYaltitude) {
                c.setHighVector(c.getHighVector().setY(maxYaltitude));
                if (locked.contains(direction.Top) && !locked.contains(direction.Bottom) && !permaLocked.contains(direction.Top)) {
                    maxMap.put(direction.Top, maxY - Math.abs(c.getHighVector().getBlockY() - player.getLocation().getBlockY()));
                    directionMap.put(direction.Top, maxMap.get(direction.Top) / 2);
                    locked.remove(direction.Top);
                    permaLocked.add(direction.Bottom);
                }
            }

            if (c.getLowVector().getBlockY() > maxYaltitude - 1) {
                c.setLowVector(c.getLowVector().setY(maxYaltitude - 1));
            } else if (c.getLowVector().getBlockY() < minYaltitude) {
                c.setLowVector(c.getLowVector().setY(minYaltitude));
                if (!locked.contains(direction.Top) && !locked.contains(direction.Bottom) && !permaLocked.contains(direction.Bottom)) {
                    maxMap.put(direction.Bottom, maxY - Math.abs(player.getLocation().getBlockY() - c.getLowVector().getBlockY()));
                    directionMap.put(direction.Bottom, maxMap.get(direction.Bottom) / 2);
                    permaLocked.add(direction.Top);
                }
            }

            if (checkCollision) {
                boolean collides = false;
                if (gap > 0) {
                    CuboidArea temp = new CuboidArea(c.getLowLocation().clone().add(-gap, -gap, -gap), c.getHighLocation().clone().add(gap, gap, gap));
                    collides = plugin.getResidenceManager().collidesWithResidence(temp) != null;
                } else {
                    collides = plugin.getResidenceManager().collidesWithResidence(c) != null;
                }
                if (collides) {
                    int newOffset = (int) (Math.abs(offset) / 2D);
                    if (newOffset < 1)
                        newOffset = 1;
                    directionMap.put(dir, -(newOffset));
                    cuboid.setLowLocation(c.getLowLocation());
                    cuboid.setHighLocation(c.getHighLocation());
                    continue;
                }
            }

            if (maxMap.get(dir).equals(Math.abs(offset))) {
                locked.add(dir);
            }

            double newOffset = (Math.abs(offset) / 2D);

            offset = newOffset > 1 ? (int) Math.ceil(newOffset) : (int) newOffset;

            directionMap.put(dir, offset);

            if (maxX > 0 && maxX < c.getXSize() || c.getXSize() > groupMaxX) {
                if (Math.abs(offset) < 1)
                    locked.add(dir);
                dir = dir.getNext();
                continue;
            }

            if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY() &&
                    (maxY > 0 && maxY < c.getYSize() || c.getYSize() > maxY)) {
                if (Math.abs(offset) < 1) {
                    locked.add(dir);
                }
                dir = dir.getNext();
                continue;
            }

            if (maxZ > 0 && maxZ < c.getZSize() || c.getZSize() > groupMaxZ) {
                if (Math.abs(offset) < 1)
                    locked.add(dir);
                dir = dir.getNext();
                continue;
            }

            if (checkBalance && plugin.getConfigManager().enableEconomy() && !Residence.getInstance().getEconomyManager().canAfford(player, c.getCost(group))) {
                lm.Economy_NotEnoughMoneyAmount.sendMessage(player, Residence.getInstance().getEconomyManager().format(c.getCost(group)));
                return failReason.money;
            }

            cuboid.setLowLocation(c.getLowLocation());
            cuboid.setHighLocation(c.getHighLocation());

            dir = dir.getNext();
        }

        plugin.getSelectionManager().placeLoc1(player, cuboid.getLowLocation());
        plugin.getSelectionManager().placeLoc2(player, cuboid.getHighLocation());

        cuboid = plugin.getSelectionManager().getSelectionCuboid(player);

        ClaimedResidence collison = null;
        if (gap > 0) {
            CuboidArea temp = new CuboidArea(cuboid.getLowLocation().clone().add(-gap, -gap, -gap), cuboid.getHighLocation().clone().add(gap, gap, gap));
            collison = plugin.getResidenceManager().collidesWithResidence(temp);
        } else {
            collison = plugin.getResidenceManager().collidesWithResidence(cuboid);
        }

        if (collison != null) {
            lm.Area_Collision.sendMessage(player, collison.getName());
            return failReason.collision;
        }

        if (cuboid.getXSize() > groupMaxX)
            lm.Area_ToBigX.sendMessage(player, cuboid.getXSize(), groupMaxX);

        if (cuboid.getYSize() > groupMaxY)
            lm.Area_ToBigY.sendMessage(player, cuboid.getYSize(), groupMaxY);

        if (cuboid.getZSize() > groupMaxZ)
            lm.Area_ToBigZ.sendMessage(player, cuboid.getZSize(), groupMaxZ);

        if (cuboid.getXSize() > groupMaxX || cuboid.getYSize() > groupMaxY || cuboid.getZSize() > groupMaxZ) {
            lm.Area_SizeLimit.sendMessage(player);
            return failReason.area;
        }

        return failReason.none;
    }

    public enum direction {
        Top(new Vector(0, 1, 0), new Vector(0, 0, 0)),
        Bottom(new Vector(0, 0, 0), new Vector(0, 1, 0)),
        East(new Vector(1, 0, 0), new Vector(0, 0, 0)),
        West(new Vector(0, 0, 0), new Vector(1, 0, 0)),
        North(new Vector(0, 0, 1), new Vector(0, 0, 0)),
        South(new Vector(0, 0, 0), new Vector(0, 0, 1));

        private Vector low;
        private Vector high;

        direction(Vector low, Vector high) {
            this.low = low;
            this.high = high;
        }

        public Vector getLow() {
            return low;
        }

        public Vector getHigh() {
            return high;
        }

        public direction getNext() {
            boolean next = false;
            direction dir = direction.Top;
            for (direction one : direction.values()) {
                if (next) {
                    dir = one;
                    next = false;
                    break;
                }
                if (this.equals(one)) {
                    next = true;
                }
            }
            return dir;
        }

    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Create maximum allowed residence around you");
        c.get("Info", Arrays.asList("&eUsage: &6/res auto (residence name) (radius)"));
        LocaleManager.addTabCompleteMain(this);
    }
}
