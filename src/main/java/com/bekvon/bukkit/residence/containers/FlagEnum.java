package com.bekvon.bukkit.residence.containers;

import com.bekvon.bukkit.residence.permissions.PermissionGroup;

public class FlagEnum {
    private PermissionGroup group;
    private long time;

    public FlagEnum(PermissionGroup group, long time) {
        this.group = group;
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    public PermissionGroup getGroup() {
        return this.group;
    }
}
