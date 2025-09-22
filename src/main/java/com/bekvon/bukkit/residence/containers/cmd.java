package com.bekvon.bukkit.residence.containers;

import org.bukkit.command.CommandSender;

import com.bekvon.bukkit.residence.Residence;

public interface cmd {

    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin);

    public void getLocale();

}
