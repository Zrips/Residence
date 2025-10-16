package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_13 implements Listener {

    private Residence plugin;

    public ResidenceListener1_13(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryFade(BlockFadeEvent event) {
        if (Version.isCurrentLower(Version.v1_13_R1))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.dryup.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;
        CMIMaterial mat = CMIMaterial.get(event.getBlock());
        if (!mat.equals(CMIMaterial.FARMLAND))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getNewState().getLocation());
        if (perms.has(Flags.dryup, FlagCombo.OnlyFalse)) {
            Block b = event.getBlock();
            try {
                BlockData data = b.getBlockData();
                Farmland farm = (Farmland) data;
                if (farm.getMoisture() < 2) {
                    farm.setMoisture(7);
                    b.setBlockData(farm);
                }
            } catch (NoClassDefFoundError e) {
            }
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {
        if (Version.isCurrentLower(Version.v1_13_R1))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.dryup.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;
        try {

            if (!event.getChangedType().toString().equalsIgnoreCase("FARMLAND"))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation());
            if (perms.has(Flags.dryup, FlagCombo.OnlyFalse)) {
                Block b = event.getBlock();
                try {
                    BlockData data = b.getBlockData();
                    Farmland farm = (Farmland) data;
                    if (farm.getMoisture() < 2) {
                        farm.setMoisture(7);
                        b.setBlockData(farm);
                    }
                } catch (NoClassDefFoundError e) {
                }
                event.setCancelled(true);
                return;
            }
        } catch (Exception | Error e) {

        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.PHYSICAL) || !event.getClickedBlock().getType().equals(Material.TURTLE_EGG))
            return;
        if (!ResidenceBlockListener.canBreakBlock(event.getPlayer(), event.getClickedBlock(), true))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onButtonHitWithProjectile(ProjectileHitEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.button.isGlobalyEnabled())
            return;
        // Avoid Projectile getWorld NPE
        if (event.getHitBlock() == null)
            return;

        if (plugin.isDisabledWorldListener(event.getHitBlock().getWorld()))
            return;

        Block block = event.getHitBlock().getLocation().clone().add(event.getHitBlockFace().getDirection()).getBlock();

        @NotNull
        CMIMaterial cmat = CMIMaterial.get(block.getType());
        boolean isButton = cmat.isButton();
        boolean isPlate = cmat.isPlate();

        if (!isButton && !isPlate)
            return;

        ClaimedResidence res = ClaimedResidence.getByLoc(block.getLocation());
        if (res != null && res.getRaid().isUnderRaid())
            return;

        Flags targetFlag = null;
        if (isButton) {
            targetFlag = Flags.button;

        // Button or a Plate, for easier future additions
        } else if (isPlate) {
            targetFlag = Flags.pressure;
        }

        Player player = Utils.potentialProjectileToPlayer(event.getEntity());
        if (player != null) {

            if (ResAdmin.isResAdmin(player))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            boolean hasUse = perms.playerHas(player, Flags.use, true);

            if (isButton) {
                if (perms.playerHas(player, targetFlag, hasUse))
                    return;

            // Button or a Plate, for easier future additions
            } else if (isPlate) {
                if (perms.playerHas(player, targetFlag, hasUse))
                    return;
            }

            // The perfect spot, the earlier check sends exactly one deny msgs
            // Deny msgs for the EntityInteractEvent below to avoid chat spam
            // Send matching deny msgs for flag types
            lm.Flag_Deny.sendMessage(player, targetFlag);

        //Check when the entity has no player source
        } else {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            boolean hasUse = perms.has(Flags.use, true);

            if (isButton) {
                if (perms.has(targetFlag, hasUse)) {
                    return;
                }

            // Button or a Plate, for easier future additions
            } else if (isPlate) {
                if (perms.has(targetFlag, hasUse)) {
                    return;
                }
            }
        }

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractButton(EntityInteractEvent event) {

        Block block = event.getBlock();
        if (block == null)
            return;

        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Entity entity = event.getEntity();
        // Only check Projectile and DropItem
        if (!(entity instanceof Projectile) && !(entity instanceof Item))
            return;

        @NotNull
        CMIMaterial cmat = CMIMaterial.get(block.getType());
        boolean isButton = cmat.isButton();
        boolean isPlate = cmat.isPlate();

        // Only check Button and Plate
        if (!isButton && !isPlate)
            return;

        Flags targetFlag = null;
        if (isButton) {
            targetFlag = Flags.button;

        // Easier future addition
        } else if (isPlate) {
            targetFlag = Flags.pressure;

        }

        // Only get projectile player source
        Player player = Utils.potentialProjectileToPlayer(entity);
        if (player != null) {

            if (ResAdmin.isResAdmin(player))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            boolean hasUse = perms.playerHas(player, Flags.use, true);

            if (isButton) {
                if (perms.playerHas(player, targetFlag, hasUse))
                    return;

                event.setCancelled(true);
                return;

            // Easier future addition
            } else if (isPlate) {
                if (perms.playerHas(player, targetFlag, hasUse))
                    return;

                event.setCancelled(true);
                return;

            }

        // Entity not player source
        } else {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            boolean hasUse = perms.has(Flags.use, true);

            if (isButton) {
                if (perms.has(targetFlag, hasUse))
                    return;

                event.setCancelled(true);
                return;

            // Easier future addition
            } else if (isPlate) {
                if (perms.has(targetFlag, hasUse))
                    return;

                event.setCancelled(true);
                return;

            } 
        }
    }
}
