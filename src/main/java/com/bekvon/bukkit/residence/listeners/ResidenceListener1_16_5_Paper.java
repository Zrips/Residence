package com.bekvon.bukkit.residence.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Utils;

import io.papermc.paper.event.block.TargetHitEvent;

public class ResidenceListener1_16_5_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener1_16_5_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHitTargetBlock(TargetHitEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.use.isGlobalyEnabled())
            return;

        Block block = event.getHitBlock();
        if (block == null)
            return;

        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Player player = Utils.potentialProjectileToPlayer(event.getEntity());
        if (player != null) {

            if (ResAdmin.isResAdmin(player))
                return;

            if (FlagPermissions.has(block.getLocation(), player, Flags.use, true))
                return;

            lm.Flag_Deny.sendMessage(player, Flags.use);
            event.setCancelled(true);

        } else {
            // Entity not player source
            // Check potential block as a shooter which should be allowed if its inside same residence
            if (Utils.isSourceBlockInsideSameResidence(event.getEntity(), ClaimedResidence.getByLoc(block.getLocation())))
                return;

            if (FlagPermissions.has(block.getLocation(), Flags.use, true))
                return;

            event.setCancelled(true);

        }
    }
}
