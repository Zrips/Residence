package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.Items.CMIMaterial;

public class ResidenceListener1_15 implements Listener {

    private Residence plugin;

    public ResidenceListener1_15(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractBeeHive(PlayerInteractEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        Material mat = block.getType();

        if (!mat.equals(Material.BEE_NEST) && !mat.equals(Material.BEEHIVE))
            return;

        ItemStack iih = event.getItem();
        CMIMaterial heldItem = CMIMaterial.get(iih);

        if (heldItem.equals(CMIMaterial.GLASS_BOTTLE)) {
            if (CMILib.getInstance().getReflectionManager().getHoneyLevel(block) < CMILib.getInstance().getReflectionManager().getMaxHoneyLevel(block)) {
                return;
            }
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
            if (res == null)
                return;
            if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.honey, FlagCombo.TrueOrNone)) {
                lm.Residence_FlagDeny.sendMessage(player, Flags.honey, res.getName());
                event.setCancelled(true);
                return;
            }
        }

        if (heldItem.equals(CMIMaterial.SHEARS)) {
            if (CMILib.getInstance().getReflectionManager().getHoneyLevel(block) < CMILib.getInstance().getReflectionManager().getMaxHoneyLevel(block)) {
                return;
            }
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
            if (res == null)
                return;
            if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.honeycomb, FlagCombo.TrueOrNone)) {
                lm.Residence_FlagDeny.sendMessage(player, Flags.honeycomb, res.getName());
                event.setCancelled(true);
                return;
            }
        }
    }
}
