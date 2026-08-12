package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
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
