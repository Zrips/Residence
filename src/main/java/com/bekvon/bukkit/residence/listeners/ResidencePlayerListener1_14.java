package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

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
}
