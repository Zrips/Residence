package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.PlayerManager;

public class confirm implements cmd {

    @Override
    @CommandAnnotation(info = "Confirms removal of a residence.", usage = { "&eUsage: &6/res confirm", "Confirms removal of a residence." }, regVar = { 0 }, consoleVar = { 0 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        ClaimedResidence res = plugin.deleteConfirm.remove(PlayerManager.getSenderUUID(sender));
        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }
        plugin.getResidenceManager().removeResidence(sender instanceof Player ? (Player) sender : null, res, resadmin);
        return true;
    }

    @Override
    public void getLocale() {
        LocaleManager.addTabCompleteMain(this);
    }

}
