package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class ResidenceListener1_16 implements Listener {

    private Residence plugin;

    public ResidenceListener1_16(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLightningStrikeEvent(LightningStrikeEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled())
            return;

        if (!event.getCause().equals(LightningStrikeEvent.Cause.TRIDENT))
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getWorld()))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getLightning().getLocation());
        if (perms.has(Flags.animalkilling, true))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractRespawn(PlayerInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Material mat = block.getType();

        if (mat == Material.RESPAWN_ANCHOR) {

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            if (perms.playerHas(player, Flags.anchor, perms.playerHas(player, Flags.destroy, true)))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.anchor);
            event.setCancelled(true);

        } else if (mat == Material.REDSTONE_WIRE) {

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            if (perms.playerHas(player, Flags.build, true))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.build);
            event.setCancelled(true);

        }
    }
}
