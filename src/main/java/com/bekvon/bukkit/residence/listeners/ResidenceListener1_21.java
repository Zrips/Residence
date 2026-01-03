package com.bekvon.bukkit.residence.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffectType;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;

public class ResidenceListener1_21 implements Listener {

    private Residence plugin;

    public ResidenceListener1_21(Residence plugin) {
        this.plugin = plugin;
    }

    HashMap<UUID, Long> boats = new HashMap<UUID, Long>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        boats.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnVehicleEnterEvent(VehicleEnterEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.boarding.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getVehicle().getWorld()))
            return;

        Entity entity = event.getEntered();

        if (!(entity instanceof LivingEntity))
            return;

        if (!Utils.isAnimal(entity))
            return;

        if (FlagPermissions.getPerms(entity.getLocation()).has(Flags.boarding, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());
        if (res == null)
            return;

        Player closest = null;
        double dist = 32D;

        for (Player player : res.getPlayersInResidence()) {

            double tempDist = player.getLocation().distance(entity.getLocation());

            if (tempDist < dist) {
                closest = player;
                dist = tempDist;
            }
        }

        if (closest == null)
            return;

        if (res.getPermissions().playerHas(closest, Flags.leash, FlagCombo.OnlyFalse)) {
            Long time = boats.computeIfAbsent(closest.getUniqueId(), k -> 0L);

            if (time + 1000L < System.currentTimeMillis()) {
                boats.put(closest.getUniqueId(), System.currentTimeMillis());
                lm.Residence_FlagDeny.sendMessage(closest, Flags.leash, res.getName());
            }

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnEntityDeath(EntityDeathEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.build.isGlobalyEnabled())
            return;
        // disabling event on world
        LivingEntity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;
        if (!ent.hasPotionEffect(PotionEffectType.WEAVING))
            return;

        Location loc = ent.getLocation();
        FlagPermissions perms = FlagPermissions.getPerms(loc);
        if (perms.has(Flags.build, FlagCombo.TrueOrNone))
            return;

        // Removing weaving effect on death as there is no other way to properly handle
        // this effect inside residence
        ent.removePotionEffect(PotionEffectType.WEAVING);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractCopperGolem(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.copper.isGlobalyEnabled())
            return;

        Entity entity = event.getRightClicked();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        if (CMIEntityType.get(entity) != CMIEntityType.COPPER_GOLEM)
            return;

        Player player = event.getPlayer();

        if (ResAdmin.isResAdmin(player))
            return;

        CMIMaterial hand = CMIMaterial.get(player.getInventory().getItemInMainHand());

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            hand = CMIMaterial.get(player.getInventory().getItemInOffHand());
        }
        // Avoid overwriting Leash Flag, Lead Shears
        if (hand != CMIMaterial.HONEYCOMB && !hand.toString().contains("_AXE"))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation(), player);
        if (perms.playerHas(player, Flags.copper, perms.playerHas(player, Flags.animalkilling, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.copper);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFishingBobberHit(ProjectileHitEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.hook.isGlobalyEnabled())
            return;
        // anti NPE
        Entity HitEntity = event.getHitEntity();
        if (HitEntity == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(HitEntity.getWorld()))
            return;

        Projectile hook = event.getEntity();
        // only fishing_bobber
        if (CMIEntityType.get(hook) != CMIEntityType.FISHING_BOBBER)
            return;
        // have player source
        if (!(hook.getShooter() instanceof Player))
            return;

        Player player = (Player) hook.getShooter();
        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(HitEntity.getLocation(), player);
        if (perms.playerHas(player, Flags.hook, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.hook);
        event.setCancelled(true);
    }
}
