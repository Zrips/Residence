package com.bekvon.bukkit.residence.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.Zrips.CMILib.Colors.CMIChatColor;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceChatEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected String message;
    CMIChatColor color = CMIChatColor.WHITE;
    private String prefix = "";

    public ResidenceChatEvent(ClaimedResidence resref, Player player, String prefix, String message, CMIChatColor color) {
        super("RESIDENCE_CHAT_EVENT", resref, player);
        this.message = message;
        this.prefix = prefix;
        this.color = color;
    }

    public String getChatMessage() {
        return message;
    }

    public String getChatprefix() {
        return CMIChatColor.translate(prefix);
    }

    public void setChatprefix(String prefix) {
        this.prefix = prefix;
    }

    public void setChatMessage(String newmessage) {
        message = newmessage;
    }

    public CMIChatColor getColor() {
        return color;
    }

    public void setColor(CMIChatColor c) {
        color = c;
    }
}
