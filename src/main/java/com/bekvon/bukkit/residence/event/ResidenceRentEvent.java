package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ResidenceRentEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    RentEventType eventtype;

    public ResidenceRentEvent(ClaimedResidence resref, Player player, RentEventType type) {
        super("RESIDENCE_RENT_EVENT", resref, player);
        eventtype = type;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public RentEventType getCause() {
        return eventtype;
    }

    public enum RentEventType {
        RENT, UNRENT, RENTABLE, UNRENTABLE, RENT_EXPIRE
    }

}
