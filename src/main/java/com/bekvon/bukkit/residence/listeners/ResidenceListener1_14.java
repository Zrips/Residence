package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Logs.CMIDebug;

public class ResidenceListener1_14 implements Listener {

    private Residence plugin;

    public ResidenceListener1_14(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLecternBookTake(PlayerTakeLecternBookEvent event) {

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

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        Entity entity = event.getEntity();

        if (entity.getType() != EntityType.RAVAGER)
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());
        if (perms.has(Flags.destroy, true))
            return;

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {

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
}
