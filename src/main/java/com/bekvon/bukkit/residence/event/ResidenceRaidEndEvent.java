package com.bekvon.bukkit.residence.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceRaidEndEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private ClaimedResidence res;
    protected boolean cancelled;

    public ResidenceRaidEndEvent(ClaimedResidence res) {
        this.res = res;
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
