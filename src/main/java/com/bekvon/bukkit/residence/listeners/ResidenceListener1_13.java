package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
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
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
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
    public void onButtonHitWithProjectile(ProjectileHitEvent e) {

        // Disabling listener if flag disabled globally
        if (!Flags.button.isGlobalyEnabled())
            return;

        if (e.getHitBlock() == null)
            return;

        if (plugin.isDisabledWorldListener(e.getHitBlock().getWorld()))
            return;

        Block block = e.getHitBlock().getLocation().clone().add(e.getHitBlockFace().getDirection()).getBlock();

        @NotNull
        CMIMaterial cmat = CMIMaterial.get(block.getType());

        if (!cmat.isButton() && !cmat.isPlate())
            return;

        ClaimedResidence res = ClaimedResidence.getByLoc(block.getLocation());

        if (res != null && res.getRaid().isUnderRaid())
            return;

        Player player = Utils.potentialProjectileToPlayer(e.getEntity());

        if (player != null) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);

            boolean hasuse = perms.playerHas(player, Flags.use, true);

            Flags result = FlagPermissions.getMaterialUseFlagList().get(block.getType());
            if (result == null)
                return;

            if (perms.playerHas(player, result, hasuse))
                return;

            switch (result) {
            case button:
                if (ResPerm.bypass_button.hasPermission(player, 10000L))
                    return;
                break;
            }
            // The perfect spot, the earlier check sends exactly one deny message.
            // Move it to the end and the players chat will be flooded with deny messages.
            lm.Flag_Deny.sendMessage(player, result);
        } else {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());

            boolean hasuse = perms.has(Flags.use, true);

            Flags result = FlagPermissions.getMaterialUseFlagList().get(block.getType());

            if (result == null)
                return;

            if (perms.has(result, hasuse))
                return;
        }

        e.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractButton(EntityInteractEvent event) {

        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        Entity ent = event.getEntity();
        if (!(ent instanceof Arrow) && !(ent instanceof Trident) && !(ent instanceof Item))
            return;

        @NotNull
        CMIMaterial cmat = CMIMaterial.get(event.getBlock().getType());

        if (!cmat.isButton() && !cmat.isPlate())
            return;

        Player player = Utils.potentialProjectileToPlayer(ent);

        if (player != null) {

            Flags result = FlagPermissions.getMaterialUseFlagList().get(event.getBlock().getType());
            if (result == null)
                return;

            FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation(), player);

            boolean hasUse = perms.playerHas(player, Flags.use, true);

            if (perms.playerHas(player, result, hasUse))
                return;

            switch (result) {
            case button:
                if (ResPerm.bypass_button.hasPermission(player, 10000L))
                    return;
                break;
            }
        } else {
            FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation());
            if (perms.has(Flags.button, perms.has(Flags.use, true)))
                return;
        }

        event.setCancelled(true);
    }
}
