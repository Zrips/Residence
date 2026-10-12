package com.bekvon.bukkit.residence.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_08 implements Listener {

    private final Residence plugin;

    public ResidenceListener1_08(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtArmoStand(PlayerInteractAtEntityEvent event) {

        Entity entity = event.getRightClicked();

        if (FlagPermissions.shouldIgnoreCheck(Flags.container, entity)) {
            return;
        }
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify(event.getPlayer(), entity, Flags.container, Flags.use)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {

        Block sourceBlock = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(sourceBlock)) {
            return;
        }
        // ExplosionResult.TRIGGER_BLOCK does not destroy blocks
        // it is triggered by (Enchantment: Wind Burst)
        if (Version.isCurrentEqualOrHigher(Version.v1_21_0)
                && event.getExplosionResult() == org.bukkit.ExplosionResult.TRIGGER_BLOCK) {
            ResidenceListener1_21.onWindExplode(event);
            return;
        }
        boolean shouldCheckExplode = Flags.explode.isGlobalyEnabled();
        if (shouldCheckExplode) {
            FlagPermissions sourceBlockPerms = FlagPermissions.getPerms(sourceBlock.getLocation());
            // Explosion is prohibited at the source location; cancel the event directly
            if (!sourceBlockPerms.has(Flags.explode, sourceBlockPerms.has(Flags.destroy, true))) {
                event.setCancelled(true);
                return;
            }
        }
        boolean shouldCheckDestroy = Flags.destroy.isGlobalyEnabled();
        if (!shouldCheckExplode && !shouldCheckDestroy) {
            return;
        }
        // Source allows explosion, so check each affected block for destruction
        event.blockList().removeIf(block -> {
            FlagPermissions blockPerms = FlagPermissions.getPerms(block.getLocation());
            return (shouldCheckExplode && blockPerms.has(Flags.explode, FlagCombo.OnlyFalse))
                    || (shouldCheckDestroy && blockPerms.has(Flags.destroy, FlagCombo.OnlyFalse));
        });
    }
}
