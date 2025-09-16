package com.bekvon.bukkit.residence.pl3xmap;

import com.bekvon.bukkit.residence.Residence;

import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.world.World;

public class Pl3xMapLayer extends WorldLayer {

    public static final String ID = "Residence";

    public Pl3xMapLayer(World world) {
        super(ID, world, () -> ID);

        setShowControls(true);
        setDefaultHidden(Residence.getInstance().getConfigManager().Pl3xMapHideByDefault);
        setPriority(4);
        setZIndex(63);
    }
}
