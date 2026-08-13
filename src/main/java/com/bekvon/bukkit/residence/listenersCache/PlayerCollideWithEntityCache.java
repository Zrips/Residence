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

        private final UUID entityUuid;
        private final UUID playerUuid;

        public PlayerCollideWithEntityKey(@NotNull Entity entity, @NotNull Player player) {
            this.entityUuid = entity.getUniqueId();
            this.playerUuid = player.getUniqueId();
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
            return entityUuid.equals(other.entityUuid) && playerUuid.equals(other.playerUuid);
        }

        @Override
        public int hashCode() {
            int result = entityUuid.hashCode();
            result = 31 * result + playerUuid.hashCode();
            return result;
        }
    }
}
