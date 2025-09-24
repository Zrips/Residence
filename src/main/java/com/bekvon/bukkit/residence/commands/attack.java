package com.bekvon.bukkit.residence.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import net.Zrips.CMILib.Time.CMITimeManager;

public class attack implements cmd {

    @Override
    @CommandAnnotation(info = "Start raid on residence", usage = "&eUsage: &6/res attack [resName]", simple = true, priority = 3100, regVar = { 0, 1 }, consoleVar = { 666 })
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;

        if (!ConfigManager.RaidEnabled) {
            lm.Raid_NotEnabled.sendMessage(player);
            return null;
        }

        ClaimedResidence res = null;
        if (args.length == 1)
            res = plugin.getResidenceManager().getByName(args[0]);
        else
            res = plugin.getResidenceManager().getByLoc(player.getLocation());

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return null;
        }

        if (!res.isTopArea()) {
            lm.Raid_attack_noSubzones.sendMessage(player);
            return null;
        }

        if (res.isOwner(player)) {
            lm.Raid_attack_noSelf.sendMessage(player);
            return null;
        }

        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        if (resPlayer.getJoinedRaid() != null) {
            lm.Raid_defend_alreadyInAnother.sendMessage(player, resPlayer.getJoinedRaid().getRes().getName());
            return null;
        }

        final ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(res.getOwnerUUID());
        if (!rPlayer.isOnline()) {
            lm.Raid_attack_isOffline.sendMessage(player);
            return null;
        }

        if (!rPlayer.isOnline()) {
            lm.Raid_attack_isOffline.sendMessage(player);
            return null;
        }

        if (res.getRaid().isPlayerImmune() && !res.getRaid().isInPreRaid() && !res.getRaid().isUnderRaid()) {
            lm.Raid_attack_playerImmune.sendMessage(player, CMITimeManager.to24hourShort(res.getRaid().getPlayerImmunityUntil() - System.currentTimeMillis() + 1000L));
            return null;
        }

        if (res.getRaid().isUnderRaidCooldown() && !res.getRaid().isInPreRaid() && !res.getRaid().isUnderRaid()) {
            lm.Raid_attack_cooldown.sendMessage(player, CMITimeManager.to24hourShort(res.getRaid().getCooldownEnd() - System.currentTimeMillis() + 1000L));
            return null;
        }

        if (res.getRaid().isUnderRaid() || res.getRaid().isInPreRaid()) {
            if (!res.getRaid().isAttacker(player))
                res.getRaid().addAttacker(player);
            lm.Raid_attack_Joined.sendMessage(player, res.getName());
            return null;
        }

        boolean started = res.getRaid().preStartRaid(player);

        if (started) {
            res.getRaid().startRaid();
            return true;
        }

        lm.Raid_cantStart.sendMessage(sender);

        return false;
    }

    @Override
    public void getLocale() {
        LocaleManager.addTabCompleteMain(this, "[cresidence]");
    }

}
