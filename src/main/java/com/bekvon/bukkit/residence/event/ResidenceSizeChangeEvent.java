package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ResidenceSizeChangeEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

    protected String resname;
    CuboidArea oldarea;
    CuboidArea newarea;
    ClaimedResidence res;

    public ResidenceSizeChangeEvent(Player player, ClaimedResidence res, CuboidArea oldarea, CuboidArea newarea) {
	super("RESIDENCE_SIZE_CHANGE", res, player);
	resname = res.getName();
	this.res = res;
	this.oldarea = oldarea;
	this.newarea = newarea;
    }

    public String getResidenceName() {
	return resname;
    }

    @Override
    public ClaimedResidence getResidence() {
	return res;
    }

    public CuboidArea getOldArea() {
	return oldarea;
    }

    public CuboidArea getNewArea() {
	return newarea;
    }
}
