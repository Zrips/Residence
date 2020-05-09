package com.bekvon.bukkit.residence.allNms;

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.residence.containers.NMS;
import net.minecraft.server.v1_7_R4.PacketPlayOutWorldParticles;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class v1_7_R4 implements NMS {
    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
        List<Block> blocks = new ArrayList<Block>();
        blocks.add(event.getBlock());
        return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
        return (ent instanceof Horse || ent instanceof Bat || ent instanceof Snowman || ent instanceof IronGolem || ent instanceof Ocelot || ent instanceof Pig
                || ent instanceof Sheep || ent instanceof Chicken || ent instanceof Wolf || ent instanceof Cow || ent instanceof Squid || ent instanceof Villager);
    }

    @Override
    public boolean isArmorStandEntity(EntityType ent) {
        return false;
    }

    @Override
    public boolean isSpectator(GameMode mode) {
        return false;
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
        CraftPlayer cPlayer = (CraftPlayer) player;
        if (cPlayer.getHandle().playerConnection == null)
            return;

        Effect effect = ef.getParticle().getEffect();
        if (effect == null)
            return;
        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(effect.name(), (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
                (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());
        cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}
