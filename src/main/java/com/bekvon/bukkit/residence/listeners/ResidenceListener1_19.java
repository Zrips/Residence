package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

public class ResidenceListener1_19 implements Listener {

    private Residence plugin;

    public ResidenceListener1_19(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onUseGoatHorn(PlayerInteractEvent event) {
        if (event.useItemInHand() == Result.DENY) {
            return;
        }
        Player player = event.getPlayer();

        if (FlagPermissions.shouldIgnoreCheck(Flags.goathorn, player)) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.GOAT_HORN) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify(player, player, Flags.goathorn, null)) {
            event.setCancelled(true);
        }
    }
}
