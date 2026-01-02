package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

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
        if (!Flags.build.isGlobalyEnabled())
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Material mat = block.getType();

        if (mat != Material.BEE_NEST && mat != Material.BEEHIVE)
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        CMIMaterial heldItem = CMIMaterial.get(event.getItem());

        if (heldItem == CMIMaterial.GLASS_BOTTLE) {
            if (CMILib.getInstance().getReflectionManager().getHoneyLevel(block) < CMILib.getInstance().getReflectionManager().getMaxHoneyLevel(block)) {
                return;
            }

            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
            if (perms.playerHas(player, Flags.honey, perms.playerHas(player, Flags.build, true)))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.honey);
            event.setCancelled(true);

        } else if (heldItem == CMIMaterial.SHEARS) {
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
