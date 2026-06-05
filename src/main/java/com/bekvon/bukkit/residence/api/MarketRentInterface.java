package com.bekvon.bukkit.residence.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Interface for the residence rent market system.
 * Obtain an instance via {@link ResidenceApi#getMarketRentManager()}.
 */
public interface MarketRentInterface {
    /**
     * Gets all residences currently listed as rentable.
     *
     * @return set of rentable claimed residences
     */
    public Set<ClaimedResidence> getRentableResidences();

    /**
     * Gets all residences that are currently rented by someone.
     *
     * @return set of currently rented claimed residences
     */
    public Set<ClaimedResidence> getCurrentlyRentedResidences();

    /**
     * Gets the rental information for a specific residence.
     *
     * @param landName residence name
     * @return the rented land data, or null if not rented
     */
    public RentedLand getRentedLand(String landName);

    /**
     * Gets a list of residence names that a player is currently renting.
     *
     * @param playerName player name
     * @return list of rented residence names
     */
    public List<String> getRentedLands(String playerName);

    /**
     * Lists a residence for rent with default settings.
     *
     * @param player       residence owner
     * @param landName     residence name
     * @param amount       rent cost per period
     * @param days         rent duration in days
     * @param AllowRenewing whether tenants can renew their rent
     * @param resadmin     whether in admin mode
     */
    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean resadmin);

    /**
     * Lists a residence for rent with a stay-in-market option.
     *
     * @param player       residence owner
     * @param landName     residence name
     * @param amount       rent cost per period
     * @param days         rent duration in days
     * @param AllowRenewing whether tenants can renew their rent
     * @param StayInMarket whether the residence stays on the rental market after being rented
     * @param resadmin     whether in admin mode
     */
    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean resadmin);

    /**
     * Lists a residence for rent with full options.
     *
     * @param player       residence owner
     * @param landName     residence name
     * @param amount       rent cost per period
     * @param days         rent duration in days
     * @param AllowRenewing whether tenants can renew their rent
     * @param StayInMarket whether the residence stays on the rental market after being rented
     * @param AllowAutoPay whether automatic payment renewal is allowed
     * @param resadmin     whether in admin mode
     */
    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean AllowAutoPay, boolean resadmin);

    /**
     * Rents a residence for a player.
     *
     * @param player   the renting player
     * @param landName residence name
     * @param repeat   whether to auto-repeat the rent when it expires
     * @param resadmin whether in admin mode
     */
    public void rent(Player player, String landName, boolean repeat, boolean resadmin);

    /**
     * Removes a residence from the rental market (owner cancels listing).
     *
     * @param player   the residence owner
     * @param landName residence name
     * @param resadmin whether in admin mode
     */
    public void removeFromForRent(Player player, String landName, boolean resadmin);

    /**
     * Unrents a residence, stopping the tenant's rental.
     *
     * @param player   the renting player
     * @param landName residence name
     * @param resadmin whether in admin mode
     */
    public void unrent(Player player, String landName, boolean resadmin);

    /**
     * Removes a rental record for a residence by name.
     *
     * @param landName residence name
     */
    public void removeFromRent(String landName);

    /**
     * Removes a residence from the rentable market.
     *
     * @param landName residence name
     */
    public void removeRentable(String landName);

    /**
     * Checks whether a residence is listed for rent.
     *
     * @param landName residence name
     * @return true if the residence is listed for rent
     */
    public boolean isForRent(String landName);

    /**
     * Checks whether a residence is currently rented by someone.
     *
     * @param landName residence name
     * @return true if the residence is currently rented
     */
    public boolean isRented(String landName);

    /**
     * Gets the name of the player currently renting a residence.
     *
     * @param landName residence name
     * @return the renting player's name, or null if not rented
     */
    public String getRentingPlayer(String landName);

    /**
     * Gets the rent cost per period for a residence.
     *
     * @param landName residence name
     * @return the rent cost
     */
    public int getCostOfRent(String landName);

    /**
     * Checks whether a rentable residence allows repeat renting.
     *
     * @param landName residence name
     * @return true if repeat renting is allowed
     */
    public boolean getRentableRepeatable(String landName);

    /**
     * Checks whether a rented residence is set to auto-repeat.
     *
     * @param landName residence name
     * @return true if the rent auto-repeats
     */
    public boolean getRentedAutoRepeats(String landName);

    /**
     * Gets the rent duration in days for a residence.
     *
     * @param landName residence name
     * @return the number of rent days
     */
    public int getRentDays(String landName);

    /**
     * Checks all current rents and expires any that have passed their duration.
     */
    public void checkCurrentRents();

    /**
     * Sets whether a rentable residence allows repeat renting.
     *
     * @param player   the residence owner
     * @param landName residence name
     * @param value    true to allow repeat renting
     * @param resadmin whether in admin mode
     */
    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin);

    /**
     * Sets whether a rented residence should auto-repeat on expiration.
     *
     * @param player   the renting player
     * @param landName residence name
     * @param value    true to enable auto-repeat
     * @param resadmin whether in admin mode
     */
    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin);

    /**
     * Gets the number of residences a player is currently renting.
     *
     * @param player player name
     * @return the number of rented residences
     * @deprecated Use {@link #getRentCount(UUID)} instead.
     */
    @Deprecated
    public int getRentCount(String player);

    /**
     * Gets the number of rentable residences a player has listed.
     *
     * @param player player name
     * @return the number of rentable residences
     * @deprecated Use {@link #getRentableCount(UUID)} instead.
     */
    @Deprecated
    public int getRentableCount(String player);

    /**
     * Gets the number of rentable residences a player has listed.
     *
     * @param playerUUID player UUID
     * @return the number of rentable residences
     */
    public int getRentableCount(UUID playerUUID);

    /**
     * Gets the number of residences a player is currently renting.
     *
     * @param playerUUID player UUID
     * @return the number of rented residences
     */
    public int getRentCount(UUID playerUUID);
}
