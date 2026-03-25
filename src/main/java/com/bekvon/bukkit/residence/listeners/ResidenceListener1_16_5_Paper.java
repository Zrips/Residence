package com.bekvon.bukkit.residence.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.utils.Utils;
import com.destroystokyo.paper.event.entity.EntityZapEvent;

import io.papermc.paper.event.block.TargetHitEvent;

public class ResidenceListener1_16_5_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener1_16_5_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHitTargetBlock(TargetHitEvent event) {

        Block block = event.getHitBlock();
        if (block == null) {
            return;
        }
        if (ResidenceListener1_14.shouldBlockProjectileHit(block, event.getEntity(), Flags.use)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityZapEvent(EntityZapEvent event) {

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || !Utils.isAnimal(entity)) {
            return;
        }
        if (ResidenceListener1_16.shouldBlockLightning(event.getBolt(), entity.getLocation())) {
            event.setCancelled(true);
        }

    }
}
