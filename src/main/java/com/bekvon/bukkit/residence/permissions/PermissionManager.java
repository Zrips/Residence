package com.bekvon.bukkit.residence.permissions;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.PlayerGroup;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.platymuus.bukkit.permissions.PermissionsPlugin;

public class PermissionManager {
    protected static PermissionsInterface perms;
    protected LinkedHashMap<String, PermissionGroup> groups;
    protected Map<String, String> playersGroup;
    protected FlagPermissions globalFlagPerms;

    protected HashMap<String, PlayerGroup> groupsMap = new HashMap<String, PlayerGroup>();
    private PermissionGroup defaultGroup = null;
    private Residence plugin;

    private int autoCacheClearId = 0;
    private static final int cacheClearDelay = 600;

    public PermissionManager(Residence plugin) {
	this.plugin = plugin;
	try {
	    groups = new LinkedHashMap<String, PermissionGroup>();
	    playersGroup = Collections.synchronizedMap(new HashMap<String, String>());
	    globalFlagPerms = new FlagPermissions();
	    this.readConfig();
	    checkPermissions();
	} catch (Exception ex) {
	    Logger.getLogger(PermissionManager.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    public void startCacheClearScheduler() {
	autoCacheClearId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, cacheClear, cacheClearDelay * 20L, cacheClearDelay * 20L);
    }

    public void stopCacheClearScheduler() {
	try {
	    if (autoCacheClearId > 0)
		Bukkit.getScheduler().cancelTask(autoCacheClearId);
	} catch (Throwable e) {
	}
    }

    public FlagPermissions getAllFlags() {
	return this.globalFlagPerms;
    }

    public Map<String, String> getPlayersGroups() {
	return playersGroup;
    }

    public Map<String, PermissionGroup> getGroups() {
	return groups;
    }

    public PermissionGroup getDefaultGroup() {
	if (defaultGroup == null)
	    defaultGroup = groups.get(Residence.getInstance().getConfigManager().getDefaultGroup().toLowerCase());
	return defaultGroup;
    }

    public PermissionGroup getGroupByName(String group) {
	group = group.toLowerCase();
	if (!groups.containsKey(group)) {
	    return getDefaultGroup();
	}
	return groups.get(group);
    }

    public String getPermissionsGroup(Player player) {
	return this.getPermissionsGroup(player.getName(), player.getWorld().getName()).toLowerCase();
    }

    public String getPermissionsGroup(String player, String world) {
	if (perms == null)
	    return plugin.getConfigManager().getDefaultGroup().toLowerCase();
	try {
	    return perms.getPlayerGroup(player, world).toLowerCase();
	} catch (Exception e) {
	    return plugin.getConfigManager().getDefaultGroup().toLowerCase();
	}
    }

    public boolean isResidenceAdmin(CommandSender sender) {
	return (ResPerm.admin.hasPermission(sender) || (sender.isOp() && plugin.getConfigManager().getOpsAreAdmins()));
    }

    private void checkPermissions() {
	Server server = plugin.getServ();
	Plugin p = server.getPluginManager().getPlugin("Vault");
	if (p != null) {
	    ResidenceVaultAdapter vault = new ResidenceVaultAdapter(server);
	    if (vault.permissionsOK()) {
		perms = vault;
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found Vault using permissions plugin:" + vault.getPermissionsName());
		return;
	    }
	    Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found Vault, but Vault reported no usable permissions system...");
	}
	p = server.getPluginManager().getPlugin("PermissionsBukkit");
	if (p != null) {
	    perms = new PermissionsBukkitAdapter((PermissionsPlugin) p);
	    Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found PermissionsBukkit Plugin!");
	    return;
	}

	PluginManager pluginManager = plugin.getServer().getPluginManager();
	Plugin pl = pluginManager.getPlugin("LuckPerms");
	if (pl != null && pl.isEnabled()) {
	    Integer ver = plugin.getVersionChecker().convertVersion(pl.getDescription().getVersion());
	    if (ver >= 40000 && ver < 50000) {
		perms = new LuckPerms4Adapter();
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found LuckPerms4 Plugin!");
		return;
	    } else if (ver > 50000) {
		perms = new LuckPerms5Adapter();
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found LuckPerms5 Plugin!");
		return;
	    }
	    plugin.consoleMessage("&cLuckPerms plugin was found but its outdated");
	}

	p = server.getPluginManager().getPlugin("bPermissions");
	if (p != null) {
	    perms = new BPermissionsAdapter();
	    Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found bPermissions Plugin!");
	    return;
	}
	p = server.getPluginManager().getPlugin("Permissions");
	if (p != null) {
	    if (plugin.getConfigManager().useLegacyPermissions()) {
		perms = new LegacyPermissions(((Permissions) p).getHandler());
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found Permissions Plugin!");
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + "Permissions running in Legacy mode!");
	    } else {
		perms = new OriginalPermissions(((Permissions) p).getHandler());
		Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found Permissions Plugin!");
	    }
	    return;
	}
	Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Permissions plugin NOT FOUND!");
    }

    private void readConfig() {

	FileConfiguration groupsFile = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "groups.yml"));
	FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "flags.yml"));

	String defaultGroup = plugin.getConfigManager().getDefaultGroup().toLowerCase();
	globalFlagPerms = FlagPermissions.parseFromConfigNode("FlagPermission", flags.getConfigurationSection("Global"));
	ConfigurationSection nodes = groupsFile.getConfigurationSection("Groups");
	if (nodes != null) {
	    Set<String> entrys = nodes.getKeys(false);
	    int i = 0;
	    for (String key : entrys) {
		try {
		    i++;
		    groups.put(key.toLowerCase(), new PermissionGroup(key.toLowerCase(), nodes.getConfigurationSection(key), globalFlagPerms, i));
		    List<String> mirrors = nodes.getConfigurationSection(key).getStringList("Mirror");
		    for (String group : mirrors) {
			groups.put(group.toLowerCase(), new PermissionGroup(group.toLowerCase(), nodes.getConfigurationSection(key), globalFlagPerms, i));
		    }
		} catch (Exception ex) {
		    Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Error parsing group from config:" + key + " Exception:" + ex);
		}
	    }
	}

	if (!groups.containsKey(defaultGroup)) {
	    groups.put(defaultGroup, new PermissionGroup(defaultGroup));
	}
	if (groupsFile.isConfigurationSection("GroupAssignments")) {
	    Set<String> keys = groupsFile.getConfigurationSection("GroupAssignments").getKeys(false);
	    if (keys != null) {
		for (String key : keys) {
		    playersGroup.put(key.toLowerCase(), groupsFile.getString("GroupAssignments." + key, defaultGroup).toLowerCase());
		}
	    }
	}
    }

    public boolean hasGroup(String group) {
	group = group.toLowerCase();
	return groups.containsKey(group);
    }

    public PermissionsInterface getPermissionsPlugin() {
	return perms;
    }

    public enum ResPerm {
	chatcolor("Allows to change residence chat color"),
	chatprefix("Allows to change residence chat prefix"),
	chatkick("Allows to kick player from residence chat"),
	permisiononerror("Allows to see missing permission on error message"),

	command_message_enter("Allows to change residence enter message"),
	command_message_leave("Allows to change residence leave message"),
	command_message_enter_remove("Allows to remove residence enter message"),
	command_message_leave_remove("Allows to remove residence leave message"),

	cleanbypass("Prevents residence from ebing removed on automatic cleanup"),
	worldguard_$1("Allows to create residence inside region", "region"),
	flag_$1("Gives access to defined flag", "flagName"),
	tpdelaybypass("Allows to bypass teleport delay"),
	backup("Allows to use backup flag to save residence into schematics"),
	admin_tp("Allows teleportation into residence where its not allowed"),
	topadmin("Defines as residence top admin"),
	admin("Defines as residence admin"),
	admin_move("Allows movement in residence where its not allowed"),
	newguyresidence("Creates residence on first chest place"),
	bypass_ignorey("Allows to ignore Y corrdiante restrictions"),
	bypass_ignoreyinsubzone("Allows to ignore subzone limitations"),
	bypass_destroy("Allows to bypass destroy flag"),
	bypass_build("Allows to bypass build flag"),
	bypass_container("Allows to bypass container flag"),
	bypass_use("Allows to bypass use flag"),
	bypass_door("Allows to bypass door flag"),
	bypass_button("Allows to bypass button flag"),
	bypass_fly("Allows to bypass fly flag"),
	bypass_nofly("Allows to bypass nofly flag"),
	bypass_tp("Allows to bypass command flag"),
	bypass_command("Allows to bypass command flag"),
	bypass_itempickup("Allows to bypass itempickup flag"),
	buy("Allows to buy residence"),
	sell("Allows to sell residence"),
	max_res_unlimited("Defines residence limit as unlimited"),
	max_res_$1("Defines residence limit", "number"),
	max_subzones_unlimited("Defines subzone limit as unlimited"),
	max_subzones_$1("Defines subzone limit", "number"),
	max_subzonedepth_unlimited("Defines subzone depth limit as unlimited"),
	max_subzonedepth_$1("Defines subzone depth limit", "number"),
	max_rents_unlimited("Defines residence rent limit as unlimited"),
	max_rents_$1("Defines residence rent limit", "number"),
	group_$1("Defines players residence group", "groupName"),
	market_evict("Allows to evict players from rented residences"),
	rename("Allows to rename residence"),
	select("Allows to select residence area"),
	select_auto_others("Allows to toggle auto selection for others"),
	resize("Allows to resize residence"),
	create("Allows to create residence"),
	create_subzone("Allows to create residence subzones"),
	randomtp("Allows to use rt command"),
	randomtp_admin("Allows to use rt command on another player"),
	randomtp_cooldownbypass("Allows to bypass random teleport command cooldown"),
	randomtp_delaybypass("Allows to bypass random teleport command delay"),
	delete("Allows to delete residence"),
	delete_subzone("Allows to delete subzone"),
	command_kick_bypass("Allows to bypass kick from residence"),
	command_contract_subzone("Allows to contract subzones"),
	command_expand_subzone("Allows to expand subzones"),
	versioncheck("Shows when we have new version of plugin"),
	command_$1("Gives access to particular command", "commandName"),
	command_$1_others("Allows to perform command on another player", "commandName");

	private Boolean show = true;
	private String desc;
	private String[] wars;
	private String fixedPermission = null;

	ResPerm(String desc, Boolean show) {
	    this.desc = desc;
	    this.show = show;
	}

	ResPerm(String desc) {
	    this.desc = desc;
	}

	ResPerm(String desc, String... wars) {
	    this.desc = desc;
	    this.wars = wars;
	}

	public String getDesc() {
	    return desc;
	}

	public void setDesc(String desc) {
	    this.desc = desc;
	}

	public String getPermissionForShow() {
	    return getPermissionForShow(false);
	}

	public String getPermissionForShow(boolean cmd) {
	    if (this.getWars() == null)
		return getPermission("");

	    String[] w = new String[this.getWars().length];

	    for (int i = 0; i < this.getWars().length; i++) {
		w[i] = Residence.getInstance().getLM().getMessage(lm.Permissions_variableColor) + "[" + this.getWars()[i] + "]" + (!cmd ? Residence.getInstance().getLM().getMessage(
		    lm.Permissions_permissionColor) : Residence.getInstance().getLM().getMessage(lm.Permissions_cmdPermissionColor));
	    }

	    return getPermission(w);
	}

	public String getPermission() {
	    return getPermission("");
	}

	public String getPermission(String... extra) {
	    if (fixedPermission != null)
		return fixedPermission;

	    String perm = this.name().replace("_", ".");
	    int i = 0;
	    for (String one : extra) {
		i++;
		if (one == null || one.isEmpty())
		    continue;
		perm = perm.replace("$" + i, one.toLowerCase());
	    }
	    perm = perm.replace("$star", "*");

	    if (!this.name().contains("$")) {
		fixedPermission = "residence." + perm;
	    }

	    return "residence." + perm;
	}

	public boolean hasPermission(CommandSender sender) {
	    return hasPermission(sender, false);
	}

	public boolean hasPermission(CommandSender sender, Integer... extra) {
	    String[] ex = new String[extra.length];
	    for (int i = 0; i < extra.length; i++) {
		ex[i] = String.valueOf(extra[i]);
	    }
	    return hasPermission(sender, false, ex);
	}

	public boolean hasPermission(CommandSender sender, lm lm) {
	    return hasPermission(sender, true, true, null, lm);
	}

	public boolean hasPermission(CommandSender sender, lm lm, String... extra) {
	    return hasPermission(sender, true, true, null, lm, extra);
	}

	public boolean hasPermission(CommandSender sender, Flags flag) {
	    return hasPermission(sender, false, flag.toString());
	}

	public boolean hasPermission(CommandSender sender, String... extra) {
	    return hasPermission(sender, false, extra);
	}

	public boolean hasPermission(CommandSender sender, Long delay, String... extra) {
	    return hasPermission(sender, false, true, delay, null, extra);
	}

	public boolean hasPermission(CommandSender sender, boolean inform, String... extra) {
	    return hasPermission(sender, inform, true, extra);
	}

	public boolean hasPermission(CommandSender sender, boolean inform, boolean informConsole, String... extra) {
	    return hasPermission(sender, inform, informConsole, null, null, extra);
	}

	public boolean hasPermission(CommandSender sender, boolean inform, Long delayInMiliSeconds) {
	    return hasPermission(sender, inform, true, delayInMiliSeconds, null);
	}

	public boolean hasPermission(CommandSender sender, boolean inform, boolean informConsole, Long delay, lm lms, String... extra) {
	    if (sender == null)
		return false;

	    if (!(sender instanceof Player)) {
		return true;
	    }

	    String perm = this.getPermission(extra);

	    Player player = (Player) sender;

	    PermissionInfo info = Residence.getInstance().getPermissionManager().getFromCache(player, perm);
	    boolean has = false;
	    if (info != null && info.getDelay() + info.getLastChecked() > System.currentTimeMillis()) {
		has = info.isEnabled();
	    } else {
		has = sender.hasPermission(perm);
		Residence.getInstance().getPermissionManager().addToCache(player, perm, has, delay == null ? 200L : delay);
	    }

	    if (!has && inform) {
		boolean showPerm = ResPerm.permisiononerror.hasPermission(sender, 50000L);
		RawMessage rm = new RawMessage();
		rm.add(Residence.getInstance().getLM().getMessage(lms == null ? lm.General_NoPermission : lms), showPerm ? perm : null);
		rm.show(sender);

		informConsole(sender, perm, informConsole);
	    }
	    return has;
	}

//	public boolean hasPermission(Player player, String inName) {
//	    
//	    String name = inName.toLowerCase(java.util.Locale.ENGLISH);
//
//	    if (player.isPermissionSet(name)) {
//		return player.getperpermissions.get(name).getValue();
//	    } else {
//		Permission perm = Bukkit.getServer().getPluginManager().getPermission(name);
//
//		if (perm != null) {
//		    return perm.getDefault().getValue(isOp());
//		} else {
//		    return Permission.DEFAULT_PERMISSION.getValue(isOp());
//		}
//	    }
//	}

	private static void informConsole(CommandSender sender, String permission, boolean informConsole) {
	    if (informConsole) {
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		Residence.getInstance().msg(console, Residence.getInstance().getLM().getMessage(lm.General_NoPermission, "[playerName]", sender.getName(), "[permission]", permission));
	    }
	}

	public boolean hasSetPermission(CommandSender sender, String... extra) {
	    return hasSetPermission(sender, false, extra);
	}

	public boolean hasSetPermission(CommandSender sender, boolean inform, String... extra) {
	    return hasSetPermission(sender, this.getPermission(extra), inform);
	}

	public static boolean hasSetPermission(CommandSender sender, String perm, boolean inform) {
	    boolean has = Residence.getInstance().getPermissionManager().isSetPermission(sender, perm);
	    if (!has && inform) {
		boolean showPerm = ResPerm.permisiononerror.hasPermission(sender);
		RawMessage rm = new RawMessage();
		rm.add(Residence.getInstance().getLM().getMessage(lm.General_NoPermission), showPerm ? perm : null);
		rm.show(sender);

		informConsole(sender, perm, true);
	    }
	    return has;
	}

	public String[] getWars() {
	    return wars;
	}

	public void setWars(String[] wars) {
	    this.wars = wars;
	}

	public Boolean getShow() {
	    return show;
	}

	public void setShow(Boolean show) {
	    this.show = show;
	}

	public static boolean hasPermission(CommandSender sender, String permision, Boolean output) {
	    return hasPermission(sender, permision, output, true);
	}

	public static boolean hasPermission(CommandSender sender, String permision, Boolean output, boolean informConsole) {
	    if (!(sender instanceof Player)) {
		return true;
	    }
	    Player player = (Player) sender;
	    if (player.hasPermission(permision)) {
		return true;
	    }
	    if (output) {
		boolean showPerm = ResPerm.permisiononerror.hasPermission(sender);
		RawMessage rm = new RawMessage();
		rm.add(Residence.getInstance().getLM().getMessage(lm.General_NoPermission), showPerm ? permision : null);
		rm.show(sender);

		informConsole(sender, permision, informConsole);
	    }
	    return false;
	}
    }

    private Runnable cacheClear = new Runnable() {
	@Override
	public void run() {
	    cache.clear();
	}
    };

    private HashMap<UUID, HashMap<String, PermissionInfo>> cache = new HashMap<UUID, HashMap<String, PermissionInfo>>();

    public void removeFromCache(Player player) {
	cache.remove(player.getUniqueId());
    }

    public PermissionInfo getFromCache(Player player, String perm) {
	HashMap<String, PermissionInfo> old = cache.get(player.getUniqueId());
	if (old == null) {
	    return null;
	}
	return old.get(perm);
    }

    public PermissionInfo addToCache(Player player, String perm, boolean has, Long delayInMiliseconds) {
	HashMap<String, PermissionInfo> old = cache.get(player.getUniqueId());
	if (old == null) {
	    old = new HashMap<String, PermissionInfo>();
	}

	PermissionInfo info = new PermissionInfo(perm, delayInMiliseconds);
	info.setLastChecked(System.currentTimeMillis());
	info.setEnabled(has);

	old.put(perm, info);
	cache.put(player.getUniqueId(), old);

	return info;
    }

    public PermissionAttachmentInfo getSetPermission(CommandSender sender, String perm) {
	if (sender instanceof Player)
	    for (PermissionAttachmentInfo permission : ((Player) sender).getEffectivePermissions()) {
		if (permission.getPermission().equalsIgnoreCase(perm)) {
		    return permission;
		}
	    }
	return null;
    }

    public boolean isSetPermission(CommandSender sender, String perm) {
	if (sender instanceof Player)
	    return isSetPermission((Player) sender, perm);
	return true;
    }

    public boolean isSetPermission(Player player, String perm) {
	return player.hasPermission(new Permission(perm, PermissionDefault.FALSE));
    }
}
