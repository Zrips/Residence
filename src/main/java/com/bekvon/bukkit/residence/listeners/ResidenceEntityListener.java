package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Entities.CMIEntity;
import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class ResidenceEntityListener implements Listener {

    Residence plugin;

    public ResidenceEntityListener(Residence plugin) {
        this.plugin = plugin;
    }

    private final static String CrossbowShooter = "CrossbowShooter";

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanTeleport(EntityTeleportEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getTo()))
            return;

        if (event.getEntityType() != EntityType.ENDERMAN)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getTo());
        if (perms.has(Flags.monsters, FlagCombo.OnlyFalse) || perms.has(Flags.nomobs, FlagCombo.OnlyTrue))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;

        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Entity entity = event.getEntity();

        if (entity instanceof Enderman) {
            if (FlagPermissions.has(block.getLocation(), Flags.destroy, FlagCombo.OnlyFalse))
                event.setCancelled(true);

        } else if (entity instanceof Boat) {
            if (!CMIMaterial.get(block.getType()).equals(CMIMaterial.LILY_PAD))
                return;

            Player riderPlayer = null;

            if (Version.isCurrentEqualOrLower(Version.v1_11_R1)) {
                Entity rider = entity.getPassenger();
                riderPlayer = rider instanceof Player ? (Player) rider : null;

            } else {
                List<Entity> passengers = entity.getPassengers();
                if (!passengers.isEmpty()) {
                    // first passenger
                    Entity rider = passengers.get(0);
                    riderPlayer = rider instanceof Player ? (Player) rider : null;
                }
            }

            if (riderPlayer != null) {
                if (ResAdmin.isResAdmin(riderPlayer))
                    return;

                if (FlagPermissions.has(block.getLocation(), riderPlayer, Flags.destroy, FlagCombo.OnlyFalse))
                    event.setCancelled(true);

            } else {
                if (FlagPermissions.has(block.getLocation(), Flags.destroy, FlagCombo.OnlyFalse))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntitySpawnEvent(EntitySpawnEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.nomobs.isGlobalyEnabled())
            return;

        Entity entity = event.getEntity();
        if (entity == null)
            return;

        if (!(entity instanceof LivingEntity))
            return;

        if (!isMonster(entity))
            return;

        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());

        if (perms.has(Flags.nomobs, FlagCombo.OnlyTrue)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.trample.isGlobalyEnabled())
            return;
        // disabling event on world
        Block block = event.getBlock();
        if (block == null)
            return;
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;
        CMIMaterial mat = CMIMaterial.get(block);
        Entity entity = event.getEntity();
        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        boolean hastrample = perms.has(Flags.trample, perms.has(Flags.build, true));
        if (!hastrample && entity.getType() != EntityType.FALLING_BLOCK && (mat.equals(CMIMaterial.FARMLAND) || mat.equals(CMIMaterial.SOUL_SAND))) {
            event.setCancelled(true);
        }
    }

    public static boolean isMonster(Entity ent) {
        return (ent instanceof Monster || ent instanceof Slime || ent instanceof Ghast || Version.isCurrentEqualOrHigher(Version.v1_11_R1) && ent instanceof org.bukkit.entity.Shulker);
    }

    private static boolean isTamed(Entity ent) {
        return (ent instanceof Tameable ? ((Tameable) ent).isTamed() : false);
    }

    private static boolean damageableProjectile(Entity ent) {
        if (ent instanceof Projectile && ent.getType().toString().equalsIgnoreCase("Splash_potion")) {

            if (((ThrownPotion) ent).getEffects().isEmpty())
                return true;
            for (PotionEffect one : ((ThrownPotion) ent).getEffects()) {
                for (String oneHarm : Residence.getInstance().getConfigManager().getNegativePotionEffects()) {
                    if (oneHarm.equalsIgnoreCase(one.getType().getName()))
                        return true;
                }
            }
        }
        return ent instanceof Projectile || ent.getType().toString().equalsIgnoreCase("Trident") || ent.getType().toString().equalsIgnoreCase("Spectral_Arrow");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(EntityDamageEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled())
            return;
        Entity entity = event.getEntity();
        if (entity == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        if (!Utils.isAnimal(entity))
            return;

        if (event.getCause() == DamageCause.LIGHTNING || event.getCause() == DamageCause.FIRE_TICK) {
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());
            if (res != null && res.getPermissions().has(Flags.animalkilling, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }
            return;
        }

        if (!(event instanceof EntityDamageByEntityEvent))
            return;

        EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
        Entity damager = attackevent.getDamager();

        boolean damageable = damageableProjectile(damager);

        if (!damageable && !(damager instanceof Player))
            return;

        if (damageable && !(((Projectile) damager).getShooter() instanceof Player))
            return;

        Player cause = Utils.potentialProjectileToPlayer(damager);

        if (cause == null)
            return;

        if (ResAdmin.isResAdmin(cause))
            return;

        if (FlagPermissions.has(entity.getLocation(), cause, Flags.animalkilling, true))
            return;

        lm.Flag_Deny.sendMessage(cause, Flags.animalkilling);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKillingByFlame(EntityCombustByEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity entity = event.getEntity();
        if (entity == null)
            return;
        if (!Utils.isAnimal(entity))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

        if (res == null)
            return;

        Entity damager = event.getCombuster();

        if (!damageableProjectile(damager) && !(damager instanceof Player))
            return;

        if (damageableProjectile(damager) && !(((Projectile) damager).getShooter() instanceof Player))
            return;

        Player cause = Utils.potentialProjectileToPlayer(damager);

        if (cause == null)
            return;

        if (ResAdmin.isResAdmin(cause))
            return;

        if (res.getPermissions().playerHas(cause, Flags.animalkilling, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(cause, Flags.animalkilling, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalDamageByMobs(EntityDamageByEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity entity = event.getEntity();
        if (entity == null)
            return;
        if (!Utils.isAnimal(entity))
            return;

        Entity damager = event.getDamager();

        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player || damager instanceof Player)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());
        FlagPermissions world = plugin.getWorldFlags().getPerms(entity.getWorld().getName());
        if (!perms.has(Flags.animalkilling, world.has(Flags.animalkilling, true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnEntityDeath(EntityDeathEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.mobitemdrop.isGlobalyEnabled() && !Flags.mobexpdrop.isGlobalyEnabled())
            return;
        // disabling event on world
        LivingEntity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;
        if (ent instanceof Player)
            return;
        Location loc = ent.getLocation();
        FlagPermissions perms = FlagPermissions.getPerms(loc);
        if (!perms.has(Flags.mobitemdrop, true)) {
            event.getDrops().clear();
        }
        if (!perms.has(Flags.mobexpdrop, true)) {
            event.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void VehicleDestroy(VehicleDestroyEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.vehicledestroy.isGlobalyEnabled())
            return;

        // disabling event on world
        Entity damager = event.getAttacker();
        if (damager == null)
            return;

        if (plugin.isDisabledWorldListener(damager.getWorld()))
            return;

        Vehicle vehicle = event.getVehicle();

        if (!vehicleDamageable(damager, vehicle))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void vehicleCombust(EntityCombustByEntityEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.vehicledestroy.isGlobalyEnabled())
            return;

        // disabling event on world
        Entity damager = event.getCombuster();
        if (damager == null)
            return;

        if (plugin.isDisabledWorldListener(damager.getWorld()))
            return;

        if (!(event.getEntity() instanceof Vehicle))
            return;

        Vehicle vehicle = (Vehicle) event.getEntity();

        if (!vehicleDamageable(damager, vehicle))
            event.setCancelled(true);
    }

    private boolean vehicleDamageable(Entity damager, Vehicle vehicle) {

        if (vehicle == null)
            return true;

        Player cause = Utils.potentialProjectileToPlayer(damager);

        if (cause == null) {
            FlagPermissions perms = FlagPermissions.getPerms(vehicle.getLocation());
            if (!perms.has(Flags.vehicledestroy, true)) {
                return false;
            }
            return true;
        }

        if (ResAdmin.isResAdmin(cause))
            return true;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(vehicle.getLocation());

        if (res == null)
            return true;

        if (res.getPermissions().playerHas(cause, Flags.vehicledestroy, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(cause, Flags.vehicledestroy, res.getName());
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void MonsterKilling(EntityDamageByEntityEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.mobkilling.isGlobalyEnabled())
            return;
        // disabling event on world
        Entity entity = event.getEntity();
        if (entity == null)
            return;
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        if (!isMonster(entity))
            return;

        Entity damager = event.getDamager();

        if (!damageableProjectile(damager) && !(damager instanceof Player))
            return;

        if (damageableProjectile(damager) && !(((Projectile) damager).getShooter() instanceof Player))
            return;

        Player cause = Utils.potentialProjectileToPlayer(damager);

        if (cause == null)
            return;

        if (ResAdmin.isResAdmin(cause))
            return;

        if (FlagPermissions.has(entity.getLocation(), cause, Flags.mobkilling, true))
            return;

        lm.Flag_Deny.sendMessage(cause, Flags.mobkilling);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void AnimalLeash(PlayerLeashEntityEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        Entity entity = event.getEntity();

        Player player = event.getPlayer();

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation(), player);
        if (perms.playerHas(player, Flags.leash, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.leash);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFenceLeashInteract(PlayerInteractEntityEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getRightClicked().getWorld()))
            return;
        Player player = event.getPlayer();

        Entity entity = event.getRightClicked();

        if (CMIEntityType.get(entity.getType()) != CMIEntityType.LEASH_KNOT)
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation(), player);
        if (perms.playerHas(player, Flags.leash, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.leash);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWitherSpawn(CreatureSpawnEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.witherspawn.isGlobalyEnabled())
            return;
        // disabling event on world
        Entity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        if (ent.getType() != EntityType.WITHER)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getLocation());
        if (perms.has(Flags.witherspawn, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPhantomSpawn(CreatureSpawnEvent event) {
        if (Version.isCurrentLower(Version.v1_13_R1))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.phantomspawn.isGlobalyEnabled())
            return;
        // disabling event on world
        Entity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        if (ent.getType() != EntityType.PHANTOM)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getLocation());
        if (perms.has(Flags.phantomspawn, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // disabling event on world
        Entity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;
        FlagPermissions perms = FlagPermissions.getPerms(event.getLocation());
        if (Utils.isAnimal(ent)) {
            if (!perms.has(Flags.animals, true)) {
                event.setCancelled(true);
                return;
            }
            switch (event.getSpawnReason()) {
            case BUILD_WITHER:
                break;
            case BUILD_IRONGOLEM:
            case BUILD_SNOWMAN:
            case CUSTOM:
            case DEFAULT:
                if (perms.has(Flags.canimals, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case BREEDING:
            case CHUNK_GEN:
            case CURED:
            case DISPENSE_EGG:
            case EGG:
            case JOCKEY:
            case MOUNT:
            case VILLAGE_INVASION:
            case VILLAGE_DEFENSE:
            case NETHER_PORTAL:
            case OCELOT_BABY:
            case NATURAL:
                if (perms.has(Flags.nanimals, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case SPAWNER_EGG:
            case SPAWNER:
                if (perms.has(Flags.sanimals, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
                break;
            default:
                break;
            }
        } else if (isMonster(ent)) {
            if (perms.has(Flags.monsters, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                return;
            }
            switch (event.getSpawnReason()) {
            case BUILD_WITHER:
            case CUSTOM:
            case DEFAULT:
                if (perms.has(Flags.cmonsters, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case CHUNK_GEN:
            case CURED:
            case DISPENSE_EGG:
            case INFECTION:
            case JOCKEY:
            case MOUNT:
            case NETHER_PORTAL:
            case SILVERFISH_BLOCK:
            case SLIME_SPLIT:
            case LIGHTNING:
            case NATURAL:
                if (perms.has(Flags.nmonsters, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case SPAWNER_EGG:
            case SPAWNER:
                if (perms.has(Flags.smonsters, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
                break;
            default:
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {

        // disabling event on world
        Player player = event.getPlayer();
        if (player == null)
            return;
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;
        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getEntity().getLocation(), player);
        if (!perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true))) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.place);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.shoot.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        if (CMIEntityType.get(event.getEntity()) == CMIEntityType.EXPERIENCE_BOTTLE)
            return;

        if (event.getEntity().getShooter() instanceof Player) {
            if (ResAdmin.isResAdmin((Player) event.getEntity().getShooter()))
                return;
        }

        FlagPermissions perms = FlagPermissions.getPerms(event.getEntity().getLocation());
        if (perms.has(Flags.shoot, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            if (event.getEntity().getShooter() instanceof Player)
                lm.Flag_Deny.sendMessage((Player) event.getEntity().getShooter(), Flags.shoot);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {

        // disabling event on world
        Hanging ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        if (!(event.getRemover() instanceof Player))
            return;

        Player player = (Player) event.getRemover();
        if (ResAdmin.isResAdmin(player))
            return;

        if (plugin.getResidenceManager().isOwnerOfLocation(player, ent.getLocation()))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation(), player);
        if (!perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true))) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.destroy);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakEventByExplosion(HangingBreakEvent event) {
        // disabling event on world
        Hanging ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        if (!event.getCause().equals(RemoveCause.EXPLOSION))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (perms.has(Flags.explode, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakEvent(HangingBreakEvent event) {
        // disabling event on world
        Hanging ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        CMIEntityType type = CMIEntityType.get(event.getEntity().getType());

        if (!type.equals(CMIEntityType.ITEM_FRAME) && !type.equals(CMIEntityType.GLOW_ITEM_FRAME))
            return;

        if (!event.getCause().equals(RemoveCause.PHYSICS))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (!perms.has(Flags.destroy, perms.has(Flags.build, true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        // disabling event on world
        Hanging ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        if (event.getRemover() instanceof Player)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (!perms.has(Flags.destroy, perms.has(Flags.build, true))) {

            if (Utils.isSourceBlockInsideSameResidence(event.getRemover(), ClaimedResidence.getByLoc(event.getEntity().getLocation())))
                return;

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.burn.isGlobalyEnabled())
            return;
        // disabling event on world
        Entity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;
        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (!perms.has(Flags.burn, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        // disabling event on world
        Entity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;
        EntityType entity = event.getEntityType();
        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());

        switch (CMIEntityType.get(entity)) {
        case CREEPER:

            // Disabling listener if flag disabled globally
            if (!Flags.creeper.isGlobalyEnabled())
                break;
            if (!perms.has(Flags.creeper, perms.has(Flags.explode, true))) {
                if (plugin.getConfigManager().isCreeperExplodeBelow()) {
                    if (ent.getLocation().getBlockY() >= plugin.getConfigManager().getCreeperExplodeBelowLevel()) {
                        event.setCancelled(true);
                        ent.remove();
                    } else {
                        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
                        if (res != null) {
                            event.setCancelled(true);
                            ent.remove();
                        }
                    }
                } else {
                    event.setCancelled(true);
                    ent.remove();
                }
            }
            break;
        case TNT:
        case TNT_MINECART:

            // Disabling listener if flag disabled globally
            if (!Flags.tnt.isGlobalyEnabled())
                break;

            if (!perms.has(Flags.tnt, perms.has(Flags.explode, true))) {
                if (plugin.getConfigManager().isTNTExplodeBelow()) {
                    if (ent.getLocation().getBlockY() >= plugin.getConfigManager().getTNTExplodeBelowLevel()) {
                        event.setCancelled(true);
                        ent.remove();
                    } else {
                        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
                        if (res != null) {
                            event.setCancelled(true);
                            ent.remove();
                        }
                    }
                } else {
                    event.setCancelled(true);
                    ent.remove();
                }
            }
            break;
        case SMALL_FIREBALL:
        case FIREBALL:
            // Disabling listener if flag disabled globally
            if (!Flags.explode.isGlobalyEnabled())
                break;
            if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.fireball, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                ent.remove();
            }
            break;
        case WITHER_SKULL:
            // Disabling listener if flag disabled globally
            if (!Flags.explode.isGlobalyEnabled())
                break;
            if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.witherdestruction, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                ent.remove();
            }
            break;
        case WIND_CHARGE:

            if (!Flags.pvp.isGlobalyEnabled())
                break;

            if (perms.has(Flags.pvp, FlagCombo.OnlyFalse)) {
                ProjectileSource shooter = ((Projectile) ent).getShooter();
                if (shooter instanceof Player)
                    lm.Flag_Deny.sendMessage((Player) shooter, Flags.pvp);

                Location loc = ent.getLocation();
                ent.getWorld().spawnParticle(Particle.GUST_EMITTER_SMALL, loc, 0);
                ent.getWorld().playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 2, 0.5F);
                event.setCancelled(true);
            }

            break;
        case WITHER:
            break;
        default:
            if (perms.has(Flags.destroy, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                ent.remove();
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {

        // disabling event on world
        Location loc = event.getLocation();
        if (plugin.isDisabledWorldListener(loc.getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity ent = event.getEntity();

        Boolean cancel = false;
        Boolean remove = true;
        FlagPermissions perms = FlagPermissions.getPerms(loc);
        FlagPermissions world = plugin.getWorldFlags().getPerms(loc.getWorld().getName());

        CMIEntityType ctype = CMIEntityType.get(event.getEntityType());

        if (ent != null && ctype != null) {

            switch (ctype) {
            case CREEPER:
                // Disabling listener if flag disabled globally
                if (!Flags.creeper.isGlobalyEnabled())
                    break;
                if (!perms.has(Flags.creeper, perms.has(Flags.explode, true)))
                    if (plugin.getConfigManager().isCreeperExplodeBelow()) {
                        if (loc.getBlockY() >= plugin.getConfigManager().getCreeperExplodeBelowLevel())
                            cancel = true;
                        else {
                            ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
                            if (res != null)
                                cancel = true;
                        }
                    } else
                        cancel = true;
                break;
            case TNT:
            case TNT_MINECART:
                // Disabling listener if flag disabled globally
                if (!Flags.tnt.isGlobalyEnabled())
                    break;
                if (!perms.has(Flags.tnt, perms.has(Flags.explode, true))) {
                    if (plugin.getConfigManager().isTNTExplodeBelow()) {
                        if (loc.getBlockY() >= plugin.getConfigManager().getTNTExplodeBelowLevel())
                            cancel = true;
                        else {
                            ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
                            if (res != null)
                                cancel = true;
                        }
                    } else
                        cancel = true;
                }
                break;
            case SMALL_FIREBALL:
            case FIREBALL:
                // Disabling listener if flag disabled globally
                if (!Flags.explode.isGlobalyEnabled())
                    return;
                if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.fireball, FlagCombo.OnlyFalse))
                    cancel = true;
                break;
            case WITHER:
            case WITHER_SKULL:
                // Disabling listener if flag disabled globally
                if (!Flags.explode.isGlobalyEnabled())
                    break;
                if (perms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.witherdestruction, FlagCombo.OnlyFalse))
                    cancel = true;
                break;
            case WIND_CHARGE:
                // Disabling listener if flag disabled globally
                if (!Flags.pvp.isGlobalyEnabled())
                    break;
                if (perms.has(Flags.pvp, FlagCombo.OnlyFalse)) {
                    cancel = true;
                    event.setYield(0);
                }
                break;
            case ENDER_DRAGON:
                remove = false;
                break;
            default:
                if (!perms.has(Flags.destroy, world.has(Flags.destroy, true))) {
                    cancel = true;
                    remove = false;
                }
                break;
            }
        } else if (!perms.has(Flags.destroy, world.has(Flags.destroy, true))) {
            cancel = true;
        }

        if (cancel) {
            event.setCancelled(true);
            if (ent != null && remove) {
                if (!event.getEntityType().equals(EntityType.WITHER))
                    ent.remove();
            }
            return;
        }

        List<Block> preserve = new ArrayList<Block>();
        for (Block block : event.blockList()) {
            FlagPermissions blockperms = FlagPermissions.getPerms(block.getLocation());

            if (ent != null && ctype != null) {
                switch (ctype) {
                case CREEPER:
                    // Disabling listener if flag disabled globally
                    if (!Flags.creeper.isGlobalyEnabled())
                        continue;
                    if (!blockperms.has(Flags.creeper, blockperms.has(Flags.explode, true)))
                        if (plugin.getConfigManager().isCreeperExplodeBelow()) {
                            if (block.getY() >= plugin.getConfigManager().getCreeperExplodeBelowLevel())
                                preserve.add(block);
                            else {
                                ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
                                if (res != null)
                                    preserve.add(block);
                            }
                        } else
                            preserve.add(block);
                    continue;
                case TNT:
                case TNT_MINECART:
                    // Disabling listener if flag disabled globally
                    if (!Flags.tnt.isGlobalyEnabled())
                        continue;
                    if (!blockperms.has(Flags.tnt, blockperms.has(Flags.explode, true))) {
                        if (plugin.getConfigManager().isTNTExplodeBelow()) {
                            if (block.getY() >= plugin.getConfigManager().getTNTExplodeBelowLevel())
                                preserve.add(block);
                            else {
                                ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
                                if (res != null)
                                    preserve.add(block);
                            }
                        } else
                            preserve.add(block);
                    }
                    continue;
                case ENDER_DRAGON:
                    // Disabling listener if flag disabled globally
                    if (!Flags.dragongrief.isGlobalyEnabled())
                        break;
                    if (blockperms.has(Flags.dragongrief, FlagCombo.OnlyFalse))
                        preserve.add(block);
                    break;
                case ENDER_CRYSTAL:
                    // Disabling listener if flag disabled globally
                    if (!Flags.explode.isGlobalyEnabled())
                        continue;
                    if (blockperms.has(Flags.explode, FlagCombo.OnlyFalse))
                        preserve.add(block);
                    continue;
                case SMALL_FIREBALL:
                case FIREBALL:
                    // Disabling listener if flag disabled globally
                    if (!Flags.explode.isGlobalyEnabled())
                        continue;
                    if (blockperms.has(Flags.explode, FlagCombo.OnlyFalse) || perms.has(Flags.fireball, FlagCombo.OnlyFalse))
                        preserve.add(block);
                    continue;
                case WITHER:
                case WITHER_SKULL:
                    // Disabling listener if flag disabled globally
                    if (!Flags.explode.isGlobalyEnabled())
                        break;
                    if (blockperms.has(Flags.explode, FlagCombo.OnlyFalse) || blockperms.has(Flags.witherdestruction, FlagCombo.OnlyFalse))
                        preserve.add(block);
                    break;
                default:
                    if (blockperms.has(Flags.destroy, FlagCombo.OnlyFalse) || blockperms.has(Flags.explode, FlagCombo.OnlyFalse))
                        preserve.add(block);
                    continue;
                }
            } else {
                if (!blockperms.has(Flags.destroy, world.has(Flags.destroy, true))) {
                    preserve.add(block);
                }
            }
        }

        for (Block block : preserve) {
            event.blockList().remove(block);
        }
    }

    // Various zombies break the door
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {

        Block block = event.getBlock();
        if (block == null)
            return;

        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (plugin.getPermsByLoc(block.getLocation()).has(Flags.destroy, true))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.witherdestruction.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity ent = event.getEntity();

        if (ent.getType() != EntityType.WITHER)
            return;

        if (!plugin.getPermsByLoc(event.getBlock().getLocation()).has(Flags.witherdestruction, FlagCombo.OnlyFalse))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.pvp.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        ProjectileSource shooter = event.getPotion().getShooter();

        if (shooter instanceof Witch)
            return;

        boolean harmfull = false;

        mein: for (PotionEffect one : event.getPotion().getEffects()) {
            for (String oneHarm : plugin.getConfigManager().getNegativePotionEffects()) {
                if (oneHarm.equalsIgnoreCase(one.getType().getName())) {
                    harmfull = true;
                    break mein;
                }
            }
        }

        if (!harmfull)
            return;

        Entity ent = event.getEntity();
        boolean srcpvp = FlagPermissions.getPerms(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
        boolean animalKilling = FlagPermissions.getPerms(ent.getLocation()).has(Flags.animalkilling, FlagCombo.TrueOrNone);
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        boolean animalDamage = false;
        while (it.hasNext()) {
            LivingEntity target = it.next();

            if (Utils.isAnimal(target)) {
                if (!animalKilling) {
                    event.setIntensity(target, 0);
                    animalDamage = true;
                }
                continue;
            }

            if (target.getType() != EntityType.PLAYER)
                continue;
            Boolean tgtpvp = FlagPermissions.getPerms(target.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
            if (!srcpvp || !tgtpvp) {
                event.setIntensity(target, 0);
                continue;
            }

            ClaimedResidence area = plugin.getResidenceManager().getByLoc(target.getLocation());

            if ((target instanceof Player) && (shooter instanceof Player)) {
                Player attacker = null;
                if (shooter instanceof Player) {
                    attacker = (Player) shooter;
                }
                if (attacker != null) {
                    if (!(target instanceof Player))
                        return;
                    ClaimedResidence srcarea = plugin.getResidenceManager().getByLoc(attacker.getLocation());
                    if (srcarea != null && area != null && srcarea.equals(area) && srcarea.getPermissions().playerHas((Player) target, Flags.friendlyfire, FlagCombo.OnlyFalse) &&
                            srcarea.getPermissions().playerHas(attacker, Flags.friendlyfire, FlagCombo.OnlyFalse)) {
                        CMIActionBar.send(attacker, plugin.getLM().getMessage(lm.General_NoFriendlyFire));
                        event.setIntensity(target, 0);
                    }
                }
            }
        }

        if (!animalKilling && animalDamage && shooter instanceof Player) {
            lm.Flag_Deny.sendMessage((Player) shooter, Flags.animalkilling);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerKillingByFlame(EntityCombustByEntityEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.pvp.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;
        Entity entity = event.getEntity();
        if (entity == null)
            return;
        if (!(entity instanceof Player))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

        if (res == null)
            return;

        Entity damager = event.getCombuster();

        if (!damageableProjectile(damager) && !(damager instanceof Player))
            return;

        if (damageableProjectile(damager) && !(((Projectile) damager).getShooter() instanceof Player))
            return;

        Player cause = Utils.potentialProjectileToPlayer(damager);

        if (cause == null)
            return;

        boolean srcpvp = FlagPermissions.has(cause.getLocation(), Flags.pvp, FlagCombo.TrueOrNone);
        boolean tgtpvp = FlagPermissions.has(entity.getLocation(), Flags.pvp, FlagCombo.TrueOrNone);
        if (!srcpvp || !tgtpvp)
            event.setCancelled(true);
    }

    @EventHandler
    public void OnFallDamage(EntityDamageEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.falldamage.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;
        if (event.getCause() != DamageCause.FALL)
            return;
        Entity ent = event.getEntity();
        if (!(ent instanceof Player))
            return;

        if (!FlagPermissions.getPerms(ent.getLocation()).has(Flags.falldamage, FlagCombo.TrueOrNone)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnArmorStandFlameDamage(EntityDamageEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        if (event.getCause() != DamageCause.FIRE_TICK)
            return;

        Entity ent = event.getEntity();
        if (!Utils.isArmorStandEntity(ent.getType()) && !(ent instanceof Arrow))
            return;

        if (!FlagPermissions.getPerms(ent.getLocation()).has(Flags.destroy, true)) {
            event.setCancelled(true);
            ent.setFireTicks(0);
        }
    }

    @EventHandler
    public void OnArmorStandExplosion(EntityDamageEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        if (event.isCancelled())
            return;

        if (event.getCause() != DamageCause.BLOCK_EXPLOSION && event.getCause() != DamageCause.ENTITY_EXPLOSION)
            return;
        Entity ent = event.getEntity();
        if (!Utils.isArmorStandEntity(ent.getType()) && !(ent instanceof Arrow))
            return;

        if (!FlagPermissions.getPerms(ent.getLocation()).has(Flags.destroy, true)) {
            event.setCancelled(true);
            ent.setFireTicks(0);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCatchingFire(EntityDamageByEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.pvp.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        if (!damageableProjectile(event.getDamager()))
            return;

        if (event.getEntity() == null || !(event.getEntity() instanceof Player))
            return;

        Projectile projectile = (Projectile) event.getDamager();

        FlagPermissions perms = FlagPermissions.getPerms(projectile.getLocation());

        if (!perms.has(Flags.pvp, FlagCombo.TrueOrNone))
            projectile.setFireTicks(0);
    }

    @EventHandler
    public void OnPlayerDamageByLightning(EntityDamageEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.pvp.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;
        if (event.getCause() != DamageCause.LIGHTNING)
            return;
        Entity ent = event.getEntity();
        if (!(ent instanceof Player))
            return;
        if (!FlagPermissions.getPerms(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByFireballEvent(EntityDamageByEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.fireball.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity dmgr = event.getDamager();
        if (dmgr.getType() != EntityType.SMALL_FIREBALL && dmgr.getType() != EntityType.FIREBALL)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getEntity().getLocation());
        if (perms.has(Flags.fireball, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByWitherEvent(EntityDamageByEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.witherdamage.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity dmgr = event.getDamager();
        if (dmgr.getType() != EntityType.WITHER && dmgr.getType() != EntityType.WITHER_SKULL)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getEntity().getLocation());
        if (perms.has(Flags.witherdamage, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        if (CMIEntityType.get(event.getEntityType()) != CMIEntityType.ENDER_CRYSTAL && !CMIEntity.isItemFrame(event.getEntity()) && !Utils.isArmorStandEntity(event.getEntityType()))
            return;

        Entity dmgr = event.getDamager();

        Player player = Utils.potentialProjectileToPlayer(dmgr);

        CMIEntityType type = CMIEntityType.get(event.getEntityType());

        if ((dmgr instanceof Projectile) && (!(((Projectile) dmgr).getShooter() instanceof Player))) {

            if (Utils.isSourceBlockInsideSameResidence(dmgr, ClaimedResidence.getByLoc(event.getEntity().getLocation())))
                return;

            Location loc = event.getEntity().getLocation();
            FlagPermissions perm = FlagPermissions.getPerms(loc);
            if (perm.has(Flags.destroy, FlagCombo.OnlyFalse))
                event.setCancelled(true);

            return;
        } else if (type == CMIEntityType.TNT || type == CMIEntityType.TNT_MINECART) {

            // Disabling listener if flag disabled globally
            if (Flags.explode.isGlobalyEnabled()) {
                FlagPermissions perms = FlagPermissions.getPerms(event.getEntity().getLocation());
                if (perms.has(Flags.explode, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
            }
        } else if (type == CMIEntityType.WITHER_SKULL || type == CMIEntityType.WITHER) {

            // Disabling listener if flag disabled globally
            if (Flags.witherdamage.isGlobalyEnabled()) {
                FlagPermissions perms = FlagPermissions.getPerms(event.getEntity().getLocation());
                if (perms.has(Flags.witherdamage, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        Location loc = event.getEntity().getLocation();

        FlagPermissions perms = FlagPermissions.getPerms(loc, player);

        if (isMonster(dmgr) && !perms.has(Flags.destroy, false)) {
            event.setCancelled(true);
            return;
        }

        if (player == null)
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        if (CMIEntity.isItemFrame(event.getEntity())) {
            ItemStack stack = null;
            if (event.getEntityType() == EntityType.ITEM_FRAME) {
                ItemFrame it = (ItemFrame) event.getEntity();
                stack = it.getItem();
            } else {
                org.bukkit.entity.GlowItemFrame it = (org.bukkit.entity.GlowItemFrame) event.getEntity();
                stack = it.getItem();
            }

            if (stack != null) {
                if (!ResPerm.bypass_container.hasPermission(player, 10000L) && !perms.playerHas(player, Flags.container, true)) {
                    event.setCancelled(true);
                    lm.Flag_Deny.sendMessage(player, Flags.container);
                }

                // Specific fix for the Itemadders plugin.
                // Custom event will not have damage source while it contains item as paper
                // inside of it
                if (Version.isCurrentEqualOrHigher(Version.v1_21_R1) && event.getDamageSource() != null && event.getDamageSource().getCausingEntity() == null && !perms.playerHas(player, Flags.destroy,
                        perms.playerHas(player, Flags.build, true))) {
                    event.setCancelled(true);
                    lm.Flag_Deny.sendMessage(player, Flags.destroy);
                }

                return;
            }
        }

        if (!perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true))) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.destroy);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {

        if (Version.isCurrentEqualOrLower(Version.v1_14_R1))
            return;

        if (event.getBow() == null)
            return;

        if (event.getBow().getType() != Material.CROSSBOW)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (CMIEntityType.get(event.getProjectile()) == CMIEntityType.FIREWORK_ROCKET)
            event.getProjectile().setMetadata(CrossbowShooter, new FixedMetadataValue(plugin, event.getEntity().getUniqueId()));
    }

    public static boolean canDamageEntity(Entity damager, Entity victim, boolean inform) {

        boolean tamedAnimal = isTamed(victim);
        ClaimedResidence area = Residence.getInstance().getResidenceManager().getByLoc(victim.getLocation());

        ClaimedResidence srcarea = null;
        if (damager != null) {
            srcarea = Residence.getInstance().getResidenceManager().getByLoc(damager.getLocation());
        }
        boolean srcpvp = true;
        boolean allowSnowBall = false;
        boolean isSnowBall = false;
        boolean isOnFire = false;
        if (srcarea != null) {
            srcpvp = srcarea.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone);
        }

//	    ent = attackevent.getEntity();
        if ((victim instanceof Player || tamedAnimal) && (damager instanceof Player || (damager instanceof Projectile && (((Projectile) damager)
                .getShooter() instanceof Player))) || damager instanceof Firework) {

            Player attacker = null;
            if (damager instanceof Player) {
                attacker = (Player) damager;
            } else if (damager instanceof Projectile) {
                Projectile project = (Projectile) damager;
                if (project.getType() == EntityType.SNOWBALL && srcarea != null) {
                    isSnowBall = true;
                    allowSnowBall = srcarea.getPermissions().has(Flags.snowball, FlagCombo.TrueOrNone);
                }
                if (project.getFireTicks() > 0)
                    isOnFire = true;

                attacker = (Player) ((Projectile) damager).getShooter();
            } else if (damager instanceof Firework) {
                List<MetadataValue> meta = damager.getMetadata(CrossbowShooter);
                if (meta != null && !meta.isEmpty()) {
                    try {
                        String uid = meta.get(0).asString();
                        attacker = Bukkit.getPlayer(UUID.fromString(uid));
                    } catch (Throwable e) {
                    }
                }
            }

            if (!(victim instanceof Player))
                return true;

            if (srcarea != null && area != null && srcarea.equals(area) && attacker != null && area.getRaid().isUnderRaid() && area.getRaid().onSameTeam(attacker, (Player) victim)
                    && !ConfigManager.RaidFriendlyFire) {
                return false;
            }

            if (srcarea != null && area != null && srcarea.equals(area) && attacker != null && area.getRaid().isUnderRaid() && !area.getRaid().onSameTeam(attacker, (Player) victim)) {
                return true;
            }

            if (srcarea != null && area != null && srcarea.equals(area) && attacker != null &&
                    srcarea.getPermissions().playerHas((Player) victim, Flags.friendlyfire, FlagCombo.OnlyFalse) &&
                    srcarea.getPermissions().playerHas(attacker, Flags.friendlyfire, FlagCombo.OnlyFalse)) {

                CMIActionBar.send(attacker, Residence.getInstance().getLM().getMessage(lm.General_NoFriendlyFire));
                if (isOnFire)
                    victim.setFireTicks(0);
                return false;
            }

            if (!srcpvp && !isSnowBall || !allowSnowBall && isSnowBall) {
                if (attacker != null && inform)
                    lm.General_NoPVPZone.sendMessage(attacker);
                if (isOnFire)
                    victim.setFireTicks(0);
                return false;
            }

            /* Check for Player vs Player */
            if (area == null) {
                /* World PvP */
                if (damager != null)
                    if (!Residence.getInstance().getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.pvp, FlagCombo.TrueOrNone)) {
                        if (attacker != null && inform)
                            lm.General_WorldPVPDisabled.sendMessage(attacker);
                        return false;
                    }

                /* Attacking from safe zone */
                if (attacker != null) {
                    FlagPermissions aPerm = FlagPermissions.getPerms(attacker.getLocation());
                    if (!aPerm.has(Flags.pvp, FlagCombo.TrueOrNone)) {
                        if (inform)
                            lm.General_NoPVPZone.sendMessage(attacker);
                        return false;
                    }
                }
            } else {
                /* Normal PvP */
                if (!isSnowBall && !area.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone) || isSnowBall && !allowSnowBall) {
                    if (attacker != null)
                        if (inform)
                            lm.General_NoPVPZone.sendMessage(attacker);
                    return false;
                }
            }
            return true;
        } else if ((victim instanceof Player || tamedAnimal) && (damager instanceof Creeper)) {
            if (area == null && !Residence.getInstance().getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.creeper, true) || area != null && !area.getPermissions().has(Flags.creeper,
                    true)) {
                return false;
            }
        }
        return true;
    }

    private static void process(lm lm, Player attacker, boolean isOnFire, Entity ent, EntityDamageEvent event, Entity damager) {
        if (attacker != null)
            lm.sendMessage(attacker);
        if (isOnFire)
            ent.setFireTicks(0);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        Entity ent = event.getEntity();
        if (ent.hasMetadata("NPC"))
            return;

        boolean tamedAnimal = isTamed(ent);
        ClaimedResidence area = plugin.getResidenceManager().getByLoc(ent.getLocation());
        /* Living Entities */
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
            Entity damager = attackevent.getDamager();

            ClaimedResidence srcarea = null;
            if (damager != null) {
                srcarea = plugin.getResidenceManager().getByLoc(damager.getLocation());
            }
            boolean srcpvp = true;
            boolean allowSnowBall = false;
            boolean isSnowBall = false;
            boolean isOnFire = false;
            if (srcarea != null) {
                srcpvp = srcarea.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone);
            }

            ent = attackevent.getEntity();
            if ((ent instanceof Player || tamedAnimal) && (damager instanceof Player || (damager instanceof Projectile && (((Projectile) damager)
                    .getShooter() instanceof Player))) && event.getCause() != DamageCause.FALL || damager instanceof Firework) {

                Player attacker = null;
                if (damager instanceof Player) {
                    attacker = (Player) damager;
                } else if (damager instanceof Projectile) {
                    Projectile project = (Projectile) damager;
                    if (project.getType() == EntityType.SNOWBALL && srcarea != null) {
                        isSnowBall = true;
                        allowSnowBall = srcarea.getPermissions().has(Flags.snowball, FlagCombo.TrueOrNone);
                    }
                    if (project.getFireTicks() > 0)
                        isOnFire = true;

                    ProjectileSource shooter = ((Projectile) damager).getShooter();
                    if (shooter instanceof Player)
                        attacker = (Player) shooter;
                } else if (damager instanceof Firework) {
                    List<MetadataValue> meta = damager.getMetadata(CrossbowShooter);
                    if (meta != null && !meta.isEmpty()) {
                        try {
                            String uid = meta.get(0).asString();
                            attacker = Bukkit.getPlayer(UUID.fromString(uid));
                        } catch (Throwable e) {
                        }
                    }
                }

                if (!(ent instanceof Player))
                    return;

                if (srcarea != null && area != null && srcarea.equals(area) && attacker != null && area.getRaid().isUnderRaid() && area.getRaid().onSameTeam(attacker, (Player) ent)
                        && !ConfigManager.RaidFriendlyFire) {
                    event.setCancelled(true);
                }

                if (srcarea != null && area != null && srcarea.equals(area) && attacker != null && area.getRaid().isUnderRaid() && !area.getRaid().onSameTeam(attacker, (Player) ent)) {
                    return;
                }

                if (srcarea != null && area != null && srcarea.equals(area) && attacker != null &&
                        srcarea.getPermissions().playerHas((Player) ent, Flags.friendlyfire, FlagCombo.OnlyFalse) &&
                        srcarea.getPermissions().playerHas(attacker, Flags.friendlyfire, FlagCombo.OnlyFalse)) {

                    CMIActionBar.send(attacker, plugin.getLM().getMessage(lm.General_NoFriendlyFire));
                    if (isOnFire)
                        ent.setFireTicks(0);
                    event.setCancelled(true);
                }

                if (!srcpvp && !isSnowBall || !allowSnowBall && isSnowBall) {
                    process(lm.General_NoPVPZone, attacker, isOnFire, ent, event, damager);
                    return;
                }

                /* Check for Player vs Player */
                if (area == null) {
                    /* World PvP */
                    if (damager != null)
                        if (!plugin.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.pvp, FlagCombo.TrueOrNone)) {
                            process(lm.General_WorldPVPDisabled, attacker, isOnFire, ent, event, damager);
                            return;
                        }

                    /* Attacking from safe zone */
                    if (attacker != null) {
                        FlagPermissions aPerm = FlagPermissions.getPerms(attacker.getLocation());
                        if (!aPerm.has(Flags.pvp, FlagCombo.TrueOrNone)) {
                            process(lm.General_NoPVPZone, attacker, isOnFire, ent, event, damager);
                            return;
                        }
                    }
                } else {
                    /* Normal PvP */
                    if (!isSnowBall && !area.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone) || isSnowBall && !allowSnowBall) {
                        process(lm.General_NoPVPZone, attacker, isOnFire, ent, event, damager);
                        return;
                    }
                }
                return;
            } else if ((ent instanceof Player || tamedAnimal) && (damager instanceof Creeper)) {
                if (area == null && !plugin.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.creeper, true)) {
                    event.setCancelled(true);
                } else if (area != null && !area.getPermissions().has(Flags.creeper, true)) {
                    event.setCancelled(true);
                }
            }
        }
        if (area == null) {
            if (!plugin.getWorldFlags().getPerms(ent.getWorld().getName()).has(Flags.damage, true) && (ent instanceof Player || tamedAnimal)) {
                event.setCancelled(true);
            }
        } else {
            if (!area.getPermissions().has(Flags.damage, true) && (ent instanceof Player || tamedAnimal)) {
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) {
            /* Put out a fire on a player */
            if ((ent instanceof Player || tamedAnimal) && (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event
                    .getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
                ent.setFireTicks(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtFish(PlayerInteractEntityEvent event) {

        if (Version.isCurrentLower(Version.v1_12_R1))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();
        if (!(ent instanceof org.bukkit.entity.Fish))
            return;

        ItemStack iih = CMIItemStack.getItemInMainHand(player);
        if (iih == null)
            return;

        if (!CMIMaterial.get(iih).equals(CMIMaterial.WATER_BUCKET))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation(), player);

        if (!perms.playerHas(player, Flags.animalkilling, FlagCombo.TrueOrNone)) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.animalkilling);
        }
    }
}
