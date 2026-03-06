package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class Utils {

    public Utils() {
    }

    public static boolean verifyResidenceName(@Nullable CommandSender sender, @NotNull String name) {

        if (name == null)
            return false;

        if (!verifyResidenceNameCharacters(name)) {
            if (sender != null)
                lm.Invalid_NameCharacters.sendMessage(sender);
            return false;
        }
        if (!verifyResidenceNameLength(name)) {
            if (sender != null)
                lm.Invalid_NameLegth.sendMessage(sender, Residence.getInstance().getConfigManager().getResidenceNameLength());
            return false;
        }
        return true;
    }

    public static List<Entity> getPassengers(Vehicle vehicle) {
        if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
            return vehicle.getPassengers();
        else {
            List<Entity> passengers = new ArrayList<>();
            if (vehicle.getPassenger() != null)
                passengers.add(vehicle.getPassenger());
            return passengers;
        }
    }

    public static boolean verifyResidenceNameCharacters(@NotNull String name) {

        if (name == null)
            return false;

        if (name.contains(":") || name.contains(".") || name.contains("|"))
            return false;

        if (Residence.getInstance().getConfigManager().getResidenceNameRegex() == null)
            return true;

        return name.equals(name.replaceAll(Residence.getInstance().getConfigManager().getResidenceNameRegex(), ""));
    }

    public static boolean verifyResidenceNameLength(@NotNull String name) {
        if (name == null)
            return false;
        return name.length() <= Residence.getInstance().getConfigManager().getResidenceNameLength();
    }

    public static Player potentialProjectileToPlayer(Entity entity) {
        entity = potentialProjectileToEntity(entity);
        if (entity instanceof Player)
            return (Player) entity;
        return null;
    }

    public static boolean isSourceBlockInsideSameResidence(@NotNull Entity potentialProjecticle, @Nullable ClaimedResidence targetResidence) {
        // Check potential block as a shooter which should be allowed if its inside same
        // residence
        if (!(potentialProjecticle instanceof Projectile))
            return false;

        @NotNull
        Projectile projectile = (Projectile) potentialProjecticle;
        if (!(projectile.getShooter() instanceof BlockProjectileSource))
            return false;

        BlockProjectileSource bps = (BlockProjectileSource) projectile.getShooter();
        ClaimedResidence bres = ClaimedResidence.getByLoc(bps.getBlock().getLocation());
        ClaimedResidence res = targetResidence == null ? ClaimedResidence.getByLoc(potentialProjecticle.getLocation()) : targetResidence;
        return Objects.equals(res, bres);
    }

    public static Entity potentialProjectileToEntity(Entity entity) {
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            Object shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                return (Player) shooter;
            }
            if (shooter instanceof Entity) {
                return potentialProjectileToEntity((Entity) shooter);
            }
        }

        return entity;
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
        if (ent == null) {
            return false;
        }
        CMIEntityType type = CMIEntityType.get(ent);
        return (ent instanceof Animals ||
                ent instanceof WaterMob ||
                ent instanceof NPC ||
                ent instanceof Bat ||
                ent instanceof Snowman ||
                ent instanceof IronGolem ||
                type == CMIEntityType.ALLAY ||
                type == CMIEntityType.COPPER_GOLEM);
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
