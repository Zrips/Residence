package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;

public class ResidencePlayerEvent extends ResidenceEvent implements ResidencePlayerEventInterface {

    Player p;

    public ResidencePlayerEvent(String eventName, ClaimedResidence resref, Player player) {
        super(eventName, resref);
        res = resref;
        p = player;
    }

    @Override
    public boolean isPlayer() {
        return p != null;
    }

    @Override
    public boolean isAdmin() {
        if (isPlayer()) {
            return Residence.getInstance().getPermissionManager().isResidenceAdmin(p);
        }
        return true;
    }

    @Override
    public Player getPlayer() {
        return p;
    }
}
