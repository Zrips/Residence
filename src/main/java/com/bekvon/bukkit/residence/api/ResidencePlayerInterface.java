package com.bekvon.bukkit.residence.api;

import java.util.ArrayList;
import java.util.UUID;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;

/**
 * Interface for querying player-related residence data.
 * Obtain an instance via {@code ResidenceApi.getPlayerManager()}.
 */
public interface ResidencePlayerInterface {

    /**
     * Get residence list by player name.
     *
     * @param player Player name
     * @return List of residence names
     */
    public ArrayList<String> getResidenceList(String player);

    /**
     * Get residence list by player UUID.
     *
     * @param uuid Player UUID
     * @return List of residence names
     */
    ArrayList<String> getResidenceList(UUID uuid);

    /**
     * Get residence list by player name, optionally including hidden residences.
     *
     * @param player     Player name
     * @param showhidden Whether to include hidden residences
     * @return List of residence names
     */
    public ArrayList<String> getResidenceList(String player, boolean showhidden);

    /**
     * Get the residence player object for the given player name.
     *
     * @param player Player name
     * @return ResidencePlayer instance for the player
     */
    public ResidencePlayer getResidencePlayer(String player);

}
