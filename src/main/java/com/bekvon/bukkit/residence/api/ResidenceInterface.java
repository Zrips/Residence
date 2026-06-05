package com.bekvon.bukkit.residence.api;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Residence management API for querying residences, managing shop lists, and creating residences.
 * Get an instance via {@link ResidenceApi#getResidenceManager()}.
 */
public interface ResidenceInterface {
    /**
     * Get residence at a location. Returns sub-residence if inside one, otherwise the main residence.
     *
     * @param loc Bukkit location
     * @return ClaimedResidence or null if not found
     */
    public ClaimedResidence getByLoc(Location loc);

    /**
     * Get residence by name. Case-insensitive. Use dot notation for sub-residences (e.g. {@code main.sub}).
     *
     * @param name residence name or sub-residence path
     * @return ClaimedResidence or null if not found
     */
    public ClaimedResidence getByName(String name);

    /**
     * Add a residence to the shop cache list (in-memory only).
     *
     * @param res residence to add
     */
    public void addShop(ClaimedResidence res);

    /**
     * Add a residence to the shop cache list by name.
     *
     * @param res residence name
     * @deprecated use {@link #addShop(ClaimedResidence)} instead
     */
    @Deprecated
    public void addShop(String res);

    /**
     * Remove a residence from the shop cache list (in-memory only).
     *
     * @param res residence to remove
     */
    public void removeShop(ClaimedResidence res);

    /**
     * Remove a residence from the shop cache list by name. Case-insensitive.
     *
     * @param res residence name
     * @deprecated use {@link #removeShop(ClaimedResidence)} instead
     */
    @Deprecated
    public void removeShop(String res);

    /**
     * Get the cached shop residence list. Do not modify the returned list directly.
     *
     * @return list of shop residences
     */
    public List<ClaimedResidence> getShops();

    /**
     * Create a residence as a server residence (no player, no economy charge).
     *
     * @param name residence name
     * @param loc1 first corner
     * @param loc2 second corner
     * @return true if created successfully
     * @deprecated use the overload with {@link Player} for proper permissions and feedback
     */
    @Deprecated
    public boolean addResidence(String name, Location loc1, Location loc2);

    /**
     * Create a residence with a specified owner name (no player, no economy charge).
     *
     * @param name residence name
     * @param owner owner name, or null for server residence
     * @param loc1 first corner
     * @param loc2 second corner
     * @return true if created successfully
     * @deprecated use the overload with {@link Player} for proper permissions and feedback
     */
    @Deprecated
    public boolean addResidence(String name, String owner, Location loc1, Location loc2);

    /**
     * Create a residence for a player using two corner locations.
     *
     * @param player residence owner
     * @param name residence name
     * @param loc1 first corner
     * @param loc2 second corner
     * @param resadmin true to skip player limits and economy charges
     * @return true if created successfully
     */
    public boolean addResidence(Player player, String name, Location loc1, Location loc2, boolean resadmin);

    /**
     * Create a residence using the player's current selection.
     *
     * @param player residence owner
     * @param name residence name
     * @param resadmin true to skip player limits and economy charges
     * @return true if created successfully
     */
    public boolean addResidence(Player player, String name, boolean resadmin);
}
