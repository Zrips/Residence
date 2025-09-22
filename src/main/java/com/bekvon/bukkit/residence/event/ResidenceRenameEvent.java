package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceRenameEvent extends CancellableResidenceEvent {
    private String NewResName;
    protected String OldResName;
    protected ClaimedResidence res;

    public ResidenceRenameEvent(ClaimedResidence resref, String NewName, String OldName) {
        super("RESIDENCE_RENAME", resref);
        NewResName = NewName;
        OldResName = OldName;
        res = resref;
    }

    public String getNewResidenceName() {
        return NewResName;
    }

    public void setNewResidenceName(String newResName) {
        NewResName = newResName;
    }

    public String getOldResidenceName() {
        return OldResName;
    }

    @Override
    public ClaimedResidence getResidence() {
        return res;
    }
}
