package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Cushion;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import io.papermc.paper.event.entity.EntityBreakByEntityEvent;
import io.papermc.paper.event.entity.EntityBreakEvent;
import io.papermc.paper.event.entity.EntityBreakEvent.RemoveCause;

public class ResidenceListener26_3_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener26_3_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    // Spigot classifies Cushion as HangingEntity type, use HangingBreakByEntityEvent
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCushionBreakByEntity(EntityBreakByEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.destroy, entity)) {
            return;
        }
        if (!(entity instanceof Cushion)) {
            return;
        }
        if (ResidenceEntityListener.shouldDenyEntityHit(event.getRemover(), entity.getLocation(), Flags.destroy, Flags.build)) {
            event.setCancelled(true);
        }
    }

    // Spigot classifies Cushion as HangingEntity type, use HangingBreakEvent
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCushionBreakByExplosion(EntityBreakEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.explode, entity)) {
            return;
        }
        if (!(entity instanceof Cushion)) {
            return;
        }
        if (event.getCause() != RemoveCause.EXPLOSION) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation());
        if (!perms.has(Flags.explode, perms.has(Flags.destroy, true))) {
            event.setCancelled(true);
        }
    }

    // Spigot classifies Cushion as HangingEntity type, use HangingPlaceEvent
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCushionPlace(EntityPlaceEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.place, entity)) {
            return;
        }
        if (!(entity instanceof Cushion)) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify(player, entity, Flags.place, Flags.build)) {
            event.setCancelled(true);
        }
    }
}
