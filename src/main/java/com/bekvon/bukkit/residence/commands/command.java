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

public class command implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3000, regVar = {-100}, consoleVar = {-100, -1})
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
            plugin.msg(sender, lm.Invalid_Residence);
            return null;
        }

        if (!res.isOwner(sender) && !resadmin) {
            plugin.msg(sender, lm.Residence_NotOwner);
            return null;
        }

        switch (action) {
            case "allow":
                if (res.addCmdWhiteList(cmd)) {
                    plugin.msg(sender, lm.command_addedAllow, res.getName());
                } else
                    plugin.msg(sender, lm.command_removedAllow, res.getName());
                return true;
            case "block":
                if (res.addCmdBlackList(cmd)) {
                    plugin.msg(sender, lm.command_addedBlock, res.getName());
                } else
                    plugin.msg(sender, lm.command_removedBlock, res.getName());
                return true;
            case "list":
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < res.getCmdBlackList().size(); i++) {
                    sb.append("/").append(res.getCmdBlackList().get(i).replace("_", " "));
                    if (i + 1 < res.getCmdBlackList().size())
                        sb.append(", ");
                }
                plugin.msg(sender, lm.command_Blocked, sb.toString());

                sb = new StringBuilder();
                for (int i = 0; i < res.getCmdWhiteList().size(); i++) {
                    sb.append("/").append(res.getCmdWhiteList().get(i).replace("_", " "));
                    if (i + 1 < res.getCmdWhiteList().size())
                        sb.append(", ");
                }
                plugin.msg(sender, lm.command_Allowed, sb.toString());
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
        Residence.getInstance().getLocaleManager().CommandTab.put(Collections.singletonList(this.getClass().getSimpleName()), Arrays.asList("[residence]%%allow%%block%%list",
                "allow%%block%%list"));
    }
}
