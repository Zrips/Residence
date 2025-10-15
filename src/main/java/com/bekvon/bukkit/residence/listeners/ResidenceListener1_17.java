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
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;

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

        if (ResidenceBlockListener.canPlaceBlock(event.getPlayer(), event.getBlock(), true))
            return;

        event.setCancelled(true);

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

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getEntity();

        ItemStack iih = event.getOriginalBucket();
        if (iih == null)
            return;

        if (!CMIMaterial.get(iih).equals(CMIMaterial.WATER_BUCKET))
            return;

        FlagPermissions perms = Residence.getInstance().getPermsByLocForPlayer(ent.getLocation(), player);

        if (!perms.playerHas(player, Flags.animalkilling, FlagCombo.TrueOrNone)) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.animalkilling);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChangeCopper(EntityChangeBlockEvent event) {

        Block block = event.getBlock();
        if (block == null)
            return;

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
        if (perms.playerHas(player, Flags.copper, perms.has(Flags.destroy, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.copper);

        event.setCancelled(true);

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {

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
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        Block block = event.getBlock();

        if (block == null)
            return;

        ClaimedResidence originRes = plugin.getResidenceManager().getByLoc(block.getLocation());

        List<BlockState> blocks = new ArrayList<BlockState>(event.getBlocks());

        Player player = event.getPlayer();

        if (ResPerm.bypass_build.hasPermission(player, 10000L))
            return;

        for (BlockState oneBlock : blocks) {
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(oneBlock.getLocation());
            if (res == null)
                continue;
            if (player != null) {
                FlagPermissions perms = Residence.getInstance().getPermsByLocForPlayer(oneBlock.getLocation(), player);
                if (!perms.playerHas(player, Flags.build, FlagCombo.TrueOrNone)) {
                    event.getBlocks().remove(oneBlock);
                }
            } else if (originRes == null || !originRes.equals(res)) {
                event.getBlocks().remove(oneBlock);
            }
        }

    }
}
