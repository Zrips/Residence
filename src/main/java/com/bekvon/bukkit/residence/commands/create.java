package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.FileHandler.ConfigReader;

public class create implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 100)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length != 1) {
            return false;
        }

        if (plugin.getWorldEdit() != null) {
            if (plugin.getWorldEditTool() == plugin.getConfigManager().getSelectionTool()) {
                plugin.getSelectionManager().worldEdit(player);
            }
        }

        if (plugin.getSelectionManager().hasPlacedBoth(player)) {

            if (sender instanceof Player && !plugin.getPermissionManager().isResidenceAdmin(sender) && plugin.isDisabledWorldCommand((plugin.getSelectionManager().getSelection(player))
                .getWorld())) {
                lm.General_DisabledWorld.sendMessage(sender);
                return null;
            }

            Residence.getInstance().getPlayerManager().getResidencePlayer(player).forceUpdateGroup();

            plugin.getResidenceManager().addResidence(player, args[0], resadmin);
            return true;
        }
        lm.Select_Points.sendMessage(sender);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Create Residences");
        c.get("Info", Arrays.asList("&eUsage: &6/res create [residence_name]"));
        LocaleManager.addTabCompleteMain(this);
    }
}
