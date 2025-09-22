package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.Placeholders.Placeholder.CMIPlaceHolders;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class placeholders implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 150, regVar = { 0, 1, 2, 3 }, consoleVar = { 3 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        LC.info_Spliter.sendMessage(sender);

        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;

        int page = 1;
        if (args.length > 0 && args[0].startsWith("-p:")) {
            try {
                page = Integer.parseInt(args[0].substring("-p:".length(), args[0].length()));
            } catch (Exception e) {
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("parse")) {

            String placeHolder = null;
            for (String one : args) {
                if (one.equalsIgnoreCase("parse"))
                    continue;
                if (one.contains("%") || one.contains("{")) {
                    placeHolder = one;
                    continue;
                }
                player = Bukkit.getPlayer(one);
            }

            lm.command_Parsed.sendMessage(sender, plugin.getPlaceholderAPIManager().updatePlaceHolders(player, placeHolder));

            return true;
        }

        PageInfo pi = new PageInfo(player != null ? 10 : CMIPlaceHolders.values().length, CMIPlaceHolders.values().length, page);

        for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
            if (pi.isBreak())
                break;

            if (!pi.isEntryOk())
                continue;

            RawMessage rm = new RawMessage();
            String extra = "";

            if (player != null)
                extra = plugin.getPlaceholderAPIManager().updatePlaceHolders(player, lm.command_PlacehlderResult.getMessage(one.getFull()));

            String place = one.getFull();
            StringBuilder hover = new StringBuilder();
            if (plugin.isPlaceholderAPIEnabled()) {
                place = one.getFull();
                hover.append(one.getFull());
            }

            rm.addText(lm.command_PlacehlderList.getMessage(pi.getPositionForOutput(), place) + extra).addHover(hover.toString()).addSuggestion(one.getFull());
            rm.show(sender);
        }

        if (player != null) {
            pi.autoPagination(sender, "res placeholders", "-p:");
        }

        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "List of placeholders");
        c.get("Info", Arrays.asList("&eUsage: &6/res placeholders (parse) (placeholder) (playerName)"));
        c.get("parse", "[result]");
        LocaleManager.addTabCompleteMain(this);
        LocaleManager.addTabCompleteMain(this, "parse", "[placeholder]", "[playername]");
    }
}
