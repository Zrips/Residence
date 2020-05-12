package com.bekvon.bukkit.CMIGUI;

import com.bekvon.bukkit.cmiLib.CMIReflections;
import com.bekvon.bukkit.residence.Residence;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GUIListener implements Listener {
    Residence plugin;
    private HashMap<UUID, Long> LastClick = new HashMap<UUID, Long>();

    public GUIListener(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onNormalInventoryClose(InventoryCloseEvent event) {

        final Player player = (Player) event.getPlayer();
        if (GUIManager.isOpenedGui(player)) {
            if (GUIManager.removePlayer(player)) {
                player.updateInventory();
                clearIconItems(player);
            }
        }
    }

    private void clearIconItems(Player player) {
        for (ItemStack one : player.getInventory().getContents()) {
            Object res = CMIReflections.getNbt(one, GUIManager.CMIGUIIcon);
            if (res == null || !(res instanceof String) || !((String) res).equalsIgnoreCase(GUIManager.LIProtection))
                continue;
            player.getInventory().remove(one);
        }
    }

    private boolean canClickByTimer(UUID uuid) {
        Long time = LastClick.get(uuid);
        if (time == null) {
            LastClick.put(uuid, System.currentTimeMillis());
            return true;
        }

        if (time + 51 > System.currentTimeMillis()) {
            LastClick.put(uuid, System.currentTimeMillis());
            return false;
        }

        LastClick.put(uuid, System.currentTimeMillis());
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(final InventoryClickEvent event) {

        if (event.isCancelled()) {
            if (!event.getWhoClicked().getGameMode().toString().equalsIgnoreCase("Spectator"))
                return;
        }
        final Player player = (Player) event.getWhoClicked();

        if (!GUIManager.isOpenedGui(player))
            return;

        CMIGui gui = GUIManager.getGui(player);

        if (event.getClick() == ClickType.DOUBLE_CLICK || event.getHotbarButton() != -1) {
            event.setCancelled(true);
            return;
        }

        if (!gui.isAllowShift() && event.isShiftClick())
            event.setCancelled(true);

        if (!event.getAction().equals(InventoryAction.PICKUP_ALL) &&
                !event.getAction().equals(InventoryAction.PICKUP_ONE) &&
                !event.getAction().equals(InventoryAction.PICKUP_HALF) &&
                !event.getAction().equals(InventoryAction.PICKUP_SOME) &&
                !event.getAction().equals(InventoryAction.PLACE_ALL) &&
                !event.getAction().equals(InventoryAction.PLACE_ONE) &&
                !event.getAction().equals(InventoryAction.PLACE_SOME) &&
                !gui.isAllowShift() && !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
            event.setCancelled(true);

        if (!gui.isAllowPickUpAll() && !canClickByTimer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        final List<Integer> buttons = new ArrayList<Integer>();
        buttons.add(event.getRawSlot());
        if (!GUIManager.canClick(player, buttons)) {
            event.setCancelled(true);
        }

        if (GUIManager.isLockedPart(player, buttons))
            event.setCancelled(true);

//	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//	    @Override
//	    public void run() {
        InventoryAction action = event.getAction();
        GUIManager.processClick(player, buttons, GUIManager.getClickType(event.isLeftClick(), event.isShiftClick(), action));
//	    }
//	}, 1);

//	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//	    @Override
//	    public void run() {
//		clearIconItems(player);
//	    }
//	}, 1);

    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onInventoryClicks(final InventoryClickEvent event) {
//	final Player player = (Player) event.getWhoClicked();
//    }
//
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onInventoryClicks(final InventoryDragEvent event) {
//	final Player player = (Player) event.getWhoClicked();
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryMove(final InventoryDragEvent event) {
        if (event.isCancelled()) {
            if (!event.getWhoClicked().getGameMode().toString().equalsIgnoreCase("Spectator"))
                return;
        }
        final Player player = (Player) event.getWhoClicked();

        if (!GUIManager.isOpenedGui(player))
            return;

        CMIGui gui = GUIManager.getGui(player);

        if (!gui.isAllowPickUpAll() && !canClickByTimer(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        final List<Integer> buttons = new ArrayList<Integer>(event.getRawSlots());
        if (!GUIManager.canClick(player, buttons))
            event.setCancelled(true);

        if (GUIManager.isLockedPart(player, buttons))
            event.setCancelled(true);

//	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//	    @Override
//	    public void run() {

        GUIManager.processClick(player, buttons, GUIManager.getClickType(true, false, null));
//	    }
//	}, 1);
    }

}
