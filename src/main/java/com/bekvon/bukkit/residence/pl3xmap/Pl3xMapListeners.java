package com.bekvon.bukkit.residence.pl3xmap;

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

import net.Zrips.CMILib.Logs.CMIDebug;

public class Pl3xMapListeners implements Listener {

    private Residence plugin;

    public Pl3xMapListeners(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaAdd(ResidenceAreaAddEvent event) {
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaDelete(ResidenceAreaDeleteEvent event) {
        plugin.getPl3xManager().fireUpdateRemove(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSubZoneCreate(ResidenceSubzoneCreationEvent event) {
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceFlagChange(ResidenceFlagChangeEvent event) {
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        plugin.getPl3xManager().fireUpdateRemove(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceOwnerChange(ResidenceOwnerChangeEvent event) {
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRename(ResidenceRenameEvent event) {
        plugin.getPl3xManager().handleResidenceRemove(event.getOldResidenceName(), event.getResidence(), event.getResidence().getSubzoneDeep());
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRent(ResidenceRentEvent event) {
        plugin.getPl3xManager().handleResidenceRemove(event.getResidence().getName(), event.getResidence(), event.getResidence().getSubzoneDeep());
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSizeChange(ResidenceSizeChangeEvent event) {
        plugin.getPl3xManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }
}
