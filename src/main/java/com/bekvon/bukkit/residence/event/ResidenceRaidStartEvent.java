package com.bekvon.bukkit.residence.event;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.raid.RaidAttacker;

public class ResidenceRaidStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

    private ClaimedResidence res;
    private HashMap<UUID, RaidAttacker> attackers;
    protected boolean cancelled;

    public ResidenceRaidStartEvent(ClaimedResidence res, HashMap<UUID, RaidAttacker> hashMap) {
	this.res = res;
	this.attackers = hashMap;
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

    public HashMap<UUID, RaidAttacker> getAttackers() {
	return attackers;
    }
}
