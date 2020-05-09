package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.residence.Residence;

public class ResidenceFixesListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAnvilPlace(PlayerInteractEvent event) {
	// disabling event on world
	if (Residence.getInstance().isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
	    return;
	ItemStack iih = event.getItem();
	if (iih == null || iih.getType() == Material.AIR || iih.getType() != Material.ANVIL)
	    return;
	BlockFace face = event.getBlockFace();
	Block bclicked = event.getClickedBlock();
	if (bclicked == null)
	    return;
	Location loc = new Location(bclicked.getWorld(), bclicked.getX() + face.getModX(), bclicked.getY() + face.getModY(),
	    bclicked.getZ() + face.getModZ());
	Block block = loc.getBlock();
	CMIMaterial mat = CMIMaterial.get(block);
	if (block == null || block.getType() == Material.AIR || !mat.isSkull() && block.getType() != Material.FLOWER_POT)
	    return;
	event.setCancelled(true);
    }
}
