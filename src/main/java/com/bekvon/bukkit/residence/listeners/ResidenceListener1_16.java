package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_16 implements Listener {

    private Residence plugin;

    public ResidenceListener1_16(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLightningTransformAnimal(EntityTransformEvent event) {
        // Paper 1.16.5+ uses EntityZapEvent for more detailed handling
        if (Version.isCurrentEqualOrHigher(Version.v1_17_0)
                || (Version.isCurrentEqualOrHigher(Version.v1_16_R3) && Version.isCurrentSubEqualOrHigher(5))) {
            if (Version.isPaperBranch()) {
                return;
            }
        }
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled()) {
            return;
        }
        Entity entity = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld())) {
            return;
        }
        if (event.getTransformReason() != EntityTransformEvent.TransformReason.LIGHTNING) {
            return;
        }
        if (!(entity instanceof LivingEntity) || !Utils.isAnimal(entity)) {
            return;
        }
        if (FlagPermissions.has(entity.getLocation(), Flags.animalkilling, true)) {
            return;
        }

        event.setCancelled(true);

    }
}
