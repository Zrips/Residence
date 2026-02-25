package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Items.CMIMaterial;

public class ResidenceListener1_20 implements Listener {

    private Residence plugin;

    public ResidenceListener1_20(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignWax(PlayerInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.build.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        if (player == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getItem() == null || event.getItem().getType() != Material.HONEYCOMB)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (!CMIMaterial.isSign(block.getType()))
            return;

        if (ResPerm.bypass_build.hasPermission(player, 10000L))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);

        if (perms.playerHas(player, Flags.build, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.build);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerSignOpenEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.build.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        if (player == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;

        if (player.hasMetadata("NPC"))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getSign().getLocation(), player);

        boolean hasUse = perms.playerHas(player, Flags.use, FlagCombo.TrueOrNone);
        boolean hasBuild = perms.playerHas(player, Flags.build, FlagCombo.TrueOrNone);

        if (hasUse && hasBuild || ResAdmin.isResAdmin(player))
            return;

        event.setCancelled(true);

        if (!hasUse)
            lm.Flag_Deny.sendMessage(player, Flags.use);
        else
            lm.Flag_Deny.sendMessage(player, Flags.build);

    }

    // Projectile hit chorus_flower decorated_pot pointed_dripstone
    // Sweep SuspiciousBlocks
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectilePlayerChangeBlock(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;

        Block block = event.getBlock();
        if (block == null)
            return;

        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Entity entity = event.getEntity();
        // Only check projectile
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;

            Player player = null;

            // Get projectile player source
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }

            // have player source
            if (player != null) {

                if (ResAdmin.isResAdmin(player))
                    return;

                FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
                if (perms.playerHas(player, Flags.destroy, true))
                    return;

                lm.Flag_Deny.sendMessage(player, Flags.destroy);
                event.setCancelled(true);
                return;

            }
            // Not player source
            // Check potential block as a shooter which should be allowed if its inside same
            // residence
            if (Utils.isSourceBlockInsideSameResidence(entity, ClaimedResidence.getByLoc(block.getLocation())))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            if (perms.has(Flags.destroy, true))
                return;

            event.setCancelled(true);
            return;

        }
        // Event not triggered by projectile
        // Only check SuspiciousBlocks
        CMIMaterial blockM = CMIMaterial.get(block.getType());
        if (blockM != CMIMaterial.SUSPICIOUS_SAND &&
                blockM != CMIMaterial.SUSPICIOUS_GRAVEL)
            return;

        // Only check player
        if (!(entity instanceof Player))
            return;

        Player player = (Player) entity;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
        if (perms.playerHas(player, Flags.brush, perms.playerHas(player, Flags.destroy, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.brush);
        event.setCancelled(true);
    }
}
