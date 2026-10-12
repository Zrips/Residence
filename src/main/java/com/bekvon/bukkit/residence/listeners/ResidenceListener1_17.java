package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidenceBlockData;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_17 implements Listener {

    private final Residence plugin;

    public ResidenceListener1_17(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEntityEvent(PlayerBucketEntityEvent event) {

        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, entity)) {
            return;
        }
        if (FlagPermissions.shouldDenyAndNotify(event.getPlayer(), entity, Flags.animalkilling, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCopperOxidation(BlockFormEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.copperoxidation, block)) {
            return;
        }
        if (!isUnwaxedCopper(block)) {
            return;
        }
        if (FlagPermissions.has(block.getLocation(), Flags.copperoxidation, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
        }
    }

    private boolean isUnwaxedCopper(Block block) {
        CMIMaterial mat = CMIMaterial.get(block.getType());
        return mat.containsCriteria(CMIMC.COPPER) && !mat.name().startsWith("WAXED_");
    }

    public static void handlePowderSnow(Block block) {
        if (block.getType() == Material.POWDER_SNOW) {
            ResidenceBlockData.updatePowderedSnow(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPowderSnowPhysics(BlockPhysicsEvent event) {
        // https://github.com/PaperMC/Paper/pull/6751
        if (Version.isPaperBranch() && Version.isCurrentEqualOrHigher(Version.v1_18_2)) {
            return;
        }
        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.place, block)) {
            return;
        }
        Block sourceBlock = event.getSourceBlock();

        if (sourceBlock.getType() != Material.POWDER_SNOW || block.getType() == Material.AIR || block.getType() == Material.POWDER_SNOW) {
            return;
        }
        Location blockLoc = block.getLocation();

        if (blockLoc.getY() == sourceBlock.getLocation().getY()) {
            return;
        }
        if (ClaimedResidence.getByLoc(blockLoc) != null) {
            ResidenceBlockData.addPowderedSnow(sourceBlock, block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFertilizeEvent(BlockFertilizeEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.build, block)) {
            return;
        }
        if (ResidenceBlockListener.handlePlantGrowCrossResidence(event.getBlocks(), block.getLocation(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
