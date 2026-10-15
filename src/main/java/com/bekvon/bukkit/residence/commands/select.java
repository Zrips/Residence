package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.selectorOptions;

import net.Zrips.CMILib.Container.CMIVectorInt3D;
import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.GUI.CMIGui;
import net.Zrips.CMILib.GUI.CMIGuiButton;
import net.Zrips.CMILib.GUI.GUIManager.GUIClickType;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Locale.LC;

public class select implements cmd {

    private enum Action {
        coords, size, cost, vert, sky, bedrock, expand, contract, shift, chunk, move, aim, quickbar, considerlimits, togglecombo, residence, worldedit, clear, options, outward;

        public static Action get(String name) {
            for (Action a : Action.values()) {
                if (a.name().equalsIgnoreCase(name)) {
                    return a;
                }
            }
            return null;
        }
    }

    @Override
    @CommandAnnotation(simple = true, priority = 1300)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        PermissionGroup group = rPlayer.getGroup();

        Action action = null;
        String value = null;
        String subvalue = null;
        for (String one : args) {
            if (action == null) {
                action = Action.get(one);
                if (action != null) {
                    continue;
                }
            }
            if (value == null) {
                value = one;
                continue;
            }
            subvalue = one;
        }

        if (!group.selectCommandAccess() && !resadmin) {
            lm.Select_Disabled.sendMessage(sender);
            return true;
        }

        if (!group.canCreateResidences() && rPlayer.getMaxSubzones() <= 0 && !resadmin) {
            lm.Select_Disabled.sendMessage(sender);
            return true;
        }

        if (!ResPerm.create.hasPermission(player) && !ResPerm.select.hasPermission(player) && !resadmin) {
            lm.Select_Disabled.sendMessage(sender);
            return true;
        }

        if (action == null) {
            try {
                plugin.getSelectionManager().selectBySize(player, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                return true;
            } catch (Exception ex) {
            }
            action = Action.size;
        }

        switch (action) {
        case size:
        case cost:
            if (!plugin.getSelectionManager().hasPlacedBoth(player) && !plugin.getSelectionManager().worldEdit(player))
                return false;

            try {
                plugin.getSelectionManager().showSelectionInfo(player);
            } catch (Exception ex) {
                Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            return true;
        case vert:
            plugin.getSelectionManager().vert(player, resadmin);
            plugin.getSelectionManager().afterSelectionUpdate(player, true);
            return true;
        case clear:
            plugin.getSelectionManager().clearSelection(player);
            return true;
        case sky:
            plugin.getSelectionManager().sky(player, resadmin);
            plugin.getSelectionManager().afterSelectionUpdate(player, true);
            return true;
        case bedrock:
            plugin.getSelectionManager().bedrock(player, resadmin);
            plugin.getSelectionManager().afterSelectionUpdate(player, true);
            return true;
        case coords:
            lm.General_Separator.sendMessage(sender);

            if (!plugin.getSelectionManager().hasPlacedBoth(player)) {
                lm.Select_Points.sendMessage(sender);
                return true;
            }

            Location playerLoc1 = plugin.getSelectionManager().getPlayerLoc1(player);
            if (playerLoc1 != null) {
                lm.Select_Primary.sendMessage(sender, lm.General_CoordsTop.getMessage(playerLoc1.getBlockX(), playerLoc1.getBlockY(), playerLoc1.getBlockZ()));
            }
            Location playerLoc2 = plugin.getSelectionManager().getPlayerLoc2(player);
            if (playerLoc2 != null) {
                lm.Select_Secondary.sendMessage(sender, lm.General_CoordsBottom.getMessage(playerLoc2.getBlockX(), playerLoc2.getBlockY(), playerLoc2.getBlockZ()));
            }
            lm.General_Separator.sendMessage(sender);
            plugin.getSelectionManager().afterSelectionUpdate(player, false);
            return true;

        case chunk:
            plugin.getSelectionManager().getSelection(player).selectChunk();
            plugin.getSelectionManager().afterSelectionUpdate(player, true);
            return true;
        case worldedit:
            if (plugin.getSelectionManager().worldEdit(player)) {
                lm.Select_Success.sendMessage(sender);
                plugin.getSelectionManager().afterSelectionUpdate(player, false);
            }
            return true;
        case expand:
        case contract:
        case shift:

            int amount = 0;
            try {
                amount = Integer.parseInt(value);
            } catch (Exception ex) {
                lm.Invalid_Amount.sendMessage(sender);
                return true;
            }

            switch (action) {
            case expand:
                plugin.getSelectionManager().modify(player, false, amount);
                return true;
            case contract:
                plugin.getSelectionManager().contract(player, amount);
                return true;
            case shift:
                if (amount > 100)
                    amount = 100;
                if (amount < -100)
                    amount = -100;
                plugin.getSelectionManager().modify(player, true, amount);
                return true;
            }

            break;

        case outward:

            amount = 0;

            if (value != null) {
                try {
                    amount = Integer.parseInt(value);
                    value = null;
                } catch (Exception ex) {
                }
            }

            if (amount == 0 && subvalue != null) {
                try {
                    amount = Integer.parseInt(subvalue);
                    subvalue = null;
                } catch (Exception ex) {
                }
            }

            if (amount == 0) {
                amount = 1;
            }

            Player target = player;
            if (value != null) {
                if (!ResPerm.select_auto_others.hasPermission(player, true)) {
                    return true;
                }
                target = Bukkit.getPlayer(value);
                if (target == null) {
                    lm.General_NotOnline.sendMessage(sender);
                    return true;
                }
            }

            Location loc = player.getLocation();

            CMIBlockWorldArea selectedArea = plugin.getSelectionManager().getSelectionArea(player);

            CMIVectorInt3D lowPoint = selectedArea != null ? selectedArea.getLowPoint().add(new CMIVectorInt3D(-amount, -amount, -amount))
                    : new CMIVectorInt3D(loc.getBlockX() - amount, loc.getBlockY() - amount, loc.getBlockZ() - amount);
            CMIVectorInt3D highPoint = selectedArea != null ? selectedArea.getHighPoint().add(new CMIVectorInt3D(amount, amount, amount))
                    : new CMIVectorInt3D(loc.getBlockX() + amount, loc.getBlockY() + amount, loc.getBlockZ() + amount);

            plugin.getSelectionManager().placeLoc(player, lowPoint.toLocation(player.getWorld()), highPoint.toLocation(player.getWorld()), true);

            break;
        case options:

            target = player;
            if (value != null) {
                if (!ResPerm.select_auto_others.hasPermission(player, true)) {
                    return true;
                }
                target = Bukkit.getPlayer(value);
                if (target == null) {
                    lm.General_NotOnline.sendMessage(sender);
                    return true;
                }
            }

            @NotNull
            selectorOptions combo = plugin.getComboSelectionManager().getOrCreate(target);

            CMIGui gui = new CMIGui(target);
            gui.setInvSize(5);

            CMIGuiButton button = new CMIGuiButton(10, combo.isEnabled() ? CMIMaterial.GREEN_WOOL : CMIMaterial.RED_WOOL) {
                @Override
                public void click(GUIClickType type) {
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(player, !combo.isEnabled());
                    updateLooks();
                    update();
                }

                @Override
                public void updateLooks() {
                    this.setItem(combo.isEnabled() ? CMIMaterial.GREEN_WOOL : CMIMaterial.RED_WOOL);
                    this.setName(lm.Select_ComboEnabled.getMessage((combo.isEnabled() ? LC.modify_enabled.getLocale() : LC.modify_disabled.getLocale())));
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            button = new CMIGuiButton(12, CMIMaterial.LEATHER_BOOTS) {
                @Override
                public void click(GUIClickType type) {
                    combo.setMove(!combo.isMove());
                    updateLooks();
                    update();
                }

                @Override
                public void updateLooks() {
                    this.setName(lm.Select_MoveExpand.getMessage(combo.isMove() ? LC.modify_enabled.getLocale() : LC.modify_disabled.getLocale()));
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            button = new CMIGuiButton(13, CMIMaterial.BOW) {
                @Override
                public void click(GUIClickType type) {
                    combo.setAim(!combo.isAim());
                    updateLooks();
                    update();
                }

                @Override
                public void updateLooks() {
                    this.setName(lm.Select_RangeInteract.getMessage(combo.isAim() ? LC.modify_enabled.getLocale() : LC.modify_disabled.getLocale()));
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            button = new CMIGuiButton(14, CMIMaterial.ACACIA_DOOR) {
                @Override
                public void click(GUIClickType type) {
                    combo.setQuickBar(!combo.isQuickBar());
                    updateLooks();
                    update();
                }

                @Override
                public void updateLooks() {
                    this.setName(lm.Select_MouseScroll.getMessage(combo.isQuickBar() ? LC.modify_enabled.getLocale() : LC.modify_disabled.getLocale()));
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            button = new CMIGuiButton(30, CMIMaterial.CHEST) {
                @Override
                public void click(GUIClickType type) {
                    combo.setGroupLimit(!combo.isGroupLimit());
                    updateLooks();
                    update();
                }

                @Override
                public void updateLooks() {
                    this.setName(lm.Select_GroupLimits.getMessage(combo.isGroupLimit() ? LC.modify_enabled.getLocale() : LC.modify_disabled.getLocale()));
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            button = new CMIGuiButton(31, SelectionManager.getTool()) {
                @Override
                public void click(GUIClickType type) {
                    combo.setSmartToolSelection(!combo.isSmartToolSelection());
                    updateLooks();
                    update();
                }

                @Override
                public void updateLooks() {
                    this.setName(lm.Select_SmartTool.getMessage(combo.isSmartToolSelection() ? LC.modify_enabled.getLocale() : LC.modify_disabled.getLocale()));
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            button = new CMIGuiButton(33, CMIMaterial.ACACIA_FENCE) {
                @Override
                public void click(GUIClickType type) {
                    plugin.getSelectionManager().afterSelectionUpdate(player);
                }

                @Override
                public void updateLooks() {
                    this.setName(lm.Select_Show.getMessage());
                }
            };
            button.hideItemFlags();
            gui.addButton(button);

            gui.fillEmptyButtons();
            gui.open();
            return true;
        case togglecombo:
        case move:
        case aim:
        case quickbar:
        case considerlimits:
            Boolean state = null;

            if (value != null) {
                if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
                    state = true;
                    value = null;
                } else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
                    state = false;
                    value = null;
                }
            }

            if (state == null && subvalue != null) {
                if (subvalue.equalsIgnoreCase("on") || subvalue.equalsIgnoreCase("true")) {
                    state = true;
                } else if (subvalue.equalsIgnoreCase("off") || subvalue.equalsIgnoreCase("false")) {
                    state = false;
                }
            }

            target = player;
            if (value != null) {
                if (!ResPerm.select_auto_others.hasPermission(player, true)) {
                    return true;
                }
                target = Bukkit.getPlayer(value);
                if (target == null) {
                    lm.General_NotOnline.sendMessage(sender);
                    return true;
                }
            }

            boolean alreadyEnabled = plugin.getComboSelectionManager().has(target.getUniqueId());

            combo = plugin.getComboSelectionManager().getOrCreate(target);

            switch (action) {
            case move:
                if (state != null) {
                    combo.setMove(state);
                } else
                    combo.setMove(!combo.isMove());
                if (!combo.isEnabled())
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(target, true);
                break;
            case aim:
                if (state != null) {
                    combo.setAim(state);
                } else
                    combo.setAim(!combo.isAim());
                if (!combo.isEnabled())
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(target, true);
                break;
            case considerlimits:
                if (state != null) {
                    combo.setGroupLimit(state);
                } else
                    combo.setGroupLimit(!combo.isGroupLimit());
                if (!combo.isEnabled())
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(target, true);
                break;
            case quickbar:
                if (state != null) {
                    combo.setQuickBar(state);
                } else
                    combo.setQuickBar(!combo.isQuickBar());
                if (!combo.isEnabled())
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(target, true);
                break;
            case togglecombo:
                if (state != null) {
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(target, state);
                } else
                    Residence.getInstance().getComboSelectionManager().toggleAutoSelection(target, !combo.isEnabled());
                return true;
            }

            if (!alreadyEnabled) {
                // only reset on enable, not disable, to avoid clearing selection when disabling
                // auto-selection
                plugin.getSelectionManager().clearSelection(target);
                plugin.getSelectionManager().placeLoc1(player, player.getLocation().clone(), false);
                plugin.getSelectionManager().placeLoc2(player, player.getLocation().clone().add(0, 1, 0), true);
            }

            return true;
        case residence:
            String resName;
            String areaName;
            ClaimedResidence res = null;
            if (value != null) {
                res = plugin.getResidenceManager().getByName(value);
            } else {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
            }

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            resName = res.getName();
            CuboidArea area = null;

            if (subvalue != null) {
                area = res.getArea(subvalue);
                areaName = subvalue;
            } else {
                areaName = res.getAreaNameByLoc(player.getLocation());
                area = res.getArea(areaName);
            }

            if (area == null) {
                area = res.getMainArea();
                areaName = res.getMainAreaName();
            }

            if (area != null) {
                plugin.getSelectionManager().placeLoc1(player, area.getHighLocation(), false);
                plugin.getSelectionManager().placeLoc2(player, area.getLowLocation(), true);
                lm.Select_Area.sendMessage(sender, areaName, resName);
            } else {
                lm.Area_NonExist.sendMessage(sender);
            }
            return true;
        }

        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        // Main command
        c.get("Description", "Selection Commands");
        c.get("Info", Arrays.asList("This command selects areas for usage with residence.",
                "/res select [x] [y] [z] - selects a radius of blocks, with you in the middle."));
        LocaleManager.addTabCompleteSub(this, "[x]", "[y]", "[z]");

        // Sub commands
        c.setFullPath(c.getPath() + "SubCommands.");

        c.get("coords.Description", "Display selected coordinates");
        c.get("coords.Info", Arrays.asList("&eUsage: &6/res select coords"));
        LocaleManager.addTabCompleteSub(this, "coords");

        c.get("size.Description", "Display selected size");
        c.get("size.Info", Arrays.asList("&eUsage: &6/res select size"));
        LocaleManager.addTabCompleteSub(this, "size");

        c.get("move.Description", "Turns on move selection tool");
        c.get("move.Info", Arrays.asList("&eUsage: &6/res select move [playername]"));
        LocaleManager.addTabCompleteSub(this, "move", "[playername]");

        c.get("considerlimits.Description", "Toggles considerlimits selection");
        c.get("considerlimits.Info", Arrays.asList("&eUsage: &6/res select considerlimits [playername]"));
        LocaleManager.addTabCompleteSub(this, "considerlimits", "[playername]");

        c.get("togglecombo.Description", "Toggles combo selection");
        c.get("togglecombo.Info", Arrays.asList("&eUsage: &6/res select togglecombo [playername]"));
        LocaleManager.addTabCompleteSub(this, "togglecombo", "[playername]");

        c.get("options.Description", "Opens selection GUI");
        c.get("options.Info", Arrays.asList("&eUsage: &6/res select options [playername]"));
        LocaleManager.addTabCompleteSub(this, "options", "[playername]");

        c.get("outward.Description", "Expands selection outward by a specified amount");
        c.get("outward.Info", Arrays.asList("&eUsage: &6/res select outward [playername]"));
        LocaleManager.addTabCompleteSub(this, "outward", "[playername]");

        c.get("aim.Description", "Turns on aim selection tool");
        c.get("aim.Info", Arrays.asList("&eUsage: &6/res select aim [playername]"));
        LocaleManager.addTabCompleteSub(this, "aim", "[playername]");

        c.get("clear.Description", "Clears selection");
        c.get("clear.Info", Arrays.asList("&eUsage: &6/res select clear [playername]"));
        LocaleManager.addTabCompleteSub(this, "clear", "[playername]");

        c.get("quickbar.Description", "Turns on quickbar selection tool");
        c.get("quickbar.Info", Arrays.asList("&eUsage: &6/res select quickbar [playername]"));
        LocaleManager.addTabCompleteSub(this, "quickbar", "[playername]");

        c.get("cost.Description", "Display selection cost");
        c.get("cost.Info", Arrays.asList("&eUsage: &6/res select cost"));
        LocaleManager.addTabCompleteSub(this, "cost");

        c.get("vert.Description", "Expand Selection Vertically");
        c.get("vert.Info", Arrays.asList("&eUsage: &6/res select vert", "Will expand selection as high and as low as allowed."));
        LocaleManager.addTabCompleteSub(this, "vert");

        c.get("sky.Description", "Expand Selection to Sky");
        c.get("sky.Info", Arrays.asList("&eUsage: &6/res select sky", "Expands as high as your allowed to go."));
        LocaleManager.addTabCompleteSub(this, "sky");

        c.get("bedrock.Description", "Expand Selection to Bedrock");
        c.get("bedrock.Info", Arrays.asList("&eUsage: &6/res select bedrock", "Expands as low as your allowed to go."));
        LocaleManager.addTabCompleteSub(this, "bedrock");

        c.get("expand.Description", "Expand selection in a direction.");
        c.get("expand.Info", Arrays.asList("&eUsage: &6/res select expand <amount>", "Expands <amount> in the direction your looking."));
        LocaleManager.addTabCompleteSub(this, "expand", "5");

        c.get("shift.Description", "Shift selection in a direction");
        c.get("shift.Info", Arrays.asList("&eUsage: &6/res select shift <amount>", "Pushes your selection by <amount> in the direction your looking."));
        LocaleManager.addTabCompleteSub(this, "shift", "5");

        c.get("chunk.Description", "Select the chunk your currently in.");
        c.get("chunk.Info", Arrays.asList("&eUsage: &6/res select chunk", "Selects the chunk your currently standing in."));
        LocaleManager.addTabCompleteSub(this, "chunk");

        c.get("residence.Description", "Select a existing area in a residence.");
        c.get("residence.Info", Arrays.asList("&eUsage: &6/res select residence <residence>", "Selects a existing area in a residence."));
        LocaleManager.addTabCompleteSub(this, "residence", "[residence]");

        c.get("worldedit.Description", "Set selection using the current WorldEdit selection.");
        c.get("worldedit.Info", Arrays.asList("&eUsage: &6/res select worldedit", "Sets selection area using the current WorldEdit selection."));
        LocaleManager.addTabCompleteSub(this, "worldedit");
    }
}
