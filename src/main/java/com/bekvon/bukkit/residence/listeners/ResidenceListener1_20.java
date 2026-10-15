package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_20 implements Listener {

    private Residence plugin;

    public ResidenceListener1_20(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerSignOpenEvent event) {

        Player player = event.getPlayer();

        if (FlagPermissions.shouldIgnoreCheck(Flags.build, player)) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify(player, event.getSign().getLocation(), Flags.build, Flags.use)) {
            event.setCancelled(true);
        }
    }

    // In versions below 1.20, canceling PlayerInteractEvent has no effect on Tridents with Riptide
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerUseRiptide(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.useItemInHand() == Result.DENY) {
            return;
        }

        Player player = event.getPlayer();

        if (FlagPermissions.shouldIgnoreCheck(Flags.riptide, player)) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.TRIDENT) {
            return;
        }

        if (!item.containsEnchantment(Enchantment.RIPTIDE)) {
            return;
        }

        boolean inRain = Version.isPaperBranch()
                ? player.isInRain()
                : player.getWorld().hasStorm();

        if (!inRain && !player.isInWater() && player.getPlayerWeather() != WeatherType.DOWNFALL) {
            return;
        }

        if (FlagPermissions.shouldDenyAndNotify(player, Flags.riptide)) {
            event.setCancelled(true);
        }
    }
}
