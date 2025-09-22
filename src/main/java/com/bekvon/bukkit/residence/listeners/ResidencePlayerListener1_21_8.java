package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

public class ResidencePlayerListener1_21_8 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_21_8(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onKnockback(EntityKnockbackEvent event) {

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getEntity();

        Location loc = entity.getLocation();

        if (Utils.isAnimal(entity)) {
            if (plugin.getPermsByLoc(loc).has(Flags.animalkilling, FlagCombo.OnlyFalse))
                event.setCancelled(true);
            return;
        }

        if (ResidenceEntityListener.isMonster(entity)) {
            if (plugin.getPermsByLoc(loc).has(Flags.mobkilling, FlagCombo.OnlyFalse))
                event.setCancelled(true);
            return;
        }

        if (entity instanceof Player) {
            if (plugin.getPermsByLoc(loc).has(Flags.pvp, FlagCombo.OnlyFalse))
                event.setCancelled(true);
            return;
        }
    }
}
