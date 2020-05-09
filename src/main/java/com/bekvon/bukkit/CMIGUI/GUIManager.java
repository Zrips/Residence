package com.bekvon.bukkit.CMIGUI;

import com.bekvon.bukkit.cmiLib.CMIReflections;
import com.bekvon.bukkit.residence.Residence;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;

public class GUIManager {

    public final static String CMIGUIIcon = "CMIGUIIcon";
    public final static String LIProtection = "LIProtection";
    private static HashMap<UUID, CMIGui> map = new HashMap<UUID, CMIGui>();

    static {
        registerListener();
    }

    public static void registerListener() {
        Residence.getInstance().getServer().getPluginManager().registerEvents(new GUIListener(Residence.getInstance()), Residence.getInstance());
    }

    public static GUIClickType getClickType(boolean left, boolean shift, InventoryAction action) {

        if (!left && !shift && (action.equals(InventoryAction.NOTHING) || action.equals(InventoryAction.CLONE_STACK)))
            return GUIClickType.MiddleMouse;

        if (left && !shift) {
            return GUIClickType.Left;
        } else if (left && shift) {
            return GUIClickType.LeftShift;
        } else if (!left && !shift) {
            return GUIClickType.Right;
        } else {
            return GUIClickType.RightShift;
        }
    }

    public static boolean processClick(final Player player, List<Integer> buttons, final GUIClickType clickType) {
        CMIGui gui = map.get(player.getUniqueId());
        if (gui == null)
            return false;
        int clicks = 0;
        for (Integer one : buttons) {

            final CMIGuiButton button = gui.getButtons().get(one);

            if (button == null)
                continue;
            clicks++;
            boolean canClick = true;

            if (canClick) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Residence.getInstance(), new Runnable() {
                    @Override
                    public void run() {

                        for (GUIButtonCommand oneC : button.getCommands(clickType)) {
                            GUIManager.performCommand(player, oneC.getCommand(), oneC.getVis());
                        }
                    }
                }, 1);
            }

            button.click();
            button.click(clickType);
//	    button.updateLooks();
//	    button.update(gui);

            if (button.isCloseInv())
                player.closeInventory();

            if (!button.getCommands(clickType).isEmpty())
                break;
        }
        if (clicks == 0)
            gui.outsideClick(clickType);

        return false;
    }

    public static void performCommand(CommandSender sender, String command, CommandType type) {
        if (sender instanceof Player) {
            performCommand((Player) sender, command, type);
        } else {
            ServerCommandEvent event = new ServerCommandEvent(sender, command.startsWith("/") ? command : "/" + command);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), event.getCommand().startsWith("/") ? event.getCommand().substring(1, event.getCommand().length()) : event.getCommand());
            }
            if (!type.equals(CommandType.silent))
                Bukkit.getLogger().log(Level.INFO, sender.getName() + " issued " + type.name() + " command: /" + command);
        }
    }

    public static void performCommand(Player player, String command, CommandType type) {
        if (player == null) {
            Residence.getInstance().consoleMessage("&cCant perform command (" + command + "). Player is NULL");
            return;
        }
        if (command == null) {
            Residence.getInstance().consoleMessage("&cCant perform command (" + command + "). Command is NULL");
            return;
        }
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command.startsWith("/") ? command : "/" + command);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            player.performCommand(event.getMessage().startsWith("/") ? event.getMessage().substring(1, event.getMessage().length()) : event.getMessage());
        }
        if (!type.equals(CommandType.silent))
            Bukkit.getLogger().log(Level.INFO, player.getName() + " issued " + type.name() + " command: /" + command);
    }

    public static boolean isLockedPart(Player player, List<Integer> buttons) {
        CMIGui gui = map.get(player.getUniqueId());
        if (gui == null)
            return false;

        int size = gui.getInv().getSize();
        int mainInvMax = size + 36 - 9;
        int quickbar = size + 36;

        for (Integer one : buttons) {
            if (one > quickbar || quickbar < 0)
                continue;
            if (one < size && (gui.isLocked(InvType.Gui) && gui.isPermLocked(InvType.Gui))) {
                return true;
            } else if (one >= size && one < mainInvMax && (gui.isLocked(InvType.Main) && gui.isPermLocked(InvType.Main))) {
                return true;
            } else if (one >= mainInvMax && one < quickbar && ((gui.isLocked(InvType.Quickbar) && gui.isPermLocked(InvType.Quickbar)) || (gui.isLocked(InvType.Main) && gui.isPermLocked(InvType.Main)))) {
                return true;
            }
        }

        return false;
    }

    public static boolean canClick(Player player, List<Integer> buttons) {
        try {
            CMIGui gui = map.get(player.getUniqueId());
            if (gui == null)
                return true;

            for (Integer one : buttons) {
                CMIGuiButton button = gui.getButtons().get(one);
                if (button == null)
                    continue;
                if (button.getFieldType() == GUIFieldType.Locked)
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static CMIGui getGui(Player player) {
        return map.get(player.getUniqueId());
    }

    public static boolean isOpenedGui(Player player) {
        CMIGui gui = map.get(player.getUniqueId());
        if (gui == null)
            return false;
        if (player.getOpenInventory() == null)
            return false;
//	if (!player.getOpenInventory().getTopInventory().equals(gui.getInv()))
//	    return false;
        return true;
    }

    public static boolean removePlayer(Player player) {
        CMIGui removed = map.remove(player.getUniqueId());
        if (removed == null)
            return false;
        if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory().equals(removed.getInv()))
            player.closeInventory();

        removed.processClose();
        removed.onClose();

        return true;
    }

    public static void generateInventory(CMIGui gui) {
        Inventory GuiInv = null;
        if (gui.getInvSize() != null) {
            GuiInv = Bukkit.createInventory(null, gui.getInvSize().getFields(), gui.getTitle());
        } else {
            GuiInv = Bukkit.createInventory(null, gui.getInvType(), gui.getTitle());
        }

        if (GuiInv == null)
            return;

        for (Entry<Integer, CMIGuiButton> one : gui.getButtons().entrySet()) {
            if (one.getKey() > GuiInv.getSize())
                continue;
            try {
                ItemStack item = one.getValue().getItem(gui.getPlayer());
                item = item == null ? null : item.clone();
                if (item != null && one.getValue().isLocked()) {
                    item = CMIReflections.setNbt(item, CMIGUIIcon, LIProtection);
//		    if (gui.getPlayer().getName().equals("Zrips"))
//			one.getValue().unlockField();
                }
                GuiInv.setItem(one.getKey(), item);
            } catch (ArrayIndexOutOfBoundsException e) {
//		e.printStackTrace();
                break;
            }
        }
        gui.setInv(GuiInv);
    }

    public static void openGui(CMIGui gui) {

        Player player = gui.getPlayer();
        if (player.isSleeping())
            return;
        CMIGui oldGui = null;
        if (isOpenedGui(player)) {
            oldGui = getGui(player);
            if (!gui.isSimilar(oldGui)) {
                oldGui = null;
            }
        }
        if (oldGui == null) {
            generateInventory(gui);
            player.closeInventory();
            player.openInventory(gui.getInv());
            gui.onOpen();
            map.put(player.getUniqueId(), gui);
        } else {
            updateContent(gui);
        }

    }

    public static void updateContent(CMIGui gui) {
        Player player = gui.getPlayer();
        if (player.getOpenInventory() == null || player.getOpenInventory().getTopInventory() == null) {
            player.closeInventory();
        }
        CMIReflections.updateInventoryTitle(player, gui.getTitle());
        player.getOpenInventory().getTopInventory().setContents(gui.getInv().getContents());
        gui.setInv(player.getOpenInventory().getTopInventory());
        map.put(player.getUniqueId(), gui);
    }

    public void closeAll() {
        for (Entry<UUID, CMIGui> one : map.entrySet()) {
            Player player = Bukkit.getPlayer(one.getKey());
            if (player == null)
                continue;
            player.closeInventory();
        }
    }

    public enum GUIButtonLocation {
        topLeft(0, 0), topRight(0, 1), bottomLeft(1, 0), bottomRight(1, 1);

        private Integer row;
        private Integer collumn;

        GUIButtonLocation(Integer row, Integer collumn) {
            this.collumn = collumn;
            this.row = row;
        }

        public Integer getRow() {
            return row;
        }

        public Integer getCollumn() {
            return collumn;
        }

    }

    public enum GUIRows {
        r1(1), r2(2), r3(3), r4(4), r5(5), r6(6);

        private int rows;

        GUIRows(int rows) {
            this.rows = rows;
        }

        public static GUIRows getByRows(Integer rows) {
            if (rows > 9)
                rows = rows / 9;
            for (GUIRows one : GUIRows.values()) {
                if (one.getRows().equals(rows))
                    return one;
            }
            return GUIRows.r6;
        }

        public Integer getFields() {
//	    if (Version.isCurrentEqualOrHigher(Version.v1_14_R1)) {
//		return rows * 9 < 27 ? 27 : rows * 9;
//	    }
            return rows * 9;
        }

        public Integer getRows() {
            return rows;
        }
    }

    public enum GUIFieldType {
        Free, Locked
    }

    public enum InvType {
        Gui, Main, Quickbar
    }

//    public void updateInventory(CMIGui old, CMIGui gui) {
//
//	Inventory GuiInv = gui.getInv();
//	if (GuiInv == null)
//	    return;
//
//	plugin.getNMS().updateInventoryTitle(gui.getPlayer(), gui.getTitle());
//
//	for (Entry<Integer, CMIGuiButton> one : gui.getButtons().entrySet()) {
//	    if (one.getKey() > GuiInv.getSize())
//		continue; 
//	    GuiInv.setItem(one.getKey(), one.getValue().getItem());
//	}
//	gui.setInv(GuiInv);
//    }

    public enum CmiInventoryType {
        regular, SavedInv, EditableInv, RecipeCreator, ArmorStandEditor, ArmorStandCopy, EntityInventoryEditor, Recipes, SellHand
    }

    public enum GUIClickType {
        Left, LeftShift, Right, RightShift, MiddleMouse
    }

}
