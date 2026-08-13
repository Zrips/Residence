package com.bekvon.bukkit.residence.listeners;

import java.util.List;

import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
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
        Entity target;
        Player pushedBy;

        if (entity1 instanceof Player) {
            pushedBy = (Player) entity1;
            target = entity2;

        } else if (entity2 instanceof Player) {
            pushedBy = (Player) entity2;
            target = entity1;

        } else {
            // Only handle entity pushes involving a player
            return;
        }
        PlayerCollideWithEntityCache.PlayerCollideWithEntityKey key
                = new PlayerCollideWithEntityCache.PlayerCollideWithEntityKey(target, pushedBy);

        if (PlayerCollideWithEntityCache.getOrCompute(key, () -> shouldDenyPush(target, pushedBy))) {
            event.setCancelled(true);
        }
    }

    private boolean shouldDenyPush(@NotNull Entity target, @NotNull Player pushedBy) {
        // Does not apply to player-player collisions; they are handled client-side
        if (target instanceof Player) {
            return false;
        }
        if (pushedBy.hasMetadata("NPC") || ResAdmin.isResAdmin(pushedBy)) {
            return false;
        }
        FlagPermissions perms = FlagPermissions.getPerms(target.getLocation(), pushedBy);
        boolean fallback = true;

        if (target instanceof Boat || target instanceof Minecart) {
            fallback = perms.playerHas(pushedBy, Flags.vehicledestroy, true);

        } else if (Utils.isAnimal(target)) {
            fallback = perms.playerHas(pushedBy, Flags.animalkilling, true);

        } else if (ResidenceEntityListener.isMonster(target)) {
            fallback = perms.playerHas(pushedBy, Flags.mobkilling, true);

        }
        if (!perms.playerHas(pushedBy, Flags.push, fallback)) {
            if (DenyMessageCache.shouldSendDenyMessage(pushedBy, Flags.push)) {
                lm.Flag_Deny.sendMessage(pushedBy, Flags.push);
            }
            return true;
        }
        return false;
    }
}
