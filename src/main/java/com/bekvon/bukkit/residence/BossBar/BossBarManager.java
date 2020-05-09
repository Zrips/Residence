package com.bekvon.bukkit.residence.BossBar;

import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map.Entry;

public class BossBarManager {

    Residence plugin;

    public BossBarManager(Residence plugin) {
        this.plugin = plugin;
    }

    public synchronized void updateBossBars(ResidencePlayer player) {
        if (Version.isCurrentLower(Version.v1_9_R1))
            return;
        if (player == null)
            return;
        HashMap<String, BossBarInfo> temp = new HashMap<String, BossBarInfo>(player.getBossBarInfo());
        for (Entry<String, BossBarInfo> one : temp.entrySet()) {
            Show(one.getValue());
        }
    }

    public synchronized void Show(final BossBarInfo barInfo) {
        if (Version.isCurrentLower(Version.v1_9_R1))
            return;
        final ResidencePlayer user = barInfo.getUser();

        if (user == null || !user.isOnline())
            return;

        barInfo.updateCycle();

        BossBar bar = barInfo.getBar();

        String name = barInfo.getTitleOfBar();

        boolean isNew = true;
        if (bar == null) {
            BarColor color = barInfo.getColor();
            if (color == null)
                switch (user.getBossBarInfo().size()) {
                    case 1:
                        color = BarColor.GREEN;
                        break;
                    case 2:
                        color = BarColor.RED;
                        break;
                    case 3:
                        color = BarColor.WHITE;
                        break;
                    case 4:
                        color = BarColor.YELLOW;
                        break;
                    case 5:
                        color = BarColor.PINK;
                        break;
                    case 6:
                        color = BarColor.PURPLE;
                        break;
                    default:
                        color = BarColor.BLUE;
                        break;
                }
            bar = Bukkit.createBossBar(name, color, barInfo.getStyle() != null ? barInfo.getStyle() : BarStyle.SEGMENTED_10);
        } else {
            bar.setTitle(name);
            if (barInfo.getStyle() != null)
                bar.setStyle(barInfo.getStyle());
            if (barInfo.getColor() != null)
                bar.setColor(barInfo.getColor());
            isNew = false;
        }

        Double percentage = barInfo.getPercentage();
        if (percentage == null)
            percentage = 1D;

        if (barInfo.getAdjustPerc() != null) {
            Double curP = barInfo.getPercentage();
            if (curP != null && curP <= 0 && barInfo.getAdjustPerc() < 0) {
                barInfo.cancelAutoScheduler();
//		if (barInfo.getCommands() != null) {
//		    plugin.getSpecializedCommandManager().processCmds(barInfo.getCommands(user.getPlayer()), user.getPlayer());
//		}
                return;
            }
            if (curP != null && curP >= 1 && barInfo.getAdjustPerc() > 0) {
                barInfo.cancelAutoScheduler();
//		if (barInfo.getCommands() != null) {
//		    plugin.getSpecializedCommandManager().processCmds(barInfo.getCommands(user.getPlayer()), user.getPlayer());
//		}
                return;
            }
            if (curP == null)
                if (barInfo.getAdjustPerc() > 0)
                    curP = 0D;
                else
                    curP = 1D;
            curP += barInfo.getAdjustPerc();
            barInfo.setPercentage(curP);
        } else
            barInfo.setPercentage(percentage);

        try {
            bar.setProgress(barInfo.getPercentage());
            if (isNew) {
                Player target = user.getPlayer();
                if (target == null)
                    return;
                bar.addPlayer(target);
                bar.setVisible(true);
            }
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
        }
        barInfo.setBar(bar);

        barInfo.cancelHideScheduler();
        barInfo.cancelAutoScheduler();

        if (barInfo.getAuto() != null && barInfo.getAuto() > 0) {
            barInfo.setAutoId(Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {

                    Show(barInfo);

                    return;
                }
            }, barInfo.getAuto()));
        }
        if (barInfo.getKeepFor() > 0) {

            barInfo.setId(Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    barInfo.getBar().setVisible(false);
                    barInfo.remove();
                    return;
                }
            }, barInfo.getKeepFor()));
        }
    }

}
