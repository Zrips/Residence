package com.bekvon.bukkit.CMIGUI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bekvon.bukkit.CMIGUI.GUIManager.GUIClickType;
import com.bekvon.bukkit.CMIGUI.GUIManager.GUIFieldType;
import com.bekvon.bukkit.cmiLib.CMIItemStack;
import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.CMIReflections;
import com.bekvon.bukkit.residence.Residence;

public class CMIGuiButton {

    private Integer slot = null;
    private GUIFieldType fieldType = GUIFieldType.Locked;
    private boolean closeInv = false;

    private HashMap<GUIClickType, List<GUIButtonCommand>> commandMap = new HashMap<GUIClickType, List<GUIButtonCommand>>();
    private List<String> permissions = new ArrayList<String>();
    private ItemStack item = null;

    @Override
    public CMIGuiButton clone() {
	CMIGuiButton b = new CMIGuiButton(slot, fieldType, item);
	b.setPermissions(new ArrayList<String>(permissions));
	b.setCommandMap(new HashMap<GUIClickType, List<GUIButtonCommand>>(commandMap));
	return b;
    }

    public CMIGuiButton(Integer slot, GUIFieldType fieldType, ItemStack item) {
	this.slot = slot;
	this.fieldType = fieldType;
	this.item = item == null ? null : item.clone();
    }

    public CMIGuiButton(Integer slot) {
	this.slot = slot;
    }

    public void hideItemFlags() {
	if (item != null)
	    item = CMIReflections.HideFlag(item, 63);
    }

    public CMIGuiButton(ItemStack item) {
	this.item = item == null ? null : item.clone();
    }

    public CMIGuiButton(CMIMaterial mat) {
	this.item = mat == null ? null : mat.newItemStack();
    }

    public CMIGuiButton(Integer slot, CMIItemStack item) {
	this(slot, item.getItemStack());
    }

    public CMIGuiButton(Integer slot, Material material) {
	this(slot, CMIMaterial.get(material), null);
    }

    public CMIGuiButton(Integer slot, CMIMaterial material) {
	this(slot, material, null);
    }

    @Deprecated
    public CMIGuiButton(Integer slot, Material material, int data) {
	this(slot, material, data, null);
    }

    public CMIGuiButton(Integer slot, Material material, String name) {
	this(slot, CMIMaterial.get(material), name);
    }

    public CMIGuiButton(Integer slot, CMIMaterial material, String name) {
	this.slot = slot;
	this.item = material == null ? null : material.newItemStack();
	if (name != null) {
	    ItemMeta meta = this.item.getItemMeta();
	    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
	    this.item.setItemMeta(meta);
	}
    }

    @Deprecated
    public CMIGuiButton(Integer slot, Material material, int data, String name) {
	this.slot = slot;
	this.item = new ItemStack(material, 1, (short) data);
	if (name != null) {
	    ItemMeta meta = this.item.getItemMeta();
	    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
	    this.item.setItemMeta(meta);
	}
    }

    public CMIGuiButton(Integer slot, ItemStack item) {
	this.slot = slot;
	this.item = item == null ? null : item.clone();
	if (this.item != null && this.item.getDurability() == 32767) {
	    CMIMaterial d = CMIMaterial.getRandom(CMIMaterial.get(this.item));
	    if (d != null && d.getLegacyData() != -1)
		this.item.setDurability((short) d.getLegacyData());
	}
    }

    private int schedId = -1;
    private int updateInterval = 20;
    private int ticks = 0;
    private CMIGui sgui;

    public void setGui(CMIGui sgui) {
	this.sgui = sgui;
    }

    public void startAutoUpdate(int intervalTicks) {
	updateInterval = intervalTicks;
	tasker();
    }

    @Deprecated
    public void startAutoUpdate(CMIGui sgui, int intervalTicks) {
	updateInterval = intervalTicks;
	this.sgui = sgui;
	tasker();
    }

    private void tasker() {
	if (schedId != -1) {
	    Bukkit.getScheduler().cancelTask(schedId);
	    schedId = -1;
	}
	CMIGuiButton b = this;
	schedId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Residence.getInstance(), new Runnable() {
	    @Override
	    public void run() {
		ticks++;
		if (sgui != null && GUIManager.getGui(sgui.getPlayer()) != sgui) {
		    if (schedId != -1) {
			Bukkit.getScheduler().cancelTask(schedId);
			schedId = -1;
			return;
		    }
		}
		updateLooks();
		update(sgui);
		if (sgui != null)
		    sgui.updateButton(b);
	    }
	}, 20L, updateInterval);
    }

    public void updateLooks() {

    }

    public void update() {
	if (this.sgui != null)
	    sgui.updateButton(this);
    }

    public void update(CMIGui gui) {
	if (gui != null)
	    gui.updateButton(this);
    }

    public Integer getSlot() {
	return slot;
    }

    public CMIGuiButton setSlot(Integer slot) {
	this.slot = slot;
	return this;
    }

    public GUIFieldType getFieldType() {
	return fieldType;
    }

    public CMIGuiButton setFieldType(GUIFieldType fieldType) {
	this.fieldType = fieldType;
	return this;
    }

    public CMIGuiButton lockField() {
	this.fieldType = GUIFieldType.Locked;
	return this;
    }

    public CMIGuiButton unlockField() {
	this.fieldType = GUIFieldType.Free;
	return this;
    }

    public boolean isLocked() {
	return this.fieldType.equals(GUIFieldType.Locked);
    }

    public List<String> getPermissions() {
	return permissions;
    }

    public CMIGuiButton addPermission(String perm) {
	this.permissions.add(perm);
	return this;
    }

    public void setPermissions(List<String> permissions) {
	this.permissions = permissions;
    }

    public List<GUIButtonCommand> getCommands(GUIClickType type) {
	List<GUIButtonCommand> list = commandMap.get(type);
	if (list == null)
	    list = new ArrayList<GUIButtonCommand>();
	return list;
    }

    public CMIGuiButton setName(String name) {
	if (this.item == null)
	    return this;
	ItemMeta meta = this.item.getItemMeta();
	if (meta != null) {
	    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
	    this.item.setItemMeta(meta);
	}
	return this;
    }

    public CMIGuiButton addLore(List<String> l) {
	l = spreadList(l);
	for (String one : l) {
	    addLore(one);
	}
	return this;
    }

    public List<String> spreadList(List<String> ls) {
	List<String> s = new ArrayList<String>();
	for (int i = 0; i < ls.size(); i++) {
	    if (ls.get(i).contains(" \\n")) {
		s.addAll(Arrays.asList(ls.get(i).split(" \\\\n")));
	    } else if (ls.get(i).contains(" \n")) {
		s.addAll(Arrays.asList(ls.get(i).split(" \\n")));
	    } else
		s.add(ls.get(i));
	}
	return s;
    }

    public CMIGuiButton addLore(String l) {
	if (this.item == null)
	    return this;
	ItemMeta meta = this.item.getItemMeta();

	if (meta != null) {
	    List<String> lore = meta.getLore();
	    if (lore == null)
		lore = new ArrayList<String>();

	    if (l.contains("\\n")) {
		String[] split = l.split("\\\\n");
		for (String one : split) {
		    lore.add(ChatColor.translateAlternateColorCodes('&', one));
		}
	    } else if (l.contains("\n")) {
		String[] split = l.split("\\n");
		for (String one : split) {
		    lore.add(ChatColor.translateAlternateColorCodes('&', one));
		}
	    } else
		lore.add(ChatColor.translateAlternateColorCodes('&', l));
	    meta.setLore(lore);
	    this.item.setItemMeta(meta);
	}
	return this;
    }

    public CMIGuiButton clearLore() {
	if (this.item == null)
	    return this;
	ItemMeta meta = this.item.getItemMeta();
	if (meta != null) {
	    meta.setLore(new ArrayList<String>());
	    this.item.setItemMeta(meta);
	}
	return this;
    }

    public CMIGuiButton addItemName(String name) {
	if (this.item == null)
	    return this;
	ItemMeta meta = this.item.getItemMeta();
	meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
	this.item.setItemMeta(meta);
	return this;
    }

    public CMIGuiButton addCommand(String command) {
	return addCommand(null, command);
    }

    public CMIGuiButton addCommand(String command, CommandType vis) {
	return addCommand(null, command, vis);
    }

    public CMIGuiButton addCommand(GUIClickType type, String command) {
	return addCommand(type, command, CommandType.gui);
    }

    public CMIGuiButton addCommand(GUIClickType type, String command, CommandType vis) {
	if (type == null) {
	    for (GUIClickType one : GUIClickType.values()) {
		List<GUIButtonCommand> list = commandMap.get(one);
		if (list == null)
		    list = new ArrayList<GUIButtonCommand>();
		list.add(new GUIButtonCommand(command, vis));
		commandMap.put(one, list);
	    }
	} else {
	    List<GUIButtonCommand> list = commandMap.get(type);
	    if (list == null)
		list = new ArrayList<GUIButtonCommand>();
	    list.add(new GUIButtonCommand(command, vis));
	    commandMap.put(type, list);
	}
	return this;
    }

    public void click() {
//	click(null, null);
    }

    public void click(GUIClickType type) {
//	click(type, null);
    }

    public CMIGuiButton addCommand(Location loc) {
	if (loc == null)
	    return this;
	addCommand("cmi tppos " + loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getBlockZ() + " " + loc.getPitch() + " " + loc.getYaw());
	return this;
    }

    public ItemStack getItem() {
	return item;
    }

    public ItemStack getItem(Player player) {

	if (item != null) {
	    ItemStack i = item.clone();

	    if (this.isLocked())
		i = CMIReflections.setNbt(i, GUIManager.CMIGUIIcon, GUIManager.LIProtection);

	    ItemMeta meta = i.hasItemMeta() ? i.getItemMeta() : null;

	    if (meta != null)
		i.setItemMeta(meta);
	    return i;
	}

	return item;
    }

    public CMIGuiButton setItem(ItemStack item) {
	this.item = item == null ? null : item.clone();
	return this;
    }

    public void setCommandMap(HashMap<GUIClickType, List<GUIButtonCommand>> commandMap) {
	this.commandMap = commandMap;
    }

    public boolean isCloseInv() {
	return closeInv;
    }

    public void setCloseInv(boolean closeInv) {
	this.closeInv = closeInv;
    }

    public CMIGui getGui() {
	return sgui;
    }

    public int getTicks() {
	return ticks;
    }

}
