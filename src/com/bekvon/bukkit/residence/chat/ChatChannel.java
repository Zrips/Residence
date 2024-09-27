package com.bekvon.bukkit.residence.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceChatEvent;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;

public class ChatChannel {

    protected String channelName;
    protected List<String> members;
    protected String ChatPrefix = "";
    protected CMIChatColor ChannelColor = CMIChatColor.WHITE;

    public ChatChannel(String channelName, String ChatPrefix, CMIChatColor chatColor) {
	this.channelName = channelName;
	this.ChatPrefix = ChatPrefix;
	this.ChannelColor = chatColor;
	members = new ArrayList<String>();
    }

    public String getChannelName() {
	return channelName;
    }

    public void setChatPrefix(String ChatPrefix) {
	this.ChatPrefix = ChatPrefix;
    }

    public void setChannelColor(CMIChatColor ChannelColor) {
	this.ChannelColor = ChannelColor;
    }

    public void chat(String sourcePlayer, String message) {
        CMIScheduler.runTask(Residence.getInstance(), () -> {
	    Server serv = Residence.getInstance().getServ();
	    ResidenceChatEvent cevent = new ResidenceChatEvent(Residence.getInstance().getResidenceManager().getByName(channelName), serv.getPlayer(sourcePlayer), this.ChatPrefix, message,
		this.ChannelColor);
	    Residence.getInstance().getServ().getPluginManager().callEvent(cevent);
	    if (cevent.isCancelled())
		return;
	    for (String member : members) {
		Player player = serv.getPlayer(member);

		Residence.getInstance().msg(player, lm.Chat_ChatMessage, cevent.getChatprefix(), Residence.getInstance().getConfigManager().getChatColor(), sourcePlayer, cevent.getColor(), cevent
		    .getChatMessage());
	    }

	    if (Residence.getInstance().getConfigManager().isChatListening()) {
		cevent.getResidence().getPlayersInResidence().forEach((v) -> {
		    if (members.contains(v.getName()))
			return;
		    if (!cevent.getResidence().isOwner(v) && !cevent.getResidence().getPermissions().playerHas(v, Flags.chat, FlagCombo.OnlyTrue))
			return; 
		    Residence.getInstance().msg(v, lm.Chat_ChatListeningMessage, cevent.getChatprefix(), Residence.getInstance().getConfigManager().getChatColor(), sourcePlayer, cevent.getColor(), cevent
			.getChatMessage(), channelName);
		});
	    }

		
	    Bukkit.getConsoleSender().sendMessage("ResidentialChat[" + channelName + "] - " + sourcePlayer + ": " + CMIChatColor.stripColor(cevent.getChatMessage()));
	});
    }

    public void join(String player) {
	if (!members.contains(player))
	    members.add(player);
    }

    public void leave(String player) {
	members.remove(player);
    }

    public boolean hasMember(String player) {
	return members.contains(player);
    }

    public int memberCount() {
	return members.size();
    }
}
