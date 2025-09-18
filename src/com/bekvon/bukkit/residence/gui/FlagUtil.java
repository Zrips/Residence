package com.bekvon.bukkit.residence.gui;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.PlayerCache;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.GUI.CMIGui;
import net.Zrips.CMILib.GUI.CMIGuiButton;
import net.Zrips.CMILib.GUI.GUIManager.GUIRows;
import net.Zrips.CMILib.Items.CMIAsyncHead;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class FlagUtil {

    private FlagData flagData = new FlagData();
    private Residence plugin;

    public FlagUtil(Residence plugin) {
        this.plugin = plugin;
    }

    public void load() {
        ConfigReader c = null;
        try {
            c = new ConfigReader(Residence.getInstance(), "flags.yml");
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

                CMIAsyncHead ahead = new CMIAsyncHead() {
                    @Override
                    public void afterAsyncUpdate(ItemStack item) {
                        CMIScheduler.runTask(plugin, () -> flagData.addFlagButton(oneFlag.toLowerCase(), item));
                    }
                };

                CMIItemStack i = CMILib.getInstance().getItemManager().getItem(value, ahead);

                if (i == null || i.getType() == null)
                    i = new CMIItemStack(CMIMaterial.STONE);

                ItemStack item = i.getItemStack();
                flagData.addFlagButton(oneFlag.toLowerCase(), item);
            }
        }
    }

    public void openPsetFlagGui(Player player, UUID targetPlayer, ClaimedResidence res, boolean resadmin, int page) {
        if (player == null || !player.isOnline())
            return;

        setFlagInfo flag = new setFlagInfo(res, player, targetPlayer, resadmin);
        flag.recalculate();

        PageInfo pi = new PageInfo(45, flag.getButtons().size(), page);
        openUI(flag, pi, player, lm.Gui_Pset_Title.getMessage(res.getName(), PlayerCache.getName(targetPlayer)));
        return;
    } 

    private void openUI(setFlagInfo flag, PageInfo pi, Player player, String title) {

        CMIGui gui = new CMIGui(player) {
            @Override
            public void pageChange(int page) {
                openUI(flag, new PageInfo(45, flag.getButtons().size(), page), player, title);
            }
        };
        gui.setTitle(title);
        gui.setInvSize(GUIRows.r6);

        for (CMIGuiButton one : flag.getButtons()) {
            if (pi.isContinue())
                continue;
            if (pi.isBreak())
                break;
            gui.addButton(one);
        }
        gui.addPagination(pi);

        gui.fillEmptyButtons();
        gui.open();

    }

    public void openSetFlagGui(Player player, ClaimedResidence res, boolean resadmin, int page) {
        if (player == null || !player.isOnline())
            return;
        setFlagInfo flag = new setFlagInfo(res, player, resadmin);
        flag.recalculate();
        PageInfo pi = new PageInfo(45, flag.getButtons().size(), page);
        openUI(flag, pi, player, lm.Gui_Set_Title.getMessage(res.getName()));
    }

    public FlagData getFlagData() {
        return flagData;
    }
}
