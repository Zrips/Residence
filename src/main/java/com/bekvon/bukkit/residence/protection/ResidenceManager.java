package com.bekvon.bukkit.residence.protection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.MinimizeFlags;
import com.bekvon.bukkit.residence.containers.MinimizeMessages;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.listeners.ResidenceLWCListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.GetTime;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ResidenceManager implements ResidenceInterface {
    protected ConcurrentHashMap<String, ClaimedResidence> residences;
    protected Map<String, Map<ChunkRef, List<ClaimedResidence>>> chunkResidences;
    protected List<ClaimedResidence> shops = new ArrayList<ClaimedResidence>();
    private Residence plugin;

    public ResidenceManager(Residence plugin) {
        residences = new ConcurrentHashMap<String, ClaimedResidence>();
        chunkResidences = new HashMap<String, Map<ChunkRef, List<ClaimedResidence>>>();
        shops = new ArrayList<ClaimedResidence>();
        this.plugin = plugin;
    }

    public boolean isOwnerOfLocation(Player player, Location loc) {
        ClaimedResidence res = getByLoc(loc);
        if (res != null && res.isOwner(player))
            return true;
        return false;
    }

    public ClaimedResidence getByLoc(CommandSender sender) {
        if ((sender instanceof Player))
            return getByLoc(((Player) sender).getLocation());
        return null;
    }

    public ClaimedResidence getByLoc(Player player) {
        return getByLoc(player.getLocation());
    }

    @Override
    public @Nullable ClaimedResidence getByLoc(@Nullable Location loc) {

        if (loc == null)
            return null;

        World world = loc.getWorld();

        if (world == null)
            return null;

        String worldName = world.getName();

        if (worldName == null)
            return null;

        Map<ChunkRef, List<ClaimedResidence>> ChunkMap = chunkResidences.get(worldName);

        if (ChunkMap == null)
            return null;

        List<ClaimedResidence> residences = ChunkMap.get(new ChunkRef(loc));

        if (residences == null)
            return null;

        for (ClaimedResidence residence : residences) {
            if (residence == null)
                continue;
            if (!residence.containsLoc(loc))
                continue;

            ClaimedResidence subres = residence.getSubzoneByLoc(loc);
            return subres == null ? residence : subres;
        }
        return null;
    }

    public List<ClaimedResidence> getByChunk(Chunk chunk) {
        List<ClaimedResidence> list = new ArrayList<ClaimedResidence>();
        if (chunk == null)
            return list;
        World world = chunk.getWorld();
        if (world == null)
            return list;
        String worldName = world.getName();
        if (worldName == null)
            return list;
        if (!chunkResidences.containsKey(worldName))
            return list;
        ChunkRef chunkRef = new ChunkRef(chunk.getX(), chunk.getZ());
        Map<ChunkRef, List<ClaimedResidence>> ChunkMap = chunkResidences.get(worldName);
        List<ClaimedResidence> ls = ChunkMap.get(chunkRef);
        return ls == null ? list : new ArrayList<ClaimedResidence>(ls);
    }

    @Override
    public ClaimedResidence getByName(String name) {
        if (name == null) {
            return null;
        }
        String[] split = name.split("\\.");
        if (split.length == 1) {
            return residences.get(name.toLowerCase());
        }

        if (split.length == 0)
            return null;

        ClaimedResidence res = residences.get(split[0].toLowerCase());
        for (int i = 1; i < split.length; i++) {
            if (res != null) {
                res = res.getSubzone(split[i].toLowerCase());
            } else {
                return null;
            }
        }
        return res;
    }

    @Override
    @Deprecated
    public void addShop(String resName) {
        ClaimedResidence res = getByName(resName);
        if (res != null)
            shops.add(res);
    }

    public void addShops(ClaimedResidence res) {
        ResidencePermissions perms = res.getPermissions();
        if (perms.has(Flags.shop, FlagCombo.OnlyTrue, false))
            addShop(res);
        for (ClaimedResidence one : res.getSubzones()) {
            addShops(one);
        }
    }

    @Override
    public void addShop(ClaimedResidence res) {
        shops.add(res);
    }

    @Override
    public void removeShop(ClaimedResidence res) {
        shops.remove(res);
    }

    @Override
    @Deprecated
    public void removeShop(String resName) {
        for (ClaimedResidence one : shops) {
            if (one.getName().equalsIgnoreCase(resName)) {
                shops.remove(one);
                break;
            }
        }
    }

    @Override
    public List<ClaimedResidence> getShops() {
        return shops;
    }

    @Override
    @Deprecated
    public boolean addResidence(String name, Location loc1, Location loc2) {
        return this.addResidence(null, name, null, plugin.getServerLandName(), new CuboidArea(loc1, loc2), true, false);
    }

    @Override
    @Deprecated
    public boolean addResidence(String name, String owner, Location loc1, Location loc2) {
        return this.addResidence(null, owner, null, name, new CuboidArea(loc1, loc2), true, false);
    }

    @Override
    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin) {
        return this.addResidence(player, player.getName(), player.getUniqueId(), name, new CuboidArea(loc1, loc2), resadmin, !resadmin);
    }

    @Deprecated
    public boolean addResidence(Player player, String owner, String name, Location loc1, Location loc2, boolean resadmin) {
        return addResidence(player, owner, null, name, new CuboidArea(loc1, loc2), resadmin, !resadmin);
    }

    @Override
    public boolean addResidence(Player player, String resName, boolean resadmin) {
        return addResidence(player, player.getName(), player.getUniqueId(), resName, plugin.getSelectionManager().getSelectionCuboid(player), resadmin, !resadmin);
    }

    public boolean addResidence(Player player, String resName, boolean resadmin, boolean deductMoney) {
        return addResidence(player, player.getName(), player.getUniqueId(), resName, plugin.getSelectionManager().getSelectionCuboid(player), resadmin, deductMoney);
    }

    public boolean addResidence(Player player, String owner, UUID ownerUUId, String resName, Location loc1, Location loc2, boolean resadmin) {
        return addResidence(player, owner, ownerUUId, resName, new CuboidArea(loc1, loc2), resadmin, !resadmin);
    }

    public boolean addResidence(Player player, String owner, UUID ownerUUId, String resName, CuboidArea area, boolean resadmin, boolean deductMoney) {

        if (!Utils.verifyResidenceName(player, resName))
            return false;

        if (area.getLowVector() == null || area.getHighVector() == null) {
            lm.Select_Points.sendMessage(player);
            return false;
        }

        if (plugin.isDisabledWorld(area.getWorld()) && plugin.getConfigManager().isDisableResidenceCreation()) {
            lm.General_CantCreate.sendMessage(player);
            return false;
        }

        if (owner == null)
            owner = plugin.getServerLandName();
        if (ownerUUId == null)
            ownerUUId = plugin.getServerUUID();

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = plugin.getPermissionManager().getDefaultGroup();

        if (rPlayer != null) {
            group = rPlayer.getGroup();

            if (!resadmin && !group.canCreateResidences() && !ResPerm.create.hasPermission(player, lm.General_NoPermission)) {
                return false;
            }

            if (!resadmin && !ResPerm.create.hasPermission(player, true)) {
                return false;
            }

            if (rPlayer.getResAmount() >= rPlayer.getMaxRes() && !resadmin) {
                lm.Residence_TooMany.sendMessage(player);
                return false;
            }
        }

        CuboidArea newArea = area.clone();
        ClaimedResidence newRes = new ClaimedResidence(owner, ownerUUId, area.getWorld().getName());
        newRes.getPermissions().applyDefaultFlags();
        newRes.setEnterMessage(group.getDefaultEnterMessage());
        newRes.setLeaveMessage(group.getDefaultLeaveMessage());
        newRes.setName(resName);
        newRes.setCreateTime();

        if (residences.containsKey(resName.toLowerCase())) {
            lm.Residence_AlreadyExists.sendMessage(player, residences.get(resName.toLowerCase()).getResidenceName());
            return false;
        }

        newRes.BlockSellPrice = group.getSellPerBlock();

        if (!newRes.addArea(player, newArea, "main", resadmin, false))
            return false;

        if (player != null && newArea.containsLoc(player.getLocation())) {
            newRes.setTpLoc(player, resadmin);
        }

        if (Residence.getInstance().getConfigManager().isChargeOnCreation() && !newRes.isSubzone() && plugin.getConfigManager().enableEconomy() && !resadmin && deductMoney) {
            double chargeamount = newArea.getCost(group);

            if (chargeamount > 0 && !plugin.getTransactionManager().chargeEconomyMoney(player, chargeamount)) {
                // Need to remove area if we can't create residence
                newRes.removeArea("main");
                return false;
            }
        }

        ResidenceCreationEvent resevent = new ResidenceCreationEvent(player, resName, newRes, newArea);
        plugin.getServ().getPluginManager().callEvent(resevent);
        if (resevent.isCancelled())
            return false;

        residences.put(resName.toLowerCase(), newRes);

        calculateChunks(newRes);
        plugin.getLeaseManager().removeExpireTime(newRes);
        plugin.getPlayerManager().addResidence(newRes.getOwnerUUID(), newRes);

        if (player != null) {
            Visualizer v = new Visualizer(player);
            v.setAreas(newArea);
            plugin.getSelectionManager().showBounds(player, v);
            plugin.getAutoSelectionManager().getList().remove(player.getUniqueId());
            lm.Area_Create.sendMessage(player, "main");
            lm.Residence_Create.sendMessage(player, resName);
        }
        if (plugin.getConfigManager().useLeases()) {
            plugin.getLeaseManager().setExpireTime(player, newRes, group.getLeaseGiveTime());
        }
        return true;

    }

    public void listResidences(CommandSender sender) {
        this.listResidences(sender, (UUID) null, 1, false, false, false, null);
    }

    public void listResidences(CommandSender sender, boolean resadmin) {
        this.listResidences(sender, (UUID) null, 1, false, false, resadmin, null);
    }

    @Deprecated
    public void listResidences(CommandSender sender, String targetplayer, boolean showhidden) {
        this.listResidences(sender, targetplayer, 1, showhidden, false, showhidden);
    }

    @Deprecated
    public void listResidences(CommandSender sender, String targetplayer, int page) {
        this.listResidences(sender, targetplayer, page, false, false, false);
    }

    public void listResidences(CommandSender sender, int page, boolean showhidden) {
        this.listResidences(sender, (UUID) null, page, showhidden, false, showhidden, null);
    }

    public void listResidences(CommandSender sender, int page, boolean showhidden, boolean onlyHidden) {
        this.listResidences(sender, (UUID) null, page, showhidden, onlyHidden, showhidden, null);
    }

    @Deprecated
    public void listResidences(CommandSender sender, String string, int page, boolean showhidden) {
        this.listResidences(sender, string, page, showhidden, false, showhidden);
    }

    @Deprecated
    public void listResidences(CommandSender sender, String targetplayer, int page, boolean showhidden, boolean onlyHidden, boolean resadmin) {
        this.listResidences(sender, targetplayer, page, showhidden, onlyHidden, resadmin, null);
    }

    @Deprecated
    public void listResidences(CommandSender sender, String targetplayer, int page, boolean showhidden, boolean onlyHidden, boolean resadmin, World world) {
        listResidences(sender, ResidencePlayer.getUUID(targetplayer), page, showhidden, onlyHidden, resadmin, world);
    }

    public void listResidences(CommandSender sender, UUID targetUuid, int page, boolean showhidden, boolean onlyHidden, boolean resadmin, World world) {

        if (targetUuid == null) {
            if (sender instanceof Player) {
                targetUuid = ((Player) sender).getUniqueId();
            } else {
                targetUuid = plugin.getServerUUID();
            }
        }

        boolean own = !(sender instanceof Player && !((Player) sender).getUniqueId().equals(targetUuid));

        if (showhidden && !ResAdmin.isResAdmin(sender) && !own) {
            showhidden = false;
        } else if (own)
            showhidden = true;

        boolean hidden = showhidden;
        TreeMap<String, ClaimedResidence> ownedResidences = plugin.getPlayerManager().getResidencesMap(targetUuid, hidden, onlyHidden, world);
        ownedResidences.putAll(plugin.getRentManager().getRentsMap(targetUuid, onlyHidden, world));
        ownedResidences.putAll(plugin.getPlayerManager().getTrustedResidencesMap(targetUuid, hidden, onlyHidden, world));

        plugin.getInfoPageManager().printListInfo(sender, targetUuid, ownedResidences, page, resadmin, world);
    }

    public void listAllResidences(CommandSender sender, int page) {
        this.listAllResidences(sender, page, false);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden, World world) {
        TreeMap<String, ClaimedResidence> list = getFromAllResidencesMap(showhidden, false, world);
        plugin.getInfoPageManager().printListInfo(sender, (UUID) null, list, page, showhidden, world);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden) {
        this.listAllResidences(sender, page, showhidden, false);
    }

    public void listAllResidences(CommandSender sender, int page, boolean showhidden, boolean onlyHidden) {
        TreeMap<String, ClaimedResidence> list = getFromAllResidencesMap(showhidden, onlyHidden, null);
        plugin.getInfoPageManager().printListInfo(sender, (UUID) null, list, page, showhidden, null);
    }

    public String[] getResidenceList() {
        return this.getResidenceList(true, true).toArray(new String[0]);
    }

    @Deprecated
    public Map<String, ClaimedResidence> getResidenceMapList(String targetplayer, boolean showhidden) {
        Map<String, ClaimedResidence> temp = new HashMap<String, ClaimedResidence>();
        for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
            if (res.getValue().isOwner(targetplayer)) {
                boolean hidden = res.getValue().getPermissions().has("hidden", false);
                if ((showhidden) || (!showhidden && !hidden)) {
                    temp.put(res.getValue().getName().toLowerCase(), res.getValue());
                }
            }
        }
        return temp;
    }

    public ArrayList<ClaimedResidence> getFromAllResidences(boolean showhidden, boolean onlyHidden, World world) {
        ArrayList<ClaimedResidence> list = new ArrayList<>();
        for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
            boolean hidden = res.getValue().getPermissions().has("hidden", false);
            if (onlyHidden && !hidden)
                continue;
            if (world != null && !world.getName().equalsIgnoreCase(res.getValue().getWorldName()))
                continue;
            if ((showhidden) || (!showhidden && !hidden)) {
                list.add(res.getValue());
            }
        }
        return list;
    }

    public TreeMap<String, ClaimedResidence> getFromAllResidencesMap(boolean showhidden, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> list = new TreeMap<String, ClaimedResidence>();
        for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
            boolean hidden = res.getValue().getPermissions().has("hidden", false);
            if (onlyHidden && !hidden)
                continue;
            if (world != null && !world.getName().equalsIgnoreCase(res.getValue().getWorldName()))
                continue;
            if ((showhidden) || (!showhidden && !hidden)) {
                list.put(res.getKey(), res.getValue());
            }
        }
        return list;
    }

    public ArrayList<String> getResidenceList(boolean showhidden, boolean showsubzones) {
        return this.getResidenceList((UUID) null, showhidden, showsubzones, false, false);
    }

    @Deprecated
    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones) {
        return this.getResidenceList(targetplayer, showhidden, showsubzones, false, false);
    }

    public ArrayList<String> getResidenceList(UUID uuid, boolean showhidden, boolean showsubzones, boolean onlyHidden) {
        return this.getResidenceList(uuid, showhidden, showsubzones, false, onlyHidden);
    }

    @Deprecated
    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, boolean onlyHidden) {
        return this.getResidenceList(targetplayer, showhidden, showsubzones, false, onlyHidden);
    }

    public ArrayList<String> getResidenceList(UUID uuid, boolean showhidden, boolean showsubzones, boolean formattedOutput, boolean onlyHidden) {
        ArrayList<String> list = new ArrayList<>();
        for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
            list.addAll(getResidenceList(uuid, showhidden, showsubzones, "", res.getKey(), res.getValue(), formattedOutput, onlyHidden));
        }
        return list;
    }

    @Deprecated
    public ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, boolean formattedOutput, boolean onlyHidden) {
        ArrayList<String> list = new ArrayList<>();
        for (Entry<String, ClaimedResidence> res : residences.entrySet()) {
            list.addAll(getResidenceList(targetplayer, showhidden, showsubzones, "", res.getKey(), res.getValue(), formattedOutput, onlyHidden));
        }
        return list;
    }

    @Deprecated
    private ArrayList<String> getResidenceList(String targetplayer, boolean showhidden, boolean showsubzones, String parentzone, String resname, ClaimedResidence res, boolean formattedOutput,
            boolean onlyHidden) {
        return getResidenceList(ResidencePlayer.getUUID(targetplayer), showhidden, showsubzones, parentzone, resname, res, formattedOutput, onlyHidden);
    }

    private ArrayList<String> getResidenceList(UUID target, boolean showhidden, boolean showsubzones, String parentzone, String resname, ClaimedResidence res, boolean formattedOutput,
            boolean onlyHidden) {

        ArrayList<String> list = new ArrayList<>();

        boolean hidden = res.getPermissions().has("hidden", false);

        if (onlyHidden && !hidden)
            return list;

        if ((showhidden) || (!showhidden && !hidden)) {
            if (target == null || res.isOwner(target)) {
                if (formattedOutput) {
                    list.add(lm.Residence_List.getMessage(parentzone, resname, res.getWorldName()) + (hidden ? lm.Residence_Hidden.getMessage() : ""));
                } else {
                    list.add(parentzone + resname);
                }
            }
            if (showsubzones) {
                for (Entry<String, ClaimedResidence> sz : res.subzones.entrySet()) {
                    list.addAll(getResidenceList(target, showhidden, showsubzones, parentzone + resname + ".", sz.getKey(), sz.getValue(), formattedOutput, onlyHidden));
                }
            }
        }
        return list;
    }

    public String checkAreaCollision(CuboidArea newarea, ClaimedResidence parentResidence) {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        for (Entry<String, ClaimedResidence> entry : set) {
            ClaimedResidence check = entry.getValue();
            if (check != parentResidence && check.checkCollision(newarea)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String checkAreaCollision(CuboidArea newarea, ClaimedResidence parentResidence, UUID ignoredOwner) {
        Set<Entry<String, ClaimedResidence>> set = residences.entrySet();
        for (Entry<String, ClaimedResidence> entry : set) {
            ClaimedResidence check = entry.getValue();
            if (check != parentResidence && check.checkCollision(newarea)) {
                if (ignoredOwner == null || !entry.getValue().isOwner(ignoredOwner))
                    return entry.getKey();
            }
        }
        return null;
    }

    public Set<ClaimedResidence> getResidences(String worldName, List<ChunkRef> chunks) {

        Map<ChunkRef, List<ClaimedResidence>> refs = chunkResidences.get(worldName);

        Set<ClaimedResidence> resSet = new HashSet<ClaimedResidence>();

        if (refs == null)
            return resSet;

        for (ChunkRef one : chunks) {
            List<ClaimedResidence> res = refs.get(one);
            if (res != null)
                resSet.addAll(res);
        }

        return resSet;
    }

    public ClaimedResidence collidesWithResidence(CuboidArea newarea) {
        Set<ClaimedResidence> res = getResidences(newarea.getWorldName(), newarea.getChunks());
        for (ClaimedResidence check : res) {
            if (check.checkCollision(newarea)) {
                return check;
            }
        }
        return null;
    }

    public void removeResidence(ClaimedResidence res) {
        this.removeResidence(null, res, true, false);
    }

    @Deprecated
    public void removeResidence(String name) {
        this.removeResidence(null, name, true);
    }

    public void removeResidence(CommandSender sender, ClaimedResidence res, boolean resadmin) {
        if (sender instanceof Player)
            removeResidence((Player) sender, res, resadmin);
        else
            removeResidence(null, res, true, false);
    }

    @Deprecated
    public void removeResidence(CommandSender sender, String name, boolean resadmin) {
        if (sender instanceof Player)
            removeResidence((Player) sender, name, resadmin);
        else
            removeResidence(null, name, true);
    }

    @Deprecated
    public void removeResidence(Player player, String name, boolean resadmin) {
        ClaimedResidence res = this.getByName(name);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }
        removeResidence(player, res, resadmin);
    }

    public void removeResidence(Player player, ClaimedResidence res, boolean resadmin) {
        removeResidence(ResidencePlayer.get(player), res, resadmin);
    }

    public void removeResidence(ResidencePlayer rPlayer, ClaimedResidence res, boolean resadmin) {
        removeResidence(rPlayer, res, resadmin, false);
    }

    public void removeResidence(ResidencePlayer rPlayer, ClaimedResidence res, boolean resadmin, boolean regenerate) {

        Player player = null;
        if (rPlayer != null)
            player = rPlayer.getPlayer();

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        String name = res.getName();

        if (plugin.getConfigManager().isRentPreventRemoval() && !resadmin) {
            ClaimedResidence rented = res.getRentedSubzone();
            if (rented != null) {
                lm.Residence_CantRemove.sendMessage(player, res.getName(), rented.getName(), rented.getRentedLand().getRenterName());
                return;
            }
            if (player != null && res.isRented() && !player.getName().equalsIgnoreCase(res.getRentedLand().getRenterName())) {
                lm.Residence_CantRemove.sendMessage(player, res.getName(), res.getName(), res.getRentedLand().getRenterName());
                return;
            }
        }

        if (player != null && !resadmin) {
            if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin && res.getParent() != null && !res.getParent().isOwner(player)) {
                lm.General_NoPermission.sendMessage(player);
                return;
            }
        }

        if (rPlayer != null)
            rPlayer.forceUpdateGroup();

        ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(player, res, rPlayer == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
        plugin.getServ().getPluginManager().callEvent(resevent);
        if (resevent.isCancelled())
            return;

        ClaimedResidence parent = res.getParent();
        removeChunkList(res);

        if (parent == null) {

            residences.remove(name.toLowerCase());

            regenerateArea(res);

            if (plugin.getConfigManager().isRemoveLwcOnDelete() && plugin.isLwcPresent())
                ResidenceLWCListener.removeLwcFromResidence(player, res);

            if (regenerate) {
                for (CuboidArea one : res.getAreaArray()) {
                    plugin.getSelectionManager().regenerate(one);
                }
            }
            lm.Residence_Remove.sendMessage(player, name);
        } else {
            String[] split = name.split("\\.");
            if (player != null) {
                parent.removeSubzone(player, split[split.length - 1], true);
            } else {
                parent.removeSubzone(split[split.length - 1]);
            }
        }

        cleanResidenceRecords(res, true);

        for (ClaimedResidence sub : res.getSubzones()) {
            removeResidence(rPlayer, sub, resadmin, false);
        }
    }

    public void giveBackOwnerMoneyForResidence(ClaimedResidence res) {
        if (res.isServerLand())
            return;

        double giveBack = 0D;

        if (res.getParent() == null && plugin.getConfigManager().enableEconomy() && plugin.getConfigManager().useResMoneyBack())
            giveBack += res.getWorth();

        if (res.getBank().getStoredMoneyD() > 0 && plugin.getConfigManager().isResBankBack())
            giveBack += res.getBank().getStoredMoneyD();

        if (giveBack > 0)
            plugin.getTransactionManager().giveEconomyMoney(res.getOwnerUUID(), giveBack);
    }

    private void regenerateArea(ClaimedResidence res) {

        if (Version.isCurrentLower(Version.v1_13_R1)
                || !plugin.getConfigManager().isUseClean()
                || !plugin.getConfigManager().getCleanWorlds().contains(res.getWorldName().toLowerCase()))
            return;

        CuboidArea[] arr = res.getAreaArray();
        List<Supplier<CompletableFuture<Void>>> tasks = new ArrayList<>();

        for (CuboidArea area : arr) {
            Location low = area.getLowLocation().clone();
            Location high = area.getHighLocation().clone();

            if (high.getBlockY() <= plugin.getConfigManager().getCleanLevel())
                continue;

            if (low.getBlockY() < plugin.getConfigManager().getCleanLevel())
                low.setY(plugin.getConfigManager().getCleanLevel());
            World world = low.getWorld();

            for (ChunkRef chunkRef : area.getChunks()) {
                tasks.add(() -> CMIScheduler.runAtLocation(plugin, high.getWorld(), chunkRef.getX(), chunkRef.getZ(), () -> {
                    cleaner(chunkRef, world, high, low);
                }));
            }
        }

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        for (Supplier<CompletableFuture<Void>> task : tasks) {
            chain = chain.thenCompose(v -> {
                if (!plugin.isEnabled() || Bukkit.getServer().isStopping())
                    return CompletableFuture.completedFuture(null);
                return task.get();
            }).thenCompose(v -> delay());
        }
    }

    private void cleaner(ChunkRef chunkRef, World world, Location high, Location low) {

        if (!plugin.isEnabled() || Bukkit.getServer().isStopping())
            return;

        Set<Location> locations = ConcurrentHashMap.newKeySet();

        ChunkSnapshot chunkSnapshot = null;
        for (int x = chunkRef.getX() * 16; x <= chunkRef.getX() * 16 + 15; x++) {
            for (int z = chunkRef.getZ() * 16; z <= chunkRef.getZ() * 16 + 15; z++) {

                // Limit to exact residence area
                if (x < low.getBlockX() || x > high.getBlockX() || z < low.getBlockZ() || z > high.getBlockZ())
                    continue;

                int hy = world.getHighestBlockYAt(x, z);
                if (high.getBlockY() < hy)
                    hy = high.getBlockY();

                int cx = Math.abs(x % 16);
                int cz = Math.abs(z % 16);

                if (chunkSnapshot == null) {
                    if (!world.getBlockAt(x, 0, z).getChunk().isLoaded()) {
                        world.getBlockAt(x, 0, z).getChunk().load();
                        chunkSnapshot = world.getBlockAt(x, 0, z).getChunk().getChunkSnapshot(false, false, false);
                        world.getBlockAt(x, 0, z).getChunk().unload();
                    } else {
                        chunkSnapshot = world.getBlockAt(x, 0, z).getChunk().getChunkSnapshot();
                    }
                }

                if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
                    for (int y = low.getBlockY(); y <= hy; y++) {
                        BlockData type = chunkSnapshot.getBlockData(cx, y, cz);
                        if (!plugin.getConfigManager().getCleanBlocks().contains(type.getMaterial()))
                            continue;
                        locations.add(new Location(world, x, y, z));
                    }
                }
            }
        }

        for (Location one : locations) {
            if (plugin.isEnabled() && !Bukkit.getServer().isStopping())
                one.getBlock().setType(Material.AIR);
        }
    }

    private CompletableFuture<Void> delay() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        CMIScheduler.runTaskLater(plugin, () -> future.complete(null), 1);
        return future;
    }

    public boolean removeAllByOwner(String owner) {
        ResidencePlayer rPlayer = ResidencePlayer.get(owner);
        if (rPlayer == null)
            return false;
        for (ClaimedResidence oneRes : rPlayer.getResList()) {
            removeResidence(rPlayer, oneRes, true);
        }
        return true;
    }

    public int getOwnedZoneCount(UUID playerUUID) {
        return ResidencePlayer.get(playerUUID).getResAmount();
    }

    @Deprecated
    public int getOwnedZoneCount(String player) {
        return ResidencePlayer.get(player).getResAmount();
    }

    public boolean hasMaxZones(UUID playerUUID, int target) {
        return getOwnedZoneCount(playerUUID) < target;
    }

    @Deprecated
    public boolean hasMaxZones(String player, int target) {
        return getOwnedZoneCount(player) < target;
    }

    @Deprecated
    public void printAreaInfo(String areaname, CommandSender sender) {
        printAreaInfo(areaname, sender, false);
    }

    @Deprecated
    public void printAreaInfo(String areaname, CommandSender sender, boolean resadmin) {
        ClaimedResidence res = this.getByName(areaname);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return;
        }

        printAreaInfo(res, sender, resadmin);
    }

    public void printAreaInfo(ClaimedResidence res, CommandSender sender, boolean resadmin) {

        String areaname = res.getName();

        lm.General_Separator.sendMessage(sender);

        ResidencePermissions perms = res.getPermissions();

        String resNameOwner = lm.Residence_Line.getMessage(areaname);
        resNameOwner += lm.General_Owner.getMessage(perms.getOwner());
        if (plugin.getConfigManager().enableEconomy() && (res.isOwner(sender) || !(sender instanceof Player) || resadmin))
            resNameOwner += lm.Bank_Name.getMessage(res.getBank().getStoredMoneyFormated());

        resNameOwner = CMIChatColor.translate(resNameOwner);

        String worldInfo = lm.General_World.getMessage(perms.getWorldName());

        if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
            CuboidArea area = res.getAreaArray()[0];
            String cord1 = lm.General_CoordsTop.getMessage(area.getHighVector().getBlockX(), area.getHighVector().getBlockY(), area.getHighVector().getBlockZ());
            String cord2 = lm.General_CoordsBottom.getMessage(area.getLowVector().getBlockX(), area.getLowVector().getBlockY(), area.getLowVector().getBlockZ());
            worldInfo += lm.General_CoordsLiner.getMessage(cord1, cord2);
        }

        worldInfo += "\n" + lm.General_CreatedOn.getMessage(GetTime.getTime(res.createTime));

        String ResFlagList = perms.listFlags(5);
        if (!(sender instanceof Player))
            ResFlagList = perms.listFlags();
        String ResFlagMsg = lm.General_ResidenceFlags.getMessage(ResFlagList);

        if (perms.getFlags().size() > 2 && sender instanceof Player) {
            ResFlagMsg = lm.General_ResidenceFlags.getMessage(perms.listFlags(5, 3)) + "...";
        }

        if (sender instanceof Player) {
            RawMessage rm = new RawMessage();
            rm.addText(resNameOwner).addHover(worldInfo);
            rm.show(sender);

            rm = new RawMessage();

            rm.addText(ResFlagMsg).addHover(ResFlagList);
            rm.show(sender);
        } else {
            lm.showMessage(sender, resNameOwner);
            lm.showMessage(sender, worldInfo);
            lm.showMessage(sender, ResFlagMsg);
        }

        if (!plugin.getConfigManager().isShortInfoUse() || !(sender instanceof Player))
            lm.General_PlayersFlags.sendMessage(sender, perms.listPlayersFlags());
        else if (plugin.getConfigManager().isShortInfoUse() || sender instanceof Player) {

            RawMessage rm = perms.listPlayersFlagsRaw(PlayerManager.getSenderUUID(sender), lm.General_PlayersFlags.getMessage(""));
            rm.addCommand("res info " + res.getName() + " -players");
            rm.show(sender);
        }

        String groupFlags = perms.listGroupFlags();
        if (groupFlags.length() > 0)
            lm.General_GroupFlags.sendMessage(sender, groupFlags);

        RawMessage rm = new RawMessage();
        rm.addText(lm.General_TotalResSize.getMessage(res.getTotalSize(), res.getXZSize()));

        try {
            rm.addHover(Arrays.asList(
                    lm.General_ResSize_eastWest.getMessage(res.getMainArea().getXSize()),
                    lm.General_ResSize_northSouth.getMessage(res.getMainArea().getZSize()),
                    lm.General_ResSize_upDown.getMessage(res.getMainArea().getYSize())));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        rm.show(sender);

        if (plugin.getEconomyManager() != null) {
            lm.General_TotalWorth.sendMessage(sender, plugin.getEconomyManager().format(res.getWorthByOwner()), plugin.getEconomyManager().format(res.getWorth()));
        }

        if (res.getSubzonesAmount(false) > 0)
            lm.General_TotalSubzones.sendMessage(sender, res.getSubzonesAmount(false), res.getSubzonesAmount(true));

        if (plugin.getConfigManager().useLeases() && plugin.getLeaseManager().isLeased(res)) {
            String time = plugin.getLeaseManager().getExpireTime(res);
            if (time != null)
                lm.Economy_LeaseExpire.sendMessage(sender, time);
        }

        if (plugin.getConfigManager().enabledRentSystem() && res.isForRent() && !res.isRented()) {
            String forRentMsg = lm.Rent_isForRent.getMessage();

            RentableLand rentable = res.getRentable();
            StringBuilder rentableString = new StringBuilder();
            if (rentable != null) {
                rentableString.append(lm.General_Cost.getMessage(rentable.cost, rentable.days) + "\n");
                rentableString.append(lm.Rentable_AllowRenewing.getMessage(rentable.AllowRenewing) + "\n");
                rentableString.append(lm.Rentable_StayInMarket.getMessage(rentable.StayInMarket) + "\n");
                rentableString.append(lm.Rentable_AllowAutoPay.getMessage(rentable.AllowAutoPay));
            }
            if (sender instanceof Player) {

                rm = new RawMessage();
                rm.addText(forRentMsg).addHover(rentableString.toString());
                rm.show(sender);
            } else
                lm.showMessage(sender, forRentMsg);
        } else if (plugin.getConfigManager().enabledRentSystem() && res.isRented()) {

            RentableLand rentable = res.getRentable();
            RentedLand rented = res.getRentedLand();

            String RentedMsg = lm.Residence_RentedBy.getMessage(rented.getRenterName());

            StringBuilder rentableString = new StringBuilder();

            rentableString.append(lm.Rent_Expire.getMessage(GetTime.getTime(rented.endTime)) + "\n");
            if (rented.isRenter(sender) || resadmin || res.isOwner(sender))
                rentableString.append((rented.AutoPay ? lm.Rent_AutoPayTurnedOn.getMessage() : lm.Rent_AutoPayTurnedOff.getMessage()) + "\n");

            if (rentable != null) {
                rentableString.append(lm.General_Cost.getMessage(rentable.cost, rentable.days) + "\n");
                rentableString.append(lm.Rentable_AllowRenewing.getMessage(rentable.AllowRenewing) + "\n");
                rentableString.append(lm.Rentable_StayInMarket.getMessage(rentable.StayInMarket) + "\n");
                rentableString.append(lm.Rentable_AllowAutoPay.getMessage(rentable.AllowAutoPay));
            }

            if (sender instanceof Player) {

                rm = new RawMessage();
                rm.addText(RentedMsg).addHover(rentableString.toString());
                rm.show(sender);
            } else
                lm.showMessage(sender, RentedMsg);
        } else if (res.isForSell()) {
            String SellMsg = lm.Economy_LandForSale.getMessage() + " " + res.getSellPrice();
            lm.showMessage(sender, SellMsg);
        }

        lm.General_Separator.sendMessage(sender);
    }

    @Deprecated
    public void printAreaPlayers(String areaname, CommandSender sender, int page) {
        ClaimedResidence res = this.getByName(areaname);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return;
        }

        printAreaPlayers(res, sender, page);
    }

    public void printAreaPlayers(ClaimedResidence res, CommandSender sender, int page) {

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return;
        }

        lm.General_Separator.sendMessage(sender);

        ResidencePermissions perms = res.getPermissions();

        perms.listPlayers(sender, null, page);

        PageInfo pi = new PageInfo(10, perms.getPlayerFlags().size(), page) {
            @Override
            public Boolean pageChange(int page) {
                printAreaPlayers(res, sender, page);
                return null;
            }
        };
        pi.autoPagination(sender, "res info " + res.getName() + " -players", "-p:");
    }

    public void mirrorPerms(Player reqPlayer, String targetArea, String sourceArea, boolean resadmin) {
        ClaimedResidence receiver = this.getByName(targetArea);
        ClaimedResidence source = this.getByName(sourceArea);
        if (source == null || receiver == null) {
            lm.Invalid_Residence.sendMessage(reqPlayer);
            return;
        }
        if (!resadmin) {
            if (!receiver.getPermissions().hasResidencePermission(reqPlayer, true) || !source.getPermissions().hasResidencePermission(reqPlayer, true)) {
                lm.General_NoPermission.sendMessage(reqPlayer);
                return;
            }
        }
        receiver.getPermissions().applyTemplate(reqPlayer, source.getPermissions(), resadmin);
    }

    public Map<String, Object> save() {
        clearSaveChache();
        Map<String, Object> worldmap = new LinkedHashMap<>();
        for (String worldName : getWorldNames()) {
            Map<String, Object> resmap = new LinkedHashMap<>();
            for (Entry<String, ClaimedResidence> res : (new TreeMap<String, ClaimedResidence>(residences)).entrySet()) {
                if (!res.getValue().getWorldName().equals(worldName))
                    continue;

                try {
                    resmap.put(res.getValue().getResidenceName(), res.getValue().save());
                } catch (Throwable ex) {
                    lm.consoleMessage("Failed to save residence (" + res.getKey() + ")!");
                    Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            worldmap.put(worldName, resmap);
        }
        return worldmap;
    }

    private void clearSaveChache() {
        optimizeMessages.clear();
        optimizeFlags.clear();
    }

    // Optimizing save file
    HashMap<String, List<MinimizeMessages>> optimizeMessages = new HashMap<String, List<MinimizeMessages>>();
    HashMap<String, List<MinimizeFlags>> optimizeFlags = new HashMap<String, List<MinimizeFlags>>();

    public MinimizeMessages addMessageToTempCache(String world, String enter, String leave) {
        List<MinimizeMessages> ls = optimizeMessages.get(world);
        if (ls == null)
            ls = new ArrayList<MinimizeMessages>();
        for (MinimizeMessages one : ls) {
            if (!one.add(enter, leave))
                continue;
            return one;
        }
        MinimizeMessages m = new MinimizeMessages(ls.size() + 1, enter, leave);
        ls.add(m);
        optimizeMessages.put(world, ls);
        return m;
    }

    public HashMap<Integer, Object> getMessageCatch(String world) {
        HashMap<Integer, Object> t = new HashMap<Integer, Object>();
        List<MinimizeMessages> ls = optimizeMessages.get(world);
        if (ls == null)
            return null;
        for (MinimizeMessages one : ls) {
            Map<String, String> root = new HashMap<>();
            root.put("EnterMessage", one.getEnter());
            root.put("LeaveMessage", one.getLeave());
            t.put(one.getId(), root);
        }
        return t;
    }

    public MinimizeFlags addFlagsTempCache(String world, Map<String, Boolean> map) {
        if (world == null)
            return null;
        List<MinimizeFlags> ls = optimizeFlags.get(world);
        if (ls == null)
            ls = new ArrayList<MinimizeFlags>();
        for (MinimizeFlags one : ls) {
            if (!one.add(map))
                continue;
            return one;
        }
        MinimizeFlags m = new MinimizeFlags(ls.size() + 1, map);
        ls.add(m);
        optimizeFlags.put(world, ls);
        return m;
    }

    public HashMap<Integer, Object> getFlagsCatch(String world) {
        HashMap<Integer, Object> t = new HashMap<Integer, Object>();
        List<MinimizeFlags> ls = optimizeFlags.get(world);
        if (ls == null)
            return null;
        for (MinimizeFlags one : ls) {
            t.put(one.getId(), one.getFlags());
        }
        return t;
    }

    private void clearLoadChache() {
        cacheMessages.clear();
        cacheFlags.clear();
    }

    HashMap<String, HashMap<Integer, MinimizeMessages>> cacheMessages = new HashMap<String, HashMap<Integer, MinimizeMessages>>();
    HashMap<String, HashMap<Integer, MinimizeFlags>> cacheFlags = new HashMap<String, HashMap<Integer, MinimizeFlags>>();

    public HashMap<String, HashMap<Integer, MinimizeMessages>> getCacheMessages() {
        return cacheMessages;
    }

    public HashMap<String, HashMap<Integer, MinimizeFlags>> getCacheFlags() {
        return cacheFlags;
    }

    public String getChacheMessageEnter(String world, int id) {
        HashMap<Integer, MinimizeMessages> c = cacheMessages.get(world);
        if (c == null)
            return null;
        MinimizeMessages m = c.get(id);
        if (m == null)
            return null;
        return m.getEnter();
    }

    public String getChacheMessageLeave(String world, int id) {
        HashMap<Integer, MinimizeMessages> c = cacheMessages.get(world);
        if (c == null)
            return null;
        MinimizeMessages m = c.get(id);
        if (m == null)
            return null;
        return m.getLeave();
    }

    public Map<String, Boolean> getChacheFlags(String world, int id) {
        HashMap<Integer, MinimizeFlags> c = cacheFlags.get(world);
        if (c == null)
            return null;
        MinimizeFlags m = c.get(id);
        if (m == null)
            return null;
        return m.getFlags();
    }

    public Set<String> getWorldNames() {
        Set<String> worldnames = new HashSet<String>();
        File saveFolder = new File(plugin.dataFolder, "Save");
        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (plugin.getConfigManager().isLoadEveryWorld() && worldFolder.isDirectory()) {
                for (File f : worldFolder.listFiles()) {
                    if (!f.isFile())
                        continue;
                    String name = f.getName();
                    if (!name.startsWith(Residence.saveFilePrefix))
                        continue;
                    worldnames.add(name.substring(Residence.saveFilePrefix.length(), name.length() - 4));
                }
            }
            plugin.getServ().getWorlds().forEach((w) -> {
                worldnames.add(w.getName());
            });
        } catch (Exception ex) {
            plugin.getServ().getWorlds().forEach((w) -> {
                worldnames.add(w.getName());
            });
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return worldnames;
    }

    int batchSize = 1000000;
    ExecutorService executorService = null;

    public void load(Map<String, Object> root) throws Exception {
        if (root == null)
            return;
        residences.clear();

        int numCores = Runtime.getRuntime().availableProcessors();

        numCores = CMINumber.clamp(numCores, 1, numCores - 1);

        executorService = Executors.newFixedThreadPool(numCores);
        batchSize = (int) Math.ceil(root.entrySet().size() / (double) numCores);

        for (Entry<String, Object> worldSet : root.entrySet()) {

            long time = System.currentTimeMillis();

            String worldName = worldSet.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> reslist = (Map<String, Object>) worldSet.getValue();

            if (!plugin.isDisabledWorld(worldName) && !plugin.getConfigManager().CleanerStartupLog)
                lm.consoleMessage("Loading " + worldName + " data into memory...");
            if (reslist != null) {
                try {
                    chunkResidences.put(worldName, multithreadLoadMap(worldName, reslist));
                } catch (Exception ex) {
                    lm.consoleMessage("Error in loading save file for world: " + worldName);
                    if (plugin.getConfigManager().stopOnSaveError())
                        throw (ex);
                }
            }

            long pass = System.currentTimeMillis() - time;
            String pastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

            if (!plugin.isDisabledWorld(worldName))
                lm.consoleMessage("Loaded &e" + worldName + "&f data into memory. (&e" + pastTime + "&f) -> " + (reslist == null ? "?" : reslist.size())
                        + " residences");
        }

        executorService.shutdown();

        clearLoadChache();
    }

    int chunkCount = 0;

    public Map<ChunkRef, List<ClaimedResidence>> multithreadLoadMap(String worldName, Map<String, Object> root) throws InterruptedException, ExecutionException {
        Map<ChunkRef, List<ClaimedResidence>> retRes = new ConcurrentHashMap<>();

        if (root == null) {
            return retRes;
        }

        chunkCount = 0;

        List<Future<Void>> futures = new ArrayList<>();

        batchSize = CMINumber.clamp(batchSize, 500, root.entrySet().size());

        int i = 0;

        List<Entry<String, Object>> batch = new ArrayList<>();

        int total = root.entrySet().size() - 1;

        for (Entry<String, Object> entry : root.entrySet()) {
            batch.add(entry);

            if (batch.size() < batchSize && i < total) {
                i++;
                continue;
            }

            List<Entry<String, Object>> currentBatch = batch;
            batch = new ArrayList<>();
            i++;

            futures.add(processBatch(worldName, currentBatch, retRes));
        }

        if (!batch.isEmpty())
            futures.add(processBatch(worldName, batch, retRes));

        for (Future<Void> future : futures) {
            future.get();
        }

        return retRes;
    }

    private Future<Void> processBatch(String worldName, List<Entry<String, Object>> currentBatch, Map<ChunkRef, List<ClaimedResidence>> retRes) {
        return executorService.submit(() -> {
            for (Entry<String, Object> currentEntry : currentBatch) {
                try {
                    ClaimedResidence residence = ClaimedResidence.load(worldName, (Map<String, Object>) currentEntry.getValue(), null, plugin);
                    if (residence == null) {
                        continue;
                    }

                    if (residence.getPermissions().getOwnerUUID().toString().equals(plugin.getServerUUID().toString()) &&
                            !residence.getOwner().equalsIgnoreCase("Server land") &&
                            !residence.getOwner().equalsIgnoreCase(plugin.getServerLandName())) {
                        continue;
                    }

                    if (residence.getOwner().equalsIgnoreCase("Server land")) {
                        residence.getPermissions().setOwner(plugin.getServerLandName(), false);
                    }

                    String resName = currentEntry.getKey().toLowerCase();

                    int increment = getNameIncrement(resName);

                    if (residence.getResidenceName() == null)
                        residence.setName(currentEntry.getKey());

                    if (increment > 0) {
                        residence.setName(residence.getResidenceName() + increment);
                        resName += increment;
                    }

                    List<ChunkRef> chunks = getChunks(residence);

                    if (chunks.size() > 1000000)
                        lm.consoleMessage(CMIChatColor.YELLOW + "Detected extensively big residence area (" + currentEntry.getKey() + ") which covers " + chunks
                                .size() + " chunks!");

                    for (ChunkRef chunk : chunks) {
                        retRes.compute(chunk, (k, v) -> {
                            if (v == null) {
                                v = new ArrayList<>(1);
                            }
                            v.add(residence);
                            chunkCount++;
                            return v;
                        });
                    }

                    plugin.getPlayerManager().addResidence(residence.getOwnerUUID(), residence);

                    residences.put(resName, residence);
                } catch (Exception ex) {
                    lm.consoleMessage(CMIChatColor.RED + "Failed to load residence (" + currentEntry.getKey() + ")! Reason:" + ex.getMessage() + " Error Log:");
                    Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
                    if (plugin.getConfigManager().stopOnSaveError()) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            return null;
        });
    }

    // Old method for single core loading
    public Map<ChunkRef, List<ClaimedResidence>> loadMap(String worldName, Map<String, Object> root) throws Exception {
        Map<ChunkRef, List<ClaimedResidence>> retRes = new HashMap<>();
        if (root == null)
            return retRes;

        int i = 0;
        for (Entry<String, Object> res : root.entrySet()) {
            if (i >= 100)
                i = 0;
            i++;
            try {
                @SuppressWarnings("unchecked")
                ClaimedResidence residence = ClaimedResidence.load(worldName, (Map<String, Object>) res.getValue(), null, plugin);
                if (residence == null)
                    continue;

                if (residence.getPermissions().getOwnerUUID().toString().equals(plugin.getServerUUID().toString()) &&
                        !residence.getOwner().equalsIgnoreCase("Server land") &&
                        !residence.getOwner().equalsIgnoreCase(plugin.getServerLandName()))
                    continue;

                if (residence.getOwner().equalsIgnoreCase("Server land")) {
                    residence.getPermissions().setOwner(plugin.getServerLandName(), false);
                }
                String resName = res.getKey().toLowerCase();

                // Checking for duplicated residence names and renaming them
                int increment = getNameIncrement(resName);

                if (residence.getResidenceName() == null)
                    residence.setName(res.getKey());

                if (increment > 0) {
                    residence.setName(residence.getResidenceName() + increment);
                    resName += increment;
                }

                for (ChunkRef chunk : getChunks(residence)) {
                    List<ClaimedResidence> ress = new ArrayList<>();
                    if (retRes.containsKey(chunk)) {
                        ress.addAll(retRes.get(chunk));
                    }
                    ress.add(residence);
                    retRes.put(chunk, ress);
                }

                plugin.getPlayerManager().addResidence(residence.getOwnerUUID(), residence);

                residences.put(resName.toLowerCase(), residence);

            } catch (Exception ex) {
                lm.consoleMessage(CMIChatColor.RED + "Failed to load residence (" + res.getKey() + ")! Reason:" + ex.getMessage() + " Error Log:");
                Logger.getLogger(ResidenceManager.class.getName()).log(Level.SEVERE, null, ex);
                if (plugin.getConfigManager().stopOnSaveError()) {
                    throw (ex);
                }
            }
        }

        return retRes;
    }

    private int getNameIncrement(String name) {
        String orName = name;
        int i = 0;
        while (i < 1000) {
            if (residences.containsKey(name.toLowerCase())) {
                i++;
                name = orName + i;
            } else
                break;
        }
        return i;
    }

    private static List<ChunkRef> getChunks(ClaimedResidence res) {
        List<ChunkRef> chunks = new ArrayList<>();
        res.getAreaMap().values().forEach(area -> chunks.addAll(area.getChunks()));
        return chunks;
    }

    public boolean renameResidence(String oldName, String newName) {
        return this.renameResidence(null, oldName, newName, true);
    }

    public boolean renameResidence(Player player, String oldName, String newName, boolean resadmin) {
        return this.renameResidence((CommandSender) player, oldName, newName, resadmin);
    }

    public boolean renameResidence(CommandSender sender, String oldName, String newName, boolean resadmin) {
        if (!ResPerm.rename.hasPermission(sender, true)) {
            return false;
        }

        if (!Utils.verifyResidenceName(sender, newName))
            return false;

        ClaimedResidence res = this.getByName(oldName);
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return false;
        }

        if (res.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_cantDo.sendMessage(sender);
            return false;
        }

        oldName = res.getName();
        if (res.getPermissions().hasResidencePermission(sender, true) || resadmin) {
            if (res.getParent() == null) {
                if (residences.containsKey(newName.toLowerCase())) {
                    lm.Residence_AlreadyExists.sendMessage(sender, newName);
                    return false;
                }

                ResidenceRenameEvent resevent = new ResidenceRenameEvent(res, newName, oldName);
                plugin.getServ().getPluginManager().callEvent(resevent);

                if (resevent.isCancelled())
                    return false;

                newName = resevent.getNewResidenceName();

                removeChunkList(oldName);
                res.setName(newName);

                residences.put(newName.toLowerCase(), res);
                residences.remove(oldName.toLowerCase());

                calculateChunks(res);

                plugin.getSignUtil().updateSignResName(res);

                lm.Residence_Rename.sendMessage(sender, oldName, newName);

                return true;
            }
            String[] oldname = oldName.split("\\.");
            ClaimedResidence parent = res.getParent();

            boolean feed = parent.renameSubzone(sender, oldname[oldname.length - 1], newName, resadmin);

            plugin.getSignUtil().updateSignResName(res);

            return feed;
        }

        lm.General_NoPermission.sendMessage(sender);

        return false;
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin) {
        giveResidence(reqPlayer, targPlayer, residence, resadmin, false);
    }

    public void giveResidence(Player reqPlayer, String targPlayer, String residence, boolean resadmin, boolean includeSubzones) {
        giveResidence(reqPlayer, targPlayer, getByName(residence), resadmin, includeSubzones);
    }

    public void giveResidence(Player reqPlayer, String targPlayer, ClaimedResidence res, boolean resadmin, boolean includeSubzones) {

        if (res == null) {
            lm.Invalid_Residence.sendMessage(reqPlayer);
            return;
        }

        String residence = res.getName();

        if (!res.getPermissions().hasResidencePermission(reqPlayer, true) && !resadmin) {
            lm.General_NoPermission.sendMessage(reqPlayer);
            return;
        }
        Player giveplayer = plugin.getServ().getPlayer(targPlayer);
        if (giveplayer == null || !giveplayer.isOnline()) {
            lm.General_NotOnline.sendMessage(reqPlayer);
            return;
        }
        CuboidArea[] areas = res.getAreaArray();

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(giveplayer);
        PermissionGroup group = rPlayer.getGroup();

        if (areas.length > group.getMaxPhysicalPerResidence() && !resadmin) {
            lm.Residence_GiveLimits.sendMessage(reqPlayer);
            return;
        }
        if (!hasMaxZones(giveplayer.getUniqueId(), rPlayer.getMaxRes()) && !resadmin) {
            lm.Residence_GiveLimits.sendMessage(reqPlayer);
            return;
        }
        if (!resadmin) {
            for (CuboidArea area : areas) {
                if (!res.isSubzone() && !res.isSmallerThanMax(giveplayer, area, resadmin) || res.isSubzone() && !res.isSmallerThanMaxSubzone(giveplayer, area,
                        resadmin)) {
                    lm.Residence_GiveLimits.sendMessage(reqPlayer);
                    return;
                }
            }
        }

        if (!res.getPermissions().setOwner(giveplayer, true))
            return;
        // Fix phrases here
        lm.Residence_Give.sendMessage(reqPlayer, residence, giveplayer.getName());
        lm.Residence_Received.sendMessage(giveplayer, residence, reqPlayer.getName());
        plugin.getSignUtil().updateSignResName(res);
        if (includeSubzones)
            for (ClaimedResidence one : res.getSubzones()) {
                giveResidence(reqPlayer, targPlayer, one, resadmin, includeSubzones);
            }
    }

    public void removeAllFromWorld(CommandSender sender, String world) {
        removeAllFromWorld(sender, world, null);
    }

    public void removeAllFromWorld(CommandSender sender, String world, List<String> playerExceptions) {
        int count = 0;
        Iterator<ClaimedResidence> it = residences.values().iterator();
        while (it.hasNext()) {
            ClaimedResidence next = it.next();

            if (!next.getPermissions().getWorldName().equals(world))
                continue;

            if (playerExceptions != null && !playerExceptions.isEmpty()) {
                if (playerExceptions.contains(next.getOwner().toLowerCase()))
                    continue;

                if (playerExceptions.contains(next.getOwnerUUID().toString()))
                    continue;
            }

            cleanResidenceRecords(next, false);

            it.remove();
            count++;
        }
        chunkResidences.remove(world);
        chunkResidences.put(world, new HashMap<ChunkRef, List<ClaimedResidence>>());
        if (count == 0) {
            sender.sendMessage(CMIChatColor.RED + "No residences found in world: " + CMIChatColor.YELLOW + world);
        } else {
            sender.sendMessage(CMIChatColor.RED + "Removed " + CMIChatColor.YELLOW + count + CMIChatColor.RED + " residences in world: " + CMIChatColor.YELLOW + world);
        }
    }

    private void cleanResidenceRecords(ClaimedResidence res, boolean removeSigns) {
        String name = res.getName();

        CMIDebug.d("removal?!");

        plugin.getLeaseManager().removeExpireTime(res);
        for (ClaimedResidence oneSub : res.getSubzones()) {
            plugin.getPlayerManager().removeResFromPlayer(res.getOwnerUUID(), oneSub);
            plugin.getRentManager().removeRentable(ClaimedResidence.getByName(name + "." + oneSub.getResidenceName()), removeSigns);
            plugin.getTransactionManager().removeFromSale(ClaimedResidence.getByName(name + "." + oneSub.getResidenceName()), removeSigns);
        }
        plugin.getPlayerManager().removeResFromPlayer(res.getOwnerUUID(), res);
        plugin.getRentManager().removeRentable(res, removeSigns);
        plugin.getTransactionManager().removeFromSale(res, removeSigns);

        giveBackOwnerMoneyForResidence(res);
    }

    public int getResidenceCount() {
        return residences.size();
    }

    public Map<String, ClaimedResidence> getResidences() {
        return residences;
    }

    @Deprecated
    public void removeChunkList(String name) {
        if (name == null)
            return;
        name = name.toLowerCase();
        ClaimedResidence res = residences.get(name);

        if (res == null)
            return;
        removeChunkList(res);
    }

    public void removeChunkList(ClaimedResidence res) {
        if (res == null)
            return;
        String world = res.getPermissions().getWorldName();

        Map<ChunkRef, List<ClaimedResidence>> worldChunks = chunkResidences.get(world);

        if (worldChunks == null)
            return;

        List<ChunkRef> chunks = getChunks(res);

        for (ChunkRef chunk : chunks) {
            List<ClaimedResidence> ress = worldChunks.get(chunk);
            if (ress == null)
                continue;
            ress.remove(res);
        }
    }

    @Deprecated
    public void calculateChunks2(String name) {
        if (name == null)
            return;
        name = name.toLowerCase();
        ClaimedResidence res = residences.get(name);
        if (res == null)
            return;
        calculateChunks(res);
    }

    public void calculateChunks(ClaimedResidence res) {
        if (res == null)
            return;
        String world = res.getPermissions().getWorldName();

        Map<ChunkRef, List<ClaimedResidence>> worldChunks = chunkResidences.computeIfAbsent(world, k -> new HashMap<ChunkRef, List<ClaimedResidence>>());

        List<ChunkRef> chunks = getChunks(res);

        for (ChunkRef chunk : chunks) {
            List<ClaimedResidence> resList = worldChunks.computeIfAbsent(chunk, k -> new ArrayList<ClaimedResidence>());
            if (!resList.contains(res))
                resList.add(res);
        }
    }

    public static final class ChunkRef {

        public static int getChunkCoord(final int val) {
            // For more info, see CraftBukkit.CraftWorld.getChunkAt( Location )
            return val >> 4;
        }

        private final int z;
        private final int x;

        public ChunkRef(Location loc) {
            this.x = getChunkCoord(loc.getBlockX());
            this.z = getChunkCoord(loc.getBlockZ());
        }

        public ChunkRef(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ChunkRef other = (ChunkRef) obj;
            return this.x == other.x && this.z == other.z;
        }

        @Override
        public int hashCode() {
            return x ^ z;
        }

        /**
         * Useful for debug
         * 
         * @return
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ x: ").append(x).append(", z: ").append(z).append(" }");
            return sb.toString();
        }

        public int getZ() {
            return z;
        }

        public int getX() {
            return x;
        }
    }

}