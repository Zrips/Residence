package com.bekvon.bukkit.residence.listenersCache;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerCollideWithEntityCache {

    private static final Cache<PlayerCollideWithEntityKey, Boolean> PLAYER_COLLIDE_WITH_ENTITY_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .build();

    private PlayerCollideWithEntityCache() {
    }

    public static boolean getOrCompute(PlayerCollideWithEntityKey key, BooleanSupplier loader) {
        try {
            // On cache miss or expiry, compute via loader and store the result
            return PLAYER_COLLIDE_WITH_ENTITY_CACHE.get(key, loader::getAsBoolean);
        } catch (ExecutionException e) {
            return true;
        }
    }

    public static final class PlayerCollideWithEntityKey {

        private final UUID playerUuid;
        private final UUID entityUuid;

        public PlayerCollideWithEntityKey(@NotNull Player player, @NotNull Entity entity) {
            this.playerUuid = player.getUniqueId();
            this.entityUuid = entity.getUniqueId();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PlayerCollideWithEntityKey)) {
                return false;
            }
            PlayerCollideWithEntityKey other = (PlayerCollideWithEntityKey) obj;
            return playerUuid.equals(other.playerUuid) && entityUuid.equals(other.entityUuid);
        }

        @Override
        public int hashCode() {
            int result =playerUuid.hashCode();
            result = 31 * result + entityUuid.hashCode();
            return result;
        }
    }
}
