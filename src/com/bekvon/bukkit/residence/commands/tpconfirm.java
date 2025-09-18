package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class tpconfirm implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length != 0) {
            return false;
        }
        if (plugin.getTeleportMap().containsKey(player.getUniqueId())) {
            plugin.getTeleportMap().get(player.getUniqueId()).getResidence().tpToResidence(player, player, resadmin);
            plugin.getTeleportMap().remove(player.getUniqueId());
        } else
            lm.General_NoTeleportConfirm.sendMessage(sender);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Ignore unsafe teleportation warning");
        c.get("Info", Arrays.asList("&eUsage: &6/res tpconfirm", "Teleports you to a residence, when teleportation is unsafe."));
    }
}
