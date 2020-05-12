package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ResidenceTPEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    Player reqPlayer;
    Location loc;

    public ResidenceTPEvent(ClaimedResidence resref, Location teleloc, Player player, Player reqplayer) {
        super("RESIDENCE_TP", resref, player);
        reqPlayer = reqplayer;
        loc = teleloc;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getRequestingPlayer() {
        return reqPlayer;
    }

    public Location getTeleportLocation() {
        return loc;
    }
}
