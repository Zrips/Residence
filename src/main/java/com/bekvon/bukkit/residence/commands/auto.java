package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class auto implements cmd {

    public static void resize(Residence plugin, Player player, CuboidArea cuboid, boolean checkBalance) {

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        double cost = cuboid.getCost(group);

        double balance = 0;
        if (plugin.getEconomyManager() != null)
            balance = plugin.getEconomyManager().getBalance(player.getName());

        direction dir = direction.Top;

        List<direction> locked = new ArrayList<direction>();

        boolean checkCollision = plugin.getConfigManager().isAutomaticResidenceCreationCheckCollision();
        int skipped = 0;
        int done = 0;
        while (true) {
            if (Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
                if (dir.equals(direction.Top) || dir.equals(direction.Bottom)) {
                    dir = dir.getNext();
                    continue;
                }
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
            c.setLowLocation(cuboid.getLowLoc().clone().add(-dir.getLow().getX(), -dir.getLow().getY(), -dir.getLow().getZ()));
            c.setHighLocation(cuboid.getHighLoc().clone().add(dir.getHigh().getX(), dir.getHigh().getY(), dir.getHigh().getZ()));

            if (c.getLowLoc().getY() < 0) {
                c.getLowLoc().setY(0);
                locked.add(dir);
                dir = dir.getNext();
                if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY())
                    skipped++;
                continue;
            }

            if (c.getHighLoc().getY() >= c.getWorld().getMaxHeight()) {
                c.getHighLoc().setY(c.getWorld().getMaxHeight() - 1);
                locked.add(dir);
                dir = dir.getNext();
                if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY())
                    skipped++;
                continue;
            }

            if (checkCollision && plugin.getResidenceManager().collidesWithResidence(c) != null) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (c.getXSize() > group.getMaxX() - group.getMinX()) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY())
                if (c.getYSize() > group.getMaxY() - group.getMinY()) {
                    locked.add(dir);
                    dir = dir.getNext();
                    skipped++;
                    continue;
                }

            if (c.getZSize() > group.getMaxZ() - group.getMinZ()) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            skipped = 0;

            if (checkBalance) {
                if (plugin.getConfigManager().enableEconomy()) {
                    cost = c.getCost(group);
                    if (cost > balance)
                        break;
                }
            }

            cuboid.setLowLocation(c.getLowLoc());
            cuboid.setHighLocation(c.getHighLoc());

            dir = dir.getNext();
        }

        plugin.getSelectionManager().placeLoc1(player, cuboid.getLowLoc());
        plugin.getSelectionManager().placeLoc2(player, cuboid.getHighLoc());
    }

    @Override
    @CommandAnnotation(simple = true, priority = 150, regVar = {0, 1, 2}, consoleVar = {666})
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;

        String resName = null;

        int lenght = -1;
        if (args.length == 1)
            try {
                lenght = Integer.parseInt(args[0]);
            } catch (Exception | Error e) {

            }

        if (args.length > 0 && lenght == -1)
            resName = args[0];
        else
            resName = player.getName();

        if (args.length == 2) {
            resName = args[0];
            try {
                lenght = Integer.parseInt(args[1]);
            } catch (Exception ex) {
            }
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        PermissionGroup group = rPlayer.getGroup();

        Location loc = player.getLocation();

        int X = group.getMinX();
        int Y = group.getMinY();
        int Z = group.getMinZ();

        if (lenght > 0) {
            if (lenght > group.getMaxX())
                X = group.getMaxX();
            else
                X = lenght;
            if (lenght > group.getMaxZ())
                Z = group.getMaxZ();
            else
                Z = lenght;
            if (lenght > group.getMaxY())
                Y = group.getMaxY();
            else
                Y = lenght;
        }

        int rX = (X - 1) / 2;
        int rY = (Y - 1) / 2;
        int rZ = (Z - 1) / 2;

        int minX = loc.getBlockX() - rX;
        int maxX = loc.getBlockX() + rX;

        if (maxX - minX + 1 < X)
            maxX++;

        int minY = loc.getBlockY() - rY;
        int maxY = loc.getBlockY() + rY;

        if (maxY - minY + 1 < Y)
            maxY++;

        if (minY < 0) {
            maxY += -minY;
            minY = 0;
        }

        if (maxY > loc.getWorld().getMaxHeight()) {
            int dif = maxY - loc.getWorld().getMaxHeight();
            if (minY > 0)
                minY -= dif;
            if (minY < 0)
                minY = 0;
            maxY = loc.getWorld().getMaxHeight() - 1;
        }

        int minZ = loc.getBlockZ() - rZ;
        int maxZ = loc.getBlockZ() + rZ;
        if (maxZ - minZ + 1 < Z)
            maxZ++;

        plugin.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), minX, minY, minZ), false);
        plugin.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), maxX, maxY, maxZ), false);

        resize(plugin, player, plugin.getSelectionManager().getSelectionCuboid(player), true);

        if (plugin.getResidenceManager().getByName(resName) != null) {
            for (int i = 1; i < 50; i++) {
                String tempName = resName + plugin.getConfigManager().AutomaticResidenceCreationIncrementFormat().replace("[number]", i + "");
                if (plugin.getResidenceManager().getByName(tempName) == null) {
                    resName = tempName;
                    break;
                }
            }
        }

        player.performCommand((resadmin ? "resadmin" : "res") + " create " + resName);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Create maximum allowed residence around you");
        c.get("Info", Collections.singletonList("&eUsage: &6/res auto (residence name) (radius)"));
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
}
