package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidenceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    ClaimedResidence res;
    private String message;

    public ResidenceEvent(String eventName, ClaimedResidence resref) {
        message = eventName;
        res = resref;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getMessage() {
        return message;
    }

    public ClaimedResidence getResidence() {
        return res;
    }
}
