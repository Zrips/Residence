package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
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

import net.Zrips.CMILib.Items.CMIMaterial;

public class ResidenceListener1_20 implements Listener {

    private Residence plugin;

    public ResidenceListener1_20(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignWax(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getItem() == null || event.getItem().getType() != Material.HONEYCOMB)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (!CMIMaterial.isSign(block.getType()))
            return;

        if (ResPerm.bypass_build.hasPermission(event.getPlayer(), 10000L))
            return;

        Player player = event.getPlayer();

        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        if (perms.playerHas(player, Flags.build, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.build);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerSignOpenEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;

        FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getSign().getLocation(), player);

        boolean hasuse = perms.playerHas(player, Flags.use, FlagCombo.TrueOrNone);
        boolean hasBuild = perms.playerHas(player, Flags.build, FlagCombo.TrueOrNone);

        if (hasuse && hasBuild || ResAdmin.isResAdmin(player))
            return;

        event.setCancelled(true);

        if (!hasuse)
            lm.Flag_Deny.sendMessage(player, Flags.use);
        else
            lm.Flag_Deny.sendMessage(player, Flags.build);

    }

    // Projectile hit chorus_flower decorated_pot pointed_dripstone
    // Brush sweep suspicious_sand suspicious_gravel
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectilePlayerChangeBlock(EntityChangeBlockEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        Block targetBlock = event.getBlock();
        Player player = null;

        // Check if projectile
        if (event.getEntity() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getEntity();

            // Get projectile player source
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }

            if (player != null) {

                if (ResAdmin.isResAdmin(player))
                    return;

                FlagPermissions perms = FlagPermissions.getPerms(targetBlock.getLocation(), player);
                if (perms.playerHas(player, Flags.destroy, true))
                    return;

                lm.Flag_Deny.sendMessage(player, Flags.destroy);

                event.setCancelled(true);

            }
            // Check projectile not player source
            FlagPermissions perms = FlagPermissions.getPerms(targetBlock.getLocation());
            if (perms.has(Flags.destroy, true))
                return;

            event.setCancelled(true);

            // Not projectile, get player
        }else if (event.getEntity() instanceof Player) {
            player = (Player) event.getEntity();

            CMIMaterial heldItem = CMIMaterial.get(player.getItemInHand());
            CMIMaterial blockM = CMIMaterial.get(targetBlock.getType());

            // Check player hold brush interact suspicious_sand suspicious_gravel
            if (heldItem != null && heldItem.equals(CMIMaterial.BRUSH) &&
                    (blockM == CMIMaterial.SUSPICIOUS_SAND || blockM == CMIMaterial.SUSPICIOUS_GRAVEL)) {

                if (ResAdmin.isResAdmin(player))
                    return;

                ClaimedResidence res = plugin.getResidenceManager().getByLoc(targetBlock.getLocation());
                if (res != null && !res.getPermissions().playerHas(player, Flags.brush, FlagCombo.OnlyTrue)) {

                    lm.Flag_Deny.sendMessage(player, Flags.brush);

                    event.setCancelled(true);
                }
            }
        }
    }
}
