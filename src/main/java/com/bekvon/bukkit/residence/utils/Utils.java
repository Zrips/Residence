package com.bekvon.bukkit.residence.utils;

import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Iterator;

public class Utils {

    public Utils() {
    }

    public static String to24hourShort(Long ticks) {
        long years = ticks / 1000 / 60 / 60 / 24 / 365;
        ticks = ticks - (years * 1000 * 60 * 60 * 24 * 365);

        long days = ticks / 1000 / 60 / 60 / 24;
        ticks = ticks - (days * 1000 * 60 * 60 * 24);

        long hours = ticks / 1000 / 60 / 60;
        ticks = ticks - (hours * 1000 * 60 * 60);

        long minutes = ticks / 1000 / 60;
        ticks = ticks - (minutes * 1000 * 60);

        long sec = ticks / 1000;
        ticks = ticks - (sec * 1000);

        String time = "";

        if (years > 0)
            time += years == 1 ? Residence.getInstance().getLM().getMessage(lm.info_oneYear, years) : Residence.getInstance().getLM().getMessage(lm.info_years, years);

        if (days > 0)
            time += days == 1 ? Residence.getInstance().getLM().getMessage(lm.info_oneDay, days) : Residence.getInstance().getLM().getMessage(lm.info_day, days);

        if (hours > 0)
            time += hours == 1 ? Residence.getInstance().getLM().getMessage(lm.info_oneHour, hours) : Residence.getInstance().getLM().getMessage(lm.info_hour, hours);

        if (minutes > 0)
            time += Residence.getInstance().getLM().getMessage(lm.info_min, minutes);

        if (sec > 0)
            time += Residence.getInstance().getLM().getMessage(lm.info_sec, sec);

        if (time.isEmpty())
            time += Residence.getInstance().getLM().getMessage(lm.info_sec, 0);

        return time;
    }

    public static int getMaxWorldHeight(World world) {
        if (world == null)
            return 256;

        switch (world.getEnvironment()) {
            case NETHER:
                return 128;
            case NORMAL:
            case THE_END:
                return 256;
            default:
                break;
        }
        return 256;
    }

    public static Block getTargetBlock(Player player, int distance, boolean ignoreNoneSolids) {
        return getTargetBlock(player, null, distance, ignoreNoneSolids);
    }

    public static Block getTargetBlock(Player player, int distance) {
        return getTargetBlock(player, null, distance, false);
    }

    public static Block getTargetBlock(Player player, Material lookingFor, int distance) {
        return getTargetBlock(player, lookingFor, distance, false);
    }

    public static Block getTargetBlock(Player player, Material lookingFor, int distance, boolean ignoreNoneSolids) {
        if (distance > 15 * 16)
            distance = 15 * 16;
        if (distance < 1)
            distance = 1;
        ArrayList<Block> blocks = new ArrayList<Block>();
        Iterator<Block> itr = new BlockIterator(player, distance);
        while (itr.hasNext()) {
            Block block = itr.next();
            blocks.add(block);
            if (distance != 0 && blocks.size() > distance) {
                blocks.remove(0);
            }
            Material material = block.getType();

            if (ignoreNoneSolids && !block.getType().isSolid())
                continue;

            if (lookingFor == null) {
                if (!CMIMaterial.AIR.equals(material) && !CMIMaterial.CAVE_AIR.equals(material) && !CMIMaterial.VOID_AIR.equals(material)) {
                    break;
                }
            } else {
                if (lookingFor.equals(material)) {
                    return block;
                }
            }
        }
        return !blocks.isEmpty() ? blocks.get(blocks.size() - 1) : null;
    }

    public static String convertLocToStringTiny(Location loc) {
        String map = "";
        if (loc != null) {
            if (loc.getWorld() != null) {
                map += loc.getWorld().getName();
                map += ";" + loc.getBlockX();
                map += ";" + loc.getBlockY();
                map += ";" + loc.getBlockZ();
            }
        }
        return map.replace(",", ".");
    }

    public static String convertLocToStringShort(Location loc) {
        String map = "";
        if (loc != null) {
            if (loc.getWorld() != null) {
                map += loc.getWorld().getName();
                map += ";" + (int) (loc.getX() * 100) / 100D;
                map += ";" + (int) (loc.getY() * 100) / 100D;
                map += ";" + (int) (loc.getZ() * 100) / 100D;
            }
        }
        return map.replace(",", ".");
    }

    public static Location convertStringToLocation(String map) {
        Location loc = null;
        if (map == null)
            return null;
        if (!map.contains(";"))
            return null;

        String[] split = map.replace(",", ".").split(";");
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;

        if (split.length > 0)
            try {
                x = Double.parseDouble(split[1]);
            } catch (Exception e) {
                return loc;
            }

        if (split.length > 1)
            try {
                y = Double.parseDouble(split[2]);
            } catch (Exception e) {
                return loc;
            }

        if (split.length > 2)
            try {
                z = Double.parseDouble(split[3]);
            } catch (Exception e) {
                return loc;
            }

        if (split.length > 3)
            try {
                yaw = Float.parseFloat(split[4]);
            } catch (Exception e) {
            }

        if (split.length > 4)
            try {
                pitch = Float.parseFloat(split[5]);
            } catch (Exception e) {
            }

        World w = Bukkit.getWorld(split[0]);
        if (w == null)
            return null;
        loc = new Location(w, x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);

        return loc;
    }
}
