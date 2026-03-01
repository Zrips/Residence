package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onTrampleTurtleEgg(PlayerInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;
        Block block = event.getClickedBlock();
        if (block == null || event.getAction() != Action.PHYSICAL || block.getType() != Material.TURTLE_EGG)
            return;
        if (!ResidenceBlockListener.canBreakBlock(event.getPlayer(), block.getLocation(), true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTouchButtonPlateDenyMsg(ProjectileHitEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.use.isGlobalyEnabled())
            return;
        // Avoid Projectile getWorld NPE
        Block hitBlock = event.getHitBlock();
        if (hitBlock == null)
            return;

        if (plugin.isDisabledWorldListener(hitBlock.getWorld()))
            return;

        if (event.getHitBlockFace() == null)
            return;

        Block block = hitBlock.getLocation().clone().add(event.getHitBlockFace().getDirection()).getBlock();

        Flags flag = null;

        CMIMaterial cmat = CMIMaterial.get(block.getType());

        if (cmat.containsCriteria(CMIMC.BUTTON)) {
            flag = Flags.button;
        } else if (cmat.containsCriteria(CMIMC.PRESSUREPLATE)) {
            flag = Flags.pressure;
        }

        if (flag == null)
            return;

        Player player = Utils.potentialProjectileToPlayer(event.getEntity());
        if (player == null)
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
        if (perms.playerHas(player, flag, perms.playerHas(player, Flags.use, true)))
            return;

        // The perfect spot, the earlier check sends exactly one deny msg
        // for the EntityInteractEvent below to avoid chat spam
        lm.Flag_Deny.sendMessage(player, flag);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTouchButtonPlate(EntityInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.use.isGlobalyEnabled())
            return;

        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Entity entity = event.getEntity();
        // Only check Projectile and DropItem
        if (!(entity instanceof Projectile) && !(entity instanceof Item))
            return;

        Flags flag = null;

        CMIMaterial cmat = CMIMaterial.get(block.getType());

        if (cmat.containsCriteria(CMIMC.BUTTON)) {
            flag = Flags.button;
        } else if (cmat.containsCriteria(CMIMC.PRESSUREPLATE)) {
            flag = Flags.pressure;
        }

        if (flag == null)
            return;

        Player player = Utils.potentialProjectileToPlayer(entity);
        if (player != null) {

            if (ResAdmin.isResAdmin(player))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            if (perms.playerHas(player, flag, perms.playerHas(player, Flags.use, true)))
                return;

        } else {
            // Check potential block as a shooter which should be allowed if its inside same
            // residence
            if (Utils.isSourceBlockInsideSameResidence(entity, ClaimedResidence.getByLoc(block.getLocation())))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            if (perms.has(flag, perms.has(Flags.use, true)))
                return;

        }

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtFish(PlayerInteractEntityEvent event) {

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
