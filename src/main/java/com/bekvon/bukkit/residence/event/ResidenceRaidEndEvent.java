package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidenceRaidEndEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    protected boolean cancelled;
    private ClaimedResidence res;

    public ResidenceRaidEndEvent(ClaimedResidence res) {
        this.res = res;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        cancelled = bln;
    }

    public ClaimedResidence getRes() {
        return res;
    }
}
