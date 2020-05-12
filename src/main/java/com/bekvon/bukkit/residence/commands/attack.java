package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class attack implements cmd {

    @Override
    @CommandAnnotation(info = "Start raid on residence", usage = "&eUsage: &6/res attack [resName]", simple = true, priority = 3100, regVar = {0, 1}, consoleVar = {666})
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        Player player = (Player) sender;

        if (!ConfigManager.RaidEnabled) {
            plugin.msg(player, lm.Raid_NotEnabled);
            return null;
        }

        ClaimedResidence res = null;
        if (args.length == 1)
            res = plugin.getResidenceManager().getByName(args[0]);
        else
            res = plugin.getResidenceManager().getByLoc(player.getLocation());

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return null;
        }

        if (!res.isTopArea()) {
            plugin.msg(player, lm.Raid_attack_noSubzones);
            return null;
        }

        if (res.isOwner(player)) {
            plugin.msg(player, lm.Raid_attack_noSelf);
            return null;
        }

        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        if (resPlayer.getJoinedRaid() != null) {
            plugin.msg(player, lm.Raid_defend_alreadyInAnother, resPlayer.getJoinedRaid().getRes().getName());
            return null;
        }

        final ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(res.getOwnerUUID());
        if (!rPlayer.isOnline()) {
            plugin.msg(player, lm.Raid_attack_isOffline);
            return null;
        }

        if (!rPlayer.isOnline()) {
            plugin.msg(player, lm.Raid_attack_isOffline);
            return null;
        }

        if (res.getRaid().isPlayerImmune()) {
            plugin.msg(player, lm.Raid_attack_playerImmune, Utils.to24hourShort(res.getRaid().getPlayerImmunityUntil() - System.currentTimeMillis() + 1000L));
            return null;
        }

        if (res.isUnderRaidCooldown() && !res.isInPreRaid() && !res.isUnderRaid()) {
            plugin.msg(player, lm.Raid_attack_cooldown, Utils.to24hourShort(res.getRaid().getCooldownEnd() - System.currentTimeMillis() + 1000L));
            return null;
        }

        if (res.isUnderRaid() || res.isInPreRaid()) {
            if (!res.getRaid().isAttacker(player))
                res.getRaid().addAttacker(player);
            plugin.msg(player, lm.Raid_attack_Joined, res.getName());
            return null;
        }

        boolean started = res.preStartRaid(player);

        if (started) {
            res.startRaid();
            return true;
        }

        plugin.msg(player, "Cant start raid");

        return false;
    }

    @Override
    public void getLocale() {
        LocaleManager.addTabComplete(this, null, "[cresidence]");
    }

}
