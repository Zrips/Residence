package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import io.papermc.paper.event.entity.EntityLungeEvent;

public class ResidenceListener26_1_2_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener26_1_2_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityLungeEvent(EntityLungeEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.lunge, entity)) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;

        if (FlagPermissions.shouldDenyAndNotify(player, player, Flags.lunge, null)) {
            event.setCancelled(true);
        }
    }
}
