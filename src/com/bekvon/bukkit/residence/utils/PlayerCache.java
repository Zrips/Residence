package com.bekvon.bukkit.residence.utils;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class PlayerCache {

    private static ConcurrentHashMap<UUID, PlayerCache> cacheByUUID = new ConcurrentHashMap<UUID, PlayerCache>();
    private static HashMap<String, PlayerCache> cacheByName = new HashMap<String, PlayerCache>();

    private UUID uuid = null;
    private String name = null;

    public PlayerCache(Player player) {
        this(player.getName(), player.getUniqueId());
    }

    public PlayerCache(String name, UUID uuid) {
        this.setUniqueId(uuid);
        this.setName(name);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean isPlayerExist(CommandSender sender, String name, boolean inform) {
        if (getUUID(name) != null)
            return true;
        if (inform)
            lm.Invalid_Player.sendMessage(sender);
        return false;
    }

    public static UUID getSenderUUID(CommandSender sender) {
        if (sender == null)
            return Residence.getInstance().getEmptyUserUUID();
        if (sender instanceof Player)
            return ((Player) sender).getUniqueId();
        return Residence.getInstance().getServerUUID();
    }

    public static void addToCache(Player player) {
        addToCache(player.getName(), player.getUniqueId());
    }

    public static @Nullable PlayerCache addToCache(@Nonnull String name, @Nonnull UUID uuid) {

        if (name == null || uuid == null)
            return null;
        
        if (uuid.equals(Residence.getInstance().getEmptyUserUUID()))
            return null;

        PlayerCache pc = cacheByUUID.get(uuid);

        synchronized (cacheByUUID) {

            if (pc == null) {
                save();
                pc = new PlayerCache(name, uuid);
            }

            cacheByUUID.put(uuid, pc);
            cacheByName.put(name.toLowerCase(), pc);
        }
        return pc;
    }

    public static @Nullable PlayerCache get(String name) {
        if (name == null)
            return null;
        PlayerCache cachedRecord = cacheByName.get(name.toLowerCase());
        
        if (cachedRecord == null && name.length() == 36) {
            try {
                UUID uuid = UUID.fromString(name);
                return get(uuid);
            } catch (Exception e) { 
            }
        }
        return cachedRecord;
    }

    public static @Nullable PlayerCache get(UUID uuid) {
        if (uuid == null)
            return null;
        return cacheByUUID.get(uuid);
    }

    @Deprecated
    public static @Nullable String getName(String uuid) {
        PlayerCache pc = get(uuid);
        return pc == null ? null : pc.getName();
    }

    public static @Nullable String getName(UUID uuid) {
        PlayerCache pc = get(uuid);
        return pc == null ? null : pc.getName();
    }

    public static @Nullable UUID getUUID(String name) {
        PlayerCache pc = get(name);
        return pc == null ? null : pc.getUniqueId();
    }

    private static CMITask saveTask = null;
    private static CompletableFuture<Void> importTask = null;

    public static void onPluginStop() {
        if (importTask != null) {
            importTask.cancel(true);
            importTask = null;
        }

        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
            saveDelayed();
        }
    }

    public static void save() {
        if (saveTask != null)
            return;
        saveTask = CMIScheduler.runLaterAsync(Residence.getInstance(), () -> {
            saveDelayed();
            saveTask = null;
        }, 200);
    }

    private static final String fileName = "playerNameCache.yml";

    private static void saveDelayed() {

        if (Version.isTestServer())
	    return;
        
        ConfigReader cfg = null;
        try {
            cfg = new ConfigReader(Residence.getInstance(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cfg == null)
            return;

        cfg.load();

        for (PlayerCache pc : cacheByUUID.values()) {
            cfg.set(pc.getUniqueId().toString(), pc.getName());
        }

        cfg.save();
    }

    private static void cacheOfflinePlayers() {
        importTask = CMIScheduler.runTaskAsynchronously(Residence.getInstance(), () -> {
            int total = Bukkit.getOfflinePlayers().length;
            int i = 0;
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                i++;
                if (player == null)
                    continue;
                try {
                    String name = player.getName();
                    if (name == null)
                        continue;

                    addToCache(name, player.getUniqueId());

                } catch (Exception e) {
                    CMIMessages.consoleMessage("[Residence] Failed to cache data of a player " + player.getUniqueId() + " (" + i + "/" + total + ")");
                }

                if (!Residence.getInstance().isFullyLoaded())
                    return;
            }
        });
    }

    public static void load() {

        File file = new File(Residence.getInstance().getDataFolder(), fileName);
        if (!file.isFile()) {
            cacheOfflinePlayers();
        }

        ConfigReader cfg = null;
        try {
            cfg = new ConfigReader(Residence.getInstance(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cfg == null)
            return;

        for (String s : cfg.getC().getKeys(false)) {
            try {
                addToCache(cfg.getC().getString(s), UUID.fromString(s));
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }

        CMIMessages.consoleMessage("Preloaded " + cacheByUUID.size() + " player cached data");
    }
}
