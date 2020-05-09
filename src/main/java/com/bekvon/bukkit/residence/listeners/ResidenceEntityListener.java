package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;

import com.bekvon.bukkit.cmiLib.ActionBarTitleMessages;
import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Debug;

public class ResidenceEntityListener implements Listener {

    Residence plugin;

    public ResidenceEntityListener(Residence plugin) {
	this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanChangeBlock(EntityChangeBlockEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (event.getEntityType() != EntityType.ENDERMAN)
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.destroy, true)) {
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
	FlagPermissions perms = plugin.getPermsByLoc(block.getLocation());
	boolean hastrample = perms.has(Flags.trample, perms.has(Flags.build, true));
	if (!hastrample && !(entity.getType() == EntityType.FALLING_BLOCK) && (mat.equals(CMIMaterial.FARMLAND) || mat.equals(CMIMaterial.SOUL_SAND))) {
	    event.setCancelled(true);
	}
    }

    public static boolean isMonster(Entity ent) {
	return (ent instanceof Monster || ent instanceof Slime || ent instanceof Ghast);
    }

    private static boolean isTamed(Entity ent) {
	return (ent instanceof Tameable ? ((Tameable) ent).isTamed() : false);
    }

    private static boolean damageableProjectile(Entity ent) {
	if (ent instanceof Projectile && ent.getType().toString().equalsIgnoreCase("Splash_potion")) {
	    for (PotionEffect one : ((ThrownPotion) ent).getEffects()) {
		for (String oneHarm : Residence.getInstance().getConfigManager().getNegativePotionEffects()) {
		    if (oneHarm.equalsIgnoreCase(one.getType().getName()))
			return true;
		}
	    }
	}
	return ent instanceof Arrow || ent instanceof Projectile && (ent.getType().toString().equalsIgnoreCase("Trident"));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(EntityDamageByEntityEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.animalkilling.isGlobalyEnabled())
	    return;
	// disabling event on world
	Entity entity = event.getEntity();
	if (entity == null)
	    return;
	if (plugin.isDisabledWorldListener(entity.getWorld()))
	    return;
	if (!plugin.getNms().isAnimal(entity))
	    return;

	Entity damager = event.getDamager();

	if (!damageableProjectile(damager) && !(damager instanceof Player))
	    return;

	if (damageableProjectile(damager) && !(((Projectile) damager).getShooter() instanceof Player))
	    return;

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (plugin.isResAdminOn(cause))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (res.getPermissions().playerHas(cause, Flags.animalkilling, FlagCombo.OnlyFalse)) {
	    plugin.msg(cause, lm.Residence_FlagDeny, Flags.animalkilling, res.getName());
	    event.setCancelled(true);
	}
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
	if (!plugin.getNms().isAnimal(entity))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	Entity damager = event.getCombuster();

	if (!damageableProjectile(damager) && !(damager instanceof Player))
	    return;

	if (damageableProjectile(damager) && !(((Projectile) damager).getShooter() instanceof Player))
	    return;

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (plugin.isResAdminOn(cause))
	    return;

	if (res.getPermissions().playerHas(cause, Flags.animalkilling, FlagCombo.OnlyFalse)) {
	    plugin.msg(cause, lm.Residence_FlagDeny, Flags.animalkilling, res.getName());
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
	if (!plugin.getNms().isAnimal(entity))
	    return;

	Entity damager = event.getDamager();

	if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player || damager instanceof Player)
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(entity.getLocation());
	FlagPermissions world = plugin.getWorldFlags().getPerms(entity.getWorld().getName());
	if (!perms.has(Flags.animalkilling, world.has(Flags.animalkilling, true))) {
	    event.setCancelled(true);
	    return;
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
	FlagPermissions perms = plugin.getPermsByLoc(loc);
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

	if (vehicle == null)
	    return;

	if (damager instanceof Projectile && !(((Projectile) damager).getShooter() instanceof Player) || !(damager instanceof Player)) {
	    FlagPermissions perms = plugin.getPermsByLoc(vehicle.getLocation());
	    if (!perms.has(Flags.vehicledestroy, true)) {
		event.setCancelled(true);
		return;
	    }
	}

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (plugin.isResAdminOn(cause))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(vehicle.getLocation());

	if (res == null)
	    return;

	if (res.getPermissions().playerHas(cause, Flags.vehicledestroy, FlagCombo.OnlyFalse)) {
	    plugin.msg(cause, lm.Residence_FlagDeny, Flags.vehicledestroy, res.getName());
	    event.setCancelled(true);
	}
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

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	if (plugin.isResAdminOn(cause))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (res.getPermissions().playerHas(cause, Flags.mobkilling, FlagCombo.OnlyFalse)) {
	    plugin.msg(cause, lm.Residence_FlagDeny, Flags.mobkilling, res.getName());
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalLeash(PlayerLeashEntityEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.leash.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
	    return;
	Player player = event.getPlayer();

	Entity entity = event.getEntity();

	if (!plugin.getNms().isAnimal(entity))
	    return;

	if (plugin.isResAdminOn(player))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

	if (res == null)
	    return;

	if (res.getPermissions().playerHas(player, Flags.leash, FlagCombo.OnlyFalse)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.leash, res.getName());
	    event.setCancelled(true);
	}
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

	FlagPermissions perms = plugin.getPermsByLoc(event.getLocation());
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

	FlagPermissions perms = plugin.getPermsByLoc(event.getLocation());
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
	FlagPermissions perms = plugin.getPermsByLoc(event.getLocation());
	if (plugin.getNms().isAnimal(ent)) {
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
	if (plugin.isResAdminOn(player))
	    return;

	FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getEntity().getLocation(), player);
	if (!perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true))) {
	    event.setCancelled(true);
	    plugin.msg(player, lm.Flag_Deny, Flags.place);
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

	if (event.getEntityType().equals(EntityType.THROWN_EXP_BOTTLE))
	    return;

	if (event.getEntity().getShooter() instanceof Player) {
	    if (plugin.isResAdminOn((Player) event.getEntity().getShooter()))
		return;
	}
	FlagPermissions perms = plugin.getPermsByLoc(event.getEntity().getLocation());
	if (perms.has(Flags.shoot, FlagCombo.OnlyFalse)) {
	    event.setCancelled(true);
	    if (event.getEntity().getShooter() instanceof Player)
		plugin.msg((Player) event.getEntity().getShooter(), lm.Flag_Deny, Flags.shoot);
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
	if (plugin.isResAdminOn(player))
	    return;

	if (plugin.getResidenceManager().isOwnerOfLocation(player, ent.getLocation()))
	    return;

	FlagPermissions perms = plugin.getPermsByLocForPlayer(ent.getLocation(), player);
	if (!perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true))) {
	    event.setCancelled(true);
	    plugin.msg(player, lm.Flag_Deny, Flags.destroy);
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

	FlagPermissions perms = plugin.getPermsByLoc(ent.getLocation());
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

	if (!event.getEntity().getType().equals(EntityType.ITEM_FRAME))
	    return;

	if (!event.getCause().equals(RemoveCause.PHYSICS))
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(ent.getLocation());
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

	FlagPermissions perms = plugin.getPermsByLoc(ent.getLocation());
	if (!perms.has(Flags.destroy, perms.has(Flags.build, true))) {
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
	FlagPermissions perms = plugin.getPermsByLoc(ent.getLocation());
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
	FlagPermissions perms = plugin.getPermsByLoc(ent.getLocation());

	switch (entity) {
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
	case PRIMED_TNT:
	case MINECART_TNT:

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
	FlagPermissions perms = plugin.getPermsByLoc(loc);
	FlagPermissions world = plugin.getWorldFlags().getPerms(loc.getWorld().getName());

	if (ent != null) {
	    switch (event.getEntityType()) {
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
	    case PRIMED_TNT:
	    case MINECART_TNT:
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
	    FlagPermissions blockperms = plugin.getPermsByLoc(block.getLocation());

	    if (ent != null) {
		switch (event.getEntityType()) {
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
		case PRIMED_TNT:
		case MINECART_TNT:
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
		    if (blockperms.has(Flags.destroy, FlagCombo.OnlyFalse))
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

	if (!plugin.getPermsByLoc(ent.getLocation()).has(Flags.witherdestruction, FlagCombo.OnlyFalse))
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
	boolean srcpvp = plugin.getPermsByLoc(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
	Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
	while (it.hasNext()) {
	    LivingEntity target = it.next();
	    if (target.getType() != EntityType.PLAYER)
		continue;
	    Boolean tgtpvp = plugin.getPermsByLoc(target.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
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
			ActionBarTitleMessages.send(attacker, plugin.getLM().getMessage(lm.General_NoFriendlyFire));
			event.setIntensity(target, 0);
		    }
		}
	    }
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

	Player cause = null;

	if (damager instanceof Player) {
	    cause = (Player) damager;
	} else {
	    cause = (Player) ((Projectile) damager).getShooter();
	}

	if (cause == null)
	    return;

	Boolean srcpvp = plugin.getPermsByLoc(cause.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
	Boolean tgtpvp = plugin.getPermsByLoc(entity.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
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

	if (!plugin.getPermsByLoc(ent.getLocation()).has(Flags.falldamage, FlagCombo.TrueOrNone)) {
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
	if (!plugin.getNms().isArmorStandEntity(ent.getType()) && !(ent instanceof Arrow))
	    return;

	if (!plugin.getPermsByLoc(ent.getLocation()).has(Flags.destroy, true)) {
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
	if (!plugin.getNms().isArmorStandEntity(ent.getType()) && !(ent instanceof Arrow))
	    return;

	if (!plugin.getPermsByLoc(ent.getLocation()).has(Flags.destroy, true)) {
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

	FlagPermissions perms = plugin.getPermsByLoc(projectile.getLocation());

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
	if (!plugin.getPermsByLoc(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone))
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

	FlagPermissions perms = plugin.getPermsByLoc(event.getEntity().getLocation());
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

	FlagPermissions perms = plugin.getPermsByLoc(event.getEntity().getLocation());
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

	if (event.getEntityType() != EntityType.ENDER_CRYSTAL && event.getEntityType() != EntityType.ITEM_FRAME && !plugin.getNms().isArmorStandEntity(event
	    .getEntityType()))
	    return;

	Entity dmgr = event.getDamager();

	Player player = null;
	if (dmgr instanceof Player) {
	    player = (Player) event.getDamager();
	} else if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
	    player = (Player) ((Projectile) dmgr).getShooter();
	} else if ((dmgr instanceof Projectile) && (!(((Projectile) dmgr).getShooter() instanceof Player))) {
	    Location loc = event.getEntity().getLocation();
	    FlagPermissions perm = plugin.getPermsByLoc(loc);
	    if (perm.has(Flags.destroy, FlagCombo.OnlyFalse))
		event.setCancelled(true);
	    return;
	} else if (dmgr.getType() == EntityType.PRIMED_TNT || dmgr.getType() == EntityType.MINECART_TNT) {

	    // Disabling listener if flag disabled globally
	    if (Flags.explode.isGlobalyEnabled()) {
		FlagPermissions perms = plugin.getPermsByLoc(event.getEntity().getLocation());
		if (perms.has(Flags.explode, FlagCombo.OnlyFalse)) {
		    event.setCancelled(true);
		    return;
		}
	    }
	} else if (dmgr.getType() == EntityType.WITHER_SKULL || dmgr.getType() == EntityType.WITHER) {

	    // Disabling listener if flag disabled globally
	    if (Flags.witherdamage.isGlobalyEnabled()) {
		FlagPermissions perms = plugin.getPermsByLoc(event.getEntity().getLocation());
		if (perms.has(Flags.witherdamage, FlagCombo.OnlyFalse)) {
		    event.setCancelled(true);
		    return;
		}
	    }
	}

	Location loc = event.getEntity().getLocation();
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
	if (res == null)
	    return;

	if (isMonster(dmgr) && !res.getPermissions().has(Flags.destroy, false)) {
	    event.setCancelled(true);
	    return;
	}

	if (player == null)
	    return;

	if (plugin.isResAdminOn(player))
	    return;

	FlagPermissions perms = plugin.getPermsByLocForPlayer(loc, player);

	if (event.getEntityType() == EntityType.ITEM_FRAME) {
	    ItemFrame it = (ItemFrame) event.getEntity();
	    if (it.getItem() != null) {
//		if (!ResPerm.bypass_container.hasPermission(player, 10000L))
		    if (!perms.playerHas(player, Flags.container, true)) {
			event.setCancelled(true);
			plugin.msg(player, lm.Flag_Deny, Flags.container);
		    }
		return;
	    }
	}

	if (!perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true))) {
	    event.setCancelled(true);
	    plugin.msg(player, lm.Flag_Deny, Flags.destroy);
	}
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

	    if (area != null && ent instanceof Player && damager instanceof Player) {
		if (area.getPermissions().has(Flags.overridepvp, false) || plugin.getConfigManager().isOverridePvp() && area.getPermissions().has(Flags.pvp,
		    FlagCombo.OnlyFalse)) {
		    Player player = (Player) event.getEntity();
		    Damageable damage = player;
		    damage.damage(event.getDamage());
		    damage.setVelocity(damager.getLocation().getDirection());
		    event.setCancelled(true);
		    return;
		}
	    }

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
		.getShooter() instanceof Player))) && event.getCause() != DamageCause.FALL) {
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
		}

		if (!(ent instanceof Player))
		    return;

		if (srcarea != null && area != null && srcarea.equals(area) && attacker != null && area.isUnderRaid() && area.getRaid().onSameTeam(attacker, (Player) ent)
		    && !ConfigManager.RaidFriendlyFire) {
		    event.setCancelled(true);
		}
		
		if (srcarea != null && area != null && srcarea.equals(area) && attacker != null && area.isUnderRaid() && !area.getRaid().onSameTeam(attacker, (Player) ent)) {
		    return;
		}
		
		if (srcarea != null && area != null && srcarea.equals(area) && attacker != null &&
		    srcarea.getPermissions().playerHas((Player) ent, Flags.friendlyfire, FlagCombo.OnlyFalse) &&
		    srcarea.getPermissions().playerHas(attacker, Flags.friendlyfire, FlagCombo.OnlyFalse)) {

		    ActionBarTitleMessages.send(attacker, plugin.getLM().getMessage(lm.General_NoFriendlyFire));
		    if (isOnFire)
			ent.setFireTicks(0);
		    event.setCancelled(true);
		}

		if (!srcpvp && !isSnowBall || !allowSnowBall && isSnowBall) {
		    if (attacker != null)
			plugin.msg(attacker, lm.General_NoPVPZone);
		    if (isOnFire)
			ent.setFireTicks(0);
		    event.setCancelled(true);
		    return;
		}

		/* Check for Player vs Player */
		if (area == null) {
		    /* World PvP */
		    if (damager != null)
			if (!plugin.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.pvp, FlagCombo.TrueOrNone)) {
			    if (attacker != null)
				plugin.msg(attacker, lm.General_WorldPVPDisabled);
			    if (isOnFire)
				ent.setFireTicks(0);
			    event.setCancelled(true);
			    return;
			}

		    /* Attacking from safe zone */
		    if (attacker != null) {
			FlagPermissions aPerm = plugin.getPermsByLoc(attacker.getLocation());
			if (!aPerm.has(Flags.pvp, FlagCombo.TrueOrNone)) {
			    plugin.msg(attacker, lm.General_NoPVPZone);
			    if (isOnFire)
				ent.setFireTicks(0);
			    event.setCancelled(true);
			    return;
			}
		    }
		} else {
		    /* Normal PvP */
		    if (!isSnowBall && !area.getPermissions().has(Flags.pvp, FlagCombo.TrueOrNone) || isSnowBall && !allowSnowBall) {
			if (attacker != null)
			    plugin.msg(attacker, lm.General_NoPVPZone);
			if (isOnFire)
			    ent.setFireTicks(0);
			event.setCancelled(true);
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
}
