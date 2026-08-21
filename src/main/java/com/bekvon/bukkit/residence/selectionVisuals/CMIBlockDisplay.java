package com.bekvon.bukkit.residence.selectionVisuals;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.bekvon.bukkit.residence.Residence;

import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class CMIBlockDisplay {

    private BlockDisplay display = null;
    private boolean removed = false;
    private VisualState pendingVisual;

    private static Random rand = new Random();

    public CMIBlockDisplay() {
    }

    public void init(@NotNull Location location, @NotNull Material material) {

        if (location.getWorld() == null)
            throw new IllegalArgumentException("Location must have a world");

        if (removed)
            return;

        this.display = location.getWorld().spawn(location, BlockDisplay.class, entity -> {

            entity.setBlock(material.createBlockData());
            entity.setVisibleByDefault(false);
            entity.setPersistent(false);
            entity.setBrightness(new Brightness(15, 15));

            entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new Quaternionf(),
                    new Vector3f(0.05f, 0.05f, 0.05f),
                    new Quaternionf()));
        });

        if (removed) {
            display.remove();
            display = null;
            return;
        }

        if (pendingVisual != null) {
            applyVisual(pendingVisual);
            pendingVisual = null;
        }
    }

    private static class VisualState {

        private final double x;
        private final double y;
        private final double z;

        private final float scaleX;
        private final float scaleY;
        private final float scaleZ;

        private VisualState(double x, double y, double z, float scaleX, float scaleY, float scaleZ) {

            this.x = x;
            this.y = y;
            this.z = z;

            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
        }
    }

    public void setVisual(double lX, double lY, double lZ, float scaleX, float scaleY, float scaleZ) {

        VisualState visual = new VisualState(lX, lY, lZ, scaleX, scaleY, scaleZ);

        pendingVisual = visual;

        if (display == null)
            return;

        CMIScheduler.runAtLocation(Residence.getInstance(), display.getLocation(), () -> {

            if (display == null || display.isDead())
                return;

            if (pendingVisual != visual)
                return;

            applyVisual(visual);
        });
    }

    private void applyVisual(VisualState visual) {

        if (display == null || display.isDead())
            return;

        double locationX = visual.x + visual.scaleX / 2.0 + rand.nextFloat() / 1000F;
        double locationY = visual.y + visual.scaleY / 2.0 + rand.nextFloat() / 1000F;
        double locationZ = visual.z + visual.scaleZ / 2.0 + rand.nextFloat() / 1000F;

        Location newLocation = display.getLocation();
        newLocation.setX(locationX);
        newLocation.setY(locationY);
        newLocation.setZ(locationZ);

        PaperLib.teleportAsync(display, newLocation);

        Transformation transformation = display.getTransformation();

        display.setTransformation(new Transformation(
                new Vector3f(
                        (float) (-visual.scaleX / 2.0),
                        (float) (-visual.scaleY / 2.0),
                        (float) (-visual.scaleZ / 2.0)),
                transformation.getLeftRotation(),
                new Vector3f(
                        visual.scaleX,
                        visual.scaleY,
                        visual.scaleZ),
                transformation.getRightRotation()));
    }

    public Location getLocation() {
        if (display == null)
            return null;
        return display.getLocation();
    }

    public void show(Player player) {

        if (removed || display == null)
            return;

        CMIScheduler.runAtLocation(Residence.getInstance(), display.getLocation(), () -> {
            player.showEntity(Residence.getInstance(), display);
        });
    }

    public void hide(Player player) {
        if (display == null)
            return;
        CMIScheduler.runAtLocation(Residence.getInstance(), display.getLocation(), () -> {

            if (display == null)
                return;

            player.hideEntity(Residence.getInstance(), display);
        });
    }

    public void remove() {

        removed = true;
        pendingVisual = null;

        if (display == null)
            return;

        CMIScheduler.runAtLocation(Residence.getInstance(), display.getLocation(), () -> {

            if (display == null)
                return;

            display.remove();
            display = null;
        });
    }
}