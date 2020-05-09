package com.bekvon.bukkit.residence.allNms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.residence.containers.NMS;

import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.Packet;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;

public class v1_8_R1 implements NMS {
    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
	List<Block> blocks = new ArrayList<Block>();
	blocks.addAll(event.getBlocks());
	return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
	return (ent instanceof Horse || ent instanceof Bat || ent instanceof Snowman || ent instanceof IronGolem || ent instanceof Ocelot || ent instanceof Pig
	    || ent instanceof Sheep || ent instanceof Chicken || ent instanceof Wolf || ent instanceof Cow || ent instanceof Squid || ent instanceof Villager
	    || ent instanceof Rabbit);
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
	return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack itemInMainHand(Player player) {
	return player.getInventory().getItemInHand();
    }

    @Override
    public ItemStack itemInOffHand(Player player) {
	return null;
    }

    @Override
    public boolean isChorusTeleport(TeleportCause tpcause) {
	return false;
    }

    @Override
    public void playEffect(Player player, Location location, CMIEffect ef) {
	if (location == null || ef == null || location.getWorld() == null)
	    return;
	Packet packet = null;
	if (ef.getParticle().getEffect() == null)
	    return;
	if (!ef.getParticle().isParticle()) {
//	    int packetData = effect.getId();
//	    packet = new PacketPlayOutWorldEvent(packetData, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), id, false);
	} else {
	    Effect effect = ef.getParticle().getEffect();

	    EnumParticle particle = ef.getParticle().getEnumParticle() == null ? null : (EnumParticle) ef.getParticle().getEnumParticle();
	    int[] extra = ef.getParticle().getExtra();
	    if (particle == null) {
		for (EnumParticle p : EnumParticle.values()) {
		    if (effect.name().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			particle = p;
			if (ef.getParticle().getEffect().getData() != null) {
			    if (ef.getParticle().getEffect().equals(org.bukkit.Material.class)) {
				extra = new int[] { 0 };
			    } else {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			}
			break;
		    }
		    if (ef.getParticle().getName().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			particle = p;
			if (ef.getParticle().getEffect().getData() != null) {
			    if (ef.getParticle().getEffect().equals(org.bukkit.Material.class)) {
				extra = new int[] { 0 };
			    } else {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			}
			break;
		    }
		    if (ef.getParticle().getSecondaryName().replace("_", "").equalsIgnoreCase((p.toString().replace("_", "")))) {
			particle = p;
			if (ef.getParticle().getEffect().getData() != null) {
			    if (ef.getParticle().getEffect().equals(org.bukkit.Material.class)) {
				extra = new int[] { 0 };
			    } else {
				extra = new int[] { (0 << 12) | (0 & 0xFFF) };
			    }
			}
			break;
		    }
		}
		if (extra == null) {
		    extra = new int[0];
		}
	    }

	    if (particle == null)
		return;

	    if (ef.getParticle().getEnumParticle() == null) {
		ef.getParticle().setEnumParticle(particle);
		ef.getParticle().setExtra(extra);
	    }

	    packet = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(), (float) ef.getOffset().getY(),
		(float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount(), extra);
	}
	CraftPlayer cPlayer = (CraftPlayer) player;
	if (cPlayer.getHandle().playerConnection == null)
	    return;

	if (!location.getWorld().equals(cPlayer.getWorld()))
	    return;

	if (packet == null)
	    return;
	cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}
