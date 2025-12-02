package com.bekvon.bukkit.residence.permissions;

import java.util.HashSet;
import java.util.Set;

public class PermissionInfo {

    private String permission;
    private boolean enabled = false;
    private Long delay = 1000L;
    private Long lastChecked = null;
    private Double maxValue = null;
    private Double minValue = null;

    private Set<String> values = new HashSet<String>();

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

    public Double getMaxValue(double defaultV) {
        return maxValue == null ? defaultV : maxValue > defaultV ? maxValue : defaultV;
    }

    public int getMaxValue(int defaultV) {
        return maxValue == null ? defaultV : maxValue > defaultV ? maxValue.intValue() : defaultV;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMinValue(double defaultV) {
        return minValue == null ? defaultV : minValue < defaultV ? minValue : defaultV;
    }

    public int getMinValue(int defaultV) {
        return minValue == null ? defaultV : minValue < defaultV ? minValue.intValue() : defaultV;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getValues() {
        return values;
    }

    public void addValue(String value) {
        this.values.add(value);
    }

}
