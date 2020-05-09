package com.bekvon.bukkit.residence.allNms;

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.cmiLib.CMIEffectManager.CMIParticle;
import com.bekvon.bukkit.residence.containers.NMS;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayOutWorldParticles;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class v1_14_R1 implements NMS {
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
                ent instanceof Dolphin ||
                ent instanceof Fox ||
                ent instanceof Panda ||
                ent instanceof Cat);
    }

    @Override
    public boolean isArmorStandEntity(EntityType ent) {
        return ent == EntityType.ARMOR_STAND;
    }

    @Override
    public boolean isSpectator(GameMode mode) {
        return mode == GameMode.SPECTATOR;
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
    public ItemStack itemInOffHand(Player player) {
        return player.getInventory().getItemInOffHand();
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
