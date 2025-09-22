package com.bekvon.bukkit.residence.bigDoors;

import nl.pim16aap2.bigDoors.compatibility.IProtectionCompat;
import nl.pim16aap2.bigDoors.compatibility.IProtectionCompatDefinition;

public class BigDoorsDef implements IProtectionCompatDefinition {

    private final String name;

    public BigDoorsDef(String name) {
        this.name = name;
    }

    @Override
    public Class<? extends IProtectionCompat> getClass(String arg0) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

}
