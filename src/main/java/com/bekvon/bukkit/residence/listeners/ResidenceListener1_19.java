package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_19 implements Listener {

    private Residence plugin;

    public ResidenceListener1_19(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onUseGoatHorn(PlayerInteractEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.goathorn.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        // disabling event on world
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (CMIMaterial.get(event.getItem()) != CMIMaterial.GOAT_HORN)
            return;

        if (player.hasMetadata("NPC") || ResAdmin.isResAdmin(player))
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHopperCrossRes(InventoryMoveItemEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;

        ClaimedResidence sourceRes = ClaimedResidence.getByLoc(event.getSource().getLocation());
        ClaimedResidence destRes = ClaimedResidence.getByLoc(event.getDestination().getLocation());

        // source & dest not in Res
        if (sourceRes == null && destRes == null)
            return;

        // source & dest in Res
        if (sourceRes != null && destRes != null) {

            // in Same Res, or have Same Res owner
            if (sourceRes.equals(destRes) || sourceRes.isOwner(destRes.getOwner()))
                return;

            // not in Same Res & not Same Res owner
            // hopper can be source or dest
            if (sourceRes.getPermissions().has(Flags.container, true) &&
                    destRes.getPermissions().has(Flags.container, true))
                return;

            // source in Res, destRes definitely not in Res
        } else if (sourceRes != null) {

            if (sourceRes.getPermissions().has(Flags.container, true))
                return;

            // dest definitely in Res, source definitely not in Res
        } else {

            if (destRes.getPermissions().has(Flags.container, true))
                return;

        }

        event.setCancelled(true);

    }

    // if Flag_riding is true
    // riding InventoryVehicle: check Flag_container when opening Vehicle Inventory
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerOpenVehicleInv(InventoryOpenEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;

        Player player = (Player) event.getPlayer();
        // disabling event on world
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;

        Entity vehicle = player.getVehicle();
        if (canHaveContainer1_19(vehicle)) {

            if (ResAdmin.isResAdmin(player))
                return;

            FlagPermissions perms = FlagPermissions.getPerms(vehicle.getLocation(), player);
            if (perms.playerHas(player, Flags.container, true))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.container);
            event.setCancelled(true);
        }
    }

    // Cover All 1.19+ Vehicles with an Inventory interface
    public static boolean canHaveContainer1_19(Entity entity) {
        if (entity == null) {
            return false;
        }
        if(Version.isCurrentEqualOrHigher(Version.v1_21_R7)) {
            return (entity instanceof AbstractHorse ||
                    entity instanceof ChestBoat ||
                    entity instanceof org.bukkit.entity.AbstractNautilus);
        }
        return (entity instanceof AbstractHorse || entity instanceof ChestBoat);
    }
}
