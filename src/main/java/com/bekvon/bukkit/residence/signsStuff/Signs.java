package com.bekvon.bukkit.residence.signsStuff;

import org.bukkit.Location;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class Signs {

    ClaimedResidence Residence = null;
    Location loc = null;

    public Signs() {
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Deprecated
    public Location GetLocation() {
        return getLocation();
    }

    public Location getLocation() {
        return this.loc;
    }

    public void setResidence(ClaimedResidence Residence) {
        this.Residence = Residence;
        if (Residence != null)
            Residence.getSignsInResidence().add(this);
    }

    public ClaimedResidence getResidence() {
        return this.Residence;
    }

    @Deprecated
    public ClaimedResidence GetResidence() {
        return getResidence();
    }

}
