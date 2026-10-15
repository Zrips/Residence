package com.bekvon.bukkit.residence.selection;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.BossBar.BossBarInfo;
import net.Zrips.CMILib.Colors.CMIChatColor;

public class selectorOptions {

    private UUID uuid;

    private long time;

    private boolean enabled = false;

    private boolean aim = false;
    private boolean quickBar = false;
    private boolean move = false;
    private boolean groupLimit = false;
    private boolean smartToolSelection = SelectionManager.isSmartSelect();

    private boolean showBossBar = true;

    public selectorOptions(UUID uuid) {
        this.uuid = uuid;
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return this.time;
    }

    public boolean isAim() {
        return aim;
    }

    public selectorOptions setAim(boolean aim) {
        this.aim = aim;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }

    public boolean isQuickBar() {
        return quickBar;
    }

    public selectorOptions setQuickBar(boolean quickBar) {
        this.quickBar = quickBar;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }

    public boolean isMove() {
        return move;
    }

    public selectorOptions setMove(boolean move) {
        this.move = move;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }

    public selectorOptions setTime(long timeMillis) {
        this.time = timeMillis;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public selectorOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }

    private boolean updating = false;

    public void updateBossBar() {

        if (updating)
            return;

        if (!isEnabled() || !isShowBossBar()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null)
                return;

            CMILib.getInstance().getBossBarManager().removeBossBar(player, "ResidenceComboSelector");
            return;
        }

        showBossBar();
    }

    public void showBossBar() {

        if (updating)
            return;
        updating = true;
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        BossBarInfo bar = new BossBarInfo(player, "ResidenceComboSelector");
        bar.setTitleOfBar(getTitle());
        bar.setKeepForTicks(60 * 20);

        CMILib.getInstance().getBossBarManager().addBossBar(player, bar);

        updating = false;
    }

    private String getTitle() {
        return lm.Select_BossBar.getMessage((this.isMove() ? CMIChatColor.GREEN : CMIChatColor.YELLOW),
                (this.isAim() ? CMIChatColor.GREEN : CMIChatColor.YELLOW),
                (this.isQuickBar() ? CMIChatColor.GREEN : CMIChatColor.YELLOW),
                (this.isGroupLimit() ? CMIChatColor.GREEN : CMIChatColor.YELLOW),
                (this.isSmartToolSelection() ? CMIChatColor.GREEN : CMIChatColor.YELLOW));
    }

    public boolean isGroupLimit() {
        return groupLimit;
    }

    public selectorOptions setGroupLimit(boolean groupLimit) {
        this.groupLimit = groupLimit;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }

    public boolean isSmartToolSelection() {
        return smartToolSelection;
    }

    public selectorOptions setSmartToolSelection(boolean smartToolSelection) {
        this.smartToolSelection = smartToolSelection;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }

    public boolean isShowBossBar() {
        return showBossBar;
    }

    public selectorOptions setShowBossBar(boolean showBossBar) {
        this.showBossBar = showBossBar;
        updateBossBar();
        setTime(System.currentTimeMillis());
        return this;
    }
}
