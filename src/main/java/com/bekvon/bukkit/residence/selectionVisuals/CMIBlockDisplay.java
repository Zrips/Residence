package com.bekvon.bukkit.residence.selectionVisuals;

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

import net.Zrips.CMILib.Container.CMIVector3D;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class CMIBlockDisplay {

    private BlockDisplay display;
    private volatile VisualState visualState;
    private volatile boolean removed;
    private volatile boolean visible = true;

    private static class VisualState {

        private final double x, y, z;
        private final float scaleX, scaleY, scaleZ;

        private VisualState(double x, double y, double z, float scaleX, float scaleY, float scaleZ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.scaleZ = scaleZ;
        }

        private CMIVector3D getLocation() {
            return (CMIVector3D) new CMIVector3D().setZ(z).setX(x).setY(y);
        }

        private CMIVector3D getScale() {
            return (CMIVector3D) new CMIVector3D().setZ(scaleZ).setX(scaleX).setY(scaleY);
        }
    }

    public void init(@NotNull Location location, @NotNull Material material) {

        if (location.getWorld() == null)
            throw new IllegalArgumentException("Location must have a world");

        if (removed)
            return;

        display = location.getWorld().spawn(location, BlockDisplay.class, entity -> {
            entity.setBlock(material.createBlockData());
            entity.setVisibleByDefault(false);
            entity.setPersistent(false);
            entity.setBrightness(new Brightness(15, 15));
            entity.setTransformation(new Transformation(
                    new Vector3f(),
                    new Quaternionf(),
                    new Vector3f(0.05f, 0.05f, 0.05f),
                    new Quaternionf()));
        });

        if (removed) {
            display.remove();
            display = null;
            return;
        }

        VisualState state = visualState;
        if (state != null) {
            CMIScheduler.runAtEntity(Residence.getInstance(), display, () -> {
                if (display == null || display.isDead() || removed || visualState != state)
                    return;

                applyVisual(state);
            });
        }
    }

    public boolean setVisual(double x, double y, double z, float scaleX, float scaleY, float scaleZ) {

        VisualState state = new VisualState(x, y, z, scaleX, scaleY, scaleZ);
        visualState = state;

        BlockDisplay current = display;

        if (current == null)
            return false;

        CMIScheduler.runAtEntity(Residence.getInstance(), current, () -> {
            if (display != current || current.isDead() || removed || visualState != state)
                return;

            applyVisual(state);
        });
        return true;
    }

    private void applyVisual(VisualState state) {

        BlockDisplay current = display;
        if (current == null || current.isDead() || removed)
            return;

        Location location = new Location(current.getWorld(), state.x, state.y, state.z);

        if (!Version.isFolia()) {
            current.teleport(location);
            updateTransformation(current, state);
            return;
        }

        CMIScheduler.runAtEntity(Residence.getInstance(), current, () -> {
            if (display != current || current.isDead() || removed || visualState != state)
                return;

            updateTransformation(current, state);
        });

        current.teleportAsync(location);
    }

    private void updateTransformation(BlockDisplay display, VisualState state) {

        Transformation transformation = display.getTransformation();

        display.setTransformation(new Transformation(
                new Vector3f(),
                transformation.getLeftRotation(),
                new Vector3f(state.scaleX, state.scaleY, state.scaleZ),
                transformation.getRightRotation()));
    }

    public Location getLocation() {
        BlockDisplay current = display;
        return current == null ? null : current.getLocation();
    }

    public void show(Player player) {

        BlockDisplay current = display;
        if (removed || current == null)
            return;

        visible = true;

        CMIScheduler.runAtEntity(Residence.getInstance(), current, () -> {
            if (display != current || removed || !visible)
                return;

            player.showEntity(Residence.getInstance(), current);
        });
    }

    public void hide(Player player) {

        BlockDisplay current = display;
        if (current == null)
            return;

        visible = false;

        CMIScheduler.runAtEntity(Residence.getInstance(), current, () -> {
            if (display != current || visible)
                return;

            player.hideEntity(Residence.getInstance(), current);
        });
    }

    public void remove() {

        removed = true;
        visualState = null;
        visible = false;

        BlockDisplay current = display;
        if (current == null)
            return;

        CMIScheduler.runAtEntity(Residence.getInstance(), current, () -> {
            if (display != current)
                return;

            current.remove();
            display = null;
        });
    }

    public CMIVector3D getLastLocation() {
        VisualState state = visualState;
        return state == null ? new CMIVector3D() : state.getLocation();
    }

    public CMIVector3D getLastScale() {
        VisualState state = visualState;
        return state == null ? new CMIVector3D() : state.getScale();
    }
}