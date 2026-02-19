package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.RawMessages.RawMessageCommand;

public class restore implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        ClaimedResidence res = null;
        int index = -1;

        for (String one : args) {
            if (res == null) {
                res = plugin.getResidenceManager().getByName(one);
            }
            if (index <= -1) {
                try {
                    index = Integer.parseInt(one);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (res == null && sender instanceof Player) {
            res = plugin.getResidenceManager().getByLoc(((Player) sender).getLocation());
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(sender);
            return true;
        }

        if (index <= -1) {
            listResidenceForLoad(res, sender);
            return true;
        }

        boolean restored = plugin.getSchematicManager().load(res, index);

        if (restored) {
            lm.Schematic_Restored.sendMessage(sender);
        } else {
            lm.Schematic_Failed.sendMessage(sender);
        }

        return true;
    }

    private void listResidenceForLoad(ClaimedResidence res, CommandSender sender) {

        List<String> list = Residence.getInstance().getSchematicManager().getList(res);
        if (list.isEmpty()) {
            lm.Schematic_NoSchematic.sendMessage(sender);
            return;
        }

        LC.info_Spliter.sendMessage(sender);

        for (int i = 0; i < list.size(); i++) {
            int index = i + 1;
            String name = list.get(i);
            RawMessageCommand rmc = new RawMessageCommand() {
                @Override
                public void run(CommandSender sender) {
                    Residence.getInstance().getSchematicManager().load(res, index);
                }
            };
            rmc.setKeep(true);
            RawMessage message = new RawMessage();
            message.addText("&e" + index + ". &7" + name);
            message.addHover("&eLoad schematic &7" + name);
            message.addCommand(rmc);
            message.show(sender);
        }
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Restore residence from schematics");
        c.get("Info", Arrays.asList("&eUsage: &6/res restore [residence_name] [index]"));
        LocaleManager.addTabCompleteMain(this, "[residence]");
    }
}
