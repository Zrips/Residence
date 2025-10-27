package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Utils;

public class ResidenceListener1_08 implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtArmoStand(PlayerInteractAtEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(player.getWorld()))
            return;

        Entity ent = event.getRightClicked();
        if (!Utils.isArmorStandEntity(ent.getType()))
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation(), player);

        if (!perms.playerHas(player, Flags.container, perms.playerHas(player, Flags.use, true))) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.container);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void AnimalUnleash(PlayerUnleashEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.leash.isGlobalyEnabled())
            return;

        Entity entity = event.getEntity();
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(entity.getWorld()))
            return;

        Player player = event.getPlayer();

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation(), player);
        if (perms.playerHas(player, Flags.leash, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.leash);

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.explode.isGlobalyEnabled())
            return;

        Location loc = event.getBlock().getLocation();
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(loc.getWorld()))
            return;

        FlagPermissions world = FlagPermissions.getPerms(loc.getWorld());
        List<Block> preserve = new ArrayList<Block>();
        for (Block block : event.blockList()) {
            FlagPermissions blockperms = FlagPermissions.getPerms(block.getLocation());
            if (!blockperms.has(Flags.explode, world.has(Flags.explode, true))) {
                preserve.add(block);
            }
        }
        for (Block block : preserve) {
            event.blockList().remove(block);
        }
    }
}
