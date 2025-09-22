package com.bekvon.bukkit.residence.api;

import java.util.UUID;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public interface ChatInterface {
    @Deprecated
    public boolean setChannel(UUID player, String resName);

    public boolean setChannel(UUID player, ClaimedResidence res);

    public boolean removeFromChannel(UUID player);

    public ChatChannel getChannel(String channel);

    public ChatChannel getPlayerChannel(UUID player);

}
