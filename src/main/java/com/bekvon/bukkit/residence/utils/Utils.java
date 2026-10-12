package com.bekvon.bukkit.residence.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.bekvon.bukkit.residence.listeners.ResidenceListener1_14;
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
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Wither;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITaskResult;

public class Utils {

    public Utils() {
    }

    public static CompletableFuture<CMITaskResult> closeInventory(@NotNull Player player) {
        if (player == null || !player.isOnline())
            return CompletableFuture.completedFuture(null);
        return CMIScheduler.runAtEntity(Residence.getInstance(), player, () -> {
            if (player.isOnline())
                player.closeInventory();
        });
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
        // getPassengers was not added until Bukkit 1.11.2+
        if (Version.isCurrentEqualOrHigher(Version.v1_11_2)) {
            return vehicle.getPassengers();
        } else {
            Entity passenger = vehicle.getPassenger();
            return passenger != null ? Collections.singletonList(passenger) : Collections.emptyList();
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

    public static boolean isAnimal(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity instanceof Animals
                || entity instanceof WaterMob
                || entity instanceof NPC
                || entity instanceof Bat
                || entity instanceof Snowman
                || entity instanceof IronGolem
                // Temporary code, replace with enum after CMILib new GitHub Releases release
                || (Version.isCurrentEqualOrHigher(Version.v26_2_0) && entity instanceof org.bukkit.entity.SulfurCube)) {
            return true;
        }
        CMIEntityType type = CMIEntityType.get(entity);
        if (type == null) {
            return false;
        }
        switch (type) {
        case ALLAY:
        case COPPER_GOLEM:
            return true;
        default:
            return false;
        }
    }

    public static boolean isMonster(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        if (Version.isCurrentEqualOrHigher(Version.v1_19_3)) {
            return entity instanceof org.bukkit.entity.Enemy;
        }
        if (entity instanceof Monster) {
            return true;
        }
        CMIEntityType type = CMIEntityType.get(entity);
        if (type == null) {
            return false;
        }
        switch (type) {
        case ENDER_DRAGON:
        case GHAST:
        case HOGLIN:
        case PHANTOM:
        case SHULKER:
        case SLIME:
            return true;
        default:
            return false;
        }
    }

    public static boolean isTamed(Entity entity) {
        return entity instanceof Tameable && ((Tameable) entity).isTamed();
    }

    public static boolean isVillagerOrTrader(Entity entity) {
        if (Version.isCurrentEqualOrHigher(Version.v1_14_0)) {
            return entity instanceof org.bukkit.entity.AbstractVillager;
        }
        return entity instanceof Villager;
    }

    public static boolean isPhantom(Entity entity) {
        return Version.isCurrentEqualOrHigher(Version.v1_13_0) && entity instanceof org.bukkit.entity.Phantom;
    }

    public static boolean isArmorStand(Entity entity) {
        return Version.isCurrentEqualOrHigher(Version.v1_8_0) && entity instanceof org.bukkit.entity.ArmorStand;
    }

    public static boolean isCopperGolem(Entity entity) {
        return Version.isCurrentEqualOrHigher(Version.v1_21_9) && entity instanceof org.bukkit.entity.CopperGolem;
    }

    public static boolean isSpectator(org.bukkit.GameMode mode) {
        return Version.isCurrentEqualOrHigher(Version.v1_8_0) && mode == org.bukkit.GameMode.SPECTATOR;
    }

    public static boolean isMainHand(PlayerInteractEvent event) {
        if (Version.isCurrentEqualOrHigher(Version.v1_9_0)) {
            return event.getHand() == EquipmentSlot.HAND;
        }
        return true;
    }

    public static boolean isContainerEntityWithoutGui(Entity entity) {
        return  entity instanceof ItemFrame
                || entity instanceof PoweredMinecart
                || (Version.isCurrentEqualOrHigher(Version.v1_19_0) && entity instanceof org.bukkit.entity.Allay);
    }

    public static boolean isChorusTeleport(TeleportCause tpcause) {
        return Version.isCurrentEqualOrHigher(Version.v1_9_0) && tpcause == TeleportCause.CHORUS_FRUIT;
    }

    public static List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
        if (Version.isCurrentEqualOrHigher(Version.v1_8_0)) {
            return event.getBlocks();
        }
        return Collections.singletonList(event.getBlock());
    }

    public static InventoryHolder getHolderNoSnapshot(Inventory inventory) {
        if (Version.isCurrentEqualOrHigher(Version.v1_16_0) && Version.isPaperBranch()) {
            // Skipping snapshot creation is faster
            return inventory.getHolder(false);
        }
        return inventory.getHolder();
    }

    // This blacklist is configured in flags.yml
    public static boolean isItemBlacklisted(@Nullable ItemStack item, @NotNull Player player) {
        if (item == null || ResAdmin.isResAdmin(player)) {
            return false;
        }
        if (!Residence.getInstance().getItemManager().isAllowed(item.getType(), player)) {
            lm.General_ItemBlacklisted.sendMessage(player);
            return true;
        }
        return false;
    }

    // Supports 1.7.10+
    public static @Nullable ItemStack getItemInUseHand(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = CMIItemStack.getItemInMainHand(player);
        try {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                item = CMIItemStack.getItemInOffHand(player);
            }
        } catch (Throwable ignored) {
        }
        return item;
    }

    public static boolean isContainer(Material mat) {
        return FlagPermissions.getMaterialUseFlagList().get(mat) == Flags.container
                || Residence.getInstance().getConfigManager().getCustomContainers().contains(mat);
    }

    public static boolean isUndead(Entity entity) {
        if (entity == null) {
            return false;
        }
        EntityType type = entity.getType();
        if (Version.isCurrentEqualOrHigher(Version.v1_14_0)) {
            return ResidenceListener1_14.isEntityTag(type, "undead");
        }
        if (entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Wither) {
            return true;
        }
        CMIEntityType cType = CMIEntityType.get(type);
        if (cType == null) {
            return false;
        }
        switch (cType) {
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
                return true;
            default:
                return false;
        }
    }
}
