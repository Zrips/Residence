package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import org.bukkit.event.HandlerList;

public class ResidenceFlagEvent extends ResidenceEvent {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public enum FlagType {
		RESIDENCE, GROUP, PLAYER
	}

	String flagname;
	FlagType flagtype;
	FlagState flagstate;
	String flagtarget;

	public ResidenceFlagEvent(String eventName, ClaimedResidence resref, String flag, FlagType type, String target) {
		super(eventName, resref);
		flagname = flag;
		flagtype = type;
		flagtarget = target;
	}

	public String getFlag() {
		return flagname;
	}

	public FlagType getFlagType() {
		return flagtype;
	}

	public String getFlagTargetPlayerOrGroup() {
		return flagtarget;
	}
}
