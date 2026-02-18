package com.bekvon.bukkit.residence.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;

public class ResidenceListener1_21 implements Listener {

    private Residence plugin;

    public ResidenceListener1_21(Residence plugin) {
        this.plugin = plugin;
    }

    HashMap<UUID, Long> boats = new HashMap<UUID, Long>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        boats.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void OnVehicleEnterEvent(VehicleEnterEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.boarding.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getVehicle().getWorld()))
            return;

        Entity entity = event.getEntered();

        if (!(entity instanceof LivingEntity))
            return;

        if (!Utils.isAnimal(entity))
            return;

        if (FlagPermissions.getPerms(entity.getLocation()).has(Flags.boarding, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            return;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(entity.getLocation());
        if (res == null)
            return;

        Player closest = null;
        double dist = 32D;

        for (Player player : res.getPlayersInResidence()) {

            double tempDist = player.getLocation().distance(entity.getLocation());

            if (tempDist < dist) {
                closest = player;
                dist = tempDist;
            }
        }

        if (closest == null)
            return;

        if (res.getPermissions().playerHas(closest, Flags.leash, FlagCombo.OnlyFalse)) {
            Long time = boats.computeIfAbsent(closest.getUniqueId(), k -> 0L);

            if (time + 1000L < System.currentTimeMillis()) {
                boats.put(closest.getUniqueId(), System.currentTimeMillis());
                lm.Residence_FlagDeny.sendMessage(closest, Flags.leash, res.getName());
            }

            event.setCancelled(true);
        }
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
        FlagPermissions perms = FlagPermissions.getPerms(loc);
        if (perms.has(Flags.build, FlagCombo.TrueOrNone))
            return;

        // Removing weaving effect on death as there is no other way to properly handle
        // this effect inside residence
        ent.removePotionEffect(PotionEffectType.WEAVING);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractCopperGolem(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.copper.isGlobalyEnabled())
            return;

        Entity entity = event.getRightClicked();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        if (CMIEntityType.get(entity) != CMIEntityType.COPPER_GOLEM)
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        if (entity instanceof LivingEntity) {

            EntityEquipment gloemInv = ((LivingEntity) entity).getEquipment();
            // Right-click to remove items from holding copper_golem
            if (gloemInv != null && (gloemInv.getItemInMainHand().getType() != Material.AIR ||
                    gloemInv.getItemInOffHand().getType() != Material.AIR)) {

                if (FlagPermissions.has(entity.getLocation(), player, Flags.container, true))
                    return;

                lm.Flag_Deny.sendMessage(player, Flags.container);
                event.setCancelled(true);
                return;
            }
        }
        // Copper_golem has no item in hand

        Material held = (event.getHand() == EquipmentSlot.OFF_HAND)
                ? player.getInventory().getItemInOffHand().getType()
                : player.getInventory().getItemInMainHand().getType();

        // Avoid overwriting Leash Flag, Lead Shears
        if (held != Material.HONEYCOMB && !isItemTag(held, "axes"))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation(), player);
        if (perms.playerHas(player, Flags.copper, perms.playerHas(player, Flags.animalkilling, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.copper);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFishingBobberHit(ProjectileHitEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.hook.isGlobalyEnabled())
            return;
        // anti NPE
        Entity HitEntity = event.getHitEntity();
        if (HitEntity == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(HitEntity.getWorld()))
            return;

        Projectile hook = event.getEntity();
        // only fishing_bobber
        if (CMIEntityType.get(hook) != CMIEntityType.FISHING_BOBBER)
            return;
        // have player source
        if (!(hook.getShooter() instanceof Player))
            return;

        Player player = (Player) hook.getShooter();
        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(HitEntity.getLocation(), player);
        if (perms.playerHas(player, Flags.hook, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.hook);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAnimalFeeding(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.animalfeeding.isGlobalyEnabled())
            return;

        Entity entity = event.getRightClicked();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        if (!(entity instanceof Animals))
            return;

        Player player = event.getPlayer();

        Material held = event.getHand() == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand().getType()
                : player.getInventory().getItemInMainHand().getType();

        CMIEntityType type = CMIEntityType.get(entity.getType());

        if (type == null)
            return;

        if (!isFeedingAnimal(type, held))
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        FlagPermissions perms = FlagPermissions.getPerms(entity.getLocation(), player);
        if (perms.playerHas(player, Flags.animalfeeding, perms.playerHas(player, Flags.animalkilling, true)))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.animalfeeding);
        event.setCancelled(true);

    }

    private boolean isFeedingAnimal(CMIEntityType type, Material held) {
        switch (type) {
            case ARMADILLO: return isItemTag(held, "armadillo_food");
            case AXOLOTL: return isItemTag(held, "axolotl_food");
            case BEE: return isItemTag(held, "bee_food");
            case CAMEL: return isItemTag(held, "camel_food");
            case CAMEL_HUSK: return isItemTag(held, "camel_husk_food");
            case CAT: return isItemTag(held, "cat_food");
            case CHICKEN: return isItemTag(held, "chicken_food");
            case COW: return isItemTag(held, "cow_food");
            case DONKEY: return isItemTag(held, "horse_food");
            case FOX: return isItemTag(held, "fox_food");
            case FROG: return isItemTag(held, "frog_food");
            case GOAT: return isItemTag(held, "goat_food");
            case HAPPY_GHAST: return isItemTag(held, "happy_ghast_food");
            case HOGLIN: return isItemTag(held, "hoglin_food");
            case HORSE: return isItemTag(held, "horse_food");
            case LLAMA: return isItemTag(held, "llama_food");
            case MOOSHROOM: return isItemTag(held, "cow_food");
            case MULE: return isItemTag(held, "horse_food");
            case NAUTILUS: return isItemTag(held, "nautilus_food");
            case OCELOT: return isItemTag(held, "ocelot_food");
            case PANDA: return isItemTag(held, "panda_food");
            case PARROT: return isItemTag(held, "parrot_food") || isItemTag(held, "parrot_poisonous_food");
            case PIG: return isItemTag(held, "pig_food");
            case RABBIT: return isItemTag(held, "rabbit_food");
            case SHEEP: return isItemTag(held, "sheep_food");
            case SNIFFER: return isItemTag(held, "sniffer_food");
            case STRIDER: return isItemTag(held, "strider_food");
            case TRADER_LLAMA: return isItemTag(held, "llama_food");
            case TURTLE: return isItemTag(held, "turtle_food");
            case WOLF: return isItemTag(held, "wolf_food") || held == Material.BONE;
            case ZOMBIE_HORSE: return held == Material.RED_MUSHROOM;
            case ZOMBIE_NAUTILUS: return isItemTag(held, "nautilus_food");
            default: return false;
        }
    }

    private static boolean isItemTag(Material item, String tagName) {
        return ResidenceListener1_14.isItemTag(item, tagName);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEquipAnimal(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;

        Entity entity = event.getRightClicked();
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity.getWorld()))
            return;

        if (!(entity instanceof Animals))
            return;

        Player player = event.getPlayer();

        Material held = event.getHand() == EquipmentSlot.OFF_HAND
                ? player.getInventory().getItemInOffHand().getType()
                : player.getInventory().getItemInMainHand().getType();

        // check if held item and interacted entity match
        // if conditions match, also check if the target entity slot is Air
        if (!isEquipFitAnimal(entity, CMIMaterial.get(held)))
            return;

        if (ResAdmin.isResAdmin(player))
            return;

        if (FlagPermissions.has(entity.getLocation(), player, Flags.container, true))
            return;

        lm.Flag_Deny.sendMessage(player, Flags.container);
        event.setCancelled(true);

    }

    private static boolean isEquipFitAnimal(Entity entity, CMIMaterial held) {
        if (!(entity instanceof LivingEntity))
            return false;

        EntityEquipment entInv = ((LivingEntity) entity).getEquipment();
        if (entInv == null)
            return false;

        CMIEntityType type = CMIEntityType.get(entity);
        if (type == null)
            return false;

        boolean isBodySlotAir = entInv.getItem(EquipmentSlot.BODY).getType() == Material.AIR;

        if (held.containsCriteria(CMIMC.CARPET))
            return (type == CMIEntityType.LLAMA || type == CMIEntityType.TRADER_LLAMA) && isBodySlotAir &&
                    held != CMIMaterial.MOSS_CARPET && held != CMIMaterial.PALE_MOSS_CARPET;

        if (held.containsCriteria(CMIMC.HARNESS))
            return type == CMIEntityType.HAPPY_GHAST && isBodySlotAir;

        if (held.containsCriteria(CMIMC.HORSEARMOR))
            return (type == CMIEntityType.HORSE || type == CMIEntityType.ZOMBIE_HORSE) && isBodySlotAir;

        if (held.containsCriteria(CMIMC.NAUTILUSARMOR))
            return (type == CMIEntityType.NAUTILUS || type == CMIEntityType.ZOMBIE_NAUTILUS) && isBodySlotAir;

        if (held == CMIMaterial.SADDLE) {

            if (entity instanceof Pig)
                return !((Pig) entity).hasSaddle();

            if (entity instanceof Strider)
                return !((Strider) entity).hasSaddle();

            // Has Nautilus version, also supports EquipmentSlot.SADDLE
            if (type == CMIEntityType.NAUTILUS || type == CMIEntityType.ZOMBIE_NAUTILUS)
                return entInv.getItem(EquipmentSlot.SADDLE).getType() == Material.AIR;

            // Ensure entity is AbstractHorse
            switch (type) {
                case CAMEL:
                case CAMEL_HUSK:
                case DONKEY:
                case HORSE:
                case MULE:
                case SKELETON_HORSE:
                case ZOMBIE_HORSE:
                    if (!(entity instanceof AbstractHorse)) {
                        return false;
                    }
                    ItemStack horseSaddle = ((AbstractHorse) entity).getInventory().getSaddle();
                    // Do not use horseSaddle != null
                    // Saddle slot Air, getSaddle() returns null, result always false
                    return horseSaddle == null || horseSaddle.getType() == Material.AIR;
                default:
                    return false;
            }
        }
        return false;
    }
}
