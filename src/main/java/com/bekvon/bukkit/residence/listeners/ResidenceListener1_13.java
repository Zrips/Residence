package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;
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

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onButtonHitWithProjectile(BlockRedstoneEvent e) {
//
//        if (tempButtonLocation == null)
//            return;
//
//        // Disabling listener if flag disabled globally
//        if (!Flags.button.isGlobalyEnabled())
//            return;
//
//        if (e.getBlock() == null)
//            return;
//
//        if (plugin.isDisabledWorldListener(e.getBlock().getWorld()))
//            return;
//
//        Block block = e.getBlock();
//
//        if (!tempButtonLocation.equals(block.getLocation()) && !tempButtonLocation.clone().add(0, 1, 0).equals(block.getLocation()))
//            return;
//
//        if (!CMIMaterial.isButton(block.getType()))
//            return;
//
//        e.setNewCurrent(0);
//    }

//    private Location tempButtonLocation = null;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onButtonHitWithProjectile(ProjectileHitEvent e) {

//        tempButtonLocation = null;
        // Disabling listener if flag disabled globally
        if (!Flags.button.isGlobalyEnabled())
            return;

        if (e.getHitBlock() == null)
            return;

        if (plugin.isDisabledWorldListener(e.getHitBlock().getWorld()))
            return;

        if (!(e.getEntity().getShooter() instanceof Player))
            return;

        Player player = (Player) e.getEntity().getShooter();

        Block block = e.getHitBlock().getLocation().clone().add(e.getHitBlockFace().getDirection()).getBlock();

//        tempButtonLocation = block.getLocation().clone();

        if (!CMIMaterial.isButton(block.getType()))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);

        boolean hasuse = perms.playerHas(player, Flags.use, true);

        ClaimedResidence res = ClaimedResidence.getByLoc(block.getLocation());

        Flags result = FlagPermissions.getMaterialUseFlagList().get(block.getType());
        if (result == null)
            return;

        if (perms.playerHas(player, result, hasuse))
            return;

        if (res != null && res.getRaid().isUnderRaid() && res.getRaid().isAttacker(player)) {
            return;
        }

        switch (result) {
            case button:
                if (ResPerm.bypass_button.hasPermission(player, 10000L))
                    return;
                break;
        }

        e.setCancelled(true);

        // The perfect spot, the earlier check sends exactly one deny message.
        // Move it to the end and the players chat will be flooded with deny messages.
        lm.Flag_Deny.sendMessage(player, result);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractButton(EntityInteractEvent event) {
        if (Version.isCurrentLower(Version.v1_13_R1))
            return;
        if (!CMIMaterial.isButton(event.getBlock().getType()))
            return;

        Entity ent = event.getEntity();
        if (!(ent instanceof Arrow) && !(ent instanceof Trident))
            return;

        Player player = null;
        if (ent instanceof Arrow && ((Arrow) ent).getShooter() instanceof Player)
            player = (Player) ((Arrow) ent).getShooter();
        else if (ent instanceof Trident && ((Trident) ent).getShooter() instanceof Player)
            player = (Player) ((Trident) ent).getShooter();

        if (player == null) {
            event.setCancelled(true);
            return;
        }

        FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation(), player);
        boolean hasUse = perms.playerHas(player, Flags.use, true);
        ClaimedResidence res = ClaimedResidence.getByLoc(event.getBlock().getLocation());

        Flags result = FlagPermissions.getMaterialUseFlagList().get(event.getBlock().getType());
        if (result == null)
            return;
        if (perms.playerHas(player, result, hasUse))
            return;
        if (res != null && res.getRaid().isUnderRaid() && res.getRaid().isAttacker(player))
            return;

        switch (result) {
            case button:
                if (ResPerm.bypass_button.hasPermission(player, 10000L))
                    return;
                break;
        }

        event.setCancelled(true);
    }
}
    private void dropAndRemove(Entity ent, ItemStack item) {
        ent.getWorld().dropItemNaturally(ent.getLocation(), item.clone());
        ent.remove();
    }

}
