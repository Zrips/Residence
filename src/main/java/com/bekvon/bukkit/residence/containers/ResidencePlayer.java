package com.bekvon.bukkit.residence.containers;

import com.bekvon.bukkit.residence.BossBar.BossBarInfo;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

public class ResidencePlayer {

    HashMap<String, BossBarInfo> barMap = new HashMap<String, BossBarInfo>();
    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;
    private UUID uuid = null;
    private Set<ClaimedResidence> ResidenceList = new HashSet<ClaimedResidence>();
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
    private Long lastMaxResRecalculation = 0L;
    private Long lastMaxRentsRecalculation = 0L;
    private Long lastMaxSubzonesRecalculation = 0L;
    private Long lastMaxSubzonesDepthRecalculation = 0L;
    private boolean updated = false;

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

    public ResidencePlayer(String userName, UUID uuid) {
        this.userName = userName;
        this.uuid = uuid;
    }

    public ResidencePlayer(String userName) {
        this.userName = userName;
    }

    public boolean isOnline() {
        this.updatePlayer();
        if (this.player != null && this.player.isOnline())
            return true;
        return false;
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

    public void setMainResidence(ClaimedResidence res) {
        if (mainResidence != null)
            mainResidence.setMainResidence(false);
        mainResidence = res;
    }

    public void recountMaxRes() {
        if (lastMaxResRecalculation + 10000L > System.currentTimeMillis())
            return;
        lastMaxResRecalculation = System.currentTimeMillis();
        if (this.getGroup() != null)
            this.maxRes = this.getGroup().getMaxZones();
        this.maxRes = this.maxRes == -1 ? maxValue : this.maxRes;

        if (player != null && player.isOnline()) {
            if (ResPerm.max_res_unlimited.hasSetPermission(player)) {
                this.maxRes = maxValue;
                return;
            }
        } else if (ofPlayer != null) {
            if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_res_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
                this.maxRes = maxValue;
                return;
            }
        }

        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxResCount(); i++) {
            if (player != null && player.isOnline()) {
                if (ResPerm.max_res_$1.hasSetPermission(player, String.valueOf(i)))
                    this.maxRes = i;
            } else if (ofPlayer != null) {
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_res_$1.getPermission(String.valueOf(i)), Residence.getInstance().getConfigManager().getDefaultWorld()))
                    this.maxRes = i;
            }
        }
    }

    public void recountMaxRents() {
        if (lastMaxRentsRecalculation + 10000L > System.currentTimeMillis())
            return;
        lastMaxRentsRecalculation = System.currentTimeMillis();

        if (player != null) {
            if (ResPerm.max_rents_unlimited.hasSetPermission(player)) {
                this.maxRents = maxValue;
                return;
            }
        } else {
            if (ofPlayer != null)
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_rents_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
                    this.maxRents = maxValue;
                    return;
                }
        }
        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxRentCount(); i++) {
            if (player != null) {
                if (ResPerm.max_rents_$1.hasSetPermission(player, String.valueOf(i)))
                    this.maxRents = i;
            } else {
                if (ofPlayer != null)
                    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_rents_$1.getPermission(String.valueOf(i)), Residence.getInstance().getConfigManager().getDefaultWorld()))
                        this.maxRents = i;
            }
        }

        int m = this.getGroup().getMaxRents();
        m = m == -1 ? maxValue : m;
        if (this.maxRents < m)
            this.maxRents = m;
    }

    public int getMaxRents() {
        recountMaxRents();
        return this.maxRents;
    }

    public void recountMaxSubzones() {
        if (lastMaxSubzonesRecalculation + 10000L > System.currentTimeMillis())
            return;
        lastMaxSubzonesRecalculation = System.currentTimeMillis();

        if (player != null) {
            if (ResPerm.max_subzones_unlimited.hasSetPermission(player)) {
                this.maxSubzones = maxValue;
                return;
            }
        } else {
            if (ofPlayer != null)
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_subzones_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
                    this.maxSubzones = maxValue;
                    return;
                }
        }
        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxSubzonesCount(); i++) {
            if (player != null) {
                if (ResPerm.max_subzones_$1.hasSetPermission(player, String.valueOf(i)))
                    this.maxSubzones = i;
            } else {
                if (ofPlayer != null)
                    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_subzones_$1.getPermission(String.valueOf(i)), Residence.getInstance().getConfigManager().getDefaultWorld()))
                        this.maxSubzones = i;
            }
        }

        int m = this.getGroup().getMaxSubzones();
        m = m == -1 ? maxValue : m;
        if (this.maxSubzones < m)
            this.maxSubzones = m;
    }

    public int getMaxSubzones() {
        recountMaxSubzones();
        return this.maxSubzones;
    }

    public void recountMaxSubzoneDepth() {
        if (lastMaxSubzonesDepthRecalculation + 10000L > System.currentTimeMillis())
            return;
        lastMaxSubzonesDepthRecalculation = System.currentTimeMillis();

        if (player != null) {
            if (ResPerm.max_subzonedepth_unlimited.hasSetPermission(player)) {
                this.maxSubzoneDepth = maxValue;
                return;
            }
        } else {
            if (ofPlayer != null)
                if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_subzonedepth_unlimited.getPermission(), Residence.getInstance().getConfigManager().getDefaultWorld())) {
                    this.maxSubzoneDepth = maxValue;
                    return;
                }
        }
        for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxSubzoneDepthCount(); i++) {
            if (player != null) {
                if (ResPerm.max_subzonedepth_$1.hasSetPermission(player, String.valueOf(i)))
                    this.maxSubzoneDepth = i;
            } else {
                if (ofPlayer != null)
                    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, ResPerm.max_subzonedepth_$1.getPermission(String.valueOf(i)), Residence.getInstance().getConfigManager().getDefaultWorld()))
                        this.maxSubzoneDepth = i;
            }
        }

        int m = this.getGroup().getMaxSubzoneDepth();
        m = m == -1 ? maxValue : m;
        if (this.maxSubzoneDepth < m)
            this.maxSubzoneDepth = m;
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

    public PermissionGroup forceUpdateGroup() {
        updatePlayer();
        return getGroup(this.player != null ? player.getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld(), true);
    }

    public PermissionGroup getGroup() {
        updatePlayer();
        return getGroup(this.player != null ? player.getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld());
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
            this.userName = ofPlayer.getName();
            this.uuid = ofPlayer.getUniqueId();
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
        if (!rem) {
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
        List<ClaimedResidence> ls = new ArrayList<ClaimedResidence>(ResidenceList);
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

    public void removeBossBar(BossBarInfo bossBar) {
        if (bossBar == null)
            return;
        if (bossBar.getBar() != null)
            bossBar.getBar().setVisible(false);
        bossBar.cancelAutoScheduler();
        bossBar.cancelHideScheduler();
        barMap.remove(bossBar.getNameOfBar().toLowerCase());
    }

    public void addBossBar(BossBarInfo barInfo) {
        if (!barMap.containsKey(barInfo.getNameOfBar().toLowerCase())) {
            barMap.put(barInfo.getNameOfBar().toLowerCase(), barInfo);
            Residence.getInstance().getBossBarManager().Show(barInfo);
        } else {
            BossBarInfo old = getBossBar(barInfo.getNameOfBar().toLowerCase());
            if (old != null) {

                if (barInfo.getColor() != null)
                    old.setColor(barInfo.getColor());

                if (barInfo.getKeepFor() != null)
                    old.setKeepForTicks(barInfo.getKeepFor());

                if (barInfo.getPercentage() != null)
                    old.setPercentage(barInfo.getPercentage());

                if (barInfo.getUser() != null)
                    old.setUser(barInfo.getUser());

                if (barInfo.getAdjustPerc() != null)
                    old.setAdjustPerc(barInfo.getAdjustPerc());

                if (barInfo.getStyle() != null)
                    old.setStyle(barInfo.getStyle());

                if (!barInfo.getTitleOfBar().isEmpty())
                    old.setTitleOfBar(barInfo.getTitleOfBar());

                if (barInfo.getBar() != null)
                    old.setBar(barInfo.getBar());

                if (barInfo.getId() != null)
                    old.setId(barInfo.getId());

                if (barInfo.getAuto() != null)
                    old.setAuto(barInfo.getAuto());
            }
            Residence.getInstance().getBossBarManager().Show(old);
        }
    }

    public BossBarInfo getBossBar(String name) {
        return barMap.get(name.toLowerCase());
    }

    public synchronized HashMap<String, BossBarInfo> getBossBarInfo() {
        return this.barMap;
    }

    public synchronized void hideBossBars() {
        for (Entry<String, BossBarInfo> one : this.barMap.entrySet()) {
            one.getValue().getBar().setVisible(false);
        }
    }

    public void clearBossMaps() {
        for (Entry<String, BossBarInfo> one : barMap.entrySet()) {
            one.getValue().cancelHideScheduler();
        }
        barMap.clear();
    }

    public ClaimedResidence getCurrentlyRaidedResidence() {
        for (ClaimedResidence one : getResList()) {
            if (one.isUnderRaid() || one.isInPreRaid()) {
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
}
