package com.bekvon.bukkit.residence.api;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;

import java.util.ArrayList;
import java.util.UUID;

public interface ResidencePlayerInterface {
    public ArrayList<String> getResidenceList(String player);

    public ArrayList<String> getResidenceList(String player, boolean showhidden);

    public PermissionGroup getGroup(String player);

    public int getMaxResidences(String player);

    public int getMaxSubzones(String player);

    public int getMaxRents(String player);

    public ResidencePlayer getResidencePlayer(String player);

    public int getMaxSubzoneDepth(String player);

    ArrayList<String> getResidenceList(UUID uuid);
}
