package com.bekvon.bukkit.residence.webmap;

import java.util.ArrayList;
import java.util.List;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.CMIList;

class MapSettings {
    private boolean hideByDefault;

    private boolean showFlags;
    private boolean excludeDefaultFlags;
    private boolean hideHidden;
    private int layerSubZoneDepth;
    private int borderWeight;
    private CMIChatColor borderColor = null;
    private CMIChatColor fillColor = null;
    private CMIChatColor fillForRent = null;
    private CMIChatColor fillRented = null;
    private CMIChatColor fillForSale = null;
    private CMIChatColor fillServerLand = null;
    private List<String> visibleRegions;
    private List<String> hiddenRegions;
    private List<String> hiddenPlayerResidences = new ArrayList<String>();
    private int y;

    public boolean isHideByDefault() {
        return hideByDefault;
    }

    public MapSettings setHideByDefault(boolean hideByDefault) {
        this.hideByDefault = hideByDefault;
        return this;
    }

    public boolean isShowFlags() {
        return showFlags;
    }

    public MapSettings setShowFlags(boolean showFlags) {
        this.showFlags = showFlags;
        return this;
    }

    public boolean isExcludeDefaultFlags() {
        return excludeDefaultFlags;
    }

    public MapSettings setExcludeDefaultFlags(boolean excludeDefaultFlags) {
        this.excludeDefaultFlags = excludeDefaultFlags;
        return this;
    }

    public boolean isHideHidden() {
        return hideHidden;
    }

    public MapSettings setHideHidden(boolean hideHidden) {
        this.hideHidden = hideHidden;
        return this;
    }

    public int getLayerSubZoneDepth() {
        return layerSubZoneDepth;
    }

    public MapSettings setLayerSubZoneDepth(int layerSubZoneDepth) {
        this.layerSubZoneDepth = layerSubZoneDepth;
        return this;
    }

    public int getBorderWeight() {
        return borderWeight;
    }

    public MapSettings setBorderWeight(int borderWeight) {
        this.borderWeight = borderWeight;
        return this;
    }

    public List<String> getVisibleRegions() {
        return visibleRegions;
    }

    public MapSettings setVisibleRegions(List<String> visibleRegions) {
        this.visibleRegions = visibleRegions;
        return this;
    }

    public List<String> getHiddenRegions() {
        return hiddenRegions;
    }

    public MapSettings setHiddenRegions(List<String> hiddenRegions) {
        this.hiddenRegions = hiddenRegions;
        return this;
    }

    public List<String> getHiddenPlayerResidences() {
        return hiddenPlayerResidences;
    }

    public MapSettings setHiddenPlayerResidences(List<String> hiddenPlayerResidences) {
        this.hiddenPlayerResidences = hiddenPlayerResidences;
        CMIList.toLowerCase(this.hiddenPlayerResidences);
        return this;
    }

    public int getY() {
        return y;
    }

    public MapSettings setY(int y) {
        this.y = y;
        return this;
    }

    public CMIChatColor getBorderColor() {
        return borderColor;
    }

    public MapSettings setBorderColor(CMIChatColor borderColor) {
        this.borderColor = borderColor == null ? CMIChatColor.getColor("white") : borderColor;
        return this;
    }

    public CMIChatColor getFillColor() {
        return fillColor;
    }

    public MapSettings setFillColor(CMIChatColor fillColor) {
        this.fillColor = fillColor == null ? CMIChatColor.getColor("white") : fillColor;
        return this;
    }

    public CMIChatColor getFillForRent() {
        return fillForRent;
    }

    public MapSettings setFillForRent(CMIChatColor fillForRent) {
        this.fillForRent = fillForRent == null ? CMIChatColor.getColor("white") : fillForRent;
        return this;
    }

    public CMIChatColor getFillRented() {
        return fillRented;
    }

    public MapSettings setFillRented(CMIChatColor fillRented) {
        this.fillRented = fillRented == null ? CMIChatColor.getColor("white") : fillRented;
        return this;
    }

    public CMIChatColor getFillForSale() {
        return fillForSale;
    }

    public MapSettings setFillForSale(CMIChatColor fillForSale) {
        this.fillForSale = fillForSale == null ? CMIChatColor.getColor("white") : fillForSale;
        return this;
    }

    public CMIChatColor getFillServerLand() {
        return fillServerLand;
    }

    public void setFillServerLand(CMIChatColor fillServerLand) {
        this.fillServerLand = fillServerLand == null ? CMIChatColor.getColor("white") : fillServerLand;
    }

}
