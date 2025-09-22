package com.bekvon.bukkit.residence.raid;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceRaidEndEvent;
import com.bekvon.bukkit.residence.event.ResidenceRaidPreStartEvent;
import com.bekvon.bukkit.residence.event.ResidenceRaidStartEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.BossBar.BossBarInfo;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class ResidenceRaid {

    private ClaimedResidence res;
    private Long startsAt = 0L;
    private Long endsAt = 0L;
    private Long immunityUntil = null;
//    private Long lastSiegeEnded = 0L;
    private HashMap<UUID, RaidAttacker> attackers = new HashMap<UUID, RaidAttacker>();
    private HashMap<UUID, RaidDefender> defenders = new HashMap<UUID, RaidDefender>();

    private CMITask schedRaidEndId = null;
    private CMITask shedRaidStartId = null;
    private CMITask schedBossBarId = null;

    public ResidenceRaid(ClaimedResidence res) {
        this.res = res;
    }

    public boolean onSameTeam(Player player1, Player player2) {
        return attackers.containsKey(player1.getUniqueId()) && attackers.containsKey(player2.getUniqueId()) || defenders.containsKey(player1.getUniqueId()) && defenders.containsKey(player2.getUniqueId());
    }

    public Long getEndsAt() {
        return endsAt;
    }

    public Long getCooldownEnd() {
        return endsAt + (ConfigManager.RaidCooldown * 1000);
    }

    public void setEndsAt(Long endsAt) {
        this.endsAt = endsAt;
    }

    public boolean isEnded() {
        return getEndsAt() < System.currentTimeMillis();
    }
//    public Long getLastSiegeEnded() {
//	return lastSiegeEnded;
//    }
//
//    public void setLastSiegeEnded(Long lastSiegeEnded) {
//	this.lastSiegeEnded = lastSiegeEnded;
//    }

    public ClaimedResidence getRes() {
        return res;
    }

    public HashMap<UUID, RaidDefender> getDefenders() {
        return defenders;
    }

    public void addDefender(Player defender) {
        addDefender(Residence.getInstance().getPlayerManager().getResidencePlayer(defender));
    }

    public void addDefender(ResidencePlayer defender) {
        this.attackers.remove(defender.getUniqueId());
        this.defenders.put(defender.getUniqueId(), new RaidDefender(defender));
        defender.setJoinedRaid(this);
    }

    public boolean isDefender(Player player) {
        return isDefender(player.getUniqueId());
    }

    public boolean isDefender(UUID uuid) {
        return defenders.containsKey(uuid);
    }

    public void removeDefender(Player defender) {
        removeDefender(Residence.getInstance().getPlayerManager().getResidencePlayer(defender));
    }

    public void removeDefender(ResidencePlayer defender) {
        this.defenders.remove(defender.getUniqueId());
        defender.setJoinedRaid(null);
    }

    public boolean isAttacker(Player player) {
        return isAttacker(player.getUniqueId());
    }

    public boolean isAttacker(UUID uuid) {
        return attackers.containsKey(uuid);
    }

    public HashMap<UUID, RaidAttacker> getAttackers() {
        return attackers;
    }

    public void clearAttackers() {
        for (Entry<UUID, RaidAttacker> one : this.attackers.entrySet()) {
            one.getValue().getPlayer().setJoinedRaid(null);
        }
        this.attackers.clear();
    }

    public void clearDefenders() {
        for (Entry<UUID, RaidDefender> one : this.defenders.entrySet()) {
            one.getValue().getPlayer().setJoinedRaid(null);
        }
        this.defenders.clear();
    }

    public void addAttacker(Player attacker) {
        addAttacker(Residence.getInstance().getPlayerManager().getResidencePlayer(attacker));
    }

    public void addAttacker(ResidencePlayer attacker) {
        this.defenders.remove(attacker.getUniqueId());
        this.attackers.put(attacker.getUniqueId(), new RaidAttacker(attacker));

        attacker.setJoinedRaid(this);
    }

    public void removeAttacker(Player attacker) {
        this.attackers.remove(attacker.getUniqueId());
        removeAttacker(Residence.getInstance().getPlayerManager().getResidencePlayer(attacker));
    }

    public void removeAttacker(ResidencePlayer attacker) {
        attacker.setJoinedRaid(null);
        this.attackers.remove(attacker.getUniqueId());
    }

    public Long getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Long startsAt) {
        this.startsAt = startsAt;
    }

    public static final String bossBarPreRaidIdent = "ResidencePreRaid";
    public static final String bossBarRaidIdent = "ResidenceRaid";

    public void showBossBar() {

        if (isUnderRaid()) {
            for (Entry<UUID, RaidAttacker> one : getAttackers().entrySet()) {
                ResidencePlayer rPlayer = one.getValue().getPlayer();
                if (rPlayer.isOnline())
                    showBossbar(rPlayer, BarColor.BLUE, lm.Raid_EndsIn);
            }
            for (Entry<UUID, RaidDefender> one : getDefenders().entrySet()) {
                ResidencePlayer rOwner = one.getValue().getPlayer();
                if (rOwner.isOnline())
                    showBossbar(rOwner, BarColor.BLUE, lm.Raid_EndsIn);
            }
        } else if (isInPreRaid()) {
            for (Entry<UUID, RaidAttacker> one : getAttackers().entrySet()) {
                ResidencePlayer rPlayer = one.getValue().getPlayer();
                if (rPlayer.isOnline())
                    showBossbar(rPlayer, BarColor.GREEN, lm.Raid_StartsIn);
            }
            for (Entry<UUID, RaidDefender> one : getDefenders().entrySet()) {
                ResidencePlayer rOwner = one.getValue().getPlayer();
                if (rOwner.isOnline())
                    showBossbar(rOwner, BarColor.GREEN, lm.Raid_StartsIn);
            }
        }
    }

    private void showBossbar(ResidencePlayer rPlayer, BarColor color, lm msg) {
        BossBarInfo barInfo = CMILib.getInstance().getBossBarManager().getBossBar(rPlayer.getPlayer(), isUnderRaid() ? bossBarRaidIdent : bossBarPreRaidIdent);
        if (barInfo == null) {
            barInfo = new BossBarInfo(rPlayer.getPlayer(), isUnderRaid() ? bossBarRaidIdent : bossBarPreRaidIdent) {
                @Override
                public void updateCycle() {
                    setTitleOfBar(msg.getMessage(getDefenders().size(), getAttackers().size()));
                }
            };
            Double secLeft = ((isUnderRaid() ? getEndsAt() : getStartsAt()) - System.currentTimeMillis()) / 1000D;
            barInfo.setKeepForTicks(22);
            barInfo.setColor(color);
            barInfo.setTitleOfBar(msg.getMessage(getDefenders().size(), getAttackers().size()));
            barInfo.setAdjustPerc(-(1D / secLeft));
            barInfo.setPercentage(secLeft, secLeft);
            barInfo.setStyle(BarStyle.SEGMENTED_20);
            barInfo.setAuto(20);

            CMILib.getInstance().getBossBarManager().addBossBar(rPlayer.getPlayer(), barInfo);
        }
    }

    public void endRaid() {
        setEndsAt(System.currentTimeMillis());

        if (this.schedRaidEndId != null) {
            ResidenceRaidEndEvent End = new ResidenceRaidEndEvent(res);
            Bukkit.getPluginManager().callEvent(End);
            schedRaidEndId.cancel();
            schedRaidEndId = null;
        }

        if (this.shedRaidStartId != null) {
            shedRaidStartId.cancel();
            schedRaidEndId = null;
        }

        if (this.schedBossBarId != null) {
            schedBossBarId.cancel();
            schedRaidEndId = null;
        }

        setStartsAt(0L);

        for (Entry<UUID, RaidAttacker> one : getAttackers().entrySet()) {
            Player player = Bukkit.getPlayer(one.getKey());
            if (player == null)
                continue;
            lm.Raid_Ended.sendMessage(player, res.getName());

            res.kickFromResidence(player);

        }

        for (Entry<UUID, RaidAttacker> one : getAttackers().entrySet()) {
            ResidencePlayer RPlayer = one.getValue().getPlayer();
            if (RPlayer != null) {
                RPlayer.setLastRaidAttackTimer(System.currentTimeMillis());
                BossBarInfo barInfo = CMILib.getInstance().getBossBarManager().getBossBar(RPlayer.getPlayer(), ResidenceRaid.bossBarRaidIdent);
                if (barInfo != null) {
                    barInfo.cancelAutoScheduler();
                    barInfo.remove();
                    CMILib.getInstance().getBossBarManager().removeBossBar(RPlayer.getPlayer(), barInfo);
                }
                barInfo = CMILib.getInstance().getBossBarManager().getBossBar(RPlayer.getPlayer(), ResidenceRaid.bossBarPreRaidIdent);
                if (barInfo != null) {
                    barInfo.cancelAutoScheduler();
                    barInfo.remove();
                    CMILib.getInstance().getBossBarManager().removeBossBar(RPlayer.getPlayer(), barInfo);
                }
            }
        }

        for (Entry<UUID, RaidDefender> one : getDefenders().entrySet()) {
            ResidencePlayer RPlayer = one.getValue().getPlayer();
            if (RPlayer != null) {
                BossBarInfo barInfo = CMILib.getInstance().getBossBarManager().getBossBar(RPlayer.getPlayer(), ResidenceRaid.bossBarRaidIdent);
                if (barInfo != null) {
                    barInfo.cancelAutoScheduler();
                    barInfo.remove();
                    CMILib.getInstance().getBossBarManager().removeBossBar(RPlayer.getPlayer(), barInfo);
                }
                barInfo = CMILib.getInstance().getBossBarManager().getBossBar(RPlayer.getPlayer(), ResidenceRaid.bossBarPreRaidIdent);
                if (barInfo != null) {
                    barInfo.cancelAutoScheduler();
                    barInfo.remove();
                    CMILib.getInstance().getBossBarManager().removeBossBar(RPlayer.getPlayer(), barInfo);
                }
            }
        }

        res.getRPlayer().setLastRaidDefendTimer(System.currentTimeMillis());

        clearAttackers();
        clearDefenders();

    }

    public boolean isImmune() {
        return immunityUntil == null ? false : immunityUntil > System.currentTimeMillis();
    }

    public Long getPlayerImmunityUntil() {
        ResidencePlayer rplayer = this.res.getRPlayer();
        if (rplayer == null)
            return 0L;
        return rplayer.getLastRaidDefendTimer() == null ? 0L : rplayer.getLastRaidDefendTimer() + (ConfigManager.RaidPlayerCooldown * 1000L);
    }

    public boolean isPlayerImmune() {
        return getPlayerImmunityUntil() > System.currentTimeMillis();
    }

    public Long getImmunityUntil() {
        return immunityUntil;
    }

    public void setImmunityUntil(Long immunityUntil) {
        if (immunityUntil != null && immunityUntil > System.currentTimeMillis())
            this.immunityUntil = immunityUntil;
        else
            this.immunityUntil = null;
    }

    public boolean preStartRaid(Player attacker) {

        if (isUnderRaid() || this.isInPreRaid())
            return false;

        if (getCooldownEnd() > System.currentTimeMillis())
            return false;

        if (attacker != null)
            addAttacker(attacker);
        addDefender(res.getRPlayer().getPlayer());
        setStartsAt(System.currentTimeMillis() + (ConfigManager.PreRaidTimer * 1000));
        setEndsAt(getStartsAt() + (ConfigManager.RaidTimer * 1000));

        ResidenceRaidPreStartEvent start = new ResidenceRaidPreStartEvent(res, getAttackers());

        Bukkit.getPluginManager().callEvent(start);
        if (start.isCancelled())
            return false;

        if (attacker != null)
            Residence.getInstance().getPlayerManager().getResidencePlayer(attacker).setLastRaidAttackTimer(System.currentTimeMillis());
        res.getRPlayer().setLastRaidDefendTimer(System.currentTimeMillis());
        setImmunityUntil(ConfigManager.RaidCooldown * 1000L);

        return true;
    }

    public boolean startRaid() {

        if (!isUnderRaid() && !this.isInPreRaid())
            return false;

        ResidenceRaidStartEvent start = new ResidenceRaidStartEvent(res, getAttackers());
        this.shedRaidStartId = CMIScheduler.runTaskLater(Residence.getInstance(), () -> {
            Bukkit.getPluginManager().callEvent(start);
            if (start.isCancelled())
                start.getRes().getRaid().endRaid();
        }, ((getStartsAt() - System.currentTimeMillis()) / 50));

        schedBossBarId = CMIScheduler.scheduleSyncRepeatingTask(Residence.getInstance(), () -> {
            if (!isUnderRaid() && !isInPreRaid()) {
                schedBossBarId.cancel();
                return;
            }
            showBossBar();

        }, this.isUnderRaid() ? 20L : 0L, 20L);

        this.schedRaidEndId = CMIScheduler.runTaskLater(Residence.getInstance(), this::endRaid, ((getEndsAt() - System.currentTimeMillis()) / 50));

        return true;
    }

    public boolean isUnderRaid() {
        return getEndsAt() > System.currentTimeMillis()
            && getStartsAt() < System.currentTimeMillis();
    }

    public boolean isRaidInitialized() {
        if (isUnderRaid() || isInPreRaid())
            return true;
        if (res.getParent() != null)
            return res.getParent().getRaid().isRaidInitialized();
        return false;
    }

    public boolean isInPreRaid() {
        return getEndsAt() > System.currentTimeMillis()
            && getStartsAt() > System.currentTimeMillis();
    }

    public boolean canRaid() {
        return !isUnderRaid() && getCooldownEnd() < System.currentTimeMillis();
    }

    public boolean isUnderRaidCooldown() {
        return getCooldownEnd() > System.currentTimeMillis();
    }

}
