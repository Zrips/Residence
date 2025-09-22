package com.bekvon.bukkit.residence.economy.rent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;

public class RentedLand {

    @Deprecated
    public String player = null;
    private UUID uuid = null;
    public long startTime = 0L;
    public long endTime = 0L;
    public boolean AutoPay = true;

    public boolean hasValidRenter() {
        return player != null || getUniqueId() != null;
    }

    public boolean isRenter(CommandSender sender) {
        if (sender instanceof Player)
            return isRenter((Player) sender);
        return false;
    }

    public boolean isRenter(Player player) {
        if (player == null)
            return false;
        return isRenter(player.getUniqueId());
    }

    public boolean isRenter(UUID playerUUID) {
        if (getUniqueId() != null)
            return getUniqueId().equals(playerUUID);

        ResidencePlayer resP = Residence.getInstance().getPlayerManager().getResidencePlayer(player);

        if (resP != null)
            setUniqueId(resP.getUniqueId());

        return resP != null && resP.getUniqueId().equals(playerUUID);
    }

    @Deprecated
    public boolean isRenter(String playerName) {
        if (player != null)
            return player.equals(playerName);

        if (getUniqueId() == null)
            return false;

        ResidencePlayer resP = Residence.getInstance().getPlayerManager().getResidencePlayer(getUniqueId());
        return resP != null && resP.getName().equals(playerName);
    }

    public @Nullable String getRenterName() {
        if (player != null)
            return player;
        ResidencePlayer resP = Residence.getInstance().getPlayerManager().getResidencePlayer(getUniqueId());
        return resP == null ? null : resP.getName();
    }

    public Map<String, Object> save() {
        Map<String, Object> rentables = new HashMap<>();
        if (getUniqueId() == null)
            rentables.put("Player", player);
        else if (getUniqueId() != null)
            rentables.put("UUID", getUniqueId().toString());
        rentables.put("StartTime", startTime);
        rentables.put("EndTime", endTime);
        rentables.put("AutoRefresh", AutoPay);
        return rentables;
    }

    public static RentedLand loadRented(Map<String, Object> map) {
        RentedLand newland = new RentedLand();
        if (map.containsKey("Player"))
            newland.player = (String) map.get("Player");
        if (map.containsKey("UUID")) {
            try {
                newland.uuid = UUID.fromString((String) map.get("UUID"));
            } catch (IllegalArgumentException e) {
                newland.uuid = null;
            }
        }
        newland.startTime = (Long) map.get("StartTime");
        newland.endTime = (Long) map.get("EndTime");
        newland.AutoPay = (Boolean) map.get("AutoRefresh");
        return newland;
    }

    public UUID getUniqueId() {

        if (uuid == null) {
            ResidencePlayer resP = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
            if (resP != null)
                setUniqueId(resP.getUniqueId());
        }

        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }
}
