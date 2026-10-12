package com.bekvon.bukkit.residence.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.minecart.ExplosiveMinecart;
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
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
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
import com.bekvon.bukkit.residence.utils.PotionUtils;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResidenceEntityListener implements Listener {

    private final Residence plugin;

    public ResidenceEntityListener(Residence plugin) {
        this.plugin = plugin;
    }

    private final static String CrossbowShooter = "CrossbowShooter";

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnderManTeleport(EntityTeleportEvent event) {

        Location eventTo = event.getTo();
        // disabling event on world
        if (plugin.isDisabledWorldListener(eventTo)) {
            return;
        }
        if (event.getEntityType() != EntityType.ENDERMAN)
            return;

        FlagPermissions perms = null;
        if (Flags.monsters.isGlobalyEnabled()) {
            perms = FlagPermissions.getPerms(eventTo);
            if (perms.has(Flags.monsters, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                return;
            }
        }
        if (Flags.nomobs.isGlobalyEnabled()) {
            if (perms == null) {
                perms = FlagPermissions.getPerms(eventTo);
            }
            if (perms.has(Flags.nomobs, FlagCombo.OnlyTrue)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block)) {
            return;
        }
        Entity entity = event.getEntity();
        boolean shouldDeny = false;

        if (Flags.animalgriefing.isGlobalyEnabled() && Utils.isAnimal(entity)) {
            // Animals are friendly (villagers farming/sheep grazing)
            // When Flags.animalgriefing is None, do not fall back to Flags.destroy
            shouldDeny = FlagPermissions.has(block.getLocation(), Flags.animalgriefing, FlagCombo.OnlyFalse);

        } else if (Flags.witherdestruction.isGlobalyEnabled() && entity instanceof Wither) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            shouldDeny = !perms.has(Flags.witherdestruction, perms.has(Flags.destroy, true));

        } else if (Flags.mobgriefing.isGlobalyEnabled() && Utils.isMonster(entity)) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            shouldDeny = !perms.has(Flags.mobgriefing, perms.has(Flags.destroy, true));

        } else if (entity instanceof Player) {
            shouldDeny = shouldDenyPlayerChangeBlock(block, (Player) entity);

        } else if (Flags.destroy.isGlobalyEnabled() && entity instanceof Boat) {
            shouldDeny = shouldDenyBoatBreakLilyPad((Boat) entity, block);

        } else if (Flags.destroy.isGlobalyEnabled() && entity instanceof Projectile) {
            // Projectile-triggered EntityChangeBlockEvent always breaks blocks
            shouldDeny = shouldDenyEntityHit(entity, block.getLocation(), Flags.destroy, null);

        }

        if (shouldDeny) {
            event.setCancelled(true);
        }
    }

    private boolean shouldDenyPlayerChangeBlock(Block block, Player player) {
        CMIMaterial mat = CMIMaterial.get(block.getType());
        Flags mainFlag = null;
        Flags subFlag = null;
        if (Flags.copper.isGlobalyEnabled() && mat.containsCriteria(CMIMC.COPPER)) {
            mainFlag = Flags.copper;
            subFlag = Flags.build;

        } else if (Flags.brush.isGlobalyEnabled() && (mat == CMIMaterial.SUSPICIOUS_GRAVEL || mat == CMIMaterial.SUSPICIOUS_SAND)) {
            mainFlag = Flags.brush;
            subFlag = Flags.build;

        } else if (Flags.build.isGlobalyEnabled()) {
            // by default, future player-triggered EntityChangeBlockEvent mechanisms check Flags.build
            mainFlag = Flags.build;

        }
        if (mainFlag == null) {
            return false;
        }
        return FlagPermissions.shouldDenyAndNotify(player, block, mainFlag, subFlag);
    }

    private boolean shouldDenyBoatBreakLilyPad(Boat boat, Block block) {
        if (CMIMaterial.get(block.getType()) != CMIMaterial.LILY_PAD) {
            return false;
        }
        List<Entity> passengers = Utils.getPassengers(boat);
        Entity rider = null;
        if (!passengers.isEmpty()) {
            // first passenger
            rider = passengers.get(0);
        }
        Player player = rider instanceof Player ? (Player) rider : null;
        if (player != null) {
            return FlagPermissions.shouldDenyAndNotify(player, block, Flags.destroy, null, false);
        } else {
            return FlagPermissions.has(block.getLocation(), Flags.destroy, FlagCombo.OnlyFalse);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntitySpawnEvent(EntitySpawnEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.nomobs.isGlobalyEnabled())
            return;

        Entity entity = event.getEntity();

        if (!(entity instanceof LivingEntity))
            return;

        if (!Utils.isMonster(entity)) {
            return;
        }
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());

        if (perms.has(Flags.nomobs, FlagCombo.OnlyTrue)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractEvent(EntityInteractEvent event) {

        Entity entity = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity)) {
            return;
        }
        Block block = event.getBlock();
        CMIMaterial mat = CMIMaterial.get(block.getType());
        Flags flag = null;
        // Start getting flags
        switch (mat) {
        case FARMLAND:
            flag = Flags.trample;
            break;
        case TURTLE_EGG:
            if (Utils.isAnimal(entity)) {
                flag = Flags.animalgriefing;
            } else if (Utils.isMonster(entity)) {
                flag = Flags.mobgriefing;
            } else {
                // Other entities
                flag = Flags.destroy;
            }
            break;
        default:
            if (mat.containsCriteria(CMIMC.BUTTON)) {
                if (entity instanceof Projectile) {
                    flag = Flags.button;
                }
            } else if (mat.containsCriteria(CMIMC.PRESSUREPLATE)) {
                if (entity instanceof Projectile || entity instanceof Item) {
                    flag = Flags.pressure;
                }
            }
            break;
        }
        if (flag == null || !flag.isGlobalyEnabled()) {
            return;
        }
        FlagPermissions perms;
        // Start checking flags
        switch (flag) {
        // Farmland: Mob StepOn
        case trample:
            perms = FlagPermissions.getPerms(block.getLocation());
            if (perms.has(flag, perms.has(Flags.build, true))) {
                return;
            }
            break;
        // Turtle Egg: Mob StepOn
        case animalgriefing:
        case mobgriefing:
            perms = FlagPermissions.getPerms(block.getLocation());
            if (perms.has(flag, perms.has(Flags.destroy, true))) {
                return;
            }
            break;
        // Turtle Egg: Other-entities StepOn
        case destroy:
            perms = FlagPermissions.getPerms(block.getLocation());
            if (perms.has(flag, true)) {
                return;
            }
            break;
        // Projectile hits button
        case button:
        // Projectile and Item press pressure_plate
        case pressure:
            Player player = Utils.potentialProjectileToPlayer(entity);
            if (player != null) {
                if (!FlagPermissions.shouldDenyAndNotify(player, block, flag, Flags.use, false)) {
                    return;
                }
            } else {
                ClaimedResidence res = ClaimedResidence.getByLoc(block.getLocation());
                // skip check if shooter is a dispenser in the same Residence
                if (Utils.isSourceBlockInsideSameResidence(entity, res)) {
                    return;
                }
                perms = (res != null)
                        ? res.getPermissions()
                        : plugin.getWorldFlags().getPerms(block.getWorld());
                if (perms.has(flag, perms.has(Flags.use, true))) {
                    return;
                }
            }
            break;
        default:
            return;
        }
        event.setCancelled(true);
    }

    private static boolean damageableProjectile(Entity ent) {
        return ent instanceof Projectile;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void AnimalKilling(EntityDamageEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, entity)) {
            return;
        }
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

        if (FlagPermissions.shouldDenyAndNotify(cause, entity, Flags.animalkilling, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void AnimalKillingByFlame(EntityCombustByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, entity)) {
            return;
        }
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void AnimalDamageByMobs(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, entity)) {
            return;
        }
        if (!Utils.isAnimal(entity))
            return;

        Entity damager = event.getDamager();

        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player || damager instanceof Player)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());
        if (!perms.has(Flags.animalkilling, true)) {
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

        if (plugin.isDisabledWorldListener(ent))
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

    // Fix player being able to knock back vehicle before Destroy
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageVehicle(VehicleDamageEvent event) {

        Vehicle vehicle = event.getVehicle();

        if (FlagPermissions.shouldIgnoreCheck(Flags.vehicledestroy, vehicle)) {
            return;
        }
        Entity attacker = event.getAttacker();

        if (!(attacker instanceof Player)) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify((Player) attacker, vehicle, Flags.vehicledestroy, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {

        Vehicle vehicle = event.getVehicle();

        if (FlagPermissions.shouldIgnoreCheck(Flags.vehicledestroy, vehicle)) {
            return;
        }
        Entity attacker = event.getAttacker();
        // Entity destroys vehicle
        if (attacker != null) {
            if (shouldDenyEntityDestroyVehicle(attacker, vehicle)) {
                event.setCancelled(true);
            }
            // Even if BlockExplodeEvent is canceled, vehicles can still be destroyed
            // Fix bed and respawn anchor explosions destroying vehicles
        } else if (Version.isCurrentEqualOrHigher(Version.v26_1_2) && Version.isPaperBranch()
                && event.getDamageSource().getDamageType() == org.bukkit.damage.DamageType.BAD_RESPAWN_POINT) {
            if (FlagPermissions.has(vehicle.getLocation(), Flags.vehicledestroy, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onNonLivingVehicleCombust(EntityCombustByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.vehicledestroy, entity)) {
            return;
        }
        if (entity instanceof LivingEntity) {
            return;
        }
        if (!(entity instanceof Vehicle)) {
            return;
        }
        if (shouldDenyEntityDestroyVehicle(event.getCombuster(), (Vehicle) entity)) {
            event.setCancelled(true);
        }
    }

    private boolean shouldDenyEntityDestroyVehicle(Entity attacker, Vehicle vehicle) {

        Player player = Utils.potentialProjectileToPlayer(attacker);

        if (player != null) {
            return FlagPermissions.shouldDenyAndNotify(player, vehicle, Flags.vehicledestroy, null);
        } else {
            return FlagPermissions.has(vehicle.getLocation(), Flags.vehicledestroy, FlagCombo.OnlyFalse);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void MonsterKilling(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.mobkilling, entity)) {
            return;
        }
        if (!Utils.isMonster(entity)) {
            return;
        }
        Entity damager = event.getDamager();

        if (!damageableProjectile(damager) && !(damager instanceof Player))
            return;

        if (damageableProjectile(damager) && !(((Projectile) damager).getShooter() instanceof Player))
            return;

        Player cause = Utils.potentialProjectileToPlayer(damager);

        if (cause == null)
            return;

        if (FlagPermissions.shouldDenyAndNotify(cause, entity, Flags.mobkilling, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PlayerLeashEntityEvent(PlayerLeashEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.leash, entity)) {
            return;
        }
        Player player = event.getPlayer();

        if (FlagPermissions.shouldDenyAndNotify(player, entity, Flags.leash, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPhantomOrWitherSpawn(CreatureSpawnEvent event) {

        Entity entity = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity)) {
            return;
        }
        Flags flag;
        if (Flags.witherspawn.isGlobalyEnabled() && entity instanceof Wither) {
            flag = Flags.witherspawn;

        } else if (Flags.phantomspawn.isGlobalyEnabled() && Utils.isPhantom(entity)) {
            flag = Flags.phantomspawn;

        } else {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(event.getLocation());
        if (perms.has(flag, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // disabling event on world
        Entity ent = event.getEntity();

        if (plugin.isDisabledWorldListener(ent))
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
        } else if (Utils.isMonster(ent)) {
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

        Player player = event.getPlayer();
        if (player == null)
            return;

        if (FlagPermissions.shouldIgnoreCheck(Flags.place, player)) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify(player, event.getEntity(), Flags.place, Flags.build)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(projectile)) {
            return;
        }
        Flags flag = Flags.shoot;
        CMIEntityType type = CMIEntityType.get(event.getEntityType());
        if (type != null) {
            switch (type) {
            case EXPERIENCE_BOTTLE:
            case FIREWORK_ROCKET:
                return;
            case ENDER_PEARL:
                flag = Flags.enderpearl;
                break;
            case WIND_CHARGE:
                flag = Flags.windexplode;
                break;
            case SPLASH_POTION:
            case LINGERING_POTION:
                flag = Flags.potionthrowing;
                break;
            default:
                break;
            }
        }
        // Disabling listener if flag disabled globally
        if (!flag.isGlobalyEnabled()) {
            return;
        }
        ProjectileSource shooter = projectile.getShooter();
        if (shooter instanceof Player) {

            Player player = (Player) shooter;
            if (FlagPermissions.shouldDenyAndNotify(player, projectile, flag, null)) {
                event.setCancelled(true);
            }

        } else {
            // Flags.potionthrowing only controls player-thrown potions
            if (flag == Flags.potionthrowing) {
                flag = Flags.shoot;
            }
            FlagPermissions perms = FlagPermissions.getPerms(projectile.getLocation());
            if (perms.has(flag, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakEventByExplosion(HangingBreakEvent event) {

        Hanging ent = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.explode, ent)) {
            return;
        }
        if (!event.getCause().equals(RemoveCause.EXPLOSION))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakEvent(HangingBreakEvent event) {

        Hanging ent = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.destroy, ent)) {
            return;
        }
        // ItemFrame covers item_frame/glow_item_frame
        if (!(ent instanceof ItemFrame)) {
            return;
        }
        if (!event.getCause().equals(RemoveCause.PHYSICS))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (!perms.has(Flags.destroy, perms.has(Flags.build, true))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.destroy, entity)) {
            return;
        }
        if (shouldDenyEntityHit(event.getRemover(), entity.getLocation(), Flags.destroy, Flags.build)) {
            event.setCancelled(true);
        }
    }

    public static boolean shouldDenyEntityHit(@NotNull Entity source, @NotNull Location targetLoc, @NotNull Flags mainFlag, @Nullable Flags subFlag) {

        Player player = Utils.potentialProjectileToPlayer(source);
        if (player != null) {

            return FlagPermissions.shouldDenyAndNotify(player, targetLoc, mainFlag, subFlag);

        } else {
            ClaimedResidence res = ClaimedResidence.getByLoc(targetLoc);
            // skip check if shooter is a dispenser in the same Residence
            if (Utils.isSourceBlockInsideSameResidence(source, res)) {
                return false;
            }
            FlagPermissions perms = (res != null)
                    ? res.getPermissions()
                    : Residence.getInstance().getWorldFlags().getPerms(targetLoc.getWorld());

            boolean result = (subFlag == null) || perms.has(subFlag, true);

            return !perms.has(mainFlag, result);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {

        Entity ent = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.burn, ent)) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
        if (!perms.has(Flags.burn, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {

        Entity entity = event.getEntity();

        if (plugin.isDisabledWorldListener(entity)) {
            return;
        }
        CMIEntityType type = CMIEntityType.get(event.getEntityType());
        FlagPermissions perms;
        boolean shouldDeny = false;
        boolean shouldRemove = false;

        if (type == null) {
            // Unknown entity type fallback
            if (!Flags.explode.isGlobalyEnabled()) {
                return;
            }
            perms = FlagPermissions.getPerms(entity.getLocation());
            if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
                shouldDeny = true;
            }
        } else {
            switch (type) {
            case BREEZE_WIND_CHARGE:
            case WIND_CHARGE:
                // Wind Charge explosion are handled by EntityExplodeEvent
                return;
            case CREEPER:
                if (!Flags.creeper.isGlobalyEnabled()) {
                    return;
                }
                perms = FlagPermissions.getPerms(entity.getLocation());
                if (!perms.has(Flags.creeper, perms.has(Flags.explode, true))) {
                    shouldDeny = !plugin.getConfigManager().isCreeperExplodeBelow()
                            || entity.getLocation().getBlockY() >= plugin.getConfigManager().getCreeperExplodeBelowLevel()
                            || ClaimedResidence.getByLoc(entity.getLocation()) != null;
                    shouldRemove = shouldDeny;
                }
                break;
            case TNT:
            case TNT_MINECART:
                if (!Flags.tnt.isGlobalyEnabled()) {
                    return;
                }
                perms = FlagPermissions.getPerms(entity.getLocation());
                if (!perms.has(Flags.tnt, perms.has(Flags.explode, true))) {
                    shouldDeny = !plugin.getConfigManager().isTNTExplodeBelow()
                            || entity.getLocation().getBlockY() >= plugin.getConfigManager().getTNTExplodeBelowLevel()
                            || ClaimedResidence.getByLoc(entity.getLocation()) != null;
                    shouldRemove = shouldDeny;
                }
                break;
            case FIREBALL:
            case SMALL_FIREBALL:
                if (!Flags.explode.isGlobalyEnabled() && !Flags.fireball.isGlobalyEnabled()) {
                    return;
                }
                perms = FlagPermissions.getPerms(entity.getLocation());
                if ((Flags.explode.isGlobalyEnabled() && !perms.has(Flags.explode, true))
                        || (Flags.fireball.isGlobalyEnabled() && !perms.has(Flags.fireball, true))) {
                    shouldDeny = true;
                    shouldRemove = true;
                }
                break;
            case WITHER:
            case WITHER_SKULL:
                if (!Flags.explode.isGlobalyEnabled()) {
                    return;
                }
                perms = FlagPermissions.getPerms(entity.getLocation());
                if (!perms.has(Flags.explode, perms.has(Flags.witherdestruction, true))) {
                    shouldDeny = true;
                    shouldRemove = entity instanceof WitherSkull;
                }
                break;
            case ENDER_CRYSTAL:
                if (!Flags.explode.isGlobalyEnabled()) {
                    return;
                }
                perms = FlagPermissions.getPerms(entity.getLocation());
                if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
                    shouldDeny = true;
                    shouldRemove = true;
                }
                break;
            default:
                // Other entity types
                if (!Flags.explode.isGlobalyEnabled()) {
                    return;
                }
                perms = FlagPermissions.getPerms(entity.getLocation());
                if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
                    shouldDeny = true;
                }
                break;
            }
        }
        if(shouldDeny) {
            event.setCancelled(true);
            // fix Creeper/End_Crystal not disappearing when ExplosionPrimeEvent is canceled
            // projectiles disappear on their own, so this may not be needed
            if (shouldRemove) {
                entity.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity())) {
            return;
        }
        // ExplosionResult.TRIGGER_BLOCK does not destroy blocks
        // it is triggered by (WindCharge and Wind Charged Effect)
        if (Version.isCurrentEqualOrHigher(Version.v1_21_0)
                && event.getExplosionResult() == org.bukkit.ExplosionResult.TRIGGER_BLOCK) {
            ResidenceListener1_21.onWindExplode(event);
            return;
        }
        FlagPermissions perms;
        Location loc = event.getLocation();
        boolean shouldDeny = false;

        CMIEntityType type = CMIEntityType.get(event.getEntityType());
        // Explosion is prohibited at the source location; cancel the event directly
        if (type == null) {
            // Unknown entity type fallback
            if (Flags.explode.isGlobalyEnabled()) {
                perms = FlagPermissions.getPerms(loc);
                if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
                    shouldDeny = true;
                }
            }
        } else {
            switch (type) {
            case CREEPER:
                if (!Flags.creeper.isGlobalyEnabled()) {
                    break;
                }
                perms = FlagPermissions.getPerms(loc);
                if (!perms.has(Flags.creeper, perms.has(Flags.explode, true))) {
                    shouldDeny = !plugin.getConfigManager().isCreeperExplodeBelow()
                            || loc.getBlockY() >= plugin.getConfigManager().getCreeperExplodeBelowLevel()
                            || ClaimedResidence.getByLoc(loc) != null;
                }
                break;
            case TNT:
            case TNT_MINECART:
                if (!Flags.tnt.isGlobalyEnabled()) {
                    break;
                }
                perms = FlagPermissions.getPerms(loc);
                if (!perms.has(Flags.tnt, perms.has(Flags.explode, true))) {
                    shouldDeny = !plugin.getConfigManager().isTNTExplodeBelow()
                            || loc.getBlockY() >= plugin.getConfigManager().getTNTExplodeBelowLevel()
                            || ClaimedResidence.getByLoc(loc) != null;
                }
                break;
            case FIREBALL:
            case SMALL_FIREBALL:
                if (!Flags.explode.isGlobalyEnabled() && !Flags.fireball.isGlobalyEnabled()) {
                    break;
                }
                perms = FlagPermissions.getPerms(loc);
                if ((Flags.explode.isGlobalyEnabled() && !perms.has(Flags.explode, true))
                        || (Flags.fireball.isGlobalyEnabled() && !perms.has(Flags.fireball, true))) {
                    shouldDeny = true;
                }
                break;
            case WITHER:
            case WITHER_SKULL:
                if (!Flags.explode.isGlobalyEnabled()) {
                    break;
                }
                perms = FlagPermissions.getPerms(loc);
                if (!perms.has(Flags.explode, perms.has(Flags.witherdestruction, true))) {
                    shouldDeny = true;
                }
                break;
            case ENDER_DRAGON:
                if (!Flags.dragongrief.isGlobalyEnabled()) {
                    break;
                }
                perms = FlagPermissions.getPerms(loc);
                if (!perms.has(Flags.dragongrief, true)) {
                    shouldDeny = true;
                }
                break;
            default:
                // Other entity types
                if (!Flags.explode.isGlobalyEnabled()) {
                    break;
                }
                perms = FlagPermissions.getPerms(loc);
                if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
                    shouldDeny = true;
                }
                break;
            }
        }
        if (shouldDeny) {
            event.setCancelled(true);
            return;
        }
        // Source allows explosion, so check each affected block for destruction
        handleEntityExplodeBreakBlock(event.blockList(), type);
    }

    private void handleEntityExplodeBreakBlock(List<Block> blocks, CMIEntityType type) {
        // Unknown entity type fallback
        if (type == null) {
            if (!Flags.destroy.isGlobalyEnabled() && !Flags.explode.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
                return (Flags.destroy.isGlobalyEnabled() && !perms.has(Flags.destroy, true))
                        || (Flags.explode.isGlobalyEnabled() && !perms.has(Flags.explode, true));
            });
            return;
        }
        switch (type) {
        case CREEPER:
            if (!Flags.creeper.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                Location blockLoc = block.getLocation();
                FlagPermissions perms = FlagPermissions.getPerms(blockLoc);
                return !perms.has(Flags.creeper, perms.has(Flags.explode, true))
                        && (!plugin.getConfigManager().isCreeperExplodeBelow()
                        || block.getY() >= plugin.getConfigManager().getCreeperExplodeBelowLevel()
                        || ClaimedResidence.getByLoc(blockLoc) != null);
            });
            return;
        case TNT:
        case TNT_MINECART:
            if (!Flags.tnt.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                Location blockLoc = block.getLocation();
                FlagPermissions perms = FlagPermissions.getPerms(blockLoc);
                return !perms.has(Flags.tnt, perms.has(Flags.explode, true))
                        && (!plugin.getConfigManager().isTNTExplodeBelow()
                        || block.getY() >= plugin.getConfigManager().getTNTExplodeBelowLevel()
                        || ClaimedResidence.getByLoc(blockLoc) != null);
            });
            return;
        case FIREBALL:
        case SMALL_FIREBALL:
            if (!Flags.explode.isGlobalyEnabled() && !Flags.fireball.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
                return (Flags.explode.isGlobalyEnabled() && !perms.has(Flags.explode, true))
                        || (Flags.fireball.isGlobalyEnabled() && !perms.has(Flags.fireball, true));
            });
            return;
        case WITHER:
        case WITHER_SKULL:
            if (!Flags.witherdestruction.isGlobalyEnabled() && !Flags.explode.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
                return (Flags.witherdestruction.isGlobalyEnabled() && !perms.has(Flags.witherdestruction, perms.has(Flags.destroy, true)))
                        || (Flags.explode.isGlobalyEnabled() && !perms.has(Flags.explode, true));
            });
            return;
        case ENDER_DRAGON:
            if (!Flags.dragongrief.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
                return !perms.has(Flags.dragongrief, true);
            });
            return;
        default:
            // Other entity types
            if (!Flags.destroy.isGlobalyEnabled() && !Flags.explode.isGlobalyEnabled()) {
                return;
            }
            blocks.removeIf(block -> {
                FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
                return (Flags.destroy.isGlobalyEnabled() && !perms.has(Flags.destroy, true))
                        || (Flags.explode.isGlobalyEnabled() && !perms.has(Flags.explode, true));
            });
            return;
        }
    }

    // Various zombies break the door
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityBreakDoor(EntityBreakDoorEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.mobgriefing, block)) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        if (perms.has(Flags.mobgriefing, perms.has(Flags.destroy, true))) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {

        ThrownPotion potion = event.getPotion();

        if (plugin.isDisabledWorldListener(potion)) {
            return;
        }
        ProjectileSource attacker = potion.getShooter();
        boolean isPlayerAttacker = attacker instanceof Player;
        ClaimedResidence attackerRes = null;

        if (isPlayerAttacker) {
            if (!Flags.potionthrowing.isGlobalyEnabled()) {
                return;
            }
            Player player = (Player) attacker;
            if (player.hasMetadata("NPC")) {
                return;
            }
            attackerRes = ClaimedResidence.getByLoc(player.getLocation());
            FlagPermissions attackerPerms = (attackerRes != null)
                    ? attackerRes.getPermissions()
                    : Residence.getInstance().getWorldFlags().getPerms(player.getWorld());
            if (attackerPerms.playerHas(player, Flags.potionthrowing, FlagCombo.OnlyFalse) && !ResAdmin.isResAdmin(player)) {
                lm.Flag_Deny.sendMessage(player, Flags.potionthrowing);
                event.setCancelled(true);
                return;
            }
            // Now handling Potion thrown by dispensers
        } else if (attacker instanceof BlockProjectileSource) {
            ClaimedResidence potionHitRes = ClaimedResidence.getByLoc(potion.getLocation());
            if (potionHitRes == null) {
                return;
            }
            Location shooterLoc = ((BlockProjectileSource) attacker).getBlock().getLocation();
            ClaimedResidence shooterRes = ClaimedResidence.getByLoc(shooterLoc);
            // Non-player shooter and hit location in same Residence or same owner; skip check
            if (potionHitRes == shooterRes || (shooterRes != null && shooterRes.isOwner(potionHitRes.getOwner()))) {
                return;
            }
            // Prevent Potion from being spawned into a Residence from outside
            if (potionHitRes.getPermissions().has(Flags.build, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }
            return;
        }
        // Potion allows Splash; now handling potion effects on entities
        boolean isHealingEffect = false;
        boolean isDamageEffect = false;
        boolean isHarmfulEffect = false;

        ItemMeta itemMeta = potion.getItem().getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            return;
        }
        // Start - Get the potion effect type
        for (PotionEffect effect : potion.getEffects()) {
            PotionEffectType type = effect.getType();
            if (PotionUtils.isHealingEffect(type)) {
                isHealingEffect = true;
                break;
            } else if (PotionUtils.isDamageEffect(type)) {
                isDamageEffect = true;
                break;
            } else if (PotionUtils.isHarmfulEffect(type)) {
                isHarmfulEffect = true;
                break;
            }
        }
        // End - Get the potion effect type
        if (isHealingEffect && Flags.mobkilling.isGlobalyEnabled()) {
            for (LivingEntity victim : event.getAffectedEntities()) {
                if (PotionUtils.shouldDenyHealingEffect(victim, attacker, isPlayerAttacker)) {
                    event.setIntensity(victim, 0);
                }
            }

        } else if (isDamageEffect) {
            for (LivingEntity victim : event.getAffectedEntities()) {
                if (PotionUtils.shouldDenyDamageEffect(victim, attacker, attackerRes, isPlayerAttacker)) {
                    event.setIntensity(victim, 0);
                }
            }

        } else if (isHarmfulEffect) {
            for (LivingEntity victim : event.getAffectedEntities()) {
                if (PotionUtils.shouldDenyHarmfulEffect(victim, attacker, attackerRes, isPlayerAttacker)) {
                    event.setIntensity(victim, 0);
                }
            }

        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PlayerKillingByFlame(EntityCombustByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.pvp, entity)) {
            return;
        }
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        // issues https://github.com/Zrips/Residence/issues/466
        // Not sure when Paper stopped needing this, so to be safe, we'll only skip it for Paper 1.21+
        // https://github.com/PaperMC/Paper/pull/10307
        if (Version.isCurrentEqualOrHigher(Version.v1_21_0) && Version.isPaperBranch()) {
            return;
        }
        if (Version.isCurrentEqualOrLower(Version.v1_14_0))
            return;

        if (event.getBow() == null)
            return;

        if (event.getBow().getType() != Material.CROSSBOW)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (event.getProjectile() instanceof Firework) {
            event.getProjectile().setMetadata(CrossbowShooter, new FixedMetadataValue(plugin, event.getEntity().getUniqueId()));
        }
    }

    private static void process(lm lm, Player attacker, boolean isOnFire, Entity victim, EntityDamageEvent event) {
        if (attacker != null)
            lm.sendMessage(attacker);
        if (isOnFire)
            victim.setFireTicks(0);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(victim)) {
            return;
        }
        if (!(victim instanceof Player) || victim.hasMetadata("NPC")) {
            return;
        }
        Entity attacker = event.getDamager();
        Player attackerPlayer = null;
        boolean isOnFire = false;

        if (attacker instanceof Player) {
            attackerPlayer = (Player) attacker;

            // issues https://github.com/Zrips/Residence/issues/466
            // Not sure when Paper stopped needing this, so to be safe, we'll only skip it for Paper 1.21+
            // https://github.com/PaperMC/Paper/pull/10307
            // In higher versions, Firework also belongs to Projectile, and the shooter can be obtained normall
        } else if (attacker instanceof Firework && (!Version.isPaperBranch() || Version.isCurrentLower(Version.v1_21_0))) {
            List<MetadataValue> meta = attacker.getMetadata(CrossbowShooter);
            if (meta != null && !meta.isEmpty()) {
                try {
                    String uid = meta.get(0).asString();
                    attackerPlayer = Bukkit.getPlayer(UUID.fromString(uid));
                } catch (Throwable e) {
                }
            }

        } else if (attacker instanceof Projectile) {
            Projectile project = (Projectile) attacker;
            ProjectileSource shooter = project.getShooter();
            if (!(shooter instanceof Player)) {
                return;
            }
            attackerPlayer = (Player) shooter;
            if (project.getFireTicks() > 0) {
                isOnFire = true;
            }

        }
        if (attackerPlayer == null || attackerPlayer.hasMetadata("NPC")) {
            return;
        }
        // Skip the PvP check when players attack themselves
        // e.g. Players fire an arrow from above and deliberately step into it to hit themselves
        if (attackerPlayer == victim) {
            return;
        }
        // Now both the attacker and the victim are guaranteed to be players
        ClaimedResidence attackerRes = ClaimedResidence.getByLoc(attackerPlayer.getLocation());
        ClaimedResidence victimRes = ClaimedResidence.getByLoc(victim.getLocation());
        // Attacker and victim are in the same Residence
        if (attackerRes != null && attackerRes == victimRes) {

            if (ConfigManager.RaidEnabled && victimRes.getRaid().isUnderRaid()) {
                boolean raidSameTeam = victimRes.getRaid().onSameTeam(attackerPlayer, (Player) victim);
                if (raidSameTeam && !ConfigManager.RaidFriendlyFire) {
                    event.setCancelled(true);
                    return;
                }
                if (!raidSameTeam) {
                    return;
                }
            }
            FlagPermissions attackerPerms = attackerRes.getPermissions();

            if (attackerPerms.has(Flags.pvp, FlagCombo.OnlyFalse)) {
                process(lm.General_NoPVPZone, attackerPlayer, isOnFire, victim, event);
                return;
            }
            if (attackerPerms.playerHas((Player) victim, Flags.friendlyfire, FlagCombo.OnlyFalse)
                    && attackerPerms.playerHas(attackerPlayer, Flags.friendlyfire, FlagCombo.OnlyFalse)) {
                CMIActionBar.send(attackerPlayer, plugin.getLM().getMessage(lm.General_NoFriendlyFire));
                event.setCancelled(true);
                if (isOnFire) {
                    victim.setFireTicks(0);
                }
            }
            // Attacker and victim are not in the same Residence
        } else {
            // if PVP disabled at attacker location, cancel event
            if (attackerRes != null && attackerRes.getPermissions().has(Flags.pvp, FlagCombo.OnlyFalse)) {
                process(lm.General_NoPVPZone, attackerPlayer, isOnFire, victim, event);
                return;
            }
            // if PVP disabled at victim location, cancel event
            if (victimRes != null && victimRes.getPermissions().has(Flags.pvp, FlagCombo.OnlyFalse)) {
                process(lm.General_NoPVPZone, attackerPlayer, isOnFire, victim, event);
                return;
            }
            // Now attacker and victim are not in Residence
            if (attackerRes == null && victimRes == null) {
                /* World PvP */
                if (plugin.getWorldFlags().getPerms(attackerPlayer.getWorld()).has(Flags.pvp, FlagCombo.OnlyFalse)) {
                    process(lm.General_WorldPVPDisabled, attackerPlayer, isOnFire, victim, event);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(victim)) {
            return;
        }
        Entity attacker = event.getDamager();
        // Check held item blacklist
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            if (Utils.isItemBlacklisted(CMIItemStack.getItemInMainHand(player), player)) {
                event.setCancelled(true);
                return;
            }
        }
        // Decorative entity damage uses separate logic
        if (victim instanceof EnderCrystal || victim instanceof ItemFrame || Utils.isArmorStand(victim)) {
            handleDecorativeEntityDamage(event);
            return;
        }
        Flags mainFlag = null;
        Flags subFlag = null;

        if (Flags.creeper.isGlobalyEnabled() && attacker instanceof Creeper) {
            mainFlag = Flags.creeper;
            subFlag = Flags.explode;

        } else if (Flags.explode.isGlobalyEnabled() && attacker instanceof EnderCrystal) {
            mainFlag = Flags.explode;
            subFlag = Flags.destroy;

        } else if (Flags.fireball.isGlobalyEnabled() && (event.getEntityType() == EntityType.FIREBALL || event.getEntityType() == EntityType.SMALL_FIREBALL)) {
            mainFlag = Flags.fireball;

        } else if (Flags.snowball.isGlobalyEnabled() && attacker instanceof Snowball) {
            mainFlag = Flags.snowball;

        } else if (Flags.tnt.isGlobalyEnabled() && (attacker instanceof TNTPrimed || attacker instanceof ExplosiveMinecart)) {
            mainFlag = Flags.tnt;
            subFlag = Flags.explode;

        } else if (Flags.witherdestruction.isGlobalyEnabled() && (attacker instanceof Wither || attacker instanceof WitherSkull)) {
            mainFlag = Flags.witherdamage;

        }
        if (mainFlag == null) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(victim.getLocation());
        boolean result = (subFlag == null || perms.has(subFlag, true));
        if (perms.has(mainFlag, result)) {
            return;
        }
        event.setCancelled(true);
    }

    private void handleDecorativeEntityDamage(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();
        Player player = Utils.potentialProjectileToPlayer(attacker);
        // if player damages ItemFrame with items inside, check Flags.container
        // this corresponds to taking the item out of the ItemFrame
        if (Flags.container.isGlobalyEnabled() && player != null && victim instanceof ItemFrame
                && ((ItemFrame) victim).getItem() != null && ((ItemFrame) victim).getItem().getType() != Material.AIR) {
            if (ResPerm.bypass_container.hasPermission(player, 10000L)) {
                return;
            }
            if (FlagPermissions.shouldDenyAndNotify(player, victim, Flags.container, Flags.use)) {
                event.setCancelled(true);
            }
            // damage from player or player-fired projectile
        } else if (Flags.destroy.isGlobalyEnabled() && player != null) {
            if (FlagPermissions.shouldDenyAndNotify(player, victim, Flags.destroy, Flags.build)) {
                event.setCancelled(true);
            }
            // damage from non-players or projectiles fired by non-players
        } else if (Flags.destroy.isGlobalyEnabled()) {
            ClaimedResidence res = null;
            if (attacker instanceof Projectile) {
                res = ClaimedResidence.getByLoc(victim.getLocation());
                // skip check if shooter is a dispenser in the same Residence
                if (Utils.isSourceBlockInsideSameResidence(attacker, res)) {
                    return;
                }
            }
            FlagPermissions perms = (res != null)
                    ? res.getPermissions()
                    : plugin.getWorldFlags().getPerms(victim.getWorld());

            if (!perms.has(Flags.destroy, perms.has(Flags.build, true))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {

        Entity entity = event.getEntity();

        if (plugin.isDisabledWorldListener(entity)) {
            return;
        }
        DamageCause cause = event.getCause();
        if (Flags.damage.isGlobalyEnabled() && cause != DamageCause.VOID
                && (entity instanceof Player || Utils.isTamed(entity))
                && FlagPermissions.has(entity.getLocation(), Flags.damage, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            entity.setFireTicks(0);

        } else if (Flags.falldamage.isGlobalyEnabled() && cause == DamageCause.FALL && entity instanceof Player) {
            if (FlagPermissions.has(entity.getLocation(), Flags.falldamage, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }
        } else if (Flags.pvp.isGlobalyEnabled() && cause == DamageCause.LIGHTNING && entity instanceof Player) {
            if (FlagPermissions.has(entity.getLocation(), Flags.pvp, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }
        } else if (Flags.destroy.isGlobalyEnabled()
                && (cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION || cause == DamageCause.FIRE_TICK)
                && (entity instanceof Arrow || Utils.isArmorStand(entity))) {
            if (FlagPermissions.has(entity.getLocation(), Flags.destroy, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                entity.setFireTicks(0);
            }
            // Even if BlockExplodeEvent is canceled, damage will still be dealt
            // Fix bed and respawn anchor explosions damaging entities
        } else if (cause == DamageCause.BLOCK_EXPLOSION) {
            if (Flags.pvp.isGlobalyEnabled() && entity instanceof Player) {
                if (FlagPermissions.has(entity.getLocation(), Flags.pvp, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                }
            } else if (Flags.animalkilling.isGlobalyEnabled() && Utils.isAnimal(entity)) {
                if (FlagPermissions.has(entity.getLocation(), Flags.animalkilling, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                }
            } else if (Flags.mobkilling.isGlobalyEnabled() && Utils.isMonster(entity)) {
                if (FlagPermissions.has(entity.getLocation(), Flags.mobkilling, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                }
            } else if (Flags.explode.isGlobalyEnabled()) {
                FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());
                if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
