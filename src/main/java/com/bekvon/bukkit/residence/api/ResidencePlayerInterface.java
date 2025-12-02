package com.bekvon.bukkit.residence.api;

import java.util.ArrayList;
import java.util.UUID;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;

public interface ResidencePlayerInterface {

    public ArrayList<String> getResidenceList(String player);

    ArrayList<String> getResidenceList(UUID uuid);

    public ArrayList<String> getResidenceList(String player, boolean showhidden);

    public ResidencePlayer getResidencePlayer(String player);

}
