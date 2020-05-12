package com.bekvon.bukkit.residence.raid;

import com.bekvon.bukkit.residence.BossBar.BossBarInfo;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceRaidEndEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class ResidenceRaid {

    public static final String bossBarPreRaidIdent = "ResidencePreRaid";
    public static final String bossBarRaidIdent = "ResidenceRaid";
    private ClaimedResidence res;
    private Long startsAt = 0L;
    private Long endsAt = 0L;
    private Long immunityUntil = null;
    //    private Long lastSiegeEnded = 0L;
    private HashMap<UUID, RaidAttacker> attackers = new HashMap<UUID, RaidAttacker>();
    private HashMap<UUID, RaidDefender> defenders = new HashMap<UUID, RaidDefender>();
    private int schedId = -1;

    public ResidenceRaid(ClaimedResidence res) {
        this.res = res;
    }

    public boolean onSameTeam(Player player1, Player player2) {
        return attackers.containsKey(player1.getUniqueId()) && attackers.containsKey(player2.getUniqueId()) || defenders.containsKey(player1.getUniqueId()) && defenders.containsKey(player2.getUniqueId());
    }

    public Long getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Long endsAt) {
        this.endsAt = endsAt;
    }
//    public Long getLastSiegeEnded() {
//	return lastSiegeEnded;
//    }
//
//    public void setLastSiegeEnded(Long lastSiegeEnded) {
//	this.lastSiegeEnded = lastSiegeEnded;
//    }

    public Long getCooldownEnd() {
        return endsAt + (ConfigManager.RaidCooldown * 1000);
    }

    public boolean isEnded() {
        return getEndsAt() < System.currentTimeMillis();
    }

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

    public int getSchedId() {
        return schedId;
    }

    public void setSchedId(int schedId) {
        this.schedId = schedId;
    }

    public void showBossBar() {

        if (res.isUnderRaid()) {
            for (Entry<UUID, RaidAttacker> one : res.getRaid().getAttackers().entrySet()) {
                ResidencePlayer rPlayer = one.getValue().getPlayer();
                if (rPlayer.isOnline())
                    showBossbar(rPlayer, BarColor.BLUE, lm.Raid_EndsIn);
            }
            for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
                ResidencePlayer rOwner = one.getValue().getPlayer();
                if (rOwner.isOnline())
                    showBossbar(rOwner, BarColor.BLUE, lm.Raid_EndsIn);
            }
        } else if (res.isInPreRaid()) {
            for (Entry<UUID, RaidAttacker> one : res.getRaid().getAttackers().entrySet()) {
                ResidencePlayer rPlayer = one.getValue().getPlayer();
                if (rPlayer.isOnline())
                    showBossbar(rPlayer, BarColor.GREEN, lm.Raid_StartsIn);
            }
            for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
                ResidencePlayer rOwner = one.getValue().getPlayer();
                if (rOwner.isOnline())
                    showBossbar(rOwner, BarColor.GREEN, lm.Raid_StartsIn);
            }
        }
    }

    private void showBossbar(ResidencePlayer rPlayer, BarColor color, lm msg) {
        BossBarInfo barInfo = rPlayer.getBossBar(res.isUnderRaid() ? bossBarRaidIdent : bossBarPreRaidIdent);
        if (barInfo == null) {
            barInfo = new BossBarInfo(rPlayer, res.isUnderRaid() ? bossBarRaidIdent : bossBarPreRaidIdent) {
                @Override
                public void updateCycle() {
                    setTitleOfBar(Residence.getInstance().msg(msg, getDefenders().size(), getAttackers().size()));
                }
            };
            Double secLeft = ((res.isUnderRaid() ? res.getRaid().getEndsAt() : res.getRaid().getStartsAt()) - System.currentTimeMillis()) / 1000D;
            barInfo.setKeepForTicks(22);
            barInfo.setColor(color);
            barInfo.setTitleOfBar(Residence.getInstance().msg(msg, getDefenders().size(), getAttackers().size()));
            barInfo.setAdjustPerc(-(1D / secLeft));
            barInfo.setPercentage(secLeft, secLeft);
            barInfo.setStyle(BarStyle.SEGMENTED_20);
            barInfo.setAuto(20);

            rPlayer.addBossBar(barInfo);
        }
    }

    public void endRaid() {
        setEndsAt(System.currentTimeMillis());

        if (getSchedId() > 0) {
            ResidenceRaidEndEvent End = new ResidenceRaidEndEvent(res);
            Bukkit.getPluginManager().callEvent(End);
            Bukkit.getScheduler().cancelTask(getSchedId());
            setSchedId(-1);
        }

        setStartsAt(0L);

        for (Entry<UUID, RaidAttacker> one : getAttackers().entrySet()) {
            Player player = Bukkit.getPlayer(one.getKey());
            if (player == null)
                continue;
            Residence.getInstance().msg(player, lm.Raid_Ended, res.getName());
            Location outside = res.getOutsideFreeLoc(player.getLocation(), player);
            if (outside != null)
                player.teleport(outside);
        }

        for (Entry<UUID, RaidAttacker> one : getAttackers().entrySet()) {
            ResidencePlayer RPlayer = one.getValue().getPlayer();
            if (RPlayer != null) {
                RPlayer.setLastRaidAttackTimer(System.currentTimeMillis());
                BossBarInfo barInfo = RPlayer.getBossBar(ResidenceRaid.bossBarRaidIdent);
                if (barInfo != null)
                    RPlayer.removeBossBar(barInfo);
            }
        }

        for (Entry<UUID, RaidDefender> one : getDefenders().entrySet()) {
            ResidencePlayer RPlayer = one.getValue().getPlayer();
            if (RPlayer != null) {
                BossBarInfo barInfo = RPlayer.getBossBar(ResidenceRaid.bossBarRaidIdent);
                if (barInfo != null)
                    RPlayer.removeBossBar(barInfo);
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
}
