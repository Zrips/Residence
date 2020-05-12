package com.bekvon.bukkit.residence.api;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public interface ChatInterface {
    public boolean setChannel(String player, String resName);

    public boolean setChannel(String player, ClaimedResidence res);

    public boolean removeFromChannel(String player);

    public ChatChannel getChannel(String channel);

    public ChatChannel getPlayerChannel(String player);

}
