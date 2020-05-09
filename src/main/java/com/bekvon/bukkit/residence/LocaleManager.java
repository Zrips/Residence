package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.containers.CommandStatus;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class LocaleManager {

    public HashMap<List<String>, List<String>> CommandTab = new HashMap<List<String>, List<String>>();
    public String path = "CommandHelp.SubCommands.res.SubCommands.";
    private Residence plugin;
    private ConfigReader c = null;

    public LocaleManager(Residence plugin) {
        this.plugin = plugin;
    }

    public static void addTabComplete(Object cl, String subCmd, String... tabs) {
        if (subCmd == null || subCmd.isEmpty())
            Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(cl.getClass().getSimpleName()), Arrays.asList(tabs));
        else
            Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(cl.getClass().getSimpleName(), subCmd), Arrays.asList(tabs));
    }

    private static YamlConfiguration loadConfiguration(BufferedReader in, String language) {
        Validate.notNull(in, "File cannot be null");
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(in);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } catch (InvalidConfigurationException ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Residence] Your locale file for " + language
                    + " is incorect! Use http://yaml-online-parser.appspot.com/ to find issue.");
            return null;
        }

        return config;
    }

    // Language file
    public void LoadLang(String lang) {

        File f = new File(plugin.getDataFolder(), "Language" + File.separator + lang + ".yml");
        if (!f.isFile())
            try {
                f.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        if (in == null)
            return;
        YamlConfiguration conf = loadConfiguration(in, lang);
        if (conf == null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        c = null;
        try {
            c = new ConfigReader("Language" + File.separator + lang + ".yml");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (c == null)
            return;
        c.copyDefaults(true);

        if (lang.equalsIgnoreCase(plugin.getConfigManager().getLanguage()))
            c.setRecordContents(true);

        c.header(Arrays.asList("NOTE If you want to modify this file, it is HIGHLY recommended that you make a copy",
                "of this file and modify that instead. This file will be updated automatically by Residence",
                "when a newer version is detected, and your changes will be overwritten.  Once you ",
                "have a copy of this file, change the Language: option under the Residence config.yml",
                "to whatever you named your copy."));

        for (lm lm : lm.values()) {
            if (lm.getText() instanceof String)
                c.get(lm.getPath(), String.valueOf(lm.getText()));
            else if (lm.getText() instanceof ArrayList<?>) {
                List<String> result = new ArrayList<String>();
                for (Object obj : (ArrayList<?>) lm.getText()) {
                    if (obj instanceof String) {
                        result.add((String) obj);
                    }
                }
                c.get(lm.getPath(), result);
            }

            if (lm.getComments() != null)
                c.addComment(lm.getPath(), lm.getComments());
        }

        c.addComment("CommandHelp", "");

        c.get("CommandHelp.Description", "Contains Help for Residence");
        c.get("CommandHelp.SubCommands.res.Description", "Main Residence Command");
        c.get("CommandHelp.SubCommands.res.Info", Arrays.asList("&2Use &6/res [command] ? <page> &2to view more help Information."));

        for (Entry<String, CommandStatus> cmo : plugin.getCommandFiller().getCommandMap().entrySet()) {
            c.setP(plugin.getLocaleManager().path + cmo.getKey() + ".");
            try {
                Class<?> cl = Class.forName(plugin.getCommandFiller().packagePath + "." + cmo.getKey());
                if (cmd.class.isAssignableFrom(cl)) {
                    cmd cm = (cmd) cl.getConstructor().newInstance();
                    if (!cmo.getValue().getInfo().isEmpty())
                        c.get("Description", cmo.getValue().getInfo());
                    if (cmo.getValue().getUsage().length != 0)
                        c.get("Info", Arrays.asList(cmo.getValue().getUsage()));
                    cm.getLocale();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                continue;
            }
        }
        c.resetP();

        if (lang.equalsIgnoreCase(plugin.getConfigManager().getLanguage())) {
            for (Flags one : Flags.values()) {
                String pt = plugin.getLocaleManager().path + "flags.SubCommands." + one.toString();
                one.setTranslated(c.getC().getString(pt + ".Translated"));
                one.setDesc(c.getC().getString(pt + ".Description"));
            }
        }

        // custom commands

        c.get("CommandHelp.SubCommands.res.SubCommands.resreload.Description", "Reload residence.");
        c.get("CommandHelp.SubCommands.res.SubCommands.resreload.Info",
                Arrays.asList("&eUsage: &6/resreload"));

        c.get("CommandHelp.SubCommands.res.SubCommands.resload.Description", "Load residence save file.");
        c.get("CommandHelp.SubCommands.res.SubCommands.resload.Info",
                Arrays.asList("&eUsage: &6/resload", "UNSAFE command, does not save residences first.", "Loads the residence save file after you have made changes."));

        c.get("CommandHelp.SubCommands.res.SubCommands.removeworld.Description", "Remove all residences from world");
        c.get("CommandHelp.SubCommands.res.SubCommands.removeworld.Info",
                Arrays.asList("&eUsage: &6/res removeworld [worldname]", "Can only be used from console"));

        // Write back config
        c.save();
    }

    public ConfigReader getLocaleConfig() {
        return c;
    }
}
