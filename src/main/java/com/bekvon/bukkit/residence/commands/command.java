package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class command implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3000, regVar = { -100 }, consoleVar = { -100, -1 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        String cmd = args[args.length - 1];
        if (!cmd.equalsIgnoreCase("list"))
            args = Arrays.copyOfRange(args, 0, args.length - 1);
        ClaimedResidence res = null;
        String action = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
        if (args.length > 0)
            args = Arrays.copyOfRange(args, 0, args.length - 1);
        if (args.length > 0)
            res = plugin.getResidenceManager().getByName(args[0]);

        if (res == null && sender instanceof Player)
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return null;
        }

        if (!res.isOwner(sender) && !resadmin) {
            lm.Residence_NotOwner.sendMessage(sender);
            return null;
        }

        switch (action) {
        case "allow":
            if (res.addCmdWhiteList(cmd)) {
                lm.command_addedAllow.sendMessage(sender, res.getName());
            } else
                lm.command_removedAllow.sendMessage(sender, res.getName());
            return true;
        case "block":
            if (res.addCmdBlackList(cmd)) {
                lm.command_addedBlock.sendMessage(sender, res.getName());
            } else
                lm.command_removedBlock.sendMessage(sender, res.getName());
            return true;
        case "list":
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < res.getCmdBlackList().size(); i++) {
                sb.append("/" + res.getCmdBlackList().get(i).replace("_", " "));
                if (i + 1 < res.getCmdBlackList().size())
                    sb.append(", ");
            }
            lm.command_Blocked.sendMessage(sender, sb.toString());

            sb = new StringBuilder();
            for (int i = 0; i < res.getCmdWhiteList().size(); i++) {
                sb.append("/" + res.getCmdWhiteList().get(i).replace("_", " "));
                if (i + 1 < res.getCmdWhiteList().size())
                    sb.append(", ");
            }
            lm.command_Allowed.sendMessage(sender, sb.toString());
            return true;
        }

        return false;

    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manages allowed or blocked commands in residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res command <residence> <allow/block/list> <command>",
            "Shows list, adds or removes allowed or disabled commands in residence",
            "Use _ to include command with multiple variables"));
        LocaleManager.addTabCompleteMain(this, "[residence]%%allow%%block%%list", "allow%%block%%list");
    }
}
