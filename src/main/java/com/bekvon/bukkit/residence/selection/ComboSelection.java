package com.bekvon.bukkit.residence.selection;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Container.CMIVectorInt3D;
import net.Zrips.CMILib.Cuboids.CMIBlockArea;
import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ComboSelection implements Listener {

    private HashMap<UUID, selectorOptions> list = new HashMap<UUID, selectorOptions>();
    private HashMap<UUID, selectorOptions> disabledList = new HashMap<UUID, selectorOptions>();
    private Residence plugin;

    public ComboSelection(Residence residence) {
        this.plugin = residence;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public boolean toggleAutoSelection(Player player) {
        boolean state = !list.containsKey(player.getUniqueId());
        return toggleAutoSelection(player, state);
    }

    public boolean toggleAutoSelection(Player player, boolean state) {
        return toggleAutoSelection(player.getUniqueId(), state);
    }

    public boolean toggleAutoSelection(UUID uuid, boolean state) {
        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(uuid);
        if (state && rPlayer.isOnline()) {
            selectorOptions old = disabledList.remove(uuid);
            if (old == null) {
                old = list.get(uuid);
                if (old == null)
                    old = new selectorOptions(uuid);
            }

            old.setEnabled(true);

            list.put(uuid, old);
            return true;
        } else {
            selectorOptions old = remove(uuid);
            if (old != null) {
                old.setEnabled(false);
                disabledList.put(uuid, old);
            }
            return false;
        }
    }

    public @NotNull selectorOptions getOrCreate(Player player) {
        selectorOptions old = list.get(player.getUniqueId());
        if (old != null)
            return old;

        old = disabledList.get(player.getUniqueId());

        if (old != null)
            return old;

        old = new selectorOptions(player.getUniqueId());

        disabledList.put(player.getUniqueId(), old);

        return old;
    }

    public @Nullable selectorOptions getAutoSelection(UUID uuid) {
        return list.get(uuid);
    }

    public HashMap<UUID, selectorOptions> getList() {
        return list;
    }

    public boolean has(UUID uuid) {
        return list.containsKey(uuid);
    }

    public selectorOptions remove(UUID uuid) {
        return list.remove(uuid);
    }

    public boolean isActive() {
        return !list.isEmpty();
    }

    public void updateByMove(Player player) {
        updateByMove(player, player.getLocation());
    }

    public void updateByMove(Player player, Location targetLoc) {

        selectorOptions autoSelector = getAutoSelection(player.getUniqueId());
        if (autoSelector == null)
            return;

        ResidencePlayer rplayer = ResidencePlayer.get(player);

        int curentTime = (int) (System.currentTimeMillis() - autoSelector.getTime()) / 1000;

        if (curentTime > 3 * 60) {
            autoSelector.setMove(false);
            lm.Select_AutoMoveDisabled.sendMessage(player);
            return;
        }

        Location cloc = targetLoc.clone();

        CMIBlockWorldArea currentArea = plugin.getSelectionManager().getSelectionArea(player);

        if (currentArea == null || !player.getWorld().equals(currentArea.getWorld().getWorld())) {
            plugin.getSelectionManager().placeLoc(player, player.getEyeLocation(), player.getLocation(), true);
            return;
        }

        CMIVectorInt3D loc1 = currentArea != null ? currentArea.getLowPoint() : null;
        CMIVectorInt3D loc2 = currentArea != null ? currentArea.getHighPoint() : null;

        if (loc1 == null) {
            plugin.getSelectionManager().placeLoc1(player, cloc, false);
            return;
        }

        if (loc2 == null) {
            plugin.getSelectionManager().placeLoc2(player, cloc, true);
            return;
        }

        CMIBlockArea area = new CMIBlockArea(loc1, loc2);

        CMIVectorInt3D low = area.getLowPoint();
        CMIVectorInt3D high = area.getHighPoint();

        CMIVectorInt3D newLow = low.clone();
        CMIVectorInt3D newHigh = high.clone();

        // Expand the candidate cuboid toward the target.
        if (cloc.getBlockX() < newLow.getX())
            newLow.setX(cloc.getBlockX());

        if (cloc.getBlockY() <= newLow.getY())
            newLow.setY(cloc.getBlockY() - 1);

        if (cloc.getBlockZ() < newLow.getZ())
            newLow.setZ(cloc.getBlockZ());

        if (cloc.getBlockX() > newHigh.getX())
            newHigh.setX(cloc.getBlockX());

        if (cloc.getBlockY() >= newHigh.getY())
            newHigh.setY(cloc.getBlockY() + 1);

        if (cloc.getBlockZ() > newHigh.getZ())
            newHigh.setZ(cloc.getBlockZ());

        // Create the candidate selection.
        CMIBlockWorldArea candidateArea = new CMIBlockWorldArea(newLow.toLocation(player.getWorld()), newHigh.toLocation(player.getWorld()));

        // Constrain the candidate against the existing selection and player limits.
        CMIBlockWorldArea adjustedArea = null;
        if (autoSelector.isGroupLimit())
            adjustedArea = constrainSelection(currentArea, candidateArea, rplayer);
        else
            adjustedArea = candidateArea;

        if (adjustedArea == null)
            return;

        CMIVectorInt3D adjustedLow = adjustedArea.getLowPoint();
        CMIVectorInt3D adjustedHigh = adjustedArea.getHighPoint();

        if (adjustedLow.getX() == low.getX()
                && adjustedLow.getY() == low.getY()
                && adjustedLow.getZ() == low.getZ()
                && adjustedHigh.getX() == high.getX()
                && adjustedHigh.getY() == high.getY()
                && adjustedHigh.getZ() == high.getZ()) {
            return;
        }

        plugin.getSelectionManager().placeLoc1(player, adjustedHigh.toLocation(player.getWorld()), false);
        plugin.getSelectionManager().placeLoc2(player, adjustedLow.toLocation(player.getWorld()), true);
        plugin.getSelectionManager().showSelectionInfoInActionBar(player);
    }

    public static CMIBlockWorldArea constrainSelection(CMIBlockWorldArea existing, CMIBlockWorldArea candidate, ResidencePlayer rplayer) {
        if (existing == null || candidate == null)
            return null;

        CMIVectorInt3D oldLow = existing.getLowPoint();
        CMIVectorInt3D oldHigh = existing.getHighPoint();

        CMIVectorInt3D low = candidate.getLowPoint().clone();
        CMIVectorInt3D high = candidate.getHighPoint().clone();

        int maxX = rplayer.getMaxX();
        int maxZ = rplayer.getMaxZ();
        int maxY = rplayer.getGroup().getMaxYSize();

        boolean ignoreY = SelectionManager.isIgnoreY();

        // X axis
        if (maxX > 0 && high.getX() - low.getX() + 1 > maxX) {
            boolean expandedLow = low.getX() < oldLow.getX();
            boolean expandedHigh = high.getX() > oldHigh.getX();

            if (expandedLow && !expandedHigh) {
                low.setX(high.getX() - maxX + 1);
            } else if (expandedHigh && !expandedLow) {
                high.setX(low.getX() + maxX - 1);
            } else if (expandedLow) {
                // Both sides expanded: preserve the high boundary.
                low.setX(high.getX() - maxX + 1);
            } else {
                high.setX(low.getX() + maxX - 1);
            }
        }

        // Z axis
        if (maxZ > 0 && high.getZ() - low.getZ() + 1 > maxZ) {
            boolean expandedLow = low.getZ() < oldLow.getZ();
            boolean expandedHigh = high.getZ() > oldHigh.getZ();

            if (expandedLow && !expandedHigh) {
                low.setZ(high.getZ() - maxZ + 1);
            } else if (expandedHigh && !expandedLow) {
                high.setZ(low.getZ() + maxZ - 1);
            } else if (expandedLow) {
                low.setZ(high.getZ() - maxZ + 1);
            } else {
                high.setZ(low.getZ() + maxZ - 1);
            }
        }

        // Y axis
        if (!ignoreY && maxY > 0 && high.getY() - low.getY() + 1 > maxY) {
            boolean expandedLow = low.getY() < oldLow.getY();
            boolean expandedHigh = high.getY() > oldHigh.getY();

            if (expandedLow && !expandedHigh) {
                low.setY(high.getY() - maxY + 1);
            } else if (expandedHigh && !expandedLow) {
                high.setY(low.getY() + maxY - 1);
            } else if (expandedLow) {
                low.setY(high.getY() - maxY + 1);
            } else {
                high.setY(low.getY() + maxY - 1);
            }
        }

        return new CMIBlockWorldArea(low.toLocation(candidate.getWorld().getWorld()), high.toLocation(candidate.getWorld().getWorld()));
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {

        if (!isActive())
            return;

        @Nullable
        selectorOptions auto = getAutoSelection(event.getPlayer().getUniqueId());

        if (auto == null)
            return;

        if (!auto.isQuickBar())
            return;

        Player player = event.getPlayer();

        if (!auto.isEnabled()) {
            toggleAutoSelection(player, false);
            return;
        }

        int difference = event.getNewSlot() - event.getPreviousSlot();

        if (difference > 4)
            difference -= 9;
        else if (difference < -4)
            difference += 9;

        int slotsMoved = -difference;

        CMIBlockWorldArea existingArea = plugin.getSelectionManager().getSelectionArea(player);

        if (existingArea == null || !player.getWorld().equals(existingArea.getWorld().getWorld())) {
            plugin.getSelectionManager().placeLoc(player, player.getEyeLocation(), player.getLocation(), true);
            return;
        }

        CMIBlockWorldArea candidateArea = plugin.getSelectionManager().resize(existingArea.clone(), player, false, slotsMoved);

        if (candidateArea == null)
            return;

        CMIBlockWorldArea adjustedArea = candidateArea;

        if (auto.isGroupLimit()) {
            adjustedArea = constrainSelection(existingArea, candidateArea, ResidencePlayer.get(player));
        }

        if (adjustedArea == null)
            return;

        plugin.getSelectionManager().updateLocations(player, adjustedArea.getLowPoint().toLocation(player.getWorld()), adjustedArea.getHighPoint().toLocation(player.getWorld()));
        plugin.getSelectionManager().showSelectionInfoInActionBar(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        toggleAutoSelection(event.getPlayer(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isActive())
            return;

        Player player = event.getPlayer();

        selectorOptions auto = getAutoSelection(player.getUniqueId());

        if (auto == null)
            return;

        if (!auto.isAim())
            return;

        if (!auto.isEnabled()) {
            toggleAutoSelection(player, false);
            return;
        }

        Block block = Utils.getTargetBlock(player, 50, true);

        if (block == null || CMIMaterial.isAir(block.getType()))
            block = Utils.getTargetBlock(player, 25, true);

        if (block == null)
            return;

        CMIBlockWorldArea area = plugin.getSelectionManager().getSelectionArea(player);

        if (area == null) {
            plugin.getSelectionManager().placeLoc(player, block.getLocation(), block.getLocation(), true);
            return;
        }

        CMIVectorInt3D vLow = area.getLowPoint().clone();
        CMIVectorInt3D vHigh = area.getHighPoint().clone();

        CMIVectorInt3D vBlock = new CMIVectorInt3D(block.getLocation());

        if (Math.abs(vBlock.getX() - vLow.getX()) <= Math.abs(vBlock.getX() - vHigh.getX())) {
            vLow.setX(vBlock.getX());
        } else {
            vHigh.setX(vBlock.getX());
        }

        if (Math.abs(vBlock.getY() - vLow.getY()) <= Math.abs(vBlock.getY() - vHigh.getY())) {
            vLow.setY(vBlock.getY());
        } else {
            vHigh.setY(vBlock.getY());
        }

        if (Math.abs(vBlock.getZ() - vLow.getZ()) <= Math.abs(vBlock.getZ() - vHigh.getZ())) {
            vLow.setZ(vBlock.getZ());
        } else {
            vHigh.setZ(vBlock.getZ());
        }

        CMIBlockWorldArea candidateArea = new CMIBlockWorldArea(vLow.toLocation(player.getWorld()), vHigh.toLocation(player.getWorld()));

        CMIBlockWorldArea adjustedArea = candidateArea;

        if (auto.isGroupLimit()) {
            adjustedArea = constrainSelection(area, candidateArea, ResidencePlayer.get(player));
        }

        if (adjustedArea == null)
            return;

        plugin.getSelectionManager().updateLocations(player, adjustedArea.getLowPoint().toLocation(player.getWorld()), adjustedArea.getHighPoint().toLocation(player.getWorld()));
        plugin.getSelectionManager().showSelectionInfoInActionBar(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {

        if (!isActive())
            return;

        @Nullable
        selectorOptions auto = getAutoSelection(event.getPlayer().getUniqueId());

        if (auto == null)
            return;

        if (!auto.isMove())
            return;

        if (!auto.isEnabled()) {
            this.toggleAutoSelection(event.getPlayer(), false);
            return;
        }

        CMIScheduler.runTaskAsynchronously(plugin, () -> updateByMove(event.getPlayer()));
    }
}
