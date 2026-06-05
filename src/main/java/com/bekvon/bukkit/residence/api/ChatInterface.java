package com.bekvon.bukkit.residence.api;

import java.util.UUID;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Interface for managing residence chat channels.
 * Obtain an instance via {@link com.bekvon.bukkit.residence.api.ResidenceApi#getChatManager()}.
 */
public interface ChatInterface {
    /**
     * Set a player's chat channel by residence name.
     *
     * @param player  Player UUID
     * @param resName Residence name
     * @return true if channel set successfully
     * @deprecated Use {@link #setChannel(UUID, ClaimedResidence)} instead.
     */
    @Deprecated
    public boolean setChannel(UUID player, String resName);

    /**
     * Set a player's chat channel for a residence.
     *
     * @param player Player UUID
     * @param res    Target residence
     * @return true if channel set successfully
     */
    public boolean setChannel(UUID player, ClaimedResidence res);

    /**
     * Remove a player from their current chat channel.
     *
     * @param player Player UUID
     * @return true if removed successfully
     */
    public boolean removeFromChannel(UUID player);

    /**
     * Get a chat channel by name.
     *
     * @param channel Channel name
     * @return the ChatChannel instance, or null if not found
     */
    public ChatChannel getChannel(String channel);

    /**
     * Get the chat channel a player is currently in.
     *
     * @param player Player UUID
     * @return the player's current ChatChannel, or null if not in any channel
     */
    public ChatChannel getPlayerChannel(UUID player);

}
