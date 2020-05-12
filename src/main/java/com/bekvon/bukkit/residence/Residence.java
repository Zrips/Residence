package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.cmiLib.ActionBarTitleMessages;
import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.VersionChecker;
import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.BossBar.BossBarManager;
import com.bekvon.bukkit.residence.Placeholders.Placeholder;
import com.bekvon.bukkit.residence.Placeholders.PlaceholderAPIHook;
import com.bekvon.bukkit.residence.allNms.CommonNMS;
import com.bekvon.bukkit.residence.allNms.v1_13Events;
import com.bekvon.bukkit.residence.api.*;
import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.containers.*;
import com.bekvon.bukkit.residence.dynmap.DynMapListeners;
import com.bekvon.bukkit.residence.dynmap.DynMapManager;
import com.bekvon.bukkit.residence.economy.*;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.gui.FlagUtil;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.*;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.*;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.raid.ResidenceRaidListener;
import com.bekvon.bukkit.residence.selection.*;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopSignUtil;
import com.bekvon.bukkit.residence.signsStuff.SignUtil;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.*;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.residence.mcstats.Metrics;
import com.residence.zip.ZipLibrary;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.bekvon.bukkit.residence.towns.TownManager;

/**
 * @author Gary Smoak - bekvon
 */
public class Residence extends JavaPlugin {

    private Logger log;

    private static Residence instance;
    public PermissionManager gmanager;
    public WorldItemManager imanager;
    public WorldFlagManager wmanager;
    public HelpEntry helppages;
    public File dataFolder;
    public Map<String, String> deleteConfirm;
    public Map<String, String> UnrentConfirm = new HashMap<String, String>();
    public List<String> resadminToggle;
    public HashMap<String, Long> rtMap = new HashMap<String, Long>();
    public List<String> teleportDelayMap = new ArrayList<String>();
    public HashMap<String, ClaimedResidence> teleportMap = new HashMap<String, ClaimedResidence>();
    protected String ResidenceVersion;
    protected List<String> authlist;
    protected ResidenceManager rmanager;
    protected SelectionManager smanager;
    protected ConfigManager cmanager;
    protected boolean spigotPlatform = false;
    protected SignUtil signmanager;
    protected ResidenceBlockListener blistener;
    protected ResidencePlayerListener plistener;
    protected ResidenceEntityListener elistener;
    protected ResidenceFixesListener flistener;
    protected ResidenceRaidListener slistener;
    protected ResidenceCommandListener cManager;
    protected BossBarManager BossBarManager;
    protected SpigotListener spigotlistener;
    protected ShopListener shlistener;
    protected TransactionManager tmanager;
    protected PermissionListManager pmanager;
    protected LeaseManager leasemanager;
    protected RentManager rentmanager;
    protected ChatManager chatmanager;
    protected Server server;
    protected LocaleManager LocaleManager;
    protected Language newLanguageManager;
    protected PlayerManager PlayerManager;
    protected FlagUtil FlagUtilManager;
    protected ShopSignUtil ShopSignUtilManager;
    //    private TownManager townManager;
    protected RandomTp RandomTpManager;
    protected DynMapManager DynManager;
    protected Sorting SortingManager;
    protected ActionBarTitleMessages ABManager;
    protected AutoSelection AutoSelectionManager;
    protected WESchematicManager SchematicManager;
    protected CommandFiller cmdFiller;
    protected ZipLibrary zip;
    protected boolean firstenable = true;
    protected EconomyInterface economy;
    protected int leaseBukkitId = -1;
    protected int rentBukkitId = -1;
    protected int healBukkitId = -1;
    protected int feedBukkitId = -1;
    protected int DespawnMobsBukkitId = -1;
    protected int autosaveBukkitId = -1;
    protected VersionChecker versionChecker;
    protected boolean initsuccess = false;
    private InformationPager InformationPagerManager;
    private WorldGuardInterface worldGuardUtil;
    private int wepVersion = 6;
    private int saveVersion = 1;
    private ConcurrentHashMap<String, OfflinePlayer> OfflinePlayerList = new ConcurrentHashMap<String, OfflinePlayer>();
    private Map<UUID, OfflinePlayer> cachedPlayerNameUUIDs = new HashMap<UUID, OfflinePlayer>();
    private com.sk89q.worldedit.bukkit.WorldEditPlugin wep = null;
    private com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = null;
    private CMIMaterial wepid;
    //    private String ServerLandname = "Server_Land";
    private String ServerLandUUID = "00000000-0000-0000-0000-000000000000";
    private String TempUserUUID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private NMS nms;
    private Placeholder Placeholder;
    private boolean PlaceholderAPIEnabled = false;

    private String prefix = ChatColor.GREEN + "[" + ChatColor.GOLD + "Residence" + ChatColor.GREEN + "]" + ChatColor.GRAY;
    // API
    private ResidenceApi API = new ResidenceApi();
    private MarketBuyInterface MarketBuyAPI = null;
    private MarketRentInterface MarketRentAPI = null;
    private ResidencePlayerInterface PlayerAPI = null;
    private ResidenceInterface ResidenceAPI = null;
    private ChatInterface ChatAPI = null;
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
                    Bukkit.getScheduler().runTaskAsynchronously(Residence.this, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                saveYml();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, getPrefix() + " SEVERE SAVE ERROR", ex);
            }
        }
    };

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
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static Residence getInstance() {
        return instance;
    }

    public boolean isSpigot() {
        return spigotPlatform;
    }

    public HashMap<String, ClaimedResidence> getTeleportMap() {
        return teleportMap;
    }

    public List<String> getTeleportDelayMap() {
        return teleportDelayMap;
    }

    public HashMap<String, Long> getRandomTeleportMap() {
        return rtMap;
    }
    // API end

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

    public BossBarManager getBossBarManager() {
        if (BossBarManager == null)
            BossBarManager = new BossBarManager(this);
        return BossBarManager;
    }

    public ChatInterface getResidenceChatAPI() {
        if (ChatAPI == null)
            ChatAPI = chatmanager;
        return ChatAPI;
    }

    public ResidenceCommandListener getCommandManager() {
        if (cManager == null)
            cManager = new ResidenceCommandListener(this);
        return cManager;
    }

    public ResidenceApi getAPI() {
        return API;
    }

    public NMS getNms() {
        return nms;
    }

    public void reloadPlugin() {
        this.onDisable();
        this.reloadConfig();
        this.onEnable();
    }

    @Override
    public void onDisable() {
        server.getScheduler().cancelTask(autosaveBukkitId);
        server.getScheduler().cancelTask(healBukkitId);
        server.getScheduler().cancelTask(feedBukkitId);

        server.getScheduler().cancelTask(DespawnMobsBukkitId);

        this.getPermissionManager().stopCacheClearScheduler();

        if (getConfigManager().useLeases()) {
            server.getScheduler().cancelTask(leaseBukkitId);
        }
        if (getConfigManager().enabledRentSystem()) {
            server.getScheduler().cancelTask(rentBukkitId);
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

//	    File file = new File(this.getDataFolder(), "uuids.yml");
//	    YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
//	    if (!conf.isConfigurationSection("UUIDS"))
//		conf.createSection("UUIDS");
//	    for (Entry<UUID, String> one : getCachedPlayerNameUUIDs().entrySet()) {
//		conf.set("UUIDS." + one.getKey().toString(), one.getValue());
//	    }
//	    try {
//		conf.save(file);
//	    } catch (IOException e) {
//		e.printStackTrace();
//	    }

            Bukkit.getConsoleSender().sendMessage(getPrefix() + " Disabled!");
        }
    }

    @Override
    public void onEnable() {
        try {
            log = getLogger();
            log.setLevel(Level.ALL);
            instance = this;

            initsuccess = false;
            versionChecker = new VersionChecker(this);
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
                if (plugin != null) {
                    if (!plugin.isEnabled()) {
                        Bukkit.getConsoleSender().sendMessage(getPrefix() + " - Enabling multiworld plugin: " + multiworld);
                        server.getPluginManager().enablePlugin(plugin);
                    }
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

            nms = new CommonNMS();

            gmanager = new PermissionManager(this);
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

            this.getConfigManager().copyOverTranslations();

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

                if (langFile.isFile()) {
                    FileConfiguration langconfig = new YamlConfiguration();
                    langconfig.load(in);
                    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
                } else {
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Language file does not exist...");
                }
                if (in != null)
                    in.close();
            }

            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " Scanning for economy systems...");
                switch (this.getConfigManager().getEconomyType()) {
                    case None:
                        if (gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
                            ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
                            if (vault.economyOK()) {
                                economy = vault;
                                consoleMessage("Found Vault using economy system: &5" + vault.getEconomyName());
                            }
                        }
                        if (economy == null) {
                            this.loadVaultEconomy();
                        }
                        break;

                    case Vault:
                        if (gmanager.getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
                            ResidenceVaultAdapter vault = (ResidenceVaultAdapter) gmanager.getPermissionsPlugin();
                            if (vault.economyOK()) {
                                economy = vault;
                                consoleMessage("Found Vault using economy system: &5" + vault.getEconomyName());
                            }
                        }
                        if (economy == null) {
                            this.loadVaultEconomy();
                        }
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
                Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                            if (player == null)
                                continue;
                            String name = player.getName();
                            if (name == null)
                                continue;
                            addOfflinePlayerToChache(player);
                        }
                    }
                });
            }

            rmanager = new ResidenceManager(this);

            leasemanager = new LeaseManager(this);

            tmanager = new TransactionManager(this);

            pmanager = new PermissionListManager(this);

            getLocaleManager().LoadLang(getConfigManager().getLanguage());
            getLM().LanguageReload();

            try {
                this.loadYml();
            } catch (Exception e) {
                this.getLogger().log(Level.SEVERE, "Unable to load save file", e);
                throw e;
            }

            signmanager = new SignUtil(this);
            getSignUtil().LoadSigns();

            if (getConfigManager().isUseResidenceFileClean())
                (new FileCleanUp(this)).cleanFiles();

            if (firstenable) {
                if (!this.isEnabled()) {
                    return;
                }
                FlagPermissions.initValidFlags();

                if (smanager == null)
                    setWorldEdit();
                setWorldGuard();

                blistener = new ResidenceBlockListener(this);
                plistener = new ResidencePlayerListener(this);
                elistener = new ResidenceEntityListener(this);
                flistener = new ResidenceFixesListener();
                slistener = new ResidenceRaidListener();

                shlistener = new ShopListener(this);
                spigotlistener = new SpigotListener();

                PluginManager pm = getServer().getPluginManager();
                pm.registerEvents(blistener, this);
                pm.registerEvents(plistener, this);
                pm.registerEvents(elistener, this);
                pm.registerEvents(flistener, this);
                pm.registerEvents(shlistener, this);
                pm.registerEvents(slistener, this);

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

            // TODO completely remove crackshot if not going to be fixed
//            if (getServer().getPluginManager().getPlugin("CrackShot") != null)
//                getServer().getPluginManager().registerEvents(new CrackShot(this), this);

            // DynMap
            Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
            if (dynmap != null && getConfigManager().DynMapUse) {
                DynManager = new DynMapManager(this);
                getServer().getPluginManager().registerEvents(new DynMapListeners(this), this);
                getDynManager().api = (DynmapAPI) dynmap;
                getDynManager().activate();
            }

            int autosaveInt = getConfigManager().getAutoSaveInterval();
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = autosaveInt * 60 * 20;
            autosaveBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, autoSave, autosaveInt, autosaveInt);
            healBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doHeals, 20, getConfigManager().getHealInterval() * 20);
            feedBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, doFeed, 20, getConfigManager().getFeedInterval() * 20);
            if (getConfigManager().AutoMobRemoval())
                DespawnMobsBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, DespawnMobs, 20 * getConfigManager().AutoMobRemovalInterval(), 20
                        * getConfigManager().AutoMobRemovalInterval());

            if (getConfigManager().useLeases()) {
                int leaseInterval = getConfigManager().getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = leaseInterval * 60 * 20;
                leaseBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, leaseExpire, leaseInterval, leaseInterval);
            }
            if (getConfigManager().enabledRentSystem()) {
                int rentint = getConfigManager().getRentCheckInterval();
                if (rentint < 1) {
                    rentint = 1;
                }
                rentint = rentint * 60 * 20;
                rentBukkitId = server.getScheduler().scheduleSyncRepeatingTask(this, rentExpire, rentint, rentint);
            }
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (getPermissionManager().isResidenceAdmin(player)) {
                    turnResAdminOn(player);
                }
            }
            try {
                Metrics metrics = new Metrics(this);
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
        getShopSignUtilManager().BoardUpdate();
        getVersionChecker().VersionCheck(null);
    }

    private boolean setupPlaceHolderAPI() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            return false;
        if ((new PlaceholderAPIHook(this)).register())
            Bukkit.getConsoleSender().sendMessage(this.getPrefix() + " PlaceholderAPI hooked.");
        return true;
    }

    public SignUtil getSignUtil() {
        return signmanager;
    }

    public void consoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', getPrefix() + " " + message));
    }

    public boolean validName(String name) {
        if (name.contains(":") || name.contains(".") || name.contains("|")) {
            return false;
        }
        if (getConfigManager().getResidenceNameRegex() == null) {
            return true;
        }
        String namecheck = name.replaceAll(getConfigManager().getResidenceNameRegex(), "");
        if (!name.equals(namecheck)) {
            return false;
        }
        return true;
    }

    private void setWorldEdit() {
        try {
            Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                this.wep = (com.sk89q.worldedit.bukkit.WorldEditPlugin) plugin;
                try {
                    Class.forName("com.sk89q.worldedit.bukkit.selections.Selection");
                    smanager = new WorldEditSelectionManager(server, this);
//                    if (wep != null)
//                        SchematicManager = new SchematicsManager(this);
                } catch (ClassNotFoundException e) {
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
                Bukkit.getConsoleSender().sendMessage(getPrefix() + " WorldEdit NOT found!");
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
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

    public VersionChecker getVersionChecker() {
        return versionChecker;
    }

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
        return gmanager;
    }

    public PermissionListManager getPermissionListManager() {
        return pmanager;
    }

    public DynMapManager getDynManager() {
        return DynManager;
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

    public ConfigManager getConfigManager() {
        if (cmanager == null)
            cmanager = new ConfigManager(this);
        return cmanager;
    }

    @Deprecated
    public void setConfigManager(ConfigManager cm) {
        cmanager = cm;
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
        if (resadminToggle.contains(player.getName())) {
            return true;
        }
        return false;
    }

    public void turnResAdminOn(Player player) {
        resadminToggle.add(player.getName());
    }

    public boolean isResAdminOn(String player) {
        if (resadminToggle.contains(player))
            return true;
        return false;
    }

    private void saveYml() throws IOException {
        File saveFolder = new File(dataFolder, "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        worldFolder.mkdirs();
        YMLSaveHelper yml;
        Map<String, Object> save = rmanager.save();
        for (Entry<String, Object> entry : save.entrySet()) {
            File ymlSaveLoc = new File(worldFolder, "res_" + entry.getKey() + ".yml");
            File tmpFile = new File(worldFolder, "tmp_res_" + entry.getKey() + ".yml");
            yml = new YMLSaveHelper(tmpFile);
            yml.getRoot().put("Version", saveVersion);
            World world = server.getWorld(entry.getKey());
            if (world != null)
                yml.getRoot().put("Seed", world.getSeed());
            if (this.getResidenceManager().getMessageCatch(entry.getKey()) != null)
                yml.getRoot().put("Messages", this.getResidenceManager().getMessageCatch(entry.getKey()));
            if (this.getResidenceManager().getFlagsCatch(entry.getKey()) != null)
                yml.getRoot().put("Flags", this.getResidenceManager().getFlagsCatch(entry.getKey()));
            yml.getRoot().put("Residences", entry.getValue());
            yml.save();
            if (ymlSaveLoc.isFile()) {
                File backupFolder = new File(worldFolder, "Backup");
                backupFolder.mkdirs();
                File backupFile = new File(backupFolder, "res_" + entry.getKey() + ".yml");
                if (backupFile.isFile()) {
                    backupFile.delete();
                }
                ymlSaveLoc.renameTo(backupFile);
            }
            tmpFile.renameTo(ymlSaveLoc);
        }

        // For Sale save
        File ymlSaveLoc = new File(saveFolder, "forsale.yml");
        File tmpFile = new File(saveFolder, "tmp_forsale.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.save();
        yml.getRoot().put("Version", saveVersion);
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
        yml.getRoot().put("Version", saveVersion);
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
        yml.getRoot().put("Version", saveVersion);
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
        yml.getRoot().put("Version", saveVersion);
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


    protected boolean loadYml() throws Exception {
        File saveFolder = new File(dataFolder, "Save");
        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (!saveFolder.isDirectory()) {
                this.getLogger().warning("Save directory does not exist...");
                this.getLogger().warning("Please restart server");
                return true;
            }
            long time;
            YMLSaveHelper yml;
            File loadFile;
            HashMap<String, Object> worlds = new HashMap<>();
            for (World world : getServ().getWorlds()) {
                loadFile = new File(worldFolder, "res_" + world.getName() + ".yml");
                if (loadFile.isFile()) {
                    time = System.currentTimeMillis();
                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Loading save data for world " + world.getName() + "...");

                    yml = new YMLSaveHelper(loadFile);
                    yml.load();

                    if (yml.getRoot().containsKey("Messages")) {
                        HashMap<Integer, MinimizeMessages> c = getResidenceManager().getCacheMessages().get(world.getName());
                        if (c == null)
                            c = new HashMap<Integer, MinimizeMessages>();
                        Map<Integer, Object> ms = (Map<Integer, Object>) yml.getRoot().get("Messages");
                        if (ms != null) {
                            for (Entry<Integer, Object> one : ms.entrySet()) {
                                try {
                                    Map<String, String> msgs = (Map<String, String>) one.getValue();
                                    c.put(one.getKey(), new MinimizeMessages(one.getKey(), msgs.get("EnterMessage"), msgs.get("LeaveMessage")));
                                } catch (Exception e) {

                                }
                            }
                            getResidenceManager().getCacheMessages().put(world.getName(), c);
                        }
                    }

                    if (yml.getRoot().containsKey("Flags")) {
                        HashMap<Integer, MinimizeFlags> c = getResidenceManager().getCacheFlags().get(world.getName());
                        if (c == null)
                            c = new HashMap<Integer, MinimizeFlags>();
                        Map<Integer, Object> ms = (Map<Integer, Object>) yml.getRoot().get("Flags");
                        if (ms != null) {
                            for (Entry<Integer, Object> one : ms.entrySet()) {
                                try {
                                    HashMap<String, Boolean> msgs = (HashMap<String, Boolean>) one.getValue();
                                    c.put(one.getKey(), new MinimizeFlags(one.getKey(), msgs));
                                } catch (Exception e) {

                                }
                            }
                            getResidenceManager().getCacheFlags().put(world.getName(), c);
                        }
                    }

                    worlds.put(world.getName(), yml.getRoot().get("Residences"));

                    int pass = (int) (System.currentTimeMillis() - time);
                    String PastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

                    Bukkit.getConsoleSender().sendMessage(getPrefix() + " Loaded " + world.getName() + " data. (" + PastTime + ")");
                }
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
                tmanager.load((Map) yml.getRoot().get("Economy"));
            }
            loadFile = new File(saveFolder, "leases.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                leasemanager = getLeaseManager().load((Map) yml.getRoot().get("Leases"));
            }
            loadFile = new File(saveFolder, "permlists.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                pmanager = getPermissionListManager().load((Map) yml.getRoot().get("PermissionLists"));
            }
            loadFile = new File(saveFolder, "rent.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
//		rentmanager = new RentManager();
                rentmanager.load((Map) yml.getRoot().get("RentSystem"));
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

    private void writeDefaultConfigFromJar() {
        if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "config.yml"), "config.yml", true)) {
            System.out.println("[Residence] Wrote default config...");
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

    private boolean writeDefaultFileFromJar(File writeName, String jarPath, boolean backupOld) {
        try {
            File fileBackup = new File(this.getDataFolder(), "backup-" + writeName);
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if (jarloc.isFile()) {
                JarFile jar = new JarFile(jarloc);
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
                    char[] tempbytes = new char[512];
                    int readbytes = isr.read(tempbytes, 0, 512);
                    while (readbytes > -1) {
                        osw.write(tempbytes, 0, readbytes);
                        readbytes = isr.read(tempbytes, 0, 512);
                    }
                    osw.close();
                    isr.close();
                    return true;
                }
                jar.close();
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

        String a = "%%__USER__%%";
        String b = "%%__RESOURCE__%%";
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
        if (player.getName() != null)
            OfflinePlayerList.put(player.getName().toLowerCase(), player);
        if (player.getUniqueId() != null)
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
        return this.getLM().getMessage(lm.server_land);
    }

    public String getServerLandName() {
        return this.getLM().getMessage(lm.server_land);
    }

    public String getServerLandUUID() {
        return ServerLandUUID;
    }

    public String getTempUserUUID() {
        return TempUserUUID;
    }

    public String getPlayerName(UUID uuid) {
        OfflinePlayer p = getServ().getPlayer(uuid);
        if (p == null)
            p = getServ().getOfflinePlayer(uuid);
        if (p != null)
            return p.getName();
        return null;
    }

    public boolean isDisabledWorldListener(World world) {
        return isDisabledWorldListener(world.getName());
    }

    public boolean isDisabledWorldListener(String worldname) {
        if (getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableListeners)
            return true;
        return false;
    }

    public boolean isDisabledWorldCommand(World world) {
        return isDisabledWorldCommand(world.getName());
    }

//    public static void msg(Player player, String path, Object... variables) {
//	if (player != null)
//	    if (Residence.getLM().containsKey(path))
//		player.sendMessage(Residence.getLM().getMessage(path, variables));
//	    else
//		player.sendMessage(path);
//    }

    public boolean isDisabledWorldCommand(String worldname) {
        if (getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableCommands)
            return true;
        return false;
    }

    public String msg(String path) {
        return getLM().getMessage(path);
    }

    public void msg(CommandSender sender, String text) {
        if (sender != null && text.length() > 0)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

//    private boolean isWorldOk(CommandSender sender) {
//	if (!this.getConfigManager().DisableNoFlagMessageUse)
//	    return true;
//
//	if (sender.hasPermission("residence.checkbadflags"))
//	    return true;
//	
//	if (!(sender instanceof Player))
//	    return true;
//
//	Player player = (Player) sender;
//	String world = player.getWorld().getName();
//	
//	for (String one : this.getConfigManager().DisableNoFlagMessageWorlds) {
//	    if (one.equalsIgnoreCase(world))
//		return false;
//	}
//	return true;
//    }

    public void msg(Player player, String text) {
        if (player != null && !text.isEmpty())
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', text));
    }

    public void msg(CommandSender sender, lm lm, Object... variables) {
//	if (!isWorldOk(sender))
//	    return;

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

//    public boolean hasPermission(CommandSender sender, String permision, boolean output) {
//	return hasPermission(sender, permision, output, null);
//    }
//
//    public boolean hasPermission(CommandSender sender, String permision) {
//	return hasPermission(sender, permision, true, null);
//    }
//
//    public boolean hasPermission(CommandSender sender, String permision, String message) {
//	return hasPermission(sender, permision, true, message);
//    }
//
//    public boolean hasPermission(CommandSender sender, String permision, lm message) {
//	return hasPermission(sender, permision, true, getLM().getMessage(message));
//    }
//
//    public boolean hasPermission(CommandSender sender, String permision, Boolean output, String message) {
//	if (sender == null)
//	    return false;
//	if (sender instanceof ConsoleCommandSender) {
//	    return true;
//	} else if (sender instanceof Player) {
//	    if (sender.hasPermission(permision))
//		return true;
//	    if (output) {
//		String outMsg = getLM().getMessage(lm.General_NoPermission);
//		if (message != null)
//		    outMsg = message;
//
//		RawMessage rm = new RawMessage();
//		rm.add(outMsg, "§2" + permision);
//		rm.show(sender);
//		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
//		console.sendMessage(ChatColor.RED + sender.getName() + " No permission -> " + permision);
//	    }
//	}
//	return false;
//    }

    public String getPrefix() {
        return prefix;
    }

    public String[] reduceArgs(String[] args) {
        if (args.length <= 1)
            return new String[0];
        return Arrays.copyOfRange(args, 1, args.length);
    }
//    public TownManager getTownManager() {
//	return townManager;
//    }

    public int getWorldGuardVersion() {
        return wepVersion;
    }

}
