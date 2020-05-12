package com.bekvon.bukkit.residence.containers;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMS {

    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event);

    public boolean isAnimal(Entity ent);

    public boolean isArmorStandEntity(EntityType entityType);

    public boolean isSpectator(GameMode mode);

    public boolean isMainHand(PlayerInteractEvent event);

    public ItemStack itemInMainHand(Player player);

    public boolean isChorusTeleport(TeleportCause tpcause);

//    void playEffect(Player player, Location location, CMIEffect ef);

    ItemStack itemInOffHand(Player player);
}
