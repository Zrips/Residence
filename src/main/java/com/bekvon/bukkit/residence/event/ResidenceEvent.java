package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidenceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String message;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    ClaimedResidence res;

    public ResidenceEvent(String eventName, ClaimedResidence resref) {
        this(false, eventName, resref);
    }

    public ResidenceEvent(boolean async, String eventName, ClaimedResidence resref) {
        super(async);
        message = eventName;
        res = resref;
    }

    public String getMessage() {
        return message;
    }

    public ClaimedResidence getResidence() {
        return res;
    }

    public static void call(Event event) {
        if (event.isAsynchronous())
            callAsync(event);
        else
            callSync(event);
    }

    public static void callSync(Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static CompletableFuture<Void> callAsync(Event event) {
        return CMIScheduler.runTaskAsynchronously(Residence.getInstance(), () -> Bukkit.getServer().getPluginManager().callEvent(event));
    }
}
