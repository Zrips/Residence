package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.containers.ResAdmin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Utils;

import com.destroystokyo.paper.event.entity.EntityZapEvent;

public class ResidenceListener1_21_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener1_21_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityZapEvent(EntityZapEvent event) {

        Entity entity = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        Player player = event.getBolt().getCausingPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        if (Flags.animalkilling.isGlobalyEnabled() && Utils.isAnimal(entity)) {

            if (player != null) {
                if (FlagPermissions.has(entity.getLocation(), player, Flags.animalkilling, true))
                    return;

                lm.Flag_Deny.sendMessage(player, Flags.animalkilling);

            } else {
                if (FlagPermissions.has(entity.getLocation(), Flags.animalkilling, true))
                    return;
            }

            event.setCancelled(true);

        } else if (Flags.mobkilling.isGlobalyEnabled() && ResidenceEntityListener.isMonster(entity)) {

            if (player != null) {
                if (FlagPermissions.has(entity.getLocation(), player, Flags.mobkilling, true))
                    return;

                lm.Flag_Deny.sendMessage(player, Flags.mobkilling);

            } else {
                if (FlagPermissions.has(entity.getLocation(), Flags.mobkilling, true))
                    return;
            }

            event.setCancelled(true);

        }
    }
}
