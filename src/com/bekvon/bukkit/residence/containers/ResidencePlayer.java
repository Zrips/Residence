package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.utils.PlayerCache;

public class ResidencePlayer {

    private String userName = null;
    private UUID uuid = null;

    private Set<ClaimedResidence> residenceList = new HashSet<ClaimedResidence>();
    private Set<ClaimedResidence> trustedList = new HashSet<ClaimedResidence>();
    private ClaimedResidence mainResidence = null;

    private PlayerGroup groups = null;

    private static final int maxValue = 9999;

    public ResidencePlayer(OfflinePlayer off) {
        if (off == null)
            return;
        this.uuid = off.getUniqueId();
        this.userName = off.getName();
        PlayerCache.addToCache(userName, uuid);
        this.updatePlayer();
    }

    public ResidencePlayer(Player player) {
        if (player == null)
            return;
        this.updatePlayer(player);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(this.uuid) != null;
    }

    public ResidencePlayer(String userName, UUID uuid) {
        this.userName = userName;
        this.uuid = uuid;
    }

    public ResidencePlayer(String userName) {
        this.userName = userName;
    }

    public void setMainResidence(ClaimedResidence res) {
        if (mainResidence != null)
            mainResidence.setMainResidence(false);
        mainResidence = res;
    }

    public ClaimedResidence getMainResidence() {
        if (mainResidence == null || !mainResidence.isOwner(this.getPlayerName())) {
            for (ClaimedResidence one : residenceList) {
                if (one == null)
                    continue;
                if (one.isMainResidence()) {
                    mainResidence = one;
                    return mainResidence;
                }
            }
            for (String one : Residence.getInstance().getRentManager().getRentedLands(this.userName)) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(one);
                if (res != null) {
                    mainResidence = res;
                    return mainResidence;
                }
            }
            for (ClaimedResidence one : residenceList) {
                if (one == null)
                    continue;
                mainResidence = one;
                return mainResidence;
            }
        }
        return mainResidence;
    }

    private ResidencePlayerMaxValues getMaxData() {
        return ResidencePlayerMaxValues.get(getUniqueId());
    }

    public void recountMaxX() {
        int m = this.getGroup().getMaxX();
        m = m == -1 ? getMaxData().getMaxX() : m;
        getMaxData().setMaxX(Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_res_x_$1).getMaxValue(m));
    }

    public void recountMaxZ() {
        int m = this.getGroup().getMaxZ();
        m = m == -1 ? getMaxData().getMaxZ() : m;
        getMaxData().setMaxZ(Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_res_z_$1).getMaxValue(m));
    }

    public void recountMaxRes() {

        if (getPlayer() != null && getPlayer().isOnline()) {
            if (ResPerm.max_res_unlimited.hasSetPermission(getPlayer())) {
                getMaxData().setMaxRes(maxValue);
                return;
            }
        }

        int m = this.getGroup().getMaxZones();
        m = m == -1 ? maxValue : m;

        getMaxData().setMaxRes(Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_res_$1).getMaxValue(m));
    }

    public void recountMaxRents() {

        if (getPlayer() != null) {
            if (ResPerm.max_rents_unlimited.hasSetPermission(getPlayer())) {
                getMaxData().setMaxRents(maxValue);
                return;
            }
        }

        int m = this.getGroup().getMaxRents();
        m = m == -1 ? maxValue : m;

        getMaxData().setMaxRents(Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_rents_$1).getMaxValue(m));
    }

    public int getMaxRents() {
        recountMaxRents();
        return getMaxData().getMaxRents();
    }

    public void recountMaxSubzones() {

        if (getPlayer() != null) {
            if (ResPerm.max_subzones_unlimited.hasSetPermission(getPlayer())) {
                getMaxData().setMaxSubzones(maxValue);
                return;
            }
        }

        int m = this.getGroup().getMaxSubzones();
        m = m == -1 ? maxValue : m;
        getMaxData().setMaxSubzones(Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_subzones_$1).getMaxValue(m));
    }

    public int getMaxSubzones() {
        recountMaxSubzones();
        return getMaxData().getMaxSubzones();
    }

    public void recountMaxSubzoneDepth() {

        if (getPlayer() != null) {
            if (ResPerm.max_subzonedepth_unlimited.hasSetPermission(getPlayer())) {
                getMaxData().setMaxSubzoneDepth(maxValue);
                return;
            }
        }

        int m = this.getGroup().getMaxSubzoneDepth();
        m = m == -1 ? maxValue : m;

        getMaxData().setMaxSubzoneDepth(Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_subzonedepth_$1).getMaxValue(m));

    }

    public int getMaxSubzoneDepth() {
        recountMaxSubzoneDepth();
        return getMaxData().getMaxSubzoneDepth();
    }

    public int getMaxRes() {
        recountMaxRes();
        PermissionGroup g = getGroup();
        if (getMaxData().getMaxRes() < g.getMaxZones()) {
            return g.getMaxZones();
        }
        return getMaxData().getMaxRes();
    }

    public int getMaxX() {
        recountMaxX();
        PermissionGroup g = getGroup();
        if (getMaxData().getMaxX() < g.getMaxX()) {
            return g.getMaxX();
        }
        return getMaxData().getMaxX();
    }

    public int getMaxZ() {
        recountMaxZ();
        PermissionGroup g = getGroup();
        if (getMaxData().getMaxZ() < g.getMaxZ()) {
            return g.getMaxZ();
        }
        return getMaxData().getMaxZ();
    }

    public PermissionGroup getGroup() {
        return getGroup(false);
    }

    public PermissionGroup forceUpdateGroup() {
        return getGroup(getPlayer() != null ? getPlayer().getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld(), true);
    }

    public PermissionGroup getGroup(boolean forceUpdate) {
        updatePlayer();
        return getGroup(getPlayer() != null ? getPlayer().getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld(), forceUpdate);
    }

    public PermissionGroup getGroup(String world) {
        return getGroup(world, false);
    }

    public PermissionGroup getGroup(String world, boolean force) {
        if (groups == null)
            groups = new PlayerGroup(this);
        groups.updateGroup(world, force);
        PermissionGroup group = groups.getGroup(world);
        if (group == null)
            group = Residence.getInstance().getPermissionManager().getDefaultGroup();
        return group;
    }

    private boolean updated = false;

    public ResidencePlayer updatePlayer(Player player) {
        if (updated)
            return this;
        if (player.isOnline())
            updated = true;
        this.uuid = player.getUniqueId();
        this.userName = player.getName();
        PlayerCache.addToCache(player);
        return this;
    }

    public ResidencePlayer updatePlayer(OfflinePlayer player) {
        if (updated)
            return this;
        if (player.isOnline())
            updated = true;

        this.uuid = player.getUniqueId();
        this.userName = player.getName();
        return this;
    }

    public void onQuit() {
        updated = false;
    }

    private void updatePlayer() {
        Player player = null;
        if (this.uuid != null)
            player = Bukkit.getPlayer(this.uuid);

        if (player != null) {
            updatePlayer(player);
            return;
        }

        if (this.userName != null) {
            player = Bukkit.getPlayerExact(this.userName);
        }

        if (player != null) {
            updatePlayer(player);
            return;
        }

        this.userName = PlayerCache.getName(this.getUniqueId());
    }

    public synchronized void addResidence(ClaimedResidence residence) {
        if (residence == null)
            return;
        // Exclude subzones
        if (residence.isSubzone())
            return;
        residence.getPermissions().setOwnerUUID(uuid);
        if (this.userName != null)
            residence.getPermissions().setOwnerLastKnownName(userName);
        this.residenceList.add(residence);
    }

    public void removeResidence(ClaimedResidence residence) {
        if (residence == null)
            return;
        boolean removed = this.residenceList.remove(residence);
        // in case its fails to remove, double check by name
        if (removed)
            return;

        Iterator<ClaimedResidence> iter = this.residenceList.iterator();
        while (iter.hasNext()) {
            ClaimedResidence one = iter.next();
            if (one.getName().equalsIgnoreCase(residence.getName())) {
                iter.remove();
                break;
            }
        }
    }

    public int getResAmount() {
        int i = 0;
        for (ClaimedResidence one : residenceList) {
            if (one.isSubzone())
                continue;
            i++;
        }
        return i;
    }

    public List<ClaimedResidence> getResList() {
        return new ArrayList<ClaimedResidence>(residenceList);
    }

    @Deprecated
    public String getPlayerName() {
        return getName();
    }

    public String getName() {
        if (userName == null)
            this.updatePlayer();
        return userName;
    }

    public UUID getUniqueId() {
        return uuid;

    }

    @Deprecated
    public UUID getUuid() {
        return getUniqueId();
    }

    public Player getPlayer() {
        if (this.getUniqueId() != null)
            return Bukkit.getPlayer(this.getUniqueId());
        else if (this.getName() != null)
            return Bukkit.getPlayerExact(this.getName());
        return null;
    }

    public ClaimedResidence getCurrentlyRaidedResidence() {
        for (ClaimedResidence one : getResList()) {
            if (one.getRaid().isUnderRaid() || one.getRaid().isInPreRaid()) {
                return one;
            }
        }
        return null;
    }

    public Long getLastRaidAttackTimer() {
        return ResidencePlayerRaidData.get(this.getUniqueId()).getLastRaidAttackTimer();
    }

    public void setLastRaidAttackTimer(Long lastRaidAttackTimer) {
        ResidencePlayerRaidData.get(this.getUniqueId()).setLastRaidAttackTimer(lastRaidAttackTimer);
    }

    public Long getLastRaidDefendTimer() {
        return ResidencePlayerRaidData.get(this.getUniqueId()).getLastRaidDefendTimer();
    }

    public void setLastRaidDefendTimer(Long lastRaidDefendTimer) {
        ResidencePlayerRaidData.get(this.getUniqueId()).setLastRaidDefendTimer(lastRaidDefendTimer);
    }

    public ResidenceRaid getJoinedRaid() {
        return ResidencePlayerRaidData.get(this.getUniqueId()).getJoinedRaid();
    }

    public void setJoinedRaid(ResidenceRaid raid) {
        ResidencePlayerRaidData.get(this.getUniqueId()).setJoinedRaid(raid);
    }

    public PlayerGroup getGroups() {
        return groups;
    }

    @Deprecated
    public boolean canBreakBlock(Location loc, boolean inform) {
        return canBreakBlock(loc.getBlock(), inform);
    }

    public boolean canBreakBlock(Block block, boolean inform) {
        return ResidenceBlockListener.canBreakBlock(this.getPlayer(), block, inform);
    }

    @Deprecated
    public boolean canPlaceBlock(Location loc, boolean inform) {
        return canPlaceBlock(loc.getBlock(), inform);
    }

    public boolean canPlaceBlock(Block block, boolean inform) {
        return ResidenceBlockListener.canPlaceBlock(this.getPlayer(), block, inform);
    }

    public boolean canDamageEntity(Entity entity, boolean inform) {
        return ResidenceEntityListener.canDamageEntity(this.getPlayer(), entity, inform);
    }

    public Set<ClaimedResidence> getTrustedResidenceList() {
        return trustedList;
    }

    public synchronized void addTrustedResidence(ClaimedResidence residence) {
        if (residence == null)
            return;
        this.trustedList.add(residence);
    }

    public void removeTrustedResidence(ClaimedResidence residence) {
        if (residence == null)
            return;
        this.trustedList.remove(residence);
    }
//    public boolean canDamagePlayer(Player player, boolean inform) {
//
//    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public static ResidencePlayer get(String name) {
        return Residence.getInstance().getPlayerManager().getResidencePlayer(name);
    }

    public static ResidencePlayer get(Player player) {
        return Residence.getInstance().getPlayerManager().getResidencePlayer(player);
    }

    public static ResidencePlayer get(UUID uuid) {
        return Residence.getInstance().getPlayerManager().getResidencePlayer(uuid);
    }

}
