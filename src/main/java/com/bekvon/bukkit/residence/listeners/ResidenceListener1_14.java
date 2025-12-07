package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Logs.CMIDebug;

public class ResidenceListener1_14 implements Listener {

    private Residence plugin;

    public ResidenceListener1_14(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLecternBookTake(PlayerTakeLecternBookEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getLectern().getWorld()))
            return;

        Player player = event.getPlayer();

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getLectern().getLocation(), player);
        if (perms.playerHas(player, Flags.container, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.container);

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRavager(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        if (event.getEntity().getType() != EntityType.RAVAGER)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation());
        if (perms.has(Flags.destroy, true))
            return;

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.vehicledestroy.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getVehicle().getWorld()))
            return;

        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {

            Player player = (Player) attacker;

            if (ResAdmin.isResAdmin(player))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(event.getVehicle().getLocation(), player);
            if (perms.playerHas(player, Flags.vehicledestroy, true))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.vehicledestroy);

            event.setCancelled(true);

        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHitBell(ProjectileHitEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.use.isGlobalyEnabled())
            return;

        Block block = event.getHitBlock();
        if (block == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (block.getType() != Material.BELL)
            return;

        Player player = Utils.potentialProjectileToPlayer(event.getEntity());
        if (player != null) {

            if (ResAdmin.isResAdmin(player))
                return;

            if (FlagPermissions.has(block.getLocation(), player, Flags.use, true))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.use);
            event.setCancelled(true);

        } else {
            // Entity not player source
            // Check potential block as a shooter which should be allowed if its inside same
            // residence
            if (Utils.isSourceBlockInsideSameResidence(event.getEntity(), ClaimedResidence.getByLoc(block.getLocation())))
                return;

            if (FlagPermissions.has(block.getLocation(), Flags.use, true))
                return;

            event.setCancelled(true);

        }
    }
}
