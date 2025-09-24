package com.bekvon.bukkit.residence.protection;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidencePlayerInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class PlayerManager implements ResidencePlayerInterface {
    private ConcurrentHashMap<String, ResidencePlayer> playersByName = new ConcurrentHashMap<String, ResidencePlayer>();
    private ConcurrentHashMap<UUID, ResidencePlayer> playersByUUID = new ConcurrentHashMap<UUID, ResidencePlayer>();

    private Set<UUID> toBeSaved = ConcurrentHashMap.newKeySet();

    private Residence plugin;

    public PlayerManager(Residence plugin) {
        this.plugin = plugin;
    }

    public static UUID createTempUUID(String playerName) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(playerName.getBytes(StandardCharsets.UTF_8));

            long msb = 0xFFFFFFFF00000000L
                | ((hash[0] & 0xFFL) << 24)
                | ((hash[1] & 0xFFL) << 16)
                | ((hash[2] & 0xFFL) << 8)
                | (hash[3] & 0xFFL);
            long lsb = 0;
            for (int i = 4; i < 12; i++) {
                lsb = (lsb << 8) | (hash[i] & 0xFFL);
            }
            return new UUID(msb, lsb);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isTempUUID(UUID id) {
        return (id.getMostSignificantBits() >>> 32) == 0xFFFFFFFFL;
    }

    public static UUID getSenderUUID(CommandSender sender) {
        if (sender == null)
            return Residence.getInstance().getServerUUID();
        if (sender instanceof Player)
            return ((Player) sender).getUniqueId();
        return Residence.getInstance().getServerUUID();
    }

    public static boolean isPlayerExist(CommandSender sender, String name, boolean inform) {
        if (Residence.getInstance().getPlayerManager().getUUID(name) != null)
            return true;
        if (inform)
            lm.Invalid_Player.sendMessage(sender);
        return false;
    }

    @Deprecated
    public @Nullable String getName(String uuid) {
        ResidencePlayer pc = getResidencePlayer(uuid);
        return pc == null ? null : pc.getName();
    }

    public @Nullable String getName(UUID uuid) {

        if (Residence.getInstance().getServerUUID().equals(uuid))
            return Residence.getInstance().getServerLandName();

        ResidencePlayer pc = getResidencePlayer(uuid);
        return pc == null ? null : pc.getName();
    }

    public @Nullable UUID getUUID(String name) {
        ResidencePlayer pc = getResidencePlayer(name);

        if (pc == null)
            return null;

        return pc.getUniqueId();
    }

    public void updateUserName(String from, String to, ResidencePlayer residencePlayer) {
        ResidencePlayer byName = from == null ? null : playersByName.remove(from.toLowerCase());
        if (byName != null)
            playersByName.put(to.toLowerCase(), byName);
        else
            playersByName.put(to.toLowerCase(), residencePlayer);
    }

    public void addPlayer(ResidencePlayer resPlayer) {
        if (resPlayer == null)
            return;

        String name = resPlayer.getName();
        UUID uuid = resPlayer.getUniqueId();

        // Removing record based on temp UUID
        if (name != null) {
            ResidencePlayer byName = playersByName.get(name.toLowerCase());
            if (byName != null && isTempUUID(byName.getUniqueId())) {
                ResidencePlayer byTempUUID = playersByUUID.remove(byName.getUniqueId());
                if (byTempUUID != null) {

                    renameFile(byTempUUID.getUniqueId(), uuid);

                    byTempUUID.setUniqueId(resPlayer.getUniqueId());

                    playersByUUID.put(byTempUUID.getUniqueId(), byTempUUID);

                    if (!byName.equals(byTempUUID)) {
                        playersByName.remove(name.toLowerCase());
                        playersByName.put(name.toLowerCase(), byTempUUID);
                    }

                    byTempUUID.save();

                    return;
                }
            }
        }

        if (name != null) {
            playersByName.put(name.toLowerCase(), resPlayer);
        }

        if (uuid != null) {
            playersByUUID.put(uuid, resPlayer);
            if (!resPlayer.isSaved())
                resPlayer.save();
        }
    }

    public void addPlayer(@Nonnull String name, @Nonnull UUID uuid) {

        ResidencePlayer rp = getResidencePlayer(uuid);

//        if (rp == null)
//            rp = getResidencePlayer(name);

        if (rp == null && name != null && uuid != null) {
            rp = new ResidencePlayer(name, uuid);
            addPlayer(rp);
            return;
        }
    }

    public ResidencePlayer playerJoin(Player player) {
        return playerJoin((OfflinePlayer) player);
    }

    public ResidencePlayer playerJoin(OfflinePlayer player) {
        ResidencePlayer resPlayer = playersByUUID.get(player.getUniqueId());

        if (resPlayer == null) {
            resPlayer = new ResidencePlayer(player);
            addPlayer(resPlayer);
            resPlayer.save();
        } else {
            resPlayer.updatePlayer(player);
        }
        return resPlayer;
    }

    public @Nullable ResidencePlayer playerJoin(String player, UUID uuid) {
        if (!playersByName.containsKey(player.toLowerCase())) {
            ResidencePlayer resPlayer = new ResidencePlayer(player, uuid);
            addPlayer(resPlayer);
            return resPlayer;
        }
        return null;
    }

    public int getResidenceCount(UUID uuid) {
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null)
            return resPlayer.getResList().size();
        return 0;
    }

    @Override
    public ArrayList<String> getResidenceList(UUID uuid) {
        ArrayList<String> temp = new ArrayList<String>();
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null) {
            for (ClaimedResidence one : resPlayer.getResList()) {
                temp.add(one.getName());
            }
            return temp;
        }
        return temp;
    }

    @Override
    @Deprecated
    public ArrayList<String> getResidenceList(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null)
            return getResidenceList(player.getUniqueId());
        ArrayList<String> temp = new ArrayList<String>();
        ResidencePlayer resPlayer = this.getResidencePlayer(name);
        if (resPlayer != null) {
            for (ClaimedResidence one : resPlayer.getResList()) {
                temp.add(one.getName());
            }
            return temp;
        }
        return temp;
    }

    @Override
    @Deprecated
    public ArrayList<String> getResidenceList(String player, boolean showhidden) {
        return getResidenceList(player, showhidden, false);
    }

    @Deprecated
    public ArrayList<String> getResidenceList(String player, boolean showhidden, boolean onlyHidden) {
        ArrayList<String> temp = new ArrayList<String>();
        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer == null)
            return temp;
        for (ClaimedResidence one : resPlayer.getResList()) {
            boolean hidden = one.getPermissions().has("hidden", false);
            if (!showhidden && hidden)
                continue;

            if (onlyHidden && !hidden)
                continue;

            temp.add(lm.Residence_List.getMessage("", one.getName(), one.getWorldName()) + (hidden ? lm.Residence_Hidden.getMessage() : ""));
        }
        Collections.sort(temp, String.CASE_INSENSITIVE_ORDER);
        return temp;
    }

    @Deprecated
    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden) {
        return getResidences(player, showhidden, false);
    }

    @Deprecated
    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden, boolean onlyHidden) {
        return getResidences(player, showhidden, onlyHidden, null);
    }

    @Deprecated
    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden, boolean onlyHidden, World world) {
        ArrayList<ClaimedResidence> temp = new ArrayList<ClaimedResidence>();
        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer == null)
            return temp;
        for (ClaimedResidence one : resPlayer.getResList()) {
            boolean hidden = one.getPermissions().has(Flags.hidden, false);
            if (!showhidden && hidden)
                continue;
            if (onlyHidden && !hidden)
                continue;
            if (world != null && !world.getName().equalsIgnoreCase(one.getWorldName()))
                continue;
            temp.add(one);
        }
        return temp;
    }

    @Deprecated
    public TreeMap<String, ClaimedResidence> getResidencesMap(String player, boolean showhidden, boolean onlyHidden, World world) {

        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer == null)
            return new TreeMap<String, ClaimedResidence>();

        return getResidencesMap(resPlayer, showhidden, onlyHidden, world);
    }

    public TreeMap<String, ClaimedResidence> getResidencesMap(UUID uuid, boolean showhidden, boolean onlyHidden, World world) {
        ResidencePlayer resPlayer = this.getResidencePlayer(uuid);
        if (resPlayer == null)
            return new TreeMap<String, ClaimedResidence>();
        return getResidencesMap(resPlayer, showhidden, onlyHidden, world);
    }

    public TreeMap<String, ClaimedResidence> getResidencesMap(ResidencePlayer resPlayer, boolean showhidden, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> temp = new TreeMap<String, ClaimedResidence>();

        if (resPlayer == null)
            return temp;

        for (ClaimedResidence one : resPlayer.getResList()) {
            boolean hidden = one.getPermissions().has(Flags.hidden, false);
            if (!showhidden && hidden) {
                continue;
            }
            if (onlyHidden && !hidden)
                continue;
            if (world != null && !world.getName().equalsIgnoreCase(one.getWorldName()))
                continue;
            temp.put(one.getName(), one);
        }
        return temp;
    }

    @Deprecated
    public TreeMap<String, ClaimedResidence> getTrustedResidencesMap(String player, boolean showhidden, boolean onlyHidden, World world) {
        return getTrustedResidencesMap(this.getResidencePlayer(player), showhidden, onlyHidden, world);
    }

    public TreeMap<String, ClaimedResidence> getTrustedResidencesMap(UUID uuid, boolean showhidden, boolean onlyHidden, World world) {
        return getTrustedResidencesMap(this.getResidencePlayer(uuid), showhidden, onlyHidden, world);
    }

    public TreeMap<String, ClaimedResidence> getTrustedResidencesMap(ResidencePlayer resPlayer, boolean showhidden, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> temp = new TreeMap<String, ClaimedResidence>();

        if (resPlayer == null) {
            return temp;
        }

        Iterator<ClaimedResidence> iter = resPlayer.getTrustedResidenceList().iterator();
        while (iter.hasNext()) {
            ClaimedResidence one = iter.next();
            boolean hidden = one.getPermissions().has(Flags.hidden, false);
            if (!showhidden && hidden)
                continue;
            if (onlyHidden && !hidden)
                continue;
            if (world != null && !world.getName().equalsIgnoreCase(one.getWorldName()))
                continue;
            if (!one.isTrusted(resPlayer)) {
                iter.remove();
                continue;
            }
            temp.put(one.getName(), one);
        }
        return temp;
    }

    public ResidencePlayer getResidencePlayer(OfflinePlayer player) {
        if (player == null)
            return null;

        ResidencePlayer resPlayer = getResidencePlayer(player.getUniqueId());

        if (resPlayer != null)
            return resPlayer.updatePlayer(player);

        return playerJoin(player);
    }

    public ResidencePlayer getResidencePlayer(Player player) {
        if (player == null)
            return null;

        ResidencePlayer resPlayer = getResidencePlayer(player.getUniqueId());

        if (resPlayer != null)
            return resPlayer.updatePlayer(player);

        return playerJoin(player);
    }

    @Override
    public @Nullable ResidencePlayer getResidencePlayer(String player) {
        if (player == null)
            return null;

        if (player.equalsIgnoreCase("CONSOLE"))
            return null;

        ResidencePlayer rplayer = playersByName.get(player.toLowerCase());
        if (rplayer != null)
            return rplayer;

        if (player.length() == 36 && player.contains("-")) {
            try {
                UUID uuid = UUID.fromString(player);
                return getResidencePlayer(uuid);
            } catch (Exception e) {
            }
        }

        return null;
    }

    public @Nullable ResidencePlayer getResidencePlayer(UUID uuid) {
        if (uuid == null)
            return null;
        return playersByUUID.get(uuid);
    }

    public void addResidence(Player player, ClaimedResidence residence) {
        addResidence(player.getUniqueId(), residence);
    }

    public void addResidence(UUID uuid, ClaimedResidence residence) {
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null) {
            resPlayer.addResidence(residence);
        }
        try {
            for (Entry<UUID, Map<String, Boolean>> one : residence.getPermissions().getPlayerFlags().entrySet()) {
                if (!residence.isTrusted(one.getKey()))
                    continue;
                ResidencePlayer rplayer = getResidencePlayer(one.getKey());
                if (rplayer == null)
                    continue;
                rplayer.addTrustedResidence(residence);
            }

            // Deprecated
            for (Entry<String, Map<String, Boolean>> one : residence.getPermissions().getPlayerFlagsByName().entrySet()) {
                String name = one.getKey();
                if (!residence.isTrusted(name))
                    continue;
                ResidencePlayer rplayer = getResidencePlayer(one.getKey());
                if (rplayer == null)
                    continue;
                rplayer.addTrustedResidence(residence);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return;
    }

    public void removeResFromPlayer(ClaimedResidence residence) {
        if (residence == null)
            return;
        removeResFromPlayer(residence.getOwnerUUID(), residence);
    }

    public void removeResFromPlayer(OfflinePlayer player, ClaimedResidence residence) {
        removeResFromPlayer(player.getUniqueId(), residence);
    }

    public void removeResFromPlayer(Player player, ClaimedResidence residence) {
        removeResFromPlayer(player.getUniqueId(), residence);
    }

    public void removeResFromPlayer(UUID uuid, ClaimedResidence residence) {
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null)
            resPlayer.removeResidence(residence);
    }

    private static CMITask saveTask = null;

    public void onPluginStop() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
            saveDelayed();
        }
    }

    public void save() {
        if (saveTask != null)
            return;

        if (toBeSaved.isEmpty())
            return;

        saveTask = CMIScheduler.runLaterAsync(Residence.getInstance(), () -> {
            saveDelayed();
            saveTask = null;
        }, 20);
    }

    private static final String folderName = "PlayerData";

    private void saveDelayed() {

        File folder = new File(Residence.getInstance().getDataFolder(), "Save" + File.separator + folderName);

        for (UUID uuid : getForSave()) {

            ResidencePlayer pc = getResidencePlayer(uuid);

            File file = new File(folder, uuid.toString() + ".yml");

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            for (Map.Entry<String, Object> e : pc.serialize().entrySet()) {
                yaml.set(e.getKey(), e.getValue());
            }

            try {
                yaml.save(file);
                remove(uuid);
                pc.setSaved(true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void addForSave(UUID uuid) {
        toBeSaved.add(uuid);
        save();
    }

    private Set<UUID> getForSave() {
        return new HashSet<>(toBeSaved);
    }

    private void remove(UUID id) {
        toBeSaved.remove(id);
    }

    private void cacheOfflinePlayers() {

        lm.consoleMessage("Preloading player data. This is one time thing. Wait until finished");

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
                addPlayer(name, player.getUniqueId());
            } catch (Exception e) {
                lm.consoleMessage("Failed to cache data of a player " + player.getUniqueId() + " (" + i + "/" + total + ")");
            }

            if (i % 1000 == 0) {
                lm.consoleMessage("Cached data (" + i + "/" + total + ")");
            }
        }

        save();
    }

    public void load() {

        File folder = new File(Residence.getInstance().getDataFolder(), "Save");
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
        boolean loadOffline = false;

        folder = new File(Residence.getInstance().getDataFolder(), "Save" + File.separator + folderName);
        if (!folder.isDirectory()) {
            folder.mkdir();
            loadOffline = true;
        }

        if (loadOffline) {
            cacheOfflinePlayers();
        }

        if (!folder.exists() || !folder.isDirectory()) {
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null)
            return;

        int i = 0;
        for (File playerFile : files) {

            String name = playerFile.getName();

            if (!name.toLowerCase().endsWith(".yml"))
                continue;
            String base = name.substring(0, name.length() - 4);
            UUID uuid = null;
            try {
                uuid = UUID.fromString(base);
            } catch (IllegalArgumentException e) {
                continue;
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(playerFile);

            Map<String, Object> result = sectionToMap(yaml);

            ResidencePlayer rc = ResidencePlayer.deserialize(uuid, result);

            if (rc != null) {
                rc.setSaved(true);
                addPlayer(rc);
                i++;
            }
        }

        lm.consoleMessage("Preloaded " + i + " player cached data");
    }

    private static void renameFile(UUID from, UUID to) {

        CMIScheduler.runTaskAsynchronously(Residence.getInstance(), () -> {
            if (!isTempUUID(from)) {
                return;
            }

            File folder = new File(Residence.getInstance().getDataFolder(), "Save" + File.separator + folderName);
            if (!folder.isDirectory())
                folder.mkdir();

            File fromFile = new File(folder, from.toString() + ".yml");
            if (!fromFile.isFile()) {
                return;
            }

            File toFile = new File(folder, to.toString() + ".yml");

            fromFile.renameTo(toFile);
        });
    }

    private static Map<String, Object> sectionToMap(ConfigurationSection section) {
        Map<String, Object> out = new HashMap<>();
        for (String k : section.getKeys(false)) {
            Object val = section.get(k);
            if (val instanceof ConfigurationSection) {
                out.put(k, sectionToMap((ConfigurationSection) val));
            } else {
                out.put(k, val);
            }
        }
        return out;
    }
}
