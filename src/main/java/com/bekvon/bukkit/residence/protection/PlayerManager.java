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

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidencePlayerInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.ResidencePlayerMaxValues;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentManager;

import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

/**
 * Manages player data and residence ownership mappings.
 * Implements {@link ResidencePlayerInterface} to provide player-related API access.
 * Get an instance via {@code ResidenceApi.getPlayerManager()}.
 */
public class PlayerManager implements ResidencePlayerInterface {
    private ConcurrentHashMap<String, ResidencePlayer> playersByName = new ConcurrentHashMap<String, ResidencePlayer>();
    private ConcurrentHashMap<UUID, ResidencePlayer> playersByUUID = new ConcurrentHashMap<UUID, ResidencePlayer>();

    private Set<UUID> toBeSaved = ConcurrentHashMap.newKeySet();

    private Residence plugin;

    /**
     * Constructs a new PlayerManager.
     *
     * @param plugin Residence plugin instance
     */
    public PlayerManager(Residence plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a deterministic temporary UUID from a player name using SHA-1 hashing.
     * Used when a player's real UUID is not yet available.
     *
     * @param playerName Player name
     * @return A temporary UUID derived from the name, or null on error
     */
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

    /**
     * Checks whether a UUID was generated as a temporary UUID.
     *
     * @param id UUID to check
     * @return true if the UUID is a temporary UUID
     */
    public static boolean isTempUUID(UUID id) {
        return (id.getMostSignificantBits() >>> 32) == 0xFFFFFFFFL;
    }

    /**
     * Gets the UUID of a command sender. Returns the server UUID for console or null senders.
     *
     * @param sender Command sender
     * @return UUID of the player, or the server UUID for non-player senders
     */
    public static UUID getSenderUUID(CommandSender sender) {
        if (sender == null)
            return Residence.getInstance().getServerUUID();
        if (sender instanceof Player)
            return ((Player) sender).getUniqueId();
        return Residence.getInstance().getServerUUID();
    }

    /**
     * Checks if a player exists in the system by name.
     *
     * @param sender Command sender to notify on failure (may be null)
     * @param name   Player name to check
     * @param inform Whether to send an invalid player message to the sender
     * @return true if the player exists
     */
    public static boolean isPlayerExist(CommandSender sender, String name, boolean inform) {
        if (Residence.getInstance().getPlayerManager().getUUID(name) != null)
            return true;
        if (inform)
            lm.Invalid_Player.sendMessage(sender);
        return false;
    }

    /**
     * Gets the player name for a given UUID string.
     *
     * @param uuid UUID string
     * @return Player name, or null if not found
     * @deprecated Use {@link #getName(UUID)} instead
     */
    @Deprecated
    public @Nullable String getName(String uuid) {
        ResidencePlayer pc = getResidencePlayer(uuid);
        return pc == null ? null : pc.getName();
    }

    /**
     * Gets the player name for a given UUID. Returns the server land name for the server UUID.
     *
     * @param uuid Player UUID
     * @return Player name, or null if not found
     */
    public @Nullable String getName(UUID uuid) {

        if (Residence.getInstance().getServerUUID().equals(uuid))
            return Residence.getInstance().getServerLandName();

        ResidencePlayer pc = getResidencePlayer(uuid);
        return pc == null ? null : pc.getName();
    }

    /**
     * Gets the UUID for a given player name.
     *
     * @param name Player name
     * @return Player UUID, or null if not found
     */
    public @Nullable UUID getUUID(String name) {
        ResidencePlayer pc = getResidencePlayer(name);

        if (pc == null)
            return null;

        return pc.getUniqueId();
    }

    /**
     * Updates the name mapping for a player. Moves the entry from the old name to the new name.
     *
     * @param from           Old player name (may be null)
     * @param to             New player name
     * @param residencePlayer Player instance to use if old name not found
     */
    public void updateUserName(String from, String to, ResidencePlayer residencePlayer) {
        ResidencePlayer byName = from == null ? null : playersByName.remove(from.toLowerCase());
        if (byName != null)
            playersByName.put(to.toLowerCase(), byName);
        else
            playersByName.put(to.toLowerCase(), residencePlayer);
    }

    /**
     * Converts UUID references in rent and max-value records from one UUID to another.
     *
     * @param from Source UUID
     * @param to   Target UUID
     */
    private static void convertUUID(UUID from, UUID to) {
        RentManager.updateUUID(from, to);
        ResidencePlayerMaxValues.updateUUID(from, to);
    }

    /**
     * Adds a ResidencePlayer to the cache. If the player previously had a temporary UUID,
     * migrates data from the temp UUID to the real UUID.
     *
     * @param resPlayer ResidencePlayer to add
     */
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

                    UUID from = byTempUUID.getUniqueId();
                    UUID to = resPlayer.getUniqueId();

                    renameFile(from, uuid);

                    byTempUUID.setUniqueId(to);

                    playersByUUID.put(from, byTempUUID);

                    if (!byName.equals(byTempUUID)) {
                        playersByName.remove(name.toLowerCase());
                        playersByName.put(name.toLowerCase(), byTempUUID);
                    }

                    convertUUID(from, to);

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

    /**
     * Adds a player by name and UUID. Creates a new ResidencePlayer if one does not already exist.
     *
     * @param name Player name
     * @param uuid Player UUID
     * @return The existing or newly created ResidencePlayer, or null if creation failed
     */
    public ResidencePlayer addPlayer(@NotNull String name, @NotNull UUID uuid) {

        ResidencePlayer rp = getResidencePlayer(uuid);

        if (rp == null && name != null && uuid != null) {
            rp = new ResidencePlayer(name, uuid);
            addPlayer(rp);
        }

        return rp;
    }

    /**
     * Handles a player joining the server. Updates or creates the ResidencePlayer record.
     *
     * @param player Joining player
     * @return The player's ResidencePlayer instance
     */
    public @NotNull ResidencePlayer playerJoin(Player player) {
        return playerJoin((OfflinePlayer) player);
    }

    /**
     * Handles an offline player joining. Updates or creates the ResidencePlayer record,
     * saves it, and updates last-seen timestamp.
     *
     * @param player Offline player
     * @return The player's ResidencePlayer instance
     */
    public @NotNull ResidencePlayer playerJoin(OfflinePlayer player) {
        ResidencePlayer resPlayer = playersByUUID.get(player.getUniqueId());

        if (resPlayer == null) {
            resPlayer = new ResidencePlayer(player);
            addPlayer(resPlayer);
        } else {
            resPlayer.updatePlayer(player);
        }
        resPlayer.save();
        resPlayer.setLastSeen(System.currentTimeMillis());
        resPlayer.updateLastKnownWorld();

        return resPlayer;
    }

    /**
     * Handles a player join by name and UUID. Creates a new ResidencePlayer if not already cached.
     *
     * @param player Player name
     * @param uuid   Player UUID
     * @return New ResidencePlayer if created, or null if already exists
     */
    public @Nullable ResidencePlayer playerJoin(String player, UUID uuid) {
        if (!playersByName.containsKey(player.toLowerCase())) {
            ResidencePlayer resPlayer = new ResidencePlayer(player, uuid);
            addPlayer(resPlayer);
            return resPlayer;
        }
        return null;
    }

    /**
     * Gets the number of residences owned by a player.
     *
     * @param uuid Player UUID
     * @return Number of residences, or 0 if player not found
     */
    public int getResidenceCount(UUID uuid) {
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null)
            return resPlayer.getResList().size();
        return 0;
    }

    /**
     * Get residence list by player UUID
     *
     * @param uuid Player UUID
     * @return List of residence names
     */
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

    /**
     * Get residence list by player name
     *
     * @param name Player name
     * @return List of residence names
     * @deprecated Use {@link #getResidenceList(UUID)} instead
     */
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

    /**
     * Get residence list by player name with hidden filter
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @return List of formatted residence strings
     * @deprecated Use UUID-based methods instead
     */
    @Override
    @Deprecated
    public ArrayList<String> getResidenceList(String player, boolean showhidden) {
        return getResidenceList(player, showhidden, false);
    }

    /**
     * Get residence list by player name with hidden and only-hidden filters.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @return List of formatted residence strings
     * @deprecated Use UUID-based methods instead
     */
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

    /**
     * Get residences owned by a player with hidden filter.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @return List of ClaimedResidence objects
     * @deprecated Use UUID-based methods instead
     */
    @Deprecated
    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden) {
        return getResidences(player, showhidden, false);
    }

    /**
     * Get residences owned by a player with hidden and only-hidden filters.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @return List of ClaimedResidence objects
     * @deprecated Use UUID-based methods instead
     */
    @Deprecated
    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden, boolean onlyHidden) {
        return getResidences(player, showhidden, onlyHidden, null);
    }

    /**
     * Get residences owned by a player with hidden, only-hidden, and world filters.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return List of ClaimedResidence objects
     * @deprecated Use UUID-based methods instead
     */
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

    /**
     * Get a sorted map of residences owned by a player.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return TreeMap of residence name to ClaimedResidence
     * @deprecated Use {@link #getResidencesMap(UUID, boolean, boolean, World)} instead
     */
    @Deprecated
    public TreeMap<String, ClaimedResidence> getResidencesMap(String player, boolean showhidden, boolean onlyHidden, World world) {

        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer == null)
            return new TreeMap<String, ClaimedResidence>();

        return getResidencesMap(resPlayer, showhidden, onlyHidden, world);
    }

    /**
     * Get a sorted map of residences owned by a player.
     *
     * @param uuid       Player UUID
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return TreeMap of residence name to ClaimedResidence
     */
    public TreeMap<String, ClaimedResidence> getResidencesMap(UUID uuid, boolean showhidden, boolean onlyHidden, World world) {
        ResidencePlayer resPlayer = this.getResidencePlayer(uuid);
        if (resPlayer == null)
            return new TreeMap<String, ClaimedResidence>();
        return getResidencesMap(resPlayer, showhidden, onlyHidden, world);
    }

    /**
     * Get a sorted map of residences for a ResidencePlayer with filters.
     *
     * @param resPlayer  ResidencePlayer instance
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return TreeMap of residence name to ClaimedResidence
     */
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

    /**
     * Get a sorted map of trusted residences for a player by name.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return TreeMap of residence name to ClaimedResidence
     * @deprecated Use {@link #getTrustedResidencesMap(UUID, boolean, boolean, World)} instead
     */
    @Deprecated
    public TreeMap<String, ClaimedResidence> getTrustedResidencesMap(String player, boolean showhidden, boolean onlyHidden, World world) {
        return getTrustedResidencesMap(this.getResidencePlayer(player), showhidden, onlyHidden, world);
    }

    /**
     * Get a sorted map of trusted residences for a player by UUID.
     *
     * @param uuid       Player UUID
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return TreeMap of residence name to ClaimedResidence
     */
    public TreeMap<String, ClaimedResidence> getTrustedResidencesMap(UUID uuid, boolean showhidden, boolean onlyHidden, World world) {
        return getTrustedResidencesMap(this.getResidencePlayer(uuid), showhidden, onlyHidden, world);
    }

    /**
     * Get a sorted map of trusted residences for a ResidencePlayer. Removes entries
     * where the player is no longer trusted.
     *
     * @param resPlayer  ResidencePlayer instance
     * @param showhidden Whether to include hidden residences
     * @param onlyHidden Whether to show only hidden residences
     * @param world      World to filter by (null for all worlds)
     * @return TreeMap of residence name to ClaimedResidence
     */
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

    /**
     * Get the ResidencePlayer for an OfflinePlayer. Creates a new record if not found.
     *
     * @param player Offline player
     * @return ResidencePlayer instance, or null if player is null
     */
    public ResidencePlayer getResidencePlayer(OfflinePlayer player) {
        if (player == null)
            return null;

        ResidencePlayer resPlayer = getResidencePlayer(player.getUniqueId());

        if (resPlayer != null)
            return resPlayer.updatePlayer(player);

        return playerJoin(player);
    }

    /**
     * Get the ResidencePlayer for an online Player. Creates a new record if not found.
     *
     * @param player Online player
     * @return ResidencePlayer instance
     */
    public @NotNull ResidencePlayer getResidencePlayer(Player player) {
        if (player == null)
            return null;

        ResidencePlayer resPlayer = getResidencePlayer(player.getUniqueId());

        if (resPlayer != null)
            return resPlayer.updatePlayer(player);

        return playerJoin(player);
    }

    /**
     * Get residence list by player name
     *
     * @param player Player name or UUID string
     * @return ResidencePlayer instance, or null if not found
     */
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

    /**
     * Get the ResidencePlayer for a UUID.
     *
     * @param uuid Player UUID
     * @return ResidencePlayer instance, or null if not found
     */
    public @Nullable ResidencePlayer getResidencePlayer(UUID uuid) {
        if (uuid == null)
            return null;
        return playersByUUID.get(uuid);
    }

    /**
     * Adds a residence to a player's ownership and registers trusted players.
     *
     * @param player    Player who owns the residence
     * @param residence ClaimedResidence to add
     */
    public void addResidence(Player player, ClaimedResidence residence) {
        addResidence(player.getUniqueId(), residence);
    }

    /**
     * Adds a residence to a player's ownership by UUID and registers trusted players.
     *
     * @param uuid      Owner UUID
     * @param residence ClaimedResidence to add
     */
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

    /**
     * Removes a residence from its owner's record.
     *
     * @param residence ClaimedResidence to remove
     */
    public void removeResFromPlayer(ClaimedResidence residence) {
        if (residence == null)
            return;
        removeResFromPlayer(residence.getOwnerUUID(), residence);
    }

    /**
     * Removes a residence from a player's record.
     *
     * @param player    Offline player
     * @param residence ClaimedResidence to remove
     */
    public void removeResFromPlayer(OfflinePlayer player, ClaimedResidence residence) {
        removeResFromPlayer(player.getUniqueId(), residence);
    }

    /**
     * Removes a residence from a player's record.
     *
     * @param player    Online player
     * @param residence ClaimedResidence to remove
     */
    public void removeResFromPlayer(Player player, ClaimedResidence residence) {
        removeResFromPlayer(player.getUniqueId(), residence);
    }

    /**
     * Removes a residence from a player's record by UUID.
     *
     * @param uuid      Player UUID
     * @param residence ClaimedResidence to remove
     */
    public void removeResFromPlayer(UUID uuid, ClaimedResidence residence) {
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null)
            resPlayer.removeResidence(residence);
    }

    private static CMITask saveTask = null;

    /**
     * Called when the plugin is stopping. Cancels any pending save task and flushes data.
     */
    public void onPluginStop() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
            saveDelayed();
        }
    }

    /**
     * Schedules a delayed save for all pending player data. Skips if a save is already scheduled.
     */
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

    /**
     * Performs the actual save of all pending player data to disk.
     */
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

    /**
     * Marks a player UUID for saving and triggers a save cycle.
     *
     * @param uuid Player UUID to save
     */
    public void addForSave(UUID uuid) {
        toBeSaved.add(uuid);
        save();
    }

    /**
     * Returns a copy of the set of UUIDs pending save.
     *
     * @return Set of UUIDs to be saved
     */
    private Set<UUID> getForSave() {
        return new HashSet<>(toBeSaved);
    }

    /**
     * Removes a UUID from the pending save set.
     *
     * @param id Player UUID to remove
     */
    private void remove(UUID id) {
        toBeSaved.remove(id);
    }

    /**
     * Caches all known offline players into memory. This is a one-time operation
     * performed on first load when no player data folder exists.
     */
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
                ResidencePlayer rp = addPlayer(name, player.getUniqueId());
                if (rp != null)
                    rp.setLastSeen(player.getLastPlayed());
            } catch (Exception e) {
                lm.consoleMessage("Failed to cache data of a player " + player.getUniqueId() + " (" + i + "/" + total + ")");
            }

            if (i % 1000 == 0) {
                lm.consoleMessage("Cached data (" + i + "/" + total + ")");
            }
        }

        save();
    }

    /**
     * Loads all player data from disk. On first run, caches offline players first.
     * Deserializes each player YAML file and populates the in-memory cache.
     */
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

    /**
     * Renames a player data file from a temp UUID to the real UUID.
     *
     * @param from Source UUID (must be a temp UUID)
     * @param to   Target UUID
     */
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

    /**
     * Recursively converts a ConfigurationSection to a flat Map, handling nested sections.
     *
     * @param section Configuration section to convert
     * @return Map of keys to values
     */
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
