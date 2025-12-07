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
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
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
        // Disabling listener if flag disabled globally
        if (!Flags.destroy.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        if (player == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(player.getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        Material mat = block.getType();

        if (!mat.equals(Material.BEE_NEST) && !mat.equals(Material.BEEHIVE))
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        ItemStack iih = event.getItem();
        CMIMaterial heldItem = CMIMaterial.get(iih);

        if (heldItem.equals(CMIMaterial.GLASS_BOTTLE)) {
            if (CMILib.getInstance().getReflectionManager().getHoneyLevel(block) < CMILib.getInstance().getReflectionManager().getMaxHoneyLevel(block)) {
                return;
            }

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            if (perms.playerHas(player, Flags.honey, perms.playerHas(player, Flags.build, true)))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.honey);
            event.setCancelled(true);
            return;
        }

        if (heldItem.equals(CMIMaterial.SHEARS)) {
            if (CMILib.getInstance().getReflectionManager().getHoneyLevel(block) < CMILib.getInstance().getReflectionManager().getMaxHoneyLevel(block)) {
                return;
            }

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            if (perms.playerHas(player, Flags.honeycomb, perms.playerHas(player, Flags.build, true)))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.honeycomb);
            event.setCancelled(true);
        }
    }
}
