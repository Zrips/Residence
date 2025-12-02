package com.bekvon.bukkit.residence.containers;

public class RandomLoc {

    double x = 0D;
    double y = 0D;
    double z = 0D;

    public RandomLoc(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public RandomLoc(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}
