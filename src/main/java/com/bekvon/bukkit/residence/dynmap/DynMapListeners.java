package com.bekvon.bukkit.residence.dynmap;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class DynMapListeners implements Listener {

    private Residence plugin;

    public DynMapListeners(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaAdd(ResidenceAreaAddEvent event) {
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaDelete(ResidenceAreaDeleteEvent event) {
        plugin.getDynManager().fireUpdateRemove(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSubZoneCreate(ResidenceSubzoneCreationEvent event) {
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceFlagChange(ResidenceFlagChangeEvent event) {
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        plugin.getDynManager().fireUpdateRemove(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceOwnerChange(ResidenceOwnerChangeEvent event) {
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRename(ResidenceRenameEvent event) {
        plugin.getDynManager().handleResidenceRemove(event.getOldResidenceName(), event.getResidence(), event.getResidence().getSubzoneDeep());
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRent(ResidenceRentEvent event) {
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSizeChange(ResidenceSizeChangeEvent event) {
        plugin.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }
}
