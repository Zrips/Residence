package com.bekvon.bukkit.residence.listeners;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Teleporting;

import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ResidenceListener1_09 implements Listener {

    private Residence plugin;

    public ResidenceListener1_09(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void EntityToggleGlideEvent(EntityToggleGlideEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        if (ResAdmin.isResAdmin(event.getEntity())) {
            return;
        }

        Player player = (Player) event.getEntity();

        FlagPermissions perms = FlagPermissions.getPerms(player);

        if (perms.playerHas(player, Flags.elytra, FlagCombo.TrueOrNone))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.elytra);

        event.setCancelled(true);
        CMIScheduler.runAtLocation(plugin, player.getLocation(), () -> {
            // Need to enable before disabling to prevent client side bug
            player.setGliding(true);
            player.setGliding(false);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceChange(ResidenceChangedEvent event) {

        ClaimedResidence newRes = event.getTo();

        Player player = event.getPlayer();
        if (player == null)
            return;

        if (!player.isGliding())
            return;

        if (newRes == null)
            return;

        if (newRes.getPermissions().playerHas(player, Flags.elytra, FlagCombo.TrueOrNone))
            return;

        player.setGliding(false);

        Location loc = ResidencePlayerListener.getSafeLocation(player.getLocation());
        if (loc == null) {
            // get defined land location in case no safe landing spot are found
            loc = plugin.getConfigManager().getFlyLandLocation();
            if (loc == null) {
                // get main world spawn location in case valid location is not found
                loc = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
        }
        if (loc != null) {
            lm.Flag_Deny.sendMessage(player, Flags.elytra);
            player.closeInventory();
            Teleporting.teleport(player, loc);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLingeringSplashPotion(LingeringPotionSplashEvent event) {
        ProjectileHitEvent ev = event;
        ThrownPotion potion = (ThrownPotion) ev.getEntity();

        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(potion.getWorld()))
            return;
        if (event.isCancelled())
            return;

        boolean harmfull = false;
        mein: for (PotionEffect one : potion.getEffects()) {
            for (String oneHarm : Residence.getInstance().getConfigManager().getNegativePotionEffects()) {
                if (oneHarm.equalsIgnoreCase(one.getType().getName())) {
                    harmfull = true;
                    break mein;
                }
            }
        }
        if (!harmfull)
            return;

        Entity ent = potion;
        boolean srcpvp = FlagPermissions.has(ent.getLocation(), Flags.pvp, FlagCombo.TrueOrNone);
        if (!srcpvp)
            event.setCancelled(true);
    }

    private static Method basePotionData = null;
    private static Method basePotionType = null;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLingeringEffectApply(AreaEffectCloudApplyEvent event) {

        // disabling event on world
        if (Residence.getInstance().isDisabledWorldListener(event.getEntity().getWorld()))
            return;

        boolean harmfull = false;

        // Temporally fail safe to avoid console spam for getting base potion data until fix roles out
        try {

            if (Version.isCurrentEqualOrHigher(Version.v1_20_R4)) {
                for (String oneHarm : Residence.getInstance().getConfigManager().getNegativeLingeringPotionEffects()) {
                    if (!event.getEntity().getBasePotionType().name().equalsIgnoreCase(oneHarm))
                        continue;
                    harmfull = true;
                    break;
                }
            } else {
                try {

                    if (basePotionData == null) {
                        basePotionData = event.getEntity().getClass().getMethod("getBasePotionData");
                        Object data = basePotionData.invoke(event.getEntity());
                        basePotionType = data.getClass().getMethod("getType");
                    }
                    Object data = basePotionData.invoke(event.getEntity());
                    org.bukkit.potion.PotionType type = (org.bukkit.potion.PotionType) basePotionType.invoke(data);
                    for (String oneHarm : Residence.getInstance().getConfigManager().getNegativeLingeringPotionEffects()) {
                        if (type.name().equalsIgnoreCase(oneHarm)) {
                            harmfull = true;
                            break;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            return;
        }

        if (!harmfull)
            return;

        Entity ent = event.getEntity();
        boolean srcpvp = FlagPermissions.has(ent.getLocation(), Flags.pvp, true);
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity target = it.next();
            if (!(target instanceof Player))
                continue;
            Boolean tgtpvp = FlagPermissions.has(target.getLocation(), Flags.pvp, true);
            if (!srcpvp || !tgtpvp) {
                event.getAffectedEntities().remove(target);
                event.getEntity().remove();
                break;
            }
        }
    }
}
