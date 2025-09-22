package com.bekvon.bukkit.residence.utils;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class SafeLocationCache {

    private ClaimedResidence res;
    private long time = 0;
    private LocationCheck validity = new LocationCheck();

    public SafeLocationCache(ClaimedResidence res) {
        this.res = res;
        setTime(System.currentTimeMillis());
    }

    public ClaimedResidence getResidence() {
        return res;
    }

    public void setResidence(ClaimedResidence res) {
        this.res = res;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public LocationCheck getValidity() {
        return validity;
    }

    public void setValidity(LocationCheck validity) {
        this.validity = validity;
    }

}
