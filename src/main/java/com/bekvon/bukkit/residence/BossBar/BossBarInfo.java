package com.bekvon.bukkit.residence.BossBar;

import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.List;
import java.util.Random;

public class BossBarInfo {
    private ResidencePlayer user;
    private Double percentage = null;
    private Double adjustPerc = null;
    private Integer keepFor = 60;
    private Integer auto = null;
    private BossBar bar;
    private BarColor startingColor = null;
    private BarStyle style = null;
    private Integer autoId = null;
    private Integer id = null;
    private String nameOfBar;
    private String titleOfBar = "Title";
    private List<String> cmds = null;

    public BossBarInfo(ResidencePlayer user, String nameOfBar) {
        this.user = user;
        this.nameOfBar = nameOfBar;
    }

    public BossBarInfo(ResidencePlayer user, String nameOfBar, BossBar bar) {
        this.user = user;
        this.nameOfBar = nameOfBar;
        this.bar = bar;
    }

    public void cancelAutoScheduler() {
        if (autoId != null) {
            Bukkit.getScheduler().cancelTask(this.autoId);
            autoId = null;
        }
    }

    public void cancelHideScheduler() {
        if (id != null) {
            Bukkit.getScheduler().cancelTask(this.id);
            id = null;
        }
    }

    public void remove() {
        cancelAutoScheduler();
        cancelHideScheduler();
        if (bar != null)
            bar.setVisible(false);
        user.removeBossBar(this);
    }

    public ResidencePlayer getUser() {
        return this.user;
    }

    public void setUser(ResidencePlayer user) {
        this.user = user;
    }

    public BossBar getBar() {
        return this.bar;
    }

    public void setBar(BossBar bar) {
        this.bar = bar;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        if (percentage != null) {
            if (percentage < 0)
                percentage = 0D;
            if (percentage > 1)
                percentage = 1D;
        }
        this.percentage = percentage;
    }

    public void setPercentage(Double max, Double current) {
        current = current * 100 / max / 100D;
        setPercentage(current);
    }

    public String getNameOfBar() {
        if (nameOfBar == null)
            nameOfBar = "CmiBossbar" + (new Random().nextInt(100));
        return nameOfBar;
    }

    public void setNameOfBar(String nameOfBar) {
        this.nameOfBar = nameOfBar;
    }

    public Integer getKeepFor() {
        return keepFor;
    }

    public void setKeepForTicks(Integer keepFor) {
        if (keepFor != null)
            this.keepFor = keepFor;
    }

    public String getTitleOfBar() {
        if (titleOfBar != null && titleOfBar.contains("[autoTimeLeft]")) {
            if (this.percentage != null && this.adjustPerc != null && this.auto != null) {
                double leftTicks = this.percentage / (this.adjustPerc < 0 ? -this.adjustPerc : this.adjustPerc);
                long totalTicks = (long) (leftTicks * (this.auto < 0 ? -this.auto : this.auto));
                long mili = totalTicks * 50;
                return titleOfBar.replace("[autoTimeLeft]", Utils.to24hourShort(mili + 1000));
            }
        }
        return titleOfBar == null ? "" : titleOfBar;
    }

    public void setTitleOfBar(String titleOfBar) {
        if (titleOfBar == null || titleOfBar.isEmpty())
            this.titleOfBar = null;
        else
            this.titleOfBar = ChatColor.translateAlternateColorCodes('&', titleOfBar);
    }

    public BarColor getColor() {
        return startingColor;
    }

    public void setColor(BarColor startingColor) {
//	if (startingColor == null)
//	    startingColor = BarColor.GREEN;
        this.startingColor = startingColor;
    }

    public Double getAdjustPerc() {
        return adjustPerc;
    }

    public void setAdjustPerc(Double adjustPerc) {
        this.adjustPerc = adjustPerc;
    }

    public BarStyle getStyle() {
        return style;
    }

    public void setStyle(BarStyle style) {
//	if (style == null)
//	    style = BarStyle.SEGMENTED_10;
        this.style = style;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
//	cancelHideScheduler();
        this.id = id;
    }

    public Integer getAuto() {
        return auto;
    }

    public void setAuto(Integer auto) {
        this.auto = auto;
    }

    public Integer getAutoId() {
        return autoId;
    }

    public void setAutoId(Integer autoId) {
        this.autoId = autoId;
    }

    public void updateCycle() {

    }

//    public List<String> getCommands() {
//	return cmds;
//    }
//
//    public List<String> getCommands(Player player) {
//	Snd snd = new Snd();
//	snd.setSender(player);
//	snd.setTarget(player);
//	return CMI.getInstance().getLM().updateSnd(snd, new ArrayList<String>(cmds));
//    }

//    public void setCmds(List<String> cmds) {
//	this.cmds = cmds;
//    }
}
