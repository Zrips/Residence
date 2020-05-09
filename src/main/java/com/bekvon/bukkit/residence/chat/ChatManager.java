package com.bekvon.bukkit.residence.chat;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ChatInterface;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Server;

import java.util.HashMap;
import java.util.Map;

public class ChatManager implements ChatInterface {

    protected Map<String, ChatChannel> channelmap;
    protected Server server;

    public ChatManager() {
        server = Residence.getInstance().getServ();
        channelmap = new HashMap<String, ChatChannel>();
    }

    @Override
    public boolean setChannel(String player, String resName) {
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(resName);
        if (res == null)
            return false;
        return setChannel(player, res);
    }

    @Override
    public boolean setChannel(String player, ClaimedResidence res) {
        this.removeFromChannel(player);
        if (!channelmap.containsKey(res.getName()))
            channelmap.put(res.getName(), new ChatChannel(res.getName(), res.getChatPrefix(), res.getChannelColor()));
        channelmap.get(res.getName()).join(player);
        return true;
    }

    @Override
    public boolean removeFromChannel(String player) {
        for (ChatChannel chan : channelmap.values()) {
            if (chan.hasMember(player)) {
                chan.leave(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public ChatChannel getChannel(String channel) {
        return channelmap.get(channel);
    }

    @Override
    public ChatChannel getPlayerChannel(String player) {
        for (ChatChannel chan : channelmap.values()) {
            if (chan.hasMember(player))
                return chan;
        }
        return null;
    }
}
