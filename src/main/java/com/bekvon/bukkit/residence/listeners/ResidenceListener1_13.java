package com.bekvon.bukkit.residence.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidenceBlockData;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ResidenceListener1_13 implements Listener {

    private Residence plugin;

    public ResidenceListener1_13(Residence plugin) {
        this.plugin = plugin;
    }

    public static void checkBoundingBox(ClaimedResidence res, double range, org.bukkit.World world) {
        for (Player player : res.getPlayersInResidence()) {
            Vector vloc = player.getLocation().toVector();

            // Limit check area in case residence is very big
            BoundingBox searchBox = BoundingBox.of(
                    vloc.clone().subtract(new Vector(range, range, range)),
                    vloc.clone().add(new Vector(range, range, range)));

            CMIScheduler.runAtLocation(Residence.getInstance(), player.getLocation(), () -> {
                Set<Entity> ent = new HashSet<>(world.getNearbyEntities(searchBox));
                ResidencePlayerListener.processEntities(ent, res);
            });
        }
    }

    public static boolean shouldCancelFarmLandChange(Block block) {

        if (block.getType() != Material.FARMLAND)
            return false;

        if (!FlagPermissions.has(block.getLocation(), Flags.dryup, FlagCombo.OnlyFalse))
            return false;

        try {
            ResidenceBlockData.updateFarmLand(block);
        } catch (NoClassDefFoundError e) {
        }
        return true;
    }

    // Send message when player projectile hitting Button/Pressure_Plate is denied
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityInteractDenyMsg(ProjectileHitEvent event) {

        Block hitBlock = event.getHitBlock();
        if (hitBlock == null || event.getHitBlockFace() == null) {
            return;
        }
        // disabling event on world
        if (plugin.isDisabledWorldListener(hitBlock)) {
            return;
        }
        Block hitBlockFace = hitBlock.getLocation().clone().add(event.getHitBlockFace().getDirection()).getBlock();
        CMIMaterial mat = CMIMaterial.get(hitBlockFace.getType());
        Flags flag = null;

        if (mat.containsCriteria(CMIMC.BUTTON)) {
            flag = Flags.button;
        } else if (mat.containsCriteria(CMIMC.PRESSUREPLATE)) {
            flag = Flags.pressure;
        }
        if (flag == null || !flag.isGlobalyEnabled()) {
            return;
        }
        Player player = Utils.potentialProjectileToPlayer(event.getEntity());
        if (player == null)
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(hitBlockFace.getLocation(), player);
        if (perms.playerHas(player, flag, perms.playerHas(player, Flags.use, true)))
            return;

        // The perfect spot, the earlier check sends exactly one deny msg
        // avoid chat spam
        lm.Flag_Deny.sendMessage(player, flag);

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtFish(PlayerInteractEntityEvent event) {
        // 1.17+ has PlayerBucketEntityEvent
        if (Version.isCurrentEqualOrHigher(Version.v1_17_0)) {
            return;
        }
        Entity ent = event.getRightClicked();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, ent)) {
            return;
        }
        if (!(ent instanceof Fish))
            return;

        Player player = event.getPlayer();

        Material held = ResidenceListener1_09.getHeldMaterial(event);

        if (held != Material.WATER_BUCKET)
            return;

        if (FlagPermissions.shouldDenyAndNotify(player, ent, Flags.animalkilling, null)) {
            event.setCancelled(true);
        }
    }
}
