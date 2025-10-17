package com.bekvon.bukkit.residence.permissions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Logs.CMIDebug;

public class PermissionGroup {

    protected String groupname;

    private int xmax = 0;
    private int ymax = 0;
    private int zmax = 0;

    private int xmin = 0;
    private int ymin = 0;
    private int zmin = 0;

    protected int Subzonexmax = 0;
    protected int Subzoneymax = 0;
    protected int Subzonezmax = 0;

    protected int Subzonexmin = 0;
    protected int Subzoneymin = 0;
    protected int Subzonezmin = 0;

    protected int resmax = 0;
    private double costperarea = 0;
    private double sellperarea = 0;
    protected boolean tpaccess = false;
    protected int subzonedepth = 0;
    protected int maxSubzones = 3;
    protected FlagPermissions flagPerms;
    protected Map<String, Boolean> creatorDefaultFlags;
//    protected Map<String, Boolean> rentedDefaultFlags;
    protected Map<String, Map<String, Boolean>> groupDefaultFlags;
    protected Map<String, Boolean> residenceDefaultFlags;

    protected Map<String, Map<String, FlagPermissions>> groupperms;
    protected Map<String, FlagPermissions> worldperms;

    protected boolean messageperms = false;
    protected String defaultEnterMessage = null;
    protected String defaultLeaveMessage = null;
    private int maxLeaseTime = 16;
    private int leaseGiveTime = 14;
    protected double renewcostperarea = 0.02D;
    protected boolean canBuy = false;
    protected boolean canSell = false;
    protected boolean buyIgnoreLimits = false;
    protected boolean cancreate = false;
    protected int maxPhysical = 2;
    protected boolean unstuck = false;
    protected boolean kick = false;
    protected int minHeight = 0;
    protected int maxHeight = 256;
    protected int maxRents = 0;
    protected int MaxRentDays = -1;
    protected int maxRentables = 0;
    protected boolean selectCommandAccess = true;
    protected boolean itemListAccess = true;
    protected int priority = 0;

    public PermissionGroup(String name) {
        flagPerms = new FlagPermissions();
        creatorDefaultFlags = new HashMap<String, Boolean>();
//	rentedDefaultFlags = new HashMap<String, Boolean>();
        residenceDefaultFlags = new HashMap<String, Boolean>();
        groupDefaultFlags = new HashMap<String, Map<String, Boolean>>();

        worldperms = new HashMap<>();
        groupperms = new HashMap<>();

        groupname = name;
    }

    public void setPriority(int number) {
        this.priority = number;
    }

    public int getPriority() {
        return this.priority;
    }

    public PermissionGroup(String name, ConfigurationSection node) {
        this(name);
        this.parseGroup(node);
    }

    public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms) {
        this(name, node);
        flagPerms.setParent(parentFlagPerms);
    }

    public PermissionGroup(String name, ConfigurationSection node, FlagPermissions parentFlagPerms, int priority) {
        this(name, node);
        flagPerms.setParent(parentFlagPerms);
        this.priority = priority;
    }

    public void mirrorIn(ConfigurationSection limits) {
        parseGroup(limits);
    }

    private void parseGroup(ConfigurationSection limits) {
        if (limits == null) {
            return;
        }
        if (limits.contains("Residence.CanCreate"))
            cancreate = limits.getBoolean("Residence.CanCreate", false);

        if (limits.contains("Residence.MaxResidences"))
            resmax = limits.getInt("Residence.MaxResidences", 0);

        if (limits.contains("Residence.MaxAreasPerResidence"))
            maxPhysical = limits.getInt("Residence.MaxAreasPerResidence", 2);

        if (limits.contains("Residence.MaxEastWest"))
            xmax = limits.getInt("Residence.MaxEastWest", 0);
        if (limits.contains("Residence.MinEastWest"))
            xmin = limits.getInt("Residence.MinEastWest", 0);

        xmin = Math.min(getMinX(), getMaxX());

        if (limits.contains("Residence.MaxUpDown"))
            ymax = limits.getInt("Residence.MaxUpDown", 0);
        if (limits.contains("Residence.MinUpDown"))
            ymin = limits.getInt("Residence.MinUpDown", 0);
        ymin = Math.min(ymin, ymax);

        if (Residence.getInstance().getConfigManager().isSelectionIgnoreY()) {
            // This needs to be 256 to include entire height where 255 and block 0
            ymax = CMIWorld.getMaxHeight(Bukkit.getWorlds().get(0)) + Math.abs(CMIWorld.getMinHeight(Bukkit.getWorlds().get(0)));
        }

        if (limits.contains("Residence.MaxNorthSouth"))
            zmax = limits.getInt("Residence.MaxNorthSouth", 0);
        if (limits.contains("Residence.MinNorthSouth"))
            zmin = limits.getInt("Residence.MinNorthSouth", 0);
        zmin = Math.min(zmin, zmax);

        if (limits.contains("Residence.MinHeight"))
            minHeight = limits.getInt("Residence.MinHeight", 0);
        // This needs to be 256 to include entire height where 255 and block 0
        if (limits.contains("Residence.MaxHeight"))
            maxHeight = limits.getInt("Residence.MaxHeight", CMIWorld.getMaxHeight(Bukkit.getWorlds().get(0)));

        if (limits.contains("Residence.CanTeleport"))
            tpaccess = limits.getBoolean("Residence.CanTeleport", false);

        if (limits.contains("Residence.MaxSubzonesInArea"))
            maxSubzones = limits.getInt("Residence.MaxSubzonesInArea", 3);

        if (limits.contains("Residence.SubzoneDepth"))
            subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);

        if (limits.contains("Residence.SubzoneMaxEastWest"))
            Subzonexmax = limits.getInt("Residence.SubzoneMaxEastWest", getMaxX());
        else
            Subzonexmax = getMaxX();
        Subzonexmax = Math.min(getMaxX(), Subzonexmax);

        if (limits.contains("Residence.SubzoneMinEastWest"))
            Subzonexmin = limits.getInt("Residence.SubzoneMinEastWest", 0);
        Subzonexmin = Math.min(Subzonexmin, Subzonexmax);

        if (limits.contains("Residence.SubzoneMaxUpDown"))
            Subzoneymax = limits.getInt("Residence.SubzoneMaxUpDown", ymax);
        else
            Subzoneymax = getMaxYSize();

        Subzoneymax = Math.min(ymax, Subzoneymax);
        if (limits.contains("Residence.SubzoneMinUpDown"))
            Subzoneymin = limits.getInt("Residence.SubzoneMinUpDown", 0);
        Subzoneymin = Math.min(Subzoneymin, Subzoneymax);

        if (limits.contains("Residence.SubzoneMaxNorthSouth"))
            Subzonezmax = limits.getInt("Residence.SubzoneMaxNorthSouth", zmax);
        else
            Subzonezmax = getMaxZ();

        if (Residence.getInstance().getConfigManager().isSelectionIgnoreYInSubzone()) {
            // This needs to be 256 to include entire height where 255 and block 0
            Subzonezmax = CMIWorld.getMaxHeight(Bukkit.getWorlds().get(0)) + Math.abs(CMIWorld.getMinHeight(Bukkit.getWorlds().get(0)));
        }

        Subzonezmax = Math.min(zmax, Subzonezmax);

        if (limits.contains("Residence.SubzoneMinNorthSouth"))
            Subzonezmin = limits.getInt("Residence.SubzoneMinNorthSouth", 0);
        Subzonezmin = Math.min(Subzonezmin, Subzonezmax);

        if (limits.contains("Messaging.CanChange"))
            messageperms = limits.getBoolean("Messaging.CanChange", false);
        if (limits.contains("Messaging.DefaultEnter"))
            defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
        if (limits.contains("Messaging.DefaultLeave"))
            defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
        if (limits.contains("Lease.MaxDays"))
            maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
        if (limits.contains("Lease.RenewIncrement"))
            leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
        if (limits.contains("Rent.MaxRents"))
            maxRents = limits.getInt("Rent.MaxRents", 0);

        if (limits.contains("Rent.MaxRentDays"))
            MaxRentDays = limits.getInt("Rent.MaxRentDays", -1);

        if (limits.contains("Rent.MaxRentables"))
            maxRentables = limits.getInt("Rent.MaxRentables", 0);
        if (limits.contains("Economy.RenewCost"))
            renewcostperarea = limits.getDouble("Economy.RenewCost", 0.02D);
        if (limits.contains("Economy.CanBuy"))
            canBuy = limits.getBoolean("Economy.CanBuy", false);
        if (limits.contains("Economy.CanSell"))
            canSell = limits.getBoolean("Economy.CanSell", false);
        if (limits.contains("Economy.IgnoreLimits"))
            buyIgnoreLimits = limits.getBoolean("Economy.IgnoreLimits", false);
        if (limits.contains("Economy.BuyCost"))
            costperarea = limits.getDouble("Economy.BuyCost", 0);

        if (limits.contains("Economy.SellCost"))
            sellperarea = limits.getDouble("Economy.SellCost", 0);

        if (limits.isBoolean("Residence.Unstuck"))
            unstuck = limits.getBoolean("Residence.Unstuck", false);
        if (limits.contains("Residence.Kick"))
            kick = limits.getBoolean("Residence.Kick", false);
        if (limits.contains("Residence.SelectCommandAccess"))
            selectCommandAccess = limits.getBoolean("Residence.SelectCommandAccess", true);
        if (limits.contains("Residence.ItemListAccess"))
            itemListAccess = limits.getBoolean("Residence.ItemListAccess", true);
        ConfigurationSection node = limits.getConfigurationSection("Flags.Permission");
        Set<String> flags = null;
        if (node != null) {
            flags = node.getKeys(false);
        }
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.Permission." + flagname, false);
                flagPerms.setFlag(flagname, access ? FlagState.TRUE : FlagState.FALSE);
            }
        }
        node = limits.getConfigurationSection("Flags.CreatorDefault");
        if (node != null) {
            flags = node.getKeys(false);
        }
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.CreatorDefault." + flagname, false);
                creatorDefaultFlags.put(flagname, access);
            }
        }

//	node = limits.getConfigurationSection("Flags.RentedDefault");
//	if (node == null) {
//	    Object defaultRented = limits.get("Flags.CreatorDefault");
//	    if (defaultRented != null) {
//		limits.set("Flags.RentedDefault", defaultRented);
//	    }
//	    node = limits.getConfigurationSection("Flags.RentedDefault");
//	}
//	if (node != null) {
//	    flags = node.getKeys(false);
//	    if (flags != null) {
//		Iterator<String> flagit = flags.iterator();
//		while (flagit.hasNext()) {
//		    String flagname = flagit.next();
//		    rentedDefaultFlags.put(flagname, limits.getBoolean("Flags.RentedDefault." + flagname, false));
//		}
//	    }
//	}

        node = limits.getConfigurationSection("Flags.Default");
        if (node != null) {
            flags = node.getKeys(false);
        }
        if (flags != null) {
            Iterator<String> flagit = flags.iterator();
            while (flagit.hasNext()) {
                String flagname = flagit.next();
                boolean access = limits.getBoolean("Flags.Default." + flagname, false);
                residenceDefaultFlags.put(flagname, access);
            }
        }
        node = limits.getConfigurationSection("Flags.GroupDefault");
        Set<String> groupDef = null;
        if (node != null) {
            groupDef = node.getKeys(false);
        }
        if (groupDef != null) {
            Iterator<String> groupit = groupDef.iterator();
            while (groupit.hasNext()) {
                String name = groupit.next();
                Map<String, Boolean> gflags = new HashMap<String, Boolean>();
                flags = limits.getConfigurationSection("Flags.GroupDefault." + name).getKeys(false);
                Iterator<String> flagit = flags.iterator();
                while (flagit.hasNext()) {
                    String flagname = flagit.next();
                    boolean access = limits.getBoolean("Flags.GroupDefault." + name + "." + flagname, false);
                    gflags.put(flagname, access);
                }
                groupDefaultFlags.put(name, gflags);
            }
        }
    }

    public String getGroupName() {
        return groupname;
    }

    public int getMaxX() {
        return xmax;
    }

    public int getMaxYSize() {
        return ymax;
    }

    public int getMinYSize() {
        return ymin;
    }

    @Deprecated
    public int getMaxY() {
        return getMaxYSize();
    }

    public int getMaxZ() {
        return zmax;
    }

    public int getMinX() {
        return xmin;
    }

    @Deprecated
    public int getMinY() {
        return getMinYSize();
    }

    public int getMinZ() {
        return zmin;
    }

    public int getSubzoneMaxX() {
        return Subzonexmax;
    }

    public int getSubzoneMaxY() {
        return Subzoneymax;
    }

    public int getSubzoneMaxZ() {
        return Subzonezmax;
    }

    public int getSubzoneMinX() {
        return Subzonexmin;
    }

    public int getSubzoneMinY() {
        return Subzoneymin;
    }

    public int getSubzoneMinZ() {
        return Subzonezmin;
    }

    @Deprecated
    public int getMinHeight() {
        return getLowestYAllowed();
    }

    public int getLowestYAllowed() {
        return minHeight;
    }

    public int getHighestYAllowed() {
        return maxHeight;
    }

    @Deprecated
    public int getMaxHeight() {
        return getHighestYAllowed();
    }

    public int getMaxZones() {
        return resmax;
    }

    public double getCostPerBlock() {
        return costperarea;
    }

    public double getSellPerBlock() {
        return sellperarea;
    }

    public boolean hasTpAccess() {
        return tpaccess;
    }

    public int getMaxSubzoneDepth() {
        return subzonedepth;
    }

    public int getMaxSubzones() {
        return maxSubzones;
    }

    public boolean canSetEnterLeaveMessages() {
        return messageperms;
    }

    public String getDefaultEnterMessage() {
        if (defaultEnterMessage == null)
            return "";
        return defaultEnterMessage;
    }

    public String getDefaultLeaveMessage() {
        if (defaultLeaveMessage == null)
            return "";
        return defaultLeaveMessage;
    }

    public int getMaxLeaseTime() {
        return maxLeaseTime;
    }

    public int getLeaseGiveTime() {
        return leaseGiveTime;
    }

    public double getLeaseRenewCost() {
        return renewcostperarea;
    }

    public boolean canBuyLand() {
        return canBuy;
    }

    public boolean canSellLand() {
        return canSell;
    }

    public int getMaxRents() {
        return maxRents;
    }

    public int getMaxRentDays() {
        return MaxRentDays;
    }

    public int getMaxRentables() {
        return maxRentables;
    }

    public boolean buyLandIgnoreLimits() {
        return buyIgnoreLimits;
    }

    public boolean hasUnstuckAccess() {
        return unstuck;
    }

    public boolean hasKickAccess() {
        return kick;
    }

    public int getMaxPhysicalPerResidence() {
        return maxPhysical;
    }

    public Set<Entry<String, Boolean>> getDefaultResidenceFlags() {
        return residenceDefaultFlags.entrySet();
    }

    public Set<Entry<String, Boolean>> getDefaultCreatorFlags() {
        return creatorDefaultFlags.entrySet();
    }

//    public Set<Entry<String, Boolean>> getDefaultRentedFlags() {
//	return rentedDefaultFlags.entrySet();
//    }

    public Set<Entry<String, Map<String, Boolean>>> getDefaultGroupFlags() {
        return groupDefaultFlags.entrySet();
    }

    public boolean canCreateResidences() {
        return cancreate;
    }

    public boolean hasFlagAccess(Flags flag) {
        return flagPerms.has(flag, false);
    }

    @Deprecated
    public boolean hasFlagAccess(String flag) {
        return flagPerms.has(flag, false);
    }

//    public boolean inLimits(CuboidArea area) {
//	if (area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax) {
//	    return false;
//	}
//	return true;
//    }
//
//    public boolean inLimitsSubzone(CuboidArea area) {
//	if (area.getXSize() > Subzonexmax || area.getYSize() > Subzoneymax || area.getZSize() > Subzonezmax) {
//	    return false;
//	}
//	return true;
//    }

    public boolean selectCommandAccess() {
        return selectCommandAccess;
    }

    public boolean itemListAccess() {
        return itemListAccess;
    }

    @Deprecated
    public void printLimits(CommandSender player, OfflinePlayer target, boolean resadmin) {
        printLimits(player, target.getUniqueId(), resadmin);
    }

    public void printLimits(CommandSender sender, UUID target, boolean resadmin) {

        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(target);
        rPlayer.getGroup(true);
        PermissionGroup group = rPlayer.getGroup();

        Player player = rPlayer.getPlayer();

        lm.General_Separator.sendMessage(sender);

        lm.Limits_PGroup.sendMessage(sender, rPlayer.getGroups().getPermissionGroup().getGroupName());

        lm.Limits_RGroup.sendMessage(sender, group.getGroupName());
        if (player != null && resadmin)
            lm.Limits_Admin.sendMessage(sender, Residence.getInstance().getPermissionManager().isResidenceAdmin(sender));
        lm.Limits_CanCreate.sendMessage(sender, group.canCreateResidences());
        lm.Limits_MaxRes.sendMessage(sender, rPlayer.getMaxRes());
        lm.Limits_NumberOwn.sendMessage(sender, rPlayer.getResAmount());
        lm.Limits_MaxEW.sendMessage(sender, group.getMinX() + "-" + rPlayer.getMaxX());
        lm.Limits_MaxNS.sendMessage(sender, group.getMinZ() + "-" + rPlayer.getMaxZ());
        lm.Limits_MaxUD.sendMessage(sender, group.getMinYSize() + "-" + group.getMaxYSize());
        lm.Limits_MinMax.sendMessage(sender, group.getLowestYAllowed(), group.getHighestYAllowed());
        lm.Limits_MaxSubzones.sendMessage(sender, rPlayer.getMaxSubzones());
        lm.Limits_MaxSubDepth.sendMessage(sender, rPlayer.getMaxSubzoneDepth());
        lm.Limits_MaxRents.sendMessage(sender, rPlayer.getMaxRents() + (getMaxRentDays() != -1 ? lm.Limits_MaxRentDays.getMessage(getMaxRentDays()) : ""));
        lm.Limits_EnterLeave.sendMessage(sender, group.messageperms);
        if (Residence.getInstance().getEconomyManager() != null) {
            lm.Limits_Cost.sendMessage(sender, group.costperarea);
            lm.Limits_Sell.sendMessage(sender, group.sellperarea);
        }
        lm.Limits_Flag.sendMessage(sender, group.flagPerms.listFlags());
        if (Residence.getInstance().getConfigManager().useLeases()) {
            lm.Limits_MaxDays.sendMessage(sender, group.maxLeaseTime);
            lm.Limits_LeaseTime.sendMessage(sender, group.leaseGiveTime);
            lm.Limits_RenewCost.sendMessage(sender, group.renewcostperarea);
        }
        lm.General_Separator.sendMessage(sender);
    }

    public double getCostperarea() {
        return costperarea;
    }

    public double getSellperarea() {
        return sellperarea;
    }

    @Deprecated
    public int getXmin() {
        return xmin;
    }

    @Deprecated
    public int getXmax() {
        return xmax;
    }

    @Deprecated
    public int getZmin() {
        return zmin;
    }

    @Deprecated
    public int getYmin() {
        return getMinYSize();
    }

    @Deprecated
    public int getYmax() {
        return getMaxYSize();
    }

    @Deprecated
    public int getZmax() {
        return zmax;
    }

    public static @Nullable PermissionGroup getGroup(@NotNull Player player) {
        return getGroup(player.getUniqueId(), null);
    }

    public static @Nullable PermissionGroup getGroup(@NotNull UUID uuid) {
        return getGroup(uuid, null);
    }

    public static @Nullable PermissionGroup getGroup(@NotNull UUID uuid, @Nullable String world) {
        ResidencePlayer player = ResidencePlayer.get(uuid);
        
        if (player == null) 
            return null;
        
        if (world == null)
            return player.getGroup();
        
        return player.getGroup(world);
    }
}
