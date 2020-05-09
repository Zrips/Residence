package com.bekvon.bukkit.residence.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bekvon.bukkit.CMIGUI.CMIGuiButton;
import com.bekvon.bukkit.CMIGUI.GUIManager.GUIClickType;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.utils.Debug;

public class setFlagInfo {

    private ClaimedResidence residence;
    private Player player;
    private String targetPlayer = null;
    private LinkedHashMap<Flags, List<String>> description = new LinkedHashMap<Flags, List<String>>();
    private List<CMIGuiButton> buttons = new ArrayList<CMIGuiButton>();
    private boolean admin = false;

    public setFlagInfo(ClaimedResidence residence, Player player, boolean admin) {
	this.residence = residence;
	this.player = player;
	this.admin = admin;
	fillFlagDescriptions();
    }

    public setFlagInfo(ClaimedResidence residence, Player player, String targetPlayer, boolean admin) {
	this.residence = residence;
	this.player = player;
	this.targetPlayer = targetPlayer;
	this.admin = admin;
	fillFlagDescriptions();
    }

    public void setAdmin(boolean state) {
	this.admin = state;
    }

    public boolean isAdmin() {
	return this.admin;
    }

    public ClaimedResidence getResidence() {
	return this.residence;
    }

    public Player getPlayer() {
	return this.player;
    }

    private void fillFlagDescriptions() {
	for (Flags flag : Flags.values()) {
	    List<String> lore = new ArrayList<String>();
	    int i = 0;
	    String sentence = "";
	    for (String oneWord : flag.getDesc().split(" ")) {
		sentence += oneWord + " ";
		if (i > 4) {
		    lore.add(ChatColor.YELLOW + sentence);
		    sentence = "";
		    i = 0;
		}
		i++;
	    }
	    lore.add(ChatColor.YELLOW + sentence);
	    description.put(flag, lore);
	}
    }

    public void recalculate() {
	if (targetPlayer == null)
	    recalculateResidence();
	else
	    recalculatePlayer();
    }

    private void recalculateResidence() {
	buttons.clear();

	List<String> flags = residence.getPermissions().getPosibleFlags(player, true, this.admin);

	Map<String, Boolean> resFlags = new HashMap<String, Boolean>();
	Map<String, Object> TempPermMap = new LinkedHashMap<String, Object>();

	Map<String, Boolean> globalFlags = Residence.getInstance().getPermissionManager().getAllFlags().getFlags();

	for (Entry<String, Boolean> one : residence.getPermissions().getFlags().entrySet()) {
	    if (flags.contains(one.getKey())) {
		resFlags.put(one.getKey(), one.getValue());
	    }
	}

	for (Entry<String, Boolean> one : globalFlags.entrySet()) {
	    String fname = one.getKey();

	    Flags flag = Flags.getFlag(fname);

	    if (flag != null && !flag.isGlobalyEnabled())
		continue;

	    if (!flags.contains(one.getKey())) {
		continue;
	    }

	    if (resFlags.containsKey(one.getKey()))
		TempPermMap.put(one.getKey(), resFlags.get(one.getKey()) ? FlagState.TRUE : FlagState.FALSE);
	    else
		TempPermMap.put(one.getKey(), FlagState.NEITHER);
	}

	if (targetPlayer == null)
	    TempPermMap.remove("admin");

	TempPermMap = Residence.getInstance().getSortingManager().sortByKeyASC(TempPermMap);

//	FlagData flagData = Residence.getInstance().getFlagUtilManager().getFlagData();

	LinkedHashMap<String, Object> permMap = new LinkedHashMap<String, Object>();
	for (Entry<String, Object> one : TempPermMap.entrySet()) {
	    permMap.put(one.getKey(), one.getValue());
	}

	String cmdPrefix = admin ? "resadmin" : "res";

	int i = 0;
	for (Entry<String, Object> one : permMap.entrySet()) {
	    i = i > 44 ? 0 : i;

	    CMIGuiButton button = new CMIGuiButton(i, updateLook(one.getKey())) {
		@Override
		public void click(GUIClickType type) {
		    String command = "true";
		    switch (type) {
		    case Left:
			break;
		    case Right:
			command = "false";
			break;
		    case RightShift:
		    case LeftShift:
		    case MiddleMouse:
			command = "remove";
			break;
		    default:
			break;
		    }

		    Bukkit.dispatchCommand(player, cmdPrefix + " set " + residence.getName() + " " + one.getKey() + " " + command);
		    if (Residence.getInstance().getConfigManager().isConsoleLogsShowFlagChanges())
			Residence.getInstance().consoleMessage(player.getName() + " issued server command: /" + cmdPrefix + " set " + residence.getName() + " " + one.getKey() + " " + command);
		    updateLooks();
		}

		@Override
		public void updateLooks() {
		    this.setItem(updateLook(one.getKey()));
		    hideItemFlags();
		    this.update();
		}
	    };
	    button.hideItemFlags();

	    buttons.add(button);
	    i++;
	}
	description.clear();
    }

    private void recalculatePlayer() {
	Map<String, Boolean> globalFlags = new HashMap<String, Boolean>();
	for (Flags oneFlag : Flags.values()) {
	    globalFlags.put(oneFlag.toString(), oneFlag.isEnabled());
	}

	List<String> flags = residence.getPermissions().getPosibleFlags(player, false, this.admin);

	Map<String, Boolean> resFlags = new HashMap<String, Boolean>();

	for (Entry<String, Boolean> one : residence.getPermissions().getFlags().entrySet()) {
	    if (flags.contains(one.getKey()))
		resFlags.put(one.getKey(), one.getValue());
	}

	if (targetPlayer != null) {

	    Set<String> PosibleResPFlags = FlagPermissions.getAllPosibleFlags();
	    Map<String, Boolean> temp = new HashMap<String, Boolean>();
	    for (String one : PosibleResPFlags) {
		if (globalFlags.containsKey(one))
		    temp.put(one, globalFlags.get(one));
	    }
	    globalFlags = temp;

	    Map<String, Boolean> pFlags = residence.getPermissions().getPlayerFlags(targetPlayer);

	    if (pFlags != null)
		for (Entry<String, Boolean> one : pFlags.entrySet()) {
		    resFlags.put(one.getKey(), one.getValue());
		}
	}

	LinkedHashMap<String, Object> TempPermMap = new LinkedHashMap<String, Object>();

	for (Entry<String, Boolean> one : globalFlags.entrySet()) {
	    if (!flags.contains(one.getKey()))
		continue;

	    if (resFlags.containsKey(one.getKey()))
		TempPermMap.put(one.getKey(), resFlags.get(one.getKey()) ? FlagState.TRUE : FlagState.FALSE);
	    else
		TempPermMap.put(one.getKey(), FlagState.NEITHER);
	}

	TempPermMap = (LinkedHashMap<String, Object>) Residence.getInstance().getSortingManager().sortByKeyASC(TempPermMap);

	LinkedHashMap<String, Object> permMap = new LinkedHashMap<String, Object>();
	for (Entry<String, Object> one : TempPermMap.entrySet()) {
	    permMap.put(one.getKey(), one.getValue());
	}

	String targetPlayerName = targetPlayer == null ? "" : " " + targetPlayer;
	String cmdPrefix = admin ? "resadmin" : "res";

	int i = 0;
	for (Entry<String, Object> one : permMap.entrySet()) {

	    i = i > 44 ? 0 : i;

	    CMIGuiButton button = new CMIGuiButton(i, updateLook(one.getKey())) {
		@Override
		public void click(GUIClickType type) {
		    String command = "true";
		    switch (type) {
		    case Left:
			break;
		    case Right:
			command = "false";
			break;
		    case RightShift:
		    case LeftShift:
		    case MiddleMouse:
			command = "remove";
			break;
		    default:
			break;
		    }

		    Bukkit.dispatchCommand(player, cmdPrefix + " pset " + residence.getName() + targetPlayerName + " " + one.getKey() + " " + command);
		    if (Residence.getInstance().getConfigManager().isConsoleLogsShowFlagChanges())
			Residence.getInstance().consoleMessage(player.getName() + " issued server command: /" + cmdPrefix + " pset " + residence.getName() + targetPlayerName + " " + one
			    .getKey() + " " + command);
		    updateLooks();
		}

		@Override
		public void updateLooks() {
		    this.setItem(updateLook(one.getKey()));
		    hideItemFlags();
		    this.update();
		}

	    };
	    i++;

	    button.hideItemFlags();
	    buttons.add(button);
	}
	description.clear();
    }

    private ItemStack updateLook(String flagName) {

	Boolean have = null;
	ResidencePermissions fp = this.residence.getPermissions();

	if (this.targetPlayer != null) {
	    if (fp.playerHas(targetPlayer, flagName, FlagCombo.OnlyTrue))
		have = true;
	    else if (fp.playerHas(targetPlayer, flagName, FlagCombo.OnlyFalse))
		have = false;
	} else {
	    if (fp.has(flagName, FlagCombo.OnlyTrue))
		have = true;
	    else if (fp.has(flagName, FlagCombo.OnlyFalse))
		have = false;
	}

	FlagState state = FlagState.NEITHER;
	if (have == null) {

	} else if (have)
	    state = FlagState.TRUE;
	else
	    state = FlagState.FALSE;

	ItemStack MiscInfo = Residence.getInstance().getConfigManager().getGuiBottonStates(state).clone();

	FlagData flagData = Residence.getInstance().getFlagUtilManager().getFlagData();

	if (flagData.contains(flagName))
	    MiscInfo = flagData.getItem(flagName).clone();

	if (state == FlagState.TRUE) {
	    ItemMeta im = MiscInfo.getItemMeta();
	    im.addEnchant(Enchantment.LUCK, 1, true);
	    MiscInfo.setItemMeta(im);
	} else
	    MiscInfo.removeEnchantment(Enchantment.LUCK);

	ItemMeta MiscInfoMeta = MiscInfo.getItemMeta();

	Flags flag = Flags.getFlag(flagName);
	if (flag != null)
	    flagName = flag.getName();
	if (flagName == null)
	    flagName = "Unknown";
	// Can it be null?
	if (MiscInfoMeta == null)
	    return MiscInfo;
	MiscInfoMeta.setDisplayName(ChatColor.GREEN + flagName);
	List<String> lore = new ArrayList<String>();
	String variable = "";
	switch (state) {
	case FALSE:
	    variable = Residence.getInstance().msg(lm.General_False);
	    break;
	case TRUE:
	    variable = Residence.getInstance().msg(lm.General_True);
	    break;
	case NEITHER:
	    variable = Residence.getInstance().msg(lm.General_Removed);
	    break;
	}
	lore.add(Residence.getInstance().msg(lm.General_FlagState, variable));
	if (description.containsKey(flag))
	    lore.addAll(description.get(flag));
	lore.addAll(Residence.getInstance().msgL(lm.Gui_Actions));
	MiscInfoMeta.setLore(lore);
	MiscInfo.setItemMeta(MiscInfoMeta);

	return MiscInfo;
    }

    public List<CMIGuiButton> getButtons() {
	return buttons;
    }
}
