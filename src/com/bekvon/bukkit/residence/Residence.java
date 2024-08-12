package com.bekvon.bukkit.residence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.kingdoms.main.Kingdoms;
import org.kingdoms.manager.game.GameManagement;

import com.bekvon.bukkit.residence.Placeholders.Placeholder;
import com.bekvon.bukkit.residence.Placeholders.PlaceholderAPIHook;
import com.bekvon.bukkit.residence.allNms.v1_10Events;
import com.bekvon.bukkit.residence.allNms.v1_13Events;
import com.bekvon.bukkit.residence.allNms.v1_8Events;
import com.bekvon.bukkit.residence.allNms.v1_9Events;
import com.bekvon.bukkit.residence.api.ChatInterface;
import com.bekvon.bukkit.residence.api.MarketBuyInterface;
import com.bekvon.bukkit.residence.api.MarketRentInterface;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.api.ResidencePlayerInterface;
import com.bekvon.bukkit.residence.bigDoors.BigDoorsManager;
import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.commands.padd;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.MinimizeFlags;
import com.bekvon.bukkit.residence.containers.MinimizeMessages;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.dynmap.DynMapListeners;
import com.bekvon.bukkit.residence.dynmap.DynMapManager;
import com.bekvon.bukkit.residence.economy.BlackHoleEconomy;
import com.bekvon.bukkit.residence.economy.CMIEconomy;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.EssentialsEcoAdapter;
import com.bekvon.bukkit.residence.economy.IConomy6Adapter;
import com.bekvon.bukkit.residence.economy.RealShopEconomy;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.gui.FlagUtil;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidenceFixesListener;
import com.bekvon.bukkit.residence.listeners.ResidenceLWCListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_12;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_13;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_14;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_15;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_16;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_17;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_19;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_20;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_21;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener1_9;
import com.bekvon.bukkit.residence.listeners.SpigotListener;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.pl3xmap.Pl3xMapListeners;
import com.bekvon.bukkit.residence.pl3xmap.Pl3xMapManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.protection.PlayerManager;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.bekvon.bukkit.residence.raid.ResidenceRaidListener;
import com.bekvon.bukkit.residence.selection.AutoSelection;
import com.bekvon.bukkit.residence.selection.KingdomsUtil;
import com.bekvon.bukkit.residence.selection.Schematics7Manager;
import com.bekvon.bukkit.residence.selection.SchematicsManager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WESchematicManager;
import com.bekvon.bukkit.residence.selection.WorldEdit7SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.selection.WorldGuard7Util;
import com.bekvon.bukkit.residence.selection.WorldGuardInterface;
import com.bekvon.bukkit.residence.selection.WorldGuardUtil;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopSignUtil;
import com.bekvon.bukkit.residence.signsStuff.SignUtil;
import com.bekvon.bukkit.residence.slimeFun.SlimefunManager;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.CrackShot;
import com.bekvon.bukkit.residence.utils.FileCleanUp;
import com.bekvon.bukkit.residence.utils.RandomTp;
import com.bekvon.bukkit.residence.utils.SafeLocationCache;
import com.bekvon.bukkit.residence.utils.Sorting;
import com.bekvon.bukkit.residence.utils.TabComplete;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.earth2me.essentials.Essentials;
import com.residence.mcstats.Metrics;
import com.residence.zip.ZipLibrary;

import fr.crafter.tickleman.realeconomy.RealEconomy;
import fr.crafter.tickleman.realplugin.RealPlugin;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.Util.CMIVersionChecker;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;
import net.pl3x.map.core.Pl3xMap;

/**
 * 
 * @author Gary Smoak - bekvon
 * 
 */
public class Residence extends JavaPlugin {

    private static Residence instance;

    private boolean fullyLoaded = false;

    protected String ResidenceVersion;
    protected List<String> authlist;
    protected ResidenceManager rmanager;
    protected SelectionManager smanager;
    public PermissionManager gmanager;
    protected ConfigManager configManager;

    protected boolean spigotPlatform = false;

    protected SignUtil signmanager;

    protected ResidenceBlockListener blistener;
    protected ResidencePlayerListener plistener;
    protected ResidenceEntityListener elistener;

    protected ResidenceFixesListener flistener;
    protected ResidenceRaidListener slistener;

    protected ResidenceCommandListener commandManager;

    protected SpigotListener spigotlistener;
    protected ShopListener shlistener;
    protected TransactionManager tmanager;
    protected PermissionListManager pmanager;
    protected LeaseManager leasemanager;
    public WorldItemManager imanager;
    public WorldFlagManager wmanager;
    protected RentManager rentmanager;
    protected ChatManager chatmanager;
    protected Server server;
    public HelpEntry helppages;
    protected LocaleManager LocaleManager;
    protected Language newLanguageManager;
    protected PlayerManager PlayerManager;
    protected FlagUtil FlagUtilManager;
    protected ShopSignUtil ShopSignUtilManager;
//    private TownManager townManager;
    protected RandomTp RandomTpManager;
    protected DynMapManager DynManager;
    protected Pl3xMapManager Pl3xManager;
    protected Sorting SortingManager;
    protected AutoSelection AutoSelectionManager;
    protected WESchematicManager SchematicManager;
    private InformationPager InformationPagerManager;
    private WorldGuardInterface worldGuardUtil;
    private int wepVersion = 6;
    private KingdomsUtil kingdomsUtil;

    protected CommandFiller cmdFiller;

    protected ZipLibrary zip;

    protected boolean firstenable = true;
    protected EconomyInterface economy;
    public File dataFolder;
    protected CMITask leaseBukkitId = null;
    protected CMITask rentBukkitId = null;
    protected CMITask healBukkitId = null;
    protected CMITask feedBukkitId = null;
    protected CMITask effectRemoveBukkitId = null;
    protected CMITask DespawnMobsBukkitId = null;
    protected CMITask autosaveBukkitId = null;

    private boolean SlimeFun = false;
    private boolean BigDoors = false;
    private boolean lwc = false;
    Metrics metrics = null;

    protected boolean initsuccess = false;
    public Map<String, String> deleteConfirm;
    public Map<String, String> UnrentConfirm = new HashMap<String, String>();
    public List<String> resadminToggle;
    private ConcurrentHashMap<String, OfflinePlayer> OfflinePlayerList = new ConcurrentHashMap<String, OfflinePlayer>();
    private Map<UUID, OfflinePlayer> cachedPlayerNameUUIDs = new HashMap<UUID, OfflinePlayer>();
    private Map<UUID, String> cachedPlayerNames = new HashMap<UUID, String>();
    private com.sk89q.worldedit.bukkit.WorldEditPlugin wep = null;
    private com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = null;
    private CMIMaterial wepid;

//    private String ServerLandname = "Server_Land";
    private UUID ServerLandUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private UUID TempUserUUID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    public HashMap<String, Long> rtMap = new HashMap<String, Long>();
    public List<String> teleportDelayMap = new ArrayList<String>();
    public HashMap<UUID, SafeLocationCache> teleportMap = new HashMap<UUID, SafeLocationCache>();

    private Placeholder Placeholder;
    private boolean PlaceholderAPIEnabled = false;

    private String prefix = ChatColor.GREEN + "[" + ChatColor.GOLD + "Residence" + ChatColor.GREEN + "]" + ChatColor.GRAY;

    public boolean isSpigot() {
        return spigotPlatform;
    }

    public HashMap<UUID, SafeLocationCache> getTeleportMap() {
        return teleportMap;
    }

    public List<String> getTeleportDelayMap() {
        return teleportDelayMap;
    }

    public HashMap<String, Long> getRandomTeleportMap() {
        return rtMap;
    }

    // API
    private ResidenceApi API = new ResidenceApi();
    private MarketBuyInterface MarketBuyAPI = null;
    private MarketRentInterface MarketRentAPI = null;
    private ResidencePlayerInterface PlayerAPI = null;
    private ResidenceInterface ResidenceAPI = null;
    private ChatInterface ChatAPI = null;

    public ResidencePlayerInterface getPlayerManagerAPI() {
        if (PlayerAPI == null)
            PlayerAPI = PlayerManager;
        return PlayerAPI;
    }

    public ResidenceInterface getResidenceManagerAPI() {
        if (ResidenceAPI == null)
            ResidenceAPI = rmanager;
        return ResidenceAPI;
    }

    public Placeholder getPlaceholderAPIManager() {
        if (Placeholder == null)
            Placeholder = new Placeholder(this);
        return Placeholder;
    }

    public boolean isPlaceholderAPIEnabled() {
        return PlaceholderAPIEnabled;
    }

    public MarketRentInterface getMarketRentManagerAPI() {
        if (MarketRentAPI == null)
            MarketRentAPI = rentmanager;
        return MarketRentAPI;
    }

    public MarketBuyInterface getMarketBuyManagerAPI() {
        if (MarketBuyAPI == null)
            MarketBuyAPI = tmanager;
        return MarketBuyAPI;

    }

    public ChatInterface getResidenceChatAPI() {
        if (ChatAPI == null)
            ChatAPI = chatmanager;
        return ChatAPI;
    }

    public ResidenceCommandListener getCommandManager() {
        if (commandManager == null)
            commandManager = new ResidenceCommandListener(this);
        return commandManager;
    }

    public ResidenceApi getAPI() {
        return API;
    }
    // API end

    private Runnable doHeals = new Runnable() {
        @Override
        public void run() {
            plistener.doHeals();
        }
    };

    private Runnable doFeed = new Runnable() {
        @Override
        public void run() {
            plistener.feed();
        }
    };

    private Runnable removeBadEffects = new Runnable() {
        @Override
        public void run() {
            plistener.badEffects();
        }
    };

    private Runnable DespawnMobs = new Runnable() {
        @Override
        public void run() {
            plistener.DespawnMobs();
        }
    };

    private Runnable rentExpire = new Runnable() {
        @Override
        public void run() {
            rentmanager.checkCurrentRents();
            if (getConfigManager().showIntervalMessages()) {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " - Rent Expirations checked!");
            }
        }
    };
    private Runnable leaseExpire = new Runnable() {
        @Override
        public void run() {
            leasemanager.doExpirations();
            if (getConfigManager().showIntervalMessages()) {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " - Lease Expirations checked!");
            }
        }
    };
    private Runnable autoSave = new Runnable() {
        @Override
        public void run() {
            try {
                if (initsuccess) {
                    CMIScheduler.runTaskAsynchronously(() -> {
                        try {
                            saveYml();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, getPrefix() + " SEVERE SAVE ERROR", ex);
            }
        }
    };

    public void reloadPlugin() {
        this.onDisable();
        this.reloadConfig();
        this.onEnable();
    }

    @Override
    public void onDisable() {
        if (autosaveBukkitId != null)
            autosaveBukkitId.cancel();
        if (healBukkitId != null)
            healBukkitId.cancel();
        if (feedBukkitId != null)
            feedBukkitId.cancel();
        if (effectRemoveBukkitId != null)
            effectRemoveBukkitId.cancel();
        if (DespawnMobsBukkitId != null)
            DespawnMobsBukkitId.cancel();

        this.getPermissionManager().stopCacheClearScheduler();

        this.getSelectionManager().onDisable();

        if (this.metrics != null)
            try {
                metrics.disable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (getConfigManager().useLeases() && leaseBukkitId != null) {
            leaseBukkitId.cancel();
        }
        if (getConfigManager().enabledRentSystem() && rentBukkitId != null) {
            rentBukkitId.cancel();
        }

        if (getDynManager() != null && getDynManager().getMarkerSet() != null)
            getDynManager().getMarkerSet().deleteMarkerSet();

        if (initsuccess) {
            try {
                saveYml();
                if (zip != null)
                    zip.backup();
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, "[Residence] SEVERE SAVE ERROR", ex);
            }
            Bukkit.getConsoleSender().sendMessage(getPrefix() + " Disabled!");
        }
    }

    @Override
    public void onEnable() {
        try {
            instance = this;

            initsuccess = false;
            deleteConfirm = new HashMap<String, String>();
            resadminToggle = new ArrayList<String>();
            server = this.getServer();
            dataFolder = this.getDataFolder();

            ResidenceVersion = this.getDescription().getVersion();
            authlist = this.getDescription().getAuthors();

            cmdFiller = new CommandFiller();
            cmdFiller.fillCommands();

            SortingManager = new Sorting();

            if (!dataFolder.isDirectory()) {
                dataFolder.mkdirs();
            }

            if (!new File(dataFolder, "groups.yml").isFile() && !new File(dataFolder, "flags.yml").isFile() && new File(dataFolder, "config.yml").isFile()) {
                this.ConvertFile();
            }

            if (!new File(dataFolder, "uuids.yml").isFile()) {
                File file = new File(this.getDataFolder(), "uuids.yml");
                file.createNewFile();
            }

            if (!new File(dataFolder, "flags.yml").isFile()) {
                this.writeDefaultFlagsFromJar();
            }
            if (!new File(dataFolder, "groups.yml").isFile()) {
                this.writeDefaultGroupsFromJar();
            }

            this.getCommand("res").setExecutor(getCommandManager());
            this.getCommand("resadmin").setExecutor(getCommandManager());
            this.getCommand("residence").setExecutor(getCommandManager());

            this.getCommand("rc").setExecutor(getCommandManager());
            this.getCommand("resreload").setExecutor(getCommandManager());
            this.getCommand("resload").setExecutor(getCommandManager());

            TabComplete tab = new TabComplete();
            this.getCommand("res").setTabCompleter(tab);
            this.getCommand("resadmin").setTabCompleter(tab);
            this.getCommand("residence").setTabCompleter(tab);

//	    Residence.getConfigManager().UpdateConfigFile();

//	    if (this.getConfig().getInt("ResidenceVersion", 0) == 0) {
//		this.writeDefaultConfigFromJar();
//		this.getConfig().load("config.yml");
//		System.out.println("[Residence] Config Invalid, wrote default...");
//	    }
            String multiworld = getConfigManager().getMultiworldPlugin();
            if (multiworld != null) {
                Plugin plugin = server.getPluginManager().getPlugin(multiworld);
                if (plugin != null && !plugin.isEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " - Enabling multiworld plugin: " + multiworld);
                    server.getPluginManager().enablePlugin(plugin);
                }
            }

            getConfigManager().UpdateFlagFile();

            getFlagUtilManager().load();

            try {
                Class<?> c = Class.forName("org.bukkit.entity.Player");
                for (Method one : c.getDeclaredMethods()) {
                    if (one.getName().equalsIgnoreCase("Spigot"))
                        spigotPlatform = true;
                }
            } catch (Exception e) {
            }

            this.getPermissionManager().startCacheClearScheduler();

            imanager = new WorldItemManager(this);
            wmanager = new WorldFlagManager(this);

            chatmanager = new ChatManager();
            rentmanager = new RentManager(this);

            LocaleManager = new LocaleManager(this);

            PlayerManager = new PlayerManager(this);
            ShopSignUtilManager = new ShopSignUtil(this);
            RandomTpManager = new RandomTp(this);
//	    townManager = new TownManager(this);

            InformationPagerManager = new InformationPager(this);

            zip = new ZipLibrary(this);

            Plugin lwcp = Bukkit.getPluginManager().getPlugin("LWC");
            try {
                if (lwcp != null) {
                    try {
                        ResidenceLWCListener.register(this);
                        Bukkit.getConsoleSender().sendMessage(this.getPrefix() + " LWC hooked.");
                        lwc = true;
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            SlimeFun = Bukkit.getPluginManager().getPlugin("Slimefun") != null;

            if (SlimeFun) {
                try {
                    SlimefunManager.register(this);
                } catch (Throwable e) {
                    SlimeFun = false;
                    e.printStackTrace();
                }
            }

            BigDoors = Bukkit.getPluginManager().getPlugin("BigDoors") != null;

            if (BigDoors) {
                try {
                    BigDoorsManager.register(this);
                } catch (Throwable e) {
                    BigDoors = false;
                    e.printStackTrace();
                }
            }

            this.getConfigManager().copyOverTranslations();

            parseHelpEntries();

            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " Scanning for economy systems...");
                switch (this.getConfigManager().getEconomyType()) {
                case CMIEconomy:
                    this.loadCMIEconomy();
                    break;
                case Essentials:
                    this.loadEssentialsEconomy();
                    break;
                case None:
                    if (this.getPermissionManager().getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
                        ResidenceVaultAdapter vault = (ResidenceVaultAdapter) this.getPermissionManager().getPermissionsPlugin();
                        if (vault.economyOK()) {
                            economy = vault;
                            consoleMessage("Found Vault using economy system: &5" + vault.getEconomyName());
                        }
                    }
                    if (economy == null) {
                        this.loadVaultEconomy();
                    }
                    if (economy == null) {
                        this.loadCMIEconomy();
                    }
                    if (economy == null) {
                        this.loadEssentialsEconomy();
                    }
                    if (economy == null) {
                        this.loadRealEconomy();
                    }
                    if (economy == null) {
                        this.loadIConomy();
                    }
                    break;
                case RealEconomy:
                    this.loadRealEconomy();
                    break;
                case Vault:
                    if (this.getPermissionManager().getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
                        ResidenceVaultAdapter vault = (ResidenceVaultAdapter) this.getPermissionManager().getPermissionsPlugin();
                        if (vault.economyOK()) {
                            economy = vault;
                            consoleMessage("Found Vault using economy system: &5" + vault.getEconomyName());
                        }
                    }
                    if (economy == null) {
                        this.loadVaultEconomy();
                    }
                    break;
                case iConomy:
                    this.loadIConomy();
                    break;
                default:
                    break;
                }

                if (economy == null) {
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Unable to find an economy system...");
                    economy = new BlackHoleEconomy();
                }
            }

            // Only fill if we need to convert player data
            if (getConfigManager().isUUIDConvertion()) {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " Loading (" + Bukkit.getOfflinePlayers().length + ") player data");
                for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                    if (player == null)
                        continue;
                    String name = player.getName();
                    if (name == null)
                        continue;
                    this.addOfflinePlayerToChache(player);
                }
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " Player data loaded: " + OfflinePlayerList.size());
            } else {
                CMIScheduler.runTaskAsynchronously(() -> {
                    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                        if (player == null)
                            continue;
                        String name = player.getName();
                        if (name == null)
                            continue;
                        addOfflinePlayerToChache(player);
                    }
                });
            }

            rmanager = new ResidenceManager(this);

            leasemanager = new LeaseManager(this);

            tmanager = new TransactionManager(this);

            pmanager = new PermissionListManager(this);

            getLocaleManager().LoadLang(getConfigManager().getLanguage());
            getLM().LanguageReload();

            if (firstenable) {
                if (!this.isEnabled()) {
                    return;
                }

                File f = new File(getDataFolder(), "flags.yml");
                YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
                for (String oneFlag : conf.getStringList("Global.GroupedFlags." + padd.groupedFlag)) {
                    Flags flag = Flags.getFlag(oneFlag);
                    if (flag != null) {
                        flag.addGroup(padd.groupedFlag);
                    }
                    FlagPermissions.addFlagToFlagGroup(padd.groupedFlag, oneFlag);
                }

            }

            try {
                this.loadYml();
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "Unable to load save file", e);
                throw e;
            }

            signmanager = new SignUtil(this);
            getSignUtil().LoadSigns();

            if (getConfigManager().isUseResidenceFileClean())
                (new FileCleanUp(this)).cleanOldResidence();

            if (firstenable) {
                if (!this.isEnabled()) {
                    return;
                }
                FlagPermissions.initValidFlags();

                if (smanager == null)
                    setWorldEdit();
                setWorldGuard();

                setKingdoms();

                PluginManager pm = getServer().getPluginManager();

                blistener = new ResidenceBlockListener(this);
                plistener = new ResidencePlayerListener(this);
                if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
                    pm.registerEvents(new ResidencePlayerListener1_9(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_12_R1))
                    pm.registerEvents(new ResidencePlayerListener1_12(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
                    pm.registerEvents(new ResidencePlayerListener1_13(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_14_R1))
                    pm.registerEvents(new ResidencePlayerListener1_14(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_15_R1))
                    pm.registerEvents(new ResidencePlayerListener1_15(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_16_R1))
                    pm.registerEvents(new ResidencePlayerListener1_16(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_17_R1))
                    pm.registerEvents(new ResidencePlayerListener1_17(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_19_R1))
                    pm.registerEvents(new ResidencePlayerListener1_19(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_20_R1))
                    pm.registerEvents(new ResidencePlayerListener1_20(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_21_R1))
                    pm.registerEvents(new ResidencePlayerListener1_21(this), this);

                elistener = new ResidenceEntityListener(this);
                flistener = new ResidenceFixesListener();
                slistener = new ResidenceRaidListener();

                shlistener = new ShopListener(this);
                spigotlistener = new SpigotListener();

                pm.registerEvents(blistener, this);
                pm.registerEvents(plistener, this);
                pm.registerEvents(elistener, this);
                pm.registerEvents(flistener, this);
                pm.registerEvents(shlistener, this);
                pm.registerEvents(slistener, this);

                // 1.8 event
                if (Version.isCurrentEqualOrHigher(Version.v1_8_R1))
                    pm.registerEvents(new v1_8Events(), this);

                // 1.9 event
                if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
                    pm.registerEvents(new v1_9Events(), this);

                // 1.10 event
                if (Version.isCurrentEqualOrHigher(Version.v1_10_R1))
                    pm.registerEvents(new v1_10Events(), this);

                // 1.13 event
                if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
                    pm.registerEvents(new v1_13Events(this), this);

                firstenable = false;
            } else {
                plistener.reload();
            }

            AutoSelectionManager = new AutoSelection(this);

            try {
                Class.forName("org.bukkit.event.player.PlayerItemDamageEvent");
                getServer().getPluginManager().registerEvents(spigotlistener, this);
            } catch (Exception e) {
            }

            if (setupPlaceHolderAPI()) {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " PlaceholderAPI was found - Enabling capabilities.");
                PlaceholderAPIEnabled = true;
            }

            if (getServer().getPluginManager().getPlugin("CrackShot") != null)
                getServer().getPluginManager().registerEvents(new CrackShot(this), this);

            try {
                // DynMap
                Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
                if (dynmap != null && getConfigManager().DynMapUse) {
                    DynManager = new DynMapManager(this);
                    getServer().getPluginManager().registerEvents(new DynMapListeners(this), this);
                    getDynManager().api = (DynmapAPI) dynmap;
                    getDynManager().activate();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            try {
                // Pl3xMap
                Plugin pl3xmap = Bukkit.getPluginManager().getPlugin("Pl3xMap");
                if (pl3xmap != null && getConfigManager().Pl3xMapUse) {
                    Pl3xManager = new Pl3xMapManager(this);
                    getServer().getPluginManager().registerEvents(new Pl3xMapListeners(this), this);
                    getPl3xManager().api = Pl3xMap.api();
                    getPl3xManager().activate();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            int autosaveInt = getConfigManager().getAutoSaveInterval();
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = autosaveInt * 60 * 20;
            autosaveBukkitId = CMIScheduler.scheduleSyncRepeatingTask(autoSave, autosaveInt, autosaveInt);

            if (getConfigManager().getHealInterval() > 0)
                healBukkitId = CMIScheduler.scheduleSyncRepeatingTask(doHeals, 20, getConfigManager().getHealInterval() * 20);
            if (getConfigManager().getFeedInterval() > 0)
                feedBukkitId = CMIScheduler.scheduleSyncRepeatingTask(doFeed, 20, getConfigManager().getFeedInterval() * 20);
            if (getConfigManager().getSafeZoneInterval() > 0)
                effectRemoveBukkitId = CMIScheduler.scheduleSyncRepeatingTask(removeBadEffects, 20, getConfigManager().getSafeZoneInterval() * 20);

            if (getConfigManager().AutoMobRemoval())
                DespawnMobsBukkitId = CMIScheduler.scheduleSyncRepeatingTask(DespawnMobs, 20 * getConfigManager().AutoMobRemovalInterval(), 20
                    * getConfigManager().AutoMobRemovalInterval());

            if (getConfigManager().useLeases()) {
                int leaseInterval = getConfigManager().getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = leaseInterval * 60 * 20;
                leaseBukkitId = CMIScheduler.scheduleSyncRepeatingTask(leaseExpire, leaseInterval, leaseInterval);
            }
            if (getConfigManager().enabledRentSystem()) {
                int rentint = getConfigManager().getRentCheckInterval();
                if (rentint < 1) {
                    rentint = 1;
                }
                rentint = rentint * 60 * 20;
                rentBukkitId = CMIScheduler.scheduleSyncRepeatingTask(rentExpire, rentint, rentint);
            }
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (getPermissionManager().isResidenceAdmin(player)) {
                    turnResAdminOn(player);
                }
            }
            try {
                metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
            Bukkit.getConsoleSender().sendMessage(getPrefix() + " Enabled! Version " + this.getDescription().getVersion() + " by Zrips");
            initsuccess = true;

        } catch (Exception ex) {
            initsuccess = false;
            getServer().getPluginManager().disablePlugin(this);
            Bukkit.getConsoleSender().sendMessage(getPrefix() + " - FAILED INITIALIZATION! DISABLED! ERROR:");
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            Bukkit.getServer().shutdown();
        }

        getShopSignUtilManager().LoadShopVotes();
        getShopSignUtilManager().LoadSigns();
        getShopSignUtilManager().boardUpdate();

        CMIVersionChecker.VersionCheck(null, 11480, this.getDescription());
        fullyLoaded = true;
    }

    public void parseHelpEntries() {

        try {
            File langFile = new File(new File(dataFolder, "Language"), getConfigManager().getLanguage() + ".yml");

            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }

            if (langFile.isFile()) {
                FileConfiguration langconfig = new YamlConfiguration();
                langconfig.load(in);
                helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
            } else {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " Language file does not exist...");
            }
            if (in != null)
                in.close();
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(getPrefix() + " Failed to load language file: " + getConfigManager().getLanguage()
                + ".yml setting to default - English");

            File langFile = new File(new File(dataFolder, "Language"), "English.yml");

            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }

            try {
                if (langFile.isFile()) {
                    FileConfiguration langconfig = new YamlConfiguration();
                    langconfig.load(in);
                    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
                } else {
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Language file does not exist...");
                }
            } catch (Throwable e) {

            } finally {
                if (in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private boolean setupPlaceHolderAPI() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            return false;
        return new PlaceholderAPIHook(this).register();
    }

    public SignUtil getSignUtil() {
        return signmanager;
    }

    public void consoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(CMIChatColor.translate(getPrefix() + " " + message));
    }

    public boolean validName(String name) {
        if (name.contains(":") || name.contains(".") || name.contains("|")) {
            return false;
        }
        if (getConfigManager().getResidenceNameRegex() == null) {
            return true;
        }
        String namecheck = name.replaceAll(getConfigManager().getResidenceNameRegex(), "");
        return name.equals(namecheck);
    }

    private void setWorldEdit() {
        try {
            Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                this.wep = (com.sk89q.worldedit.bukkit.WorldEditPlugin) plugin;
                try {
                    Class.forName("com.sk89q.worldedit.bukkit.selections.Selection");

                    if (getConfigManager().isWorldEditIntegration())
                        smanager = new WorldEditSelectionManager(server, this);
                    if (wep != null)
                        SchematicManager = new SchematicsManager(this);
                } catch (ClassNotFoundException e) {
                    if (getConfigManager().isWorldEditIntegration())
                        smanager = new WorldEdit7SelectionManager(server, this);
                    if (wep != null)
                        SchematicManager = new Schematics7Manager(this);
                }
                if (smanager == null)
                    smanager = new SelectionManager(server, this);
                if (this.getWorldEdit().getConfig().isInt("wand-item"))
                    wepid = CMIMaterial.get(this.getWorldEdit().getConfig().getInt("wand-item"));
                else
                    wepid = CMIMaterial.get((String) this.getWorldEdit().getConfig().get("wand-item"));

                Bukkit.getConsoleSender().sendMessage(getPrefix() + " Found WorldEdit " + this.getWorldEdit().getDescription().getVersion());
            } else {
                smanager = new SelectionManager(server, this);
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    private GameManagement kingdomsmanager = null;

    private void setKingdoms() {
        if (Bukkit.getPluginManager().getPlugin("Kingdoms") != null) {
            try {
                kingdomsmanager = Kingdoms.getManagers();
            } catch (Throwable e) {
                this.consoleMessage("Failed to recognize Kingdoms plugin. Compatability disabled");
            }
        }
    }

    public GameManagement getKingdomsManager() {
        return kingdomsmanager;
    }

    private void setWorldGuard() {
        Plugin wgplugin = server.getPluginManager().getPlugin("WorldGuard");
        if (wgplugin != null) {
            wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) wgplugin;
            Bukkit.getConsoleSender().sendMessage(getPrefix() + " Found WorldGuard " + wg.getDescription().getVersion());
        }
    }

    public Residence getPlugin() {
        return this;
    }

//    public LWC getLwc() {
//	return lwc;
//    }

    public File getDataLocation() {
        return dataFolder;
    }

    public ShopSignUtil getShopSignUtilManager() {
        if (ShopSignUtilManager == null)
            ShopSignUtilManager = new ShopSignUtil(this);
        return ShopSignUtilManager;
    }

    public CommandFiller getCommandFiller() {
        if (cmdFiller == null) {
            cmdFiller = new CommandFiller();
            cmdFiller.fillCommands();
        }
        return cmdFiller;
    }

    public ResidenceManager getResidenceManager() {
        return rmanager;
    }

    public SelectionManager getSelectionManager() {
        if (smanager == null)
            setWorldEdit();
        return smanager;
    }

    public FlagUtil getFlagUtilManager() {
        if (FlagUtilManager == null)
            FlagUtilManager = new FlagUtil(this);
        return FlagUtilManager;
    }

    public PermissionManager getPermissionManager() {
        if (gmanager == null)
            gmanager = new PermissionManager(this);
        return gmanager;
    }

    public PermissionListManager getPermissionListManager() {
        return pmanager;
    }

    public DynMapManager getDynManager() {
        return DynManager;
    }

    public Pl3xMapManager getPl3xManager() {
        return Pl3xManager;
    }

    public WESchematicManager getSchematicManager() {
        return SchematicManager;
    }

    public AutoSelection getAutoSelectionManager() {
        return AutoSelectionManager;
    }

    public Sorting getSortingManager() {
        return SortingManager;
    }

    public RandomTp getRandomTpManager() {
        return RandomTpManager;
    }

    public EconomyInterface getEconomyManager() {
        return economy;
    }

    public Server getServ() {
        return server;
    }

    public LeaseManager getLeaseManager() {
        return leasemanager;
    }

    public PlayerManager getPlayerManager() {
        return PlayerManager;
    }

    public HelpEntry getHelpPages() {
        return helppages;
    }

    @Deprecated
    public void setConfigManager(ConfigManager cm) {
        configManager = cm;
    }

    public ConfigManager getConfigManager() {
        if (configManager == null)
            configManager = new ConfigManager(this);
        return configManager;
    }

    public TransactionManager getTransactionManager() {
        return tmanager;
    }

    public WorldItemManager getItemManager() {
        return imanager;
    }

    public WorldFlagManager getWorldFlags() {
        return wmanager;
    }

    public RentManager getRentManager() {
        return rentmanager;
    }

    public LocaleManager getLocaleManager() {
        return LocaleManager;
    }

    public Language getLM() {
        if (newLanguageManager == null) {
            newLanguageManager = new Language(this);
            newLanguageManager.LanguageReload();
        }
        return newLanguageManager;
    }

    public ResidencePlayerListener getPlayerListener() {
        return plistener;
    }

    public ResidenceBlockListener getBlockListener() {
        return blistener;
    }

    public ResidenceEntityListener getEntityListener() {
        return elistener;
    }

    public ChatManager getChatManager() {
        return chatmanager;
    }

    public String getResidenceVersion() {
        return ResidenceVersion;
    }

    public List<String> getAuthors() {
        return authlist;
    }

    public FlagPermissions getPermsByLoc(Location loc) {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if (res != null) {
            return res.getPermissions();
        }
        return wmanager.getPerms(loc.getWorld().getName());

    }

    public FlagPermissions getPermsByLocForPlayer(Location loc, Player player) {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if (res != null) {
            return res.getPermissions();
        }
        if (player != null)
            return wmanager.getPerms(player);

        return wmanager.getPerms(loc.getWorld().getName());
    }

    private void loadIConomy() {
        Plugin p = getServer().getPluginManager().getPlugin("iConomy");
        if (p != null) {
            if (p.getDescription().getVersion().startsWith("6")) {
                economy = new IConomy6Adapter((com.iCo6.iConomy) p);
            } else {
                consoleMessage("UNKNOWN iConomy version!");
                return;
            }
            consoleMessage("Successfully linked with &5iConomy");
            consoleMessage("Version: " + p.getDescription().getVersion());
        } else {
            consoleMessage("iConomy NOT found!");
        }
    }

    private void loadEssentialsEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Essentials");
        if (p != null) {
            economy = new EssentialsEcoAdapter((Essentials) p);
            consoleMessage("Successfully linked with &5Essentials Economy");
        } else {
            consoleMessage("Essentials Economy NOT found!");
        }
    }

    private void loadCMIEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("CMI");
        if (p != null) {
            economy = new CMIEconomy();
            consoleMessage("Successfully linked with &5CMIEconomy");
        } else {
            consoleMessage("CMIEconomy NOT found!");
        }
    }

    private void loadRealEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("RealPlugin");
        if (p != null) {
            economy = new RealShopEconomy(new RealEconomy((RealPlugin) p));
            consoleMessage("Successfully linked with &5RealShop Economy");
        } else {
            consoleMessage("RealShop Economy NOT found!");
        }
    }

    private void loadVaultEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if (p != null) {
            ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
            if (vault.economyOK()) {
                consoleMessage("Found Vault using economy: &5" + vault.getEconomyName());
                economy = vault;
            } else {
                consoleMessage("Found Vault, but Vault reported no usable economy system...");
            }
        } else {
            consoleMessage("Vault NOT found!");
        }
    }

    public boolean isResAdminOn(CommandSender sender) {
        if (sender instanceof Player)
            return isResAdminOn((Player) sender);
        return true;
    }

    public boolean isResAdminOn(Player player) {
        if (player == null)
            return true;
        return resadminToggle.contains(player.getName());
    }

    public void turnResAdminOn(Player player) {
        resadminToggle.add(player.getName());
    }

    public boolean isResAdminOn(String player) {
        return resadminToggle.contains(player);
    }

    private static void saveFile(File worldFolder, String fileName, String key, Object value) throws IOException {
        File ymlFlagsSaveLoc = new File(worldFolder, "res_" + fileName + ".yml");
        File tmpFlagsFile = new File(worldFolder, "tmp_res_" + fileName + ".yml");

        // Separate Flags file
        YMLSaveHelper flagsTemp = new YMLSaveHelper(tmpFlagsFile);
        flagsTemp.getRoot().put(key, value);
        flagsTemp.save();

        if (ymlFlagsSaveLoc.isFile()) {
            File backupFolder = new File(worldFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "res_" + fileName + ".yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlFlagsSaveLoc.renameTo(backupFile);
        }
        tmpFlagsFile.renameTo(ymlFlagsSaveLoc);
    }

    private static void saveBackup(File ymlSaveLoc, String worldName, File worldFolder) {
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(worldFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "res_" + worldName + ".yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
    }

    private void saveYml() throws IOException {
        File saveFolder = new File(dataFolder, "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        if (!worldFolder.isDirectory())
            worldFolder.mkdirs();
        YMLSaveHelper syml;
        Map<String, Object> save = rmanager.save();
        for (Entry<String, Object> entry : save.entrySet()) {

            boolean emptyRecord = false;
            // Not saving files without any records in them. Mainly for servers with many small temporary worlds
            try {
                emptyRecord = ((LinkedHashMap) entry.getValue()).isEmpty();
            } catch (Throwable e) {
            }

            File ymlSaveLoc = new File(worldFolder, "res_" + entry.getKey() + ".yml");

            if (emptyRecord) {
                saveBackup(ymlSaveLoc, entry.getKey(), worldFolder);
                continue;
            }

            File tmpFile = new File(worldFolder, "tmp_res_" + entry.getKey() + ".yml");

            syml = new YMLSaveHelper(tmpFile);
            if (this.getResidenceManager().getMessageCatch(entry.getKey()) != null)
                syml.getRoot().put("Messages", this.getResidenceManager().getMessageCatch(entry.getKey()));
            if (this.getResidenceManager().getFlagsCatch(entry.getKey()) != null)
                syml.getRoot().put("Flags", this.getResidenceManager().getFlagsCatch(entry.getKey()));

            syml.getRoot().put("Residences", entry.getValue());
            syml.save();

            saveBackup(ymlSaveLoc, entry.getKey(), worldFolder);

            tmpFile.renameTo(ymlSaveLoc);
        }

        YMLSaveHelper yml;
        // For Sale save
        File ymlSaveLoc = new File(saveFolder, "forsale.yml");
        File tmpFile = new File(saveFolder, "tmp_forsale.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.save();
        yml.getRoot().put("Economy", tmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "forsale.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // Leases save
        ymlSaveLoc = new File(saveFolder, "leases.yml");
        tmpFile = new File(saveFolder, "tmp_leases.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Leases", leasemanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "leases.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // permlist save
        ymlSaveLoc = new File(saveFolder, "permlists.yml");
        tmpFile = new File(saveFolder, "tmp_permlists.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("PermissionLists", pmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "permlists.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // rent save
        ymlSaveLoc = new File(saveFolder, "rent.yml");
        tmpFile = new File(saveFolder, "tmp_rent.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("RentSystem", rentmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "rent.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        if (getConfigManager().showIntervalMessages()) {
            System.out.println("[Residence] - Saved Residences...");
        }
    }

    public final static String saveFilePrefix = "res_";

    private void loadFlags(String worldName, YMLSaveHelper yml) {
        if (!yml.getRoot().containsKey("Flags"))
            return;

        HashMap<Integer, MinimizeFlags> c = getResidenceManager().getCacheFlags().get(worldName);
        if (c == null)
            c = new HashMap<Integer, MinimizeFlags>();
        Map<Integer, Object> ms = (Map<Integer, Object>) yml.getRoot().get("Flags");
        if (ms == null)
            return;

        for (Entry<Integer, Object> one : ms.entrySet()) {
            try {
                HashMap<String, Boolean> msgs = (HashMap<String, Boolean>) one.getValue();
                c.put(one.getKey(), new MinimizeFlags(one.getKey(), msgs));
            } catch (Exception e) {

            }
        }
        getResidenceManager().getCacheFlags().put(worldName, c);
    }

    private void loadMessages(String worldName, YMLSaveHelper yml) {
        if (!yml.getRoot().containsKey("Messages"))
            return;

        HashMap<Integer, MinimizeMessages> c = getResidenceManager().getCacheMessages().get(worldName);
        if (c == null)
            c = new HashMap<Integer, MinimizeMessages>();
        Map<Integer, Object> ms = (Map<Integer, Object>) yml.getRoot().get("Messages");
        if (ms == null)
            return;

        for (Entry<Integer, Object> one : ms.entrySet()) {
            try {
                Map<String, String> msgs = (Map<String, String>) one.getValue();
                c.put(one.getKey(), new MinimizeMessages(one.getKey(), msgs.get("EnterMessage"), msgs.get("LeaveMessage")));
            } catch (Exception e) {

            }
        }
        getResidenceManager().getCacheMessages().put(worldName, c);
    }

    private void loadMessagesAndFlags(String worldName, YMLSaveHelper yml, File worldFolder) {
        loadMessages(worldName, yml);
        loadFlags(worldName, yml);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean loadYml() throws Exception {
        File saveFolder = new File(dataFolder, "Save");
        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (!saveFolder.isDirectory()) {
                saveFolder.mkdir();
                if (!saveFolder.isDirectory()) {
                    this.getLogger().warning("Save directory does not exist...");
                    this.getLogger().warning("Please restart server");
                    return true;
                }
            }
            long time;
            YMLSaveHelper yml;
            File loadFile;
            HashMap<String, Object> worlds = new HashMap<>();

            for (String worldName : this.getResidenceManager().getWorldNames()) {
                loadFile = new File(worldFolder, saveFilePrefix + worldName + ".yml");
                if (!loadFile.isFile())
                    continue;

                time = System.currentTimeMillis();

                if (!isDisabledWorld(worldName) && !this.getConfigManager().CleanerStartupLog)
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Loading save data for world " + worldName + "...");

                yml = new YMLSaveHelper(loadFile);
                yml.load();
                if (yml.getRoot() == null)
                    continue;

                loadMessagesAndFlags(worldName, yml, worldFolder);

                worlds.put(worldName, yml.getRoot().get("Residences"));

                int pass = (int) (System.currentTimeMillis() - time);
                String PastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

                if (!isDisabledWorld(worldName) && !this.getConfigManager().CleanerStartupLog)
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Loaded " + worldName + " data. (" + PastTime + ")");
            }

            getResidenceManager().load(worlds);

            // Getting shop residences
            Map<String, ClaimedResidence> resList = rmanager.getResidences();
            for (Entry<String, ClaimedResidence> one : resList.entrySet()) {
                addShops(one.getValue());
            }

            if (getConfigManager().isUUIDConvertion()) {
                getConfigManager().ChangeConfig("Global.UUIDConvertion", false);
            }

            loadFile = new File(saveFolder, "forsale.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                tmanager = new TransactionManager(this);
                Map<String, Object> root = yml.getRoot();
                if (root != null)
                    tmanager.load((Map) root.get("Economy"));
            }
            loadFile = new File(saveFolder, "leases.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                Map<String, Object> root = yml.getRoot();
                if (root != null)
                    leasemanager = getLeaseManager().load((Map) root.get("Leases"));
            }
            loadFile = new File(saveFolder, "permlists.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                Map<String, Object> root = yml.getRoot();
                if (root != null)
                    pmanager = getPermissionListManager().load((Map) root.get("PermissionLists"));
            }
            loadFile = new File(saveFolder, "rent.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                Map<String, Object> root = yml.getRoot();
                if (root != null)
                    rentmanager.load((Map) root.get("RentSystem"));
            }

//	    for (Player one : Bukkit.getOnlinePlayers()) {
//		ResidencePlayer rplayer = getPlayerManager().getResidencePlayer(one);
//		if (rplayer != null)
//		    rplayer.recountRes();
//	    }

            // System.out.print("[Residence] Loaded...");
            return true;
        } catch (Exception ex) {
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void addShops(ClaimedResidence res) {
        ResidencePermissions perms = res.getPermissions();
        if (perms.has(Flags.shop, FlagCombo.OnlyTrue, false))
            rmanager.addShop(res);
        for (ClaimedResidence one : res.getSubzones()) {
            addShops(one);
        }
    }

    private void writeDefaultGroupsFromJar() {
        if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "groups.yml"), "groups.yml", true)) {
            System.out.println("[Residence] Wrote default groups...");
        }
    }

    private void writeDefaultFlagsFromJar() {
        if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "flags.yml"), "flags.yml", true)) {
            System.out.println("[Residence] Wrote default flags...");
        }
    }

    private void ConvertFile() {
        File file = new File(this.getDataFolder(), "config.yml");

        File file_old = new File(this.getDataFolder(), "config_old.yml");

        File newfile = new File(this.getDataFolder(), "groups.yml");

        File newTempFlags = new File(this.getDataFolder(), "flags.yml");

        try {
            copy(file, file_old);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            copy(file, newfile);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            copy(file, newTempFlags);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        File newGroups = new File(this.getDataFolder(), "config.yml");

        List<String> list = new ArrayList<String>();
        list.add("ResidenceVersion");
        list.add("Global.Flags");
        list.add("Global.FlagPermission");
        list.add("Global.ResidenceDefault");
        list.add("Global.CreatorDefault");
        list.add("Global.GroupDefault");
        list.add("Groups");
        list.add("GroupAssignments");
        list.add("ItemList");

        try {
            remove(newGroups, list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File newConfig = new File(this.getDataFolder(), "groups.yml");
        list.clear();
        list = new ArrayList<String>();
        list.add("ResidenceVersion");
        list.add("Global");
        list.add("ItemList");

        try {
            remove(newConfig, list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File newFlags = new File(this.getDataFolder(), "flags.yml");
        list.clear();
        list = new ArrayList<String>();
        list.add("ResidenceVersion");
        list.add("GroupAssignments");
        list.add("Groups");
        list.add("Global.Language");
        list.add("Global.SelectionToolId");
        list.add("Global.InfoToolId");
        list.add("Global.MoveCheckInterval");
        list.add("Global.SaveInterval");
        list.add("Global.DefaultGroup");
        list.add("Global.UseLeaseSystem");
        list.add("Global.LeaseCheckInterval");
        list.add("Global.LeaseAutoRenew");
        list.add("Global.EnablePermissions");
        list.add("Global.LegacyPermissions");
        list.add("Global.EnableEconomy");
        list.add("Global.EnableRentSystem");
        list.add("Global.RentCheckInterval");
        list.add("Global.ResidenceChatEnable");
        list.add("Global.UseActionBar");
        list.add("Global.ResidenceChatColor");
        list.add("Global.AdminOnlyCommands");
        list.add("Global.AdminOPs");
        list.add("Global.MultiWorldPlugin");
        list.add("Global.ResidenceFlagsInherit");
        list.add("Global.PreventRentModify");
        list.add("Global.StopOnSaveFault");
        list.add("Global.ResidenceNameRegex");
        list.add("Global.ShowIntervalMessages");
        list.add("Global.VersionCheck");
        list.add("Global.CustomContainers");
        list.add("Global.CustomBothClick");
        list.add("Global.CustomRightClick");

        try {
            remove(newFlags, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void remove(File newGroups, List<String> list) throws IOException {

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(newGroups);
        conf.options().copyDefaults(true);

        for (String one : list) {
            conf.set(one, null);
        }
        try {
            conf.save(newGroups);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copy(File source, File target) throws IOException {
        InputStream in = new FileInputStream(source);
        OutputStream out = new FileOutputStream(target);
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            in.close();
            out.close();
        }
    }

//    private void writeDefaultLanguageFile(String lang) {
//	File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
//	outFile.getParentFile().mkdirs();
//	if (this.writeDefaultFileFromJar(outFile, "languagefiles/" + lang + ".yml", true)) {
//	    System.out.println("[Residence] Wrote default " + lang + " Language file...");
//	}
//    }
//
//    private boolean checkNewLanguageVersion(String lang) throws IOException, FileNotFoundException, InvalidConfigurationException {
//	File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
//	File checkFile = new File(new File(this.getDataFolder(), "Language"), "temp-" + lang + ".yml");
//	if (outFile.isFile()) {
//	    FileConfiguration testconfig = new YamlConfiguration();
//	    testconfig.load(outFile);
//	    int oldversion = testconfig.getInt("FieldsVersion", 0);
//	    if (!this.writeDefaultFileFromJar(checkFile, "languagefiles/" + lang + ".yml", false)) {
//		return false;
//	    }
//	    FileConfiguration testconfig2 = new YamlConfiguration();
//	    testconfig2.load(checkFile);
//	    int newversion = testconfig2.getInt("FieldsVersion", oldversion);
//	    if (checkFile.isFile()) {
//		checkFile.delete();
//	    }
//	    if (newversion > oldversion) {
//		return true;
//	    }
//	    return false;
//	}
//	return true;
//    }

    private boolean writeDefaultFileFromJar(File writeName, String jarPath, boolean backupOld) {
        try {
            File fileBackup = new File(this.getDataFolder(), "backup-" + writeName);
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if (jarloc.isFile()) {
                JarFile jar = new JarFile(jarloc);
                try {
                    JarEntry entry = jar.getJarEntry(jarPath);
                    if (entry != null && !entry.isDirectory()) {
                        InputStream in = jar.getInputStream(entry);
                        InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
                        if (writeName.isFile()) {
                            if (backupOld) {
                                if (fileBackup.isFile()) {
                                    fileBackup.delete();
                                }
                                writeName.renameTo(fileBackup);
                            } else {
                                writeName.delete();
                            }
                        }
                        FileOutputStream out = new FileOutputStream(writeName);
                        OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                        try {
                            char[] tempbytes = new char[512];
                            int readbytes = isr.read(tempbytes, 0, 512);
                            while (readbytes > -1) {
                                osw.write(tempbytes, 0, readbytes);
                                readbytes = isr.read(tempbytes, 0, 512);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            osw.close();
                            isr.close();
                            out.close();
                        }
                        return true;
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                } finally {
                    jar.close();
                }
            }
            return false;
        } catch (Exception ex) {
            System.out.println("[Residence] Failed to write file: " + writeName);
            return false;
        }
    }

    public boolean isPlayerExist(CommandSender sender, String name, boolean inform) {
        if (getPlayerUUID(name) != null)
            return true;
        if (inform)
            sender.sendMessage(msg(lm.Invalid_Player));
        @SuppressWarnings("unused")
        String a = "%%__USER__%%";
        @SuppressWarnings("unused")
        String b = "%%__RESOURCE__%%";
        @SuppressWarnings("unused")
        String c = "%%__NONCE__%%";
        return false;

    }

    public UUID getPlayerUUID(String playername) {
//	if (Residence.getConfigManager().isOfflineMode())
//	    return null;
        Player p = getServ().getPlayer(playername);
        if (p == null) {
            OfflinePlayer po = OfflinePlayerList.get(playername.toLowerCase());
            if (po != null)
                return po.getUniqueId();
        } else
            return p.getUniqueId();
        return null;
    }

    public OfflinePlayer getOfflinePlayer(String Name) {
        if (Name == null)
            return null;
        OfflinePlayer offPlayer = OfflinePlayerList.get(Name.toLowerCase());
        if (offPlayer != null)
            return offPlayer;

        Player player = Bukkit.getPlayer(Name);
        if (player != null)
            return player;

//	offPlayer = Bukkit.getOfflinePlayer(Name);
//	if (offPlayer != null)
//	    addOfflinePlayerToChache(offPlayer);
        return offPlayer;
    }

    public String getPlayerUUIDString(String playername) {
        UUID playerUUID = getPlayerUUID(playername);
        if (playerUUID != null)
            return playerUUID.toString();
        return null;
    }

    public OfflinePlayer getOfflinePlayer(UUID uuid) {
        OfflinePlayer offPlayer = cachedPlayerNameUUIDs.get(uuid);
        if (offPlayer != null)
            return offPlayer;

        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            return player;

//	offPlayer = Bukkit.getOfflinePlayer(uuid);
//	if (offPlayer != null)
//	    addOfflinePlayerToChache(offPlayer);
        return offPlayer;
    }

    public void addOfflinePlayerToChache(OfflinePlayer player) {
        if (player == null)
            return;
        if (player.getName() != null) {
            OfflinePlayerList.put(player.getName().toLowerCase(), player);
            cachedPlayerNames.put(player.getUniqueId(), player.getName());
        }
        cachedPlayerNameUUIDs.put(player.getUniqueId(), player);
    }

    public String getPlayerName(String uuid) {
        try {
            return getPlayerName(UUID.fromString(uuid));
        } catch (IllegalArgumentException ex) {
        }
        return null;
    }

    @Deprecated
    public String getServerLandname() {
        return getServerLandName();
    }

    public String getServerLandName() {
        return this.getLM().getMessage(lm.server_land);
    }

    @Deprecated
    public String getServerLandUUID() {
        return ServerLandUUID.toString();
    }

    @Deprecated
    public String getTempUserUUID() {
        return TempUserUUID.toString();
    }

    public UUID getServerUUID() {
        return ServerLandUUID;
    }

    public UUID getEmptyUserUUID() {
        return TempUserUUID;
    }

    public String getPlayerName(UUID uuid) {
        String cache = cachedPlayerNames.get(uuid);
        if (cache != null) {
            return cache.equalsIgnoreCase("_UNKNOWN_") ? null : cache;
        }

        if (uuid == null)
            return null;
        OfflinePlayer p = getServ().getPlayer(uuid);
        if (p == null)
            p = getOfflinePlayer(uuid);
        if (p != null) {
            cachedPlayerNames.put(uuid, p.getName());
            return p.getName();
        }

        // Last attempt, slowest one
        p = getServ().getOfflinePlayer(uuid);

        if (p != null) {
            String name = p.getName() == null ? "_UNKNOWN_" : p.getName();
            cachedPlayerNames.put(uuid, name);
            return p.getName();
        }

        return null;
    }

    public boolean isDisabledWorld(World world) {
        return isDisabledWorld(world.getName());
    }

    public boolean isDisabledWorld(String worldname) {
        return getConfigManager().DisabledWorldsList.contains(worldname);
    }

    public boolean isDisabledWorldListener(World world) {
        return isDisabledWorldListener(world.getName());
    }

    public boolean isDisabledWorldListener(String worldname) {
        return getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableListeners;
    }

    public boolean isDisabledWorldCommand(World world) {
        return isDisabledWorldCommand(world.getName());
    }

    public boolean isDisabledWorldCommand(String worldname) {
        return getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableCommands;
    }

    public String msg(String path) {
        return getLM().getMessage(path);
    }

    public void msg(CommandSender sender, String text) {
        if (sender != null && text.length() > 0)
            sender.sendMessage(CMIChatColor.translate(text));
    }

    public void msg(Player player, String text) {
        if (player != null && !text.isEmpty())
            player.sendMessage(CMIChatColor.translate(text));
    }

    public void msg(CommandSender sender, lm lm, Object... variables) {

        if (sender == null)
            return;

        if (getLM().containsKey(lm.getPath())) {
            String msg = getLM().getMessage(lm, variables);
            if (msg.length() > 0)
                sender.sendMessage(msg);
        } else {
            String msg = lm.getPath();
            if (msg.length() > 0)
                sender.sendMessage(lm.getPath());
        }
    }

    public List<String> msgL(lm lm) {
        return getLM().getMessageList(lm);
    }

    public String msg(lm lm, Object... variables) {
        return getLM().getMessage(lm, variables);
    }

    public InformationPager getInfoPageManager() {
        return InformationPagerManager;
    }

    public com.sk89q.worldedit.bukkit.WorldEditPlugin getWorldEdit() {
        return wep;
    }

    public com.sk89q.worldguard.bukkit.WorldGuardPlugin getWorldGuard() {
        return wg;
    }

    public CMIMaterial getWorldEditTool() {
        if (wepid == null)
            wepid = CMIMaterial.NONE;
        return wepid;
    }

    public WorldGuardInterface getWorldGuardUtil() {
        if (worldGuardUtil == null) {

            int version = 6;
            try {
                version = Integer.parseInt(wg.getDescription().getVersion().substring(0, 1));
            } catch (Exception | Error e) {
            }
            if (version >= 7) {
                wepVersion = version;
                worldGuardUtil = new WorldGuard7Util(this);
            } else {
                worldGuardUtil = new WorldGuardUtil(this);
            }
        }
        return worldGuardUtil;
    }

    public KingdomsUtil getKingdomsUtil() {
        if (kingdomsUtil == null)
            kingdomsUtil = new KingdomsUtil(this);
        return kingdomsUtil;
    }

    public static Residence getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }

    public String[] reduceArgs(String[] args) {
        if (args.length <= 1)
            return new String[0];
        return Arrays.copyOfRange(args, 1, args.length);
    }

    public int getWorldGuardVersion() {
        return wepVersion;
    }

    public boolean isSlimefunPresent() {
        return SlimeFun;
    }

    public boolean isLwcPresent() {
        return lwc;
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }
}
