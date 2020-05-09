package com.bekvon.bukkit.residence.containers;

public class SelectionSides {
    Boolean NorthSide = true;
    Boolean WestSide = true;
    Boolean EastSide = true;
    Boolean SouthSide = true;
    Boolean TopSide = true;
    Boolean BottomSide = true;

    public SelectionSides() {
    }

    public SelectionSides(Boolean NorthSide, Boolean WestSide, Boolean EastSide, Boolean SouthSide, Boolean TopSide, Boolean BottomSide) {
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
