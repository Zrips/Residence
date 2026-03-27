package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_13 implements Listener {

    private Residence plugin;

    public ResidenceListener1_13(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryFade(BlockFadeEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.dryup.isGlobalyEnabled())
            return;

        if (shouldCancelFarmLandChange(event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.dryup.isGlobalyEnabled())
            return;

        if (shouldCancelFarmLandChange(event.getBlock()))
            event.setCancelled(true);
    }

    private boolean shouldCancelFarmLandChange(Block block) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return false;

        if (block.getType() != Material.FARMLAND)
            return false;

        if (!FlagPermissions.has(block.getLocation(), Flags.dryup, FlagCombo.OnlyFalse))
            return false;

        try {
            BlockData data = block.getBlockData();
            Farmland farm = (Farmland) data;
            if (farm.getMoisture() < 2) {
                farm.setMoisture(7);
                block.setBlockData(farm);
            }
        } catch (NoClassDefFoundError e) {
        }
        return true;
    }

    // Send message when player projectile hitting Button/Pressure_Plate is denied
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractDenyMsg(ProjectileHitEvent event) {

        Block hitBlock = event.getHitBlock();
        if (hitBlock == null || event.getHitBlockFace() == null) {
            return;
        }
        Block hitBlockFace = hitBlock.getLocation().clone().add(event.getHitBlockFace().getDirection()).getBlock();

        Flags flag = FlagPermissions.checkBlockPhysicalFlag(hitBlockFace);
        if (flag != Flags.button && flag != Flags.pressure) {
            return;
        }

        Player player = Utils.potentialProjectileToPlayer(event.getEntity());
        if (player == null)
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(hitBlockFace.getLocation(), player);
        if (perms.playerHas(player, flag, perms.playerHas(player, Flags.use, true)))
            return;

        // The perfect spot, the earlier check sends exactly one deny msg
        // avoid chat spam
        lm.Flag_Deny.sendMessage(player, flag);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtFish(PlayerInteractEntityEvent event) {
        // 1.17+ has PlayerBucketEntityEvent
        if (Version.isCurrentEqualOrHigher(Version.v1_17_R1))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled())
            return;

        Entity ent = event.getRightClicked();
        // disabling event on world
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        if (!(ent instanceof Fish))
            return;

        Player player = event.getPlayer();

        Material held = (event.getHand() == EquipmentSlot.OFF_HAND)
                ? player.getInventory().getItemInOffHand().getType()
                : player.getInventory().getItemInMainHand().getType();

        if (held != Material.WATER_BUCKET)
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        if (FlagPermissions.has(ent.getLocation(), player, Flags.animalkilling, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.animalkilling);
        event.setCancelled(true);

    }
}
