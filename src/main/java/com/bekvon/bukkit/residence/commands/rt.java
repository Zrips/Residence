package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class rt implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 2500)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {

        if (args.length != 0 && args.length != 1 && args.length != 2) {
            return false;
        }

        if (!resadmin && !ResPerm.randomtp.hasPermission(sender))
            return true;

        World wname = null;

        Player tPlayer = null;

        if (args.length > 0) {
            c: for (int i = 0; i < args.length; i++) {
                for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
                    if (!one.getCenter().getWorld().getName().equalsIgnoreCase(args[i]))
                        continue;
                    wname = one.getCenter().getWorld();
                    continue c;
                }
                Player p = Bukkit.getPlayer(args[i]);
                if (p != null)
                    tPlayer = p;
            }
        }

        if (args.length > 0 && wname == null && tPlayer == null) {
            lm.Invalid_World.sendMessage(sender);
            String worlds = "";
            for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
                if (!worlds.isEmpty())
                    worlds += ", ";
                worlds += one.getCenter().getWorld().getName();
                break;
            }
            lm.RandomTeleport_WorldList.sendMessage(sender, worlds);
            return true;
        }

        if (tPlayer == null && sender instanceof Player)
            tPlayer = (Player) sender;

        if (wname == null && tPlayer != null)
            wname = tPlayer.getLocation().getWorld();

        if (wname == null && tPlayer == null) {
            lm.Invalid_World.sendMessage(sender);
            String worlds = "";
            for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
                if (!worlds.isEmpty())
                    worlds += ", ";
                worlds += one.getCenter().getWorld().getName();
                break;
            }
            lm.RandomTeleport_WorldList.sendMessage(sender, worlds);
            return true;
        }

        if (tPlayer == null)
            return false;

        if (!sender.getName().equalsIgnoreCase(tPlayer.getName()) && !ResPerm.randomtp_admin.hasPermission(sender))
            return false;

        int sec = plugin.getConfigManager().getrtCooldown();
        if (plugin.getRandomTeleportMap().containsKey(tPlayer.getUniqueId()) && !resadmin && !ResPerm.randomtp_cooldownbypass.hasPermission(sender, false)) {
            if (plugin.getRandomTeleportMap().get(tPlayer.getUniqueId()) + (sec * 1000) > System.currentTimeMillis()) {
                int left = (int) (sec - ((System.currentTimeMillis() - plugin.getRandomTeleportMap().get(tPlayer.getUniqueId())) / 1000));
                lm.RandomTeleport_TpLimit.sendMessage(tPlayer, left);
                return true;
            }
        }
        if (!plugin.getRandomTpManager().isDefinedRnadomTp(wname)) {
            lm.RandomTeleport_Disabled.sendMessage(sender);
            return true;
        }

        World worldName = wname;
        Player player = tPlayer;

        if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
            Location lc = plugin.getRandomTpManager().getRandomlocationSync(worldName);
            teleport(sender, player, lc, sec, resadmin);
        } else {
            CompletableFuture<Location> aloc = plugin.getRandomTpManager().getRandomlocationAsync(worldName);
            aloc.thenApply(lc -> {
                return teleport(sender, player, lc, sec, resadmin);
            });
        }

        return true;
    }

    private static boolean teleport(CommandSender sender, Player player, Location lc, int sec, boolean resadmin) {

        Residence.getInstance().getRandomTeleportMap().put(player.getUniqueId(), System.currentTimeMillis());

        if (lc == null) {
            lm.RandomTeleport_IncorrectLocation.sendMessage(sender, sec);
            return true;
        }

        CMIScheduler.runAtLocationLater(Residence.getInstance(), lc, () -> {
            if (Residence.getInstance().getConfigManager().getTeleportDelay() > 0 && !resadmin && !ResPerm.randomtp_delaybypass.hasPermission(sender, false)) {
                lm.RandomTeleport_TeleportStarted.sendMessage(player, lc.getX(), lc.getY(), lc.getZ(), Residence.getInstance().getConfigManager().getTeleportDelay());
                Residence.getInstance().getRandomTpManager().performDelaydTp(lc, player);
            } else
                Residence.getInstance().getRandomTpManager().performInstantTp(lc, player);

        }, 1);
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Teleports to random location in world");
        c.get("Info", Arrays.asList("&eUsage: &6/res rt (worldname) (playerName)", "Teleports you to random location in defined world."));
        LocaleManager.addTabCompleteMain(this, "[worldname]", "[playername]");
    }
}
