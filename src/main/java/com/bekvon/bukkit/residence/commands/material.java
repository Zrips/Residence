package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

public class material implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        if (args.length != 1) {
            return false;
        }

        try {
            lm.General_MaterialGet.sendMessage(sender, args[0], CMIMaterial.get(args[0]).getName());
        } catch (Exception ex) {
            lm.Invalid_Material.sendMessage(sender);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Check if material exists by its id");
        c.get("Info", Arrays.asList("&eUsage: &6/res material [material]"));
        LocaleManager.addTabCompleteMain(this, "[materialId]");
    }
}
