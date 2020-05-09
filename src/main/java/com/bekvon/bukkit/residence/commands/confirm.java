package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class confirm implements cmd {

    @Override
    @CommandAnnotation(info = "Confirms removal of a residence.", usage = { "&eUsage: &6/res confirm", "Confirms removal of a residence." }, regVar = { 0 }, consoleVar = { 0 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
	String area = plugin.deleteConfirm.remove(sender.getName());
	if (area == null) {
	    plugin.msg(sender, lm.Invalid_Residence);
	    return true;
	}
	plugin.getResidenceManager().removeResidence(sender instanceof Player ? (Player) sender : null, area, resadmin);
	return true;
    }

    @Override
    public void getLocale() {
    }

}
