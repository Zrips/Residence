package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.Server;

public class WorldEditSelectionManager extends SelectionManager {

    public WorldEditSelectionManager(Server serv, Residence plugin) {
        super(serv, plugin);
    }

    // TODO implement using proper API instead of Reflection, if necessary
    // Is this class really necessary?
}
