package com.bekvon.bukkit.residence.signsStuff;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;

public class Signs {

    ClaimedResidence Residence = null;
    Location loc = null;

    public Signs() {
    }

    @Deprecated
    public Location GetLocation() {
        return this.loc;
    }

    public Location getLocation() {
        return this.loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public ClaimedResidence getResidence() {
        return this.Residence;
    }

    public void setResidence(ClaimedResidence Residence) {
        this.Residence = Residence;
        if (Residence != null)
            Residence.getSignsInResidence().add(this);
    }

    @Deprecated
    public ClaimedResidence GetResidence() {
        return this.Residence;
    }

}
