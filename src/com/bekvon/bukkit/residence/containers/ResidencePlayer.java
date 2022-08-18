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
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

public class ResidencePlayer {

    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;
    private UUID uuid = null;

    private Set<ClaimedResidence> ResidenceList = new HashSet<ClaimedResidence>();
    private Set<ClaimedResidence> trustedList = new HashSet<ClaimedResidence>();
    private ClaimedResidence mainResidence = null;

    private PlayerGroup groups = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;
    private int maxSubzoneDepth = -1;

    private int maxValue = 9999;

    private Long lastRaidAttackTimer = 0L;
    private Long lastRaidDefendTimer = 0L;

    private ResidenceRaid raid = null;

    public ResidencePlayer(OfflinePlayer off) {
	if (off == null)
	    return;
	this.uuid = off.getUniqueId();
	this.userName = off.getName();
	Residence.getInstance().addOfflinePlayerToChache(off);
	this.updatePlayer();
    }

    public ResidencePlayer(Player player) {
	if (player == null)
	    return;
	Residence.getInstance().addOfflinePlayerToChache(player);
	this.updatePlayer(player);
    }

    public boolean isOnline() {
	this.updatePlayer();
	if (this.player != null && this.player.isOnline())
	    return true;
	return false;
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
	    for (ClaimedResidence one : ResidenceList) {
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
	    for (ClaimedResidence one : ResidenceList) {
		if (one == null)
		    continue;
		mainResidence = one;
		return mainResidence;
	    }
	}
	return mainResidence;
    }

    public void recountMaxRes() {

	if (player != null && player.isOnline()) {
	    if (ResPerm.max_res_unlimited.hasSetPermission(player)) {
		this.maxRes = maxValue;
		return;
	    }
	} else if (ofPlayer != null && ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_res_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
	    this.maxRes = maxValue;
	    return;
	}

	int m = this.getGroup().getMaxZones();
	m = m == -1 ? maxValue : m;

	this.maxRes = Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_res_$1).getMaxValue(m);
    }

    public void recountMaxRents() {

	if (player != null) {
	    if (ResPerm.max_rents_unlimited.hasSetPermission(player)) {
		this.maxRents = maxValue;
		return;
	    }
	} else {
	    if (ofPlayer != null && ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_rents_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
		this.maxRents = maxValue;
		return;
	    }
	}

	int m = this.getGroup().getMaxRents();
	m = m == -1 ? maxValue : m;

	this.maxRents = Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_rents_$1).getMaxValue(m);
    }

    public int getMaxRents() {
	recountMaxRents();
	return this.maxRents;
    }

    public void recountMaxSubzones() {

	if (player != null) {
	    if (ResPerm.max_subzones_unlimited.hasSetPermission(player)) {
		this.maxSubzones = maxValue;
		return;
	    }
	} else {
	    if (ofPlayer != null && ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_subzones_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
		this.maxSubzones = maxValue;
		return;
	    }
	}

	int m = this.getGroup().getMaxSubzones();
	m = m == -1 ? maxValue : m;
	this.maxSubzones = Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_subzones_$1).getMaxValue(m);
    }

    public int getMaxSubzones() {
	recountMaxSubzones();
	return this.maxSubzones;
    }

    public void recountMaxSubzoneDepth() {

	if (player != null) {
	    if (ResPerm.max_subzonedepth_unlimited.hasSetPermission(player)) {
		this.maxSubzoneDepth = maxValue;
		return;
	    }
	} else {
	    if (ofPlayer != null && ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_subzonedepth_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
		this.maxSubzoneDepth = maxValue;
		return;
	    }
	}

	int m = this.getGroup().getMaxSubzoneDepth();
	m = m == -1 ? maxValue : m;

	this.maxSubzoneDepth = Residence.getInstance().getPermissionManager().getPermissionInfo(this.getUniqueId(), ResPerm.max_subzonedepth_$1).getMaxValue(m);

    }

    public int getMaxSubzoneDepth() {
	recountMaxSubzoneDepth();
	return this.maxSubzoneDepth;
    }

    public int getMaxRes() {
	recountMaxRes();
	PermissionGroup g = getGroup();
	if (this.maxRes < g.getMaxZones()) {
	    return g.getMaxZones();
	}
	return this.maxRes;
    }

    public PermissionGroup getGroup() {
	return getGroup(false);
    }

    public PermissionGroup forceUpdateGroup() {
	return getGroup(this.player != null ? player.getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld(), true);
    }

    public PermissionGroup getGroup(boolean forceUpdate) {
	updatePlayer();
	return getGroup(this.player != null ? player.getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld(), forceUpdate);
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
	this.player = player;
	this.uuid = player.getUniqueId();
	this.userName = player.getName();
	this.ofPlayer = player;
	return this;
    }

    public void onQuit() {
	this.ofPlayer = null;
	this.player = null;
	updated = false;
    }

    private void updatePlayer() {
	player = Bukkit.getPlayer(this.uuid);
	if (player != null)
	    updatePlayer(player);
	if (player != null && player.isOnline())
	    return;
	if (this.uuid != null && Bukkit.getPlayer(this.uuid) != null) {
	    player = Bukkit.getPlayer(this.uuid);
	    this.userName = player.getName();
	    return;
	}

	if (this.userName != null) {
	    player = Bukkit.getPlayer(this.userName);
	}
	if (player != null) {
	    this.userName = player.getName();
	    this.uuid = player.getUniqueId();
	    this.ofPlayer = player;
	    return;
	}
	if (this.player == null && ofPlayer == null)
	    ofPlayer = Residence.getInstance().getOfflinePlayer(userName);
	if (ofPlayer != null) {
		this.uuid = ofPlayer.getUniqueId();
		// Update the userName from cache, avoid reading NBT cost
	    this.userName = Residence.getInstance().getPlayerName(uuid);
	    return;
	}
    }

    public void addResidence(ClaimedResidence residence) {
	if (residence == null)
	    return;
	// Exclude subzones
	if (residence.isSubzone())
	    return;
	residence.getPermissions().setOwnerUUID(uuid);
	if (this.userName != null)
	    residence.getPermissions().setOwnerLastKnownName(userName);
	this.ResidenceList.add(residence);
    }

    public void removeResidence(ClaimedResidence residence) {
	if (residence == null)
	    return;
	boolean rem = this.ResidenceList.remove(residence);
	// in case its fails to remove, double check by name
	if (rem == false) {
	    Iterator<ClaimedResidence> iter = this.ResidenceList.iterator();
	    while (iter.hasNext()) {
		ClaimedResidence one = iter.next();
		if (one.getName().equalsIgnoreCase(residence.getName())) {
		    iter.remove();
		    break;
		}
	    }
	}
    }

    public int getResAmount() {
	int i = 0;
	for (ClaimedResidence one : ResidenceList) {
	    if (one.isSubzone())
		continue;
	    i++;
	}
	return i;
    }

    public List<ClaimedResidence> getResList() {
	List<ClaimedResidence> ls = new ArrayList<ClaimedResidence>();
	ls.addAll(ResidenceList);
	return ls;
    }

    @Deprecated
    public String getPlayerName() {
	return getName();
    }

    public String getName() {
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
	this.updatePlayer();
	return player;
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
	return lastRaidAttackTimer;
    }

    public void setLastRaidAttackTimer(Long lastRaidAttackTimer) {
	this.lastRaidAttackTimer = lastRaidAttackTimer;
    }

    public Long getLastRaidDefendTimer() {
	return lastRaidDefendTimer;
    }

    public void setLastRaidDefendTimer(Long lastRaidDefendTimer) {
	this.lastRaidDefendTimer = lastRaidDefendTimer;
    }

    public ResidenceRaid getJoinedRaid() {
	return raid;
    }

    public void setJoinedRaid(ResidenceRaid raid) {
	this.raid = raid;
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

    public void addTrustedResidence(ClaimedResidence residence) {
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
