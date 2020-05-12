package com.bekvon.bukkit.residence.containers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ABInterface {

    public void send(CommandSender sender, String msg);

    public void send(Player player, String msg);

    public void sendTitle(Player player, Object title, Object subtitle);

    void sendTitle(Player receivingPacket, Object title);
}
