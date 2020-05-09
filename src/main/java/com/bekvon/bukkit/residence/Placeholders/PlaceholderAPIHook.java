package com.bekvon.bukkit.residence.Placeholders;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.Placeholders.Placeholder.CMIPlaceHolders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private Residence plugin;

    public PlaceholderAPIHook(Residence plugin) {
	this.plugin = plugin;
    }

    @Override
    public boolean persist() {
	return true;
    }

    @Override
    public boolean canRegister() {
	return true;
    }

    @Override
    public String getAuthor() {
	return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
	return "residence";
    }

    @Override
    public String getVersion() {
	return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
	CMIPlaceHolders placeHolder = CMIPlaceHolders.getByName(identifier);
	if (placeHolder == null) {
	    return null;
	}
	return plugin.getPlaceholderAPIManager().getValue(player, placeHolder);
    }
}