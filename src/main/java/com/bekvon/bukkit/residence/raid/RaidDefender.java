package com.bekvon.bukkit.residence.raid;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;

public class RaidDefender {
    private ResidencePlayer rPlayer = null;
    private int blocksBroken = 0;
    private int blocksPlaced = 0;

    public RaidDefender(ResidencePlayer rPlayer) {
        this.rPlayer = rPlayer;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(int blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
    }

    public ResidencePlayer getPlayer() {
        return rPlayer;
    }
}
