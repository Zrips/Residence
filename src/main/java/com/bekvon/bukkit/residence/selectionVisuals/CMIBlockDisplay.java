package com.bekvon.bukkit.residence.selectionVisuals;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.bekvon.bukkit.residence.Residence;

public class CMIBlockDisplay {

    private final BlockDisplay display;

    private static Random rand = new Random();

    public CMIBlockDisplay(@NotNull Location location, @NotNull Material material) {

        if (location == null || location.getWorld() == null)
            throw new IllegalArgumentException("Location must have a world");

        if (material == null)
            throw new IllegalArgumentException("Material cannot be null");

        this.display = location.getWorld().spawn(
                location,
                BlockDisplay.class,
                entity -> {
                    entity.setBlock(material.createBlockData());
                    entity.setVisibleByDefault(false);
                    entity.setPersistent(false);
                    entity.setBrightness(new Brightness(15, 15));
                    
                    entity.setTransformation(new Transformation(
                            new Vector3f(0, 0, 0),
                            new Quaternionf(),
                            new Vector3f(0.05f, 0.05f, 0.05f), 
                            new Quaternionf()            
                    ));                    
                });
    }

    public void setVisual(double locationX, double locationY, double locationZ, float scaleX, float scaleY, float scaleZ) {

        Location entityLocation = display.getLocation();
        Transformation transformation = display.getTransformation();

        locationX = locationX + rand.nextFloat() / 1000F;
        locationY = locationY + rand.nextFloat() / 1000F;
        locationZ = locationZ + rand.nextFloat() / 1000F;

        display.setTransformation(new Transformation(
                new Vector3f(
                        (float) (locationX - entityLocation.getX()),
                        (float) (locationY - entityLocation.getY()),
                        (float) (locationZ - entityLocation.getZ())),
                transformation.getLeftRotation(),
                new Vector3f(scaleX, scaleY, scaleZ),
                transformation.getRightRotation()));
    }

    public BlockDisplay getEntity() {
        return display;
    }

    public void setBlock(Material material) {
        display.setBlock(material.createBlockData());
    }

    public void setLocation(Location location) {
        display.teleport(location);
    }

    /**
     * Moves the display with client-side interpolation.
     *
     * @param location target location
     * @param duration number of ticks over which the movement occurs
     */
    public void move(Location location, int duration) {
        display.setTeleportDuration(duration);
        display.teleport(location);
    }

    public Location getLocation() {
        return display.getLocation();
    }

    public void setTransformation(Vector3f translation, Vector3f scale, AxisAngle4f leftRotation, AxisAngle4f rightRotation) {
        display.setTransformation(new Transformation(translation, leftRotation, scale, rightRotation));
    }

    public void setScale(float x, float y, float z) {
        Transformation transformation = display.getTransformation();
        display.setTransformation(
                new Transformation(
                        transformation.getTranslation(),
                        transformation.getLeftRotation(),
                        new Vector3f(x, y, z),
                        transformation.getRightRotation()));
    }

    public void setTranslation(float x, float y, float z) {
        Transformation transformation = display.getTransformation();

        display.setTransformation(new Transformation(
                new Vector3f(x, y, z),
                transformation.getLeftRotation(),
                transformation.getScale(),
                transformation.getRightRotation()));
    }

    public void setRotation(float pitch, float yaw, float roll) {
        Transformation transformation = display.getTransformation();

        Quaternionf rotation = new Quaternionf().rotateY((float) Math.toRadians(-yaw)).rotateX((float) Math.toRadians(pitch)).rotateZ((float) Math.toRadians(roll));

        display.setTransformation(
                new Transformation(
                        transformation.getTranslation(),
                        rotation,
                        transformation.getScale(),
                        transformation.getRightRotation()));
    }

    public void show(Player player) {
        player.showEntity(Residence.getInstance(), display);
    }

    public void hide(Player player) {
        player.hideEntity(Residence.getInstance(), display);
    }

    public void remove() {
        if (!display.isDead()) {
            display.remove();
        }
    }

    public boolean isDead() {
        return display.isDead();
    }
}