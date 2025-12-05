package com.bekvon.bukkit.residence.utils;

import net.Zrips.CMILib.Items.CMIMaterial;

public class LocationCheck {
    private boolean permissionPass = true;
    private int fallDistance = 0;
    private LocationValidity validity = LocationValidity.Valid;
    private CMIMaterial damagingMaterial = null;

    public boolean isPermissionPass() {
        return permissionPass;
    }

    public void setPermissionPass(boolean permissionPass) {
        this.permissionPass = permissionPass;
    }

    public int getFallDistance() {
        return fallDistance;
    }

    public void setFallDistance(int fallDistance) {
        this.fallDistance = fallDistance;
    }

    public LocationValidity getValidity() {
        return validity;
    }

    public void setValidity(LocationValidity validity) {
        this.validity = validity;
    }

    public CMIMaterial getDamagingMaterial() {
        return damagingMaterial;
    }

    public void setDamagingMaterial(CMIMaterial damagingMaterial) {
        this.damagingMaterial = damagingMaterial;
    }
}
