package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ResidenceOwnerChangeEvent extends ResidenceEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

    protected String newowner;
    private UUID uuid;
    protected boolean cancelled;

    @Deprecated
    public ResidenceOwnerChangeEvent(ClaimedResidence resref, String newOwner) {
	super("RESIDENCE_OWNER_CHANGE", resref);
	this.newowner = newOwner;
    }

    public ResidenceOwnerChangeEvent(ClaimedResidence resref, String newOwner, UUID uuid) {
	super("RESIDENCE_OWNER_CHANGE", resref);
	this.newowner = newOwner;
	this.uuid = uuid;
    }

    public ResidenceOwnerChangeEvent(ClaimedResidence resref, Player player) {
	super("RESIDENCE_OWNER_CHANGE", resref);
	this.newowner = player.getName();
	this.uuid = player.getUniqueId();
    }

    public String getNewOwner() {
	return newowner;
    }

    public UUID getNewOwnerUuid() {
	return uuid;
    }

    @Override
    public boolean isCancelled() {
	return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
	cancelled = bln;
    }
}
