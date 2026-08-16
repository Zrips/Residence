package com.bekvon.bukkit.residence.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listenersCache.DenyMessageCache;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;

import net.Zrips.CMILib.Version.Version;

public class ResidenceListener1_21_8_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener1_21_8_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onKnockback(EntityPushedByEntityAttackEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld())) {
            return;
        }
        if (shouldCancelKnockBack(event.getEntity(), event.getPushedBy()))
            event.setCancelled(true);
    }

    public static boolean shouldCancelKnockBack(Entity target, Entity pushedBy) {

        Player player = Utils.potentialProjectileToPlayer(pushedBy);

        if (target instanceof ArmorStand) {
            return shouldDeny(target, player, Flags.destroy);
        }
        if (target instanceof Boat || target instanceof Minecart) {
            return shouldDeny(target, player, Flags.vehicledestroy);
        }
        if (target instanceof Player) {
            // Monster-on-player knockback doesn't need to check Flags.pvp
            // Allow players to knock themselves back (e.g., by Wind Charges)
            return player != null && !target.equals(player) && FlagPermissions.has(target.getLocation(), Flags.pvp, FlagCombo.OnlyFalse);
        }
        if (Utils.isAnimal(target)) {
            // SulfurCube containing blocks doesn't take damage
            // preferentially uses Flags.push instead on Paper 26.2+
            if (Version.isCurrentEqualOrHigher(Version.v26_2_0) && Version.isPaperBranch()
                    && target instanceof org.bukkit.entity.SulfurCube) {

                EntityEquipment equipment = ((org.bukkit.entity.SulfurCube) target).getEquipment();
                // Check if SulfurCube has a block inside
                if (equipment != null && !equipment.getItem(EquipmentSlot.BODY).isEmpty()) {
                    return shouldDenyPush(target, player);
                }
                // SulfurCube without blocks still checks Flags.animalkilling
            }
            return shouldDeny(target, player, Flags.animalkilling);
        }
        if (ResidenceEntityListener.isMonster(target)) {
            return shouldDeny(target, player, Flags.mobkilling);
        }
        return false;
    }

    private static boolean shouldDeny(Entity target, Player pushedBy, Flags flag) {
        if (pushedBy != null) {
            if (pushedBy.hasMetadata("NPC") || ResAdmin.isResAdmin(pushedBy)) {
                return false;
            }
            return FlagPermissions.has(target.getLocation(), pushedBy, flag, FlagCombo.OnlyFalse);
        } else {
            return FlagPermissions.has(target.getLocation(), flag, FlagCombo.OnlyFalse);
        }
    }

    private static boolean shouldDenyPush(Entity target, Player pushedBy) {
        if (pushedBy != null) {
            if (pushedBy.hasMetadata("NPC") || ResAdmin.isResAdmin(pushedBy)) {
                return false;
            }
            FlagPermissions perms = FlagPermissions.getPerms(target.getLocation(), pushedBy);
            if (!perms.playerHas(pushedBy, Flags.push, perms.playerHas(pushedBy, Flags.animalkilling, true))) {
                if (DenyMessageCache.shouldSendDenyMessage(pushedBy, Flags.push)) {
                    lm.Flag_Deny.sendMessage(pushedBy, Flags.push);
                }
                return true;
            }
            return false;

        } else {
            FlagPermissions perms = FlagPermissions.getPerms(target.getLocation());
            return !perms.has(Flags.push, perms.has(Flags.animalkilling, true));
        }
    }
}
