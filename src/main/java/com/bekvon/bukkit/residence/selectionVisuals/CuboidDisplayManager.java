package com.bekvon.bukkit.residence.selectionVisuals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selection.VisualizerConfig;

import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;
import net.Zrips.CMILib.Version.Version;

public class CuboidDisplayManager implements Listener {

    private static final double REMOVE_DISTANCE = 64.0;
    private static final double REMOVE_DISTANCE_SQUARED = REMOVE_DISTANCE * REMOVE_DISTANCE;
    private static double RECALCULATE_DISTANCE = VisualizerConfig.getUpdateRateByTravel();
    private static double RECALCULATE_DISTANCE_SQUARED = RECALCULATE_DISTANCE * RECALCULATE_DISTANCE;

    private static final Map<UUID, PlayerData> players = new HashMap<>();

    public static void register() {

        if (Version.isCurrentEqualOrLower(Version.v1_19_4))
            return;

        Bukkit.getPluginManager().registerEvents(new CuboidDisplayManager(), Residence.getInstance());
    }

    public static void updateVariables() {
        RECALCULATE_DISTANCE = VisualizerConfig.getUpdateRateByTravel();
        RECALCULATE_DISTANCE_SQUARED = RECALCULATE_DISTANCE * RECALCULATE_DISTANCE;
    }

    public static void add(Player player, List<CuboidArea> areas, CuboidDisplayType type) {
        add(player, areas, type, type.getHideInSeconds(), type.getRange(), type.getGridSize());
    }

    public static void add(Player player, List<CuboidArea> areas, CuboidDisplayType type, double hideInSeconds, int range, double gridSize) {

        List<CuboidDisplay> ls = getAreas(player, areas, type);

        PlayerData data = getPlayerData(player);

        for (CuboidDisplay display : ls) {

            for (CuboidDisplay cachedDisplay : data.displays) {
                if (cachedDisplay.getArea() == display.getArea())
                    return;
            }

            display.setRange(range);
            display.setGridSize(gridSize);
            display.recalculate();

            display.show();
            data.displays.add(display);
        }
    }

    public static void add(Player player, CMIBlockWorldArea area, CuboidDisplayType type, double hideInSeconds) {
        PlayerData data = getPlayerData(player);

        for (CuboidDisplay display : data.displays) {
            if (display.getArea() == area)
                return;
        }

        CuboidDisplay display = new CuboidDisplay(player, area);
        display.setEdgeMaterial(type.getEdgeMaterial());
        display.setSideMaterial(type.getSideMaterial());
        display.removeAfter(hideInSeconds);
        display.recalculate();
        display.show();

        data.displays.add(display);
    }

    public static void remove(Player player, CMIBlockWorldArea area) {
        PlayerData data = players.get(player.getUniqueId());

        if (data == null)
            return;

        Iterator<CuboidDisplay> iterator = data.displays.iterator();

        while (iterator.hasNext()) {
            CuboidDisplay display = iterator.next();

            if (display.getArea() != area)
                continue;

            display.clear();
            iterator.remove();
            break;
        }

        removeEmptyPlayer(player);
    }

    public static void removeAll(Player player) {
        if (player == null)
            return;
        removeAll(player.getUniqueId());
    }

    public static void removeAll(UUID uuid) {
        PlayerData data = players.remove(uuid);

        if (data == null)
            return;

        for (CuboidDisplay display : data.displays)
            display.clear();

        data.displays.clear();
    }

    public static void removeAllPlayerDisplays() {
        for (PlayerData data : players.values()) {
            for (CuboidDisplay display : data.displays) {
                display.clear();
            }
            data.displays.clear();
        }
        players.clear();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!VisualizerConfig.isUseModernVersion()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null)
            return;

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        if (players.isEmpty())
            return;

        Player player = event.getPlayer();
        PlayerData data = players.get(player.getUniqueId());

        if (data == null)
            return;

        if (from.getWorld() != to.getWorld())
            return;

        if (data.lastUpdateLocation != null && distanceSquared(data.lastUpdateLocation, to) < RECALCULATE_DISTANCE_SQUARED) {
            return;
        }

        data.lastUpdateLocation = to.clone();

        updatePlayer(player);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!VisualizerConfig.isUseModernVersion()) {
            return;
        }
        Player player = event.getPlayer();
        PlayerData data = players.get(player.getUniqueId());

        if (data == null)
            return;

        Location destination = event.getTo();

        if (destination == null)
            return;

        data.lastUpdateLocation = destination.clone();

        if (event.getFrom().getWorld() != destination.getWorld()) {
            clearDisplays(data);
            return;
        }

        updatePlayer(player);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (!VisualizerConfig.isUseModernVersion()) {
            return;
        }
        Player player = event.getPlayer();
        PlayerData data = players.get(player.getUniqueId());

        if (data == null)
            return;

        data.lastUpdateLocation = player.getLocation().clone();

        clearDisplays(data);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!VisualizerConfig.isUseModernVersion()) {
            return;
        }
        removeAll(event.getPlayer());
    }

    public static void show(Player player, List<CuboidArea> areas, List<CuboidArea> errorAreas) {
        show(player, getAreas(player, areas, errorAreas));
    }

    private static List<CuboidDisplay> getAreas(Player player, List<CuboidArea> areas, List<CuboidArea> errorAreas) {
        return getAreas(player, areas, errorAreas, null);
    }

    private static List<CuboidDisplay> getAreas(Player player, List<CuboidArea> areas, List<CuboidArea> errorAreas, CuboidDisplayType type) {

        List<CuboidDisplay> cd = new ArrayList<>();

        cd.addAll(getAreas(player, areas, type == null ? CuboidDisplayType.SELECTION : type));
        cd.addAll(getAreas(player, errorAreas, type == null ? CuboidDisplayType.ERROR : type));

        return cd;
    }

    private static List<CuboidDisplay> getAreas(Player player, List<CuboidArea> areas, CuboidDisplayType type) {

        List<CuboidDisplay> cd = new ArrayList<>();

        if (areas == null)
            return cd;

        for (CuboidArea area : areas) {
            CuboidDisplay display = new CuboidDisplay(player, new CMIBlockWorldArea(area.getLowLocation(), area.getHighLocation()));
            display.setEdgeMaterial(type == null ? CuboidDisplayType.SELECTION.getEdgeMaterial() : type.getEdgeMaterial());
            display.setSideMaterial(type == null ? CuboidDisplayType.SELECTION.getSideMaterial() : type.getSideMaterial());
            display.removeAfter(type == null ? 5 : type.getHideInSeconds());
            display.setLineThickness(type == null ? 0.05 : type.getLineThickness());
            display.setGridSize(type == null ? 8 : type.getGridSize());
            display.setRange(type == null ? 32 : type.getRange());
            cd.add(display);
        }

        return cd;
    }

    public static void show(Player player, List<CuboidDisplay> cd) {
        PlayerData data = getPlayerData(player);

        for (CuboidDisplay display : data.displays)
            display.clear();

        data.displays.clear();
        data.lastUpdateLocation = player.getLocation().clone();

        for (CuboidDisplay area : cd) {
            area.show();
            data.displays.add(area);
        }

        updatePlayer(player);
    }

    private static void updatePlayer(Player player) {

        PlayerData data = players.get(player.getUniqueId());

        if (data == null)
            return;

        Location location = player.getLocation();

        Iterator<CuboidDisplay> iterator = data.displays.iterator();

        while (iterator.hasNext()) {

            CuboidDisplay display = iterator.next();

            if (display.getWorld() != location.getWorld()) {
                display.clear();
                iterator.remove();
                continue;
            }

            if (distanceSquaredToCuboid(location, display.getArea()) > REMOVE_DISTANCE_SQUARED) {

                display.clear();
                iterator.remove();
                continue;
            }

            display.recalculate();
        }

        removeEmptyPlayer(player);
    }

    private static double distanceSquaredToCuboid(Location location, CMIBlockWorldArea area) {

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = area.getLowPoint().getX();
        double minY = area.getLowPoint().getY();
        double minZ = area.getLowPoint().getZ();

        double maxX = area.getHighPoint().getX();
        double maxY = area.getHighPoint().getY();
        double maxZ = area.getHighPoint().getZ();

        double dx = 0;

        if (x < minX)
            dx = minX - x;
        else if (x > maxX)
            dx = x - maxX;

        double dy = 0;

        if (y < minY)
            dy = minY - y;
        else if (y > maxY)
            dy = y - maxY;

        double dz = 0;

        if (z < minZ)
            dz = minZ - z;
        else if (z > maxZ)
            dz = z - maxZ;

        return dx * dx + dy * dy + dz * dz;
    }

    private void clearDisplays(PlayerData data) {
        for (CuboidDisplay display : data.displays)
            display.clear();
    }

    private static PlayerData getPlayerData(Player player) {
        UUID uuid = player.getUniqueId();

        PlayerData data = players.get(uuid);

        if (data == null) {
            data = new PlayerData(player.getLocation());

            players.put(uuid, data);
        }

        return data;
    }

    private static void removeEmptyPlayer(Player player) {
        PlayerData data = players.get(player.getUniqueId());

        if (data != null && data.displays.isEmpty())
            players.remove(player.getUniqueId());
    }

    private double distanceSquared(Location a, Location b) {
        if (a.getWorld() != b.getWorld())
            return Double.MAX_VALUE;

        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();

        return dx * dx + dy * dy + dz * dz;
    }

    private static class PlayerData {

        private Location lastUpdateLocation;

        private final List<CuboidDisplay> displays = new ArrayList<>();

        public PlayerData(Location location) {
            this.lastUpdateLocation = location.clone();
        }
    }
}
