package com.bekvon.bukkit.residence.listeners;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.commands.auto;
import com.bekvon.bukkit.residence.containers.ResidenceBlockData;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Container.CMIBlock;
import net.Zrips.CMILib.Container.CMIVectorInt3D;
import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;

import org.jspecify.annotations.Nullable;

public class ResidenceBlockListener implements Listener {

    private final List<UUID> MessageInformed = new ArrayList<>();

    private final Residence plugin;

    public ResidenceBlockListener(Residence residence) {
        this.plugin = residence;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnvilInventoryClick(InventoryClickEvent e) {
        // Paper 1.16.5+ uses AnvilDamagedEvent
        if (Version.isCurrentEqualOrHigher(Version.v1_16_5) && Version.isPaperBranch()) {
            return;
        }
        // Disabling listener if flag disabled globally
        if (!Flags.anvilbreak.isGlobalyEnabled())
            return;
        Inventory inv = e.getInventory();
        try {
            if (inv == null || inv.getType() != InventoryType.ANVIL || e.getInventory().getLocation() == null)
                return;
        } catch (Exception | NoSuchMethodError ex) {
            return;
        }
        Block b = e.getInventory().getLocation().getBlock();
        if (b == null || !CMIMaterial.isAnvil(b.getType()))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(e.getInventory().getLocation());
        if (res == null)
            return;

        // Fix anvil only when item is picked up
        if (e.getRawSlot() != 2)
            return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            return;
        if (!res.getPermissions().has(Flags.anvilbreak, FlagCombo.OnlyFalse))
            return;

        if (Version.isCurrentLower(Version.v1_13_0)) {
            try {
                b.getClass().getMethod("setData", byte.class).invoke(b, (byte) 1);
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        } else {
            ResidenceBlockData.updateAnvilFacing(b);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.grow, block)) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        if (!perms.has(Flags.grow, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {

        Block block = event.getBlock();

        if (plugin.isDisabledWorldListener(block)) {
            return;
        }
        Block source = event.getSource();
        CMIMaterial type = CMIMaterial.get(source.getType());

        if (Flags.grow.isGlobalyEnabled() && (type == CMIMaterial.VINE || type.name().contains("_VINES"))) {
            if (FlagPermissions.has(block.getLocation(), Flags.grow, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                return;
            }

        } else if (Flags.skulk.isGlobalyEnabled() && type == CMIMaterial.SCULK_CATALYST) {
            if (FlagPermissions.has(block.getLocation(), Flags.skulk, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                return;
            }

        } else if (Flags.spread.isGlobalyEnabled()) {
            if (FlagPermissions.has(block.getLocation(), Flags.spread, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                return;
            }

        }
        // Prevent griefers from introducing vines, sculk blocks, etc. from outside into Residences
        if (Flags.build.isGlobalyEnabled() && type != CMIMaterial.GRASS_BLOCK && type != CMIMaterial.MYCELIUM) {
            ClaimedResidence sourceRes = ClaimedResidence.getByLoc(source.getLocation());
            if (shouldDenySpread(event.getNewState(), sourceRes, null)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLeaveDecay(LeavesDecayEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.decay, block)) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        if (!perms.has(Flags.decay, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {

        if (plugin.isDisabledWorldListener(event.getWorld())) {
            return;
        }
        if (Flags.glow.isGlobalyEnabled()) {
            if (FlagPermissions.has(event.getLocation(), Flags.grow, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                return;
            }
        }
        // Prevent griefers from placing tree-related blocks from outside into Residences
        if (Flags.build.isGlobalyEnabled()) {
            if (handlePlantGrowCrossResidence(event.getBlocks(), event.getLocation(), event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }

    public static boolean handlePlantGrowCrossResidence(@NotNull List<BlockState> spreadList, @NotNull Location sourceLoc, @Nullable Player player) {

        if (player != null) {
            if (ResPerm.bypass_build.hasPermission(player, 10000L)) {
                return false;
            }
            // cancel the event if the player lacks build permission at the source location
            if (FlagPermissions.shouldDenyAndNotify(player, sourceLoc, Flags.build, null)) {
                return true;
            }
        }
        // player has build permission at the source location, or event is not player-triggered
        // check build permission for spread blocks
        ClaimedResidence sourceRes = ClaimedResidence.getByLoc(sourceLoc);
        spreadList.removeIf(spread -> shouldDenySpread(spread, sourceRes, player));
        return false;
    }

    private static boolean shouldDenySpread(@NotNull BlockState spread, @Nullable ClaimedResidence sourceRes, @Nullable Player player) {

        ClaimedResidence spreadRes = ClaimedResidence.getByLoc(spread.getLocation());
        // spread-block not in Res, skip check
        if (spreadRes == null) {
            return false;
        }
        // source & spread-block in same Res, or have same Res owner, skip check
        if (sourceRes != null && (sourceRes == spreadRes || sourceRes.isOwner(spreadRes.getOwner()))) {
            return false;
        }
        // source & spread-block not in same Res, not same Res owner
        if (player != null) {
            return spreadRes.getPermissions().playerHas(player, Flags.build, FlagCombo.OnlyFalse);
        } else {
            return spreadRes.getPermissions().has(Flags.build, FlagCombo.OnlyFalse);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {

        if (!Flags.destroy.isGlobalyEnabled()) {
            return;
        }
        if (!canBreakBlock(event.getPlayer(), event.getBlock(), true)) {
            event.setCancelled(true);
        }
    }

    public static boolean canBreakBlock(@NotNull Player player, @NotNull Block block, boolean inform) {
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(block)) {
            return true;
        }
        if (ResAdmin.isResAdmin(player)) {
            return true;
        }
        ClaimedResidence res = ClaimedResidence.getByLoc(block.getLocation());
        FlagPermissions perms;

        if (res != null) {
            if (Residence.getInstance().getConfigManager().enabledRentSystem()
                    && Residence.getInstance().getConfigManager().preventRentModify()
                    && res.isRented()) {
                if (inform) {
                    lm.Rent_ModifyDeny.sendMessage(player);
                }
                return false;
            }
            // In Residence, get Residence permission status
            perms = res.getPermissions();
        } else {
            // Not in Residence, get World permission status
            perms = Residence.getInstance().getWorldFlags().getPerms(player);
        }
        boolean canBreak = perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true));
        // Residence raid active (not vanilla raid)
        if (res != null && ConfigManager.RaidEnabled && res.getRaid().isUnderRaid()) {
            if ((ConfigManager.RaidAttackerBlockBreak && res.getRaid().isAttacker(player.getUniqueId()))
                    || (ConfigManager.RaidDefenderBlockBreak && res.getRaid().isDefender(player.getUniqueId()))) {
                canBreak = true;
            }
        }
        if (canBreak) {
            return true;
        }
        Material mat = block.getType();

        if (Residence.getInstance().getItemManager().isIgnored(player, mat, block.getWorld())) {
            return true;
        }
        if (res != null && res.getItemIgnoreList().isListed(mat)) {
            return true;
        }
        if (ResPerm.bypass_destroy.hasPermission(player, 10000L)) {
            // With destroy bypass, breaking chests still requires container permission
            if (Utils.isContainer(mat) && !perms.playerHas(player, Flags.container, true)) {
                if (inform) {
                    lm.Flag_Deny.sendMessage(player, Flags.container);
                }
                return false;
            }
            return true;
        }
        if (inform) {
            lm.Flag_Deny.sendMessage(player, Flags.destroy);
        }
        return false;
    }

    public static boolean canBreakBlock(@NotNull Player player, @NotNull Location loc, boolean inform) {
        return canBreakBlock(player, loc.getBlock(), inform);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {

        Block block = event.getBlock();

        if (plugin.isDisabledWorldListener(block)) {
            return;
        }
        Entity entity = event.getEntity();

        if (Flags.build.isGlobalyEnabled() && entity instanceof Player) {
            Player player = (Player) entity;
            // Prevent Frosted Ice generation from Frost Walker without notifying players
            if (FlagPermissions.shouldDenyAndNotify(player, block, Flags.build, null, false)) {
                event.setCancelled(true);
            }

        } else if (Flags.snowtrail.isGlobalyEnabled() && entity instanceof Snowman) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            if (!perms.has(Flags.snowtrail, true)) {
                event.setCancelled(true);
            }

        } else if (Flags.animalgriefing.isGlobalyEnabled() && Utils.isAnimal(entity)) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            if (!perms.has(Flags.animalgriefing, perms.has(Flags.build, true))) {
                event.setCancelled(true);
            }

        } else if (Flags.mobgriefing.isGlobalyEnabled() && Utils.isMonster(entity)) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            if (!perms.has(Flags.mobgriefing, perms.has(Flags.build, true))) {
                event.setCancelled(true);
            }

        } else if (Flags.build.isGlobalyEnabled()) {
            FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
            if (!perms.has(Flags.build, true)) {
                event.setCancelled(true);
            }

        }
    }

    private boolean isNotIceOrSnow(Material material) {
        switch (CMIMaterial.get(material)) {
        case FROSTED_ICE:
        case ICE:
        case SNOW:
            return false;
        default:
            return true;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceForm(BlockFormEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.iceform, block)) {
            return;
        }
        // SnowGolem already has SnowTrail Flag
        if (event instanceof EntityBlockFormEvent
                && ((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
            return;
        }
        if (isNotIceOrSnow(event.getNewState().getType())) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        if (!perms.has(Flags.iceform, true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceMelt(BlockFadeEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.icemelt, block)) {
            return;
        }
        if (isNotIceOrSnow(block.getType())) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        if (!perms.has(Flags.icemelt, true)) {
            event.setCancelled(true);
        }
    }

    public static final String SourceResidenceName = "SourceResidenceName";

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.fallinprotection.isGlobalyEnabled())
            return;
        if (event.getEntityType() != EntityType.FALLING_BLOCK)
            return;
        Entity ent = event.getEntity();

        if (!ent.hasMetadata(SourceResidenceName) && /*
                                                      * Equals to air when generic falling block is spawned, not when falling block
                                                      * originates from spawnegg
                                                      */ event.getTo() == Material.AIR) {

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
            String resName = res == null ? "NULL" : res.getName();
            ent.setMetadata(SourceResidenceName, new FixedMetadataValue(plugin, resName));
            return;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());

        if (res != null && res.getPermissions().has(Flags.fallinprotection, FlagCombo.OnlyFalse))
            return;

        String resName = res == null ? "NULL" : res.getName();

        String saved = "NULL";
        if (!ent.hasMetadata(SourceResidenceName))
            return;

        @NotNull
        List<MetadataValue> meta = ent.getMetadata(SourceResidenceName);

        if (meta.isEmpty())
            return;

        saved = ent.getMetadata(SourceResidenceName).get(0).asString();

        if (res == null || saved.equalsIgnoreCase(resName))
            return;

        event.setCancelled(true);
        ent.remove();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFall(EntityChangeBlockEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.fallinprotection, block)) {
            return;
        }
        if (!plugin.getConfigManager().isBlockFall())
            return;

        if ((event.getEntityType() != EntityType.FALLING_BLOCK))
            return;

        Material typeTo = event.getTo();
        if (typeTo.hasGravity() || CMIMaterial.get(typeTo).equals(CMIMaterial.SCAFFOLDING))
            return;

        if (!plugin.getConfigManager().getBlockFallWorlds().contains(block.getLocation().getWorld().getName()))
            return;

        if (block.getY() <= plugin.getConfigManager().getBlockFallLevel())
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());

        if (res != null)
            event.getEntity().setMetadata(SourceResidenceName, new FixedMetadataValue(plugin, res.getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;
        if (!plugin.getConfigManager().ShowNoobMessage())
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;
        Block block = event.getBlock();
        if (!CMIMaterial.get(block.getType()).containsCriteria(CMIMC.CHEST))
            return;

        if (plugin.getPlayerManager().getResidenceCount(player.getUniqueId()) != 0)
            return;

        if (MessageInformed.contains(player.getUniqueId()))
            return;

        if (!ResPerm.newguyresidence.hasPermission(player))
            return;

        lm.General_NewPlayerInfo.sendMessage(player);

        MessageInformed.add(player.getUniqueId());
    }

    private boolean checkBlock(ClaimedResidence orRes, Location loc, Vector offset, Material type, Player player) {
        Block b = loc.clone().add(offset).getBlock();
        if (b.getType() != type)
            return false;
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(b.getLocation());
        return res != null && !res.equals(orRes) && !res.isOwner(player) && !res.isTrusted(player);
    }

    private static final List<Vector> chestVectors = new ArrayList<Vector>(Arrays.asList(
            new Vector(0, 0, -1),
            new Vector(0, 0, 1),
            new Vector(1, 0, 0),
            new Vector(-1, 0, 0)));

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChestPlaceNearResidence(BlockPlaceEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Block block = event.getBlock();

        Material type = block.getType();

        if (!CMIMaterial.get(type).containsCriteria(CMIMC.CHEST))
            return;

        ClaimedResidence orRes = plugin.getResidenceManager().getByLoc(block.getLocation());

        for (Vector vector : chestVectors) {
            if (!checkBlock(orRes, block.getLocation(), vector, type, player))
                continue;

            CMIActionBar.send(player, lm.General_CantPlaceChest.getMessage());
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlaceCreateRes(BlockPlaceEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;
        if (!plugin.getConfigManager().isNewPlayerUse())
            return;

        Player player = event.getPlayer();

        if (ResAdmin.isResAdmin(player))
            return;

        Block block = event.getBlock();

        if (!CMIMaterial.get(block.getType()).containsCriteria(CMIMC.CHEST))
            return;

        ResidencePlayer rp = ResidencePlayer.get(player);

        if (rp.getResAmount() > 0 || rp.getData().ownedResidence())
            return;

        Location loc = block.getLocation();

        plugin.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), loc.getBlockX() - 1, loc.getBlockY() - 1, loc.getBlockZ() - 1), true);
        plugin.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), loc.getBlockX() + 1, loc.getBlockY() + 1, loc.getBlockZ() + 1), true);

        CMIVectorInt3D max = new CMIVectorInt3D(plugin.getConfigManager().getNewPlayerRangeX() * 2,
                plugin.getConfigManager().getNewPlayerRangeY() * 2,
                plugin.getConfigManager().getNewPlayerRangeZ() * 2);

        auto.optimizedResize(player, plugin.getSelectionManager().getSelectionCuboid(player), !plugin.getConfigManager().isNewPlayerFree(), max);

        boolean created = plugin.getResidenceManager().addResidence(player, player.getName(), plugin.getSelectionManager().getPlayerLoc1(player),
                plugin.getSelectionManager().getPlayerLoc2(player), plugin.getConfigManager().isNewPlayerFree());
        if (created) {
            rp.getData().ownedResidence(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {

        if (!Flags.place.isGlobalyEnabled()) {
            return;
        }
        Block block = event.getBlock();

        if (canPlaceBlock(event.getPlayer(), block, true)) {
            return;
        }
        event.setCancelled(true);
        // Powder snow placement issue: https://github.com/Zrips/Residence/issues/784
        // Paper 1.18.2+ already fixed this: https://github.com/PaperMC/Paper/pull/6751
        if (Version.isCurrentEqualOrHigher(Version.v1_17_0)
                && !(Version.isPaperBranch() && Version.isCurrentEqualOrHigher(Version.v1_18_2))) {
            ResidenceListener1_17.handlePowderSnow(block);
        }
    }

    public static boolean canPlaceBlock(@NotNull Player player, @NotNull Block block, boolean informPlayer) {
        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(block)) {
            return true;
        }
        if (ResAdmin.isResAdmin(player)) {
            return true;
        }
        ClaimedResidence res = ClaimedResidence.getByLoc(block.getLocation());
        CMIMaterial type = null;
        Material mat = block.getType();
        FlagPermissions perms;

        if (res != null) {
            if (Residence.getInstance().getConfigManager().enabledRentSystem()
                    && Residence.getInstance().getConfigManager().preventRentModify()
                    && Residence.getInstance().getRentManager().isRented(res)) {
                if (informPlayer) {
                    lm.Rent_ModifyDeny.sendMessage(player);
                }
                return false;
            }
            type = CMIMaterial.get(mat);
            // Residence item blacklist
            if (!type.isNone() && !res.getItemBlacklist().isAllowed(mat)) {
                if (informPlayer) {
                    lm.General_ItemBlacklisted.sendMessage(player);
                }
                return false;
            }
            // In Residence, get Residence permission status
            perms = res.getPermissions();
        } else {
            // Not in Residence, get World permission status
            perms = Residence.getInstance().getWorldFlags().getPerms(player);
        }
        boolean canPlace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
        // Residence raid active (not vanilla raid)
        if (res != null && ConfigManager.RaidEnabled && res.getRaid().isUnderRaid()) {
            if ((ConfigManager.RaidAttackerBlockPlace && res.getRaid().isAttacker(player.getUniqueId()))
                    || (ConfigManager.RaidDefenderBlockPlace && res.getRaid().isDefender(player.getUniqueId()))) {
                canPlace = true;
            }
        }
        if (canPlace) {
            if (type == null) {
                type = CMIMaterial.get(mat);
            }
            if (type.containsCriteria(CMIMC.BED) && !ResPerm.bypass_build.hasPermission(player, 10000L)) {
                return canPlaceBedOtherHalf(block, player);
            }
            return true;
        }
        if (Residence.getInstance().getItemManager().isIgnored(player, mat, block.getWorld())) {
            return true;
        }
        if (ResPerm.bypass_build.hasPermission(player, 10000L)) {
            return true;
        }
        if (informPlayer) {
            lm.Flag_Deny.sendMessage(player, Flags.place);
        }
        return false;
    }

    private static boolean canPlaceBedOtherHalf(Block block, Player player) {
        Block bed = new CMIBlock(block).getSecondaryBedBlock();
        if (bed == null) {
            return true;
        }
        FlagPermissions perms = FlagPermissions.getPerms(bed.getLocation(), player);
        return perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;

        // Disabling listener if flag disabled globally
        if (!Flags.piston.isGlobalyEnabled())
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation());
        if (!perms.has(Flags.piston, true)) {
            event.setCancelled(true);
            return;
        }

        // Disabling listener if flag disabled globally
        if (!Flags.pistonprotection.isGlobalyEnabled())
            return;

        List<Block> blocks = Utils.getPistonRetractBlocks(event);

        if (!event.isSticky())
            return;

        ClaimedResidence pistonRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

        for (Block block : blocks) {
            Location locFrom = block.getLocation();
            ClaimedResidence blockFrom = plugin.getResidenceManager().getByLoc(locFrom);
            if (blockFrom == null)
                continue;
            if (blockFrom == pistonRes)
                continue;
            if (pistonRes != null && blockFrom.isOwner(pistonRes.getOwner()))
                continue;
            if (!blockFrom.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue))
                continue;
            event.setCancelled(true);
            break;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;

        // Disabling listener if flag disabled globally
        if (!Flags.piston.isGlobalyEnabled())
            return;
        FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation());
        if (!perms.has(Flags.piston, true)) {
            event.setCancelled(true);
            return;
        }

        // Disabling listener if flag disabled globally
        if (!Flags.pistonprotection.isGlobalyEnabled())
            return;

        Location origins = event.getBlock().getLocation();

        int lowestY = CMIWorld.getMaxHeight(origins.getWorld());
        int bigestY = CMIWorld.getMinHeight(origins.getWorld());
        int lowestX = Integer.MAX_VALUE;
        int lowestZ = Integer.MAX_VALUE;
        int bigestX = -Integer.MAX_VALUE;
        int bigestZ = -Integer.MAX_VALUE;

        BlockFace dir = event.getDirection();

        for (Block block : event.getBlocks()) {
            Location one = block.getLocation().clone().add(dir.getModX(), dir.getModY(), dir.getModZ());
            if (one.getBlockY() < lowestY)
                lowestY = one.getBlockY();
            if (one.getBlockX() < lowestX)
                lowestX = one.getBlockX();
            if (one.getBlockZ() < lowestZ)
                lowestZ = one.getBlockZ();
            if (one.getBlockY() > bigestY)
                bigestY = one.getBlockY();
            if (one.getBlockX() > bigestX)
                bigestX = one.getBlockX();
            if (one.getBlockZ() > bigestZ)
                bigestZ = one.getBlockZ();
        }

        ClaimedResidence pistonRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

        if (pistonRes != null && pistonRes.containsLoc(new Location(origins.getWorld(), lowestX, lowestY, lowestZ))
                && pistonRes.containsLoc(new Location(origins.getWorld(), bigestX, bigestY, bigestZ))) {
            return;
        }

        for (int i = event.getBlocks().size() - 1; i >= 0; i--) {
            Block block = event.getBlocks().get(i);
            Location locTo = block.getLocation().clone().add(dir.getModX(), dir.getModY(), dir.getModZ());
            ClaimedResidence blockTo = plugin.getResidenceManager().getByLoc(locTo);
            boolean hasPerm = blockTo != null && blockTo.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue);
            if (pistonRes == null && hasPerm || blockTo != null && pistonRes != null && !blockTo.isOwner(pistonRes.getOwner()) && hasPerm) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;

        ClaimedResidence fromRes = ClaimedResidence.getByLoc(event.getBlock().getLocation());
        ClaimedResidence toRes = ClaimedResidence.getByLoc(event.getToBlock().getLocation());

        FlagPermissions perms = FlagPermissions.getPerms(event.getToBlock().getLocation());
        boolean hasflow = perms.has(Flags.flow, FlagCombo.TrueOrNone);
        Material mat = event.getBlock().getType();

        if (perms.has(Flags.flowinprotection, FlagCombo.TrueOrNone))
            if (fromRes == null && toRes != null || fromRes != null && toRes != null && !fromRes.equals(toRes) && !fromRes.isOwner(toRes.getOwner())) {
                event.setCancelled(true);
                return;
            }

        if (perms.has(Flags.flow, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }

        if (mat == Material.LAVA) {
            if (!perms.has(Flags.lavaflow, hasflow)) {
                event.setCancelled(true);
            }
            return;
        }
        if (mat == Material.WATER) {
            if (!perms.has(Flags.waterflow, hasflow)) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryFade(BlockFadeEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.dryup, block)) {
            return;
        }
        // Moved to separate class
        if (Version.isCurrentEqualOrHigher(Version.v1_13_0)) {
            if (ResidenceListener1_13.shouldCancelFarmLandChange(block)) {
                event.setCancelled(true);
            }
            return;
        }
        CMIMaterial mat = CMIMaterial.get(block.getType());
        if (!mat.equals(CMIMaterial.FARMLAND))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(event.getNewState().getLocation());
        if (!perms.has(Flags.dryup, true)) {
            try {
                byte value = (byte) block.getClass().getMethod("getData").invoke(block);
                if (value < (byte) 2)
                    block.getClass().getMethod("setData", byte.class).invoke(block, (byte) 7);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.dryup, block)) {
            return;
        }
        // Moved to separate class
        if (Version.isCurrentEqualOrHigher(Version.v1_13_0)) {
            if (ResidenceListener1_13.shouldCancelFarmLandChange(block)) {
                event.setCancelled(true);
            }
            return;
        }
        if (!block.getWorld().isChunkLoaded((int) Math.floor(block.getLocation().getX()) >> 4, ((int) Math.floor(block.getLocation().getZ()) >> 4))) {
            return;
        }
        CMIMaterial mat = CMIMaterial.get(block.getType());
        if (!mat.equals(CMIMaterial.FARMLAND))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(block.getLocation());
        if (perms.has(Flags.dryup, FlagCombo.OnlyFalse)) {
            try {
                byte value = (byte) block.getClass().getMethod("getData").invoke(block);
                if (value < (byte) 2)
                    block.getClass().getMethod("setData", byte.class).invoke(block, (byte) 7);
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
            event.setCancelled(true);
            return;
        }
    }

    @SuppressWarnings("removal")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.build, block)) {
            return;
        }
        if (CMIMaterial.get(block.getType()) != CMIMaterial.DISPENSER)
            return;

        // target location
        Location targetLoc = Version.isCurrentEqualOrHigher(Version.v1_13_0)
                ? ResidenceBlockData.getRelative(block)
                : block.getRelative((((org.bukkit.material.Dispenser) ((org.bukkit.block.Dispenser) block).getData()).getFacing())).getLocation();

        ClaimedResidence targetRes = ClaimedResidence.getByLoc(targetLoc);

        CMIMaterial cmat = CMIMaterial.get(event.getItem());
        if (targetRes == null && targetLoc.getBlockY() >= plugin.getConfigManager().getPlaceLevel() && plugin.getConfigManager().getNoPlaceWorlds().contains(targetLoc
                .getWorld().getName())) {
            if (plugin.getConfigManager().isNoLavaPlace() && cmat == CMIMaterial.LAVA_BUCKET) {
                event.setCancelled(true);
                return;
            }

            if (plugin.getConfigManager().isNoWaterPlace() &&
                    (cmat == CMIMaterial.WATER_BUCKET || cmat.containsCriteria(CMIMC.BUCKETANIMAL))) {
                event.setCancelled(true);
                return;
            }
        }

        if (targetRes == null)
            return;

        ClaimedResidence sourceRes = ClaimedResidence.getByLoc(block.getLocation());

        // source & target in Same Res, or have Same Res owner
        if (sourceRes != null && (sourceRes.equals(targetRes) || sourceRes.isOwner(targetRes.getOwner())))
            return;

        // check targetRes Flag_build
        if (targetRes.getPermissions().has(Flags.build, true))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLavaWaterFlow(BlockFromToEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock()))
            return;
        Material mat = event.getBlock().getType();

        Location location = event.getToBlock().getLocation();
        if (!plugin.getConfigManager().getNoFlowWorlds().contains(location.getWorld().getName()))
            return;

        if (location.getBlockY() < plugin.getConfigManager().getFlowLevel())
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(location);

        if (res != null)
            return;

        if (plugin.getConfigManager().isNoLava())
            if (mat == Material.LAVA) {
                event.setCancelled(true);
                return;
            }

        if (plugin.getConfigManager().isNoWater())
            if (mat == Material.WATER) {
                event.setCancelled(true);
                return;
            }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {

        if (FlagPermissions.shouldIgnoreCheck(Flags.firespread, event.getBlock())) {
            return;
        }
        FlagPermissions perms = FlagPermissions.getPerms(event.getBlock().getLocation());
        if (!perms.has(Flags.firespread, true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBurn(PortalCreateEvent event) {

        World world = event.getWorld();

        if (FlagPermissions.shouldIgnoreCheck(Flags.build, world)) {
            return;
        }
        if (!event.getReason().toString().equals("NETHER_PAIR"))
            return;

        Player player = null;
        // Crude attempt to get player object. Older versions will create exception of
        // missing method
        try {
            if (event.getEntity() instanceof Player)
                player = (Player) event.getEntity();
        } catch (Throwable e) {
        }

        ArrayList<Vector> corners = getNetherPortalCorners(event);

        for (Vector one : corners) {
            boolean hasBuild = true;
            if (player != null) {
                ClaimedResidence res = plugin.getResidenceManager().getByLoc(new Location(world, one.getX(), one.getY(), one.getZ()));
                if (res != null) {
                    hasBuild = res.getPermissions().playerHas(player, Flags.build, FlagCombo.TrueOrNone);
                    if (!hasBuild) {
                        lm.Invalid_PortalDestination.sendMessage(player);
                    }
                }
            } else {
                FlagPermissions perms = FlagPermissions.getPerms(new Location(world, one.getX(), one.getY(), one.getZ()));
                hasBuild = perms.has(Flags.build, true);
            }
            if (!hasBuild) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<Vector> getNetherPortalCorners(PortalCreateEvent e) {
        ArrayList<Vector> locs = new ArrayList<Vector>();

        List<?> ls = new ArrayList<>();
        try {
            ls = Version.isCurrentEqualOrHigher(Version.v1_14_0)
                    ? (ArrayList<BlockState>) e.getClass().getMethod("getBlocks").invoke(e)
                    : (ArrayList<Block>) e.getClass().getMethod("getBlocks").invoke(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        int lowestY = CMIWorld.getMaxHeight(e.getWorld());
        int bigestY = -lowestY;
        int lowestX = Integer.MAX_VALUE;
        int lowestZ = Integer.MAX_VALUE;
        int bigestX = -Integer.MAX_VALUE;
        int bigestZ = -Integer.MAX_VALUE;

        for (Object ob : ls) {
            Location one = Version.isCurrentEqualOrHigher(Version.v1_14_0)
                    ? ((BlockState) ob).getLocation()
                    : ((Block) ob).getLocation();

            if (one.getBlockY() < lowestY)
                lowestY = one.getBlockY();
            if (one.getBlockX() < lowestX)
                lowestX = one.getBlockX();
            if (one.getBlockZ() < lowestZ)
                lowestZ = one.getBlockZ();

            if (one.getBlockY() > bigestY)
                bigestY = one.getBlockY();
            if (one.getBlockX() > bigestX)
                bigestX = one.getBlockX();
            if (one.getBlockZ() > bigestZ)
                bigestZ = one.getBlockZ();
        }

        int height = Math.abs(bigestY - lowestY);

        // If height is 1 then its not a nether portal
        if (height < 2)
            return locs;

        locs.add(new Vector(lowestX, lowestY, lowestZ));
        locs.add(new Vector(bigestX, bigestY, bigestZ));

        return locs;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {

        Block block = event.getBlock();
        // disabling event on world
        if (plugin.isDisabledWorldListener(block)) {
            return;
        }
        if (Flags.firespread.isGlobalyEnabled() && event.getCause() == IgniteCause.SPREAD) {
            if (FlagPermissions.has(block.getLocation(), Flags.firespread, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
            }

        } else if (Flags.ignite.isGlobalyEnabled()) {
            Player player = event.getPlayer();
            if (player != null) {
                if (FlagPermissions.shouldDenyAndNotify(player, block, Flags.ignite, null)) {
                    event.setCancelled(true);
                }
            } else {
                if (FlagPermissions.has(block.getLocation(), Flags.ignite, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractTNT(PlayerInteractEvent event) {

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        if (FlagPermissions.shouldIgnoreCheck(Flags.ignite, block)) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!CMIMaterial.get(block.getType()).equals(CMIMaterial.TNT))
            return;

        CMIMaterial held = CMIMaterial.get(event.getItem());
        if (held != CMIMaterial.FLINT_AND_STEEL && held != CMIMaterial.FIRE_CHARGE) {
            return;
        }
        Player player = event.getPlayer();

        if (FlagPermissions.shouldDenyAndNotify(player, block, Flags.ignite, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHopperMoveItem(InventoryMoveItemEvent event) {
        // Prevent trolls from pushing derailed hopper minecarts into the Residence to steal items from containers
        if (Flags.minecartsuction.isGlobalyEnabled()) {
            InventoryHolder holder = Utils.getHolderNoSnapshot(event.getInitiator());
            if (holder instanceof HopperMinecart) {
                Location loc = ((HopperMinecart) holder).getLocation();
                if (!CMIMaterial.get(loc.getBlock().getType()).containsCriteria(CMIMC.RAIL)
                        && FlagPermissions.has(loc, Flags.minecartsuction, FlagCombo.OnlyFalse)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        // Protect containers at the edge of the Residence area from theft
        if (!Flags.container.isGlobalyEnabled() || !plugin.getConfigManager().getHopperCrossResidenceCheck()) {
            return;
        }
        Location sourceLoc = null;
        Location destLoc = null;

        if (Version.isCurrentEqualOrHigher(Version.v1_9_0)) {
            sourceLoc = event.getSource().getLocation();
            destLoc = event.getDestination().getLocation();

            // Legacy versions do not support Inventory.getLocation() directly
        } else {
            InventoryHolder sourceHolder = event.getSource().getHolder();
            if (sourceHolder instanceof BlockState) {
                sourceLoc = ((BlockState) sourceHolder).getLocation();
            } else if (sourceHolder instanceof Entity) {
                sourceLoc = ((Entity) sourceHolder).getLocation();
            }

            InventoryHolder destHolder = event.getDestination().getHolder();
            if (destHolder instanceof BlockState) {
                destLoc = ((BlockState) destHolder).getLocation();
            } else if (destHolder instanceof Entity) {
                destLoc = ((Entity) destHolder).getLocation();
            }
        }
        ClaimedResidence sourceRes = ClaimedResidence.getByLoc(sourceLoc);
        ClaimedResidence destRes = ClaimedResidence.getByLoc(destLoc);
        // Source and Dest not in Res
        if (sourceRes == null && destRes == null) {
            return;
        }
        // Source and Dest in Res
        if (sourceRes != null && destRes != null) {
            // in Same Res, or have Same Res owner
            if (sourceRes == destRes || sourceRes.isOwner(destRes.getOwner())) {
                return;
            }
            // Not in Same Res and not Same Res owner; hopper can be Source or Dest
            if (sourceRes.getPermissions().has(Flags.container, true)
                    && destRes.getPermissions().has(Flags.container, true)) {
                return;
            }
            // Source in Res, Dest definitely not in Res
        } else if (sourceRes != null) {
            if (sourceRes.getPermissions().has(Flags.container, true)) {
                return;
            }
            // Dest definitely in Res, Source definitely not in Res
        } else {
            if (destRes.getPermissions().has(Flags.container, true)) {
                return;
            }
        }
        event.setCancelled(true);
    }
}
