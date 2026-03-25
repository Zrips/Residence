package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LightningStrike;
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
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_16 implements Listener {

    private Residence plugin;

    public ResidenceListener1_16(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLightningStrikeEvent(LightningStrikeEvent event) {

        if (event.getCause() != LightningStrikeEvent.Cause.TRIDENT) {
            return;
        }
        if (shouldBlockLightning(event.getLightning(), event.getLightning().getLocation())) {
            event.setCancelled(true);
        }

    }

    public static boolean shouldBlockLightning(LightningStrike lightning, Location entLoc) {
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled()) {
            return false;
        }
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(lightning.getWorld())) {
            return false;
        }
        Player player = Version.isCurrentEqualOrHigher(Version.v1_20_R2)
                ? lightning.getCausingPlayer()
                : null;

        if (player != null) {
            if (ResAdmin.isResAdmin(player)) {
                return false;
            }
            return FlagPermissions.has(entLoc, player, Flags.animalkilling, FlagCombo.OnlyFalse);
        } else {
            return FlagPermissions.has(entLoc, Flags.animalkilling, FlagCombo.OnlyFalse);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractRespawn(PlayerInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.anchor.isGlobalyEnabled())
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (block.getType() != Material.RESPAWN_ANCHOR)
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
        if (perms.playerHas(player, Flags.anchor, perms.playerHas(player, Flags.destroy, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.anchor);
        event.setCancelled(true);

    }
}
