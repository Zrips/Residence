package com.bekvon.bukkit.residence.listeners;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listenersCache.DenyMessageCache;
import com.bekvon.bukkit.residence.listenersCache.PlayerCollideWithEntityCache;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Utils;

import io.papermc.paper.event.entity.EntityCollideWithEntityEvent;

public class ResidenceListener26_2_Paper implements Listener {

    private Residence plugin;

    public ResidenceListener26_2_Paper(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCollideWithEntity(EntityCollideWithEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.push.isGlobalyEnabled()) {
            return;
        }
        // Get the two entities involved in the collision
        List<Entity> entities = event.getEntities();
        if (entities.size() < 2) {
            return;
        }
        Entity entity1 = entities.get(0);
        // disabling event on world
        if (plugin.isDisabledWorldListener(entity1.getWorld())) {
            return;
        }
        Entity entity2 = entities.get(1);
        Player player;
        Entity other;
        if (entity1 instanceof Player) {
            player = (Player) entity1;
            other = entity2;

        } else if (entity2 instanceof Player) {
            player = (Player) entity2;
            other = entity1;

        } else {
            // Only handle entity pushes involving a player
            return;
        }
        PlayerCollideWithEntityCache.PlayerCollideWithEntityKey key
                = new PlayerCollideWithEntityCache.PlayerCollideWithEntityKey(player, other);

        if (PlayerCollideWithEntityCache.getOrCompute(key, () -> shouldDenyPush(player, other))) {
            if (DenyMessageCache.shouldSendDenyMessage(player, Flags.push)) {
                lm.Flag_Deny.sendMessage(player, Flags.push);
            }
            event.setCancelled(true);
        }
    }

    private boolean shouldDenyPush(@NotNull Player player, @NotNull Entity other) {
        if (player.hasMetadata("NPC") || ResAdmin.isResAdmin(player)) {
            return false;
        }
        FlagPermissions perms = FlagPermissions.getPerms(other.getLocation(), player);

        if (Utils.isAnimal(other)) {
            return !perms.playerHas(player, Flags.push, perms.playerHas(player, Flags.animalkilling, true));

        } else if (ResidenceEntityListener.isMonster(other)) {
            return !perms.playerHas(player, Flags.push, perms.playerHas(player, Flags.mobkilling, true));

        } else if (other instanceof Vehicle) {
            return !perms.playerHas(player, Flags.push, perms.playerHas(player, Flags.vehicledestroy, true));

        }
        return !perms.playerHas(player, Flags.push, true);
    }
}
