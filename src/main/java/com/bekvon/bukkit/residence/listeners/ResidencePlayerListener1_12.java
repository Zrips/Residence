package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

public class ResidencePlayerListener1_12 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_12(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(EntityPickupItemEvent event) {
        if (!Flags.itempickup.isGlobalyEnabled())
            return;
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getItem().getLocation());
        if (res == null)
            return;
        if (event.getEntity().hasMetadata("NPC"))
            return;

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!res.getPermissions().playerHas(player, Flags.itempickup, FlagCombo.OnlyFalse))
                return;
            if (ResPerm.bypass_itempickup.hasPermission(player, 10000L))
                return;
        } else {
            if (!res.getPermissions().has(Flags.itempickup, FlagCombo.OnlyFalse))
                return;
        }
        event.setCancelled(true);
        event.getItem().setPickupDelay(plugin.getConfigManager().getItemPickUpDelay() * 20);
    }
}
