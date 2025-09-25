package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BlockIterator;

import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class Utils {

    public Utils() {
    }

    public static Player entityToPlayer(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        } else if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            Object shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
            if (shooter instanceof Entity) {
                return entityToPlayer((Entity) shooter);
            }
        } else if (entity instanceof Tameable) {
            Tameable tameable = (Tameable) entity;
            if (tameable.getOwner() instanceof Player) {
                return (Player) tameable.getOwner();
            }
        } else if (entity instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) entity;
            if (tnt.getSource() instanceof Player) {
                return entityToPlayer(tnt.getSource());
            }
        }

        return null;
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

    public static boolean isAnimal(Entity ent) {
        return (ent instanceof Animals ||
            ent instanceof WaterMob ||
            ent instanceof NPC ||
            ent instanceof Bat ||
            ent instanceof Snowman ||
            ent instanceof IronGolem ||
            // crude approach to include Allay and be multi version supported
            ent.getClass().getSimpleName().equals("CraftAllay"));
    }

    public static boolean isArmorStandEntity(EntityType ent) {
        if (Version.isCurrentEqualOrLower(Version.v1_7_R4))
            return false;
        return ent == org.bukkit.entity.EntityType.ARMOR_STAND;
    }

    public static boolean isSpectator(org.bukkit.GameMode mode) {
        if (Version.isCurrentEqualOrLower(Version.v1_7_R4))
            return false;
        return mode == org.bukkit.GameMode.SPECTATOR;
    }

    public static boolean isMainHand(PlayerInteractEvent event) {
        if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
            return true;
        return event.getHand() == EquipmentSlot.HAND ? true : false;
    }

    public static boolean isChorusTeleport(org.bukkit.event.player.PlayerTeleportEvent.TeleportCause tpcause) {
        if (Version.isCurrentEqualOrLower(Version.v1_8_R3))
            return false;
        return tpcause == org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT;
    }

    public static List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
        List<Block> blocks = new ArrayList<Block>();
        if (Version.isCurrentEqualOrLower(Version.v1_7_R4)) {
            blocks.add(event.getBlock());
        } else {
            blocks.addAll(event.getBlocks());
        }
        return blocks;
    }
}
