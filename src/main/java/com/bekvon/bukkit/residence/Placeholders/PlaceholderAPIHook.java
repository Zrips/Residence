package com.bekvon.bukkit.residence.Placeholders;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        return process(player == null ? null : player.getUniqueId(), identifier);
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        return process(player == null ? null : player.getUniqueId(), identifier);
    }

    private String process(UUID uuid, String identifier) {
        CMIPlaceHolders placeHolder = CMIPlaceHolders.getByName("residence_" + identifier);
        if (placeHolder == null) {
            return null;
        }
        return plugin.getPlaceholderAPIManager().getValue(uuid, placeHolder, "residence_" + identifier);
    }
}