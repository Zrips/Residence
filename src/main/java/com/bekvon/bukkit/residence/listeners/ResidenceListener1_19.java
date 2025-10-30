package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ResidenceListener1_19 implements Listener {

    private Residence plugin;

    public ResidenceListener1_19(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseGoatHorn(PlayerInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.goathorn.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        if (player == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;

        if (player.hasMetadata("NPC"))
            return;

        ItemStack horn = event.getItem();

        if (horn == null)
            return;

        if (!horn.getType().equals(Material.GOAT_HORN))
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(player.getLocation(), player);
        if (perms.playerHas(player, Flags.goathorn, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.goathorn);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.skulk.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        if (!Material.SCULK_CATALYST.equals(event.getSource().getType()))
            return;

        Location loc = event.getBlock().getLocation();
        FlagPermissions perms = FlagPermissions.getPerms(loc);
        if (!perms.has(Flags.skulk, true)) {
            event.setCancelled(true);
        }
    }

    private void breakHopper(Inventory hopperInventory) {
        Location hopperLoc = hopperInventory.getLocation();
        if (hopperLoc == null)
            return;
        // delay 1 tick break, ensure after event cancel
        CMIScheduler.runAtLocationLater(plugin, hopperLoc, () -> {
            Block block = hopperLoc.getBlock();
            // only hopper
            if (block == null || !(block.getType().equals(Material.HOPPER)))
                return;
            block.breakNaturally();
        }, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHopperCrossRes(InventoryMoveItemEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;

        Inventory source = event.getSource();
        Inventory dest = event.getDestination();
        if (source == null || dest == null) {
            return;
        }

        ClaimedResidence sourceRes = ClaimedResidence.getByLoc(source.getLocation());
        ClaimedResidence destRes = ClaimedResidence.getByLoc(dest.getLocation());

        // ignore source & dest not in Res
        if (sourceRes == null && destRes == null) {
            return;
        }
        // ignore source & dest in Same Res
        if (sourceRes != null && destRes != null && sourceRes.equals(destRes)) {
            return;
        }
        // source & dest not in Same Res
        if (sourceRes != null && destRes != null && !sourceRes.equals(destRes)) {
            event.setCancelled(true);
            return;
        }
        // source in Res, dest not in Res
        if (sourceRes != null && destRes == null) {
            event.setCancelled(true);
            breakHopper(dest);
            return;
        }
        // dest in Res, source not in Res
        if (sourceRes == null && destRes != null) {
            event.setCancelled(true);
            breakHopper(source);
        }
    }
}
