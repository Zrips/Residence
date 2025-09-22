package com.bekvon.bukkit.residence.gui;

import java.util.HashMap;
import org.bukkit.inventory.ItemStack;

import net.Zrips.CMILib.Items.CMIMaterial;

public class FlagData {

    private HashMap<String, ItemStack> items = new HashMap<String, ItemStack>();

    public void addFlagButton(String flag, ItemStack item) {
        this.items.put(flag, item == null ? CMIMaterial.STONE.newItemStack() : item);
    }

    public void removeFlagButton(String flag) {
        this.items.remove(flag);
    }

    public boolean contains(String flag) {
        return items.containsKey(flag);
    }

    public ItemStack getItem(String flag) {
        return items.get(flag);
    }

}
