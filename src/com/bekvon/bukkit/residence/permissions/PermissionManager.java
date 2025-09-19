package com.bekvon.bukkit.residence.permissions;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.PlayerGroup;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.Util.CMIVersionChecker;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class PermissionManager {
    protected static PermissionsInterface perms;
    protected LinkedHashMap<String, PermissionGroup> groups;
    protected Map<UUID, String> playersGroup;
    protected FlagPermissions globalFlagPerms;

    protected HashMap<String, PlayerGroup> groupsMap = new HashMap<String, PlayerGroup>();
    private PermissionGroup defaultGroup = null;
    private Residence plugin;

    private CMITask autoCacheClear = null;
    private static final int cacheClearDelay = 600;

    public PermissionManager(Residence plugin) {
        this.plugin = plugin;
        try {
            groups = new LinkedHashMap<String, PermissionGroup>();
            playersGroup = Collections.synchronizedMap(new HashMap<UUID, String>());
            globalFlagPerms = new FlagPermissions();
            this.readConfig();
            checkPermissions();
        } catch (Exception ex) {
            Logger.getLogger(PermissionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void startCacheClearScheduler() {
        autoCacheClear = CMIScheduler.scheduleSyncRepeatingTask(plugin, cacheClear, cacheClearDelay * 20L, cacheClearDelay * 20L);
    }

    public void stopCacheClearScheduler() {
        try {
            if (autoCacheClear != null)
                autoCacheClear.cancel();
        } catch (Throwable e) {
        }
    }

    public FlagPermissions getAllFlags() {
        return this.globalFlagPerms;
    }

    public Map<UUID, String> getPlayersGroups() {
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
        if (!Residence.getInstance().isFullyLoaded())
            return plugin.getConfigManager().getDefaultGroup().toLowerCase();
        try {
            return perms.getPlayerGroup(player, world).toLowerCase();
        } catch (Exception e) {
            return plugin.getConfigManager().getDefaultGroup().toLowerCase();
        }
    }

    public boolean isResidenceAdmin(CommandSender sender) {
        if (sender == null)
            return false;

        return (ResPerm.admin.hasPermission(sender) || (sender.isOp() && plugin.getConfigManager().getOpsAreAdmins()));
    }

    private void checkPermissions() {
        Server server = plugin.getServ();

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        Plugin pl = pluginManager.getPlugin("LuckPerms");
        if (pl != null && pl.isEnabled()) {
            Integer ver = CMIVersionChecker.convertVersion(pl.getDescription().getVersion());
            if (ver > 50000) {
                perms = new LuckPerms5Adapter();
                Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Found LuckPerms5 Plugin!");
                return;
            }
            plugin.consoleMessage("&cLuckPerms plugin was found but its outdated");
        }

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

                    PermissionGroup old = groups.getOrDefault(key.toLowerCase(), new PermissionGroup(key.toLowerCase(), null, globalFlagPerms, i));

                    old.mirrorIn(nodes.getConfigurationSection(key));

                    groups.put(key.toLowerCase(), old);
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

        Bukkit.getConsoleSender().sendMessage(plugin.getPrefix() + " Loaded (" + groups.size() + ") groups");

        if (groupsFile.isConfigurationSection("GroupAssignments")) {
            Set<String> keys = groupsFile.getConfigurationSection("GroupAssignments").getKeys(false);
            if (keys != null) {
                for (String key : keys) {

                    UUID uuid = ResidencePlayer.getUUID(key.toLowerCase());
                    if (uuid != null)
                        playersGroup.put(uuid, groupsFile.getString("GroupAssignments." + key, defaultGroup).toLowerCase());
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
        bypass_itemdrop("Allows to bypass itemdrop flag"),
        buy("Allows to buy residence"),
        sell("Allows to sell residence"),
        max_res_x_$1("Defines residence size limit on X axis", "number"),
        max_res_z_$1("Defines residence size limit on Z axis", "number"),
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

        private boolean show = true;
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
                rm.addText(Residence.getInstance().getLM().getMessage(lms == null ? lm.General_NoPermission : lms)).addHover(showPerm ? perm : null);
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
                lm.General_NoPermission.sendMessage(console, "[playerName]", sender.getName(), "[permission]", permission);
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
                rm.addText(Residence.getInstance().getLM().getMessage(lm.General_NoPermission)).addHover(showPerm ? perm : null);
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
                rm.addText(Residence.getInstance().getLM().getMessage(lm.General_NoPermission)).addHover(showPerm ? permision : null);
                rm.show(sender);

                informConsole(sender, permision, informConsole);
            }
            return false;
        }
    }

    private HashMap<UUID, HashMap<String, PermissionInfo>> cache = new HashMap<UUID, HashMap<String, PermissionInfo>>();

    private Runnable cacheClear = () -> cache.clear();

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

    @Deprecated
    public PermissionInfo getPermissionInfo(Player player, String perm, Long delayInMiliseconds) {
        return getPermissionInfo(player, perm, false, delayInMiliseconds);
    }

    @Deprecated
    public PermissionInfo getPermissionInfo(Player player, ResPerm perm) {
        return getPermissionInfo(player, perm, 1000L);
    }

    public PermissionInfo getPermissionInfo(UUID uuid, ResPerm perm) {
        return getPermissionInfo(uuid, perm, 1000L);
    }

    @Deprecated
    public PermissionInfo getPermissionInfo(Player player, ResPerm perm, Long delayInMiliseconds) {
        return getPermissionInfo(player.getUniqueId(), perm, delayInMiliseconds);
    }

    public PermissionInfo getPermissionInfo(UUID uuid, ResPerm perm, Long delayInMiliseconds) {
        String permission = perm.getPermission(" ");
        if (permission.endsWith(" "))
            permission = permission.replace(" ", "");
        if (permission.endsWith("."))
            permission = permission.substring(0, permission.length() - 1);
        return getPermissionInfo(uuid, permission, false, delayInMiliseconds);
    }

    @Deprecated
    public PermissionInfo getPermissionInfo(Player player, String perm) {
        return getPermissionInfo(player, perm, false, 1000L);
    }

    @Deprecated
    public PermissionInfo getPermissionInfo(Player player, String perm, boolean force) {
        return getPermissionInfo(player, perm, force, 1000L);
    }

    @Deprecated
    public PermissionInfo getPermissionInfo(Player player, String perm, boolean force, Long delay) {
        return getPermissionInfo(player.getUniqueId(), perm, force, delay);
    }

    public PermissionInfo getPermissionInfo(UUID uuid, String perm, boolean force, Long delay) {
        perm = perm.toLowerCase();
        if (!perm.endsWith("."))
            perm += ".";

        if (uuid == null)
            return new PermissionInfo(perm, delay);

        HashMap<String, PermissionInfo> c = cache.get(uuid);
        if (c == null)
            c = new HashMap<String, PermissionInfo>();

        PermissionInfo p = c.get(perm);

        if (p == null)
            p = new PermissionInfo(perm, delay);

        if (delay != null)
            p.setDelay(delay);

        String pref = perm.contains(".") ? perm.split("\\.")[0] : perm;

        if (force || p.isTimeToRecalculate()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                HashMap<String, Boolean> all = getAll(player, pref);

                Double max = null;
                Double min = null;
                for (Entry<String, Boolean> uno : all.entrySet()) {
                    if (!uno.getValue())
                        continue;

                    if (!uno.getKey().startsWith(perm))
                        continue;

                    String value = uno.getKey().replace(perm, "");

                    p.addValue(value);
                    try {
                        double t = Double.parseDouble(value);

                        if (max == null || t > max)
                            max = t;

                        if (min == null || t < min)
                            min = t;
                    } catch (Exception e) {
                    }

                }

                p.setMaxValue(max);
                p.setMinValue(min);
            }
        }
        p.setLastChecked(System.currentTimeMillis());
        c.put(perm, p);
        cache.put(uuid, c);
        return p;
    }

    private static HashMap<String, Boolean> getAll(Player player, String pref) {
        pref = pref.endsWith(".") ? pref : pref + ".";
        HashMap<String, Boolean> mine = new HashMap<String, Boolean>();
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().startsWith(pref))
                mine.put(permission.getPermission(), permission.getValue());
        }
        return mine;
    }
}
