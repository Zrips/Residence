package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.AnvilInventory;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Utils;
import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.entity.EntityZapEvent;

import net.Zrips.CMILib.Version.Version;

import io.papermc.paper.event.block.TargetHitEvent;

public class ResidenceListener1_16_5_Paper implements Listener {

    private final Residence plugin;

    public ResidenceListener1_16_5_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHitTargetBlock(TargetHitEvent event) {

        Block block = event.getHitBlock();
        if (block == null) {
            return;
        }
        if (FlagPermissions.shouldIgnoreCheck(Flags.use, block)) {
            return;
        }
        if (ResidenceEntityListener.shouldDenyEntityHit(event.getEntity(), block.getLocation(), Flags.use, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityZapEvent(EntityZapEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, entity)) {
            return;
        }
        // Check if animal can be damaged or converted by lightning
        if (!(entity instanceof LivingEntity) || !Utils.isAnimal(entity)) {
            return;
        }
        Player player = Version.isCurrentEqualOrHigher(Version.v1_20_2)
                ? event.getBolt().getCausingPlayer()
                : null;

        if (player != null) {
            if (ResAdmin.isResAdmin(player)) {
                return;
            }
            if (FlagPermissions.has(entity.getLocation(), player, Flags.animalkilling, true)) {
                return;
            }
        } else {
            if (FlagPermissions.has(entity.getLocation(), Flags.animalkilling, true)) {
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnvilDamaged(AnvilDamagedEvent event) {

        AnvilInventory anvilInv = event.getInventory();

        Location loc = anvilInv.getLocation();
        if (loc == null) {
            return;
        }
        if (FlagPermissions.shouldIgnoreCheck(Flags.anvilbreak, loc.getWorld())) {
            return;
        }
        if (FlagPermissions.has(loc, Flags.anvilbreak, true)) {
            return;
        }
        event.setCancelled(true);
    }
}
