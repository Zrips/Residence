package com.bekvon.bukkit.residence.selection;

import java.util.List;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public interface WESchematicManager {

    boolean save(ClaimedResidence res);

    boolean load(ClaimedResidence res);

    List<String> getList(ClaimedResidence res);

    boolean load(ClaimedResidence res, int index);

    boolean rename(ClaimedResidence res, String newName);

    boolean delete(ClaimedResidence res);

}
