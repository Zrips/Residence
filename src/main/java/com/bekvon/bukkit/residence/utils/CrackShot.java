//package com.bekvon.bukkit.residence.utils;
//
//import com.bekvon.bukkit.residence.Residence;
//import com.bekvon.bukkit.residence.containers.Flags;
//import com.bekvon.bukkit.residence.containers.lm;
//import com.bekvon.bukkit.residence.protection.ClaimedResidence;
//import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
//import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
//import org.bukkit.Location;
//import org.bukkit.entity.*;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//
//public class CrackShot implements Listener {
//    private Residence plugin;
//
//    public CrackShot(Residence plugin) {
//        this.plugin = plugin;
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void AnimalKilling(WeaponDamageEntityEvent event) {
//        // disabling event on world
//        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
//            return;
//        Entity damager = event.getDamager();
//
//        if ((!(damager instanceof Arrow)) && (!(damager instanceof Player))) {
//            return;
//        }
//
//        Player cause = null;
//        if ((damager instanceof Arrow) && (!(((Arrow) damager).getShooter() instanceof Player))) {
//            return;
//        }
//
//        if (damager instanceof Player) {
//            cause = (Player) damager;
//        } else {
//            cause = (Player) ((Arrow) damager).getShooter();
//        }
//
//        if (cause == null)
//            return;
//
//        if (plugin.isResAdminOn(cause)) {
//            return;
//        }
//
//        if (!(event.getVictim() instanceof LivingEntity))
//            return;
//
//        Entity entity = event.getVictim();
//        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());
//
//        if (plugin.getNms().isAnimal(entity)) {
//            if (res != null && res.getPermissions().playerHas(cause, Flags.animalkilling, FlagCombo.OnlyFalse)) {
//                cause.sendMessage(plugin.msg(lm.Residence_FlagDeny, Flags.animalkilling, res.getName()));
//                event.setCancelled(true);
//            }
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//    public void onEntityDamageByEntityEvent(WeaponDamageEntityEvent event) {
//        // disabling event on world
//        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
//            return;
//        if (event.getVictim().getType() != EntityType.ITEM_FRAME && !plugin.getNms().isArmorStandEntity(event.getVictim().getType()))
//            return;
//
//        Entity dmgr = event.getDamager();
//        Player player;
//        if (event.getDamager() instanceof Player) {
//            player = (Player) event.getDamager();
//        } else {
//            if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
//                player = (Player) ((Projectile) dmgr).getShooter();
//            } else
//                return;
//        }
//
//        if (plugin.isResAdminOn(player))
//            return;
//
//        // Note: Location of entity, not player; otherwise player could stand outside of res and still damage
//        Location loc = event.getVictim().getLocation();
//        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
//        if (res != null && res.getPermissions().playerHas(player, Flags.container, FlagCombo.OnlyFalse)) {
//            event.setCancelled(true);
//            plugin.msg(player, lm.Residence_FlagDeny, Flags.container, res.getName());
//        }
//
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
//    public void onEntityDamage(WeaponDamageEntityEvent event) {
//        // disabling event on world
//        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
//            return;
//        if (!(event.getVictim() instanceof Player))
//            return;
//
//        Player victim = (Player) event.getVictim();
//        if (victim.hasMetadata("NPC")) {
//            return;
//        }
//
//        ClaimedResidence area = plugin.getResidenceManager().getByLoc(victim.getLocation());
//        /* Living Entities */
//        Player damager = event.getPlayer();
//        ClaimedResidence srcarea = null;
//
//        if (damager == null)
//            return;
//
//        srcarea = plugin.getResidenceManager().getByLoc(damager.getLocation());
//
//        boolean srcpvp = true;
//        if (srcarea != null) {
//            srcpvp = srcarea.getPermissions().has(Flags.pvp, true);
//        }
//
//        if (!srcpvp) {
//            damager.sendMessage(plugin.msg(lm.General_NoPVPZone));
//            event.setCancelled(true);
//            return;
//        }
//        /* Check for Player vs Player */
//        if (area == null) {
//            /* World PvP */
//            if (!plugin.getWorldFlags().getPerms(damager.getWorld().getName()).has(Flags.pvp, true)) {
//                damager.sendMessage(plugin.msg(lm.General_WorldPVPDisabled));
//                event.setCancelled(true);
//            }
//        } else {
//            /* Normal PvP */
//            if (!area.getPermissions().has(Flags.pvp, true)) {
//                damager.sendMessage(plugin.msg(lm.General_NoPVPZone));
//                event.setCancelled(true);
//            }
//        }
//        return;
//
//    }
//}
