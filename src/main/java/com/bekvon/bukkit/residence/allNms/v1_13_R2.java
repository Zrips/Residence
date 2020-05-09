package com.bekvon.bukkit.residence.allNms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
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

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.cmiLib.CMIEffectManager.CMIParticle;
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
