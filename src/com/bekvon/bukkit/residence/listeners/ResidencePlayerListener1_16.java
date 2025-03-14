package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Version.Version;

public class ResidencePlayerListener1_16 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_16(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLightningStrikeEvent(LightningStrikeEvent event) {

        if (!event.getCause().equals(LightningStrikeEvent.Cause.TRIDENT))
            return;

        if (!Flags.animalkilling.isGlobalyEnabled())
            return;

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getWorld()))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getLightning().getLocation());

        if (res == null)
            return;

        if (res.getPermissions().has(Flags.animalkilling, FlagCombo.OnlyFalse))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteractRespawn(PlayerInteractEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        try {
            if (Version.isCurrentHigher(Version.v1_8_R3) && event.getHand() != EquipmentSlot.HAND)
                return;
        } catch (Exception e) {
        }
        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        Material mat = block.getType();

        if (mat.equals(Material.RESPAWN_ANCHOR)) {
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
            if (res == null)
                return;

            if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.anchor, FlagCombo.OnlyTrue) && !plugin.isResAdminOn(player)) {
                plugin.msg(player, lm.Residence_FlagDeny, Flags.anchor, res.getName());
                event.setCancelled(true);
            }
        } else if (mat.equals(Material.REDSTONE_WIRE)) {

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
            if (res == null)
                return;

            if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.build, FlagCombo.TrueOrNone) && !plugin.isResAdminOn(player)) {
                plugin.msg(player, lm.Residence_FlagDeny, Flags.build, res.getName());
                event.setCancelled(true);
            }
        }
    }
}
