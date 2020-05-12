package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.*;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;

public class setallfor implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (args.length != 3)
            return false;

        String playerName = args[0];
        String flag = args[1];

        Flags f = Flags.getFlag(flag);
        if (f != null)
            flag = f.toString();

        FlagState state = FlagPermissions.stringToFlagState(args[2]);
        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(playerName);
        if (resPlayer == null)
            return false;
        FlagPermissions GlobalFlags = Residence.getInstance().getPermissionManager().getAllFlags();

        if (flag == null || !GlobalFlags.checkValidFlag(flag.toLowerCase(), true)) {
            plugin.msg(sender, lm.Invalid_Flag);
            return true;
        }

        if (state.equals(FlagState.INVALID)) {
            plugin.msg(sender, lm.Invalid_FlagState);
            return true;
        }

        int count = 0;

        for (ClaimedResidence one : resPlayer.getResList()) {
            if (one.getPermissions().setFlag(sender, flag, state, true, false))
                count++;
        }

        plugin.msg(sender, lm.Flag_ChangedForOne, count, resPlayer.getPlayerName());

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Set general flags on all residences owned by particular player");
        c.get("Info", Collections.singletonList("&eUsage: &6/res setallfor [playerName] [flag] [true/false/remove]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Arrays.asList("[playername]", "[flag]", "true%%false%%remove"));
    }
}
