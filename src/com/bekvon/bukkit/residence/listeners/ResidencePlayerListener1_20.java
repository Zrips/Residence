package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Version.Version;

public class ResidencePlayerListener1_20 implements Listener {

    private Residence plugin;

    public ResidencePlayerListener1_20(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerSignOpenEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;

        FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getSign().getLocation(), player);

        boolean hasuse = perms.playerHas(player, Flags.use, FlagCombo.TrueOrNone);
        boolean hasBuild = perms.playerHas(player, Flags.build, FlagCombo.TrueOrNone);

        if (hasuse && hasBuild || plugin.isResAdminOn(player))
            return;

        event.setCancelled(true);

        if (!hasuse)
            plugin.msg(player, lm.Flag_Deny, Flags.use);
        else
            plugin.msg(player, lm.Flag_Deny, Flags.build);

    }

    // For objects like decorative pots
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotBreak(EntityChangeBlockEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        if (!(event.getEntity() instanceof Projectile))
            return;

        Projectile projectile = (Projectile) event.getEntity();

        Player player = null;

        if (projectile.getShooter() instanceof Player)
            player = (Player) projectile.getShooter();

        if (player != null && player.hasMetadata("NPC"))
            return;

        if (!ResidenceBlockListener.canBreakBlock(player, event.getBlock().getLocation(), true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnEntityDeath(EntityDeathEvent event) {
        if (Version.isCurrentLower(Version.v1_20_R3))
            return;
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
}
