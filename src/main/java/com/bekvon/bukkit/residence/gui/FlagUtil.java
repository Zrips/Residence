package com.bekvon.bukkit.residence.gui;

import com.bekvon.bukkit.CMIGUI.CMIGui;
import com.bekvon.bukkit.CMIGUI.CMIGuiButton;
import com.bekvon.bukkit.CMIGUI.GUIManager.GUIClickType;
import com.bekvon.bukkit.CMIGUI.GUIManager.GUIRows;
import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.text.help.PageInfo;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class FlagUtil {

    private FlagData flagData = new FlagData();
    private Residence plugin;

    public FlagUtil(Residence plugin) {
        this.plugin = plugin;
    }

    public void load() {
        ConfigReader c = null;
        try {
            c = new ConfigReader("flags.yml");
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (c != null) {
            if (!c.getC().isConfigurationSection("Global.FlagPermission"))
                return;

            Set<String> allFlags = c.getC().getConfigurationSection("Global.FlagPermission").getKeys(false);

            for (String oneFlag : allFlags) {
                if (!c.getC().contains("Global.FlagGui." + oneFlag))
                    continue;
                String value = c.get("Global.FlagGui." + oneFlag, "WHITE_WOOL");
                value = value.replace("-", ":");
                CMIMaterial Mat = CMIMaterial.get(value);
                if (Mat == null) {
                    Mat = CMIMaterial.STONE;
                }
                ItemStack item = Mat.newItemStack();
                flagData.addFlagButton(oneFlag.toLowerCase(), item);
            }
        }
    }

    public void openPsetFlagGui(Player player, String targetPlayer, ClaimedResidence res, boolean resadmin, int page) {
        if (player == null || !player.isOnline())
            return;

        setFlagInfo flag = new setFlagInfo(res, player, targetPlayer, resadmin);
        flag.recalculate();

        CMIGui gui = new CMIGui(player);
        gui.setTitle(Residence.getInstance().msg(lm.Gui_Set_Title, res.getName()));
        gui.setInvSize(GUIRows.r6);
        PageInfo pi = new PageInfo(45, flag.getButtons().size(), page);

        fillButtons(flag, pi, gui);

        gui.open();
    }

    private void fillButtons(setFlagInfo flag, PageInfo pi, CMIGui gui) {
        gui.clearButtons();
        for (CMIGuiButton one : flag.getButtons()) {
            if (pi.isContinue())
                continue;
            if (pi.isBreak())
                break;
            gui.addButton(one);
            gui.updateButton(one);
        }

        if (pi.getCurrentPage() < pi.getTotalPages()) {
            ItemStack Item = new ItemStack(Material.ARROW);
            CMIGuiButton forward = new CMIGuiButton(53, Item) {
                @Override
                public void click(GUIClickType type) {
                    fillButtons(flag, new PageInfo(45, flag.getButtons().size(), pi.getCurrentPage() + 1), gui);
                }
            };
            forward.setName(Residence.getInstance().msg(lm.General_nextPage));
            gui.addButton(forward);
            gui.updateButton(forward);
        }

        if (pi.getCurrentPage() > 1) {
            ItemStack Item = new ItemStack(Material.ARROW);
            CMIGuiButton back = new CMIGuiButton(45, Item) {
                @Override
                public void click(GUIClickType type) {
                    fillButtons(flag, new PageInfo(45, flag.getButtons().size(), pi.getCurrentPage() - 1), gui);
                }
            };
            back.setName(Residence.getInstance().msg(lm.General_prevPage));
            gui.addButton(back);
            gui.updateButton(back);
        }
    }

    public void openSetFlagGui(Player player, ClaimedResidence res, boolean resadmin, int page) {
        if (player == null || !player.isOnline())
            return;

        setFlagInfo flag = new setFlagInfo(res, player, resadmin);
        flag.recalculate();
        CMIGui gui = new CMIGui(player);
        gui.setTitle(Residence.getInstance().msg(lm.Gui_Set_Title, res.getName()));
        gui.setInvSize(GUIRows.r6);

        PageInfo pi = new PageInfo(45, flag.getButtons().size(), page);

        fillButtons(flag, pi, gui);

        gui.open();
    }

    public FlagData getFlagData() {
        return flagData;
    }
}
