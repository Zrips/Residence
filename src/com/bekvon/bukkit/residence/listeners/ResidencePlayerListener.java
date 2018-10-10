package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
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
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.StuckInfo;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.gui.SetFlag;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.GetTime;

import cmiLib.ActionBarTitleMessages;
import cmiLib.ItemManager.CMIMaterial;
import cmiLib.VersionChecker.Version;

public class ResidencePlayerListener implements Listener {

    protected Map<String, String> currentRes;
    protected Map<String, Long> lastUpdate;
    protected Map<String, Location> lastOutsideLoc;
    protected Map<UUID, StuckInfo> stuckTeleportCounter;
    protected int minUpdateTime;
    protected boolean chatenabled;
    protected List<String> playerToggleChat = new ArrayList<String>();

    public Map<String, SetFlag> GUI = new HashMap<String, SetFlag>();

    private Residence plugin;

    public ResidencePlayerListener(Residence plugin) {
	currentRes = new HashMap<String, String>();
	lastUpdate = new HashMap<String, Long>();
	lastOutsideLoc = new HashMap<String, Location>();
	stuckTeleportCounter = new HashMap<UUID, StuckInfo>();
	playerToggleChat.clear();
	minUpdateTime = plugin.getConfigManager().getMinMoveUpdateInterval();
	chatenabled = plugin.getConfigManager().chatEnabled();
	for (Player player : Bukkit.getOnlinePlayers()) {
	    lastUpdate.put(player.getName(), System.currentTimeMillis());
	}
	this.plugin = plugin;
    }

    public Map<String, SetFlag> getGUImap() {
	return GUI;
    }

    public void reload() {
	currentRes = new HashMap<String, String>();
	lastUpdate = new HashMap<String, Long>();
	lastOutsideLoc = new HashMap<String, Location>();
	stuckTeleportCounter = new HashMap<UUID, StuckInfo>();
	playerToggleChat.clear();
	minUpdateTime = plugin.getConfigManager().getMinMoveUpdateInterval();
	chatenabled = plugin.getConfigManager().chatEnabled();
	for (Player player : Bukkit.getOnlinePlayers()) {
	    lastUpdate.put(player.getName(), System.currentTimeMillis());
	}
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
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getItem().getLocation());
	if (res == null)
	    return;
	if (event.getPlayer().hasMetadata("NPC"))
	    return;
	if (!res.getPermissions().playerHas(event.getPlayer(), Flags.itempickup, FlagCombo.OnlyFalse))
	    return;
	if (event.getPlayer().hasPermission("residence.flag.itempickup.bypass"))
	    return;
	event.setCancelled(true);
	event.getItem().setPickupDelay(plugin.getConfigManager().getItemPickUpDelay() * 20);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
	if (!Flags.itemdrop.isGlobalyEnabled())
	    return;
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getPlayer().getLocation());
	if (res == null)
	    return;
	if (event.getPlayer().hasMetadata("NPC"))
	    return;
	if (!res.getPermissions().playerHas(event.getPlayer(), Flags.itemdrop, FlagCombo.OnlyFalse))
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

	if (rPlayer.getResList().size() == 0)
	    return;

	ClaimedResidence res = rPlayer.getMainResidence();

	if (res == null)
	    return;

	String honorific = plugin.getConfigManager().getGlobalChatFormat().replace("%1", res.getTopParentName());

	String format = event.getFormat();
	format = format.replace("%1$s", honorific + "%1$s");
	event.setFormat(format);
    }

    // Changing chat prefix variable to job name
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChatGlobalLow(AsyncPlayerChatEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	if (!plugin.getConfigManager().isGlobalChatEnabled())
	    return;
	if (plugin.getConfigManager().isGlobalChatSelfModify())
	    return;
	Player player = event.getPlayer();

	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

	if (rPlayer == null)
	    return;

	if (rPlayer.getResList().size() == 0)
	    return;

	ClaimedResidence res = rPlayer.getMainResidence();

	if (res == null)
	    return;

	String honorific = plugin.getConfigManager().getGlobalChatFormat().replace("%1", res.getTopParentName());
	if (honorific.equalsIgnoreCase(" "))
	    honorific = "";
	String format = event.getFormat();
	if (!format.contains("{residence}"))
	    return;
	format = format.replace("{residence}", honorific);
	event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceBackup(ResidenceFlagChangeEvent event) {
	if (!event.getFlag().equalsIgnoreCase(Flags.backup.getName()))
	    return;
	Player player = event.getPlayer();
	if (!plugin.getConfigManager().RestoreAfterRentEnds)
	    return;
	if (!plugin.getConfigManager().SchematicsSaveOnFlagChange)
	    return;
	if (plugin.getSchematicManager() == null)
	    return;
	if (player != null && !player.hasPermission("residence.backup"))
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
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		if (!player.isOnline())
		    return;
		List<String> list = plugin.getRentManager().getRentedLandsList(player.getName());
		if (list.isEmpty())
		    return;
		for (String one : list) {
		    RentedLand rentedland = plugin.getRentManager().getRentedLand(one);
		    if (rentedland == null)
			continue;
		    if (rentedland.AutoPay)
			continue;
		    if (rentedland.endTime - System.currentTimeMillis() < plugin.getConfigManager().getRentInformBefore() * 60 * 24 * 7) {
			plugin.msg(player, lm.Residence_EndingRent, one, GetTime.getTime(rentedland.endTime));
		    }
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
	if (plugin.getNms().isArmorStandEntity(event.getCaught().getType()) || event.getCaught() instanceof Boat || event.getCaught() instanceof LivingEntity) {
	    FlagPermissions perm = plugin.getPermsByLoc(event.getCaught().getLocation());
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getCaught().getLocation());
	    if (perm.has(Flags.hook, FlagCombo.OnlyFalse)) {
		event.setCancelled(true);
		if (res != null)
		    plugin.msg(player, lm.Residence_FlagDeny, Flags.hook.getName(), res.getName());
		return;
	    }
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeDayNight(ResidenceFlagChangeEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getFlag().equalsIgnoreCase(Flags.day.getName()) &&
	    !event.getFlag().equalsIgnoreCase(Flags.night.getName()))
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
	    if (event.getFlag().equalsIgnoreCase(Flags.day.getName()))
		for (Player one : event.getResidence().getPlayersInResidence())
		    one.setPlayerTime(6000L, false);
	    if (event.getFlag().equalsIgnoreCase(Flags.night.getName()))
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

	if (!event.getFlag().equalsIgnoreCase(Flags.glow.getName()))
	    return;

	switch (event.getNewState()) {
	case NEITHER:
	case FALSE:
	    if (Version.isCurrentEqualOrHigher(Version.v1_9_R1) && event.getFlag().equalsIgnoreCase(Flags.glow.getName()))
		for (Player one : event.getResidence().getPlayersInResidence())
		    one.setGlowing(false);
	    break;
	case INVALID:
	    break;
	case TRUE:
	    if (event.getFlag().equalsIgnoreCase(Flags.glow.getName()))
		if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
		    for (Player one : event.getResidence().getPlayersInResidence())
			one.setGlowing(true);
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

	if (event.getPlayer() != null && res.getPermissions().playerHas(event.getPlayer(), Flags.fly, FlagCombo.OnlyTrue))
	    for (Player one : event.getResidence().getPlayersInResidence())
		fly(one, false);

	if (res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue) && Version.isCurrentEqualOrHigher(Version.v1_9_R1))
	    for (Player one : event.getResidence().getPlayersInResidence())
		one.setGlowing(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

	Player player = event.getPlayer();
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());

	if (res == null)
	    return;

	if (res.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) || res.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
	    player.setWalkSpeed(0.2F);

	if (res.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) || res.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
	    player.resetPlayerWeather();

	if (event.getPlayer() != null && res.getPermissions().playerHas(event.getPlayer(), Flags.fly, FlagCombo.OnlyTrue))
	    fly(player, false);

	if (res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue) && Version.isCurrentEqualOrHigher(Version.v1_9_R1))
	    player.setGlowing(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeWSpeed(ResidenceFlagChangeEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getFlag().equalsIgnoreCase(Flags.wspeed1.getName()) &&
	    !event.getFlag().equalsIgnoreCase(Flags.wspeed2.getName()))
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
	    if (event.getFlag().equalsIgnoreCase(Flags.wspeed1.getName())) {
		for (Player one : event.getResidence().getPlayersInResidence())
		    one.setWalkSpeed(plugin.getConfigManager().getWalkSpeed1().floatValue());
		if (event.getResidence().getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
		    event.getResidence().getPermissions().setFlag(Flags.wspeed2.getName(), FlagState.NEITHER);
	    } else if (event.getFlag().equalsIgnoreCase(Flags.wspeed2.getName())) {
		for (Player one : event.getResidence().getPlayersInResidence())
		    one.setWalkSpeed(plugin.getConfigManager().getWalkSpeed2().floatValue());
		if (event.getResidence().getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
		    event.getResidence().getPermissions().setFlag(Flags.wspeed1.getName(), FlagState.NEITHER);
	    }
	    break;
	default:
	    break;
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeJump(ResidenceFlagChangeEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getFlag().equalsIgnoreCase(Flags.jump2.getName()) &&
	    !event.getFlag().equalsIgnoreCase(Flags.jump3.getName()))
	    return;

	switch (event.getNewState()) {
	case NEITHER:
	case FALSE:
	case INVALID:
	    break;
	case TRUE:
	    if (event.getFlag().equalsIgnoreCase(Flags.jump2.getName())) {
		if (event.getResidence().getPermissions().has(Flags.jump3, FlagCombo.OnlyTrue))
		    event.getResidence().getPermissions().setFlag(Flags.jump3.getName(), FlagState.NEITHER);
	    } else if (event.getFlag().equalsIgnoreCase(Flags.jump3.getName())) {
		if (event.getResidence().getPermissions().has(Flags.jump2, FlagCombo.OnlyTrue))
		    event.getResidence().getPermissions().setFlag(Flags.jump2.getName(), FlagState.NEITHER);
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

	if (!event.getFlag().equalsIgnoreCase(Flags.sun.getName()) && !event.getFlag().equalsIgnoreCase(Flags.rain.getName()))
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
	    if (event.getFlag().equalsIgnoreCase(Flags.sun.getName()))
		for (Player one : event.getResidence().getPlayersInResidence())
		    one.setPlayerWeather(WeatherType.CLEAR);
	    if (event.getFlag().equalsIgnoreCase(Flags.rain.getName()))
		for (Player one : event.getResidence().getPlayersInResidence())
		    one.setPlayerWeather(WeatherType.DOWNFALL);
	    break;
	default:
	    break;
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFlagChangeFly(ResidenceFlagChangeEvent event) {
	if (event.isCancelled())
	    return;

	if (!event.getFlag().equalsIgnoreCase(Flags.fly.getName()))
	    return;

	switch (event.getNewState()) {
	case NEITHER:
	case FALSE:
	    for (Player one : event.getResidence().getPlayersInResidence())
		fly(one, false);
	    break;
	case INVALID:
	    break;
	case TRUE:
	    for (Player one : event.getResidence().getPlayersInResidence())
		fly(one, true);
	    break;
	default:
	    break;
	}
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.command.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	Player player = event.getPlayer();
	String resname = plugin.getPlayerListener().getCurrentResidenceName(player.getName());
	if (resname == null)
	    return;
	ClaimedResidence res = plugin.getResidenceManager().getByName(resname);
	if (res == null)
	    return;
	if (!res.getPermissions().playerHas(player, Flags.command, FlagCombo.OnlyFalse))
	    return;

	if (plugin.getPermissionManager().isResidenceAdmin(player))
	    return;

	if (player.hasPermission("residence.flag.command.bypass"))
	    return;

	String msg = event.getMessage().replace(" ", "_").toLowerCase();

	int white = 0;
	int black = 0;

	for (String oneWhite : res.getCmdWhiteList()) {
	    String t = oneWhite.toLowerCase();
	    if (msg.startsWith("/" + t)) {
		if (t.contains("_") && t.split("_").length > white)
		    white = t.split("_").length;
		else if (white == 0)
		    white = 1;
	    }
	}

	for (String oneBlack : res.getCmdBlackList()) {
	    String t = oneBlack.toLowerCase();
	    if (msg.startsWith("/" + t)) {
		if (msg.contains("_"))
		    black = t.split("_").length;
		else
		    black = 1;
		break;
	    }
	}

	if (black == 0)
	    for (String oneBlack : res.getCmdBlackList()) {
		String t = oneBlack.toLowerCase();
		if (t.equalsIgnoreCase("*")) {
		    if (msg.contains("_"))
			black = msg.split("_").length;
		    break;
		}
	    }

	if (white != 0 && white >= black || black == 0)
	    return;

	event.setCancelled(true);
	plugin.msg(player, lm.Residence_FlagDeny, Flags.command.getName(), res.getName());

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFlagGuiClick(InventoryClickEvent event) {
	if (this.getGUImap().size() == 0)
	    return;

	Player player = (Player) event.getWhoClicked();

	if (!this.getGUImap().containsKey(player.getName()))
	    return;

	event.setCancelled(true);
	int slot = event.getRawSlot();

	if (slot > 53 || slot < 0)
	    return;

	SetFlag setFlag = this.getGUImap().get(player.getName());
	ClickType click = event.getClick();
	InventoryAction action = event.getAction();
	setFlag.toggleFlag(slot, click, action);
	setFlag.recalculateInv();

	if (!player.getOpenInventory().getTopInventory().getType().equals(InventoryType.CHEST))
	    return;

	player.getOpenInventory().getTopInventory().setContents(setFlag.getInventory().getContents());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFlagGuiClose(InventoryCloseEvent event) {
	if (this.getGUImap().isEmpty())
	    return;
	HumanEntity player = event.getPlayer();
	if (!this.getGUImap().containsKey(player.getName()))
	    return;
	this.getGUImap().remove(player.getName());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
	if (event.getPlayer() == null)
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;

	Block block = event.getClickedBlock();

	if (block == null || block.getState() == null)
	    return;

	if (!(block.getState() instanceof Sign))
	    return;

	Player player = event.getPlayer();
	if (player.hasMetadata("NPC"))
	    return;
	Location loc = block.getLocation();

	for (Signs one : plugin.getSignUtil().getSigns().GetAllSigns()) {
	    if (!one.GetLocation().getWorld().getName().equalsIgnoreCase(loc.getWorld().getName()))
		continue;
	    if (one.GetLocation().getBlockX() != loc.getBlockX())
		continue;
	    if (one.GetLocation().getBlockY() != loc.getBlockY())
		continue;
	    if (one.GetLocation().getBlockZ() != loc.getBlockZ())
		continue;

	    ClaimedResidence res = one.GetResidence();

	    boolean ForSale = res.isForSell();
	    boolean ForRent = res.isForRent();
	    String landName = res.getName();
	    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
		if (ForSale) {
		    Bukkit.dispatchCommand(player, "res market buy " + landName);
		    break;
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
		    break;
		}
	    } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
		if (ForRent && res.isRented() && plugin.getRentManager().getRentingPlayer(res).equals(player.getName())) {
		    plugin.getRentManager().payRent(player, res, false);
		}
	    }
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

	if (!ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase(plugin.msg(lm.Sign_TopLine)))
	    return;

	Signs signInfo = new Signs();

	Location loc = sign.getLocation();

	String landName = null;

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
		plugin.msg(player, lm.Invalid_Residence);
		return;
	    }

	    landName = res.getName();

	} else {
	    res = plugin.getResidenceManager().getByLoc(loc);
	    if (res != null)
		landName = res.getName();
	}

	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return;
	}

	final ClaimedResidence residence = res;

	boolean ForSale = plugin.getTransactionManager().isForSale(landName);
	boolean ForRent = plugin.getRentManager().isForRent(landName);

	int category = 1;
	if (plugin.getSignUtil().getSigns().GetAllSigns().size() > 0)
	    category = plugin.getSignUtil().getSigns().GetAllSigns().get(plugin.getSignUtil().getSigns().GetAllSigns().size() - 1).GetCategory() + 1;

	if (ForSale || ForRent) {
	    signInfo.setCategory(category);
	    signInfo.setResidence(res);
	    signInfo.setLocation(loc);
//	    signInfo.updateLocation();
	    plugin.getSignUtil().getSigns().addSign(signInfo);
	    plugin.getSignUtil().saveSigns();
	}
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		plugin.getSignUtil().CheckSign(residence);
	    }
	}, 5L);
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

	if (!(block.getState() instanceof Sign))
	    return;

	Location loc = block.getLocation();
	if (event.getPlayer().hasMetadata("NPC"))
	    return;
	for (Signs one : plugin.getSignUtil().getSigns().GetAllSigns()) {

	    if (!one.GetLocation().getWorld().getName().equalsIgnoreCase(loc.getWorld().getName()))
		continue;
	    if (one.GetLocation().getBlockX() != loc.getBlockX())
		continue;
	    if (one.GetLocation().getBlockY() != loc.getBlockY())
		continue;
	    if (one.GetLocation().getBlockZ() != loc.getBlockZ())
		continue;

	    plugin.getSignUtil().getSigns().removeSign(one);
	    plugin.getSignUtil().saveSigns();
	    break;
	}
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
	String pname = event.getPlayer().getName();
	currentRes.remove(pname);
	lastUpdate.remove(pname);
	lastOutsideLoc.remove(pname);
	plugin.getChatManager().removeFromChannel(pname);
	plugin.getPlayerListener().removePlayerResidenceChat(pname);
	plugin.addOfflinePlayerToChache(event.getPlayer());
	if (plugin.getAutoSelectionManager().getList().containsKey(pname.toLowerCase()))
	    plugin.getAutoSelectionManager().getList().remove(pname);
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

	f: if ((player.getAllowFlight() || player.isFlying()) && perms.has(Flags.nofly, false) && !plugin.isResAdminOn(player) && !player.hasPermission(
	    "residence.nofly.bypass")) {

	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null && res.isOwner(player))
		break f;

	    Location lc = player.getLocation();
	    Location location = new Location(lc.getWorld(), lc.getX(), lc.getBlockY(), lc.getZ());
	    location.setPitch(lc.getPitch());
	    location.setYaw(lc.getYaw());
	    int from = location.getBlockY();
	    int maxH = location.getWorld().getMaxHeight() - 1;
	    for (int i = 0; i < maxH; i++) {
		location.setY(from - i);
		Block block = location.getBlock();
		if (!plugin.getNms().isEmptyBlock(block)) {
		    location.setY(from - i + 1);
		    break;
		}
		if (location.getBlockY() <= 0) {
		    player.setFlying(false);
		    player.setAllowFlight(false);
		    plugin.msg(player, lm.Residence_FlagDeny, Flags.nofly.getName(), location.getWorld().getName());
		    return;
		}
	    }
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.nofly.getName(), location.getWorld().getName());
	    player.closeInventory();
	    player.teleport(location);
	    player.setFlying(false);
	    player.setAllowFlight(false);
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
	Player player = event.getPlayer();
	lastUpdate.put(player.getName(), 0L);
	if (plugin.getPermissionManager().isResidenceAdmin(player)) {
	    plugin.turnResAdminOn(player);
	}
	handleNewLocation(player, player.getLocation(), true);

	plugin.getPlayerManager().playerJoin(player, false);

	if (player.hasPermission("residence.versioncheck")) {
	    plugin.getVersionChecker().VersionCheck(player);
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
	if (res == null) {
	    return;
	}
	if (!res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse)) {
	    return;
	}
	if (bed) {
	    loc = player.getWorld().getSpawnLocation();
	}
	res = plugin.getResidenceManager().getByLoc(loc);
	if (res != null && res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse)) {
	    loc = res.getOutsideFreeLoc(loc, player);
	}

	plugin.msg(player, lm.General_NoSpawn);
	event.setRespawnLocation(loc);
    }

    @SuppressWarnings("deprecation")
    private boolean isContainer(Material mat, Block block) {
	return FlagPermissions.getMaterialUseFlagList().containsKey(mat) && FlagPermissions.getMaterialUseFlagList().get(mat).equals(Flags.container)
	    || plugin.getConfigManager().getCustomContainers().contains(block.getType().getId());
    }

    @SuppressWarnings("deprecation")
    private boolean isCanUseEntity_RClickOnly(Material mat, Block block) {

	switch (mat.name()) {
	case "ITEM_FRAME":
	case "BEACON":
	case "FLOWER_POT":
	case "COMMAND":
	case "ANVIL":
	case "CAKE_BLOCK":
	case "NOTE_BLOCK":
	case "DIODE":
	case "DIODE_BLOCK_OFF":
	case "DIODE_BLOCK_ON":
	case "REDSTONE_COMPARATOR":
	case "REDSTONE_COMPARATOR_OFF":
	case "REDSTONE_COMPARATOR_ON":
	case "BED_BLOCK":
	case "WORKBENCH":
	case "BREWING_STAND":
	case "ENCHANTMENT_TABLE":
	case "DAYLIGHT_DETECTOR":
	case "DAYLIGHT_DETECTOR_INVERTED":
	    return true;
	default:
	    break;
	}

	CMIMaterial cmat = CMIMaterial.get(mat);
	if (cmat != null) {
	    if (cmat.isPottedFlower()) {
		return true;
	    }
	}

	return plugin.getConfigManager().getCustomRightClick().contains(Integer.valueOf(block.getType().getId()));
    }

    private boolean isCanUseEntity(Material mat, Block block) {
	return plugin.getNms().isCanUseEntity_BothClick(mat, block) || isCanUseEntity_RClickOnly(mat, block);
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

	Player player = event.getPlayer();
	if (player.hasMetadata("NPC"))
	    return;

	FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
	if (relativeBlock.getType() == Material.FIRE) {
	    boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
	    if (!hasplace) {
		event.setCancelled(true);
		plugin.msg(player, lm.Flag_Deny, Flags.build.getName());
		return;
	    }
	}
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
	CMIMaterial mat = CMIMaterial.get(block);
	Player player = event.getPlayer();
	if (player.hasMetadata("NPC"))
	    return;
	FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
	boolean resadmin = plugin.isResAdminOn(player);
	if (!resadmin) {
	    boolean hasuse = perms.playerHas(player, Flags.use, true);
	    boolean haspressure = perms.playerHas(player, Flags.pressure, hasuse);
	    if ((!hasuse && !haspressure || !haspressure) && mat.isPlate()) {
		event.setCancelled(true);
		return;
	    }
	}
	if (!perms.playerHas(player, Flags.trample, perms.playerHas(player, Flags.build, true)) && (mat.equals(CMIMaterial.FARMLAND) || mat.equals(CMIMaterial.SOUL_SAND))) {
	    event.setCancelled(true);
	    return;
	}
	return;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
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

	if (!heldItem.equals(plugin.getConfigManager().getSelectionTool()))
	    return;

	if (plugin.getWorldEditTool().equals(plugin.getConfigManager().getSelectionTool()))
	    return;

	if (player.getGameMode() == GameMode.CREATIVE)
	    event.setCancelled(true);

	boolean resadmin = plugin.isResAdminOn(player);
	if (player.hasMetadata("NPC"))
	    return;
	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (player.hasPermission("residence.select") || player.hasPermission("residence.create") && !player.isPermissionSet("residence.select") || group
	    .canCreateResidences() && !player.isPermissionSet("residence.create") && !player.isPermissionSet("residence.select") || resadmin) {

	    Block block = event.getClickedBlock();

	    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
		Location loc = block.getLocation();
		plugin.getSelectionManager().placeLoc1(player, loc, true);
		plugin.msg(player, lm.Select_PrimaryPoint, plugin.msg(lm.General_CoordsTop, loc.getBlockX(), loc.getBlockY(),
		    loc.getBlockZ()));
		event.setCancelled(true);
	    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.getNms().isMainHand(event)) {
		Location loc = block.getLocation();
		plugin.getSelectionManager().placeLoc2(player, loc, true);
		plugin.msg(player, lm.Select_SecondaryPoint, plugin.msg(lm.General_CoordsBottom, loc.getBlockX(), loc
		    .getBlockY(), loc.getBlockZ()));
		event.setCancelled(true);
	    }

	    if (plugin.getSelectionManager().hasPlacedBoth(player)) {
		plugin.getSelectionManager().showSelectionInfoInActionBar(player);
		plugin.getSelectionManager().updateLocations(player);
	    }
	}
	return;
    }

    @SuppressWarnings("deprecation")
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

	if (!heldItem.equals(plugin.getConfigManager().getInfoTool()))
	    return;

	if (this.isContainer(block.getType(), block))
	    return;
	if (player.hasMetadata("NPC"))
	    return;
	Location loc = block.getLocation();
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
	if (res != null)
	    plugin.getResidenceManager().printAreaInfo(res.getName(), player, false);
	else
	    plugin.msg(player, lm.Residence_NoResHere);

	event.setCancelled(true);
	return;

    }

    private static boolean placingMinecart(Block block, ItemStack item) {
	if (block.getType().name().contains("RAIL") && item.getType().name().contains("MINECART"))
	    return true;
	return false;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
	if (event.getPlayer() == null)
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	Player player = event.getPlayer();
	ItemStack iih = plugin.getNms().itemInMainHand(player);
	CMIMaterial heldItem = CMIMaterial.get(iih);

	Block block = event.getClickedBlock();
	if (block == null)
	    return;

	Material mat = block.getType();

	if (!(event.getAction() == Action.PHYSICAL || (isContainer(mat, block) || isCanUseEntity_RClickOnly(mat, block)) && event.getAction() == Action.RIGHT_CLICK_BLOCK
	    || plugin.getNms().isCanUseEntity_BothClick(mat, block))) {
	    if (!heldItem.equals(plugin.getConfigManager().getSelectionTool()) && !heldItem.equals(plugin.getConfigManager().getInfoTool())
		&& !heldItem.isDye() && !heldItem.equals(CMIMaterial.ARMOR_STAND) && !heldItem.isBoat() && !placingMinecart(block, iih)) {
		return;
	    }
	}

	Debug.D("check pot? "+ isCanUseEntity(mat, block) + " " + mat);

	if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
	    return;

	String world = player.getWorld().getName();

	ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();

	boolean resadmin = plugin.isResAdminOn(player);
	if (!resadmin && !plugin.getItemManager().isAllowed(heldItem.getMaterial(), group, world)) {
	    plugin.msg(player, lm.General_ItemBlacklisted);
	    event.setCancelled(true);
	    return;
	}

	if (resadmin)
	    return;

	CMIMaterial blockM = CMIMaterial.get(block);

	FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
	if (heldItem != null && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
	    if (heldItem.isDye()) {
		if (heldItem.equals(CMIMaterial.BONE_MEAL) && block.getType() == Material.GRASS || heldItem.equals(CMIMaterial.COCOA_BEANS) && blockM.equals(CMIMaterial.JUNGLE_WOOD)) {
		    perms = plugin.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
		    if (!perms.playerHas(player, Flags.build, true)) {
			plugin.msg(player, lm.Flag_Deny, Flags.build.getName());
			event.setCancelled(true);
			return;
		    }
		}
	    }
	    if (heldItem.equals(CMIMaterial.ARMOR_STAND) || heldItem.isBoat()) {
		perms = plugin.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
		if (!perms.playerHas(player, Flags.build, true)) {
		    plugin.msg(player, lm.Flag_Deny, Flags.build.getName());
		    event.setCancelled(true);
		    return;
		}
	    }
	    if (placingMinecart(block, iih)) {
		perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
		if (!perms.playerHas(player, Flags.build, true)) {
		    plugin.msg(player, lm.Flag_Deny, Flags.build.getName());
		    event.setCancelled(true);
		    return;
		}
	    }
	}

	if (isContainer(mat, block) || isCanUseEntity(mat, block)) {
	    boolean hasuse = perms.playerHas(player, Flags.use, true);
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
	    if (res == null || !res.isOwner(player))
		for (Entry<Material, Flags> checkMat : FlagPermissions.getMaterialUseFlagList().entrySet()) {
		    if (mat != checkMat.getKey())
			continue;

		    if (perms.playerHas(player, checkMat.getValue(), hasuse))
			continue;

		    if (hasuse || checkMat.getValue().equals(Flags.container)) {
			event.setCancelled(true);
			plugin.msg(player, lm.Flag_Deny, checkMat.getValue());
			return;
		    }

		    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			event.setCancelled(true);
			plugin.msg(player, lm.Flag_Deny, Flags.use);
		    }
		    return;
		}

	    if (plugin.getConfigManager().getCustomContainers().contains(blockM.getId())) {
		if (!perms.playerHas(player, Flags.container, hasuse)) {
		    event.setCancelled(true);
		    plugin.msg(player, lm.Flag_Deny, Flags.container.getName());
		    return;
		}
	    }
	    if (plugin.getConfigManager().getCustomBothClick().contains(blockM.getId())) {
		if (!hasuse) {
		    event.setCancelled(true);
		    plugin.msg(player, lm.Flag_Deny, Flags.use.getName());
		    return;
		}
	    }
	    if (plugin.getConfigManager().getCustomRightClick().contains(blockM.getId()) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
		if (!hasuse) {
		    event.setCancelled(true);
		    plugin.msg(player, lm.Flag_Deny, Flags.use.getName());
		    return;
		}
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTradeEntity(PlayerInteractEntityEvent event) {

	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();
	/* Trade */
	if (ent.getType() != EntityType.VILLAGER)
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());

	if (res != null && res.getPermissions().playerHas(player, Flags.trade, FlagCombo.OnlyFalse)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.trade.getName(), res.getName());
	    event.setCancelled(true);
	}
    }

    private static boolean canRide(EntityType type) {
	switch (type.name().toLowerCase()) {
	case "horse":
	case "donkey":
	case "llama":
	case "pig":
	    return true;
	}
	return false;
    }

    private static boolean canHaveContainer(EntityType type) {
	switch (type.name().toLowerCase()) {
	case "horse":
	case "donkey":
	case "llama":
	    return true;
	}
	return false;
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();

	if (!canHaveContainer(ent.getType()))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	if (res == null)
	    return;
	if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.container, FlagCombo.OnlyFalse) && player.isSneaking()) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.container.getName(), res.getName());
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();

	if (!canRide(ent.getType()))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	if (res == null)
	    return;
	if (!res.isOwner(player) && !res.getPermissions().playerHas(player, Flags.riding, FlagCombo.TrueOrNone)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.riding.getName(), res.getName());
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();

	if (ent.getType() != EntityType.MINECART_CHEST && ent.getType() != EntityType.MINECART_HOPPER)
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	if (res == null)
	    return;
	if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.container, FlagCombo.OnlyFalse)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.container.getName(), res.getName());
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();

	if (ent.getType() != EntityType.MINECART && ent.getType() != EntityType.BOAT)
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	if (res == null)
	    return;
	if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.riding, FlagCombo.OnlyFalse)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.riding.getName(), res.getName());
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();
	/* Dye */
	if (ent.getType() != EntityType.SHEEP)
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	if (res == null)
	    return;
	if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.dye, FlagCombo.OnlyFalse)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.dye.getName(), res.getName());
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getEntity();

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	if (res == null)
	    return;

	if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.shear, FlagCombo.OnlyFalse)) {
	    plugin.msg(player, lm.Residence_FlagDeny, Flags.shear.getName(), res.getName());
	    event.setCancelled(true);
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
	if (plugin.isResAdminOn(player))
	    return;

	Entity ent = event.getRightClicked();

	/* Container - ItemFrame protection */
	if (!(ent instanceof Hanging))
	    return;

	Hanging hanging = (Hanging) ent;
	if (hanging.getType() != EntityType.ITEM_FRAME) {
	    return;
	}

	Material heldItem = plugin.getNms().itemInMainHand(player).getType();

	FlagPermissions perms = plugin.getPermsByLocForPlayer(ent.getLocation(), player);
	String world = player.getWorld().getName();

	ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();

	if (!plugin.getItemManager().isAllowed(heldItem, group, world)) {
	    plugin.msg(player, lm.General_ItemBlacklisted);
	    event.setCancelled(true);
	    return;
	}
	if (!perms.playerHas(player, Flags.container, perms.playerHas(player, Flags.use, true))) {
	    event.setCancelled(true);
	    plugin.msg(player, lm.Flag_Deny, Flags.container.getName());
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;

	Location loc = event.getBlockClicked().getLocation();

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
	if (res != null) {
	    if (plugin.getConfigManager().preventRentModify() && plugin.getConfigManager().enabledRentSystem()) {
		if (plugin.getRentManager().isRented(res.getName())) {
		    plugin.msg(player, lm.Rent_ModifyDeny);
		    event.setCancelled(true);
		    return;
		}
	    }

	    Material mat = event.getBucket();
	    if ((res.getPermissions().playerHas(player, Flags.build, FlagCombo.OnlyFalse))
		&& plugin.getConfigManager().getNoPlaceWorlds().contains(loc.getWorld().getName())) {
		if (mat == Material.LAVA_BUCKET || mat == Material.WATER_BUCKET) {
		    plugin.msg(player, lm.Flag_Deny, Flags.build);
		    event.setCancelled(true);
		    return;
		}
	    }
	}

	FlagPermissions perms = plugin.getPermsByLocForPlayer(loc, player);
	if (!perms.playerHas(player, Flags.build, true)) {
	    plugin.msg(player, lm.Flag_Deny, Flags.build.getName());
	    event.setCancelled(true);
	    return;
	}

	Material mat = event.getBucket();
	int level = plugin.getConfigManager().getPlaceLevel();
	if (res == null && plugin.getConfigManager().isNoLavaPlace() && loc.getBlockY() >= level - 1 && plugin.getConfigManager()
	    .getNoPlaceWorlds().contains(loc.getWorld().getName())) {
	    if (mat == Material.LAVA_BUCKET) {
		if (!plugin.msg(lm.General_CantPlaceLava).equalsIgnoreCase(""))
		    plugin.msg(player, lm.General_CantPlaceLava, level);
		event.setCancelled(true);
		return;
	    }
	}

	if (res == null && plugin.getConfigManager().isNoWaterPlace() && loc.getBlockY() >= level - 1 && plugin.getConfigManager()
	    .getNoPlaceWorlds().contains(loc.getWorld().getName()))
	    if (mat == Material.WATER_BUCKET) {
		if (!plugin.msg(lm.General_CantPlaceWater).equalsIgnoreCase(""))
		    plugin.msg(player, lm.General_CantPlaceWater, level);
		event.setCancelled(true);
		return;
	    }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
	if (res != null) {
	    if (plugin.getConfigManager().preventRentModify() && plugin.getConfigManager().enabledRentSystem()) {
		if (plugin.getRentManager().isRented(res.getName())) {
		    plugin.msg(player, lm.Rent_ModifyDeny);
		    event.setCancelled(true);
		    return;
		}
	    }
	}

	FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
	boolean hasdestroy = perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true));
	if (!hasdestroy) {
	    plugin.msg(player, lm.Flag_Deny, Flags.destroy.getName());
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

	if (plugin.isResAdminOn(player)) {
	    handleNewLocation(player, loc, false);
	    return;
	}

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
	if (res == null)
	    return;
	if (event.getCause() == TeleportCause.COMMAND || event.getCause() == TeleportCause.NETHER_PORTAL || event
	    .getCause() == TeleportCause.PLUGIN) {
	    if (res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse) && !res.isOwner(player)) {
		event.setCancelled(true);
		plugin.msg(player, lm.Residence_MoveDeny, res.getName());
		return;
	    }
	} else if (event.getCause() == TeleportCause.ENDER_PEARL) {
	    if (res.getPermissions().playerHas(player, Flags.enderpearl, FlagCombo.OnlyFalse)) {
		event.setCancelled(true);
		plugin.msg(player, lm.Residence_FlagDeny, Flags.enderpearl.getName(), res.getName());
		return;
	    }
	}
	if ((event.getCause() == TeleportCause.PLUGIN || event.getCause() == TeleportCause.COMMAND) && plugin.getConfigManager().isBlockAnyTeleportation()) {
	    if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.tp, FlagCombo.OnlyFalse) && !player.hasPermission("residence.admin.tp")) {
		event.setCancelled(true);
		plugin.msg(player, lm.General_TeleportDeny, res.getName());
		return;
	    }
	}
	if (plugin.getNms().isChorusTeleport(event.getCause())) {
	    if (!res.isOwner(player) && res.getPermissions().playerHas(player, Flags.chorustp, FlagCombo.OnlyFalse) && !player.hasPermission("residence.admin.tp")) {
		event.setCancelled(true);
		plugin.msg(player, lm.Residence_FlagDeny, Flags.chorustp.getName(), res.getName());
		return;
	    }
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

	if (res.getPermissions().has(Flags.keepinv, false))
	    event.setKeepInventory(true);

	if (res.getPermissions().has(Flags.keepexp, false)) {
	    event.setKeepLevel(true);
	    event.setDroppedExp(0);
	}

	if (res.getPermissions().has(Flags.respawn, false) && Bukkit.getVersion().toString().contains("Spigot"))
	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		@Override
		public void run() {
		    try {
			event.getEntity().spigot().respawn();
		    } catch (Exception e) {
		    }
		    return;
		}
	    }, 1L);
    }

    private static Location getSafeLocation(Location loc) {

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

    private void fly(Player player, boolean state) {
	if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
	    return;
	if (player.hasPermission("residence.flybypass"))
	    return;
	if (!state) {
	    boolean land = player.isFlying();
	    player.setFlying(state);
	    player.setAllowFlight(state);
	    if (land) {
		Location loc = getSafeLocation(player.getLocation());
		if (loc == null) {
		    // get defined land location in case no safe landing spot are found
		    loc = plugin.getConfigManager().getFlyLandLocation();
		    if (loc == null) {
			// get main world spawn location in case valid location is not found
			loc = Bukkit.getWorlds().get(0).getSpawnLocation();
		    }
		}
		if (loc != null) {
		    player.closeInventory();
		    player.teleport(loc);
		}
	    }
	} else {
	    player.setAllowFlight(state);
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceChange(ResidenceChangedEvent event) {
	ClaimedResidence res = event.getTo();
	ClaimedResidence ResOld = event.getFrom();
	Player player = event.getPlayer();

	if (res == null && ResOld != null) {
	    if (ResOld.getPermissions().has(Flags.night, FlagCombo.OnlyTrue) || ResOld.getPermissions().has(Flags.day, FlagCombo.OnlyTrue))
		player.resetPlayerTime();

	    if (ResOld.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) || ResOld.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
		player.setWalkSpeed(0.2F);

	    if (ResOld.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) || ResOld.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
		player.resetPlayerWeather();

	    if (ResOld.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue))
		fly(player, false);

	    if (Version.isCurrentEqualOrHigher(Version.v1_9_R1) && ResOld.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
		player.setGlowing(false);
	}

	if (res != null && ResOld != null && !res.equals(ResOld)) {
	    if (Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
		if (res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
		    player.setGlowing(true);
		else if (ResOld.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
		    player.setGlowing(false);
	    }

	    if (res.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue))
		fly(player, true);
	    else if (ResOld.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue) && !res.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue))
		fly(player, false);

	    if (res.getPermissions().has(Flags.day, FlagCombo.OnlyTrue))
		player.setPlayerTime(6000L, false);
	    else if (ResOld.getPermissions().has(Flags.day, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.day, FlagCombo.OnlyTrue))
		player.resetPlayerTime();

	    if (res.getPermissions().has(Flags.night, FlagCombo.OnlyTrue))
		player.setPlayerTime(14000L, false);
	    else if (ResOld.getPermissions().has(Flags.night, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.night, FlagCombo.OnlyTrue))
		player.resetPlayerTime();

	    if (res.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
		player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed1().floatValue());
	    else if (ResOld.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
		player.setWalkSpeed(0.2F);

	    if (res.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue)) {
		player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed2().floatValue());
	    } else if (ResOld.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
		player.setWalkSpeed(0.2F);

	    if (res.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue)) {
		player.setPlayerWeather(WeatherType.CLEAR);
	    } else if (ResOld.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue))
		player.resetPlayerWeather();

	    if (res.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue)) {
		player.setPlayerWeather(WeatherType.DOWNFALL);
	    } else if (ResOld.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue) && !res.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
		player.resetPlayerWeather();
	}

	if (res != null && ResOld == null) {
	    if (Version.isCurrentEqualOrHigher(Version.v1_9_R1)) {
		if (res.getPermissions().has(Flags.glow, FlagCombo.OnlyTrue))
		    player.setGlowing(true);
	    }

	    if (res.getPermissions().playerHas(player, Flags.fly, FlagCombo.OnlyTrue))
		fly(player, true);

	    if (res.getPermissions().has(Flags.day, FlagCombo.OnlyTrue))
		player.setPlayerTime(6000L, false);

	    if (res.getPermissions().has(Flags.night, FlagCombo.OnlyTrue))
		player.setPlayerTime(14000L, false);

	    if (res.getPermissions().has(Flags.wspeed1, FlagCombo.OnlyTrue))
		player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed1().floatValue());

	    if (res.getPermissions().has(Flags.wspeed2, FlagCombo.OnlyTrue))
		player.setWalkSpeed(plugin.getConfigManager().getWalkSpeed2().floatValue());

	    if (res.getPermissions().has(Flags.sun, FlagCombo.OnlyTrue))
		player.setPlayerWeather(WeatherType.CLEAR);

	    if (res.getPermissions().has(Flags.rain, FlagCombo.OnlyTrue))
		player.setPlayerWeather(WeatherType.DOWNFALL);
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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

	String name = player.getName();

	if (name == null)
	    return;

	Long last = lastUpdate.get(name);
	long now = System.currentTimeMillis();
	if (last != null)
	    if (now - last < plugin.getConfigManager().getMinMoveUpdateInterval())
		return;

	this.lastUpdate.put(name, now);

	boolean handled = handleNewLocation(player, locto, true);
	if (!handled)
	    event.setCancelled(true);

	if (!plugin.getTeleportDelayMap().isEmpty() && plugin.getConfigManager().getTeleportDelay() > 0 && plugin.getTeleportDelayMap().contains(player
	    .getName())) {
	    plugin.getTeleportDelayMap().remove(player.getName());
	    plugin.msg(player, lm.General_TeleportCanceled);
	    if (plugin.getConfigManager().isTeleportTitleMessage())
		ActionBarTitleMessages.sendTitle(player, "", "");
	}
    }

    private static boolean teleport(Player player, Location loc) {
	if (player == null || !player.isOnline() || loc == null)
	    return false;

//	PlayerTeleportEvent ev = new PlayerTeleportEvent(player, player.getLocation(), loc);
//	Bukkit.getServer().getPluginManager().callEvent(ev);
//	Debug.D("teleporting " + !ev.isCancelled());
	if (!player.teleport(loc))
	    return false;

//	Debug.D("tp " + player.teleport(loc));
	return true;
    }

    public boolean handleNewLocation(final Player player, Location loc, boolean move) {

	String pname = player.getName();
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);

	ClaimedResidence orres = res;
	String areaname = null;
	String subzone = null;
	if (res != null) {
	    areaname = res.getName();
	    while (res.getSubzoneByLoc(loc) != null) {
		res = res.getSubzoneByLoc(player.getLocation());
		subzone = res.getName();
		areaname = areaname + "." + subzone;
	    }
	}

	ClaimedResidence ResOld = null;
	if (currentRes.containsKey(pname)) {
	    ResOld = plugin.getResidenceManager().getByName(currentRes.get(pname));
	    if (ResOld == null) {
		currentRes.remove(pname);
	    } else {
		if (res != null && ResOld.getName().equals(res.getName())) {

		    f: if (Flags.nofly.isGlobalyEnabled() && player.isFlying() && res.getPermissions().playerHas(player, Flags.nofly, FlagCombo.OnlyTrue) && !plugin.isResAdminOn(player) && !player
			.hasPermission(
			    "residence.nofly.bypass")) {
			if (res.isOwner(player))
			    break f;
			Location lc = player.getLocation();
			Location location = new Location(lc.getWorld(), lc.getX(), lc.getBlockY(), lc.getZ());
			location.setPitch(lc.getPitch());
			location.setYaw(lc.getYaw());
			int from = location.getBlockY();
			int maxH = location.getWorld().getMaxHeight() - 1;
			for (int i = 0; i < maxH; i++) {
			    location.setY(from - i);
			    Block block = location.getBlock();
			    if (!plugin.getNms().isEmptyBlock(block)) {
				location.setY(from - i + 1);
				break;
			    }
			    if (location.getBlockY() <= 0) {
				Location lastLoc = lastOutsideLoc.get(pname);
				player.closeInventory();
				boolean teleported = false;
				if (lastLoc != null)
				    teleported = teleport(player, lastLoc);
				else
				    teleported = teleport(player, res.getOutsideFreeLoc(loc, player));
				plugin.msg(player, lm.Residence_FlagDeny, Flags.nofly.getName(), orres.getName());
				if (!teleported)
				    return false;
				return true;
			    }
			}
			plugin.msg(player, lm.Residence_FlagDeny, Flags.nofly.getName(), orres.getName());
			player.closeInventory();
			boolean teleported = teleport(player, location);
			if (!teleported)
			    return false;
			player.setFlying(false);
			player.setAllowFlight(false);
		    }

		    lastOutsideLoc.put(pname, loc);
		    return true;
		}
	    }
	}

	if (!plugin.getAutoSelectionManager().getList().isEmpty()) {
	    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
		@Override
		public void run() {
		    plugin.getAutoSelectionManager().UpdateSelection(player);
		    return;
		}
	    });
	}

	if (res == null) {
	    lastOutsideLoc.put(pname, loc);
	    if (ResOld != null) {

		// New ResidenceChangeEvent
		ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(ResOld, null, player);
		plugin.getServ().getPluginManager().callEvent(chgEvent);

//		String leave = ResOld.getLeaveMessage();
//		if (leave != null && !leave.equals("")) {
//		    if (plugin.getConfigManager().useTitleMessage()) {
//			plugin.getAB().sendTitle(player, ChatColor.YELLOW + insertMessages(player, ResOld.getName(), ResOld, leave));
//		    }
//		    if (plugin.getConfigManager().useActionBar()) {
//			plugin.getAB().send(player, (new StringBuilder()).append(ChatColor.YELLOW).append(insertMessages(player, ResOld.getName(), ResOld, leave))
//			    .toString());
//		    } else {
//			plugin.msg(player, ChatColor.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
//		    }
//		}
		currentRes.remove(pname);
	    }
	    return true;
	}

	if (move) {
	    if (res.getPermissions().playerHas(player, Flags.move, FlagCombo.OnlyFalse) && !plugin.isResAdminOn(player) && !res.isOwner(player) && !player.hasPermission(
		"residence.admin.move")) {

		Location lastLoc = lastOutsideLoc.get(pname);

		if (plugin.getConfigManager().BounceAnimation()) {
		    Visualizer v = new Visualizer(player);
		    v.setErrorAreas(res);
		    v.setOnce(true);
		    plugin.getSelectionManager().showBounds(player, v);
		}

		ClaimedResidence preRes = plugin.getResidenceManager().getByLoc(lastLoc);
		boolean teleported = false;
		if (preRes != null && preRes.getPermissions().playerHas(player, Flags.tp, FlagCombo.OnlyFalse) && !player.hasPermission("residence.admin.tp")) {
		    Location newLoc = res.getOutsideFreeLoc(loc, player);
		    player.closeInventory();
		    teleported = teleport(player, newLoc);
		} else if (lastLoc != null) {

		    StuckInfo info = updateStuckTeleport(player, loc);
		    player.closeInventory();
		    if (info != null && info.getTimesTeleported() > 5) {
			Location newLoc = res.getOutsideFreeLoc(loc, player);
			teleported = teleport(player, newLoc);
		    } else {
			teleported = teleport(player, lastLoc);
		    }
		} else {
		    Location newLoc = res.getOutsideFreeLoc(loc, player);
		    player.closeInventory();
		    teleported = teleport(player, newLoc);
		}
		if (plugin.getConfigManager().useActionBar()) {
		    ActionBarTitleMessages.send(player, plugin.msg(lm.Residence_MoveDeny, orres.getName()));
		} else {
		    plugin.msg(player, lm.Residence_MoveDeny, orres.getName());
		}
		return teleported;
	    }

	    // Preventing fly in residence only when player has move permission
	    f: if (Flags.nofly.isGlobalyEnabled() && player.isFlying() && res.getPermissions().playerHas(player, Flags.nofly, FlagCombo.OnlyTrue) && !plugin.isResAdminOn(player) && !player.hasPermission(
		"residence.nofly.bypass")) {
		if (res.isOwner(player))
		    break f;
		Location lc = player.getLocation();
		Location location = new Location(lc.getWorld(), lc.getX(), lc.getBlockY(), lc.getZ());
		location.setPitch(lc.getPitch());
		location.setYaw(lc.getYaw());
		int from = location.getBlockY();
		int maxH = location.getWorld().getMaxHeight() - 1;
		boolean teleported = false;

		for (int i = 0; i < maxH; i++) {
		    location.setY(from - i);
		    Block block = location.getBlock();
		    if (!plugin.getNms().isEmptyBlock(block)) {
			location.setY(from - i + 1);
			break;
		    }
		    if (location.getBlockY() <= 0) {
			Location lastLoc = lastOutsideLoc.get(pname);
			player.closeInventory();
			if (lastLoc != null)
			    teleported = teleport(player, lastLoc);
			else
			    teleported = teleport(player, res.getOutsideFreeLoc(loc, player));

			plugin.msg(player, lm.Residence_FlagDeny, Flags.nofly.getName(), orres.getName());
			return teleported;
		    }
		}
		plugin.msg(player, lm.Residence_FlagDeny, Flags.nofly.getName(), orres.getName());
		player.closeInventory();
		teleported = teleport(player, location);
		if (!teleported)
		    return false;
		player.setFlying(false);
		player.setAllowFlight(false);
	    }
	}

	lastOutsideLoc.put(pname, loc);

	if (!currentRes.containsKey(pname) || ResOld != res) {
	    currentRes.put(pname, areaname);

	    // "from" residence for ResidenceChangedEvent
//	    ClaimedResidence chgFrom = null;
//	    if (ResOld != res && ResOld != null) {
//		String leave = ResOld.getLeaveMessage();
//		chgFrom = ResOld;
//		if (leave != null && !leave.equals("") && ResOld != res.getParent()) {
//		    if (plugin.getConfigManager().useTitleMessage()) {
//			plugin.getAB().sendTitle(player, ChatColor.YELLOW + insertMessages(player, ResOld.getName(), ResOld, leave));
//		    }
//		    if (plugin.getConfigManager().useActionBar()) {
//			plugin.getAB().send(player, (new StringBuilder()).append(ChatColor.YELLOW).append(insertMessages(player, ResOld.getName(), ResOld, leave))
//			    .toString());
//		    } else {
//			plugin.msg(player, ChatColor.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
//		    }
//		}
//	    }

	    // New ResidenceChangedEvent
	    ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(ResOld, res, player);
	    plugin.getServ().getPluginManager().callEvent(chgEvent);

	}
	return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {

	Location from = event.getFrom();
	Location to = event.getTo();

	ClaimedResidence fromRes = plugin.getResidenceManager().getByLoc(from);
	ClaimedResidence toRes = plugin.getResidenceManager().getByLoc(to);

	if (fromRes != null && toRes != null && fromRes.equals(toRes))
	    return;
	if (event.getPlayer().hasMetadata("NPC"))
	    return;
	ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(fromRes, toRes, event.getPlayer());
	plugin.getServ().getPluginManager().callEvent(chgEvent);
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
	if (message != null) {
	    if (plugin.getConfigManager().useTitleMessage()) {
		ActionBarTitleMessages.sendTitle(player, ChatColor.YELLOW + insertMessages(player, res, message));
	    }
	    if (plugin.getConfigManager().useActionBar()) {
		ActionBarTitleMessages.send(player, (new StringBuilder()).append(ChatColor.YELLOW).append(insertMessages(player, res, message))
		    .toString());
	    } else {
		plugin.msg(player, ChatColor.YELLOW + this.insertMessages(player, res, message));
	    }
	}

	if (from == null || res == null)
	    return;

	if (!(res == from.getParent())) {
	    if (plugin.getConfigManager().isExtraEnterMessage() && !res.isOwner(player) && (plugin.getRentManager().isForRent(from) || plugin
		.getTransactionManager().isForSale(from))) {
		if (plugin.getRentManager().isForRent(from) && !plugin.getRentManager().isRented(from)) {
		    RentableLand rentable = plugin.getRentManager().getRentableLand(from);
		    if (rentable != null)
			ActionBarTitleMessages.send(player, plugin.msg(lm.Residence_CanBeRented, from.getName(), rentable.cost, rentable.days));
		} else if (plugin.getTransactionManager().isForSale(from) && !res.isOwner(player)) {
		    int sale = plugin.getTransactionManager().getSaleAmount(from);
		    ActionBarTitleMessages.send(player, plugin.msg(lm.Residence_CanBeBought, from.getName(), sale));
		}
	    }
	}

    }

    private StuckInfo updateStuckTeleport(Player player, Location loc) {

	if (loc.getY() >= player.getLocation().getY())
	    return null;

	StuckInfo info = stuckTeleportCounter.get(player.getUniqueId());
	if (info == null) {
	    info = new StuckInfo(player);
	    stuckTeleportCounter.put(player.getUniqueId(), info);
	}
	info.updateLastTp();
	return info;
    }

    public String insertMessages(Player player, ClaimedResidence res, String message) {
	try {
	    message = message.replaceAll("%player", player.getName());
	    message = message.replaceAll("%owner", res.getPermissions().getOwner());
	    message = message.replaceAll("%residence", res.getName());
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
		String resname = plugin.getPlayerListener().getCurrentResidenceName(player.getName());
		ClaimedResidence res = null;

		if (resname == null)
		    continue;

		res = plugin.getResidenceManager().getByName(resname);

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
		String resname = plugin.getPlayerListener().getCurrentResidenceName(player.getName());

		if (resname == null)
		    continue;

		ClaimedResidence res = plugin.getResidenceManager().getByName(resname);

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

    public void DespawnMobs() {
	if (!Flags.nomobs.isGlobalyEnabled())
	    return;
	try {
	    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
		String resname = plugin.getPlayerListener().getCurrentResidenceName(player.getName());

		if (resname == null)
		    continue;

		ClaimedResidence res = null;
		res = plugin.getResidenceManager().getByName(resname);

		if (!res.getPermissions().has(Flags.nomobs, false))
		    continue;

		List<Entity> entities = Bukkit.getServer().getWorld(res.getWorld()).getEntities();
		for (Entity ent : entities) {
		    if (!ResidenceEntityListener.isMonster(ent))
			continue;

		    if (res.containsLoc(ent.getLocation())) {
			ent.remove();
		    }
		}
	    }
	} catch (Exception ex) {
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getPlayer().getWorld()))
	    return;
	String pname = event.getPlayer().getName();
	if (!chatenabled || !playerToggleChat.contains(pname))
	    return;

	ChatChannel channel = plugin.getChatManager().getPlayerChannel(pname);
	if (channel != null) {
	    channel.chat(pname, event.getMessage());
	}
	event.setCancelled(true);
    }

    public void tooglePlayerResidenceChat(Player player, String residence) {
	String pname = player.getName();
	playerToggleChat.add(pname);
	plugin.msg(player, lm.Chat_ChatChannelChange, residence);
    }

    public void removePlayerResidenceChat(String pname) {
	playerToggleChat.remove(pname);
	Player player = Bukkit.getPlayer(pname);
	if (player != null)
	    plugin.msg(player, lm.Chat_ChatChannelLeave);
    }

    public void removePlayerResidenceChat(Player player) {
	String pname = player.getName();
	playerToggleChat.remove(pname);
	plugin.msg(player, lm.Chat_ChatChannelLeave);
    }

    public String getCurrentResidenceName(String player) {
	return currentRes.get(player);
    }
}
