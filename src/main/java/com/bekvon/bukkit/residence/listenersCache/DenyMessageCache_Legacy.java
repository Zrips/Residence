package com.bekvon.bukkit.residence.listenersCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.bekvon.bukkit.residence.containers.Flags;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/*
Legacy implementation for versions below 1.16
Suppress frequent duplicate Flag-Deny messages
*/
public class DenyMessageCache_Legacy implements Listener {

    private static final Map<DenyMessageKey, Long> CACHE = new HashMap<>();

    private static long expireNanos = TimeUnit.SECONDS.toNanos(1);

    public DenyMessageCache_Legacy() {
    }

    public static void reloadDenyMessageCache(int expireSeconds) {
        expireNanos = TimeUnit.SECONDS.toNanos(expireSeconds);
        CACHE.clear();
    }

    public static boolean shouldSendDenyMessage(@NotNull Player player, @NotNull Flags flag) {

        long nowTime = System.nanoTime();
        long expireTime = expireNanos;

        DenyMessageKey key = new DenyMessageKey(player, flag);
        Long lastWriteTime = CACHE.get(key);

        if (lastWriteTime == null || (nowTime - lastWriteTime >= expireTime)) {
            CACHE.put(key, nowTime);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        CACHE.keySet().removeIf(key -> uuid.equals(key.playerUuid));
    }

    private static final class DenyMessageKey {

        private final UUID playerUuid;
        private final Flags flag;

        private DenyMessageKey(@NotNull Player player, @NotNull Flags flag) {
            this.playerUuid = player.getUniqueId();
            this.flag = flag;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DenyMessageKey)) {
                return false;
            }
            DenyMessageKey other = (DenyMessageKey) obj;
            return playerUuid.equals(other.playerUuid) && flag.equals(other.flag);
        }

        @Override
        public int hashCode() {
            return 31 * playerUuid.hashCode() + flag.hashCode();
        }
    }
}
