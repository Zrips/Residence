package com.bekvon.bukkit.residence.text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Locale.YmlMaker;

public class Language {
    public FileConfiguration enlocale;
    public FileConfiguration customlocale;
    private Residence plugin;

    public Language(Residence plugin) {
        this.plugin = plugin;
    }

    /**
     * Reloads the config
     */
    public void LanguageReload() {
        File f = new File(plugin.getDataFolder(), "Language" + File.separator + plugin.getConfigManager().getLanguage() + ".yml");
        if (!f.isFile())
            try {
                f.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        f = new File(plugin.getDataFolder(), "Language" + File.separator + "English.yml");
        if (!f.isFile())
            try {
                f.createNewFile();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        customlocale = new YmlMaker(plugin, "Language" + File.separator + plugin.getConfigManager().getLanguage() + ".yml").getConfig();
        enlocale = new YmlMaker(plugin, "Language" + File.separator + "English.yml").getConfig();
        if (customlocale == null)
            customlocale = enlocale;
    }

    /**
     * Get the message with the correct key
     * 
     * @param key - the path of the message
     * @param variables - the variables separated with %
     * @return the message
     */

    public String getMessage(String key) {
        if (!key.contains("Language.") && !key.contains("CommandHelp."))
            key = "Language." + key;
        String missing = "Missing locale for " + key;
        String message = "";
        if (customlocale == null || !customlocale.contains(key))
            message = enlocale.contains(key) ? enlocale.getString(key) : missing;
        message = customlocale.contains(key) ? customlocale.getString(key) : missing;
        return CMIChatColor.translate(message);
    }

    /**
     * Get the message with the correct key
     * 
     * @param key - the path of the message
     * @param variables - the variables separated with %
     * @return the message
     */

    public String getMessage(lm lm, Object... variables) {
        String key = lm.getPath();
        if (!key.contains("Language.") && !key.contains("CommandHelp."))
            key = "Language." + key;
        String missing = "Missing locale for " + key;
        String message = "";
        if (customlocale == null || !customlocale.contains(key))
            message = enlocale.contains(key) ? enlocale.getString(key) : missing;
        message = customlocale.contains(key) ? customlocale.getString(key) : missing;
        for (int i = 1; i <= variables.length; i++) {
            String vr = String.valueOf(variables[i - 1]);
            if (variables[i - 1] instanceof Flags)
                vr = ((Flags) variables[i - 1]).getName();
            message = message.replace("%" + i, vr);
        }

        return CMIChatColor.translate(message);
    }

    /**
     * Get the message with the correct key
     * 
     * @param key - the key of the message
     * @return the message
     */
    public String getDefaultMessage(String key) {
        if (!key.contains("Language.") && !key.contains("CommandHelp."))
            key = "Language." + key;
        String missing = "Missing locale for " + key;
        return enlocale.contains(key) ? CMIChatColor.translate(enlocale.getString(key)) : missing;
    }

    /**
     * Get the message with the correct key
     * 
     * @param key - the key of the message
     * @return the message
     */
    public List<String> getMessageList2(String key) {
        if (!key.contains("Language.") && !key.contains("CommandHelp."))
            key = "Language." + key;
        String missing = "Missing locale for " + key;
        if (customlocale.isList(key))
            return ColorsArray(customlocale.getStringList(key));
        return enlocale.getStringList(key).size() > 0 ? ColorsArray(enlocale.getStringList(key)) : Arrays.asList(missing);
    }

    /**
     * Get the message with the correct key
     * 
     * @param key - the key of the message
     * @return the message
     */
    public List<String> getMessageList(lm lm) {
        String key = lm.getPath();
        if (!key.contains("Language.") && !key.contains("CommandHelp."))
            key = "Language." + key;
        String missing = "Missing locale for " + key;
        if (customlocale.isList(key))
            return ColorsArray(customlocale.getStringList(key));
        return enlocale.getStringList(key).size() > 0 ? ColorsArray(enlocale.getStringList(key)) : Arrays.asList(missing);
    }

    /**
     * Get the message with the correct key
     * 
     * @param key - the key of the message
     * @return the message
     */
    public Set<String> getKeyList(String key) {
        if (customlocale.isConfigurationSection(key))
            return customlocale.getConfigurationSection(key).getKeys(false);
        return enlocale.getConfigurationSection(key).getKeys(false);
    }

    /**
     * Check if key exists
     * 
     * @param key - the key of the message
     * @return true/false
     */
    public boolean containsKey(String key) {
        if (!key.contains("Language.") && !key.contains("CommandHelp."))
            key = "Language." + key;
        if (customlocale == null || !customlocale.contains(key))
            return enlocale.contains(key);
        return customlocale.contains(key);
    }

    private static List<String> ColorsArray(List<String> text) {
        List<String> temp = new ArrayList<String>();
        for (String part : text) {
            temp.add(Colors(part));
        }
        return temp;
    }

    private static String Colors(String text) {
        return CMIChatColor.translate(text);
    }
}
