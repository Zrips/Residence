package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ResidenceAreaDeleteEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

    DeleteCause cause;

    public ResidenceAreaDeleteEvent(Player player, ClaimedResidence resref, DeleteCause delcause) {
	super("RESIDENCE_AREA_DELETE", resref, player);
	cause = delcause;
    }

    public DeleteCause getCause() {
	return cause;
    }

}
