package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
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
    public void onSignInteract(PlayerSignOpenEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.build.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
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

    // Projectile hit chorus_flower,decorated_pot,pointed_dripstone
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileChangeBlock(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;

        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Entity entity = event.getEntity();

        if (!(entity instanceof Projectile))
            return;

        Player player = Utils.potentialProjectileToPlayer(entity);

        if (player != null) {

            if (ResAdmin.isResAdmin(player))
                return;

            if (FlagPermissions.has(block.getLocation(), player, Flags.destroy, true))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.destroy);
            event.setCancelled(true);

        } else {
            // Check potential block as a shooter which should be allowed if its inside same
            // residence
            if (Utils.isSourceBlockInsideSameResidence(entity, ClaimedResidence.getByLoc(block.getLocation())))
                return;

            if (FlagPermissions.has(block.getLocation(), Flags.destroy, true))
                return;

            event.setCancelled(true);

        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChangeSuspiciousBlock(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.brush.isGlobalyEnabled())
            return;

        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        Material mat = block.getType();

        if (mat != Material.SUSPICIOUS_GRAVEL && mat != Material.SUSPICIOUS_SAND)
            return;

        Player player = (Player) event.getEntity();
        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
        if (perms.playerHas(player, Flags.brush, perms.playerHas(player, Flags.build, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.brush);
        event.setCancelled(true);

    }
}
