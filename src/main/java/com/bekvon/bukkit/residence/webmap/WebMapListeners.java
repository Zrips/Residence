package com.bekvon.bukkit.residence.webmap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceAreaAddEvent;
import com.bekvon.bukkit.residence.event.ResidenceAreaDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceOwnerChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.event.ResidenceSizeChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceSubzoneCreationEvent;

public class WebMapListeners implements Listener {

    private Residence plugin;

    public WebMapListeners(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaAdd(ResidenceAreaAddEvent event) {
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaDelete(ResidenceAreaDeleteEvent event) {
        plugin.getWebMapManager().fireUpdateRemove(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSubZoneCreate(ResidenceSubzoneCreationEvent event) {
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceFlagChange(ResidenceFlagChangeEvent event) {
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        plugin.getWebMapManager().fireUpdateRemove(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceOwnerChange(ResidenceOwnerChangeEvent event) {
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRename(ResidenceRenameEvent event) {
        plugin.getWebMapManager().handleResidenceRemove(event.getOldResidenceName(), event.getResidence());
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRent(ResidenceRentEvent event) {
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSizeChange(ResidenceSizeChangeEvent event) {
        plugin.getWebMapManager().fireUpdateAdd(event.getResidence());
    }
}
