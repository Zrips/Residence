package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public class clearflags implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 3600, regVar = {2, 3}, consoleVar = {666})
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        Player player = (Player) sender;

        if (!resadmin) {
            plugin.msg(player, lm.General_NoPermission);
            return null;
        }

        ClaimedResidence area = plugin.getResidenceManager().getByName(args[0]);
        if (area == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return null;
        }

        if (area.isRaidInitialized()) {
            plugin.msg(sender, lm.Raid_cantDo);
            return null;
        }
        area.getPermissions().clearFlags();
        plugin.msg(player, lm.Flag_Cleared);

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Remove all flags from residence");
        c.get("Info", Collections.singletonList("&eUsage: &6/res clearflags <residence>"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Collections.singletonList("[residence]"));
    }
}
