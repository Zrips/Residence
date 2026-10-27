package com.bekvon.bukkit.residence.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Version.Version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotionUtils {

    public PotionUtils() {
    }

    private static final List<String> LEGACY_BENEFICIAL_EFFECTS = Collections.unmodifiableList(Arrays.asList(
            "ABSORPTION", "CONDUIT_POWER", "DAMAGE_RESISTANCE", "DOLPHINS_GRACE",
            "FAST_DIGGING", "FIRE_RESISTANCE", "HEAL", "HEALTH_BOOST", "HERO_OF_THE_VILLAGE",
            "INCREASE_DAMAGE", "INVISIBILITY", "JUMP", "LUCK", "NIGHT_VISION", "REGENERATION",
            "SATURATION", "SLOW_FALLING", "SPEED", "WATER_BREATHING"
    ));

    private static final List<String> LEGACY_HARMFUL_EFFECTS = Collections.unmodifiableList(Arrays.asList(
            "BAD_OMEN", "BLINDNESS", "CONFUSION", "DARKNESS", "HARM", "HUNGER",
            "LEVITATION", "POISON", "SLOW", "SLOW_DIGGING", "UNLUCK", "WEAKNESS", "WITHER"
    ));

    private static final List<String> LEGACY_NEUTRAL_EFFECTS = Collections.singletonList("GLOWING");

    @SuppressWarnings("deprecation")
    private static boolean containsIgnoreCase(@NotNull List<String> list, @NotNull PotionEffectType type) {
        String name = type.getName();
        for (String one : list) {
            if (one.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBeneficialEffect(@NotNull PotionEffectType type) {
        if (Version.isCurrentEqualOrHigher(Version.v1_20_5)) {
            return type.getEffectCategory() == PotionEffectType.Category.BENEFICIAL;
        }
        return containsIgnoreCase(LEGACY_BENEFICIAL_EFFECTS, type);
    }

    public static boolean isHarmfulEffect(@NotNull PotionEffectType type) {
        if (Version.isCurrentEqualOrHigher(Version.v1_20_5)) {
            return type.getEffectCategory() == PotionEffectType.Category.HARMFUL;
        }
        return containsIgnoreCase(LEGACY_HARMFUL_EFFECTS, type);
    }

    public static boolean isNeutralEffect(@NotNull PotionEffectType type) {
        if (Version.isCurrentEqualOrHigher(Version.v1_20_5)) {
            return type.getEffectCategory() == PotionEffectType.Category.NEUTRAL;
        }
        return containsIgnoreCase(LEGACY_NEUTRAL_EFFECTS, type);
    }

    @SuppressWarnings("deprecation")
    public static boolean isHealingEffect(@NotNull PotionEffectType type) {
        if (Version.isCurrentEqualOrHigher(Version.v1_20_5)) {
            return type == PotionEffectType.INSTANT_HEALTH;
        }
        return type.getName().equalsIgnoreCase("HEAL");
    }

    @SuppressWarnings("deprecation")
    public static boolean isDamageEffect(@NotNull PotionEffectType type) {
        if (Version.isCurrentEqualOrHigher(Version.v1_20_5)) {
            return type == PotionEffectType.INSTANT_DAMAGE;
        }
        return type.getName().equalsIgnoreCase("HARM");
    }

    public static boolean shouldDenyHealingEffect(@NotNull LivingEntity victim, @Nullable Player attackerPlayer) {
        // Healing effect damages undead mobs
        if (attackerPlayer != null) {
            if (Utils.isUndead(victim)) {
                return FlagPermissions.shouldDenyAndNotify(attackerPlayer, victim, Flags.mobkilling);
            }
        } else {
            if (Utils.isUndead(victim)) {
                return FlagPermissions.has(victim.getLocation(), Flags.mobkilling, FlagCombo.OnlyFalse);
            }
        }
        return false;
    }

    public static boolean shouldDenyDamageEffect(@NotNull LivingEntity victim, @Nullable Player attackerPlayer, @Nullable ClaimedResidence attackerRes) {
        // Damage effect heals undead instead of harming them
        if (Utils.isUndead(victim)) {
            return false;
        }
        return shouldDenyHarmfulEffect(victim, attackerPlayer, attackerRes);
    }

    public static boolean shouldDenyHarmfulEffect(@NotNull LivingEntity victim, @Nullable Player attackerPlayer, @Nullable ClaimedResidence attackerRes) {
        if (attackerPlayer != null) {
            if (Flags.pvp.isGlobalyEnabled() && victim instanceof Player) {
                // Skip the PvP check when players apply negative effects to themselves
                if (attackerPlayer == victim) {
                    return false;
                }
                FlagPermissions attackerPerms = (attackerRes != null)
                        ? attackerRes.getPermissions()
                        : Residence.getInstance().getWorldFlags().getPerms(attackerPlayer.getWorld());
                // if PVP disabled at attacker location, deny the effect
                if (attackerPerms.has(Flags.pvp, FlagCombo.OnlyFalse)) {
                    lm.Flag_Deny.sendMessage(attackerPlayer, Flags.pvp);
                    return true;
                }
                ClaimedResidence victimRes = ClaimedResidence.getByLoc(victim.getLocation());
                FlagPermissions victimPerms = (victimRes != null)
                        ? victimRes.getPermissions()
                        : Residence.getInstance().getWorldFlags().getPerms(victim.getWorld());
                // if PVP disabled at victim location, deny the effect
                if (victimPerms.has(Flags.pvp, FlagCombo.OnlyFalse)) {
                    lm.Flag_Deny.sendMessage(attackerPlayer, Flags.pvp);
                    return true;
                }
                if (attackerRes != null && attackerRes == victimRes
                        && attackerPerms.playerHas(attackerPlayer, Flags.friendlyfire, FlagCombo.OnlyFalse)
                        && attackerPerms.playerHas((Player) victim, Flags.friendlyfire, FlagCombo.OnlyFalse)) {
                    CMIActionBar.send(attackerPlayer, Residence.getInstance().getLM().getMessage(lm.General_NoFriendlyFire));
                    return true;
                }
            } else if (Flags.animalkilling.isGlobalyEnabled() && Utils.isAnimal(victim)) {
                return FlagPermissions.shouldDenyAndNotify(attackerPlayer, victim, Flags.animalkilling);

            } else if (Flags.mobkilling.isGlobalyEnabled() && Utils.isMonster(victim)) {
                return FlagPermissions.shouldDenyAndNotify(attackerPlayer, victim, Flags.mobkilling);

            }
        } else {
            if (Flags.animalkilling.isGlobalyEnabled() && Utils.isAnimal(victim)) {
                return FlagPermissions.has(victim.getLocation(), Flags.animalkilling, FlagCombo.OnlyFalse);

            } else if (Flags.mobkilling.isGlobalyEnabled() && Utils.isMonster(victim)) {
                return FlagPermissions.has(victim.getLocation(), Flags.mobkilling, FlagCombo.OnlyFalse);

            }
        }
        return false;
    }
}
