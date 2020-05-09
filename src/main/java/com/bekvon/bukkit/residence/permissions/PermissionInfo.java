package com.bekvon.bukkit.residence.permissions;

public class PermissionInfo {

    private String permission;
    private boolean enabled = false;
    private Long delay = 1000L;
    private Long lastChecked = null;
    private Double maxValue = null;
    private Double minValue = null;

    public PermissionInfo(String permission, Long delay) {
        this.permission = permission;
        if (delay != null)
            this.delay = delay;
    }

    public boolean isTimeToRecalculate() {

        return lastChecked == null || delay + lastChecked < System.currentTimeMillis();
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public Long getLastChecked() {
        if (lastChecked == null)
            lastChecked = System.currentTimeMillis();
        return lastChecked;
    }

    public void setLastChecked(long lastChecked) {
        this.lastChecked = lastChecked;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getMaxValue(Double defaultV) {
        return maxValue == null ? defaultV : maxValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMinValue(Double defaultV) {
        return minValue == null ? defaultV : minValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
