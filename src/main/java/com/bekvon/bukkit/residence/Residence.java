package com.bekvon.bukkit.residence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import com.bekvon.bukkit.residence.Placeholders.Placeholder;
import com.bekvon.bukkit.residence.Placeholders.PlaceholderAPIHook;
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
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.dynmap.DynMapListeners;
import com.bekvon.bukkit.residence.dynmap.DynMapManager;
import com.bekvon.bukkit.residence.economy.BlackHoleEconomy;
import com.bekvon.bukkit.residence.economy.CMIEconomy;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.EssentialsEcoAdapter;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.gui.FlagUtil;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.CrackShotListener;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidenceLWCListener;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_08;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_09;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_10;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_12;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_13;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_14;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_15;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_16;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_17;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_19;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_20;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_21;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_21_8_Paper;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_21_8_Spigot;
import com.bekvon.bukkit.residence.listeners.ResidenceListener1_21_9_Paper;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.pl3xmap.Pl3xMapListeners;
import com.bekvon.bukkit.residence.pl3xmap.Pl3xMapManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.LeaseManager;
import com.bekvon.bukkit.residence.protection.PermissionListManager;
import com.bekvon.bukkit.residence.protection.PlayerManager;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.protection.WorldFlagManager;
import com.bekvon.bukkit.residence.raid.ResidenceRaidListener;
import com.bekvon.bukkit.residence.selection.AutoSelection;
import com.bekvon.bukkit.residence.selection.KingdomsUtil;
import com.bekvon.bukkit.residence.selection.Schematics7Manager;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WESchematicManager;
import com.bekvon.bukkit.residence.selection.WorldEdit7SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldGuard7Util;
import com.bekvon.bukkit.residence.selection.WorldGuardInterface;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopSignUtil;
import com.bekvon.bukkit.residence.signsStuff.SignUtil;
import com.bekvon.bukkit.residence.slimeFun.SlimefunManager;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.FileCleanUp;
import com.bekvon.bukkit.residence.utils.RandomTp;
import com.bekvon.bukkit.residence.utils.SafeLocationCache;
import com.bekvon.bukkit.residence.utils.Sorting;
import com.bekvon.bukkit.residence.utils.TabComplete;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;
import com.earth2me.essentials.Essentials;
import com.residence.mcstats.Metrics;
import com.residence.zip.ZipLibrary;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Items.CMIMaterial;
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

    private String ResidenceVersion;
    private List<String> authlist;
    private ResidenceManager rmanager;
    private SelectionManager smanager;
    private PermissionManager gmanager;
    private ConfigManager configManager;

    private SignUtil signmanager;

    protected ResidencePlayerListener plistener;

    private ResidenceCommandListener commandManager;

    private TransactionManager tmanager;
    private PermissionListManager pmanager;
    private LeaseManager leasemanager;
    private WorldItemManager imanager;
    private WorldFlagManager wmanager;
    private RentManager rentmanager;
    private ChatManager chatmanager;
    private Server server;
    private LocaleManager localeManager;
    private Language newLanguageManager;
    private PlayerManager PlayerManager;
    private FlagUtil FlagUtilManager;
    private ShopSignUtil ShopSignUtilManager;
//    private TownManager townManager;
    private RandomTp RandomTpManager;
    private DynMapManager DynManager;
    private Pl3xMapManager Pl3xManager;
    private Sorting SortingManager;
    private AutoSelection AutoSelectionManager;
    private WESchematicManager SchematicManager;
    private InformationPager InformationPagerManager;
    private WorldGuardInterface worldGuardUtil;
    private int wepVersion = 6;
    private KingdomsUtil kingdomsUtil;

    private CommandFiller cmdFiller;

    private ZipLibrary zip;

    private boolean firstenable = true;
    private EconomyInterface economy;
    public File dataFolder;
    private CMITask leaseBukkitId = null;
    private CMITask rentBukkitId = null;
    private CMITask healBukkitId = null;
    private CMITask feedBukkitId = null;
    private CMITask effectRemoveBukkitId = null;
    private CMITask despawnMobsBukkitId = null;
    private CMITask autosaveBukkitId = null;

    private boolean SlimeFun = false;
    private boolean BigDoors = false;
    private boolean lwc = false;
    Metrics metrics = null;

    protected boolean initsuccess = false;
    public Map<UUID, ClaimedResidence> deleteConfirm = new HashMap<UUID, ClaimedResidence>();
    public Map<UUID, ClaimedResidence> unrentConfirm = new HashMap<UUID, ClaimedResidence>();
    public Map<UUID, Long> rtMap = new HashMap<UUID, Long>();
    public Map<UUID, SafeLocationCache> teleportMap = new HashMap<UUID, SafeLocationCache>();

    private com.sk89q.worldedit.bukkit.WorldEditPlugin wep = null;
    private com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = null;
    private CMIMaterial wepid;

    private UUID ServerLandUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private Placeholder Placeholder;
    private boolean PlaceholderAPIEnabled = false;

    public Map<UUID, SafeLocationCache> getTeleportMap() {
        return teleportMap;
    }

    public Map<UUID, Long> getRandomTeleportMap() {
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
            ResidenceAPI = getResidenceManager();
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
            MarketRentAPI = getRentManager();
        return MarketRentAPI;
    }

    public MarketBuyInterface getMarketBuyManagerAPI() {
        if (MarketBuyAPI == null)
            MarketBuyAPI = getTransactionManager();
        return MarketBuyAPI;

    }

    public ChatInterface getResidenceChatAPI() {
        if (ChatAPI == null)
            ChatAPI = getChatManager();
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

    private Runnable doHeals = () -> getPlayerListener().doHeals();

    private Runnable doFeed = () -> getPlayerListener().feed();

    private Runnable removeBadEffects = () -> getPlayerListener().badEffects();

    private Runnable DespawnMobs = () -> getPlayerListener().DespawnMobs();

    private Runnable rentExpire = () -> {
        getRentManager().checkCurrentRents();
        if (getConfigManager().showIntervalMessages()) {
            lm.consoleMessage("- Rent Expirations checked!");
        }
    };

    private Runnable leaseExpire = () -> {
        getLeaseManager().doExpirations();
        if (getConfigManager().showIntervalMessages()) {
            lm.consoleMessage("- Lease Expirations checked!");
        }
    };

    private Runnable autoSave = () -> {
        if (!initsuccess)
            return;

        CMIScheduler.runTaskAsynchronously(this, () -> {
            try {
                saveYml();
            } catch (Throwable e) {
                Logger.getLogger("Minecraft").log(Level.SEVERE, getPrefix() + " SEVERE SAVE ERROR", e);
                e.printStackTrace();
            }
        });
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
        if (despawnMobsBukkitId != null)
            despawnMobsBukkitId.cancel();

        this.getPermissionManager().stopCacheClearScheduler();

        this.getSelectionManager().onDisable();

        this.getShopSignUtilManager().forceSaveIfPending();

        if (this.metrics != null)
            metrics.shutdown();

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
                lm.consoleMessage("SEVERE SAVE ERROR");
                ex.printStackTrace();
            }

            getPlayerManager().onPluginStop();

            lm.consoleMessage("Disabled!");
        }
        fullyLoaded = false;
    }

    @Override
    public void onEnable() {
        try {
            instance = this;

            initsuccess = false;
            server = this.getServer();
            dataFolder = this.getDataFolder();

            ResidenceVersion = this.getDescription().getVersion();
            authlist = this.getDescription().getAuthors();

            getCommandFiller().fillCommands();

            if (!dataFolder.isDirectory())
                dataFolder.mkdirs();

            if (!new File(dataFolder, "flags.yml").isFile()) {
                this.writeDefaultFromJar("flags.yml");
            }
            if (!new File(dataFolder, "groups.yml").isFile()) {
                this.writeDefaultFromJar("groups.yml");
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

            String multiworld = getConfigManager().getMultiworldPlugin();
            if (multiworld != null) {
                Plugin plugin = server.getPluginManager().getPlugin(multiworld);
                if (plugin != null && !plugin.isEnabled()) {
                    lm.consoleMessage("- Enabling multiworld plugin: " + multiworld);
                    server.getPluginManager().enablePlugin(plugin);
                }
            }

            getPlayerManager().load();

            getConfigManager().UpdateFlagFile();

            getFlagUtilManager().load();

            this.getPermissionManager().startCacheClearScheduler();

            getItemManager().load();
            getWorldFlags().load();

//	    townManager = new TownManager(this);

            InformationPagerManager = new InformationPager(this);

            zip = new ZipLibrary(this);

            Plugin lwcp = Bukkit.getPluginManager().getPlugin("LWC");
            try {
                if (lwcp != null) {
                    try {
                        ResidenceLWCListener.register(this);
                        lm.consoleMessage("LWC hooked.");
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

            LocaleManager.parseHelpEntries();

            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                lm.consoleMessage("Scanning for economy systems...");
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
                            lm.consoleMessage("Found Vault using economy system: &5" + vault.getEconomyName());
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
                    break;
                case Vault:
                    if (this.getPermissionManager().getPermissionsPlugin() instanceof ResidenceVaultAdapter) {
                        ResidenceVaultAdapter vault = (ResidenceVaultAdapter) this.getPermissionManager().getPermissionsPlugin();
                        if (vault.economyOK()) {
                            economy = vault;
                            lm.consoleMessage("Found Vault using economy system: &5" + vault.getEconomyName());
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
                    lm.consoleMessage("Unable to find an economy system...");
                    economy = new BlackHoleEconomy();
                }
            }

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

                if (Version.isCurrentEqualOrHigher(Version.v1_8_R1))
                    pm.registerEvents(new ResidenceListener1_08(), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_9_R1))
                    pm.registerEvents(new ResidenceListener1_09(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_10_R1))
                    pm.registerEvents(new ResidenceListener1_10(), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_12_R1))
                    pm.registerEvents(new ResidenceListener1_12(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
                    pm.registerEvents(new ResidenceListener1_13(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_14_R1))
                    pm.registerEvents(new ResidenceListener1_14(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_15_R1))
                    pm.registerEvents(new ResidenceListener1_15(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_16_R1))
                    pm.registerEvents(new ResidenceListener1_16(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_17_R1))
                    pm.registerEvents(new ResidenceListener1_17(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_19_R1))
                    pm.registerEvents(new ResidenceListener1_19(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_20_R1))
                    pm.registerEvents(new ResidenceListener1_20(this), this);
                if (Version.isCurrentEqualOrHigher(Version.v1_21_R1))
                    pm.registerEvents(new ResidenceListener1_21(this), this);

                if (Version.isCurrentEqualOrHigher(Version.v1_21_R5) && Version.isCurrentSubEqualOrHigher(8) || Version.isCurrentEqualOrHigher(Version.v1_22_R1)) {
                    if (Version.isPaperBranch())
                        pm.registerEvents(new ResidenceListener1_21_8_Paper(this), this);
                    else
                        pm.registerEvents(new ResidenceListener1_21_8_Spigot(this), this);
                }

                if (Version.isCurrentEqualOrHigher(Version.v1_21_R6)) {
                    if (Version.isPaperBranch())
                        pm.registerEvents(new ResidenceListener1_21_9_Paper(this), this);
                }

                pm.registerEvents(new ResidenceBlockListener(this), this);
                pm.registerEvents(getPlayerListener(), this);
                pm.registerEvents(new ResidenceEntityListener(this), this);
                pm.registerEvents(new ShopListener(this), this);
                pm.registerEvents(new ResidenceRaidListener(), this);

                firstenable = false;
            } else {
                getPlayerListener().reload();
            }

            if (setupPlaceHolderAPI()) {
                lm.consoleMessage("PlaceholderAPI was found - Enabling capabilities.");
                PlaceholderAPIEnabled = true;
            }

            if (getServer().getPluginManager().getPlugin("CrackShot") != null)
                getServer().getPluginManager().registerEvents(new CrackShotListener(this), this);

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
            autosaveBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, autoSave, autosaveInt, autosaveInt);

            if (getConfigManager().getHealInterval() > 0)
                healBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, doHeals, 20, getConfigManager().getHealInterval() * 20);
            if (getConfigManager().getFeedInterval() > 0)
                feedBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, doFeed, 20, getConfigManager().getFeedInterval() * 20);
            if (getConfigManager().getSafeZoneInterval() > 0)
                effectRemoveBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, removeBadEffects, 20, getConfigManager().getSafeZoneInterval() * 20);

            if (getConfigManager().AutoMobRemoval())
                despawnMobsBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, DespawnMobs, 20 * getConfigManager().AutoMobRemovalInterval(), 20
                    * getConfigManager().AutoMobRemovalInterval());

            if (getConfigManager().useLeases()) {
                int leaseInterval = getConfigManager().getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = leaseInterval * 60 * 20;
                leaseBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, leaseExpire, leaseInterval, leaseInterval);
            }
            if (getConfigManager().enabledRentSystem()) {
                int rentint = getConfigManager().getRentCheckInterval();
                if (rentint < 1) {
                    rentint = 1;
                }
                rentint = rentint * 60 * 20;
                rentBukkitId = CMIScheduler.scheduleSyncRepeatingTask(this, rentExpire, rentint, rentint);
            }
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (getPermissionManager().isResidenceAdmin(player)) {
                    ResAdmin.turnResAdminOn(player);
                }
            }

            metrics = new Metrics(this, 27340);

            lm.consoleMessage("Enabled! Version " + this.getDescription().getVersion() + " by Zrips");
            initsuccess = true;

        } catch (Exception ex) {
            initsuccess = false;
            getServer().getPluginManager().disablePlugin(this);
            lm.consoleMessage("- FAILED INITIALIZATION! DISABLED! ERROR:");
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            Bukkit.getServer().shutdown();
        }

        getShopSignUtilManager().LoadShopVotes();
        getShopSignUtilManager().LoadSigns();
        getShopSignUtilManager().boardUpdate();

        CMIVersionChecker.VersionCheck(null, 11480, this.getDescription());
        fullyLoaded = true;
    }

    private boolean setupPlaceHolderAPI() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
            return false;
        return new PlaceholderAPIHook(this).register();
    }

    public SignUtil getSignUtil() {
        if (signmanager == null)
            signmanager = new SignUtil(this);
        return signmanager;
    }

    private void setWorldEdit() {
        try {
            Plugin plugin = server.getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                this.wep = (com.sk89q.worldedit.bukkit.WorldEditPlugin) plugin;

                if (getConfigManager().isWorldEditIntegration())
                    smanager = new WorldEdit7SelectionManager(server, this);
                if (wep != null)
                    SchematicManager = new Schematics7Manager(this);

                if (smanager == null)
                    smanager = new SelectionManager(server, this);
                if (this.getWorldEdit().getConfig().isInt("wand-item"))
                    wepid = CMIMaterial.get(this.getWorldEdit().getConfig().getInt("wand-item"));
                else
                    wepid = CMIMaterial.get((String) this.getWorldEdit().getConfig().get("wand-item"));

                lm.consoleMessage("Found WorldEdit " + this.getWorldEdit().getDescription().getVersion());
            } else {
                smanager = new SelectionManager(server, this);
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    private boolean kingdomsPresent = false;

    private void setKingdoms() {
        if (Bukkit.getPluginManager().getPlugin("Kingdoms") != null) {
            try {
                Class.forName("org.kingdoms.constants.land.location.SimpleChunkLocation");
                kingdomsPresent = true;
            } catch (Throwable e) {
                lm.consoleMessage("Failed to recognize Kingdoms plugin. Compatability disabled");
            }
        }
    }

    public boolean isKingdomsPresent() {
        return kingdomsPresent;
    }

    private void setWorldGuard() {
        Plugin wgplugin = server.getPluginManager().getPlugin("WorldGuard");
        if (wgplugin != null) {
            wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) wgplugin;
            lm.consoleMessage("Found WorldGuard " + wg.getDescription().getVersion());
        }
    }

    public Residence getPlugin() {
        return this;
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
        }
        return cmdFiller;
    }

    public ResidenceManager getResidenceManager() {
        if (rmanager == null)
            rmanager = new ResidenceManager(this);
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
        if (AutoSelectionManager == null)
            AutoSelectionManager = new AutoSelection(this);
        return AutoSelectionManager;
    }

    public Sorting getSortingManager() {
        if (SortingManager == null)
            SortingManager = new Sorting();
        return SortingManager;
    }

    public RandomTp getRandomTpManager() {
        if (RandomTpManager == null)
            RandomTpManager = new RandomTp(this);
        return RandomTpManager;
    }

    public EconomyInterface getEconomyManager() {
        return economy;
    }

    public Server getServ() {
        return server;
    }

    public LeaseManager getLeaseManager() {
        if (leasemanager == null)
            leasemanager = new LeaseManager(this);
        return leasemanager;
    }

    public PlayerManager getPlayerManager() {
        if (PlayerManager == null)
            PlayerManager = new PlayerManager(this);
        return PlayerManager;
    }

    public HelpEntry getHelpPages() {
        return LocaleManager.getHelpPages();
    }

    public ConfigManager getConfigManager() {
        if (configManager == null)
            configManager = new ConfigManager(this);
        return configManager;
    }

    public TransactionManager getTransactionManager() {
        if (tmanager == null)
            tmanager = new TransactionManager(this);
        return tmanager;
    }

    public WorldItemManager getItemManager() {
        if (imanager == null)
            imanager = new WorldItemManager(this);
        return imanager;
    }

    public WorldFlagManager getWorldFlags() {
        if (wmanager == null)
            wmanager = new WorldFlagManager(this);
        return wmanager;
    }

    public RentManager getRentManager() {
        if (rentmanager == null)
            rentmanager = new RentManager(this);
        return rentmanager;
    }

    public LocaleManager getLocaleManager() {
        if (localeManager == null)
            localeManager = new LocaleManager(this);
        return localeManager;
    }

    public Language getLM() {
        if (newLanguageManager == null) {
            newLanguageManager = new Language(this);
            newLanguageManager.LanguageReload();
        }
        return newLanguageManager;
    }

    public ResidencePlayerListener getPlayerListener() {
        if (plistener == null)
            plistener = new ResidencePlayerListener(this);
        return plistener;
    }

    public ChatManager getChatManager() {
        if (chatmanager == null)
            chatmanager = new ChatManager();
        return chatmanager;
    }

    public String getResidenceVersion() {
        return ResidenceVersion;
    }

    public List<String> getAuthors() {
        return authlist;
    }

    @Deprecated
    public FlagPermissions getPermsByLoc(Location loc) {
        return FlagPermissions.getPerms(loc);
    }

    @Deprecated
    public FlagPermissions getPermsByLocForPlayer(Location loc, Player player) {
        return FlagPermissions.getPerms(loc, player);
    }

    private void loadEssentialsEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Essentials");
        if (p != null) {
            economy = new EssentialsEcoAdapter((Essentials) p);
            lm.consoleMessage("Successfully linked with &5Essentials Economy");
        } else {
            lm.consoleMessage("Essentials Economy NOT found!");
        }
    }

    private void loadCMIEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("CMI");
        if (p != null) {
            economy = new CMIEconomy();
            lm.consoleMessage("Successfully linked with &5CMIEconomy");
        } else {
            lm.consoleMessage("CMIEconomy NOT found!");
        }
    }

    private void loadVaultEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("Vault");
        if (p != null) {
            ResidenceVaultAdapter vault = new ResidenceVaultAdapter(getServer());
            if (vault.economyOK()) {
                lm.consoleMessage("Found Vault using economy: &5" + vault.getEconomyName());
                economy = vault;
            } else {
                lm.consoleMessage("Found Vault, but Vault reported no usable economy system...");
            }
        } else {
            lm.consoleMessage("Vault NOT found!");
        }
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
        Map<String, Object> save = getResidenceManager().save();
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
        yml.getRoot().put("Economy", getTransactionManager().save());
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
        yml.getRoot().put("Leases", getLeaseManager().save());
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
        yml.getRoot().put("RentSystem", getRentManager().save());
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

    private void loadMessagesAndFlags(String worldName, YMLSaveHelper yml) {
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
                    lm.consoleMessage("Loading save data for world " + worldName + "...");

                yml = new YMLSaveHelper(loadFile);
                yml.load();
                if (yml.getRoot() == null)
                    continue;

                loadMessagesAndFlags(worldName, yml);

                worlds.put(worldName, yml.getRoot().get("Residences"));

                int pass = (int) (System.currentTimeMillis() - time);
                String pastTime = pass > 1000 ? String.format("%.2f", (pass / 1000F)) + " sec" : pass + " ms";

                if (!isDisabledWorld(worldName) && !this.getConfigManager().CleanerStartupLog)
                    lm.consoleMessage("Loaded " + worldName + " data. (" + pastTime + ")");
            }

            getResidenceManager().load(worlds);

            // Getting shop residences
            Map<String, ClaimedResidence> resList = getResidenceManager().getResidences();
            for (Entry<String, ClaimedResidence> one : resList.entrySet()) {
                this.getResidenceManager().addShops(one.getValue());
            }

            loadFile = new File(saveFolder, "forsale.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                Map<String, Object> root = yml.getRoot();
                if (root != null)
                    getTransactionManager().load((Map) root.get("Economy"));
            }
            loadFile = new File(saveFolder, "leases.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                Map<String, Object> root = yml.getRoot();
                if (root != null)
                    getLeaseManager().load((Map) root.get("Leases"));
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
                    getRentManager().load((Map) root.get("RentSystem"));
            }

            return true;
        } catch (Exception ex) {
            Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private void writeDefaultFromJar(String fileName) {
        if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), fileName), fileName, true)) {
            lm.consoleMessage("Wrote default " + fileName + "...");
        }
    }

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

    public String getServerLandName() {
        return this.getLM().getMessage(lm.server_land);
    }

    public UUID getServerUUID() {
        return ServerLandUUID;
    }

    public boolean isDisabledWorld(World world) {
        return isDisabledWorld(world.getName());
    }

    public boolean isDisabledWorld(String worldname) {
        if (!getConfigManager().EnabledWorldsList.isEmpty()) {
            return !getConfigManager().EnabledWorldsList.contains(worldname);
        }
        return getConfigManager().DisabledWorldsList.contains(worldname);
    }

    public boolean isDisabledWorldListener(World world) {
        return isDisabledWorldListener(world.getName());
    }

    public boolean isDisabledWorldListener(String worldname) {

        if (!getConfigManager().EnabledWorldsList.isEmpty()) {
            return !getConfigManager().EnabledWorldsList.contains(worldname) && getConfigManager().DisableListeners;
        }

        return getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableListeners;
    }

    public boolean isDisabledWorldCommand(World world) {
        return isDisabledWorldCommand(world.getName());
    }

    public boolean isDisabledWorldCommand(String worldname) {

        if (!getConfigManager().EnabledWorldsList.isEmpty()) {
            return !getConfigManager().EnabledWorldsList.contains(worldname) && getConfigManager().DisableCommands;
        }

        return getConfigManager().DisabledWorldsList.contains(worldname) && getConfigManager().DisableCommands;
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
        return CMIChatColor.GREEN + "[" + CMIChatColor.GOLD + "Residence" + CMIChatColor.GREEN + "]" + CMIChatColor.GRAY;
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

    @Deprecated
    public boolean isResAdminOn(CommandSender sender) {
        return ResAdmin.isResAdmin(sender);
    }

    @Deprecated
    public boolean isResAdminOn(Player player) {
        return ResAdmin.isResAdmin(player);
    }
}
