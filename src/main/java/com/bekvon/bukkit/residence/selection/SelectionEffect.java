package com.bekvon.bukkit.residence.selection;

import net.Zrips.CMILib.Effects.CMIEffect;
import net.Zrips.CMILib.Effects.CMIEffectManager.CMIParticle;

public class SelectionEffect {

    private CMIEffect frame = new CMIEffect(CMIParticle.DUST);
    private CMIEffect side = new CMIEffect(CMIParticle.DUST);

    public SelectionEffect() {
    }

    public SelectionEffect(CMIEffect frame, CMIEffect side) {
        this.setFrame(frame);
        this.setSides(side);
    }

    public CMIEffect getFrame() {
        return frame;
    }

    public void setFrame(CMIEffect frame) {
        this.frame = frame;
    }

    public CMIEffect getSides() {
        return side;
    }

    public void setSides(CMIEffect sides) {
        this.side = sides;
    }

}
