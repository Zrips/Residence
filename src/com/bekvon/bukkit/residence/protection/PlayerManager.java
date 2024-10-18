package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidencePlayerInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

import net.Zrips.CMILib.Logs.CMIDebug;

public class PlayerManager implements ResidencePlayerInterface {
    private ConcurrentHashMap<String, ResidencePlayer> players = new ConcurrentHashMap<String, ResidencePlayer>();
    private ConcurrentHashMap<String, ResidencePlayer> playersUuid = new ConcurrentHashMap<String, ResidencePlayer>();
    private Residence plugin;

    public PlayerManager(Residence plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(ResidencePlayer resPlayer) {
        if (resPlayer == null)
            return;
        addPlayer(resPlayer.getName(), resPlayer.getUniqueId(), resPlayer);
    }

    public void addPlayer(Player player, ResidencePlayer resPlayer) {
        if (player == null)
            return;
        addPlayer(player.getName(), player.getUniqueId(), resPlayer);
    }

    public void addPlayer(String name, UUID uuid, ResidencePlayer resPlayer) {
        if (name != null)
            players.put(name.toLowerCase(), resPlayer);
        if (uuid != null)
            playersUuid.put(uuid.toString(), resPlayer);
    }

    public ResidencePlayer playerJoin(Player player) {
        ResidencePlayer resPlayer = playersUuid.get(player.getUniqueId().toString());
        if (resPlayer == null) {
            resPlayer = new ResidencePlayer(player);
            addPlayer(resPlayer);
        } else {
            resPlayer.updatePlayer(player);
        }
        return resPlayer;
    }

    public ResidencePlayer playerJoin(OfflinePlayer player) {
        ResidencePlayer resPlayer = playersUuid.get(player.getUniqueId().toString());
        if (resPlayer == null) {
            resPlayer = new ResidencePlayer(player);
            addPlayer(resPlayer);
        } else {
            resPlayer.updatePlayer(player);
        }
        return resPlayer;
    }

    public ResidencePlayer playerJoin(UUID uuid) {
        ResidencePlayer resPlayer = playersUuid.get(uuid.toString());
        if (resPlayer == null) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
            if (off != null) {
                resPlayer = new ResidencePlayer(off);
                addPlayer(resPlayer);
            }
        }
        return resPlayer;
    }

    public ResidencePlayer playerJoin(String player) {
        if (!players.containsKey(player.toLowerCase())) {
            ResidencePlayer resPlayer = new ResidencePlayer(player);
            addPlayer(resPlayer);
            return resPlayer;
        }
        return null;
    }

    public ResidencePlayer playerJoin(String player, UUID uuid) {
        if (!players.containsKey(player.toLowerCase())) {
            ResidencePlayer resPlayer = new ResidencePlayer(player, uuid);
            addPlayer(resPlayer);
            return resPlayer;
        }
        return null;
    }

    public int getResidenceCount(UUID uuid) {
        ResidencePlayer resPlayer = playersUuid.get(uuid.toString());
        if (resPlayer != null) {
            return resPlayer.getResList().size();
        }
        return 0;
    }

    @Override
    public ArrayList<String> getResidenceList(UUID uuid) {
        ArrayList<String> temp = new ArrayList<String>();
//	playerJoin(player, false);
        ResidencePlayer resPlayer = playersUuid.get(uuid.toString());
        if (resPlayer != null) {
            for (ClaimedResidence one : resPlayer.getResList()) {
                temp.add(one.getName());
            }
            return temp;
        }
        return temp;
    }

    @Override
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
    public ArrayList<String> getResidenceList(String player, boolean showhidden) {
        return getResidenceList(player, showhidden, false);
    }

    public ArrayList<String> getResidenceList(String player, boolean showhidden, boolean onlyHidden) {
        ArrayList<String> temp = new ArrayList<String>();
//	playerJoin(player, false);
        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer == null)
            return temp;
        for (ClaimedResidence one : resPlayer.getResList()) {
            boolean hidden = one.getPermissions().has("hidden", false);
            if (!showhidden && hidden)
                continue;

            if (onlyHidden && !hidden)
                continue;

            temp.add(plugin.msg(lm.Residence_List, "", one.getName(), one.getWorld()) +
                (hidden ? plugin.msg(lm.Residence_Hidden) : ""));
        }
        Collections.sort(temp, String.CASE_INSENSITIVE_ORDER);
        return temp;
    }

    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden) {
        return getResidences(player, showhidden, false);
    }

    public ArrayList<ClaimedResidence> getResidences(String player, boolean showhidden, boolean onlyHidden) {
        return getResidences(player, showhidden, onlyHidden, null);
    }

    // All 3 methods could be compacted into one, if needed
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
            if (world != null && !world.getName().equalsIgnoreCase(one.getWorld()))
                continue;
            temp.add(one);
        }
        return temp;
    }

    public TreeMap<String, ClaimedResidence> getResidencesMap(String player, boolean showhidden, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> temp = new TreeMap<String, ClaimedResidence>();

        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer == null) {
            return temp;
        }

        for (ClaimedResidence one : resPlayer.getResList()) {
            boolean hidden = one.getPermissions().has(Flags.hidden, false);
            if (!showhidden && hidden) {
                continue;
            }
            if (onlyHidden && !hidden)
                continue;
            if (world != null && !world.getName().equalsIgnoreCase(one.getWorld()))
                continue;
            temp.put(one.getName(), one);
        }
        return temp;
    }

    public TreeMap<String, ClaimedResidence> getTrustedResidencesMap(String player, boolean showhidden, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> temp = new TreeMap<String, ClaimedResidence>();

        ResidencePlayer resPlayer = this.getResidencePlayer(player);
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
            if (world != null && !world.getName().equalsIgnoreCase(one.getWorld()))
                continue;
            if (!one.isTrusted(player)) {
                iter.remove();
                continue;
            }
            temp.put(one.getName(), one);
        }
        return temp;
    }

    @Override
    public PermissionGroup getGroup(String player) {
        ResidencePlayer resPlayer = getResidencePlayerIfExist(player);
        if (resPlayer != null) {
            return resPlayer.getGroup();
        }
        return null;
    }

    @Override
    public int getMaxResidences(String player) {
        ResidencePlayer resPlayer = getResidencePlayerIfExist(player);
        if (resPlayer != null) {
            return resPlayer.getMaxRes();
        }
        return -1;
    }

    @Override
    public int getMaxSubzones(String player) {
        ResidencePlayer resPlayer = getResidencePlayerIfExist(player);
        if (resPlayer != null) {
            return resPlayer.getMaxSubzones();
        }
        return -1;
    }

    @Override
    public int getMaxSubzoneDepth(String player) {
        ResidencePlayer resPlayer = getResidencePlayerIfExist(player);
        if (resPlayer != null) {
            return resPlayer.getMaxSubzoneDepth();
        }
        return -1;
    }

    @Override
    public int getMaxRents(String player) {
        ResidencePlayer resPlayer = getResidencePlayerIfExist(player);
        if (resPlayer != null) {
            return resPlayer.getMaxRents();
        }
        return -1;
    }

    public ResidencePlayer getResidencePlayer(OfflinePlayer player) {
        if (player == null)
            return null;

        ResidencePlayer resPlayer = playersUuid.get(player.getUniqueId().toString());

        if (resPlayer != null)
            return resPlayer.updatePlayer(player);

        return playerJoin(player);
    }

    public ResidencePlayer getResidencePlayer(Player player) {
        if (player == null)
            return null;

        ResidencePlayer resPlayer = playersUuid.get(player.getUniqueId().toString());

        if (resPlayer != null)
            return resPlayer.updatePlayer(player);

        return playerJoin(player);
    }

    public ResidencePlayer getResidencePlayerIfExist(String player) {
        if (player == null)
            return null;

        if (player.equalsIgnoreCase("CONSOLE"))
            return null;

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.getName().equalsIgnoreCase(player))
            return getResidencePlayer(p);
        
        OfflinePlayer offline = plugin.getOfflinePlayer(player);
        if (offline != null)
            return getResidencePlayer(offline);

        return players.get(player.toLowerCase());
    }

    @Override
    public ResidencePlayer getResidencePlayer(String player) {
        if (player == null)
            return null;

        if (player.equalsIgnoreCase("CONSOLE"))
            return null;

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.getName().equalsIgnoreCase(player))
            return getResidencePlayer(p);
        ResidencePlayer rplayer = players.get(player.toLowerCase());
        if (rplayer != null) {
            return rplayer;
        }
        return playerJoin(player);
    }

    public ResidencePlayer getResidencePlayer(UUID uuid) {
        if (uuid == null)
            return null;

        Player p = Bukkit.getPlayer(uuid);
        if (p != null)
            return getResidencePlayer(p);
        ResidencePlayer resPlayer = playersUuid.get(uuid.toString());
        if (resPlayer != null) {
            return resPlayer;
        }
        return playerJoin(uuid);
    }

    public ResidencePlayer getResidencePlayer(String name, UUID uuid) {
        if (uuid.toString().equals(plugin.getServerLandUUID()))
            return null;
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            return getResidencePlayer(p);
        }
        ResidencePlayer resPlayer = this.playersUuid.get(uuid.toString());
        if (resPlayer != null) {
            return resPlayer;
        }

        if (name != null) {
            resPlayer = this.players.get(name.toLowerCase());
            if (resPlayer != null && resPlayer.getUniqueId() == null) {
                resPlayer.setUuid(uuid);
                this.playersUuid.put(uuid.toString(), resPlayer);
            }
        }
        if (resPlayer != null)
            return resPlayer;

        return playerJoin(name, uuid);
    }

    public void addResidence(UUID uuid, ClaimedResidence residence) {
        ResidencePlayer resPlayer = getResidencePlayer(uuid);
        if (resPlayer != null) {
            resPlayer.addResidence(residence);
        }
        return;
    }

    public void addResidence(Player player, ClaimedResidence residence) {
        addResidence(player.getUniqueId(), residence);
    }

    public void addResidence(String player, ClaimedResidence residence) {
        ResidencePlayer resPlayer = getResidencePlayer(player, residence.getOwnerUUID());
        if (resPlayer != null) {

            if (resPlayer.getUniqueId() == null) {
                Bukkit.getConsoleSender().sendMessage(" <--------------------- " + resPlayer.getUniqueId() + "  " + residence.getOwnerUUID());
            }

            resPlayer.addResidence(residence);
        }

        // Adding trusted residences
        try {
            if (residence.perms.playerFlags != null) {
                for (Entry<String, Map<String, Boolean>> one : residence.perms.playerFlags.entrySet()) {
                    String name = one.getKey();
                    if (name.length() == 36)
                        name = residence.perms.cachedPlayerNameUUIDs.get(UUID.fromString(one.getKey()));

                    if (!residence.isTrusted(name))
                        continue;
                    ResidencePlayer rplayer = null;
                    if (one.getKey().length() == 36)
                        rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(UUID.fromString(one.getKey()));
                    else
                        rplayer = Residence.getInstance().getPlayerManager().getResidencePlayer(name);
                    if (rplayer == null)
                        continue;
                    rplayer.addTrustedResidence(residence);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
        ResidencePlayer resPlayer = playersUuid.get(uuid.toString());
        if (resPlayer != null)
            resPlayer.removeResidence(residence);
    }

    @Deprecated
    /** @deprecated use {@link #removeResFromPlayer(UUID, ClaimedResidence)} */
    public void removeResFromPlayer(String player, ClaimedResidence residence) {
        ResidencePlayer resPlayer = this.getResidencePlayer(player);
        if (resPlayer != null) {
            resPlayer.removeResidence(residence);
        }
    }
}
