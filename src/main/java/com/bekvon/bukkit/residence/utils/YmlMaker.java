package com.bekvon.bukkit.residence.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bekvon.bukkit.residence.Residence;

public class YmlMaker {
    Residence Plugin;
    public String fileName;
    private JavaPlugin plugin;
    public File ConfigFile;
    private FileConfiguration Configuration;

    public YmlMaker(Residence Plugin) {
	this.Plugin = Plugin;
    }

    public YmlMaker(JavaPlugin plugin, String fileName) {
	if (plugin == null) {
	    throw new IllegalArgumentException("plugin cannot be null");
	}
	this.plugin = plugin;
	this.fileName = fileName;
	File dataFolder = plugin.getDataFolder();
	if (dataFolder == null) {
	    throw new IllegalStateException();
	}
	this.ConfigFile = new File(dataFolder.toString() + File.separatorChar + this.fileName);
    }

    private static YamlConfiguration loadConfiguration(InputStreamReader inputStreamReader) {
	Validate.notNull(inputStreamReader, "File cannot be null");
	YamlConfiguration config = new YamlConfiguration();
	try {
	    config.load(inputStreamReader);
	} catch (FileNotFoundException ex) {
	} catch (IOException ex) {
	} catch (InvalidConfigurationException ex) {
	    return null;
	}
	return config;
    }

    private YamlConfiguration loadConfiguration(InputStream defConfigStream) {
	Validate.notNull(defConfigStream, "File cannot be null");
	YamlConfiguration config = new YamlConfiguration();
	try {
	    config.load(ConfigFile);
	} catch (FileNotFoundException ex) {
	} catch (IOException ex) {
	} catch (InvalidConfigurationException ex) {
	    return null;
	}
	return config;
    }

    public void reloadConfig() {
	InputStreamReader f = null;
	try {
	    f = new InputStreamReader(new FileInputStream(this.ConfigFile), "UTF-8");
	} catch (UnsupportedEncodingException e1) {
	    e1.printStackTrace();
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	}

	if (f != null)
	    this.Configuration = loadConfiguration(f);

	if (Configuration == null) {
	    if (f != null)
		try {
		    f.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    return;
	}

	InputStream defConfigStream = this.plugin.getResource(this.fileName);
	if (defConfigStream != null) {
	    YamlConfiguration defConfig = loadConfiguration(defConfigStream);
	    if (defConfig != null)
		this.Configuration.setDefaults(defConfig);
	}
	if (defConfigStream != null)
	    try {
		defConfigStream.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	if (f != null)
	    try {
		f.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
    }

    public FileConfiguration getConfig() {
	if (this.Configuration == null) {
	    reloadConfig();
	}
	return this.Configuration;
    }

    public void saveConfig() {
	if ((this.Configuration == null) || (this.ConfigFile == null)) {
	    return;
	}
	try {
	    getConfig().save(this.ConfigFile);
	} catch (IOException ex) {
	    this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.ConfigFile, ex);
	}
    }

    public void saveDefaultConfig() {
	if (!this.ConfigFile.exists()) {
	    this.plugin.saveResource(this.fileName, false);
	}
    }
}
