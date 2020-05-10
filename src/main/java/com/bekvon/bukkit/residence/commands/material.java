package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class material implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length != 1) {
            return false;
        }
        try {
            plugin.msg(player, lm.General_MaterialGet, args[0], CMIMaterial.get(Integer.parseInt(args[0])).getName());
        } catch (Exception ex) {
            plugin.msg(player, lm.Invalid_Material);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Check if material exists by its id");
        c.get("Info", Collections.singletonList("&eUsage: &6/res material [material]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[materialId]"));
    }
}
