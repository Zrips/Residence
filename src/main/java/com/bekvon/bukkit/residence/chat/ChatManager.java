package com.bekvon.bukkit.residence.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.api.ChatInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.containers.playerPersistentData;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Manages residence chat channels and player channel assignments.
 * <p>
 * Implements {@link ChatInterface}. Obtain the singleton instance via
 * {@code ResidenceApi.getChatManager()}.
 */
public class ChatManager implements ChatInterface {

    protected Map<String, ChatChannel> channelmap;
    protected Map<UUID, ChatChannel> playerChannelMap = new HashMap<UUID, ChatChannel>();

    /**
     * Creates a new ChatManager with an empty channel map.
     */
    public ChatManager() {
        channelmap = new HashMap<String, ChatChannel>();
    }

    /**
     * Set a player's chat channel by residence name.
     *
     * @param uuid    Player UUID
     * @param resName Residence name
     * @return true if channel set successfully, false if residence not found
     */
    @Override
    public boolean setChannel(UUID uuid, String resName) {
        ClaimedResidence res = ClaimedResidence.getByName(resName);
        if (res == null)
            return false;
        return setChannel(uuid, res);
    }

    /**
     * Set a player's chat channel to the given residence.
     *
     * @param uuid Player UUID
     * @param res  Target residence
     * @return true if channel set successfully, false if uuid is null
     */
    @Override
    public boolean setChannel(UUID uuid, ClaimedResidence res) {
        if (uuid == null)
            return false;
        this.removeFromChannel(uuid);
        ChatChannel channel = channelmap.computeIfAbsent(res.getName(), k -> new ChatChannel(res.getName(), res.getChatPrefix(), res.getChannelColor()));
        channel.join(uuid);
        playerChannelMap.put(uuid, channel);
        return true;
    }

    /**
     * Remove a player from their current chat channel.
     *
     * @param uuid Player UUID
     * @return true if removed successfully, false if player was not in any channel
     */
    @Override
    public boolean removeFromChannel(UUID uuid) {

        ChatChannel channel = playerChannelMap.remove(uuid);
        if (channel == null)
            return false;

        channel.leave(uuid);
        return true;
    }

    /**
     * Get a chat channel by name.
     *
     * @param channel Channel name
     * @return the ChatChannel, or null if not found
     */
    @Override
    public ChatChannel getChannel(String channel) {
        return channelmap.get(channel);
    }

    /**
     * Get the chat channel a player is currently in.
     *
     * @param uuid Player UUID
     * @return the ChatChannel the player is in, or null if not in any channel
     */
    @Override
    public ChatChannel getPlayerChannel(UUID uuid) {
        return playerChannelMap.get(uuid);
    }

    /**
     * Toggle a player's residence chat off and send a channel change message.
     *
     * @param player     the player
     * @param residence  the residence name to display in the message
     */
    public static void tooglePlayerResidenceChat(Player player, String residence) {
        playerPersistentData.get(player).setChatEnabled(false);
        lm.Chat_ChatChannelChange.sendMessage(player, residence);
    }

    /**
     * Remove a player from their residence chat channel by Player object.
     *
     * @param player the player, or null to do nothing
     */
    public static void removePlayerResidenceChat(Player player) {
        if (player == null)
            return;
        removePlayerResidenceChat(player.getUniqueId());
    }

    /**
     * Remove a player from their residence chat channel by UUID.
     *
     * @param uuid Player UUID, or null to do nothing
     */
    public static void removePlayerResidenceChat(UUID uuid) {
        if (uuid == null)
            return;
        playerPersistentData.get(uuid).setChatEnabled(true);
        lm.Chat_ChatChannelLeave.sendMessage(ResidencePlayer.getOnlinePlayer(uuid));
    }
}
