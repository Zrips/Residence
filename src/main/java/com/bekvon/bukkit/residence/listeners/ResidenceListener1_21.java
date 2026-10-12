package com.bekvon.bukkit.residence.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AbstractWindCharge;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

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
import net.Zrips.CMILib.Version.Version;

import static com.bekvon.bukkit.residence.listeners.ResidenceListener1_14.isItemTag;

public class ResidenceListener1_21 implements Listener {

    private final Residence plugin;

    public ResidenceListener1_21(Residence plugin) {
        this.plugin = plugin;
    }

    // Prevent player from taking away animals in Residence by pulling boat
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAnimalEntersLeashedBoat(VehicleEnterEvent event) {

        Entity vehicle = event.getVehicle();

        if (FlagPermissions.shouldIgnoreCheck(Flags.leash, vehicle)) {
            return;
        }
        if (!(vehicle instanceof Boat))
            return;

        Entity entity = event.getEntered();

        if (!(entity instanceof LivingEntity) || !Utils.isAnimal(entity))
            return;

        if (Version.isPaperBranch()) {
            // if vehicle is not leashed, skip check
            if (vehicle instanceof io.papermc.paper.entity.Leashable
                    && !((io.papermc.paper.entity.Leashable) vehicle).isLeashed()) {
                return;
            }

        } else if (Version.isCurrentEqualOrHigher(Version.v1_21_11)) {
            // spigot
            if (vehicle instanceof org.bukkit.entity.Leashable
                    && !((org.bukkit.entity.Leashable) vehicle).isLeashed()) {
                return;
            }

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

        if (closest.hasMetadata("NPC") || ResAdmin.isResAdmin(closest)) {
            return;
        }
        if (res.getPermissions().playerHas(closest, Flags.leash, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(closest, Flags.leash, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWeavingEffectTrigger(EntityDeathEvent event) {

        LivingEntity ent = event.getEntity();

        if (plugin.isDisabledWorldListener(ent)) {
            return;
        }
        if (!ent.hasPotionEffect(PotionEffectType.WEAVING))
            return;

        if (Flags.animalgriefing.isGlobalyEnabled() && Utils.isAnimal(ent)) {
            FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
            if (perms.has(Flags.animalgriefing, perms.has(Flags.build, true))) {
                return;
            }

        } else if (Flags.mobgriefing.isGlobalyEnabled() && Utils.isMonster(ent)) {
            FlagPermissions perms = FlagPermissions.getPerms(ent.getLocation());
            if (perms.has(Flags.mobgriefing, perms.has(Flags.build, true))) {
                return;
            }

        } else if (Flags.build.isGlobalyEnabled()) {
            if (ent instanceof Player) {
                Player player = (Player) ent;
                if (!FlagPermissions.shouldDenyAndNotify(player, player, Flags.build, null)) {
                    return;
                }
            } else {
                if (FlagPermissions.has(ent.getLocation(), Flags.build, true)) {
                    return;
                }
            }
        } else {
            return;
        }
        // Removing weaving effect on death as there is no other way to properly handle
        // this effect inside residence
        ent.removePotionEffect(PotionEffectType.WEAVING);
    }

    public static boolean shouldDenyInteractCopperGolem(Player player, Entity entity, ItemStack itemStack) {

        if (Flags.container.isGlobalyEnabled()) {
            EntityEquipment golemInv = ((LivingEntity) entity).getEquipment();
            // Right-click to remove items from holding copper_golem
            if (golemInv != null && golemInv.getItemInMainHand().getType() != Material.AIR) {
                return FlagPermissions.shouldDenyAndNotify(player, entity, Flags.container, null);
            }
        }
        // Now Copper_golem has no item in hand
        if (!Flags.copper.isGlobalyEnabled() || itemStack == null) {
            return false;
        }
        Material held = itemStack.getType();
        // Honeycomb and Axes can change CopperGolem
        if (held != Material.HONEYCOMB && !isItemTag(held, "axes")) {
            return false;
        }
        return FlagPermissions.shouldDenyAndNotify(player, entity, Flags.copper, Flags.animalkilling);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFishingBobberHitEntity(ProjectileHitEvent event) {
        // anti NPE
        Entity hitEntity = event.getHitEntity();
        if (hitEntity == null) {
            return;
        }
        if (FlagPermissions.shouldIgnoreCheck(Flags.hook, hitEntity)) {
            return;
        }
        if (event.getEntityType() != EntityType.FISHING_BOBBER) {
            return;
        }
        Projectile hook = event.getEntity();
        // have player source
        if (!(hook.getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) hook.getShooter();

        if (FlagPermissions.shouldDenyAndNotify(player, hitEntity, Flags.hook, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onAnimalFeeding(PlayerInteractEntityEvent event) {

        Entity entity = event.getRightClicked();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalfeeding, entity)) {
            return;
        }
        if (!(entity instanceof Mob))
            return;

        Material held = ResidenceListener1_09.getHeldMaterial(event);

        if (!isFeedingAnimal((Mob) entity, held))
            return;

        Player player = event.getPlayer();

        if (FlagPermissions.shouldDenyAndNotify(player, entity, Flags.animalfeeding, Flags.animalkilling)) {
            event.setCancelled(true);
        }
    }

    private boolean isFeedingAnimal(Mob entity, Material held) {
        if (CMIMaterial.get(held) == CMIMaterial.GOLDEN_DANDELION) {
            return entity instanceof Ageable && !((Ageable)entity).isAdult();
        }
        // Temporary code, replace with enum after CMILib new GitHub Releases release
        if (Version.isCurrentEqualOrHigher(Version.v26_2_0) && entity instanceof org.bukkit.entity.SulfurCube) {
            return isItemTag(held, "sulfur_cube_food") || isItemTag(held, "sulfur_cube_swallowable");
        }
        CMIEntityType type = CMIEntityType.get(entity);
        if (type == null) {
            return false;
        }
        switch (type) {
        case ARMADILLO:
            return isItemTag(held, "armadillo_food");
        case AXOLOTL:
            return isItemTag(held, "axolotl_food");
        case BEE:
            return isItemTag(held, "bee_food");
        case CAMEL:
            return isItemTag(held, "camel_food");
        case CAMEL_HUSK:
            return isItemTag(held, "camel_husk_food");
        case CAT:
            return isItemTag(held, "cat_food");
        case CHICKEN:
            return isItemTag(held, "chicken_food");
        case COW:
        case MOOSHROOM:
            return isItemTag(held, "cow_food");
        case DONKEY:
        case HORSE:
        case MULE:
            return isItemTag(held, "horse_food");
        case FOX:
            return isItemTag(held, "fox_food");
        case FROG:
            return isItemTag(held, "frog_food");
        case GOAT:
            return isItemTag(held, "goat_food");
        case HAPPY_GHAST:
            return isItemTag(held, "happy_ghast_food");
        case HOGLIN:
            return isItemTag(held, "hoglin_food");
        case LLAMA:
        case TRADER_LLAMA:
            return isItemTag(held, "llama_food");
        case NAUTILUS:
        case ZOMBIE_NAUTILUS:
            return isItemTag(held, "nautilus_food");
        case OCELOT:
            return isItemTag(held, "ocelot_food");
        case PANDA:
            return isItemTag(held, "panda_food");
        case PARROT:
            return isItemTag(held, "parrot_food") || isItemTag(held, "parrot_poisonous_food");
        case PIG:
            return isItemTag(held, "pig_food");
        case RABBIT:
            return isItemTag(held, "rabbit_food");
        case SHEEP:
            return isItemTag(held, "sheep_food");
        case SNIFFER:
            return isItemTag(held, "sniffer_food");
        case STRIDER:
            return isItemTag(held, "strider_food");
        case TURTLE:
            return isItemTag(held, "turtle_food");
        case WOLF:
            return isItemTag(held, "wolf_food") || held == Material.BONE;
        case ZOMBIE_HORSE:
            return held == Material.RED_MUSHROOM;
        default:
            return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEquipAnimal(PlayerInteractEntityEvent event) {

        Entity entity = event.getRightClicked();

        if (FlagPermissions.shouldIgnoreCheck(Flags.container, entity)) {
            return;
        }
        if (!(entity instanceof Animals))
            return;

        Material held = ResidenceListener1_09.getHeldMaterial(event);

        // check if held item and interacted entity match
        // if conditions match, also check if the target entity slot is Air
        if (!isEquipFitAnimal((Animals) entity, CMIMaterial.get(held)))
            return;

        Player player = event.getPlayer();

        if (FlagPermissions.shouldDenyAndNotify(player, entity, Flags.container, null)) {
            event.setCancelled(true);
        }
    }

    private boolean isEquipFitAnimal(Animals entity, CMIMaterial held) {
        CMIEntityType type = CMIEntityType.get(entity);
        if (type == null) {
            return false;
        }
        if (held == CMIMaterial.SADDLE) {
            switch (type) {
            // 1.21.11+ supports EquipmentSlot.SADDLE
            // Add new Saddle-Equippable entities here in the future
            case CAMEL_HUSK:
            case NAUTILUS:
            case ZOMBIE_NAUTILUS:
                return isSlotAir(entity, EquipmentSlot.SADDLE);

            // Legacy version compatibility(< 1.21.11)
            case PIG:
                return entity instanceof Pig && !((Pig) entity).hasSaddle();
            case STRIDER:
                return entity instanceof Strider && !((Strider) entity).hasSaddle();
            case CAMEL:
            case DONKEY:
            case HORSE:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
                if (entity instanceof AbstractHorse) {
                    ItemStack horseSaddle = ((AbstractHorse) entity).getInventory().getSaddle();
                    // Do not use horseSaddle != null
                    // Saddle slot Air, getSaddle() returns null, result always false
                    return horseSaddle == null || horseSaddle.getType() == Material.AIR;
                }
                return false;
            default:
                return false;
            }
        }
        // Non-Saddle Equipment check
        switch (type) {
        case LLAMA:
        case TRADER_LLAMA:
            if (held.containsCriteria(CMIMC.CARPET) && isSlotAir(entity, EquipmentSlot.BODY)) {
                return held != CMIMaterial.MOSS_CARPET && held != CMIMaterial.PALE_MOSS_CARPET;
            }
            return false;
        case HAPPY_GHAST:
            return held.containsCriteria(CMIMC.HARNESS) && isSlotAir(entity, EquipmentSlot.BODY);
        case HORSE:
        case ZOMBIE_HORSE:
            return held.containsCriteria(CMIMC.HORSEARMOR) && isSlotAir(entity, EquipmentSlot.BODY);
        case NAUTILUS:
        case ZOMBIE_NAUTILUS:
            return held.containsCriteria(CMIMC.NAUTILUSARMOR) && isSlotAir(entity, EquipmentSlot.BODY);
        default:
            return false;
        }
    }

    private boolean isSlotAir(Animals entity, EquipmentSlot slot) {
        EntityEquipment equipment = entity.getEquipment();
        return equipment != null && equipment.getItem(slot).getType() == Material.AIR;
    }

    public static void onWindExplode(BlockExplodeEvent event) {

        if (Flags.windexplode.isGlobalyEnabled()) {
            FlagPermissions originPerms = FlagPermissions.getPerms(event.getBlock().getLocation());
            // Wind-Explode is prohibited at the origin location; cancel the event directly
            if (!originPerms.has(Flags.windexplode, originPerms.has(Flags.explode, true))) {
                event.setCancelled(true);
                return;
            }
        }
        // Origin allows Wind-Explode, so check each affected block for interaction
        event.blockList().removeIf(block -> {
            Flags flag = getWindExplodeInteractBlockFlag(block);
            if (flag == null || !flag.isGlobalyEnabled()) {
                return false;
            }
            FlagPermissions blockPerms = FlagPermissions.getPerms(block.getLocation());
            return !blockPerms.has(flag, blockPerms.has(Flags.use, true));
        });
    }

    public static void onWindExplode(EntityExplodeEvent event) {

        Entity originEntity = event.getEntity();
        ProjectileSource cause;

        if (originEntity instanceof AbstractWindCharge) {
            cause = ((AbstractWindCharge) originEntity).getShooter();
        } else {
            // Any entity with Wind-Charged-Effect triggers a Wind-Explode on death
            cause = ((ProjectileSource) originEntity);
        }
        if (Flags.windexplode.isGlobalyEnabled()) {
            Location originLoc = event.getLocation();
            // Wind-Explode is prohibited at the origin location; cancel the event directly
            if (shouldDenyWindExplode(originLoc, cause, Flags.windexplode, Flags.explode)) {
                event.setCancelled(true);
                return;
            }
        }
        // Origin allows Wind-Explode, so check each affected block for interaction
        event.blockList().removeIf(block -> {
            Flags flag = getWindExplodeInteractBlockFlag(block);
            if (flag == null || !flag.isGlobalyEnabled()) {
                return false;
            }
            return shouldDenyWindExplode(block.getLocation(), cause, flag, Flags.use);
        });
    }

    private static boolean shouldDenyWindExplode(Location triggerLoc, ProjectileSource cause, Flags mainFlag, Flags subFlag) {
        if (cause instanceof Player) {
            Player player = (Player) cause;
            if (player.hasMetadata("NPC") || ResAdmin.isResAdmin(player)) {
                return false;
            }
            FlagPermissions playerPerms = FlagPermissions.getPerms(triggerLoc, player);
            // Because Flags.explode is not FlagMode.Both
            boolean result = (subFlag == Flags.explode)
                    ? playerPerms.has(subFlag, true)
                    : playerPerms.playerHas(player, subFlag, true);
            if (!playerPerms.playerHas(player, mainFlag, result)) {
                lm.Flag_Deny.sendMessage(player, mainFlag);
                return true;
            }
        } else {
            FlagPermissions perms = FlagPermissions.getPerms(triggerLoc);
            return !perms.has(mainFlag, perms.has(subFlag, true));
        }
        return false;
    }

    @Nullable
    private static Flags getWindExplodeInteractBlockFlag(Block block) {
        Flags flag = null;
        CMIMaterial mat = CMIMaterial.get(block.getType());
        if (mat.containsCriteria(CMIMC.BUTTON)) {
            flag = Flags.button;
        } else if (mat.containsCriteria(CMIMC.DOOR) || mat.containsCriteria(CMIMC.FENCEGATE) || mat.containsCriteria(CMIMC.TRAPDOOR)) {
            flag = Flags.door;
        } else if (mat == CMIMaterial.BELL || mat.containsCriteria(CMIMC.CANDLE) || mat.containsCriteria(CMIMC.CANDLECAKE)) {
            flag = Flags.use;
        } else if (mat == CMIMaterial.LEVER) {
            flag = Flags.lever;
        }
        return flag;
    }
}
