package com.bekvon.bukkit.residence.utils;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

public class CrackShot implements Listener {
    private Residence plugin;

    public CrackShot(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(WeaponDamageEntityEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Entity damager = event.getDamager();

        if ((!(damager instanceof Arrow)) && (!(damager instanceof Player))) {
            return;
        }

        Player cause = null;
        if ((damager instanceof Arrow) && (!(((Arrow) damager).getShooter() instanceof Player))) {
            return;
        }

        if (damager instanceof Player) {
            cause = (Player) damager;
        } else {
            cause = (Player) ((Arrow) damager).getShooter();
        }

        if (cause == null)
            return;

        if (ResAdmin.isResAdmin(cause)) {
            return;
        }

        if (!(event.getVictim() instanceof LivingEntity))
            return;

        Entity entity = event.getVictim();
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

        if (Utils.isAnimal(entity) && res != null && res.getPermissions().playerHas(cause, Flags.animalkilling, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(cause, Flags.animalkilling, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(WeaponDamageEntityEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getVictim().getType() != EntityType.ITEM_FRAME && !Utils.isArmorStandEntity(event.getVictim().getType()))
            return;

        Entity dmgr = event.getDamager();
        Player player;
        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else {
            if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
                player = (Player) ((Projectile) dmgr).getShooter();
            } else
                return;
        }

        if (ResAdmin.isResAdmin(player))
            return;

        // Note: Location of entity, not player; otherwise player could stand outside of res and still damage
        Location loc = event.getVictim().getLocation();
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
        if (res != null && res.getPermissions().playerHas(player, Flags.container, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            lm.Residence_FlagDeny.sendMessage(player, Flags.container, res.getName());
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(WeaponDamageEntityEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (!(event.getVictim() instanceof Player))
            return;

        Player victim = (Player) event.getVictim();
        if (victim.hasMetadata("NPC")) {
            return;
        }

        ClaimedResidence area = plugin.getResidenceManager().getByLoc(victim.getLocation());
        /* Living Entities */
        Player damager = event.getPlayer();
        ClaimedResidence srcarea = null;

        if (damager == null)
            return;

        srcarea = plugin.getResidenceManager().getByLoc(damager.getLocation());

        boolean srcpvp = true;
        if (srcarea != null) {
            srcpvp = srcarea.getPermissions().has(Flags.pvp, true);
        }

        if (!srcpvp) {
            lm.General_NoPVPZone.sendMessage(damager);
            event.setCancelled(true);
            return;
        }
        /* Check for Player vs Player */
        if (area == null) {
            /* World PvP */
            if (!plugin.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.pvp, true)) {
                lm.General_WorldPVPDisabled.sendMessage(damager);
                event.setCancelled(true);
            }
        } else {
            /* Normal PvP */
            if (!area.getPermissions().has(Flags.pvp, true)) {
                lm.General_NoPVPZone.sendMessage(damager);
                event.setCancelled(true);
            }
        }
        return;

    }
}
