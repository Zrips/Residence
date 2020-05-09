package com.bekvon.bukkit.residence;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.bekvon.bukkit.cmiLib.CMIEffectManager.CMIParticle;
import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.containers.ELMessageType;
import com.bekvon.bukkit.residence.containers.EconomyType;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.YmlMaker;

public class ConfigManager {
    protected String defaultGroup;
    protected boolean useLeases;
    protected boolean ResMoneyBack;
    protected boolean enableEconomy;
    private EconomyType VaultEconomy;
    protected boolean ExtraEnterMessage;
    protected boolean adminsOnly;
    protected boolean allowEmptyResidences;
    protected boolean NoLava;
    protected boolean NoWater;
    protected boolean NoLavaPlace;
    protected boolean useBlockFall;
    protected boolean NoWaterPlace;
    protected boolean AutoCleanUp;
    protected boolean SellSubzone;
    protected boolean LwcOnDelete;
    protected boolean LwcOnBuy;
    protected boolean LwcOnUnrent;
    protected List<Material> LwcMatList = new ArrayList<Material>();
    protected boolean UseClean;
    protected boolean PvPFlagPrevent;
    protected boolean OverridePvp;
    protected boolean BlockAnyTeleportation;
    protected CMIMaterial infoTool;
    protected int AutoCleanUpDays;
    protected boolean AutoCleanUpRegenerate;
    protected boolean CanTeleportIncludeOwner;
    protected CMIMaterial selectionTool;
    protected boolean adminOps;
    protected boolean AdminFullAccess;
    protected String multiworldPlugin;
    protected boolean enableRentSystem;
    protected boolean RentPreventRemoval;
    private boolean DeductFromBank;
    private boolean DeductFromBankThenPlayer;
    protected boolean RentInformOnEnding;
    protected boolean RentAllowRenewing;
    protected boolean RentStayInMarket;
    protected boolean RentAllowAutoPay;
    protected boolean RentPlayerAutoPay;
    protected boolean leaseAutoRenew;
    protected boolean ShortInfoUse;
    protected boolean OnlyLike;
    protected int RentInformBefore;
    protected int RentInformDelay;
    protected int rentCheckInterval;
    protected int chatPrefixLength;
    protected int leaseCheckInterval;
    protected int autoSaveInt;
    protected boolean NewSaveMechanic;
    private int ItemPickUpDelay;
    private boolean AutomaticResidenceCreationCheckCollision;
    private String AutomaticResidenceCreationIncrementFormat;

    private boolean ConsoleLogsShowFlagChanges = true;

    // Backup stuff
    protected boolean BackupAutoCleanUpUse;
    protected int BackupAutoCleanUpDays;
    protected boolean UseZipBackup;
    protected boolean BackupWorldFiles;
    protected boolean BackupforsaleFile;
    protected boolean BackupleasesFile;
    protected boolean BackuppermlistsFile;
    protected boolean BackuprentFile;
    protected boolean BackupflagsFile;
    protected boolean BackupgroupsFile;
    protected boolean BackupconfigFile;

    protected int FlowLevel;
    protected int PlaceLevel;
    protected int BlockFallLevel;
    protected int CleanLevel;
    protected int NewPlayerRangeX;
    protected int NewPlayerRangeY;
    protected int NewPlayerRangeZ;
    protected int VisualizerRange;
    protected int VisualizerShowFor;
    protected int VisualizerUpdateInterval;
    protected int TeleportDelay;
    protected boolean TeleportTitleMessage;
    protected int VisualizerRowSpacing;
    protected int VisualizerCollumnSpacing;
    protected int VisualizerSkipBy;
    private int VisualizerFrameCap;
    private int VisualizerSidesCap;
    protected boolean flagsInherit;
    protected ChatColor chatColor;
    protected boolean chatEnable;
    private ELMessageType EnterLeaveMessageType;
//    protected boolean actionBar;
//    protected boolean titleMessage;
    protected boolean ActionBarOnSelection;
    protected boolean visualizer;
    protected int minMoveUpdate;
    protected int MaxResCount;
    protected int MaxRentCount;
    protected int MaxSubzonesCount;
    protected int MaxSubzoneDepthCount;
    protected int VoteRangeFrom;
    protected int HealInterval;
    protected int FeedInterval;
    protected int VoteRangeTo;
    protected FlagPermissions globalCreatorDefaults;
    protected FlagPermissions globalRentedDefaults;
    protected FlagPermissions globalResidenceDefaults;
    protected Map<String, FlagPermissions> globalGroupDefaults;
    protected String language;
    protected String DefaultWorld;
    protected String DateFormat;
    protected String DateFormatShort;
    protected String TimeZone;
    protected boolean preventBuildInRent;
    protected boolean PreventSubZoneRemoval;
    protected boolean stopOnSaveError;
    protected boolean legacyperms;
    protected String namefix;
    protected boolean showIntervalMessages;
    protected boolean ShowNoobMessage;
    protected boolean NewPlayerUse;
    protected boolean NewPlayerFree;
    protected boolean spoutEnable;
    protected boolean AutoMobRemoval;
    protected boolean BounceAnimation;
    private boolean EnterAnimation;
    protected boolean useFlagGUI;
    protected int AutoMobRemovalInterval;
    protected boolean enableLeaseMoneyAccount;
    protected boolean Couldroncompatibility;
    protected boolean enableDebug = false;
    protected boolean versionCheck = true;
    protected boolean UUIDConvertion = true;
    protected boolean OfflineMode = false;
    protected boolean SelectionIgnoreY = false;
    protected boolean SelectionIgnoreYInSubzone = false;
    protected boolean NoCostForYBlocks = false;
    protected boolean useVisualizer;
    protected boolean DisableListeners;
    protected boolean DisableCommands;

    //Town
//    private boolean TownEnabled = false;
//    private int TownMinRange = 0;

//    protected boolean DisableNoFlagMessageUse;
//    protected List<String> DisableNoFlagMessageWorlds = new ArrayList<String>();

    protected boolean TNTExplodeBelow;
    protected int TNTExplodeBelowLevel;
    protected boolean CreeperExplodeBelow;
    protected int CreeperExplodeBelowLevel;

    protected List<CMIMaterial> customContainers = new ArrayList<CMIMaterial>();
    protected List<CMIMaterial> customBothClick = new ArrayList<CMIMaterial>();
    protected List<CMIMaterial> customRightClick = new ArrayList<CMIMaterial>();
    protected List<Material> CleanBlocks = new ArrayList<Material>();

    protected List<String> NoFlowWorlds;
    protected List<String> AutoCleanUpWorlds;
    protected List<String> NoPlaceWorlds;
    protected List<String> BlockFallWorlds;
    protected List<String> CleanWorlds;
    protected List<String> FlagsList;
    protected List<String> NegativePotionEffects;
    protected List<String> NegativeLingeringPotionEffects;
    private Double WalkSpeed1;
    private Double WalkSpeed2;

    protected Location KickLocation;
    protected Location FlyLandLocation;

    protected List<RandomTeleport> RTeleport = new ArrayList<RandomTeleport>();

    protected List<String> DisabledWorldsList = new ArrayList<String>();

    protected int rtCooldown;
    protected int rtMaxTries;

    private HashMap<FlagState, ItemStack> guiBottonStates = new HashMap<FlagState, ItemStack>();

    private boolean enforceAreaInsideArea;

    protected CMIParticle SelectedFrame;
    protected CMIParticle SelectedSides;

    protected CMIParticle OverlapFrame;
    protected CMIParticle OverlapSides;

//    protected CMIParticle SelectedSpigotFrame;
//    protected CMIParticle SelectedSpigotSides;
//
//    protected CMIParticle OverlapSpigotFrame;
//    protected CMIParticle OverlapSpigotSides;

    // DynMap
    public boolean DynMapUse;
    public boolean DynMapShowFlags;
    public boolean DynMapHideHidden;
    public boolean DynMapLayer3dRegions;
    public int DynMapLayerSubZoneDepth;
    public String DynMapBorderColor;
    public double DynMapBorderOpacity;
    public int DynMapBorderWeight;
    public String DynMapFillColor;
    public double DynMapFillOpacity;
    public String DynMapFillForRent;
    public String DynMapFillRented;
    public String DynMapFillForSale;
    public List<String> DynMapVisibleRegions;
    public List<String> DynMapHiddenRegions;
    // DynMap

    // Raid
    public static boolean RaidEnabled = false;
    public static boolean RaidAttackerBlockBreak = false;
    public static boolean RaidAttackerBlockPlace = false;
    public static boolean RaidDefenderBlockBreak = false;
    public static boolean RaidDefenderBlockPlace = false;
    public static boolean RaidDefenderTeleport = false;
    public static boolean RaidDefenderContainerUsage = false;
    public static boolean RaidFriendlyFire = false;
    public static int PreRaidTimer = 120;
    public static int RaidTimer = 120;
    public static int RaidCooldown = 120;
    public static int RaidPlayerCooldown = 120;
    // Raid

    // Schematics
    public boolean RestoreAfterRentEnds;
    public boolean SchematicsSaveOnFlagChange;
    // Schematics

    // Global chat
    public boolean GlobalChatEnabled;
    public boolean GlobalChatSelfModify;
    public String GlobalChatFormat;
    // Global chat

    private Residence plugin;

    public ConfigManager(Residence plugin) {
//	FileConfiguration config = YamlConfiguration.loadConfiguration(new File(Residence.dataFolder, "config.yml"));
	this.plugin = plugin;
	globalCreatorDefaults = new FlagPermissions();
	globalRentedDefaults = new FlagPermissions();
	globalResidenceDefaults = new FlagPermissions();
	globalGroupDefaults = new HashMap<String, FlagPermissions>();
	UpdateConfigFile();
	this.loadFlags();
    }

    public static String Colors(String text) {
	return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> getClassesFromPackage(String pckgname, String cleaner) throws ClassNotFoundException {
	List<String> result = new ArrayList<String>();
	try {
	    for (URL jarURL : ((URLClassLoader) Residence.class.getClassLoader()).getURLs()) {
		try {
		    result.addAll(getClassesInSamePackageFromJar(pckgname, jarURL.toURI().getPath(), cleaner));
		} catch (URISyntaxException e) {
		    e.printStackTrace();
		}
	    }
	} catch (NullPointerException x) {
	    throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
	}
	return result;
    }

    private static List<String> getClassesInSamePackageFromJar(String packageName, String jarPath, String cleaner) {
	JarFile jarFile = null;
	List<String> listOfCommands = new ArrayList<String>();
	try {
	    jarFile = new JarFile(jarPath);
	    Enumeration<JarEntry> en = jarFile.entries();
	    while (en.hasMoreElements()) {
		JarEntry entry = en.nextElement();
		String entryName = entry.getName();
		packageName = packageName.replace(".", "/");
		if (entryName != null && entryName.endsWith(".yml") && entryName.startsWith(packageName)) {
		    String name = entryName.replace(packageName, "").replace(".yml", "").replace("/", "");
		    if (name.contains("$"))
			name = name.split("\\$")[0];
		    if (cleaner != null)
			name = name.replace(cleaner, "");
		    listOfCommands.add(name);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (jarFile != null)
		try {
		    jarFile.close();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	return listOfCommands;
    }

    public void copyOverTranslations() {

	ArrayList<String> languages = new ArrayList<String>();
	try {
	    languages.addAll(getClassesFromPackage("Language", null));
	} catch (ClassNotFoundException e1) {
	    e1.printStackTrace();
	}

	for (String one : languages) {
	    File file = new File(plugin.getDataFolder(), "Language" + File.separator + one + ".yml");
	    if (!file.exists()) {
		YmlMaker f = new YmlMaker(plugin, "Language" + File.separator + one + ".yml");
		f.saveDefaultConfig();
		f.ConfigFile.renameTo(file);
	    }
	}
    }

    public void ChangeConfig(String path, Boolean stage) {
	File f = new File(plugin.getDataFolder(), "config.yml");

	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

	if (!conf.isBoolean(path))
	    return;

	conf.set(path, stage);

	try {
	    conf.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	plugin.getConfigManager().UpdateConfigFile();
    }

    public static List<String> ColorsArray(List<String> text, Boolean colorize) {
	List<String> temp = new ArrayList<String>();
	for (String part : text) {
	    if (colorize)
		part = Colors(part);
	    temp.add(Colors(part));
	}
	return temp;
    }

    void UpdateFlagFile() {

	File f = new File(plugin.getDataFolder(), "flags.yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

//	if (!conf.isConfigurationSection("Global.CompleteDisable"))
//	    conf.crea.createSection("Global.CompleteDisable");

	if (!conf.isList("Global.TotalFlagDisabling"))
	    conf.set("Global.TotalFlagDisabling", Arrays.asList("Completely", "Disabled", "Particular", "Flags"));

	for (Flags fl : Flags.values()) {
	    if (conf.isBoolean("Global.FlagPermission." + fl))
		continue;
	    conf.createSection("Global.FlagPermission." + fl);
	    conf.set("Global.FlagPermission." + fl, fl.isEnabled());
	}

	if (!conf.isConfigurationSection("Global.FlagGui")) {
	    conf.createSection("Global.FlagGui");
	}

	if (!conf.isConfigurationSection("Global.RentedDefault")) {
	    for (Entry<String, Boolean> one : this.getGlobalCreatorDefaultFlags().getFlags().entrySet()) {
		conf.set("Global.RentedDefault." + one.getKey(), one.getValue());
	    }
	    conf.set("Global.RentedDefault.admin", true);
	}

	ConfigurationSection guiSection = conf.getConfigurationSection("Global.FlagGui");

	for (Flags fl : Flags.values()) {
	    guiSection.set(fl.toString(), guiSection.get(fl.toString(), fl.getIcon().toString()));
	}

	try {
	    conf.save(f);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	ConfigReader cfg = null;
	try {
	    cfg = new ConfigReader(f);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	if (cfg == null)
	    return;
	cfg.load();
	cfg.addComment("Global", "These are Global Settings for Residence.");
	cfg.addComment("Global.Flags", "These are world flags that are applied when the player is NOT within a residence.");
	cfg.addComment("Global.Flags.Global", "these are default for all worlds unless specified below, they can be overridden per group");
	cfg.addComment("Global.FlagPermission", "This gives permission to change certain flags to all groups, unless specifically denied to the group.");
	cfg.addComment("Global.FlagGui", "This sets GUI items to represent each flag, if not given, then gray wool will be used");
	cfg.addComment("Global.ResidenceDefault", "These are default flags applied to all residences from any user group.");
	cfg.addComment("Global.CreatorDefault", "These are default flags applied to the residence creator of any group.");
	cfg.addComment("Global.RentedDefault", "These are default flags applied to the residence renter of any group.");
	cfg.addComment("Global.GroupedFlags", "These are grouped flags, so when using /res pset nickname redstone true, player will get all flags in list, same when setting to false or removing them.");
	cfg.addComment("Global.TotalFlagDisabling", "Completely disables defined flag which will no longer be accesable even with resadmin command",
	    "Can save some of the server processing resources if you dont want to utilize specific checks for specific flags");
	cfg.addComment("Global.GroupDefault", "These are default group flags applied to a residence made by a user of any group.");
	cfg.addComment("ItemList", "this is where you can create blacklists / whitelists");
	cfg.addComment("ItemList.DefaultList", "list name is not important, as long as it is unique. Its good to use a descripive name.");
	cfg.addComment("ItemList.DefaultList.Type", "type of the list, can be blacklist, whitelist, or ignorelist");
	cfg.addComment("ItemList.DefaultList.Items", "If you want, you can have this list only apply to one world, otherwise it applies to all worlds",
	    "World: world",
	    "You can also have the list apply only to one group, otherwise it applies for all groups",
	    "Group: default",
	    "this is the actual list of material names that this list allows or disallows",
	    "You can look up the material name by item ID in game by typing /res material <id>",
	    "Alternativly, you can simply use the item ID in the list, but its less descriptive and harder to see what the list allows or dissallows at a glance");
	cfg.save();
    }

    public void UpdateGroupedFlagsFile() {

	File f = new File(plugin.getDataFolder(), "flags.yml");
	YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);

	if (!conf.isConfigurationSection("Global.GroupedFlags")) {
	    conf.createSection("Global.GroupedFlags");
	    conf.set("Global.GroupedFlags.redstone", Arrays.asList(
		Flags.note.toString(),
		Flags.pressure.toString(),
		Flags.lever.toString(),
		Flags.button.toString(),
		Flags.diode.toString()));
	    conf.set("Global.GroupedFlags.craft", Arrays.asList(
		Flags.brew.toString(),
		Flags.table.toString(),
		Flags.enchant.toString()));
	    conf.set("Global.GroupedFlags.trusted", Arrays.asList(
		Flags.use.toString(),
		Flags.tp.toString(),
		Flags.build.toString(),
		Flags.container.toString(),
		Flags.move.toString(),
		Flags.leash.toString(),
		Flags.animalkilling.toString(),
		Flags.mobkilling.toString(),
		Flags.shear.toString(),
		Flags.chat.toString()));
	    conf.set("Global.GroupedFlags.fire", Arrays.asList(
		Flags.ignite.toString(),
		Flags.firespread.toString()));

	    try {
		conf.save(f);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	for (String oneGroup : conf.getConfigurationSection("Global.GroupedFlags").getKeys(false)) {
	    for (String OneFlag : conf.getStringList("Global.GroupedFlags." + oneGroup)) {
		FlagPermissions.addFlagToFlagGroup(oneGroup, OneFlag);
	    }
	}
    }

    public void UpdateConfigFile() {

	String defaultWorldName = Bukkit.getServer().getWorlds().size() > 0 ? Bukkit.getServer().getWorlds().get(0).getName() : "World";

	ConfigReader c = null;
	try {
	    c = new ConfigReader("config.yml");
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (c == null)
	    return;

	c.copyDefaults(true);

	c.addComment("Global", "These are Global Settings for Residence.");

	c.addComment("Global.UUIDConvertion", "Starts UUID conversion on plugin startup", "DONT change this if you are not sure what you doing");
	UUIDConvertion = c.get("Global.UUIDConvertion", true);

	c.addComment("Global.OfflineMode",
	    "If you running offline server, better to check this as true. This will help to solve issues with changing players UUID.");
	OfflineMode = c.get("Global.OfflineMode", false);

	c.addComment("Global.versionCheck", "Players with residence.versioncheck permission node will be noticed about new residence version on login");
	versionCheck = c.get("Global.versionCheck", true);

	c.addComment("Global.Language", "This loads the <language>.yml file in the Residence Language folder",
	    "All Residence text comes from this file. (NOT DONE YET)");
	language = c.get("Global.Language", "English");

	c.addComment("Global.SelectionToolId", "Wooden Hoe is the default selection tool for Residence.",
	    "You can change it to another item ID listed here: http://www.minecraftwiki.net/wiki/Data_values");
	selectionTool = CMIMaterial.get(c.get("Global.SelectionToolId", CMIMaterial.WOODEN_HOE.name()));

	c.addComment("Global.Selection.IgnoreY", "By setting this to true, all selections will be made from bedrock to sky ignoring Y coordinates");
	SelectionIgnoreY = c.get("Global.Selection.IgnoreY", false);

	c.addComment("Global.Selection.IgnoreYInSubzone",
	    "When this set to true, selections inside existing residence will be from bottom to top of that residence",
	    "When this set to false, selections inside existing residence will be exactly as they are");
	SelectionIgnoreYInSubzone = c.get("Global.Selection.IgnoreYInSubzone", false);

	c.addComment("Global.Selection.NoCostForYBlocks", "By setting this to true, player will only pay for x*z blocks ignoring height",
	    "This will lower residence price by up to 256 times, so adjust block price BEFORE enabling this");
	NoCostForYBlocks = c.get("Global.Selection.NoCostForYBlocks", false);

	c.addComment("Global.InfoToolId", "This determins which tool you can use to see info on residences, default is String.",
	    "Simply equip this tool and hit a location inside the residence and it will display the info for it.");
	infoTool = CMIMaterial.get(c.get("Global.InfoToolId", Material.STRING.toString()));

	c.addComment("Global.Optimizations.CanTeleportIncludeOwner", "This will slightly change behavior of groups file CanTeleport section which will include server owner into check",
	    "When this is set to false and CanTeleport set to false, players will not have option to teleport to other player residences, only to their own",
	    "When this is set to true and CanTeleport set to false, players will not have option to teleport to residences in general",
	    "Keep in mind that this only applies for commands like /res tp");
	CanTeleportIncludeOwner = c.get("Global.Optimizations.CanTeleportIncludeOwner", false);

	c.addComment("Global.Optimizations.DefaultWorld", "Name of your main residence world. Usually normal starting world 'World'. Capitalization essential");
	DefaultWorld = c.get("Global.Optimizations.DefaultWorld", defaultWorldName);

	c.addComment("Global.Optimizations.DisabledWorlds.List", "List Of Worlds where this plugin is disabled");
	DisabledWorldsList = c.get("Global.Optimizations.DisabledWorlds.List", new ArrayList<String>());

	c.addComment("Global.Optimizations.DisabledWorlds.DisableListeners", "Disables all listeners in included worlds");
	DisableListeners = c.get("Global.Optimizations.DisabledWorlds.DisableListeners", true);
	c.addComment("Global.Optimizations.DisabledWorlds.DisableCommands", "Disabled any command usage in included worlds");
	DisableCommands = c.get("Global.Optimizations.DisabledWorlds.DisableCommands", true);

	c.addComment("Global.Optimizations.ItemPickUpDelay", "Delay in seconds between item pickups after residence flag prevents it", "Keep it at arround 10 sec to lower unesecery checks");
	ItemPickUpDelay = c.get("Global.Optimizations.ItemPickUpDelay", 10);

	c.addComment("Global.Optimizations.AutomaticResidenceCreation.CheckCollision",
	    "When set to true /res auto command will check for new area collision with other residences to avoid overlapping.",
	    "Set it to false to gain some performace but new residence can often overlap with old ones");
	AutomaticResidenceCreationCheckCollision = c.get("Global.Optimizations.AutomaticResidenceCreation.CheckCollision", true);

	c.addComment("Global.Optimizations.AutomaticResidenceCreation.IncrementFormat",
	    "Defines new residence name increment when using automatic residence creation command if residence with that name already exist");
	AutomaticResidenceCreationIncrementFormat = c.get("Global.Optimizations.AutomaticResidenceCreation.IncrementFormat", "_[number]");

//	c.addComment("Global.Optimizations.DisabledNoFlagMessage.Use", "Enable if you want to hide no flag error messages in particular worlds",
//	    "You can bypass this with residence.checkbadflags permission node");
//	DisableNoFlagMessageUse = c.get("Global.Optimizations.DisabledNoFlagMessage.Use", false);
//	c.addComment("Global.Optimizations.DisabledNoFlagMessage.Worlds", "List Of Worlds where player wont get error messages");
//	DisableNoFlagMessageWorlds = c.get("Global.Optimizations.DisabledNoFlagMessage.Worlds", Arrays.asList(Bukkit.getWorlds().get(0).getName()));

	c.addComment("Global.Optimizations.GlobalChat.Enabled",
	    "Enables or disables chat modification by including players main residence name");
	GlobalChatEnabled = c.get("Global.Optimizations.GlobalChat.Enabled", false);
	c.addComment("Global.Optimizations.GlobalChat.SelfModify",
	    "Modifys chat to add chat titles.  If you're using a chat manager, you may add the tag {residence} to your chat format and disable this.");
	GlobalChatSelfModify = c.get("Global.Optimizations.GlobalChat.SelfModify", true);
	GlobalChatFormat = c.get("Global.Optimizations.GlobalChat.Format", "&c[&e%1&c]");

	c.addComment("Global.Optimizations.BlockAnyTeleportation",
	    "When this set to true, any teleportation to residence where player dont have tp flag, action will be denyied",
	    "This can prevent from teleporting players to residence with 3rd party plugins like esentials /tpa");
	BlockAnyTeleportation = c.get("Global.Optimizations.BlockAnyTeleportation", true);

	c.addComment("Global.Optimizations.MaxResCount", "Set this as low as possible depending of residence.max.res.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxResCount = c.get("Global.Optimizations.MaxResCount", 30);
	c.addComment("Global.Optimizations.MaxRentCount", "Set this as low as possible depending of residence.max.rents.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxRentCount = c.get("Global.Optimizations.MaxRentCount", 10);
	c.addComment("Global.Optimizations.MaxSubzoneCount", "Set this as low as possible depending of residence.max.subzones.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxSubzonesCount = c.get("Global.Optimizations.MaxSubzoneCount", 5);
	c.addComment("Global.Optimizations.MaxSubzoneDepthCount", "Set this as low as possible depending of residence.max.subzonedepth.[number] permission you are using",
	    "In example if you are giving max number of 10 for players, set it to 15, if its 30, set it to 35 just to have some small buffer in case");
	MaxSubzoneDepthCount = c.get("Global.Optimizations.MaxSubzoneDepthCount", 5);

	c.addComment("Global.Optimizations.OverridePvp", "By setting this to true, regular pvp flag will be acting as overridepvp flag",
	    "Overridepvp flag tries to ignore any pvp protection in that residence by any other plugin");
	OverridePvp = c.get("Global.Optimizations.OverridePvp", false);

	// residence kick location
	c.addComment("Global.Optimizations.KickLocation.Use",
	    "By setting this to true, when player kicks another player from residence, he will be teleported to this location instead of getting outside residence");
	Boolean UseKick = c.get("Global.Optimizations.KickLocation.Use", false);
	String KickLocationWorld = c.get("Global.Optimizations.KickLocation.World", defaultWorldName);
	Double KickLocationX = c.get("Global.Optimizations.KickLocation.X", 0.5);
	Double KickLocationY = c.get("Global.Optimizations.KickLocation.Y", 63.0);
	Double KickLocationZ = c.get("Global.Optimizations.KickLocation.Z", 0.5);
	c.addComment("Global.Optimizations.KickLocation.Pitch", "Less than 0 - head up, more than 0 - head down. Range from -90 to 90");
	Double KickPitch = c.get("Global.Optimizations.KickLocation.Pitch", 0.0);
	c.addComment("Global.Optimizations.KickLocation.Yaw", "Head position to left and right. Range from -180 to 180");
	Double KickYaw = c.get("Global.Optimizations.KickLocation.Yaw", 0.0);
	if (UseKick) {
	    World world = Bukkit.getWorld(KickLocationWorld);
	    if (world != null) {
		KickLocation = new Location(world, KickLocationX, KickLocationY, KickLocationZ);
		KickLocation.setPitch(KickPitch.floatValue());
		KickLocation.setYaw(KickYaw.floatValue());
	    }
	}

	c.addComment("Global.Optimizations.FlyLandLocation.World", "Used when players fly state is being turned to false because of fly flag and there is no solid land where to land for player");
	String FlyLocationWorld = c.get("Global.Optimizations.FlyLandLocation.World", defaultWorldName);
	Double FlyLocationX = c.get("Global.Optimizations.FlyLandLocation.X", 0.5);
	Double FlyLocationY = c.get("Global.Optimizations.FlyLandLocation.Y", 63.0);
	Double FlyLocationZ = c.get("Global.Optimizations.FlyLandLocation.Z", 0.5);
	c.addComment("Global.Optimizations.FlyLandLocation.Pitch", "Less than 0 - head up, more than 0 - head down. Range from -90 to 90");
	Double FlyPitch = c.get("Global.Optimizations.FlyLandLocation.Pitch", 0.0);
	c.addComment("Global.Optimizations.FlyLandLocation.Yaw", "Head position to left and right. Range from -180 to 180");
	Double FlyYaw = c.get("Global.Optimizations.FlyLandLocation.Yaw", 0.0);
	World world = Bukkit.getWorld(FlyLocationWorld);
	if (world != null) {
	    FlyLandLocation = new Location(world, FlyLocationX, FlyLocationY, FlyLocationZ);
	    FlyLandLocation.setPitch(FlyPitch.floatValue());
	    FlyLandLocation.setYaw(FlyYaw.floatValue());
	}

	c.addComment("Global.Optimizations.ShortInfo.Use",
	    "By setting this to true, when checking residence info with /res info, you will get only names in list, by hovering on them, you will get flag list");
	ShortInfoUse = c.get("Global.Optimizations.ShortInfo.Use", true);

	// Vote range
	c.addComment("Global.Optimizations.Vote.RangeFrom", "Range players can vote to, by default its from 0 to 10 points");
	VoteRangeFrom = c.get("Global.Optimizations.Vote.RangeFrom", 0);
	VoteRangeTo = c.get("Global.Optimizations.Vote.RangeTo", 10);

	c.addComment("Global.Optimizations.Vote.OnlyLike", "If this true, players can only give like for shop instead of point voting");
	OnlyLike = c.get("Global.Optimizations.Vote.OnlyLike", false);

	c.addComment("Global.Optimizations.ConsoleLogs.ShowFlagChanges", "If this true, flag changes throw GUI will be recorded in console");
	ConsoleLogsShowFlagChanges = c.get("Global.Optimizations.ConsoleLogs.ShowFlagChanges", true);

	// Healing/Feed interval
	c.addComment("Global.Optimizations.Intervals.Heal", "How often in seconds to heal/feed players in residence with appropriate flag",
	    "Bigger numbers can save some resources");
	HealInterval = c.get("Global.Optimizations.Intervals.Heal", 1);
	FeedInterval = c.get("Global.Optimizations.Intervals.Feed", 5);

	// negative potion effect list
	c.addComment("Global.Optimizations.NegativePotionEffects",
	    "Potions containing one of thos effects will be ignored if residence dont have pvp true flag set");
	NegativePotionEffects = c.get("Global.Optimizations.NegativePotionEffects", Arrays.asList("blindness", "confusion", "harm", "hunger", "poison", "slow",
	    "slow_digging", "weakness", "wither"));

	NegativeLingeringPotionEffects = c.get("Global.Optimizations.NegativeLingeringPotions", Arrays.asList("slowness", "instant_damage", "poison",
	    "slowness"));

	c.addComment("Global.Optimizations.WalkSpeed",
	    "Defines speed for particular wspeed1 and wspeed2 flags. It can be from 0 up to 5");
	WalkSpeed1 = c.get("Global.Optimizations.WalkSpeed.1", 0.5D);
	WalkSpeed1 = WalkSpeed1 < 0 ? 0 : WalkSpeed1;
	WalkSpeed1 = WalkSpeed1 > 5 ? 5 : WalkSpeed1;
	WalkSpeed1 = WalkSpeed1 / 5.0;
	WalkSpeed2 = c.get("Global.Optimizations.WalkSpeed.2", 2D);
	WalkSpeed2 = WalkSpeed2 < 0 ? 0 : WalkSpeed2;
	WalkSpeed2 = WalkSpeed2 > 5 ? 5 : WalkSpeed2;
	WalkSpeed2 = WalkSpeed2 / 5.0;

	c.addComment("Global.MoveCheckInterval", "The interval, in milliseconds, between movement checks.", "Reducing this will increase the load on the server.",
	    "Increasing this will allow players to move further in movement restricted zones before they are teleported out.");
	minMoveUpdate = c.get("Global.MoveCheckInterval", 500);

	c.addComment("Global.Tp.TeleportDelay", "The interval, in seconds, for teleportation.", "Use 0 to disable");
	TeleportDelay = c.get("Global.Tp.TeleportDelay", 3);
	c.addComment("Global.Tp.TeleportTitleMessage", "Show aditional message in title message area when player is teleporting to residence");
	TeleportTitleMessage = c.get("Global.Tp.TeleportTitleMessage", true);

	Set<World> worlds = new HashSet<World>();
	worlds.addAll(Bukkit.getWorlds());

	boolean commented = false;
	if (c.getC().isConfigurationSection("Global.RandomTeleportation.Worlds")) {
	    ConfigurationSection sec = c.getC().getConfigurationSection("Global.RandomTeleportation.Worlds");
	    if (sec != null)
		for (String one : sec.getKeys(false)) {
		    String path = "Global.RandomTeleportation.Worlds." + one + ".";

		    boolean enabled = c.get("Enabled", true);

		    if (!commented)
			c.addComment("Global.RandomTeleportation.Worlds." + one,
			    "World name to use this feature. Add annother one with appropriate name to enable random teleportation");

		    if (!commented)
			c.addComment(path + "MaxCoord", "Max coordinate to teleport, setting to 1000, player can be teleported between -1000 and 1000 coordinates");
		    int MaxCoord = c.get("MaxCoord", 1000);

		    if (!commented)
			c.addComment(path + "MinCoord",
			    "If maxcoord set to 1000 and mincoord to 500, then player can be teleported between -1000 to -500 and 1000 to 500 coordinates");
		    int MinCoord = c.get("MinCoord", c.getC().getInt(path + "MinCord", 500));
		    int CenterX = c.get("CenterX", 0);
		    int CenterZ = c.get("CenterZ", 0);

		    World w = getWorld(one);

		    if (w == null) {
			plugin.consoleMessage("&cCan't find world with (" + one + ") name");
			continue;
		    }

		    commented = true;
		    worlds.remove(w);

		    if (!enabled)
			continue;
		    RTeleport.add(new RandomTeleport(w, MaxCoord, MinCoord, CenterX, CenterZ));
		}
	}
	for (World one : worlds) {
	    String name = one.getName();
	    name = name.replace(".", "_");

	    String path = "Global.RandomTeleportation.Worlds." + name + ".";
	    boolean enabled = c.get("Enabled", true);
	    int MaxCoord = c.get("MaxCoord", 1000);
	    int MinCoord = c.get("MinCoord", c.getC().getInt(path + "MinCord", 500));
	    int CenterX = c.get("CenterX", 0);
	    int CenterZ = c.get("CenterZ", 0);

	    if (!enabled)
		continue;
	    RTeleport.add(new RandomTeleport(one, MaxCoord, MinCoord, CenterX, CenterZ));
	}

	c.addComment("Global.RandomTeleportation.Cooldown", "How long force player to wait before using command again.");
	rtCooldown = c.get("Global.RandomTeleportation.Cooldown", 5);

	c.addComment("Global.RandomTeleportation.MaxTries", "How many times to try find correct location for teleportation.",
	    "Keep it at low number, as player always can try again after delay");
	rtMaxTries = c.get("Global.RandomTeleportation.MaxTries", 20);

	c.addComment("Global.SaveInterval", "The interval, in minutes, between residence saves.");
	autoSaveInt = c.get("Global.SaveInterval", 10);
	c.addComment("Global.NewSaveMechanic", "New save mechanic can minimize save file couple times and speedup save/load time in general", "Bigger files have bigger impact");
	NewSaveMechanic = c.get("Global.NewSaveMechanic", true);

	c.addComment("Global.Backup.AutoCleanUp.Use",
	    "Do you want to automatically remove backup files from main backup folder if they are older than defined day amount");
	BackupAutoCleanUpUse = c.get("Global.Backup.AutoCleanUp.Use", false);
	BackupAutoCleanUpDays = c.get("Global.Backup.AutoCleanUp.Days", 30);

	c.addComment("Global.Backup.UseZip", "Do you want to backup files by creating zip files in main residence folder in backup folder",
	    "This wont have effect on regular backuped files made in save folder");
	UseZipBackup = c.get("Global.Backup.UseZip", true);

	BackupWorldFiles = c.get("Global.Backup.IncludeFiles.Worlds", true);
	BackupforsaleFile = c.get("Global.Backup.IncludeFiles.forsale", true);
	BackupleasesFile = c.get("Global.Backup.IncludeFiles.leases", true);
	BackuppermlistsFile = c.get("Global.Backup.IncludeFiles.permlists", true);
	BackuprentFile = c.get("Global.Backup.IncludeFiles.rent", true);
	BackupflagsFile = c.get("Global.Backup.IncludeFiles.flags", true);
	BackupgroupsFile = c.get("Global.Backup.IncludeFiles.groups", true);
	BackupconfigFile = c.get("Global.Backup.IncludeFiles.config", true);

	// Auto remove old residences
	c.addComment("Global.AutoCleanUp.Use", "HIGHLY EXPERIMENTAL residence cleaning on server startup if player is offline for x days.",
	    "Players can bypass this with residence.cleanbypass permission node");
	AutoCleanUp = c.get("Global.AutoCleanUp.Use", false);
	c.addComment("Global.AutoCleanUp.Days", "For how long player should be offline to delete hes residence");
	AutoCleanUpDays = c.get("Global.AutoCleanUp.Days", 60);
	c.addComment("Global.AutoCleanUp.Regenerate", "Do you want to regenerate old residence area", "This requires world edit to be present");
	AutoCleanUpRegenerate = c.get("Global.AutoCleanUp.Regenerate", false);
	c.addComment("Global.AutoCleanUp.Worlds", "Worlds to be included in check list");
	AutoCleanUpWorlds = c.get("Global.AutoCleanUp.Worlds", Arrays.asList(defaultWorldName));

	c.addComment("Global.Lwc.OnDelete", "Removes lwc protection from all defined objects when removing residence");
	LwcOnDelete = c.get("Global.Lwc.OnDelete", true);
	c.addComment("Global.Lwc.OnBuy", "Removes lwc protection from all defined objects when buying residence");
	LwcOnBuy = c.get("Global.Lwc.OnBuy", true);
	c.addComment("Global.Lwc.OnUnrent", "Removes lwc protection from all defined objects when unrenting residence");
	LwcOnUnrent = c.get("Global.Lwc.OnUnrent", true);

	c.addComment("Global.Lwc.MaterialList", "List of blocks you want to remove protection from");
	for (String oneName : c.get("Global.Lwc.MaterialList", Arrays.asList("CHEST", "TRAPPED_CHEST", "furnace", "dispenser"))) {
	    Material mat = Material.getMaterial(oneName.toUpperCase());
	    if (mat != null)
		LwcMatList.add(mat);
	    else
		Bukkit.getConsoleSender().sendMessage("Incorrect Lwc material name for " + oneName);
	}

	// TNT explosions below 63
	c.addComment("Global.AntiGreef.TNT.ExplodeBelow",
	    "When set to true will allow tnt and minecart with tnt to explode below 62 (default) level outside of residence",
	    "This will allow mining with tnt and more vanilla play");
	TNTExplodeBelow = c.get("Global.AntiGreef.TNT.ExplodeBelow", false);
	TNTExplodeBelowLevel = c.get("Global.AntiGreef.TNT.level", 62);
	// Creeper explosions below 63
	c.addComment("Global.AntiGreef.Creeper.ExplodeBelow", "When set to true will allow Creeper explode below 62 (default) level outside of residence",
	    "This will give more realistic game play");
	CreeperExplodeBelow = c.get("Global.AntiGreef.Creeper.ExplodeBelow", false);
	CreeperExplodeBelowLevel = c.get("Global.AntiGreef.Creeper.level", 62);
	// Flow
	c.addComment("Global.AntiGreef.Flow.Level", "Level from which one to start lava and water flow blocking", "This dont have effect in residence area");
	FlowLevel = c.get("Global.AntiGreef.Flow.Level", 63);
	c.addComment("Global.AntiGreef.Flow.NoLavaFlow", "With this set to true, lava flow outside residence is blocked");
	NoLava = c.get("Global.AntiGreef.Flow.NoLavaFlow", true);
	c.addComment("Global.AntiGreef.Flow.NoWaterFlow", "With this set to true, water flow outside residence is blocked");
	NoWater = c.get("Global.AntiGreef.Flow.NoWaterFlow", true);
	NoFlowWorlds = c.get("Global.AntiGreef.Flow.Worlds", Arrays.asList(defaultWorldName));

	// Place
	c.addComment("Global.AntiGreef.Place.Level", "Level from which one to start block lava and water place", "This don't have effect in residence area");
	PlaceLevel = c.get("Global.AntiGreef.Place.Level", 63);
	c.addComment("Global.AntiGreef.Place.NoLavaPlace", "With this set to true, playrs cant place lava outside residence");
	NoLavaPlace = c.get("Global.AntiGreef.Place.NoLavaPlace", true);
	c.addComment("Global.AntiGreef.Place.NoWaterPlace", "With this set to true, playrs cant place water outside residence");
	NoWaterPlace = c.get("Global.AntiGreef.Place.NoWaterPlace", true);
	NoPlaceWorlds = c.get("Global.AntiGreef.Place.Worlds", Arrays.asList(defaultWorldName));

	// Sand fall
	c.addComment("Global.AntiGreef.BlockFall.Use", "With this set to true, falling blocks will be deleted if they will land in different area");
	useBlockFall = c.get("Global.AntiGreef.BlockFall.Use", true);
	c.addComment("Global.AntiGreef.BlockFall.Level", "Level from which one to start block block's fall",
	    "This don't have effect in residence area or outside");
	BlockFallLevel = c.get("Global.AntiGreef.BlockFall.Level", 62);
	BlockFallWorlds = c.get("Global.AntiGreef.BlockFall.Worlds", Arrays.asList(defaultWorldName));

	// Res cleaning
	c.addComment("Global.AntiGreef.ResCleaning.Use",
	    "With this set to true, after player removes its residence, all blocks listed below, will be replaced with air blocks",
	    "Effective way to prevent residence creating near greefing target and then remove it",
	    "ATTENTION! Bigger residence areas could want to create bigger loads on server when cleaning up areas. So dont use this if regular player have access to huge residences. 15 million blocks would be a max limit");
	UseClean = c.get("Global.AntiGreef.ResCleaning.Use", false);
	c.addComment("Global.AntiGreef.ResCleaning.Level", "Level from whichone you want to replace blocks");
	CleanLevel = c.get("Global.AntiGreef.ResCleaning.Level", 63);
	c.addComment("Global.AntiGreef.ResCleaning.Blocks", "Block list to be replaced", "By default only water and lava will be replaced");
	List<?> pls = c.get("Global.AntiGreef.ResCleaning.Blocks", Arrays.asList(CMIMaterial.WATER.toString(), CMIMaterial.LAVA.toString()));
	for (Object one : pls) {
	    CMIMaterial mat = CMIMaterial.get(String.valueOf(one));
	    if (mat != CMIMaterial.NONE && mat.getMaterial() != null)
		CleanBlocks.add(mat.getMaterial());
	}

	CleanWorlds = c.get("Global.AntiGreef.ResCleaning.Worlds", Arrays.asList(defaultWorldName));

	c.addComment("Global.AntiGreef.Flags.Prevent",
	    "By setting this to true flags from list will be protected from change while there is some one inside residence besides owner",
	    "Protects in example from people inviting some one and changing pvp flag to true to kill them");
	PvPFlagPrevent = c.get("Global.AntiGreef.Flags.Prevent", true);
	FlagsList = c.get("Global.AntiGreef.Flags.list", Arrays.asList("pvp"));

	c.addComment("Global.DefaultGroup", "The default group to use if Permissions fails to attach or your not using Permissions.");
	defaultGroup = c.get("Global.DefaultGroup", "default");

	c.addComment("Global.UseLeaseSystem", "Enable / Disable the Lease System.");
	useLeases = c.get("Global.UseLeaseSystem", false);

	c.addComment("Global.DateFormat", "Sets date format when shown in example lease or rent expire date",
	    "How to use it properly, more information can be found at http://www.tutorialspoint.com/java/java_date_time.htm");
	DateFormat = c.get("Global.DateFormat", "E yyyy.MM.dd 'at' hh:mm:ss a zzz");

	c.addComment("Global.DateFormatShort", "Sets date format when shown in example lease or rent expire date",
	    "How to use it properly, more information can be found at http://www.tutorialspoint.com/java/java_date_time.htm");
	DateFormatShort = c.get("Global.DateFormatShort", "MM.dd hh:mm");

	c.addComment("Global.TimeZone", "Sets time zone for showing date, useful when server is in different country then main server player base",
	    "Full list of possible time zones can be found at http://www.mkyong.com/java/java-display-list-of-timezone-with-gmt/");
	TimeZone = c.get("Global.TimeZone", Calendar.getInstance().getTimeZone().getID());

	c.addComment("Global.ResMoneyBack", "Enable / Disable money returning on residence removal.");
	ResMoneyBack = c.get("Global.ResMoneyBack", false);

	c.addComment("Global.LeaseCheckInterval", "The interval, in minutes, between residence lease checks (if leases are enabled).");
	leaseCheckInterval = c.get("Global.LeaseCheckInterval", 10);

	c.addComment("Global.LeaseAutoRenew",
	    "Allows leases to automatically renew so long as the player has the money, if economy is disabled, this setting does nothing.");
	leaseAutoRenew = c.get("Global.LeaseAutoRenew", true);

	c.addComment("Global.EnablePermissions", "Whether or not to use the Permissions system in conjunction with this config.");
	c.get("Global.EnablePermissions", true);

	c.addComment("Global.LegacyPermissions", "Set to true if NOT using Permissions or PermissionsBukkit, or using a really old version of Permissions");
	legacyperms = c.get("Global.LegacyPermissions", false);

	c.addComment("Global.EnableEconomy",
	    "Enable / Disable Residence's Economy System (iConomy, MineConomy, Essentials, BOSEconomy, and RealEconomy supported).");
	enableEconomy = c.get("Global.EnableEconomy", true);

	c.addComment("Global.Type",
	    "Defaults to None which will start by looking to default economy engine throw vault API and if it fails to any supported economy engine",
	    "Custom economy engines can be defined to access economy directly", "Supported variables: " + EconomyType.toStringLine());
	VaultEconomy = EconomyType.getByName(c.get("Global.Type", "None"));
	if (VaultEconomy == null) {
	    plugin.consoleMessage("&cCould not determine economy from " + c.get("Global.Type", "Vault"));
	    plugin.consoleMessage("&cTrying to find suitable economy system");
	    VaultEconomy = EconomyType.None;
	}

	c.addComment("Global.ExtraEnterMessage",
	    "When enabled extra message will appear in chat if residence is for rent or for sell to inform how he can rent/buy residence with basic information.");
	ExtraEnterMessage = c.get("Global.ExtraEnterMessage", true);

	c.addComment("Global.Sell.Subzone", "If set to true, this will allow to sell subzones. Its recommended to keep it false tho");
	SellSubzone = c.get("Global.Sell.Subzone", false);

	c.addComment("Global.EnableRentSystem", "Enables or disables the Rent System");
	enableRentSystem = c.get("Global.EnableRentSystem", true);

//	TownEnabled = c.get("Global.Town.Enabled", true);
//	c.addComment("Global.Town.MinRange", "Range between residences","Protects from building residence near another residence if owner not belonging to same town");
//	TownMinRange = c.get("Global.Town.MinRange", 16);

	c.addComment("Global.Rent.PreventRemoval", "Prevents residence/subzone removal if its subzone is still rented by some one");
	RentPreventRemoval = c.get("Global.Rent.PreventRemoval", true);

	c.addComment("Global.Rent.DeductFromBank", "When set to true residence rent can be renewed from residence bank");
	DeductFromBank = c.get("Global.Rent.DeductFromBank", false);
	c.addComment("Global.Rent.DeductFromBankThenPlayer",
	    "When set to true residence rent can be renewed from residence bank and if there is not enough money then we will deduct rest of it from player",
	    "This will override behavior of DeductFromBank");
	DeductFromBankThenPlayer = c.get("Global.Rent.DeductFromBankThenPlayer", false);

	c.addComment("Global.Rent.Inform.OnEnding", "Informs players on rent time ending");
	RentInformOnEnding = c.get("Global.Rent.Inform.OnEnding", true);
	c.addComment("Global.Rent.Inform.Before", "Time range in minutes when to start informing about ending rent");
	RentInformBefore = c.get("Global.Rent.Inform.Before", 1440);
	c.addComment("Global.Rent.Inform.Delay", "Time range in seconds for how long to wait after player logs in to inform about ending rents");
	RentInformDelay = c.get("Global.Rent.Inform.Delay", 60);

	c.addComment("Global.Rent.DefaultValues.AllowRenewing", "Default values used when putting residence for rent");
	RentAllowRenewing = c.get("Global.Rent.DefaultValues.AllowRenewing", true);
	RentStayInMarket = c.get("Global.Rent.DefaultValues.StayInMarket", true);
	RentAllowAutoPay = c.get("Global.Rent.DefaultValues.AllowAutoPay", true);
	c.addComment("Global.Rent.DefaultValues.PlayerAutoPay", "If set to true, when player is not defining auto pay on renting, then this value will be used");
	RentPlayerAutoPay = c.get("Global.Rent.DefaultValues.PlayerAutoPay", true);

	c.addComment("Global.Rent.Schematics.RestoreAfterRentEnds",
	    "EXPERIMENTAL!!! If set to true, residence will be restored to state it was when backup flag was set to true",
	    "For securoty reassons only players with aditional residence.backup permission node can set backup flag");
	RestoreAfterRentEnds = c.get("Global.Rent.Schematics.RestoreAfterRentEnds", true);
	c.addComment("Global.Rent.Schematics.SaveOnFlagChange",
	    "When set to true, area state will be saved only when setting backup to true value",
	    "When set to false, area state will be saved before each renting to have always up to date area look",
	    "Keep in mind that when its set to false, there is slightly bigger server load as it has to save area each time when some one rents it");
	SchematicsSaveOnFlagChange = c.get("Global.Rent.Schematics.SaveOnFlagChange", true);

	c.addComment("Global.RentCheckInterval", "The interval, in minutes, between residence rent expiration checks (if the rent system is enabled).");
	rentCheckInterval = c.get("Global.RentCheckInterval", 10);

	c.addComment("Global.ResidenceChatEnable", "Enable or disable residence chat channels.");
	chatEnable = c.get("Global.ResidenceChatEnable", true);

	ELMessageType old = c.getC().isBoolean("Global.ActionBar.General") && c.getC().getBoolean("Global.ActionBar.General") ? ELMessageType.ActionBar : ELMessageType.ChatBox;
	old = c.getC().isBoolean("Global.TitleBar.EnterLeave") && c.getC().getBoolean("Global.TitleBar.EnterLeave") ? ELMessageType.TitleBar : old;

	c.addComment("Global.Messages.GeneralMessages", "Defines where you want to send residence enter/leave/deny move and similar messages. Possible options: " + ELMessageType.getAllValuesAsString(),
	    "TitleBar can have %subtitle% variable to define second line");
	EnterLeaveMessageType = ELMessageType.getByName(c.get("Global.Messages.GeneralMessages", old.toString()));
	if (EnterLeaveMessageType == null || Version.isCurrentEqualOrLower(Version.v1_7_R4))
	    EnterLeaveMessageType = ELMessageType.ChatBox;

	ActionBarOnSelection = c.get("Global.ActionBar.ShowOnSelection", true);

	c.addComment("Global.ResidenceChatColor", "Color of residence chat.");
	try {
	    chatColor = ChatColor.valueOf(c.get("Global.ResidenceChatColor", "DARK_PURPLE"));
	} catch (Exception ex) {
	    chatColor = ChatColor.DARK_PURPLE;
	}

	c.addComment("Global.ResidenceChatPrefixLenght", "Max lenght of residence chat prefix including color codes");
	chatPrefixLength = c.get("Global.ResidenceChatPrefixLength", 16);

	c.addComment("Global.AdminOnlyCommands",
	    "Whether or not to ignore the usual Permission flags and only allow OPs and groups with 'residence.admin' to change residences.");
	adminsOnly = c.get("Global.AdminOnlyCommands", false);

	c.addComment("Global.AdminOPs", "Setting this to true makes server OPs admins.");
	adminOps = c.get("Global.AdminOPs", true);

	c.addComment("Global.AdminFullAccess",
	    "Setting this to true server administration wont need to use /resadmin command to access admin command if they are op or have residence.admin permission node.");
	AdminFullAccess = c.get("Global.AdminFullAccess", false);

	c.addComment("Global.MultiWorldPlugin",
	    "This is the name of the plugin you use for multiworld, if you dont have a multiworld plugin you can safely ignore this.",
	    "The only thing this does is check to make sure the multiworld plugin is enabled BEFORE Residence, to ensure properly loading residences for other worlds.");
	multiworldPlugin = c.get("Global.MultiWorldPlugin", "Multiverse-Core");

	c.addComment("Global.ResidenceFlagsInherit", "Setting this to true causes subzones to inherit flags from their parent zones.");
	flagsInherit = c.get("Global.ResidenceFlagsInherit", true);

	c.addComment("Global.PreventRentModify", "Setting this to false will allow rented residences to be modified by the renting player.");
	preventBuildInRent = c.get("Global.PreventRentModify", true);

	c.addComment("Global.PreventSubZoneRemoval", "Setting this to true will prevent subzone deletion when subzone owner is not same as parent zone owner.");
	PreventSubZoneRemoval = c.get("Global.PreventSubZoneRemoval", true);

	c.addComment("Global.StopOnSaveFault", "Setting this to false will cause residence to continue to load even if a error is detected in the save file.");
	stopOnSaveError = c.get("Global.StopOnSaveFault", true);

	c.addComment(
	    "This is the residence name filter, that filters out invalid characters.  Google 'Java RegEx' or 'Java Regular Expressions' for more info on how they work.");
	namefix = c.get("Global.ResidenceNameRegex", "[^a-zA-Z0-9\\-\\_]");

	c.addComment("Global.ShowIntervalMessages",
	    "Setting this to true sends a message to the console every time Residence does a rent expire check or a lease expire check.");
	showIntervalMessages = c.get("Global.ShowIntervalMessages", false);

	c.addComment("Global.ShowNoobMessage", "Setting this to true sends a tutorial message to the new player when he places chest on ground.");
	ShowNoobMessage = c.get("Global.ShowNoobMessage", true);

	c.addComment("Global.NewPlayer", "Setting this to true creates residence around players placed chest if he don't have any.",
	    "Only once every server restart if he still don't have any residence");
	NewPlayerUse = c.get("Global.NewPlayer.Use", false);
	c.addComment("Global.NewPlayer.Free", "Setting this to true, residence will be created for free",
	    "By setting to false, money will be taken from player, if he has them");
	NewPlayerFree = c.get("Global.NewPlayer.Free", true);
	c.addComment("Global.NewPlayer.Range", "Range from placed chest o both sides. By setting to 5, residence will be 5+5+1 = 11 blocks wide");
	NewPlayerRangeX = c.get("Global.NewPlayer.Range.X", 5);
	NewPlayerRangeY = c.get("Global.NewPlayer.Range.Y", 5);
	NewPlayerRangeZ = c.get("Global.NewPlayer.Range.Z", 5);

	c.addComment("Global.CustomContainers",
	    "Experimental - The following settings are lists of block IDs to be used as part of the checks for the 'container' and 'use' flags when using mods.");
	pls = c.get("Global.CustomContainers", new ArrayList<String>());
	for (Object one : pls) {
	    CMIMaterial mat = CMIMaterial.get(String.valueOf(one));
	    if (mat != CMIMaterial.NONE)
		customContainers.add(mat);
	}

	pls = c.get("Global.CustomBothClick", new ArrayList<String>());
	for (Object one : pls) {
	    CMIMaterial mat = CMIMaterial.get(String.valueOf(one));
	    if (mat != CMIMaterial.NONE)
		customBothClick.add(mat);
	}

	pls = c.get("Global.CustomRightClick", new ArrayList<String>());
	for (Object one : pls) {
	    CMIMaterial mat = CMIMaterial.get(String.valueOf(one));
	    if (mat != CMIMaterial.NONE)
		customRightClick.add(mat);
	}

	c.addComment("Global.Visualizer.Use", "With this enabled player will see particle effects to mark selection boundaries");
	useVisualizer = c.get("Global.Visualizer.Use", true);
	c.addComment("Global.Visualizer.Range", "Range in blocks to draw particle effects for player",
	    "Keep it no more as 30, as player cant see more than 16 blocks");
	VisualizerRange = c.get("Global.Visualizer.Range", 16);
	c.addComment("Global.Visualizer.ShowFor", "For how long in miliseconds (5000 = 5sec) to show particle effects");
	VisualizerShowFor = c.get("Global.Visualizer.ShowFor", 5000);
	c.addComment("Global.Visualizer.updateInterval", "How often in ticks to update particles for player");
	VisualizerUpdateInterval = c.get("Global.Visualizer.updateInterval", 20);
	c.addComment("Global.Visualizer.RowSpacing", "Spacing in blocks between particle effects for rows");
	VisualizerRowSpacing = c.get("Global.Visualizer.RowSpacing", 2);
	if (VisualizerRowSpacing < 1)
	    VisualizerRowSpacing = 1;
	c.addComment("Global.Visualizer.CollumnSpacing", "Spacing in blocks between particle effects for collums");
	VisualizerCollumnSpacing = c.get("Global.Visualizer.CollumnSpacing", 2);
	if (VisualizerCollumnSpacing < 1)
	    VisualizerCollumnSpacing = 1;

	c.addComment("Global.Visualizer.SkipBy",
	    "Defines by how many particles we need to skip",
	    "This will create moving particle effect and will improve overall look of selection",
	    "By increasing this number, you can decrease update interval");
	VisualizerSkipBy = c.get("Global.Visualizer.SkipBy", 5);
	if (VisualizerSkipBy < 1)
	    VisualizerSkipBy = 1;

	c.addComment("Global.Visualizer.FrameCap", "Maximum amount of frame particles to show for one player");
	VisualizerFrameCap = c.get("Global.Visualizer.FrameCap", 500);
	if (VisualizerFrameCap < 1)
	    VisualizerFrameCap = 1;

	c.addComment("Global.Visualizer.SidesCap", "Maximum amount of sides particles to show for one player");
	VisualizerSidesCap = c.get("Global.Visualizer.SidesCap", 2000);
	if (VisualizerSidesCap < 1)
	    VisualizerSidesCap = 1;

	String effectsList = "";
	for (Effect one : Effect.values()) {
	    if (one == null)
		continue;
	    if (one.name() == null)
		continue;
	    effectsList += one.name().toLowerCase() + ", ";
	}

	c.addComment("Global.Visualizer.Selected",
	    "Particle effect names. possible: explode, largeexplode, hugeexplosion, fireworksSpark, splash, wake, crit, magicCrit",
	    " smoke, largesmoke, spell, instantSpell, mobSpell, mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager, townaura",
	    " note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust, snowballpoof, snowshovel, slime, heart, barrier", " droplet, take, mobappearance",
	    "",
	    "If using spigot based server different particles can be used:", effectsList);

	// Frame
	String efname = c.get("Global.Visualizer.Selected.Frame", "happyVillager");
	SelectedFrame = CMIParticle.getCMIParticle(efname);
	if (SelectedFrame == null) {
	    SelectedFrame = CMIParticle.HAPPY_VILLAGER;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Frame with this name, it was set to default");
	}

	// Sides
	efname = c.get("Global.Visualizer.Selected.Sides", "reddust");
	SelectedSides = CMIParticle.getCMIParticle(efname);
	if (SelectedSides == null) {
	    SelectedSides = CMIParticle.COLOURED_DUST;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Sides with this name, it was set to default");
	}

	efname = c.get("Global.Visualizer.Overlap.Frame", "FLAME");
	OverlapFrame = CMIParticle.getCMIParticle(efname);
	if (OverlapFrame == null) {
	    OverlapFrame = CMIParticle.FLAME;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Overlap Frame with this name, it was set to default");
	}

	efname = c.get("Global.Visualizer.Overlap.Sides", "FLAME");
	OverlapSides = CMIParticle.getCMIParticle(efname);
	if (OverlapSides == null) {
	    OverlapSides = CMIParticle.FLAME;
	    Bukkit.getConsoleSender().sendMessage("Can't find effect for Selected Sides with this name, it was set to default");
	}

	c.addComment("Global.Visualizer.EnterAnimation", "Shows particle effect when player enters residence. Only applies to main residence area");
	EnterAnimation = c.get("Global.Visualizer.EnterAnimation", true);

	c.addComment("Global.BounceAnimation", "Shows particle effect when player are being pushed back");
	BounceAnimation = c.get("Global.BounceAnimation", true);

	c.addComment("Global.GUI.Enabled", "Enable or disable flag GUI");
	useFlagGUI = c.get("Global.GUI.Enabled", true);

	c.addComment("Global.GUI.setTrue", "Item id and data to use when flag is set to true");

	CMIMaterial Mat = CMIMaterial.get(c.get("Global.GUI.setTrue", "GREEN_WOOL"));
	if (Mat == null)
	    Mat = CMIMaterial.GREEN_WOOL;
	guiBottonStates.put(FlagState.TRUE, Mat.newItemStack());

	c.addComment("Global.GUI.setFalse", "Item id and data to use when flag is set to false");
	Mat = CMIMaterial.get(c.get("Global.GUI.setFalse", "RED_WOOL"));
	if (Mat == null)
	    Mat = CMIMaterial.RED_WOOL;
	guiBottonStates.put(FlagState.FALSE, Mat.newItemStack());

	c.addComment("Global.GUI.setRemove", "Item id and data to use when flag is set to remove");
	Mat = CMIMaterial.get(c.get("Global.GUI.setRemove", "LIGHT_GRAY_WOOL"));
	if (Mat == null)
	    Mat = CMIMaterial.LIGHT_GRAY_WOOL;
	guiBottonStates.put(FlagState.NEITHER, Mat.newItemStack());

	c.addComment("Global.AutoMobRemoval", "Default = false. Enabling this, residences with flag nomobs will be cleared from monsters in regular intervals.",
	    "This is quite heavy on server side, so enable only if you really need this feature");
	AutoMobRemoval = c.get("Global.AutoMobRemoval.Use", false);
	c.addComment("Global.AutoMobRemoval.Interval", "How often in seconds to check for monsters in residences. Keep it at reasonable amount");
	AutoMobRemovalInterval = c.get("Global.AutoMobRemoval.Interval", 3);

	enforceAreaInsideArea = c.get("Global.EnforceAreaInsideArea", false);
	spoutEnable = c.get("Global.EnableSpout", false);
	enableLeaseMoneyAccount = c.get("Global.EnableLeaseMoneyAccount", true);

	c.addComment("Global.Couldroncompatibility",
	    "By setting this to true, partial compatibility for kCouldron servers will be enabled. Action bar messages and selection visualizer will be disabled automatically as off incorrect compatibility");
	Couldroncompatibility = c.get("Global.Couldroncompatibility", false);
	if (Couldroncompatibility) {
	    useVisualizer = false;
	    EnterLeaveMessageType = ELMessageType.ChatBox;
	    ActionBarOnSelection = false;
	}

	c.addComment("DynMap.Use", "Enables or disable DynMap Support");
	DynMapUse = c.get("DynMap.Use", false);
	c.addComment("DynMap.ShowFlags", "Shows or hides residence flags");
	DynMapShowFlags = c.get("DynMap.ShowFlags", true);
	c.addComment("DynMap.HideHidden", "If set true, residence with hidden flag set to true will be hidden from dynmap");
	DynMapHideHidden = c.get("DynMap.HideHidden", true);

	c.addComment("DynMap.Layer.3dRegions", "Enables 3D zones");
	DynMapLayer3dRegions = c.get("DynMap.Layer.3dRegions", true);
	c.addComment("DynMap.Layer.SubZoneDepth", "How deep to go into subzones to show");
	DynMapLayerSubZoneDepth = c.get("DynMap.Layer.SubZoneDepth", 2);

	c.addComment("DynMap.Border.Color", "Color of border. Pick color from this page http://www.w3schools.com/colors/colors_picker.asp");
	DynMapBorderColor = c.get("DynMap.Border.Color", "#FF0000");
	c.addComment("DynMap.Border.Opacity", "Transparency. 0.3 means that only 30% of color will be visible");
	DynMapBorderOpacity = c.get("DynMap.Border.Opacity", 0.3);
	c.addComment("DynMap.Border.Weight", "Border thickness");
	DynMapBorderWeight = c.get("DynMap.Border.Weight", 3);
	DynMapFillOpacity = c.get("DynMap.Fill.Opacity", 0.3);
	DynMapFillColor = c.get("DynMap.Fill.Color", "#FFFF00");
	DynMapFillForRent = c.get("DynMap.Fill.ForRent", "#33cc33");
	DynMapFillRented = c.get("DynMap.Fill.Rented", "#99ff33");
	DynMapFillForSale = c.get("DynMap.Fill.ForSale", "#0066ff");

	c.addComment("DynMap.VisibleRegions", "Shows only regions on this list");
	DynMapVisibleRegions = c.get("DynMap.VisibleRegions", new ArrayList<String>());
	c.addComment("DynMap.HiddenRegions", "Hides region on map even if its not hidden in game");
	DynMapHiddenRegions = c.get("DynMap.HiddenRegions", new ArrayList<String>());

	c.addComment("Raid", "In development");
	
	c.addComment("Raid.Enabled", "Determines if you want to enable raid feature for your server",
	    "When residence is under raid, attackers can move inside residence even if residence has move false flag");
	RaidEnabled = c.get("Raid.Enabled", false);
	c.addComment("Raid.PreTimer", "Time in seconds before raid starts", "This will allow defenders to get back to residence and prepare for attack");
	PreRaidTimer = c.get("Raid.PreTimer", 120);
	c.addComment("Raid.Timer", "Time in seconds raid should be", "During this time attackers can steal and kill defenders");
	RaidTimer = c.get("Raid.Timer", 120);
	c.addComment("Raid.Cooldown", "Time in seconds residence is immune for next raid","Default is 79200 seconds which results into 22 hours, this might reset if you have server restart");
	RaidCooldown = c.get("Raid.Cooldown", 79200);
	c.addComment("Raid.PlayerCooldown", "Time in seconds player is immune for next raid", "In case player has more than one residence, this can prevent player from being attacked again",
	    "Default is 79200 seconds which results into 22 hours, this might reset if you have server restart");
	RaidPlayerCooldown = c.get("Raid.PlayerCooldown", 79200);

	c.addComment("Raid.Allow.Attacker.blockBreak", "Allows to break blocks inside raided residence even if it has destroy false", "This only applies for attackers and for raid time");
	RaidAttackerBlockBreak = c.get("Raid.Allow.Attacker.blockBreak", true);
	c.addComment("Raid.Allow.Attacker.blockPlace", "Allows to place blocks inside raided residence even if it has place false", "This only applies for attackers and for raid time");
	RaidAttackerBlockPlace = c.get("Raid.Allow.Attacker.blockPlace", true);
	c.addComment("Raid.Allow.Defender.blockBreak", "Allows to break blocks inside raided residence even if it has destroy false", "This only applies for defenders and for raid time");
	RaidDefenderBlockBreak = c.get("Raid.Allow.Defender.blockBreak", true);
	c.addComment("Raid.Allow.Defender.blockPlace", "Allows to place blocks inside raided residence even if it has place false", "This only applies for defenders and for raid time");
	RaidDefenderBlockPlace = c.get("Raid.Allow.Defender.blockPlace", true);
	c.addComment("Raid.Allow.Defender.Teleport", "Allows to teleport into raided residence, includes /res tp and other 3rd party teleport commands", "This only applies for defenders and for raid time",
	    "keep in mind that attackers will not have this option and will be prevented from teleporting inside raided residence");
	RaidDefenderTeleport = c.get("Raid.Allow.Defender.Teleport", false);

	c.addComment("Raid.Allow.Defender.containerUsage", "Allows to use containers, such as chests, during raid time", "This only applies for defenders and for raid time",
	    "Keeping this at false might prevent from players moving their items to another residence which is not in raid", "Attackers will have access to any container in your residence during raid");
	RaidDefenderContainerUsage = c.get("Raid.Allow.Defender.containerUsage", false);

	c.addComment("Raid.FriendlyFire", "When set to false players in same teams (attackers or defenders) will not cause each other damage");
	RaidFriendlyFire = c.get("Raid.FriendlyFire", true);

	c.save();
    }

    public void loadFlags() {
	FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "flags.yml"));

	if (flags.isList("Global.TotalFlagDisabling")) {
	    List<String> globalDisable = flags.getStringList("Global.TotalFlagDisabling");

	    for (String fl : globalDisable) {
		Flags flag = Flags.getFlag(fl);
		if (flag == null)
		    continue;
		flag.setGlobalyEnabled(false);
	    }
	}

	globalCreatorDefaults = FlagPermissions.parseFromConfigNode("CreatorDefault", flags.getConfigurationSection("Global"));
	globalRentedDefaults = FlagPermissions.parseFromConfigNode("RentedDefault", flags.getConfigurationSection("Global"));
	globalResidenceDefaults = FlagPermissions.parseFromConfigNode("ResidenceDefault", flags.getConfigurationSection("Global"));
	loadGroups();
    }

    public void loadGroups() {
	FileConfiguration groups = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "groups.yml"));
	ConfigurationSection node = groups.getConfigurationSection("Global.GroupDefault");
	if (node != null) {
	    Set<String> keys = node.getConfigurationSection(defaultGroup).getKeys(false);
	    if (keys != null) {
		for (String key : keys) {
		    globalGroupDefaults.put(key, FlagPermissions.parseFromConfigNodeAsList(defaultGroup, "false"));
		}
	    }
	}
    }

    public World getWorld(String name) {
	name = name.replace("_", "").replace(".", "");
	for (World one : Bukkit.getWorlds()) {
	    if (one.getName().replace("_", "").replace(".", "").equalsIgnoreCase(name))
		return one;
	}
	return null;
    }

    public boolean isGlobalChatEnabled() {
	return GlobalChatEnabled;
    }

    public boolean isGlobalChatSelfModify() {
	return GlobalChatSelfModify;
    }

    public String getGlobalChatFormat() {
	return GlobalChatFormat;
    }

    public int getRentInformDelay() {
	return RentInformDelay;
    }

    public int getRentInformBefore() {
	return RentInformBefore;
    }

    public boolean isRentAllowAutoPay() {
	return RentAllowAutoPay;
    }

    public boolean isRentPlayerAutoPay() {
	return RentPlayerAutoPay;
    }

    public boolean isRentStayInMarket() {
	return RentStayInMarket;
    }

    public boolean isSellSubzone() {
	return SellSubzone;
    }

    public boolean isRentAllowRenewing() {
	return RentAllowRenewing;
    }

    public boolean isRentPreventRemoval() {
	return RentPreventRemoval;
    }

    public boolean isRentInformOnEnding() {
	return RentInformOnEnding;
    }

    public boolean isTNTExplodeBelow() {
	return TNTExplodeBelow;
    }

    public int getTNTExplodeBelowLevel() {
	return TNTExplodeBelowLevel;
    }

    public boolean isCreeperExplodeBelow() {
	return CreeperExplodeBelow;
    }

    public int getCreeperExplodeBelowLevel() {
	return CreeperExplodeBelowLevel;
    }

    public boolean useVisualizer() {
	return useVisualizer;
    }

    public int getVisualizerRange() {
	return VisualizerRange;
    }

    public int getVisualizerShowFor() {
	return VisualizerShowFor;
    }

    public int getNewPlayerRangeX() {
	return NewPlayerRangeX;
    }

    public int getNewPlayerRangeY() {
	return NewPlayerRangeY;
    }

    public int getNewPlayerRangeZ() {
	return NewPlayerRangeZ;
    }

    public int getVisualizerRowSpacing() {
	return VisualizerRowSpacing;
    }

    public int getVisualizerCollumnSpacing() {
	return VisualizerCollumnSpacing;
    }

    public int getVisualizerSkipBy() {
	return VisualizerSkipBy;
    }

    public int getVisualizerUpdateInterval() {
	return VisualizerUpdateInterval;
    }

    public CMIParticle getSelectedFrame() {
	return SelectedFrame;
    }

    public CMIParticle getSelectedSides() {
	return SelectedSides;
    }

    public CMIParticle getOverlapFrame() {
	return OverlapFrame;
    }

    public CMIParticle getOverlapSides() {
	return OverlapSides;
    }

    @Deprecated
    public CMIParticle getSelectedSpigotFrame() {
	return SelectedFrame;
    }

    @Deprecated
    public CMIParticle getSelectedSpigotSides() {
	return SelectedSides;
    }

    @Deprecated
    public CMIParticle getOverlapSpigotFrame() {
	return OverlapFrame;
    }

    @Deprecated
    public CMIParticle getOverlapSpigotSides() {
	return OverlapSides;
    }

    public int getTeleportDelay() {
	return TeleportDelay;
    }

    public boolean isTeleportTitleMessage() {
	return TeleportTitleMessage;
    }

    public boolean useLegacyPermissions() {
	return legacyperms;
    }

    public String getDefaultGroup() {
	return defaultGroup;
    }

    public String getResidenceNameRegex() {
	return namefix;
    }

    public boolean isExtraEnterMessage() {
	return ExtraEnterMessage;
    }

    public boolean enableEconomy() {
	return enableEconomy && plugin.getEconomyManager() != null;
    }

    public boolean enabledRentSystem() {
	return enableRentSystem && enableEconomy();
    }

    public boolean useLeases() {
	return useLeases;
    }

    public boolean useResMoneyBack() {
	return ResMoneyBack;
    }

    public boolean allowAdminsOnly() {
	return adminsOnly;
    }

    public boolean allowEmptyResidences() {
	return allowEmptyResidences;
    }

    public boolean isNoLava() {
	return NoLava;
    }

    public boolean isNoWater() {
	return NoWater;
    }

    public boolean isNoLavaPlace() {
	return NoLavaPlace;
    }

    public boolean isBlockFall() {
	return useBlockFall;
    }

    public boolean isNoWaterPlace() {
	return NoWaterPlace;
    }

    public List<Material> getLwcMatList() {
	return LwcMatList;
    }

    public boolean isRemoveLwcOnUnrent() {
	return LwcOnUnrent;
    }

    public boolean isRemoveLwcOnBuy() {
	return LwcOnBuy;
    }

    public boolean isRemoveLwcOnDelete() {
	return LwcOnDelete;
    }

    public boolean isUseResidenceFileClean() {
	return AutoCleanUp;
    }

    public int getResidenceFileCleanDays() {
	return AutoCleanUpDays;
    }

    public boolean isAutoCleanUpRegenerate() {
	return AutoCleanUpRegenerate;
    }

    public boolean isUseClean() {
	return UseClean;
    }

    public boolean isPvPFlagPrevent() {
	return PvPFlagPrevent;
    }

    public boolean isOverridePvp() {
	return OverridePvp;
    }

    public boolean isBlockAnyTeleportation() {
	return BlockAnyTeleportation;
    }

    @Deprecated
    public int getInfoToolID() {
	return infoTool.getId();
    }

    public CMIMaterial getInfoTool() {
	return infoTool;
    }

    public CMIMaterial getSelectionTool() {
	return selectionTool;
    }

    @Deprecated
    public int getSelectionTooldID() {
	return selectionTool.getId();
    }

    public boolean getOpsAreAdmins() {
	return adminOps;
    }

    public boolean getAdminFullAccess() {
	return AdminFullAccess;
    }

    public String getMultiworldPlugin() {
	return multiworldPlugin;
    }

    public boolean autoRenewLeases() {
	return leaseAutoRenew;
    }

    public boolean isShortInfoUse() {
	return ShortInfoUse;
    }

    public boolean isOnlyLike() {
	return OnlyLike;
    }

    public int getRentCheckInterval() {
	return rentCheckInterval;
    }

    public int getChatPrefixLength() {
	return chatPrefixLength;
    }

    public int getLeaseCheckInterval() {
	return leaseCheckInterval;
    }

    public int getAutoSaveInterval() {
	return autoSaveInt;
    }

    public boolean isNewSaveMechanic() {
	return NewSaveMechanic;
    }

    // backup stuff   
    public boolean BackupAutoCleanUpUse() {
	return BackupAutoCleanUpUse;
    }

    public int BackupAutoCleanUpDays() {
	return BackupAutoCleanUpDays;
    }

    public boolean UseZipBackup() {
	return UseZipBackup;
    }

    public boolean BackupWorldFiles() {
	return BackupWorldFiles;
    }

    public boolean BackupforsaleFile() {
	return BackupforsaleFile;
    }

    public boolean BackupleasesFile() {
	return BackupleasesFile;
    }

    public boolean BackuppermlistsFile() {
	return BackuppermlistsFile;
    }

    public boolean BackuprentFile() {
	return BackuprentFile;
    }

    public boolean BackupflagsFile() {
	return BackupflagsFile;
    }

    public boolean BackupgroupsFile() {
	return BackupgroupsFile;
    }

    public boolean BackupconfigFile() {
	return BackupconfigFile;
    }
    // backup stuff

    public int getFlowLevel() {
	return FlowLevel;
    }

    public int getPlaceLevel() {
	return PlaceLevel;
    }

    public int getBlockFallLevel() {
	return BlockFallLevel;
    }

    public int getCleanLevel() {
	return CleanLevel;
    }

    public boolean flagsInherit() {
	return flagsInherit;
    }

    public boolean chatEnabled() {
	return chatEnable;
    }

    public boolean useActionBarOnSelection() {
	return ActionBarOnSelection;
    }

    public ChatColor getChatColor() {
	return chatColor;
    }

    public int getMinMoveUpdateInterval() {
	return minMoveUpdate;
    }

    public int getMaxResCount() {
	return MaxResCount;
    }

    public int getMaxRentCount() {
	return MaxRentCount;
    }

    public int getMaxSubzonesCount() {
	return MaxSubzonesCount;
    }

    public int getMaxSubzoneDepthCount() {
	return MaxSubzoneDepthCount;
    }

    public int getVoteRangeFrom() {
	return VoteRangeFrom;
    }

    public int getHealInterval() {
	return HealInterval;
    }

    public int getFeedInterval() {
	return FeedInterval;
    }

    public int getVoteRangeTo() {
	return VoteRangeTo;
    }

    public FlagPermissions getGlobalCreatorDefaultFlags() {
	return globalCreatorDefaults;
    }

    public FlagPermissions getGlobalRentedDefaultFlags() {
	return globalRentedDefaults;
    }

    public FlagPermissions getGlobalResidenceDefaultFlags() {
	return globalResidenceDefaults;
    }

    public Map<String, FlagPermissions> getGlobalGroupDefaultFlags() {
	return globalGroupDefaults;
    }

    public String getLanguage() {
	return language;
    }

    public String getDefaultWorld() {
	return DefaultWorld;
    }

    public String getDateFormat() {
	return DateFormat;
    }

    public String getDateFormatShort() {
	return DateFormatShort;
    }

    public String getTimeZone() {
	return TimeZone;
    }

    public boolean preventRentModify() {
	return preventBuildInRent;
    }

    public boolean isPreventSubZoneRemoval() {
	return PreventSubZoneRemoval;
    }

    public boolean stopOnSaveError() {
	return stopOnSaveError;
    }

    public boolean showIntervalMessages() {
	return showIntervalMessages;
    }

    public boolean ShowNoobMessage() {
	return ShowNoobMessage;
    }

    public boolean isNewPlayerUse() {
	return NewPlayerUse;
    }

    public boolean isNewPlayerFree() {
	return NewPlayerFree;
    }

    public boolean enableSpout() {
	return spoutEnable;
    }

    public boolean AutoMobRemoval() {
	return AutoMobRemoval;
    }

    public int AutoMobRemovalInterval() {
	return AutoMobRemovalInterval;
    }

    public boolean enableLeaseMoneyAccount() {
	return enableLeaseMoneyAccount;
    }

    public boolean CouldronCompatibility() {
	return Couldroncompatibility;
    }

    public boolean debugEnabled() {
	return enableDebug;
    }

    public boolean isSelectionIgnoreY() {
	return SelectionIgnoreY;
    }

    public boolean isSelectionIgnoreYInSubzone() {
	return SelectionIgnoreYInSubzone;
    }

    public boolean isNoCostForYBlocks() {
	return NoCostForYBlocks;
    }

    public boolean versionCheck() {
	return versionCheck;
    }

    public boolean isUUIDConvertion() {
	return UUIDConvertion;
    }

    public boolean isOfflineMode() {
	return OfflineMode;
    }

    public List<CMIMaterial> getCustomContainers() {
	return customContainers;
    }

    public List<CMIMaterial> getCustomBothClick() {
	return customBothClick;
    }

    public List<CMIMaterial> getCustomRightClick() {
	return customRightClick;
    }

    public List<Material> getCleanBlocks() {
	return CleanBlocks;
    }

    public List<String> getNoFlowWorlds() {
	return NoFlowWorlds;
    }

    public List<String> getAutoCleanUpWorlds() {
	return AutoCleanUpWorlds;
    }

    public List<String> getNoPlaceWorlds() {
	return NoPlaceWorlds;
    }

    public List<String> getBlockFallWorlds() {
	return BlockFallWorlds;
    }

    public List<String> getNegativePotionEffects() {
	return NegativePotionEffects;
    }

    public List<String> getNegativeLingeringPotionEffects() {
	return NegativeLingeringPotionEffects;
    }

    public List<String> getCleanWorlds() {
	return CleanWorlds;
    }

    public List<String> getProtectedFlagsList() {
	return FlagsList;
    }

    public boolean getEnforceAreaInsideArea() {
	return enforceAreaInsideArea;
    }

    public List<RandomTeleport> getRandomTeleport() {
	return RTeleport;
    }

    public int getrtCooldown() {
	return rtCooldown;
    }

    public Location getKickLocation() {
	return KickLocation;
    }

    public Location getFlyLandLocation() {
	return FlyLandLocation;
    }

    public int getrtMaxTries() {
	return rtMaxTries;
    }

    public boolean useFlagGUI() {
	return useFlagGUI;
    }

    public boolean BounceAnimation() {
	return BounceAnimation;
    }

    public int getVisualizerFrameCap() {
	return VisualizerFrameCap;
    }

    public int getVisualizerSidesCap() {
	return VisualizerSidesCap;
    }

    public Double getWalkSpeed1() {
	return WalkSpeed1;
    }

    public Double getWalkSpeed2() {
	return WalkSpeed2;
    }

    public int getItemPickUpDelay() {
	return ItemPickUpDelay;
    }

    public boolean isAutomaticResidenceCreationCheckCollision() {
	return AutomaticResidenceCreationCheckCollision;
    }

    public String AutomaticResidenceCreationIncrementFormat() {
	return AutomaticResidenceCreationIncrementFormat;
    }

    public boolean isConsoleLogsShowFlagChanges() {
	return ConsoleLogsShowFlagChanges;
    }

    public EconomyType getEconomyType() {
	return VaultEconomy;
    }

    public boolean isCanTeleportIncludeOwner() {
	return CanTeleportIncludeOwner;
    }

    public ELMessageType getEnterLeaveMessageType() {
	return EnterLeaveMessageType;
    }

    public boolean isEnterAnimation() {
	return EnterAnimation;
    }

    public boolean isDeductFromBank() {
	return DeductFromBank;
    }

    public boolean isDeductFromBankThenPlayer() {
	return DeductFromBankThenPlayer;
    }

    public ItemStack getGuiBottonStates(FlagState state) {
	return guiBottonStates.get(state);
    }

//    public int getTownMinRange() {
//	return TownMinRange;
//    }
//
//    public boolean isTownEnabled() {
//	return TownEnabled;
//    }
}