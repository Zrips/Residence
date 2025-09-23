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

public class ResidencePlayerListener1_14 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_14(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJump(PlayerTakeLecternBookEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getLectern().getWorld()))
            return;
        if (ResAdmin.isResAdmin(event.getPlayer())) {
            return;
        }

        if (FlagPermissions.has(event.getLectern().getLocation(), event.getPlayer(), Flags.container, FlagCombo.TrueOrNone))
            return;
        event.setCancelled(true);
        lm.Flag_Deny.sendMessage(event.getPlayer(), Flags.container);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRavager(EntityChangeBlockEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Entity ent = event.getEntity();

        if (ent.getType() != EntityType.RAVAGER)
            return;

        if (!FlagPermissions.has(event.getBlock().getLocation(), Flags.destroy, FlagCombo.OnlyFalse))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getVehicle().getWorld()))
            return;

        if (event.isCancelled())
            return;

        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            if (!FlagPermissions.has(event.getVehicle().getLocation(), (Player) attacker, Flags.destroy, FlagCombo.OnlyFalse))
                return;
        } else {
            if (!FlagPermissions.has(event.getVehicle().getLocation(), Flags.destroy, FlagCombo.OnlyFalse))
                return;
        }

        event.setCancelled(true);

        if (attacker instanceof Player)
            lm.Flag_Deny.sendMessage((Player) attacker, Flags.destroy);

    }
}
