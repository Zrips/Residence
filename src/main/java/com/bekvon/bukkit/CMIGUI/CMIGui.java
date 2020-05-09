package com.bekvon.bukkit.CMIGUI;

import com.bekvon.bukkit.CMIGUI.GUIManager.*;
import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.CMIReflections;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

public class CMIGui {

    private InventoryType invType;
    private GUIRows gUIRows;
    private Player player;
    private Inventory inv;
    private String title;
    private HashMap<Integer, CMIGuiButton> buttons = new HashMap<Integer, CMIGuiButton>();
    private LinkedHashSet<CMIGuiButton> noSlotButtons = new LinkedHashSet<CMIGuiButton>();

    private HashMap<InvType, GUIFieldType> lock = new HashMap<InvType, GUIFieldType>();
    private HashMap<InvType, String> permLock = new HashMap<InvType, String>();

    private CmiInventoryType type = CmiInventoryType.regular;
    private Object whatShows;
    private Object tempData;

    private boolean allowShift = false;
    private boolean allowPickUpAll = false;

    public CMIGui(Player player) {
        this.player = player;
    }

    @Override
    public CMIGui clone() {
        CMIGui g = new CMIGui(player);
        g.setInvSize(gUIRows);
        g.setButtons(buttons);
        g.setInv(inv);
        g.setInvType(invType);
        g.setTitle(title);
        g.setCmiInventoryType(type);
        g.setWhatShows(whatShows);
        return g;
    }

    public boolean isOpened() {
        return GUIManager.isOpenedGui(getPlayer());
    }

    public boolean isSimilar(CMIGui gui) {

        if (this.getInvSize() != gui.getInvSize())
            return false;

        if (this.getInvType() != gui.getInvType())
            return false;

        return true;
    }

    public CMIGui open() {
        GUIManager.openGui(this);
        return this;
    }

    public void outsideClick(GUIClickType type) {

    }

    public InventoryType getInvType() {
        if (invType == null)
            invType = InventoryType.CHEST;
        return invType;
    }

    public void setInvType(InventoryType invType) {
        this.invType = invType;
    }

    public GUIRows getInvSize() {
        if (gUIRows == null)
            autoResize();
        return gUIRows;
    }

    public void setInvSize(GUIRows GUIRows) {
        this.gUIRows = GUIRows;
    }

    public void setInvSize(int rows) {
        this.gUIRows = GUIRows.getByRows(rows);
    }

    public void autoResize() {
        this.combineButtons();
        int max = 0;
        for (Entry<Integer, CMIGuiButton> one : this.buttons.entrySet()) {
            if (one.getKey() > max)
                max = one.getKey();
        }

        if (max < 9) {
            this.gUIRows = GUIRows.r1;
        } else if (max < 18) {
            this.gUIRows = GUIRows.r2;
        } else if (max < 27) {
            this.gUIRows = GUIRows.r3;
        } else if (max < 36) {
            this.gUIRows = GUIRows.r4;
        } else if (max < 45) {
            this.gUIRows = GUIRows.r5;
        } else {
            this.gUIRows = GUIRows.r6;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Inventory getInv() {
        if (inv == null)
            GUIManager.generateInventory(this);
        return inv;
    }

    public void setInv(Inventory inv) {
        this.inv = inv;
    }

    public String getTitle() {
        if (title == null)
            title = "";
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    public void setTitle(String title) {
        if (title.length() > 32) {
            title = title.substring(0, 31) + "~";
        }

        this.title = title;
    }

    public void updateTitle(String title) {
        setTitle(title);
        CMIReflections.updateInventoryTitle(player, this.title);
    }

    public HashMap<Integer, CMIGuiButton> getButtons() {
        combineButtons();
        return buttons;
    }

    public void setButtons(HashMap<Integer, CMIGuiButton> buttons) {
//	for (Entry<Integer, CMIGuiButton> one : buttons.entrySet()) {
//	    CMIGuiButton old = this.buttons.get(one.getKey());
//	    if (old == null)
//		old = one.getValue();
//	    buttons.put(one.getKey(), old);
//	}
        this.buttons = buttons;
    }

    public void clearButtons() {
        for (Entry<Integer, CMIGuiButton> one : getButtons().entrySet()) {
            this.inv.setItem(one.getKey(), null);
        }
        if (inv != null)
            this.inv.clear();
        buttons.clear();
        noSlotButtons.clear();
    }

    public CMIGui replaceButton(CMIGuiButton button) {
        button.updateLooks();
        if (button.getSlot() != null)
            this.buttons.remove(button.getSlot());
        if (this.getInv() != null) {
            this.getInv().setItem(button.getSlot(), button.getItem(this.getPlayer()));
        }
        return addButton(button, 54);
    }

    public CMIGui addButton(CMIGuiButton button) {
        button.updateLooks();
        return addButton(button, 54);
    }

    public CMIGui addButton(CMIGuiButton button, int maxSlot) {
        button.setGui(this);
        if (button.getSlot() != null && buttons.get(button.getSlot()) != null) {
            for (int ii = button.getSlot(); ii < maxSlot; ii++) {
                CMIGuiButton b = buttons.get(ii);
                if (b == null) {
                    buttons.put(ii, button);
                    break;
                }
            }
            return this;
        }

        if (button.getSlot() == null) {
            noSlotButtons.add(button);
            return this;
        }
        buttons.put(button.getSlot(), button);
        return this;
    }

    private void combineButtons() {
        for (CMIGuiButton button : noSlotButtons) {
            for (int ii = 0; ii < 54; ii++) {
                CMIGuiButton b = buttons.get(ii);
                if (b == null) {
                    buttons.put(ii, button);
                    break;
                }
            }
        }
        noSlotButtons.clear();
    }

    public void fillEmptyButtons() {
        fillEmptyButtons(null);
    }

    public void fillEmptyButtons(ItemStack item) {
        combineButtons();
        for (int i = 0; i < this.getInvSize().getFields(); i++) {
            if (this.buttons.containsKey(i))
                continue;
            addEmptyButton(item, i);
        }
    }

    public void updateButton(CMIGuiButton button) {
        if (inv == null || button.getSlot() == null || inv.getSize() < button.getSlot())
            return;
        this.inv.setItem(button.getSlot(), button.getItem(this.getPlayer()));
        buttons.put(button.getSlot(), button);
    }

    public void addEmptyButton(int slot) {
        addEmptyButton(null, slot);
    }

    public void addEmptyButton(ItemStack item, int slot) {
        ItemStack MiscInfo = item == null ? CMIMaterial.BLACK_STAINED_GLASS_PANE.newItemStack() : item.clone();
        ItemMeta MiscInfoMeta = MiscInfo.getItemMeta();
        MiscInfoMeta.setDisplayName(" ");
        MiscInfo.setItemMeta(MiscInfoMeta);
        CMIGuiButton button = new CMIGuiButton(slot, GUIFieldType.Locked, MiscInfo);
        addButton(button);
        updateButton(button);
    }

    public boolean isLocked(InvType type) {
        return lock.containsKey(type) ? (lock.get(type) == GUIFieldType.Locked) : false;
    }

    public void addLock(InvType type) {
        addLock(type, GUIFieldType.Locked);
    }

    public void addLock(InvType type, GUIFieldType lock) {
        this.lock.put(type, lock);
    }

    public boolean isPermLocked(InvType type) {
        return permLock.containsKey(type) ? (!this.player.hasPermission(permLock.get(type))) : true;
    }

    public void addPermLock(InvType type, String perm) {
        this.permLock.put(type, perm);
    }

    public CmiInventoryType getType() {
        return type;
    }

    public void setCmiInventoryType(CmiInventoryType type) {
        this.type = type;
    }

    public Object getWhatShows() {
        return whatShows;
    }

    public void setWhatShows(Object whatShows) {
        this.whatShows = whatShows;
    }

    public Integer getSlot(GUIButtonLocation place) {
        GUIRows size = this.getInvSize();
        int v = place.getCollumn() * 9;
        v = place.getCollumn() > 0 ? v - 1 : v;
        Integer value = (((place.getRow() * (size.getRows())) * 9) - 8) + v;
        value = place.getRow() > 0 ? value : value + 9;
        return value - 1;
    }

    public void onClose() {

    }

    public void onOpen() {

    }

    public void processClose() {
    }

//    public void addPagination(PageInfo pi, Object cmd, String pagePref) {
//	addPagination(pi,Residence.getInstance().getCommandManager().getLabel() + " " + cmd.getClass().getSimpleName(), pagePref);
//    }
//
//    public void addPagination(PageInfo pi, String cmd, String pagePref) {
//
//	if (!cmd.startsWith("/"))
//	    cmd = "/" + cmd;
////	String separator = this.getMsg(LC.info_fliperSimbols); 
//
//	int CurrentPage = pi.getCurrentPage();
//	int pageCount = pi.getTotalPages();
//	int totalEntries = pi.getTotalEntries();
//
//	if (pageCount == 1)
//	    return;
//	if (this.getInvSize().getRows() < 6)
//	    this.setInvSize(GUIRows.r6);
//
//	Integer prevSlot = this.getSlot(GUIButtonLocation.bottomLeft);
//	Integer nextSlot = this.getSlot(GUIButtonLocation.bottomRight);
//	Integer midSlot = this.getSlot(GUIButtonLocation.bottomRight) - 4;
//
//	String pagePrefix = pagePref == null ? "" : pagePref;
//
//	int NextPage = CurrentPage + 1;
//	NextPage = CurrentPage < pageCount ? NextPage : CurrentPage;
//	int Prevpage = CurrentPage - 1;
//	Prevpage = CurrentPage > 1 ? Prevpage : CurrentPage;
//
////	RawMessage rm = new RawMessage();
//
//	if (pageCount != 0) {
//
//	    for (int i = GUIRows.r5.getFields(); i < GUIRows.r6.getFields(); i++) {
//		this.getButtons().remove(i);
//	    }
//
//	    CMIGuiButton button = new CMIGuiButton(midSlot, CMIMaterial.LIGHT_GRAY_WOOL.newItemStack());
//	    button.setName(CMI.getInstance().getMsg(LC.info_pageCount, "[current]", CurrentPage, "[total]", pageCount));
//	    button.addLore(CMI.getInstance().getMsg(LC.info_pageCountHover, "[totalEntries]", totalEntries));
//	    this.addButton(button);
//
//	    if (this.getButtons().get(prevSlot) == null && CurrentPage > 1) {
//		button = new CMIGuiButton(prevSlot, CMIMaterial.WHITE_WOOL.newItemStack());
//		button.setName(CMI.getInstance().getMsg(LC.info_prevPageGui));
//		button.addLore(CMI.getInstance().getMsg(LC.info_pageCount, "[current]", CurrentPage, "[total]", pageCount));
//		button.addCommand(cmd + " " + pagePrefix + Prevpage, CommandType.silent);
//		this.addButton(button);
//	    }
//
//	    if (this.getButtons().get(nextSlot) == null && pageCount > CurrentPage) {
//		button = new CMIGuiButton(nextSlot, CMIMaterial.GRAY_WOOL.newItemStack());
//		button.setName(CMI.getInstance().getMsg(LC.info_nextPageGui));
//		button.addLore(CMI.getInstance().getMsg(LC.info_pageCount, "[current]", CurrentPage, "[total]", pageCount));
//		button.addCommand(cmd + " " + pagePrefix + NextPage, CommandType.silent);
//		this.addButton(button);
//	    }
//
//	}
//    }
//
//    public void addPagination(PageInfo pi, String pagePref, Class<?> cls, String... argsS) {
//
////	if (!cmd.startsWith("/"))
////	    cmd = "/" + cmd;
////	String separator = this.getMsg(LC.info_fliperSimbols); 
//
//	String arg = "";
//	for (String one : argsS) {
//	    if (!arg.isEmpty())
//		arg += " ";
//	    arg += one;
//	}
//	String args = arg;
//
//	int CurrentPage = pi.getCurrentPage();
//	int pageCount = pi.getTotalPages();
//	int totalEntries = pi.getTotalEntries();
//
//	if (pageCount == 1)
//	    return;
//	if (this.getInvSize().getRows() < 6)
//	    this.setInvSize(GUIRows.r6);
//
//	Integer prevSlot = this.getSlot(GUIButtonLocation.bottomLeft);
//	Integer nextSlot = this.getSlot(GUIButtonLocation.bottomRight);
//	Integer midSlot = this.getSlot(GUIButtonLocation.bottomRight) - 4;
//
//	String pagePrefix = pagePref == null ? "" : pagePref;
//
////	RawMessage rm = new RawMessage();
//
//	if (pageCount != 0) {
//
//	    for (int i = GUIRows.r5.getFields(); i < GUIRows.r6.getFields(); i++) {
//		this.getButtons().remove(i);
//	    }
//
//	    CMIGuiButton button = new CMIGuiButton(midSlot, CMIMaterial.LIGHT_GRAY_WOOL.newItemStack());
//	    button.setName(CMI.getInstance().getMsg(LC.info_pageCount, "[current]", CurrentPage, "[total]", pageCount));
//	    button.addLore(CMI.getInstance().getMsg(LC.info_pageCountHover, "[totalEntries]", totalEntries));
//	    this.addButton(button);
//
//	    if (this.getButtons().get(prevSlot) == null && CurrentPage > 1) {
//		button = new CMIGuiButton(prevSlot, CMIMaterial.WHITE_WOOL.newItemStack()) {
//		    @Override
//		    public void click(GUIClickType type) {
//
//			int Prevpage = CurrentPage - 1;
//			Prevpage = CurrentPage > 1 ? Prevpage : CurrentPage;
//			CMI.getInstance().getCommandManager().performCMICommand(player, cls, (args.isEmpty() ? "" : args + " ") + pagePrefix + Prevpage);
//
//		    }
//		};
//		button.setName(CMI.getInstance().getMsg(LC.info_prevPageGui));
//		button.addLore(CMI.getInstance().getMsg(LC.info_pageCount, "[current]", CurrentPage - 1, "[total]", pageCount));
//		this.addButton(button);
//	    }
//
//	    if (this.getButtons().get(nextSlot) == null && pageCount > CurrentPage) {
//		button = new CMIGuiButton(nextSlot, CMIMaterial.GRAY_WOOL.newItemStack()) {
//		    @Override
//		    public void click(GUIClickType type) {
//
//			int NextPage = CurrentPage + 1;
//			NextPage = CurrentPage < pageCount ? NextPage : CurrentPage;
//			CMI.getInstance().getCommandManager().performCMICommand(player, cls, (args.isEmpty() ? "" : args + " ") + pagePrefix + NextPage);
//
//		    }
//		};
//		button.setName(CMI.getInstance().getMsg(LC.info_nextPageGui));
//		button.addLore(CMI.getInstance().getMsg(LC.info_pageCount, "[current]", CurrentPage + 1, "[total]", pageCount));
////		button.addCommand(cmd + " " + pagePrefix + NextPage, CommandType.silent);
//		this.addButton(button);
//	    }
//
//	}
//    }

    public boolean isAllowShift() {
        return allowShift;
    }

    public void setAllowShift(boolean allowShift) {
        this.allowShift = allowShift;
    }

    public Object getTempData() {
        return tempData;
    }

    public void setTempData(Object tempData) {
        this.tempData = tempData;
    }

    public boolean isAllowPickUpAll() {
        return allowPickUpAll;
    }

    public void setAllowPickUpAll(boolean allowPickUpAll) {
        this.allowPickUpAll = allowPickUpAll;
    }
}
