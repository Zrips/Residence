package com.bekvon.bukkit.residence.listeners;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Version.Version;
import org.jetbrains.annotations.NotNull;

public class ResidenceListener1_14 implements Listener {

    private Residence plugin;

    public ResidenceListener1_14(Residence plugin) {
        this.plugin = plugin;
    }

    private static final Map<String, Optional<Tag<Material>>> BLOCK_TAG_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Tag<Material>>> ITEM_TAG_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Tag<EntityType>>> ENTITY_TAG_CACHE = new ConcurrentHashMap<>();

    // https://minecraft.wiki/w/Block_tag_(Java_Edition)
    public static boolean isBlockTag(@NotNull Material block, @NotNull String tagName) {
        Optional<Tag<Material>> cached = BLOCK_TAG_CACHE.computeIfAbsent(tagName, key -> Optional.ofNullable(Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(key), Material.class)));
        Tag<Material> tag = cached.orElse(null);
        return tag != null && tag.isTagged(block);
    }

    // https://minecraft.wiki/w/Item_tag_(Java_Edition)
    public static boolean isItemTag(@NotNull Material item, @NotNull String tagName) {
        Optional<Tag<Material>> cached = ITEM_TAG_CACHE.computeIfAbsent(tagName, key -> Optional.ofNullable(Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft(key), Material.class)));
        Tag<Material> tag = cached.orElse(null);
        return tag != null && tag.isTagged(item);
    }

    // https://minecraft.wiki/w/Entity_type_tag_(Java_Edition)
    public static boolean isEntityTag(@NotNull EntityType type, @NotNull String tagName) {
        Optional<Tag<EntityType>> cached = ENTITY_TAG_CACHE.computeIfAbsent(tagName, key -> Optional.ofNullable(Bukkit.getTag(Tag.REGISTRY_ENTITY_TYPES, NamespacedKey.minecraft(key), EntityType.class)));
        Tag<EntityType> tag = cached.orElse(null);
        return tag != null && tag.isTagged(type);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLecternBookTake(PlayerTakeLecternBookEvent event) {

        if (FlagPermissions.shouldIgnoreCheck(Flags.container, event.getLectern().getWorld())) {
            return;
        }
        Player player = event.getPlayer();

        if (FlagPermissions.shouldDenyAndNotify(player, event.getLectern().getLocation(), Flags.container, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProjectileHitBell(ProjectileHitEvent event) {

        Block block = event.getHitBlock();
        if (block == null) {
            return;
        }
        if (FlagPermissions.shouldIgnoreCheck(Flags.use, block)) {
            return;
        }
        if (block.getType() != Material.BELL) {
            return;
        }
        if (ResidenceEntityListener.shouldDenyEntityHit(event.getEntity(), block.getLocation(), Flags.use, null)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCoralDryFade(BlockFadeEvent event) {

        Block block = event.getBlock();

        if (FlagPermissions.shouldIgnoreCheck(Flags.coraldryup, block)) {
            return;
        }
        Material mat = block.getType();

        if (!(isBlockTag(mat, "corals") || isBlockTag(mat, "coral_blocks") || isBlockTag(mat, "wall_corals")))
            return;

        if (FlagPermissions.has(block.getLocation(), Flags.coraldryup, FlagCombo.OnlyFalse))
            event.setCancelled(true);

    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onVanillaRaidTrigger(RaidTriggerEvent event) {

        if (FlagPermissions.shouldIgnoreCheck(Flags.raid, event.getWorld())) {
            return;
        }
        if (FlagPermissions.has(event.getRaid().getLocation(), Flags.raid, true)) {
            return;
        }
        lm.Flag_Deny.sendMessage(event.getPlayer(), Flags.raid);
        event.setCancelled(true);

	}

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnimalTransformByLightning(EntityTransformEvent event) {
        // Paper 1.16.5+ uses EntityZapEvent for more detailed handling
        if (Version.isCurrentEqualOrHigher(Version.v1_16_5) && Version.isPaperBranch()) {
            return;
        }
        Entity entity = event.getEntity();

        if (FlagPermissions.shouldIgnoreCheck(Flags.animalkilling, entity)) {
            return;
        }
        if (event.getTransformReason() != EntityTransformEvent.TransformReason.LIGHTNING) {
            return;
        }
        if (!(entity instanceof LivingEntity) || !Utils.isAnimal(entity)) {
            return;
        }
        if (FlagPermissions.has(entity.getLocation(), Flags.animalkilling, true)) {
            return;
        }
        event.setCancelled(true);
    }
}
