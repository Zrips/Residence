package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ResidenceDeleteEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

    public enum DeleteCause {
	LEASE_EXPIRE, PLAYER_DELETE, OTHER
    }

    DeleteCause cause;

    public ResidenceDeleteEvent(Player player, ClaimedResidence resref, DeleteCause delcause) {
	super("RESIDENCE_DELETE", resref, player);
	cause = delcause;
    }

    public DeleteCause getCause() {
	return cause;
    }

}
