package com.bekvon.bukkit.residence.selectionVisuals;

import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Items.CMIMaterial;

public enum CuboidDisplayType {
    SELECTION(
            CMIMaterial.BLUE_WOOL,
            CMIMaterial.GREEN_WOOL,
            30.0,
			0.05,
			32,
			8
    ),
    ERROR(
            CMIMaterial.RED_WOOL,
            CMIMaterial.RED_WOOL,
            30.0,
            0.05,
            32,
            8
    ),
    ENTER_EXIT(
            CMIMaterial.BLUE_WOOL,
            CMIMaterial.LIGHT_GRAY_WOOL,
            1,
            0.025,
            4,
            0.5
    ),
    BOUNCE(
            CMIMaterial.RED_WOOL,
            CMIMaterial.RED_WOOL,
            1,
            0.025,
            4,
            0.5
    );

    CuboidDisplayType(CMIMaterial edgeMaterial, CMIMaterial lineMaterial, double hideInSeconds, double lineThickness, int range, double gridSize) {
        this.edgeMaterial = edgeMaterial;
        this.sideMaterial = lineMaterial;
		this.hideInSeconds = hideInSeconds;
		this.lineThickness = lineThickness;
		this.range = range;
		this.gridSize = gridSize;
    }

    public static CuboidDisplayType get(String s) {
        for (CuboidDisplayType t : CuboidDisplayType.values()) {
            if (t.name().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }

    private CMIMaterial edgeMaterial;
    private CMIMaterial sideMaterial;
    private double hideInSeconds = 5.0;
    private double lineThickness = 0.05;
    private int range = 16;
    private double gridSize = 8;

    public CMIMaterial getEdgeMaterial() {
        return edgeMaterial;
    }

    public void setEdgeMaterial(CMIMaterial cornerMaterial) {
        this.edgeMaterial = cornerMaterial == null ? this.edgeMaterial : cornerMaterial;;
    }

    public CMIMaterial getSideMaterial() {
        return sideMaterial;
    }

    public void setSideMaterial(CMIMaterial lineMaterial) {
        this.sideMaterial = lineMaterial == null ? this.sideMaterial : lineMaterial;
    }

    public double getHideInSeconds() {
        return hideInSeconds;
    }

    public void setHideInSeconds(double hideInSeconds) {
        this.hideInSeconds = CMINumber.clamp(hideInSeconds, 0.1, 300);
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = CMINumber.clamp(range, 1, 128);
    }

    public double getGridSize() {
        return gridSize;
    }

    public void setGridSize(double gridSize) {
        this.gridSize = CMINumber.clamp(gridSize, 0.25, 64);
    }

    public double getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(double lineThickness) {
        this.lineThickness = CMINumber.clamp(lineThickness, 0.01, 1);
    }
}
