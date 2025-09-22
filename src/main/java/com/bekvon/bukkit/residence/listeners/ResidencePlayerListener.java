package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.StuckInfo;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.containers.playerPersistentData;
import com.bekvon.bukkit.residence.containers.playerTempData;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceOwnerChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.protection.ResidenceManager.ChunkRef;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.utils.GetTime;
import com.bekvon.bukkit.residence.utils.Teleporting;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Entities.CMIEntity;
import net.Zrips.CMILib.Entities.CMIEntityType;
import net.Zrips.CMILib.Items.CMIItemStack;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.TitleMessages.CMITitleMessage;
import net.Zrips.CMILib.Util.CMIVersionChecker;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class ResidencePlayerListener implements Listener {

    private Residence plugin;

    private Runnable locationChangeCheck = () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Vector locfrom = playerTempData.get(player).getLastLocation(player.getLocation().toVector());
            Vector locto = player.getLocation().toVector();
            playerTempData.get(player).setLastLocation(locto);
            if (locfrom.getBlockX() == locto.getBlockX() && locfrom.getBlockY() == locto.getBlockY() && locfrom.getBlockZ() == locto.getBlockZ())
                continue;
            Long time = playerTempData.get(player).getLastCheck();
            if (time + 1000L > System.currentTimeMillis())
                continue;
            playerTempData.get(player).setLastCheck(System.currentTimeMillis());
            handleNewLocation(player, player.getLocation(), true);
        }
    };

    public ResidencePlayerListener(Residence plugin) {
        this.plugin = plugin;

        playerTempData.clear();
        CMIScheduler.scheduleSyncRepeatingTask(plugin, locationChangeCheck, 20L, 15 * 20L);
    }

    public void reload() {
        playerTempData.clear();
    }

    @EventHandler
    public void onJump(PlayerMoveEvent event) {

        if (!Flags.jump3.isGlobalyEnabled() && !Flags.jump2.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();
        if (player.isFlying())
            return;

        if (event.getTo().getY() - event.getFrom().getY() != 0.41999998688697815D)
            return;

        if (player.hasMetadata("NPC"))
            return;

        FlagPermissions perms = plugin.getPermsByLoc(player.getLocation());
        if (Flags.jump2.isGlobalyEnabled() && perms.has(Flags.jump2, FlagCombo.OnlyTrue))
            player.setVelocity(player.getVelocity().add(player.getVelocity().multiply(0.3)));
        else if (Flags.jump3.isGlobalyEnabled() && perms.has(Flags.jump3, FlagCombo.OnlyTrue))
            player.setVelocity(player.getVelocity().add(player.getVelocity().multiply(0.6)));

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        if (!Flags.itempickup.isGlobalyEnabled())
            return;

        // As of 1.12 version EntityPickupItemEvent event needs to be used. This is only for older servers
        // New handling located at ResidencePlayerListener1_12
        if (Version.isCurrentEqualOrHigher(Version.v1_12_R1))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getItem().getLocation());
        if (res == null)
            return;
        if (event.getPlayer().hasMetadata("NPC"))
            return;
        if (!res.getPermissions().playerHas(event.getPlayer(), Flags.itempickup, FlagCombo.OnlyFalse))
            return;
        if (ResPerm.bypass_itempickup.hasPermission(event.getPlayer(), 10000L))
            return;
        event.setCancelled(true);
        event.getItem().setPickupDelay(plugin.getConfigManager().getItemPickUpDelay() * 20);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {

        if (!Flags.itemdrop.isGlobalyEnabled())
            return;

        Player player = event.getPlayer();

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
        if (res == null)
            return;

        if (player.hasMetadata("NPC"))
            return;

        if (!res.getPermissions().playerHas(player, Flags.itemdrop, FlagCombo.OnlyFalse))
            return;

        if (ResPerm.bypass_itemdrop.hasPermission(player, 10000L))
            return;

        event.setCancelled(true);
    }

    // Adding to chat prefix main residence name
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerGlobalChat(AsyncPlayerChatEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (!plugin.getConfigManager().isGlobalChatEnabled())
            return;
        if (!plugin.getConfigManager().isGlobalChatSelfModify())
            return;
        Player player = event.getPlayer();

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        if (rPlayer == null)
            return;

        if (rPlayer.getResList().isEmpty())
            return;

        ClaimedResidence res = rPlayer.getMainResidence();

        if (res == null)
            return;

        String honorific = plugin.getConfigManager().getGlobalChatFormat().replace("%1", res.getTopParentName());

        String format = event.getFormat();
        format = format.replace("%1$s", honorific + "%1$s");
        event.setFormat(format);
    }

    // Changing chat prefix variable to residence name
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChatGlobalLow(AsyncPlayerChatEvent event) {
        procEvent(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void processNEvent(AsyncPlayerChatEvent event) {
        procEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void processHEvent(AsyncPlayerChatEvent event) {
        procEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void processHHEvent(AsyncPlayerChatEvent event) {
        procEvent(event);
    }

    private void procEvent(AsyncPlayerChatEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (!plugin.getConfigManager().isGlobalChatEnabled())
            return;
        if (plugin.getConfigManager().isGlobalChatSelfModify())
            return;

        String format = event.getFormat();
        if (!format.contains("{residence}"))
            return;

        Player player = event.getPlayer();

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        if (rPlayer == null)
            return;

        ClaimedResidence res = rPlayer.getMainResidence();

        String honorific = plugin.getConfigManager().getGlobalChatFormat().replace("%1", res == null ? "" : res.getTopParentName());
        if (honorific.equalsIgnoreCase(" "))
            honorific = "";

        format = format.replace("{residence}", honorific);
        event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceBackup(ResidenceFlagChangeEvent event) {
        if (!event.getFlag().equalsIgnoreCase(Flags.backup.toString()))
            return;
        Player player = event.getPlayer();
        if (!plugin.getConfigManager().RestoreAfterRentEnds)
            return;
        if (!plugin.getConfigManager().SchematicsSaveOnFlagChange)
            return;
        if (plugin.getSchematicManager() == null)
            return;
        if (player != null && !ResPerm.backup.hasPermission(player))
            event.setCancelled(true);
        else
            plugin.getSchematicManager().save(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceBackupRename(ResidenceRenameEvent event) {
        if (plugin.getSchematicManager() == null)
            return;
        plugin.getSchematicManager().rename(event.getResidence(), event.getNewResidenceName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
        if (plugin.getSchematicManager() == null)
            return;
        plugin.getSchematicManager().delete(event.getResidence());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFirstLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore())
            ResidenceBlockListener.newPlayers.add(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!plugin.getConfigManager().isRentInformOnEnding())
            return;
        final Player player = event.getPlayer();
        CMIScheduler.runTaskLater(plugin, () -> {
            if (!player.isOnline())
                return;
            List<ClaimedResidence> list = plugin.getRentManager().getRentedLandsList(player.getUniqueId());
            if (list.isEmpty())
                return;
            for (ClaimedResidence one : list) {
                RentedLand rentedland = one.getRentedLand();
                if (rentedland == null)
                    continue;
                if (rentedland.AutoPay)
                    continue;
                if (rentedland.endTime - System.currentTimeMillis() < plugin.getConfigManager().getRentInformBefore() * 60 * 24 * 7) {
                    lm.Residence_EndingRent.sendMessage(player, one, GetTime.getTime(rentedland.endTime));
                }
            }
        }, plugin.getConfigManager().getRentInformDelay() * 20L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFishingRodUse(PlayerFishEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.hook.isGlobalyEnabled())
            return;
        if (event == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (event.getCaught() == null)
            return;
        if ((Utils.isArmorStandEntity(event.getCaught().getType()) || event.getCaught() instanceof Boat || event.getCaught() instanceof LivingEntity) && !ResAdmin.isResAdmin(player)) {
            FlagPermissions perm = plugin.getPermsByLoc(event.getCaught().getLocation());
            ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getCaught().getLocation());
            if (perm.has(Flags.hook, FlagCombo.OnlyFalse)) {
                event.setCancelled(true);
                if (res != null)
                    lm.Residence_FlagDeny.sendMessage(player, Flags.hook, res.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeDayNight(ResidenceFlagChangeEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getFlag().equalsIgnoreCase(Flags.day.toString()) &&
            !event.getFlag().equalsIgnoreCase(Flags.night.toString()))
            return;

        switch (event.getNewState()) {
        case NEITHER:
        case FALSE:
            for (Player one : event.getResidence().getPlayersInResidence())
                one.resetPlayerTime();
            break;
        case INVALID:
            break;
        case TRUE:
            if (event.getFlag().equalsIgnoreCase(Flags.day.toString()))
                for (Player one : event.getResidence().getPlayersInResidence())
                    one.setPlayerTime(6000L, false);
            if (event.getFlag().equalsIgnoreCase(Flags.night.toString()))
                for (Player one : event.getResidence().getPlayersInResidence())
                    one.setPlayerTime(14000L, false);
            break;
        default:
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeGlow(ResidenceFlagChangeEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getFlag().equalsIgnoreCase(Flags.glow.toString()))
            return;

        switch (event.getNewState()) {
        case NEITHER:
        case FALSE:
            if (Version.isCurrentEqualOrHigher(Version.v1_9_R1) && event.getFlag().equalsIgnoreCase(Flags.glow.toString()))
                for (Player one : event.getResidence().getPlayersInResidence())
                    one.setGlowing(false);
            break;
        case INVALID:
            break;
        case TRUE:
            if (event.getFlag().equalsIgnoreCase(Flags.glow.toString()) && Version.isCurrentEqualOrHigher(Version.v1_9_R1))
                for (Player one : event.getResidence().getPlayersInResidence()) {
                    one.setGlowing(true);
                }
            break;
        default:
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDeleteEvent(ResidenceDeleteEvent event) {
        if (event.isCancelled())
            return;

        ClaimedResidence res = event.getResidence();
        if (res.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) || res.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
            for (Player one : event.getResidence().getPlayersInResidence())
                one.setWalkSpeed(0.2F);

        if (res.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) || res.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
            for (Player one : event.getResidence().getPlayersInResidence())
                one.resetPlayerWeather();

        if (res.getPermissions().has(Flags.fly, FlagCombo.OnlyTrue))
            for (Player one : event.getResidence().getPlayersInResidence())
                fly(one, false, event.getResidence());

        if (res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue) && Version.isCurrentEqualOrHigher(Version.v1_9_R1))
            for (Player one : event.getResidence().getPlayersInResidence())
                one.setGlowing(false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        plugin.getPermissionManager().removeFromCache(player);

        checkSpecialFlags(player, null, plugin.getResidenceManager().getByLoc(player.getLocation()));

        plugin.getPlayerManager().getResidencePlayer(player).onQuit();
        plugin.getTeleportMap().remove(player.getUniqueId());
        playerTempData.clearCache(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeWSpeed(ResidenceFlagChangeEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getFlag().equalsIgnoreCase(Flags.wspeed1.toString()) &&
            !event.getFlag().equalsIgnoreCase(Flags.wspeed2.toString()))
            return;

        switch (event.getNewState()) {
        case NEITHER:
        case FALSE:
            for (Player one : event.getResidence().getPlayersInResidence())
                one.setWalkSpeed(0.2F);
            break;
        case INVALID:
            break;
        case TRUE:
            if (event.getFlag().equalsIgnoreCase(Flags.wspeed1.toString())) {
                for (Player one : event.getResidence().getPlayersInResidence())
                    one.setWalkSpeed(plugin.getConfigManager().getWalkSpeed1().floatValue());
                if (event.getResidence().getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
                    event.getResidence().getPermissions().setFlag(Flags.wspeed2.toString(), FlagState.NEITHER);
            } else if (event.getFlag().equalsIgnoreCase(Flags.wspeed2.toString())) {
                for (Player one : event.getResidence().getPlayersInResidence())
                    one.setWalkSpeed(plugin.getConfigManager().getWalkSpeed2().floatValue());
                if (event.getResidence().getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
                    event.getResidence().getPermissions().setFlag(Flags.wspeed1.toString(), FlagState.NEITHER);
            }
            break;
        default:
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeWSpeed(ResidenceOwnerChangeEvent event) {
        if (event.isCancelled())
            return;

        ClaimedResidence res = event.getResidence();
        if (res.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) || res.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
            for (Player one : event.getResidence().getPlayersInResidence())
                one.setWalkSpeed(0.2F);

        if (res.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) || res.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
            for (Player one : event.getResidence().getPlayersInResidence())
                one.resetPlayerWeather();

        if (res.getPermissions().has(Flags.fly, FlagCombo.OnlyTrue))
            for (Player one : event.getResidence().getPlayersInResidence())
                fly(one, false, event.getResidence());

        if (res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue) && Version.isCurrentEqualOrHigher(Version.v1_9_R1))
            for (Player one : event.getResidence().getPlayersInResidence())
                one.setGlowing(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeJump(ResidenceFlagChangeEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getFlag().equalsIgnoreCase(Flags.jump2.toString()) &&
            !event.getFlag().equalsIgnoreCase(Flags.jump3.toString()))
            return;

        switch (event.getNewState()) {
        case NEITHER:
        case FALSE:
        case INVALID:
            break;
        case TRUE:
            if (event.getFlag().equalsIgnoreCase(Flags.jump2.toString())) {
                if (event.getResidence().getPermissions().has(Flags.jump3, FlagCombo.OnlyTrue))
                    event.getResidence().getPermissions().setFlag(Flags.jump3.toString(), FlagState.NEITHER);
            } else if (event.getFlag().equalsIgnoreCase(Flags.jump3.toString()) && event.getResidence().getPermissions().has(Flags.jump2, FlagCombo.OnlyTrue)) {
                event.getResidence().getPermissions().setFlag(Flags.jump2.toString(), FlagState.NEITHER);
            }
            break;
        default:
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeSunRain(ResidenceFlagChangeEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getFlag().equalsIgnoreCase(Flags.sun.toString()) && !event.getFlag().equalsIgnoreCase(Flags.rain.toString()))
            return;

        switch (event.getNewState()) {
        case NEITHER:
        case FALSE:
            for (Player one : event.getResidence().getPlayersInResidence())
                one.resetPlayerWeather();
            break;
        case INVALID:
            break;
        case TRUE:
            if (event.getFlag().equalsIgnoreCase(Flags.sun.toString()))
                for (Player player : event.getResidence().getPlayersInResidence())
                    player.setPlayerWeather(WeatherType.CLEAR);
            if (event.getFlag().equalsIgnoreCase(Flags.rain.toString()))
                for (Player player : event.getResidence().getPlayersInResidence())
                    player.setPlayerWeather(WeatherType.DOWNFALL);
            break;
        default:
            break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeFly(ResidenceFlagChangeEvent event) {
        if (event.isCancelled())
            return;

        if (!event.getFlag().equalsIgnoreCase(Flags.fly.toString()))
            return;

        switch (event.getNewState()) {
        case NEITHER:
        case FALSE:
            for (Player one : event.getResidence().getPlayersInResidence())
                fly(one, false, event.getResidence());
            break;
        case INVALID:
            break;
        case TRUE:
            for (Player one : event.getResidence().getPlayersInResidence())
                fly(one, true, event.getResidence());
            break;
        default:
            break;
        }
    }

    private boolean canUseCommand(String command, List<String> whiteListed, List<String> blackListed) {
        int white = 0;
        for (String oneWhite : whiteListed) {
            String t = oneWhite.toLowerCase();
            if (command.startsWith("/" + t)) {
                if (t.contains("_") && t.split("_").length > white)
                    white = t.split("_").length;
                else if (white == 0)
                    white = 1;
            }
        }

        int black = 0;
        for (String oneBlack : blackListed) {
            String t = oneBlack.toLowerCase();
            if (command.startsWith("/" + t)) {
                if (command.contains("_"))
                    black = t.split("_").length;
                else
                    black = 1;
                break;
            }
        }

        if (black == 0)
            for (String oneBlack : blackListed) {
                String t = oneBlack.toLowerCase();
                if (t.equalsIgnoreCase("*")) {
                    if (command.contains("_"))
                        black = command.split("_").length;
                    else
                        black = 1;
                    break;
                }
            }

        return white != 0 && white >= black || black == 0;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.command.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();

        FlagPermissions perms = plugin.getPermsByLocForPlayer(player.getLocation(), player);

        FlagPermissions globalPerm = plugin.getWorldFlags().getPerms(player);
        boolean globalLimited = globalPerm.playerHas(player, Flags.command, FlagCombo.OnlyFalse);
        boolean areaAllowed = perms.playerHas(player, Flags.command, FlagCombo.OnlyTrue);

        if (!globalLimited && !areaAllowed)
            return;

        if (plugin.getPermissionManager().isResidenceAdmin(player))
            return;

        if (ResPerm.bypass_command.hasPermission(player, 10000L))
            return;

        ClaimedResidence res = getCurrentResidence(player.getUniqueId());

        String msg = event.getMessage().replace(" ", "_").toLowerCase();

        if (res == null) {
            if (!globalLimited)
                return;
            if (canUseCommand(msg, globalPerm.getCMDWhiteList(), globalPerm.getCMDBlackList()))
                return;
            event.setCancelled(true);
            lm.Residence_BaseFlagDeny.sendMessage(player, Flags.command);
            return;
        }

        List<String> w = new ArrayList<String>(res.getCmdWhiteList());
        List<String> b = new ArrayList<String>(res.getCmdBlackList());

        if (!areaAllowed) {
            w.clear();
            b.clear();
        }

        if (globalPerm.isInheritCMDLimits()) {
            w.addAll(globalPerm.getCMDWhiteList());
            b.removeAll(globalPerm.getCMDWhiteList());
            b.addAll(globalPerm.getCMDBlackList());
        }

        if (canUseCommand(msg, w, b))
            return;

        event.setCancelled(true);
        lm.Residence_FlagDeny.sendMessage(player, Flags.command, res.getName());

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        Block block = event.getClickedBlock();

        if (block == null || !CMIMaterial.isSign(block.getType()))
            return;

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        Location loc = block.getLocation();

        Signs s = plugin.getSignUtil().getSigns().getResSign(loc);

        if (s == null)
            return;

        ClaimedResidence res = s.getResidence();

        boolean ForSale = res.isForSell();
        boolean ForRent = res.isForRent();
        String landName = res.getName();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (ForSale) {
                Bukkit.dispatchCommand(player, "res market buy " + landName);
                return;
            }

            if (ForRent) {
                if (res.isRented() && player.isSneaking())
                    Bukkit.dispatchCommand(player, "res market release " + landName);
                else {
                    boolean stage = true;
                    if (player.isSneaking())
                        stage = false;
                    Bukkit.dispatchCommand(player, "res market rent " + landName + " " + stage);
                }
                return;
            }
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && ForRent && res.isRented() && plugin.getRentManager().getRentingPlayer(res).equals(player.getName())) {
            plugin.getRentManager().payRent(player, res, false);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignCreate(SignChangeEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Block block = event.getBlock();

        if (!(block.getState() instanceof Sign))
            return;

        Sign sign = (Sign) block.getState();

        if (!CMIChatColor.stripColor(event.getLine(0)).equalsIgnoreCase(lm.Sign_TopLine.getMessage()))
            return;

        Signs signInfo = new Signs();

        Location loc = sign.getLocation();

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        ClaimedResidence res = null;
        if (!event.getLine(1).equalsIgnoreCase("")) {

            String resname = event.getLine(1);
            if (!event.getLine(2).equalsIgnoreCase(""))
                resname += "." + event.getLine(2);
            if (!event.getLine(3).equalsIgnoreCase(""))
                resname += "." + event.getLine(3);

            res = plugin.getResidenceManager().getByName(resname);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(player);
                return;
            }
        } else {
            res = plugin.getResidenceManager().getByLoc(loc);
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (res.getSignsInResidence().size() >= plugin.getConfigManager().getSignsMaxPerResidence()) {
            lm.Sign_TooMany.sendMessage(player);
            return;
        }

        final ClaimedResidence residence = res;

        signInfo.setResidence(res);
        signInfo.setLocation(loc);
        plugin.getSignUtil().getSigns().addSign(signInfo);
        plugin.getSignUtil().saveSigns();

        CMIScheduler.runTaskLater(plugin, () -> plugin.getSignUtil().checkSign(residence), 5L);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignDestroy(BlockBreakEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Block block = event.getBlock();

        if (block == null)
            return;

        if (!CMIMaterial.isSign(block.getType()))
            return;

        Location loc = block.getLocation();
        if (event.getPlayer().hasMetadata("NPC"))
            return;

        Signs s = plugin.getSignUtil().getSigns().getResSign(loc);
        if (s == null)
            return;

        plugin.getSignUtil().getSigns().removeSign(s);
        if (s.getResidence() != null)
            s.getResidence().getSignsInResidence().remove(s);
        plugin.getSignUtil().saveSigns();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getChatManager().removeFromChannel(uuid);
        plugin.getPlayerListener().removePlayerResidenceChat(uuid);
        plugin.getAutoSelectionManager().remove(uuid);

        playerPersistentData.remove(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.nofly.isGlobalyEnabled())
            return;
        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        FlagPermissions perms = plugin.getPermsByLocForPlayer(player.getLocation(), player);

        f: if ((player.getAllowFlight() || player.isFlying()) && perms.has(Flags.nofly, false) && !ResAdmin.isResAdmin(player)
            && !ResPerm.bypass_nofly.hasPermission(player, 10000L)) {

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
            if (res != null && res.isOwner(player))
                break f;

            Location lc = player.getLocation();
            Location location = new Location(lc.getWorld(), lc.getX(), lc.getBlockY(), lc.getZ());
            location.setPitch(lc.getPitch());
            location.setYaw(lc.getYaw());
            int from = location.getBlockY();

            int maxH = location.getWorld().getHighestBlockAt(location).getLocation().getBlockY() + 3;

            if (location.getWorld().getEnvironment() == Environment.NETHER)
                maxH = 100;

            for (int i = 0; i < maxH; i++) {
                location.setY(from - i);
                Block block = location.getBlock();
                if (!isEmptyBlock(block)) {
                    location.setY(from - i + 1);
                    break;
                }
                if (location.getBlockY() <= 0) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    lm.Residence_FlagDeny.sendMessage(player, Flags.nofly, location.getWorld().getName());
                    return;
                }
            }
            lm.Residence_FlagDeny.sendMessage(player, Flags.nofly, location.getWorld().getName());
            player.closeInventory();
            PaperLib.teleportAsync(player, location);

            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
//        lastUpdate.put(player.getUniqueId(), 0L);
        playerTempData.get(player).setLastUpdate(0L);
        if (plugin.getPermissionManager().isResidenceAdmin(player)) {
            ResAdmin.turnResAdminOn(player);
        }

        CMIScheduler.runAtEntityLater(plugin, player, () -> {
            handleNewLocation(player, player.getLocation(), true);
        }, 1L);

        plugin.getPlayerManager().playerJoin(player);

        if (ResPerm.versioncheck.hasPermission(player)) {
            CMIVersionChecker.VersionCheck(player, 11480, plugin.getDescription());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSpawn(PlayerRespawnEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getRespawnLocation().getWorld()))
            return;
        Location loc = event.getRespawnLocation();
        Boolean bed = event.isBedSpawn();
        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
        if (res == null)
            return;

        if (!res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse)) {
            return;
        }
        if (bed) {
            loc = player.getWorld().getSpawnLocation();
        }
        res = plugin.getResidenceManager().getByLoc(loc);
        if (res != null && res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse)) {
            res.kickFromResidence(player);
        }

        lm.General_NoSpawn.sendMessage(player);
        if (loc != null)
            event.setRespawnLocation(loc);

    }

    private boolean isContainer(Material mat, Block block) {
        return FlagPermissions.getMaterialUseFlagList().containsKey(mat) && FlagPermissions.getMaterialUseFlagList().get(mat).equals(Flags.container)
            || plugin.getConfigManager().getCustomContainers().contains(block.getType());
    }

    private boolean isCanUseEntity_RClickOnly(Material mat, Block block) {

        switch (mat.name()) {
        case "ITEM_FRAME":
        case "BEACON":
        case "FLOWER_POT":
        case "COMMAND":
        case "ANVIL":
        case "LECTERN":
        case "CHIPPED_ANVIL":
        case "DAMAGED_ANVIL":
        case "CAKE_BLOCK":
        case "DIODE":
        case "DIODE_BLOCK_OFF":
        case "DIODE_BLOCK_ON":
        case "COMPARATOR":
        case "REPEATER":
        case "REDSTONE_COMPARATOR":
        case "REDSTONE_COMPARATOR_OFF":
        case "REDSTONE_COMPARATOR_ON":
        case "BED_BLOCK":
        case "WORKBENCH":
        case "CRAFTING_TABLE":
        case "CRAFTER":
        case "BREWING_STAND":
        case "ENCHANTMENT_TABLE":
        case "ENCHANTING_TABLE":
        case "DAYLIGHT_DETECTOR":
        case "DAYLIGHT_DETECTOR_INVERTED":
        case "SUSPICIOUS_GRAVEL":
        case "SUSPICIOUS_SAND":
            return true;
        default:
            break;
        }

        CMIMaterial cmat = CMIMaterial.get(mat);
        if (cmat != null) {

            if (cmat.isPotted())
                return true;

            if (cmat.isCake())
                return true;

            if (cmat.isCandle())
                return true;

            if (cmat.isCandleCake())
                return true;

            if (cmat.equals(CMIMaterial.CAMPFIRE) || cmat.equals(CMIMaterial.SOUL_CAMPFIRE))
                return true;
        }

        return plugin.getConfigManager().getCustomRightClick().contains(block.getType());
    }

    public static boolean isCanUseEntity_BothClick(Material mat, Block block) {
        CMIMaterial m = CMIMaterial.get(mat);
        if (m.isDoor())
            return true;
        if (m.isButton())
            return true;
        if (m.isGate())
            return true;
        if (m.isTrapDoor())
            return true;

        if (m.isBed())
            return true;

        switch (m) {
        case LEVER:
        case PISTON:
        case STICKY_PISTON:
        case NOTE_BLOCK:
        case DRAGON_EGG:
            return true;
        default:
            return Residence.getInstance().getConfigManager().getCustomBothClick().contains(block.getType());
        }
    }

    public static boolean isEmptyBlock(Block block) {
        CMIMaterial cb = CMIMaterial.get(block);

        switch (cb) {
        case COBWEB:
        case STRING:
        case WALL_SIGN:
        case VINE:
        case TRIPWIRE_HOOK:
        case TRIPWIRE:
        case PAINTING:
        case ITEM_FRAME:
        case GLOW_ITEM_FRAME:
        case NONE:
            return true;
        default:
            break;
        }

        if (cb.isSapling())
            return true;
        if (cb.isAir())
            return true;
        if (cb.isButton())
            return true;

        return false;
    }

    private boolean isCanUseEntity(Material mat, Block block) {
        return isCanUseEntity_BothClick(mat, block) || isCanUseEntity_RClickOnly(mat, block);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerEnderCrystalInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        if (block.getType() != Material.BEDROCK && block.getType() != Material.OBSIDIAN)
            return;

        Player player = event.getPlayer();

        ItemStack iih = null;

        try {
            if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
                iih = event.getItem();
            else
                iih = CMIItemStack.getItemInMainHand(player);
        } catch (Throwable e) {
            iih = CMIItemStack.getItemInMainHand(player);
        }

        if (iih == null)
            return;

        if (!iih.getType().toString().equals("END_CRYSTAL"))
            return;

        if (player.hasMetadata("NPC"))
            return;
        if (ResAdmin.isResAdmin(player))
            return;
        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
        if (hasplace)
            return;

        event.setCancelled(true);
        lm.Flag_Deny.sendMessage(player, Flags.build);
        return;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSpawnerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        if (block.getType() != Material.SPAWNER)
            return;

        Player player = event.getPlayer();

        ItemStack iih = null;

        try {
            if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
                iih = event.getItem();
            else
                iih = CMIItemStack.getItemInMainHand(player);
        } catch (Throwable e) {
            iih = CMIItemStack.getItemInMainHand(player);
        }

        if (iih == null)
            return;

        if (!iih.getType().toString().endsWith("_EGG"))
            return;

        if (player.hasMetadata("NPC"))
            return;
        if (ResAdmin.isResAdmin(player))
            return;
        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
        if (hasplace)
            return;

        event.setCancelled(true);
        lm.Flag_Deny.sendMessage(player, Flags.build);
        return;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerFireInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        Block relativeBlock = block.getRelative(event.getBlockFace());

        if (relativeBlock == null)
            return;

        if (relativeBlock.getType() != Material.FIRE)
            return;

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        if (ResAdmin.isResAdmin(player))
            return;
        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
        if (hasplace)
            return;

        event.setCancelled(true);
        lm.Flag_Deny.sendMessage(player, Flags.build);
        return;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlatePress(PlayerInteractEvent event) {
        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getAction() != Action.PHYSICAL)
            return;
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        CMIMaterial mat = CMIMaterial.get(block);
        if (!ResAdmin.isResAdmin(player)) {
            boolean hasuse = perms.playerHas(player, Flags.use, true);
            boolean haspressure = perms.playerHas(player, Flags.pressure, hasuse);
            if ((!hasuse && !haspressure || !haspressure) && mat.isPlate()
                && !ResPerm.bypass_use.hasPermission(player, 10000L)) {
                event.setCancelled(true);
            }
        }
        if (!perms.playerHas(player, Flags.trample, perms.playerHas(player, Flags.build, true)) && (mat == CMIMaterial.FARMLAND || mat == CMIMaterial.SOUL_SAND)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSelection(PlayerInteractEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        @SuppressWarnings("deprecation")
        CMIMaterial heldItem = CMIMaterial.get(player.getItemInHand());

        if (heldItem != plugin.getConfigManager().getSelectionTool()) {
            return;
        }

        if (plugin.getWorldEditTool() == plugin.getConfigManager().getSelectionTool())
            return;

        if (player.getGameMode() == GameMode.CREATIVE)
            event.setCancelled(true);

        if (player.hasMetadata("NPC"))
            return;
        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        boolean resadmin = ResAdmin.isResAdmin(player);
        if (ResPerm.select.hasPermission(player) || ResPerm.create.hasPermission(player) && !ResPerm.select.hasSetPermission(player) || group
            .canCreateResidences() && !ResPerm.create.hasSetPermission(player) && !ResPerm.select.hasSetPermission(player) || resadmin) {

            Block block = event.getClickedBlock();

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                Location loc = block.getLocation();
                plugin.getSelectionManager().placeLoc1(player, loc, true);
                lm.Select_PrimaryPoint.sendMessage(player, lm.General_CoordsTop.getMessage(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                event.setCancelled(true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Utils.isMainHand(event)) {
                Location loc = block.getLocation();
                plugin.getSelectionManager().placeLoc2(player, loc, true);
                lm.Select_SecondaryPoint.sendMessage(player, lm.General_CoordsBottom.getMessage(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                event.setCancelled(true);
            }

            if (plugin.getSelectionManager().hasPlacedBoth(player)) {
                plugin.getSelectionManager().showSelectionInfoInActionBar(player);
                plugin.getSelectionManager().updateLocations(player);
            }
        }
        return;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInfoCheck(PlayerInteractEvent event) {
        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        Block block = event.getClickedBlock();
        if (block == null)
            return;
        Player player = event.getPlayer();

        ItemStack item = event.getItem();
        if (item == null)
            return;

        CMIMaterial heldItem = CMIMaterial.get(item);

        if (heldItem != plugin.getConfigManager().getInfoTool())
            return;

        if (this.isContainer(block.getType(), block))
            return;
        if (player.hasMetadata("NPC"))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
        if (res != null)
            plugin.getResidenceManager().printAreaInfo(res, player, false);
        else
            lm.Residence_NoResHere.sendMessage(player);

        event.setCancelled(true);

    }

    private static boolean placingMinecart(Block block, ItemStack item) {
        return block != null && block.getType().name().contains("RAIL") && item != null && item.getType().name().contains("MINECART");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerharvest(PlayerInteractEvent event) {

        if (Version.isCurrentEqualOrLower(Version.v1_16_R1))
            return;

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        try {
            if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND)
                return;
        } catch (Exception e) {
        }
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());

        if (res != null && res.isOwner(player))
            return;

        CMIMaterial mat = CMIMaterial.get(block);

        if (!mat.equals(CMIMaterial.SWEET_BERRY_BUSH) && !mat.equals(CMIMaterial.CAVE_VINES) && !mat.equals(CMIMaterial.CAVE_VINES_PLANT))
            return;

        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        if (!perms.playerHas(player, Flags.harvest, true)) {
            lm.Flag_Deny.sendMessage(player, Flags.harvest);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getPlayer() == null)
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();

        Block block = event.getClickedBlock();
        if (block == null)
            return;

        ItemStack iih = event.getItem();
        CMIMaterial heldItem = CMIMaterial.get(iih);

        Material mat = block.getType();

        if (!(event.getAction() == Action.PHYSICAL || (isContainer(mat, block) || isCanUseEntity_RClickOnly(mat, block)) && event.getAction() == Action.RIGHT_CLICK_BLOCK
            || isCanUseEntity_BothClick(mat, block)) && !heldItem.equals(plugin.getConfigManager().getSelectionTool()) && !heldItem.equals(plugin.getConfigManager().getInfoTool())
            && (!heldItem.isDye() && !heldItem.equals(CMIMaterial.GLOW_INK_SAC)) && !heldItem.equals(CMIMaterial.ARMOR_STAND) && !heldItem.isBoat() && !placingMinecart(block, iih)) {
            return;
        }

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        boolean resadmin = ResAdmin.isResAdmin(player);
        if (resadmin)
            return;

        if (!heldItem.isNone() && heldItem.isValidItem() && !plugin.getItemManager().isAllowed(heldItem.getMaterial(), plugin.getPlayerManager().getResidencePlayer(player).getGroup(), player.getWorld()
            .getName())) {
            lm.General_ItemBlacklisted.sendMessage(player);
            event.setCancelled(true);
            return;
        }

        CMIMaterial blockM = CMIMaterial.get(block);

        FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            CMIMaterial btype = CMIMaterial.get(block);

            if (heldItem.isDye() || heldItem.equals(CMIMaterial.GLOW_INK_SAC)) {

                if (heldItem.equals(CMIMaterial.BONE_MEAL) &&
                    (btype == CMIMaterial.GRASS_BLOCK ||
                        btype == CMIMaterial.GRASS ||
                        btype == CMIMaterial.SHORT_GRASS ||
                        btype == CMIMaterial.TALL_GRASS ||
                        btype == CMIMaterial.TALL_SEAGRASS ||
                        btype == CMIMaterial.MOSS_BLOCK ||
                        btype == CMIMaterial.BIG_DRIPLEAF_STEM ||
                        btype == CMIMaterial.BIG_DRIPLEAF ||
                        btype == CMIMaterial.SMALL_DRIPLEAF ||
                        btype.isSapling()) ||
                    heldItem == CMIMaterial.COCOA_BEANS && blockM == CMIMaterial.JUNGLE_WOOD) {
                    FlagPermissions tperms = plugin.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
                    if (!tperms.playerHas(player, Flags.build, true)) {
                        lm.Flag_Deny.sendMessage(player, Flags.build);
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (heldItem.equals(CMIMaterial.ARMOR_STAND) || heldItem.isBoat()) {
                FlagPermissions tperms = plugin.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
                if (!tperms.playerHas(player, Flags.build, true)) {
                    lm.Flag_Deny.sendMessage(player, Flags.build);
                    event.setCancelled(true);
                    return;
                }
            }
            if (placingMinecart(block, iih) && !perms.playerHas(player, Flags.build, true)) {
                lm.Flag_Deny.sendMessage(player, Flags.build);
                event.setCancelled(true);
                return;

            }

            if (btype.isSign() && !perms.playerHas(player, Flags.use, true)) {
                lm.Flag_Deny.sendMessage(player, Flags.use);
                event.setCancelled(true);
                return;

            }

            if (heldItem.equals(CMIMaterial.BRUSH)) {
                ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
                if (res != null && !res.getPermissions().playerHas(player, Flags.brush, FlagCombo.OnlyTrue)) {
                    lm.Flag_Deny.sendMessage(player, Flags.brush);
                    event.setCancelled(true);
                    return;

                }
            }

        }

        if (isContainer(mat, block) || isCanUseEntity(mat, block)) {

            boolean hasuse = perms.playerHas(player, Flags.use, true);

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());

            if (res != null && res.getRaid().isUnderRaid() && res.getRaid().isDefender(player) && !ConfigManager.RaidDefenderContainerUsage) {
                Flags result = FlagPermissions.getMaterialUseFlagList().get(mat);
                if (result != null && result.equals(Flags.container)) {
                    event.setCancelled(true);
                    lm.Raid_cantDo.sendMessage(player);
                    return;
                }
            }

            if (res == null || !res.isOwner(player)) {

                Flags result = FlagPermissions.getMaterialUseFlagList().get(mat);
                if (result != null) {

                    main: if (!perms.playerHas(player, result, hasuse)) {

                        if (hasuse || result.equals(Flags.container)) {

                            if (res != null && res.getRaid().isUnderRaid() && res.getRaid().isAttacker(player)) {
                                break main;
                            }
                            if (!ResPerm.bypass_container.hasPermission(player, 10000L)) {
                                event.setCancelled(true);
                                lm.Flag_Deny.sendMessage(player, result);
                            }
                            return;
                        }

                        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

                            if (res != null && res.getRaid().isUnderRaid() && res.getRaid().isAttacker(player)) {
                                break main;
                            }

                            switch (result) {
                            case door:
                                if (ResPerm.bypass_door.hasPermission(player, 10000L))
                                    break main;
                                break;
                            case button:
                                if (ResPerm.bypass_button.hasPermission(player, 10000L))
                                    break main;
                                break;
                            }
                            event.setCancelled(true);
                            lm.Flag_Deny.sendMessage(player, result);
                            return;
                        }

                        if (isCanUseEntity_BothClick(mat, block)) {

                            if (res != null && res.getRaid().isUnderRaid() && res.getRaid().isAttacker(player)) {
                                break main;
                            }
                            event.setCancelled(true);
                            lm.Flag_Deny.sendMessage(player, result);
                        }
                        return;
                    }
                }
            }

            if (plugin.getConfigManager().getCustomContainers().contains(mat)) {
                if (!perms.playerHas(player, Flags.container, hasuse)
                    || !ResPerm.bypass_container.hasPermission(player, 10000L)) {
                    event.setCancelled(true);
                    lm.Flag_Deny.sendMessage(player, Flags.container);
                    return;
                }
            }

            if (plugin.getConfigManager().getCustomBothClick().contains(mat) && !hasuse) {
                event.setCancelled(true);
                lm.Flag_Deny.sendMessage(player, Flags.use);
                return;
            }
            if (plugin.getConfigManager().getCustomRightClick().contains(mat) && event.getAction() == Action.RIGHT_CLICK_BLOCK && !hasuse) {
                event.setCancelled(true);
                lm.Flag_Deny.sendMessage(player, Flags.use);
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTradeEntity(PlayerInteractEntityEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();
        /* Trade */
        if (ent.getType() != EntityType.VILLAGER)
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());

        if (res != null && res.getPermissions().playerHas(player, Flags.trade, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(player, Flags.trade, res.getName());
            event.setCancelled(true);
        }
    }

    private static boolean canRide(Entity entity) {

        CMIEntityType type = CMIEntityType.get(entity);

        if (type == null)
            return false;

        switch (type) {
        case HORSE:
        case DONKEY:
        case PIG:
        case LLAMA:
        case TRADER_LLAMA:
        case STRIDER:
        case SKELETON_HORSE:
        case ZOMBIE_HORSE:
        case MULE:
        case CAMEL:
        case HAPPY_GHAST:
            return true;
        default:
            return false;
        }

    }

    private static boolean canHaveContainer(Entity entity) {

        CMIEntityType type = CMIEntityType.get(entity);

        if (type == null)
            return false;

        switch (type) {
        case HORSE:
        case DONKEY:
        case LLAMA:
            return true;
        default:
            return false;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractWithHorse(PlayerInteractEntityEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();

        if (!canHaveContainer(ent))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
        if (res == null)
            return;

        boolean hasContainerBypass = ResPerm.bypass_container.hasPermission(player, 10000L);

        if (!hasContainerBypass && !res.isOwner(player) && res.getPermissions().playerHas(player, Flags.container, FlagCombo.OnlyFalse) && player.isSneaking()) {
            lm.Residence_FlagDeny.sendMessage(player, Flags.container, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractWithRidable(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.riding.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();

        if (!canRide(ent))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
        if (res == null)
            return;

        if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.riding, FlagCombo.TrueOrNone)) {
            lm.Residence_FlagDeny.sendMessage(player, Flags.riding, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractWithMinecartStorage(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();

        CMIEntityType type = CMIEntityType.get(ent);

        if (type == null)
            return;

        if (!type.equals(CMIEntityType.CHEST_MINECART) &&
            !type.equals(CMIEntityType.HOPPER_MINECART) &&
            !type.equals(CMIEntityType.ALLAY))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
        if (res == null)
            return;

        boolean hasContainerBypass = ResPerm.bypass_container.hasPermission(player, 10000L);

        if (!hasContainerBypass && !res.isOwner(player) && res.getPermissions().playerHas(player, Flags.container, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(player, Flags.container, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractWithMinecart(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.riding.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();

        if (ent.getType() != EntityType.MINECART && !(ent instanceof Boat))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
        if (res == null)
            return;
        if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.riding, FlagCombo.OnlyFalse)) {
            lm.Residence_FlagDeny.sendMessage(player, Flags.riding, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDyeSheep(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.dye.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();
        /* Dye */
        if (ent.getType() != EntityType.SHEEP)
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
        if (res == null)
            return;
        if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.dye, FlagCombo.OnlyFalse)) {
            ItemStack iih = CMIItemStack.getItemInMainHand(player);
            ItemStack iiho = CMILib.getInstance().getReflectionManager().getItemInOffHand(player);
            if (iih == null && iiho == null)
                return;
            if (iih != null && !CMIMaterial.isDye(iih.getType()) && iiho != null && !CMIMaterial.isDye(iiho.getType()))
                return;

            lm.Residence_FlagDeny.sendMessage(player, Flags.dye, res.getName());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.shear.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getEntity();

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
        if (res == null)
            return;

        if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.shear, FlagCombo.OnlyFalse)) {
            ItemStack iih = CMIItemStack.getItemInMainHand(player);
            ItemStack iiho = CMILib.getInstance().getReflectionManager().getItemInOffHand(player);
            if (iih == null && iiho == null)
                return;
            if (iih != null && !CMIMaterial.SHEARS.equals(iih.getType()) && iiho != null && !CMIMaterial.SHEARS.equals(iiho.getType()))
                return;
            lm.Residence_FlagDeny.sendMessage(player, Flags.shear, res.getName());
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractAtArmoStand(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();

        ItemStack item = CMIItemStack.getItemInMainHand(player);

        try {
            if (event.getHand() == EquipmentSlot.OFF_HAND)
                item = CMIItemStack.getItemInOffHand(player);
        } catch (Throwable e) {
        }

        if (!CMIMaterial.get(item).equals(CMIMaterial.NAME_TAG))
            return;

        FlagPermissions perms = Residence.getInstance().getPermsByLocForPlayer(ent.getLocation(), player);

        if (perms.playerHas(player, Flags.nametag, FlagCombo.OnlyFalse)) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.nametag);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerItemFrameInteract(PlayerInteractEntityEvent event) {
        // Disabling listener if flag disabled globally
        if (!Flags.container.isGlobalyEnabled())
            return;

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Entity ent = event.getRightClicked();

        if (!CMIEntity.isItemFrame(ent)) {
            return;
        }

        /* Container - ItemFrame protection */
        if (!(ent instanceof Hanging))
            return;

        Material heldItem = CMIItemStack.getItemInMainHand(player).getType();

        FlagPermissions perms = plugin.getPermsByLocForPlayer(ent.getLocation(), player);
        String world = player.getWorld().getName();

        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = resPlayer.getGroup();

        if (!plugin.getItemManager().isAllowed(heldItem, group, world)) {
            lm.General_ItemBlacklisted.sendMessage(player);
            event.setCancelled(true);
            return;
        }

        boolean hasContainerBypass = ResPerm.bypass_container.hasPermission(player, 10000L);
//
        if (!hasContainerBypass && !perms.playerHas(player, Flags.container, perms.playerHas(player, Flags.use, true))) {
            event.setCancelled(true);
            lm.Flag_Deny.sendMessage(player, Flags.container);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        // disabling event on world

        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        Location loc = event.getBlockClicked().getLocation().clone();

        if (Version.isCurrentHigher(Version.v1_12_R1)) {

            if (Version.isCurrentHigher(Version.v1_13_R1) && event.getBlockClicked().getBlockData() instanceof org.bukkit.block.data.Waterlogged) {
                org.bukkit.block.data.Waterlogged waterloggedBlock = (org.bukkit.block.data.Waterlogged) event.getBlockClicked().getBlockData();
                if (waterloggedBlock.isWaterlogged())
                    loc.add(event.getBlockFace().getDirection());
            } else
                try {
                    loc.add(event.getBlockFace().getDirection());
                } catch (Throwable e) {
                }
        }

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
        if (res != null) {
            if (plugin.getConfigManager().preventRentModify() && plugin.getConfigManager().enabledRentSystem()) {
                if (plugin.getRentManager().isRented(res.getName())) {
                    lm.Rent_ModifyDeny.sendMessage(player);
                    event.setCancelled(true);
                    return;
                }
            }

            Material mat = event.getBucket();
            if ((res.getPermissions().playerHas(player, Flags.build, FlagCombo.OnlyFalse))
                && plugin.getConfigManager().getNoPlaceWorlds().contains(loc.getWorld().getName())) {
                if (mat == Material.LAVA_BUCKET || mat == Material.WATER_BUCKET) {
                    lm.Flag_Deny.sendMessage(player, Flags.build);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        FlagPermissions perms = plugin.getPermsByLocForPlayer(loc, player);
        if (!perms.playerHas(player, Flags.build, true)) {
            lm.Flag_Deny.sendMessage(player, Flags.build);
            event.setCancelled(true);
            return;
        }

        Material mat = event.getBucket();
        int level = plugin.getConfigManager().getPlaceLevel();
        if (res == null && plugin.getConfigManager().isNoLavaPlace() && loc.getBlockY() >= level - 1 && plugin.getConfigManager()
            .getNoPlaceWorlds().contains(loc.getWorld().getName())) {
            if (mat == Material.LAVA_BUCKET) {
                lm.General_CantPlaceLava.sendMessage(player, level);
                event.setCancelled(true);
                return;
            }
        }

        if (res == null && plugin.getConfigManager().isNoWaterPlace() && loc.getBlockY() >= level - 1 && plugin.getConfigManager()
            .getNoPlaceWorlds().contains(loc.getWorld().getName()) && mat == Material.WATER_BUCKET) {
            lm.General_CantPlaceWater.sendMessage(player, level);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (ResAdmin.isResAdmin(player))
            return;

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
        if (res != null && plugin.getConfigManager().preventRentModify() && plugin.getConfigManager().enabledRentSystem() && plugin.getRentManager().isRented(res.getName())) {
            lm.Rent_ModifyDeny.sendMessage(player);
            event.setCancelled(true);
            return;
        }

        FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
        boolean hasdestroy = perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true));
        if (!hasdestroy) {
            lm.Flag_Deny.sendMessage(player, Flags.destroy);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {

        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();

        if (player.hasMetadata("NPC"))
            return;

        Location loc = event.getTo();
        boolean handled = handleNewLocation(player, loc, false);
        if (ResAdmin.isResAdmin(player)) {
            return;
        }

        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
        if (res == null)
            return;

        if (event.getCause() == TeleportCause.COMMAND || event.getCause() == TeleportCause.NETHER_PORTAL || event
            .getCause() == TeleportCause.PLUGIN) {
            if (res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse) && !res.isOwner(player)
                && !ResPerm.bypass_tp.hasPermission(player, 10000L) && !ResPerm.admin_move.hasPermission(player, 10000L)) {
                event.setCancelled(true);
                lm.Residence_MoveDeny.sendMessage(player, res.getName());
                return;
            }
        } else if (event.getCause() == TeleportCause.ENDER_PEARL && (res.getPermissions().playerHas(player, Flags.enderpearl, FlagCombo.OnlyFalse) || res.getPermissions().playerHas(player, Flags.move,
            FlagCombo.OnlyFalse))) {
            event.setCancelled(true);
            lm.Residence_FlagDeny.sendMessage(player, Flags.enderpearl, res.getName());
            return;
        }
        if ((event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND) && plugin.getConfigManager().isBlockAnyTeleportation() && !res.isOwner(player) && res.getPermissions()
            .playerHas(player, Flags.tp, FlagCombo.OnlyFalse) && !ResPerm.admin_tp.hasPermission(player)) {
            event.setCancelled(true);
            lm.General_TeleportDeny.sendMessage(player, res.getName());
            return;
        }
        if (Utils.isChorusTeleport(event.getCause()) && !res.isOwner(player) && res.getPermissions().playerHas(player, Flags.chorustp, FlagCombo.OnlyFalse) && !ResPerm.admin_tp.hasPermission(
            player)) {
            event.setCancelled(true);
            lm.Residence_FlagDeny.sendMessage(player, Flags.chorustp, res.getName());
            return;
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getEntity().getWorld()))
            return;
        Player player = event.getEntity();
        if (player == null)
            return;
        if (player.hasMetadata("NPC"))
            return;
        Location loc = player.getLocation();
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
        if (res == null)
            return;

        if (res.getPermissions().has(Flags.keepinv, false)) {
            event.setKeepInventory(true);
            if (Version.isCurrentEqualOrHigher(Version.v1_14_R1))
                event.getDrops().clear();
        }

        if (res.getPermissions().has(Flags.keepexp, false)) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
        }

        if (res.getPermissions().has(Flags.respawn, false) && Version.isSpigot())
            CMIScheduler.runTaskLater(plugin, () -> {
                try {
                    event.getEntity().spigot().respawn();
                } catch (Throwable e) {
                }
            }, 20L);
    }

    public static Location getSafeLocation(Location loc) {

        int curY = loc.getBlockY();

        for (int i = 0; i <= curY; i++) {
            Block block = loc.clone().add(0, -i, 0).getBlock();
            if (!block.isEmpty() && block.getLocation().clone().add(0, 1, 0).getBlock().isEmpty() && block.getLocation().clone().add(0, 2, 0).getBlock().isEmpty())
                return loc.clone().add(0, -i + 1, 0);
        }

        for (int i = 0; i <= loc.getWorld().getMaxHeight() - curY; i++) {
            Block block = loc.clone().add(0, i, 0).getBlock();
            if (!block.isEmpty() && block.getLocation().clone().add(0, 1, 0).getBlock().isEmpty() && block.getLocation().clone().add(0, 2, 0).getBlock().isEmpty())
                return loc.clone().add(0, i + 1, 0);
        }

        return null;
    }

    private Location getFlyTeleportLocation(Player player, ClaimedResidence oldRes) {
        Location loc = getSafeLocation(player.getLocation());

        if (loc != null)
            return loc;

        if (oldRes != null) {
            if (Flags.tp.isGlobalyEnabled() && !oldRes.getPermissions().playerHas(player, Flags.tp, FlagCombo.OnlyFalse) || ResPerm.admin_tp.hasPermission(player, 10000L))
                loc = oldRes.getTeleportLocation(player, false);
        }

        if (loc != null)
            return loc;

        // get defined land location in case no safe landing spot are found
        loc = plugin.getConfigManager().getFlyLandLocation();

        if (loc != null)
            return loc;

        // get main world spawn location in case valid location is not found
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    private void fly(Player player, boolean state, ClaimedResidence oldRes) {
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;
        if (ResPerm.bypass_fly.hasPermission(player, 10000L))
            return;
        if (!state) {

            // Lets not disable fly mode if player has access to fly command from another plugin
            if (player.hasPermission("cmi.command.fly") || player.hasPermission("essentials.fly"))
                return;
            boolean land = player.isFlying();
            player.setFlying(false);

            player.setAllowFlight(false);
            if (land) {
                Location loc = getFlyTeleportLocation(player, oldRes);
                player.closeInventory();
                PaperLib.teleportAsync(player, loc);
            }
            player.setFlying(false);
            player.setAllowFlight(false);
        } else {

            player.setAllowFlight(true);
            CMIScheduler.runAtEntityLater(plugin, player, () -> {
                ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

                if (res == null || !res.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue) && player.isOnline()) {
                    if (player.hasPermission("cmi.command.fly") || player.hasPermission("essentials.fly"))
                        return;

                    player.setFlying(false);
                    player.setAllowFlight(false);
                } else if (res != null && res.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue)) {
                    // Secondary set of flight mode in case another plugin overrides previous set
                    player.setAllowFlight(true);
                }
            }, 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceChange(ResidenceChangedEvent event) {

        ClaimedResidence newRes = event.getTo();
        ClaimedResidence oldRes = event.getFrom();

        Player player = event.getPlayer();
        if (player == null)
            return;

        checkSpecialFlags(player, newRes, oldRes);
    }

    private void checkSpecialFlags(Player player, ClaimedResidence newRes, ClaimedResidence oldRes) {

        if (newRes == null && oldRes != null) {
            if (Flags.night.isGlobalyEnabled() && oldRes.getPermissions().has(Flags.night, FlagCombo.OnlyTrue) || Flags.day.isGlobalyEnabled() && oldRes.getPermissions().has(Flags.day, FlagCombo.OnlyTrue))
                player.resetPlayerTime();

            if (Flags.wspeed1.isGlobalyEnabled() && oldRes.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) || Flags.wspeed2.isGlobalyEnabled() && oldRes.getPermissions().has(Flags.wspeed2,
                FlagCombo.OnlyTrue)) {
                player.setWalkSpeed(0.2F);
            }

            if (Flags.sun.isGlobalyEnabled() && oldRes.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) || Flags.rain.isGlobalyEnabled() && oldRes.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
                player.resetPlayerWeather();

            if (Flags.fly.isGlobalyEnabled() && oldRes.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue)) {
                fly(player, false, oldRes);
            }

            if (Flags.glow.isGlobalyEnabled() && Version.isCurrentEqualOrHigher(Version.v1_9_R1) && oldRes.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
                player.setGlowing(false);

            return;
        }

        if (newRes != null && oldRes != null && !newRes.equals(oldRes)) {
            if (Flags.glow.isGlobalyEnabled() && Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
                if (newRes.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
                    player.setGlowing(true);
                else if (oldRes.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
                    player.setGlowing(false);
            }

            if (Flags.fly.isGlobalyEnabled()) {
                if (newRes.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue))
                    fly(player, true, oldRes);
                else if (oldRes.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue) && !newRes.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue))
                    fly(player, false, oldRes);
            }

            boolean updated = false;
            if (Flags.day.isGlobalyEnabled()) {
                if (newRes.getPermissions().has(Flags.day, FlagCombo.OnlyTrue)) {
                    updated = true;
                    player.setPlayerTime(6000L, false);
                } else if (oldRes.getPermissions().has(Flags.day, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.day, FlagCombo.OnlyTrue)) {
                    player.resetPlayerTime();
                }
            }

            if (Flags.night.isGlobalyEnabled()) {
                if (newRes.getPermissions().has(Flags.night, FlagCombo.OnlyTrue)) {
                    player.setPlayerTime(14000L, false);
                } else if (!updated && oldRes.getPermissions().has(Flags.night, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.night, FlagCombo.OnlyTrue)) {
                    player.resetPlayerTime();
                }
            }

            if (Flags.wspeed1.isGlobalyEnabled()) {
                if (newRes.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
                    player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed1().floatValue());
                else if (oldRes.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
                    player.setWalkSpeed(0.2F);
            }

            if (Flags.wspeed2.isGlobalyEnabled()) {
                if (newRes.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue)) {
                    player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed2().floatValue());
                } else if (oldRes.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
                    player.setWalkSpeed(0.2F);
            }

            if (Flags.sun.isGlobalyEnabled()) {
                if (newRes.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue)) {
                    player.setPlayerWeather(WeatherType.CLEAR);
                } else if (oldRes.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue))
                    player.resetPlayerWeather();
            }

            if (Flags.rain.isGlobalyEnabled()) {
                if (newRes.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue)) {
                    player.setPlayerWeather(WeatherType.DOWNFALL);
                } else if (oldRes.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue) && !newRes.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
                    player.resetPlayerWeather();
            }
            return;
        }

        if (newRes != null && oldRes == null) {
            if (Flags.glow.isGlobalyEnabled() && Version.isCurrentEqualOrHigher(Version.v1_9_R1) && newRes.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue)) {
                player.setGlowing(true);
            }

            if (Flags.fly.isGlobalyEnabled() && newRes.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue)) {
                fly(player, true, oldRes);
            }

            if (Flags.day.isGlobalyEnabled() && newRes.getPermissions().has(Flags.day, FlagCombo.OnlyTrue))
                player.setPlayerTime(6000L, false);

            if (Flags.night.isGlobalyEnabled() && newRes.getPermissions().has(Flags.night, FlagCombo.OnlyTrue))
                player.setPlayerTime(14000L, false);

            if (Flags.wspeed1.isGlobalyEnabled() && newRes.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
                player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed1().floatValue());

            if (Flags.wspeed2.isGlobalyEnabled() && newRes.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
                player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed2().floatValue());

            if (Flags.sun.isGlobalyEnabled() && newRes.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue))
                player.setPlayerWeather(WeatherType.CLEAR);

            if (Flags.rain.isGlobalyEnabled() && newRes.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
                player.setPlayerWeather(WeatherType.DOWNFALL);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        Player player = event.getPlayer();
        if (player == null)
            return;

        if (player.hasMetadata("NPC"))
            return;

        Location locfrom = event.getFrom();
        Location locto = event.getTo();
        if (locfrom.getBlockX() == locto.getBlockX() && locfrom.getBlockY() == locto.getBlockY() && locfrom.getBlockZ() == locto.getBlockZ())
            return;

        long last = playerTempData.get(player).getLastUpdate();
        if (System.currentTimeMillis() - last < plugin.getConfigManager().getMinMoveUpdateInterval())
            return;

        playerTempData.get(player).setLastUpdate(System.currentTimeMillis());

        boolean handled = handleNewLocation(player, locto, true);
        if (!handled)
            event.setCancelled(true);

        if (Teleporting.getTeleportDelayMap().isEmpty())
            return;

        if (plugin.getConfigManager().getTeleportDelay() <= 0 || !Teleporting.isUnderTeleportDelay(player.getUniqueId()))
            return;

        Teleporting.cancelTeleportDelay(player.getUniqueId());

        lm.General_TeleportCanceled.sendMessage(player);
        if (plugin.getConfigManager().isTeleportTitleMessage())
            CMITitleMessage.send(player, "", "");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveInVehicle(VehicleMoveEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getVehicle().getWorld()))
            return;

        if (event.getVehicle().getPassenger() == null)
            return;

        List<Entity> ent = new ArrayList<Entity>();

        if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
            ent.addAll(event.getVehicle().getPassengers());
        else
            ent.add(event.getVehicle().getPassenger());

        for (Entity one : ent) {

            if (!(one instanceof Player))
                continue;

            Player player = (Player) one;
            if (player == null)
                continue;

            if (player.hasMetadata("NPC"))
                continue;

            Location locfrom = event.getFrom();
            Location locto = event.getTo();
            if (locfrom.getBlockX() == locto.getBlockX() && locfrom.getBlockY() == locto.getBlockY() && locfrom.getBlockZ() == locto.getBlockZ())
                continue;

            long last = playerTempData.get(player).getLastUpdate();
            if (System.currentTimeMillis() - last < plugin.getConfigManager().getMinMoveUpdateInterval())
                continue;

            playerTempData.get(player).setLastUpdate(System.currentTimeMillis());

            boolean handled = handleNewLocation(player, locto, true);
            if (!handled) {
                PaperLib.teleportAsync(event.getVehicle(), event.getFrom());
            }

            if (Teleporting.getTeleportDelayMap().isEmpty())
                continue;

            if (plugin.getConfigManager().getTeleportDelay() <= 0 || !Teleporting.isUnderTeleportDelay(player.getUniqueId()))
                continue;

            Teleporting.cancelTeleportDelay(player.getUniqueId());
            lm.General_TeleportCanceled.sendMessage(player);
            if (plugin.getConfigManager().isTeleportTitleMessage())
                CMITitleMessage.send(player, "", "");
        }
    }

    private boolean teleport(Player player, Location loc) {

        if (player == null || !player.isOnline() || loc == null)
            return false;
        try {
            PaperLib.teleportAsync(player, loc);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void PlayerToggleFlightEvent(PlayerToggleFlightEvent event) {

        Player player = event.getPlayer();
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

        if (res == null)
            return;

        if (!Flags.nofly.isGlobalyEnabled() || !event.isFlying() || !res.getPermissions().playerHas(player, Flags.nofly, FlagCombo.OnlyTrue) || ResAdmin.isResAdmin(player)
            || res.isOwner(player) || ResPerm.bypass_nofly.hasPermission(player, 10000L))
            return;

        lm.Residence_FlagDeny.sendMessage(player, Flags.nofly, res.getName());
        event.setCancelled(true);
    }

    private boolean checkNoFly(Player player, ClaimedResidence res, Location loc) {

        Location location = player.getLocation().clone();

        int from = location.getBlockY();
        int to = CMIWorld.getMinHeight(loc.getWorld());
        boolean teleported = false;

        for (int i = from; i > to; i--) {
            location.setY(i);
            Block block = location.getBlock();
            if (!isEmptyBlock(block)) {
                break;
            }
        }

        location.add(0, 1, 0);

        int distance = (int) (player.getLocation().getY() - location.getY());

        if (distance > 4) {
            Location lastLoc = player.getLocation().clone();
            player.closeInventory();

            ClaimedResidence current = plugin.getResidenceManager().getByLoc(lastLoc);
            if (current != null && Flags.tp.isGlobalyEnabled() && current.getPermissions().playerHas(player, Flags.tp, FlagCombo.OnlyFalse) && !ResPerm.admin_tp.hasPermission(player, 10000L)) {
                teleported = current.kickFromResidence(player);
            }

            if (!teleported) {
                current = plugin.getResidenceManager().getByLoc(location.clone().add(0, 0.5, 0));
                if (current == null || Flags.tp.isGlobalyEnabled() && current.getPermissions().playerHas(player, Flags.tp, FlagCombo.OnlyFalse) && !ResPerm.admin_tp.hasPermission(player, 10000L)) {
                    teleported = teleport(player, location.clone().add(0, 0.5, 0));
                } else {
                    teleported = current.kickFromResidence(player);
                }
            }

            player.setFlying(false);

            lm.Residence_FlagDeny.sendMessage(player, Flags.nofly, res.getName());

            return teleported;
        }

        lm.Residence_FlagDeny.sendMessage(player, Flags.nofly, res.getName());

        player.closeInventory();
        player.setFlying(false);

        return true;
    }

    private void bounceAnimation(Player player, ClaimedResidence res) {
        if (!plugin.getConfigManager().BounceAnimation())
            return;

        Visualizer v = new Visualizer(player);
        v.setErrorAreas(res);
        v.setOnce(true);
        plugin.getSelectionManager().showBounds(player, v);
    }

    private void informOnMoveDeny(Player player, ClaimedResidence res) {

        switch (plugin.getConfigManager().getEnterLeaveMessageType()) {
        case ActionBar:
        case TitleBar:
            FlagPermissions perms = res.getPermissions();
            if (perms.has(Flags.title, FlagCombo.TrueOrNone))
                CMIActionBar.send(player, lm.Residence_MoveDeny.getMessage(res.getName()));
            break;
        case ChatBox:
            lm.Residence_MoveDeny.sendMessage(player, res.getName());
            break;
        default:
            break;
        }
    }

    public boolean handleNewLocation(final Player player, Location loc, boolean move) {
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);

        UUID uuid = player.getUniqueId();

        playerTempData tempData = playerTempData.get(uuid);

        ClaimedResidence resOld = tempData.getCurrentResidence();

        boolean changedResidence = !Objects.equals(resOld, res);

        if (res == null)
            playerPersistentData.get(uuid).setLastOutsideLoc(loc);

        if (!changedResidence) {
            // In case we are inside same residence, we can assume we can move there
            if (res != null)
                tempData.setLastInsideLoc(loc);
            return true;
        }

        if (!plugin.getAutoSelectionManager().getList().isEmpty())
            CMIScheduler.runTaskAsynchronously(plugin, () -> plugin.getAutoSelectionManager().UpdateSelection(player));

        if (res == null) {
            tempData.setCurrentResidence(player, res);
            // New ResidenceChangeEvent
            plugin.getServ().getPluginManager().callEvent(new ResidenceChangedEvent(resOld, null, player));
            return true;
        }

        boolean cantMove = Flags.move.isGlobalyEnabled() &&
            res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse) &&
            !ResAdmin.isResAdmin(player) &&
            !res.isOwner(player) &&
            !ResPerm.admin_move.hasPermission(player, 10000L);

        if (!cantMove)
            tempData.setCurrentResidence(player, res);

        boolean teleported = false;
        if (!cantMove && Flags.nofly.isGlobalyEnabled() && player.isFlying() && res.getPermissions().playerHas(player, Flags.nofly, FlagCombo.OnlyTrue) && !ResAdmin.isResAdmin(player)
            && !res.isOwner(player) && !ResPerm.bypass_nofly.hasPermission(player, 10000L)) {
            teleported = checkNoFly(player, res, loc);
        }

        if (move && cantMove) {

            if (res.getRaid().isUnderRaid() && (res.getRaid().isAttacker(player.getUniqueId()) || res.getRaid().isDefender(player.getUniqueId())))
                return true;

            Location lastLoc = tempData.getLastValidLocation(player);

            if (lastLoc == null)
                lastLoc = player.getLocation();

            bounceAnimation(player, res);

            ClaimedResidence preRes = plugin.getResidenceManager().getByLoc(lastLoc);

            if (preRes != null && preRes.getPermissions().playerHas(player, Flags.tp, FlagCombo.OnlyFalse) && !ResPerm.admin_tp.hasPermission(player, 10000L)) {
                res.kickFromResidence(player);
                player.closeInventory();
                informOnMoveDeny(player, res);
                return false;
            }

            StuckInfo info = updateStuckTeleport(player, loc);
            player.closeInventory();
            if (info != null && info.getTimesTeleported() > 5) {
                boolean kicked = res.kickFromResidence(player);
                if (!kicked)
                    teleport(player, lastLoc);
                return false;
            }

            if (!teleported) {
                if (player.getLocation().equals(lastLoc))
                    res.kickFromResidence(player);
                else
                    teleport(player, lastLoc);
            }
            return false;
        }

        if (!cantMove)
            tempData.setLastInsideLoc(loc);

        plugin.getServ().getPluginManager().callEvent(new ResidenceChangedEvent(resOld, res, player));

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceChangeMessagePrint(ResidenceChangedEvent event) {

        ClaimedResidence from = event.getFrom();
        ClaimedResidence to = event.getTo();
        String message = null;

        ClaimedResidence res = from == null ? to : from;

        if (from == null && to != null) {
            message = to.getEnterMessage();
            res = to;
        }

        if (from != null && to == null) {
            message = from.getLeaveMessage();
            res = from;
        }

        if (from != null && to != null) {
            message = to.getEnterMessage();
            res = to;
        }

        Player player = event.getPlayer();
        if (player.hasMetadata("NPC"))
            return;
        if (message != null && !message.isEmpty()) {

            message = lm.Limits_EnterLeavePrefix.getMessage() + message;

            Long time = playerTempData.get(player).getLastEnterLeaveInformTime();
            if (time == null || time + 100L < System.currentTimeMillis()) {

                if (res.getPermissions().has(Flags.title, FlagCombo.TrueOrNone))
                    switch (plugin.getConfigManager().getEnterLeaveMessageType()) {
                    case ActionBar:
                        CMIActionBar.send(player, (new StringBuilder()).append(ChatColor.YELLOW).append(insertMessages(player, res, message))
                            .toString());
                        break;
                    case ChatBox:
                        lm.showMessage(player, ChatColor.YELLOW + this.insertMessages(player, res, message));
                        break;
                    case TitleBar:
                        String title = ChatColor.YELLOW + insertMessages(player, res, message);
                        String subtitle = "";
                        if (title.contains("\\n")) {
                            subtitle = ChatColor.YELLOW + title.split("\\\\n", 2)[1];
                            title = title.split("\\\\n", 2)[0];
                        }
                        CMITitleMessage.send(player, title, subtitle);
                        break;
                    default:
                        break;
                    }
                playerTempData.get(player).setLastEnterLeaveInformTime(System.currentTimeMillis());
            }
        }

        if (to != null && plugin.getConfigManager().isEnterAnimation() && to.isTopArea() && (from == null || from.getTopParent() != to)) {
            to.showBounds(player, true);
        }

        if (from == null || res == null) {
            return;
        }

        if (res != from.getParent() && plugin.getConfigManager().isExtraEnterMessage() && !res.isOwner(player) && (plugin.getRentManager().isForRent(from) || plugin
            .getTransactionManager().isForSale(from))) {
            if (plugin.getRentManager().isForRent(from) && !plugin.getRentManager().isRented(from)) {
                RentableLand rentable = plugin.getRentManager().getRentableLand(from);
                if (rentable != null)
                    CMIActionBar.send(player, lm.Residence_CanBeRented.getMessage(from.getName(), rentable.cost, rentable.days));
            } else if (plugin.getTransactionManager().isForSale(from) && !res.isOwner(player)) {
                int sale = plugin.getTransactionManager().getSaleAmount(from);
                CMIActionBar.send(player, lm.Residence_CanBeBought.getMessage(from.getName(), sale));
            }
        }
    }

    private StuckInfo updateStuckTeleport(Player player, Location loc) {

        if (loc.getY() >= player.getLocation().getY())
            return null;

        StuckInfo info = playerTempData.get(player.getUniqueId()).getStuckTeleportCounter();
        info.updateLastTp();

        return info;
    }

    public String insertMessages(Player player, ClaimedResidence res, String message) {
        try {
            message = message.replace("%playerDisplay", player.getDisplayName());
            message = message.replace("%player", player.getName());
            message = message.replace("%owner", res.getPermissions().getOwner());
            message = message.replace("%residence", res.getName());
            message = message.replace("%zone", res.getResidenceName());
        } catch (Exception ex) {
            return "";
        }
        return message;
    }

    @SuppressWarnings("deprecation")
    public void doHeals() {
        if (!Flags.healing.isGlobalyEnabled())
            return;
        try {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                ClaimedResidence res = getCurrentResidence(player.getUniqueId());

                if (res == null)
                    continue;

                if (!res.getPermissions().has(Flags.healing, false))
                    continue;

                Damageable damage = player;
                double health = damage.getHealth();
                if (health < damage.getMaxHealth() && !player.isDead()) {
                    player.setHealth(health + 1);
                }
            }
        } catch (Exception ex) {
        }
    }

    public void feed() {
        if (!Flags.feed.isGlobalyEnabled())
            return;
        try {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {

                ClaimedResidence res = getCurrentResidence(player.getUniqueId());

                if (res == null)
                    continue;

                if (!res.getPermissions().has(Flags.feed, false))
                    continue;

                int food = player.getFoodLevel();
                if (food < 20 && !player.isDead()) {
                    player.setFoodLevel(food + 1);
                }
            }
        } catch (Exception ex) {
        }
    }

    public void badEffects() {
        if (!Flags.safezone.isGlobalyEnabled())
            return;
        try {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                ClaimedResidence res = getCurrentResidence(player.getUniqueId());
                if (res == null)
                    continue;
                if (!res.getPermissions().has(Flags.safezone, FlagCombo.OnlyTrue))
                    continue;
                if (player.getActivePotionEffects().isEmpty())
                    continue;
                for (PotionEffect one : player.getActivePotionEffects()) {
                    if (plugin.getConfigManager().getNegativePotionEffects().contains(one.getType().getName().toLowerCase()))
                        player.removePotionEffect(one.getType());
                }
            }
        } catch (Exception ex) {
        }
    }

    public void DespawnMobs() {
        if (!Flags.nomobs.isGlobalyEnabled())
            return;

        try {

            Set<ClaimedResidence> residences = new HashSet<ClaimedResidence>();

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                ClaimedResidence res = getCurrentResidence(player.getUniqueId());
                if (res == null)
                    continue;
                if (!res.getPermissions().has(Flags.nomobs, false)) {
                    for (ClaimedResidence sub : res.getSubzonesMap().values()) {
                        if (!sub.getPermissions().has(Flags.nomobs, false)) {
                            continue;
                        }
                        residences.add(sub);
                    }
                    continue;
                }
                residences.add(res);
            }

            int chunkRadius = 3;
            int range = 3 * 16;
            for (ClaimedResidence res : residences) {
                Set<Entity> entities = new HashSet<Entity>();

                World world = Bukkit.getWorld(res.getWorld());

                if (world == null)
                    continue;

                if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
                    for (Player player : res.getPlayersInResidence()) {
                        Vector vloc = player.getLocation().toVector();

                        // Limit check area in case residence is very big
                        BoundingBox searchBox = BoundingBox.of(
                            vloc.clone().subtract(new Vector(range, range, range)),
                            vloc.clone().add(new Vector(range, range, range)));

                        CMIScheduler.runAtLocation(plugin, player.getLocation(), () -> {
                            Set<Entity> ent = new HashSet<>(world.getNearbyEntities(searchBox));
                            processEntities(ent, res);
                        });

                    }
                    continue;
                }

                for (CuboidArea area : res.getAreaMap().values()) {
                    for (ChunkRef chunk : area.getChunks()) {

                        // Checking if chunk is near a player.
                        // In case residence is extremely big it will check all chunks which can cause performance issues
                        boolean near = false;
                        for (Player player : res.getPlayersInResidence()) {
                            int x = player.getLocation().getChunk().getX();
                            int z = player.getLocation().getChunk().getZ();

                            if (CMINumber.abs(x - chunk.getX()) > chunkRadius)
                                continue;

                            if (CMINumber.abs(z - chunk.getZ()) > chunkRadius)
                                continue;
                            near = true;
                            break;
                        }
                        if (!near)
                            continue;
                        entities.addAll(Arrays.asList(world.getChunkAt(chunk.getX(), chunk.getZ()).getEntities()));
                    }
                }

                processEntities(entities, res);
            }
        } catch (Exception ex) {
        }
    }

    private void processEntities(Set<Entity> entities, ClaimedResidence res) {
        for (Entity ent : entities) {
            if (!ResidenceEntityListener.isMonster(ent))
                continue;
            if (!res.containsLoc(ent.getLocation()))
                continue;
            ent.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
            return;
        String pname = event.getPlayer().getName();
        if (!plugin.getConfigManager().chatEnabled() || playerPersistentData.get(event.getPlayer()).isChatEnabled())
            return;

        ChatChannel channel = plugin.getChatManager().getPlayerChannel(event.getPlayer().getUniqueId());
        if (channel != null) {
            channel.chat(pname, event.getMessage());
        }
        event.setCancelled(true);
    }

    public void tooglePlayerResidenceChat(Player player, String residence) {
        playerPersistentData.get(player).setChatEnabled(false);
        lm.Chat_ChatChannelChange.sendMessage(player, residence);
    }

    @Deprecated
    public void removePlayerResidenceChat(String pname) {
        removePlayerResidenceChat(Bukkit.getPlayer(pname));
    }

    public void removePlayerResidenceChat(Player player) {
        if (player == null)
            return;
        removePlayerResidenceChat(player.getUniqueId());
    }

    public void removePlayerResidenceChat(UUID uuid) {
        if (uuid == null)
            return;
        playerPersistentData.get(uuid).setChatEnabled(true);
        lm.Chat_ChatChannelLeave.sendMessage(ResidencePlayer.getOnlinePlayer(uuid));
    }

    @Deprecated
    public ClaimedResidence getCurrentResidence(UUID uuid) {
        return playerTempData.getCurrentResidence(uuid);
    }
}
