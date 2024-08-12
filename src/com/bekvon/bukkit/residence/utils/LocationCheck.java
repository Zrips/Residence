package com.bekvon.bukkit.residence.utils;

public class LocationCheck {
    private boolean permissionPass = true;
    private int fallDistance = 0;
    private LocationValidity validity = LocationValidity.Valid;

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
}
