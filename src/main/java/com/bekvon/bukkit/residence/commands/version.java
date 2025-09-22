package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;

import net.Zrips.CMILib.Container.CMIText;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Version;

public class version implements cmd {

    @Override
    @CommandAnnotation(simple = false, priority = 5900)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        final String version = plugin.getDescription().getVersion();
        String build = null;

        String[] split = Bukkit.getVersion().split("-");
        final String serverType = split.length > 1 ? split[1] : split[0];

        try {
            if (serverType.equalsIgnoreCase("Paper") || serverType.equalsIgnoreCase("purpur") || Version.isPaper()) {
                if (Version.isCurrentEqualOrHigher(Version.v1_20_R4))
                    build = Bukkit.getVersion().split("-")[1].split(" ")[0];
                else
                    build = Bukkit.getVersion().split("-")[2].split(" ")[0];
            } else if (Version.isSpigot())
                build = Bukkit.getVersion().split("-", 2)[0];
            Integer.parseInt(build);
        } catch (Throwable e) {
            build = null;
        }

        final String buildVersion = build == null ? "" : "(" + build + ")";
        Plugin CMILib = Bukkit.getPluginManager().getPlugin("CMILib");
        String CMILibversion = null;
        if (CMILib != null) {
            CMILibversion = CMILib.getDescription().getVersion();
        }
        String javaVersion = System.getProperty("java.version");

        LC.info_Spliter.sendMessage(sender);

        CMIMessages.sendMessage(sender, "&fResidence version: &7" + version);
        CMIMessages.sendMessage(sender, "&fCMILib version: &7" + CMILibversion);
        if (build != null)
            CMIMessages.sendMessage(sender, "&fServer version: &7" + CMIText.firstToUpperCase(Version.getPlatform().toString()) + buildVersion);
        CMIMessages.sendMessage(sender, "&fJava version: &7" + javaVersion);

        LC.info_Spliter.sendMessage(sender);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "how residence version");
        c.get("Info", Arrays.asList("&eUsage: &6/res version"));
    }
}
