package com.bekvon.bukkit.residence.selectionVisuals;

import net.Zrips.CMILib.Items.CMIMaterial;

public enum CuboidDisplayType {
    SELECTION(
            CMIMaterial.BLUE_WOOL,
            CMIMaterial.GREEN_WOOL
    ),
    ERROR(
            CMIMaterial.RED_WOOL,
            CMIMaterial.RED_WOOL
    ),
    ENTER_EXIT(
            CMIMaterial.BLUE_WOOL,
            CMIMaterial.LIGHT_GRAY_WOOL
    ),
    BOUNCE(
            CMIMaterial.RED_WOOL,
            CMIMaterial.RED_WOOL
    );

    CuboidDisplayType(CMIMaterial edgeMaterial, CMIMaterial lineMaterial) {
        this.edgeMaterial = edgeMaterial;
        this.sideMaterial = lineMaterial;
    }

    public static CuboidDisplayType get(String s) {
        for (CuboidDisplayType t : CuboidDisplayType.values()) {
            if (t.name().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }

    CMIMaterial edgeMaterial;
    CMIMaterial sideMaterial;

    public CMIMaterial getEdgeMaterial() {
        return edgeMaterial;
    }

    public void setEdgeMaterial(CMIMaterial cornerMaterial) {
        this.edgeMaterial = cornerMaterial;
    }

    public CMIMaterial getSideMaterial() {
        return sideMaterial;
    }

    public void setSideMaterial(CMIMaterial lineMaterial) {
        this.sideMaterial = lineMaterial;
    }
}
