package com.bekvon.bukkit.residence.chat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bekvon.bukkit.residence.api.ChatInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ChatManager implements ChatInterface {

    protected Map<String, ChatChannel> channelmap;
    protected Map<UUID, ChatChannel> playerChannelMap = new HashMap<UUID, ChatChannel>();

    public ChatManager() {
        channelmap = new HashMap<String, ChatChannel>();
    }

    @Override
    public boolean setChannel(UUID uuid, String resName) {
        ClaimedResidence res = ClaimedResidence.getByName(resName);
        if (res == null)
            return false;
        return setChannel(uuid, res);
    }

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

    @Override
    public boolean removeFromChannel(UUID uuid) {

        ChatChannel channel = playerChannelMap.remove(uuid);
        if (channel == null)
            return false;

        channel.leave(uuid);
        return true;
    }

    @Override
    public ChatChannel getChannel(String channel) {
        return channelmap.get(channel);
    }

    @Override
    public ChatChannel getPlayerChannel(UUID uuid) {
        return playerChannelMap.get(uuid);
    }
}
