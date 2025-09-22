package com.bekvon.bukkit.residence.listeners;

import java.lang.reflect.Method;
import java.util.Iterator;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Version.Version;

public class ResidencePlayerListener1_09 implements Listener {

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
        boolean srcpvp = Residence.getInstance().getPermsByLoc(ent.getLocation()).has(Flags.pvp, FlagCombo.TrueOrNone);
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
        boolean srcpvp = Residence.getInstance().getPermsByLoc(ent.getLocation()).has(Flags.pvp, true);
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity target = it.next();
            if (!(target instanceof Player))
                continue;
            Boolean tgtpvp = Residence.getInstance().getPermsByLoc(target.getLocation()).has(Flags.pvp, true);
            if (!srcpvp || !tgtpvp) {
                event.getAffectedEntities().remove(target);
                event.getEntity().remove();
                break;
            }
        }
    }
}
