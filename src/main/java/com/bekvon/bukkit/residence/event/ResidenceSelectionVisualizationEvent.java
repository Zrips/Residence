package com.bekvon.bukkit.residence.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayType;

import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;

public final class ResidenceSelectionVisualizationEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private boolean shortShow = false;

    private HashMap<CuboidDisplayType, List<CMIBlockWorldArea>> areas = new HashMap<CuboidDisplayType, List<CMIBlockWorldArea>>();

    @Deprecated
    public ResidenceSelectionVisualizationEvent(Player player, List<CuboidArea> areas, List<CuboidArea> errorAreas) {
        this(player, areas, errorAreas, false);
    }

    @Deprecated
    public ResidenceSelectionVisualizationEvent(Player player, List<CuboidArea> areas, List<CuboidArea> errorAreas, boolean shortShow) {
        super(player);

        for (CuboidArea area : areas) {
            this.areas.computeIfAbsent(CuboidDisplayType.SELECTION, k -> new java.util.ArrayList<>()).add(new CMIBlockWorldArea(area.getWorld(), area.getLowVector(), area.getHighVector()));
        }
        for (CuboidArea area : errorAreas) {
            this.areas.computeIfAbsent(CuboidDisplayType.ERROR, k -> new java.util.ArrayList<>()).add(new CMIBlockWorldArea(area.getWorld(), area.getLowVector(), area.getHighVector()));
        }

        this.shortShow = shortShow;
    }

    public ResidenceSelectionVisualizationEvent(Player player, HashMap<CuboidDisplayType, List<CMIBlockWorldArea>> areas, boolean shortShow) {
        super(player);
        this.areas = areas;
        this.shortShow = shortShow;
    }

    public final static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public final HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public final void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public final boolean isCancelled() {
        return cancel;
    }

    @Deprecated
    public List<CuboidArea> getAreas() {
        List<CuboidArea> areas = new ArrayList<CuboidArea>();

        List<CMIBlockWorldArea> temp = this.areas.get(CuboidDisplayType.SELECTION);

        if (temp == null)
            return areas;

        for (CMIBlockWorldArea area : temp) {
            areas.add(new CuboidArea(area.getLowPoint().toLocation(area.getWorld().getWorld()), area.getHighPoint().toLocation(area.getWorld().getWorld())));
        }

        return areas;
    }

    @Deprecated
    public List<CuboidArea> getErrorAreas() {

        List<CuboidArea> areas = new ArrayList<CuboidArea>();

        List<CMIBlockWorldArea> temp = this.areas.get(CuboidDisplayType.ERROR);

        if (temp == null)
            return areas;

        for (CMIBlockWorldArea area : temp) {
            areas.add(new CuboidArea(area.getLowPoint().toLocation(area.getWorld().getWorld()), area.getHighPoint().toLocation(area.getWorld().getWorld())));
        }

        return areas;
    }

    public HashMap<CuboidDisplayType, List<CMIBlockWorldArea>> getAreasMap() {
        return areas;
    }

    public boolean isShortShow() {
        return shortShow;
    }
}
