package com.bekvon.bukkit.residence.webmap;

import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.world.World;

public class Pl3xMapLayer extends WorldLayer {

    public static final String ID = "Residence";

    public Pl3xMapLayer(World world) {
        super(ID, world, () -> ID);

        setShowControls(true);
        setPriority(4);
        setZIndex(63);
    }
}
