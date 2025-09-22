package com.bekvon.bukkit.residence.containers;

public class SelectionSides {
    boolean NorthSide = true;
    boolean WestSide = true;
    boolean EastSide = true;
    boolean SouthSide = true;
    boolean TopSide = true;
    boolean BottomSide = true;

    public SelectionSides() {
    }

    public SelectionSides(boolean NorthSide, boolean WestSide, boolean EastSide, boolean SouthSide, boolean TopSide, boolean BottomSide) {
	this.NorthSide = NorthSide;
	this.WestSide = WestSide;
	this.EastSide = EastSide;
	this.SouthSide = SouthSide;
	this.TopSide = TopSide;
	this.BottomSide = BottomSide;
    }

    public void setNorthSide(boolean state) {
	this.NorthSide = state;
    }

    public boolean ShowNorthSide() {
	return this.NorthSide;
    }

    public void setWestSide(boolean state) {
	this.WestSide = state;
    }

    public boolean ShowWestSide() {
	return this.WestSide;
    }

    public void setEastSide(boolean state) {
	this.EastSide = state;
    }

    public boolean ShowEastSide() {
	return this.EastSide;
    }

    public void setSouthSide(boolean state) {
	this.SouthSide = state;
    }

    public boolean ShowSouthSide() {
	return this.SouthSide;
    }

    public void setTopSide(boolean state) {
	this.TopSide = state;
    }

    public boolean ShowTopSide() {
	return this.TopSide;
    }

    public void setBottomSide(boolean state) {
	this.BottomSide = state;
    }

    public boolean ShowBottomSide() {
	return this.BottomSide;
    }
}
