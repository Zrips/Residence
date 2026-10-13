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
import com.bekvon.bukkit.residence.listenersCache.PlayerCollideWithEntityCache;
import com.bekvon.bukkit.residence.listenersCache.PlayerCollideWithEntityCache.PlayerCollideWithEntityKey;
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
        // Get the two entities involved in the collision
        List<Entity> entities = event.getEntities();
        if (entities.size() < 2) {
            return;
        }
        Entity entity1 = entities.get(0);

        if (FlagPermissions.shouldIgnoreCheck(Flags.push, entity1)) {
            return;
        }
        Entity entity2 = entities.get(1);
        Entity target;
        Player pushedBy;

        if (entity1 instanceof Player) {
            // Does not apply to player-player collisions; they are handled client-side
            if (entity2 instanceof Player) {
                return;
            }
            pushedBy = (Player) entity1;
            target = entity2;

        } else if (entity2 instanceof Player) {
            pushedBy = (Player) entity2;
            target = entity1;

        } else {
            // Only handle entity pushes involving a player
            return;
        }
        PlayerCollideWithEntityKey key = new PlayerCollideWithEntityKey(target, pushedBy);
        // Collisions are high-frequency events; caching is more lightweight
        if (PlayerCollideWithEntityCache.getOrCompute(key, () -> shouldDenyPush(target, pushedBy))) {
            event.setCancelled(true);
        }
    }

    private boolean shouldDenyPush(@NotNull Entity target, @NotNull Player pushedBy) {
        Flags subFlag = null;

        if (target instanceof Boat || target instanceof Minecart) {
            subFlag = Flags.vehicledestroy;

        } else if (Utils.isAnimal(target)) {
            subFlag = Flags.animalkilling;

        } else if (Utils.isMonster(target)) {
            subFlag = Flags.mobkilling;

        }
        return FlagPermissions.shouldDenyAndNotify(pushedBy, target, Flags.push, subFlag);
    }
}
