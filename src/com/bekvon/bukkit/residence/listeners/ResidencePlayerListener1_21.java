package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Version.Version;

public class ResidencePlayerListener1_21 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_21(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnEntityDeath(EntityDeathEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.build.isGlobalyEnabled())
            return;
        // disabling event on world
        LivingEntity ent = event.getEntity();
        if (ent == null)
            return;
        if (plugin.isDisabledWorldListener(ent.getWorld()))
            return;
        if (!ent.hasPotionEffect(PotionEffectType.WEAVING))
            return;

        Location loc = ent.getLocation();
        FlagPermissions perms = plugin.getPermsByLoc(loc);
        if (perms.has(Flags.build, FlagCombo.TrueOrNone))
            return;

        // Removing weaving effect on death as there is no other way to properly handle this effect inside residence
        ent.removePotionEffect(PotionEffectType.WEAVING);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFenceLeashInteract(PlayerInteractEntityEvent event) {

        // Disabling listener if flag disabled globally
        if (!Flags.leash.isGlobalyEnabled())
            return;

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getRightClicked().getWorld()))
            return;
        Player player = event.getPlayer();

        Entity entity = event.getRightClicked();

        if (!(entity instanceof Boat))
            return;

        if (plugin.isResAdminOn(player))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());

        if (res == null)
            return;

        if (res.getPermissions().playerHas(player, Flags.leash, FlagCombo.OnlyFalse)) {
            plugin.msg(player, lm.Residence_FlagDeny, Flags.leash, res.getName());
            event.setCancelled(true);
            player.updateInventory();
        }
    }
}
