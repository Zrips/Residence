package com.bekvon.bukkit.residence.containers;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

public class playerTempData {

    protected static ConcurrentHashMap<UUID, playerTempData> playersTempData = new ConcurrentHashMap<UUID, playerTempData>();

    private ClaimedResidence currentRes = null;
    private Location lastInsideLoc = null;

    private boolean canTeleportInsideCurrent = true;

    private long lastUpdate = 0L;
    private long lastCheck = 0L;
    private Vector lastLocation = null;
    private StuckInfo stuckTeleportCounter = null;

    private long lastEnterLeaveInformTime = 0L;

    public ClaimedResidence getCurrentResidence() {
        return currentRes;
    }

    public boolean setCurrentResidence(Player player, ClaimedResidence currentRes) {

        if (currentRes == null) {
            setCanTeleportInsideCurrent(true);
            setLastInsideLoc(null);
            boolean previous = this.currentRes != null;
            this.currentRes = currentRes;
            return previous;
        }

        if (this.currentRes != null && this.currentRes.equals(currentRes)) {
            return false;
        }

        setCanTeleportInsideCurrent(
            (currentRes.getPermissions().playerHas(player, Flags.tp, FlagCombo.TrueOrNone) || ResPerm.admin_tp.hasPermission(player, 10000L)) &&
                (currentRes.getPermissions().playerHas(player, Flags.move, FlagCombo.TrueOrNone) || ResPerm.admin_move.hasPermission(player, 10000L)));

        if (isCanTeleportInsideCurrent()) {
            setLastInsideLoc(player.getLocation());
        } else {
            setLastInsideLoc(null);
        }
        this.currentRes = currentRes;
        return true;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public StuckInfo getStuckTeleportCounter() {
        if (stuckTeleportCounter == null)
            stuckTeleportCounter = new StuckInfo();
        return stuckTeleportCounter;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(long lastCheck) {
        this.lastCheck = lastCheck;
    }

    public Vector getLastLocation(Vector defaultVector) {
        if (lastLocation == null)
            lastLocation = defaultVector;
        return lastLocation;
    }

    public void setLastLocation(Vector lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Location getLastInsideLoc() {
        return lastInsideLoc;
    }

    public Location getLastValidLocation(Player player) {

        Location loc = null;

        if (lastInsideLoc != null) {
            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(lastInsideLoc);
            if (res != null && Flags.tp.isGlobalyEnabled() && (res.getPermissions().playerHas(player, Flags.tp, FlagCombo.TrueOrNone) || ResAdmin.isResAdmin(player)
                || res.isOwner(player) || ResPerm.admin_move.hasPermission(player, 10000L))) {
                loc = lastInsideLoc;
            }
        }

        loc = validated(loc, player.getWorld());

        if (loc != null)
            return loc;

        loc = playerPersistentData.get(player).getLastOutsideLoc();

        return validated(loc, player.getWorld());
    }

    private static Location validated(Location loc, World world) {
        if (loc == null)
            return loc;

        if (loc.getWorld() != null && !loc.getWorld().equals(world) && Residence.getInstance().getConfigManager().getKickLocation() != null) {
            return Residence.getInstance().getConfigManager().getKickLocation();
        }
        return loc;
    }

    public void setLastInsideLoc(Location lastInsideLoc) {
        this.lastInsideLoc = lastInsideLoc;
    }

    public boolean isCanTeleportInsideCurrent() {
        return canTeleportInsideCurrent;
    }

    public void setCanTeleportInsideCurrent(boolean canTeleportInsideCurrent) {
        this.canTeleportInsideCurrent = canTeleportInsideCurrent;
    }

    public static playerTempData get(Player player) {
        return get(player.getUniqueId());
    }

    public static playerTempData get(UUID uuid) {
        return playersTempData.computeIfAbsent(uuid, k -> new playerTempData());
    }

    public static void clear() {
        playersTempData.clear();
    }

    public static void clearCache(UUID uuid) {
        playersTempData.remove(uuid);
    }

    public static ClaimedResidence getCurrentResidence(UUID uuid) {
        playerTempData record = playersTempData.get(uuid);
        return record == null ? null : record.getCurrentResidence();
    }

    public long getLastEnterLeaveInformTime() {
        return lastEnterLeaveInformTime;
    }

    public void setLastEnterLeaveInformTime(long lastEnterLeaveInformTime) {
        this.lastEnterLeaveInformTime = lastEnterLeaveInformTime;
    }
}
