package com.bekvon.bukkit.residence.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;

import com.bekvon.bukkit.residence.Residence;

public class ResidenceListener1_21_8_Spigot implements Listener {

    private Residence plugin;

    public ResidenceListener1_21_8_Spigot(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (ResidenceListener1_21_8_Paper.shouldCancelKnockBack(event.getEntity(), event.getSourceEntity()))
            event.setCancelled(true);
    }
}
