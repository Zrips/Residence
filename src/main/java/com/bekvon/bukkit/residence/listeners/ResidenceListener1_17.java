package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_17 implements Listener {

    private Residence plugin;

    public ResidenceListener1_17(Residence plugin) {
        this.plugin = plugin;
    }

    private static int MAX_ENTRIES = 50;
    public static LinkedHashMap<String, BlockData> powder_snow = new LinkedHashMap<String, BlockData>(MAX_ENTRIES + 1, .75F, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BlockData> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.place.isGlobalyEnabled())
            return;

        if (ResidenceBlockListener.canPlaceBlock(event.getPlayer(), event.getBlock(), true))
            return;

        event.setCancelled(true);

        // https://github.com/PaperMC/Paper/pull/6751
        if (Version.isPaperBranch() && Version.isCurrentEqualOrHigher(Version.v1_18_R2))
            return;

        if (event.getBlock().getType() != Material.POWDER_SNOW)
            return;

        BlockData data = ResidenceListener1_17.powder_snow.remove(event.getBlock().getLocation().toString());
        if (data == null)
            return;

        Block blockUnder = event.getBlock().getLocation().clone().add(0, -1, 0).getBlock();

        if (data.getMaterial().equals(blockUnder.getType())) {
            blockUnder.setBlockData(data);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEntityEvent(PlayerBucketEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.animalkilling.isGlobalyEnabled())
            return;

        Entity ent = event.getEntity();
        // disabling event on world
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        if (FlagPermissions.has(ent.getLocation(), player, Flags.animalkilling, FlagCombo.OnlyFalse)) {
            lm.Flag_Deny.sendMessage(player, Flags.animalkilling);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChangeCopper(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.copper.isGlobalyEnabled())
            return;

        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (!CMIMaterial.isCopperBlock(block.getType()))
            return;

        Player player = (Player) event.getEntity();

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation(), player);
        if (perms.playerHas(player, Flags.copper, perms.playerHas(player, Flags.build, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.copper);

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPowderSnowPhysics(BlockPhysicsEvent event) {
        // https://github.com/PaperMC/Paper/pull/6751
        if (Version.isPaperBranch() && Version.isCurrentEqualOrHigher(Version.v1_18_R2))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.place.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        if (!event.getSourceBlock().getType().equals(Material.POWDER_SNOW) || event.getBlock().getType().equals(Material.AIR) || event.getBlock().getType().equals(Material.POWDER_SNOW))
            return;

        Block block = event.getBlock();
        if (block == null)
            return;

        if (block.getLocation().getY() == event.getSourceBlock().getLocation().getY())
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
        if (res == null)
            return;

        powder_snow.put(event.getSourceBlock().getLocation().toString(), block.getBlockData().clone());

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFertilizeEvent(BlockFertilizeEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.build.isGlobalyEnabled())
            return;
        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block.getWorld()))
            return;

        Player player = event.getPlayer();

        if (player != null) {
            if (ResPerm.bypass_build.hasPermission(player, 10000L))
                return;
            // cancel event if player has no build permission at click-position
            // non-saplings don't consume bone_meal on event cancel
            if (FlagPermissions.has(block.getLocation(), player, Flags.build, FlagCombo.OnlyFalse)) {
                lm.Flag_Deny.sendMessage(player, Flags.build);
                event.setCancelled(true);
                return;
            }
        }
        // player has build permission at click position, or event is not triggered by
        // player
        // check build permission for spread blocks
        ClaimedResidence originRes = ClaimedResidence.getByLoc(block.getLocation());

        List<BlockState> blocks = new ArrayList<BlockState>(event.getBlocks());

        for (BlockState oneBlock : blocks) {
            ClaimedResidence spreadRes = ClaimedResidence.getByLoc(oneBlock.getLocation());
            // spread-block not in Res, skip check
            // origin & spread-block in Same Res, or have Same Res owner, skip check
            if (spreadRes == null ||
                    (originRes != null && (originRes.equals(spreadRes) || originRes.isOwner(spreadRes.getOwner()))))
                continue;

            // origin & spread-block not in Same Res, not Same Res owner

            if (player != null) {
                if (spreadRes.getPermissions().playerHas(player, Flags.build, FlagCombo.OnlyFalse))
                    event.getBlocks().remove(oneBlock);

            } else if (spreadRes.getPermissions().has(Flags.build, FlagCombo.OnlyFalse)) {
                event.getBlocks().remove(oneBlock);

            }
        }

    }
}
