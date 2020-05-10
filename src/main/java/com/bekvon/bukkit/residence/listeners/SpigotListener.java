package com.bekvon.bukkit.residence.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

public class SpigotListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        FlagPermissions perms = Residence.getInstance().getPermsByLoc(loc);
        if (!perms.has(Flags.nodurability, false))
            return;

        ItemStack held = Residence.getInstance().getNms().itemInMainHand(player);
        if (held.getType() == Material.AIR || held.getDurability() <= 0)
            return;

        if (held.getType().toString().equalsIgnoreCase("TRIDENT")) {
            return;
        }

        held.setDurability(held.getDurability());
        player.setItemInHand(held);
        event.setDamage(0);
        event.setCancelled(true);
    }
}
