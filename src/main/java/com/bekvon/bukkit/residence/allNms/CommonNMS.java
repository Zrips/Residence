package com.bekvon.bukkit.residence.allNms;

import com.bekvon.bukkit.cmiLib.CMIEffect;
import com.bekvon.bukkit.residence.containers.NMS;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;

import java.util.ArrayList;
import java.util.List;

public class CommonNMS implements NMS {

    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
        List<Block> blocks = new ArrayList<Block>(event.getBlocks());
        return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
        return ent instanceof Animals || ent instanceof WaterMob || ent instanceof Merchant;
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
        throw new UnsupportedOperationException("Not implemented yet");
//        if (location == null || ef == null || location.getWorld() == null)
//            return;
//
//        CMIParticle effect = ef.getParticle();
//        if (effect == null)
//            return;
//        if (!effect.isParticle()) {
//            return;
//        }
//
//        org.bukkit.Particle particle = effect.getParticle();
//
//        if (particle == null)
//            return;
//
//        DustOptions dd = null;
//        if (particle.equals(org.bukkit.Particle.REDSTONE))
//            dd = new org.bukkit.Particle.DustOptions(ef.getColor(), ef.getSize());
//
//        Packet<?> packet = new PacketPlayOutWorldParticles(CraftParticle.toNMS(particle, dd), true, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) ef.getOffset().getX(),
//                (float) ef.getOffset().getY(), (float) ef.getOffset().getZ(), ef.getSpeed(), ef.getAmount());
//
//        CraftPlayer cPlayer = (CraftPlayer) player;
//        if (cPlayer.getHandle().playerConnection == null)
//            return;
//
//        if (!location.getWorld().equals(cPlayer.getWorld()))
//            return;
//
//        cPlayer.getHandle().playerConnection.sendPacket(packet);
    }
}
