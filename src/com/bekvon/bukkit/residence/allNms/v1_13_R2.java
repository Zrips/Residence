package com.bekvon.bukkit.residence.allNms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.CMILib.CMIEffect;
import com.bekvon.bukkit.residence.CMILib.CMIEffectManager.CMIParticle;
import com.bekvon.bukkit.residence.CMILib.ItemManager.CMIMaterial;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.NMS;

import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketPlayOutWorldParticles;

public class v1_13_R2 implements NMS {
    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
        List<Block> blocks = new ArrayList<Block>();
        blocks.addAll(event.getBlocks());
        return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
        return (ent instanceof Horse ||
                ent instanceof Bat ||
                ent instanceof Snowman ||
                ent instanceof IronGolem ||
                ent instanceof Ocelot ||
                ent instanceof Pig ||
                ent instanceof Sheep ||
                ent instanceof Chicken ||
                ent instanceof Wolf ||
                ent instanceof Cow ||
                ent instanceof Squid ||
                ent instanceof Villager ||
                ent instanceof Rabbit ||
                ent instanceof Llama ||
                ent instanceof PolarBear ||
                ent instanceof Parrot ||
                ent instanceof Donkey ||
                ent instanceof Cod ||
                ent instanceof Salmon ||
                ent instanceof PufferFish ||
                ent instanceof TropicalFish ||
                ent instanceof Turtle ||
                ent instanceof Dolphin);
    }

    @Override
    public boolean isArmorStandEntity(EntityType ent) {
        return ent == EntityType.ARMOR_STAND;
    }

    @Override
    public boolean isArmorStandMaterial(Material material) {
        return material == Material.ARMOR_STAND;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isCanUseEntity_BothClick(Material mat, Block block) {
        CMIMaterial m = CMIMaterial.get(mat);
        if (m.isDoor())
            return true;
        if (m.isButton())
            return true;
        if (m.isGate())
            return true;
        if (m.isTrapDoor())
            return true;

        switch (CMIMaterial.get(mat)) {
            case LEVER:
            case PISTON:
            case STICKY_PISTON:
            case DRAGON_EGG:
                return true;
            default:
                return Residence.getInstance().getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getType().getId()));
        }
    }

    @Override
    public boolean isEmptyBlock(Block block) {
        switch (CMIMaterial.get(block)) {
            case COBWEB:
            case STRING:
            case WALL_BANNER:
            case WALL_SIGN:
            case VINE:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case PAINTING:
            case ITEM_FRAME:
                return true;
            default:
                break;
        }

        if (CMIMaterial.get(block).isSapling())
            return true;
        if (CMIMaterial.get(block).isAir())
            return true;
        if (CMIMaterial.get(block).isButton())
            return true;
        return false;
    }

    @Override
    public boolean isSpectator(GameMode mode) {
        return mode == GameMode.SPECTATOR;
    }

    @Override
    public void addDefaultFlags(Map<Material, Flags> matUseFlagList) {
        /* 1.8 Fence Gates */
        matUseFlagList.put(Material.SPRUCE_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.BIRCH_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.JUNGLE_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.ACACIA_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.DARK_OAK_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.IRON_TRAPDOOR, Flags.door);

        matUseFlagList.put(CMIMaterial.DAYLIGHT_DETECTOR.getMaterial(), Flags.diode);

        /* 1.11 Shulker Box */
        matUseFlagList.put(Material.BLACK_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.BLUE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.BROWN_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.CYAN_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.GRAY_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.GREEN_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.LIGHT_BLUE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.LIME_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.MAGENTA_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.ORANGE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.PINK_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.PURPLE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.RED_SHULKER_BOX, Flags.container);
        matUseFlagList.put(CMIMaterial.LIGHT_GRAY_SHULKER_BOX.getMaterial(), Flags.container);
        matUseFlagList.put(Material.WHITE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.YELLOW_SHULKER_BOX, Flags.container);

        /* 1.13 Shulker Box */

        matUseFlagList.put(CMIMaterial.OAK_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.IRON_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.SPRUCE_DOOR_ITEM.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.BIRCH_DOOR_ITEM.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.JUNGLE_DOOR_ITEM.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.ACACIA_DOOR_ITEM.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.DARK_OAK_DOOR_ITEM.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.OAK_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.ACACIA_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.BIRCH_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.DARK_OAK_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.IRON_DOOR_BLOCK.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.JUNGLE_DOOR.getMaterial(), Flags.door);
        matUseFlagList.put(CMIMaterial.SPRUCE_DOOR.getMaterial(), Flags.door);

    }

    @Override
    public boolean isMainHand(PlayerInteractEvent event) {
        return event.getHand() == EquipmentSlot.HAND ? true : false;
    }

    @Override
    public ItemStack itemInMainHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public boolean isChorusTeleport(TeleportCause tpcause) {
        if (tpcause == TeleportCause.CHORUS_FRUIT)
            return true;
        return false;
    }

    @Override
    public void playEffect(Player player, Location location, CMIEffect ef) {
        if (location == null || ef == null || location.getWorld() == null)
            return;

        CMIParticle effect = ef.getParticle();
        if (effect == null)
            return;
        if (!effect.isParticle()) {
            return;
        }

        org.bukkit.Particle particle = effect.getParticle();

        if (particle == null)
            return;

//	CMI.getInstance().d(particle, effect.name());

        DustOptions dd = null;
        if (particle.equals(org.bukkit.Particle.REDSTONE))
            dd = new org.bukkit.Particle.DustOptions(ef.getColor(), ef.getSize());

        Packet<?> packet = new PacketPlayOutWorldParticles(CraftParticle.toNMS(particle, dd), true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
                (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());

        CraftPlayer cPlayer = (CraftPlayer) player;
        if (cPlayer.getHandle().playerConnection == null)
            return;

        if (!location.getWorld().equals(cPlayer.getWorld()))
            return;

        cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}
