package com.bekvon.bukkit.residence.protection;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.commands.padd;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.MinimizeMessages;
import com.bekvon.bukkit.residence.containers.RandomLoc;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.economy.rent.RentedLand;
import com.bekvon.bukkit.residence.event.ResidenceAreaAddEvent;
import com.bekvon.bukkit.residence.event.ResidenceAreaDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.event.ResidenceSizeChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceSubzoneCreationEvent;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.itemlist.ItemList.ListType;
import com.bekvon.bukkit.residence.itemlist.ResidenceItemList;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.raid.ResidenceRaid;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.utils.LocationCheck;
import com.bekvon.bukkit.residence.utils.LocationUtil;
import com.bekvon.bukkit.residence.utils.LocationValidity;
import com.bekvon.bukkit.residence.utils.SafeLocationCache;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.Logs.CMIDebug;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.TitleMessages.CMITitleMessage;
import net.Zrips.CMILib.Version.Version;
import net.Zrips.CMILib.Version.PaperMethods.CMIChunkSnapShot;
import net.Zrips.CMILib.Version.PaperMethods.PaperLib;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Teleporters.CMITeleporter;

public class ClaimedResidence {

    private String resName = null;
    protected ClaimedResidence parent;
    protected Map<String, CuboidArea> areas;
    protected Map<String, ClaimedResidence> subzones;
    protected ResidencePermissions perms;
    protected ResidenceBank bank;
    protected double BlockSellPrice = 0.0;
    public Vector tpLoc;
    public Vector PitchYaw;
    protected String enterMessage = null;
    protected String leaveMessage = null;
    protected String ShopDesc = null;
    protected String ChatPrefix = "";
    protected CMIChatColor ChannelColor = CMIChatColor.WHITE;
    protected ResidenceItemList ignorelist;
    protected ResidenceItemList blacklist;
    protected boolean mainRes = false;
    protected long createTime = 0L;

    private long leaseExpireTime = 0;

    protected List<String> cmdWhiteList = new ArrayList<String>();
    protected List<String> cmdBlackList = new ArrayList<String>();

    List<ShopVote> ShopVoteList = new ArrayList<ShopVote>();

    protected RentableLand rentableland = null;
    protected RentedLand rentedland = null;

    protected int sellPrice = -1;

    private ResidenceRaid raid;

    private Set<Signs> signsInResidence = Collections.synchronizedSet(new HashSet<Signs>());

    public String getResidenceName() {
        return resName;
    }

    public void setName(String name) {
        if (name.contains("."))
            resName = name.split("\\.")[name.split("\\.").length - 1];
        else
            resName = name;
    }

    public void setCreateTime() {
        createTime = System.currentTimeMillis();
    }

    public long getCreateTime() {
        return createTime;
    }

    public Integer getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Integer amount) {
        sellPrice = amount;
    }

    public boolean isForSell() {
        return Residence.getInstance().getTransactionManager().isForSale(this.getName());
    }

    public boolean isForRent() {
        return Residence.getInstance().getRentManager().isForRent(this);
    }

    public boolean isSubzoneForRent() {
        for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
            if (one.getValue().isForRent())
                return true;
            if (one.getValue().isSubzoneForRent())
                return true;
        }
        return false;
    }

    public boolean isSubzoneRented() {
        for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
            if (one.getValue().isRented())
                return true;
            if (one.getValue().isSubzoneRented())
                return true;
        }
        return false;
    }

    public ClaimedResidence getRentedSubzone() {
        for (Entry<String, ClaimedResidence> one : subzones.entrySet()) {
            if (one.getValue().isRented())
                return one.getValue();
            if (one.getValue().getRentedSubzone() != null)
                return one.getValue().getRentedSubzone();
        }
        return null;
    }

    public boolean isParentForRent() {
        if (this.getParent() != null)
            return this.getParent().isForRent() ? true : this.getParent().isParentForRent();
        return false;
    }

    public boolean isParentForSell() {
        if (this.getParent() != null)
            return this.getParent().isForSell() ? true : this.getParent().isParentForSell();
        return false;
    }

    public boolean isRented() {
        return Residence.getInstance().getRentManager().isRented(this);
    }

    public void setRentable(RentableLand rl) {
        this.rentableland = rl;
    }

    public RentableLand getRentable() {
        return this.rentableland;
    }

    public void setRented(RentedLand rl) {
        this.rentedland = rl;
    }

    public RentedLand getRentedLand() {
        return this.rentedland;
    }

    public ClaimedResidence() {
        initialize();
    }

    public ClaimedResidence(String creationWorld) {
        this(Residence.getInstance().getServerLandName(), creationWorld);
    }

    public ClaimedResidence(String creator, UUID uuid, String creationWorld) {
        perms = new ResidencePermissions(this, creator, uuid, creationWorld);
        initialize();
    }

    @Deprecated
    public ClaimedResidence(String creator, String creationWorld) {
        perms = new ResidencePermissions(this, creator, creationWorld);
        initialize();
    }

    public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence) {
        this(creator, creationWorld);
        parent = parentResidence;
    }

    private void initialize() {
        subzones = new HashMap<>();
        areas = new HashMap<>();
        bank = new ResidenceBank(this);
        blacklist = new ResidenceItemList(Residence.getInstance(), this, ListType.BLACKLIST);
        ignorelist = new ResidenceItemList(Residence.getInstance(), this, ListType.IGNORELIST);
    }

    public boolean isMainResidence() {
        return mainRes;
    }

    public void setMainResidence(boolean state) {
        mainRes = state;
    }

    public boolean isSubzone() {
        return getParent() == null ? false : true;
    }

    public int getSubzoneDeep() {
        return getSubzoneDeep(0);
    }

    public int getSubzoneDeep(int deep) {
        deep++;
        if (getParent() != null) {
            return getParent().getSubzoneDeep(deep);
        }
        return deep;
    }

    public boolean isBiggerThanMin(Player player, CuboidArea area, boolean resadmin) {
        if (resadmin)
            return true;
        if (player == null)
            return true;
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (area.getXSize() < group.getMinX()) {
            Residence.getInstance().msg(player, lm.Area_ToSmallX, area.getXSize(), group.getMinX());
            return false;
        }

        if (area.getYSize() < group.getMinY()) {
            Residence.getInstance().msg(player, lm.Area_ToSmallY, area.getYSize(), group.getMinY());
            return false;
        }

        if (area.getZSize() < group.getMinZ()) {
            Residence.getInstance().msg(player, lm.Area_ToSmallZ, area.getZSize(), group.getMinZ());
            return false;
        }
        return true;
    }

    public boolean isBiggerThanMinSubzone(Player player, CuboidArea area, boolean resadmin) {
        if (resadmin)
            return true;
        if (player == null)
            return true;
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (area.getXSize() < group.getSubzoneMinX()) {
            Residence.getInstance().msg(player, lm.Area_ToSmallX, area.getXSize(), group.getSubzoneMinX());
            return false;
        }
        if (area.getYSize() < group.getSubzoneMinY()) {
            Residence.getInstance().msg(player, lm.Area_ToSmallY, area.getYSize(), group.getSubzoneMinY());
            return false;
        }
        if (area.getZSize() < group.getSubzoneMinZ()) {
            Residence.getInstance().msg(player, lm.Area_ToSmallZ, area.getZSize(), group.getSubzoneMinZ());
            return false;
        }
        return true;
    }

    public boolean isSmallerThanMax(Player player, CuboidArea area, boolean resadmin) {
        if (resadmin)
            return true;
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        if (area.getXSize() > rPlayer.getMaxX()) {
            Residence.getInstance().msg(player, lm.Area_ToBigX, area.getXSize(), rPlayer.getMaxX());
            return false;
        }

        if (!Residence.getInstance().getConfigManager().isSelectionIgnoreY() && area.getYSize() > group.getMaxY()) {
            Residence.getInstance().msg(player, lm.Area_ToBigY, area.getYSize(), group.getMaxY());
            return false;
        }

        if (area.getZSize() > rPlayer.getMaxZ()) {
            Residence.getInstance().msg(player, lm.Area_ToBigZ, area.getZSize(), rPlayer.getMaxZ());
            return false;
        }
        return true;
    }

    public boolean isSmallerThanMaxSubzone(Player player, CuboidArea area, boolean resadmin) {
        if (resadmin)
            return true;
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (area.getXSize() > group.getSubzoneMaxX()) {
            Residence.getInstance().msg(player, lm.Area_ToBigX, area.getXSize(), group.getSubzoneMaxX());
            return false;
        }

        if (area.getYSize() > group.getSubzoneMaxY() + (-group.getMinY())) {
            Residence.getInstance().msg(player, lm.Area_ToBigY, area.getYSize(), group.getSubzoneMaxY());
            return false;
        }
        if (area.getZSize() > group.getSubzoneMaxZ()) {
            Residence.getInstance().msg(player, lm.Area_ToBigZ, area.getZSize(), group.getSubzoneMaxZ());
            return false;
        }
        return true;
    }

    public boolean addArea(CuboidArea area, String name) {
        return addArea(null, area, name, true);
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin) {
        return addArea(player, area, name, resadmin, true);
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin, boolean chargeMoney) {
        if (!Residence.getInstance().validName(name)) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Invalid_NameCharacters);
            }
            return false;
        }

        if (Math.abs(area.getLowVector().getBlockX()) > 30000000 || Math.abs(area.getHighVector().getBlockX()) > 30000000 ||
            Math.abs(area.getLowVector().getBlockZ()) > 30000000 || Math.abs(area.getHighVector().getBlockZ()) > 30000000) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Invalid_Area);
            }
            return false;
        }

        String NName = name;
        name = name.toLowerCase();

        if (areas.containsKey(NName)) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Area_Exists);
            }
            return false;
        }

        if (this.isSubzone() && !isBiggerThanMinSubzone(player, area, resadmin)
            || !this.isSubzone() && !isBiggerThanMin(player, area, resadmin))
            return false;

        if (!resadmin && Residence.getInstance().getConfigManager().getEnforceAreaInsideArea() && this.getParent() == null) {
            boolean inside = false;
            for (CuboidArea are : areas.values()) {
                if (are.isAreaWithinArea(area)) {
                    inside = true;
                }
            }
            if (!inside) {
                Residence.getInstance().msg(player, lm.Subzone_SelectInside);
                return false;
            }
        }
        if (!area.getWorld().getName().equalsIgnoreCase(perms.getWorldName())) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Area_DiffWorld);
            }
            return false;
        }

        if (getParent() == null) {
            String collideResidence = Residence.getInstance().getResidenceManager().checkAreaCollision(area, this);
            ClaimedResidence cRes = Residence.getInstance().getResidenceManager().getByName(collideResidence);
            if (cRes != null) {
                if (player != null) {
                    Residence.getInstance().msg(player, lm.Area_Collision, cRes.getName());
                    Visualizer v = new Visualizer(player);
                    v.setAreas(area);
                    v.setErrorAreas(cRes);
                    Residence.getInstance().getSelectionManager().showBounds(player, v);
                }
                return false;
            }

            if (Residence.getInstance().getConfigManager().getAntiGreefRangeGaps(area.getWorldName()) > 0) {
                Location low = area.getLowLocation().clone();
                Location high = area.getHighLocation().clone();

                int gap = Residence.getInstance().getConfigManager().getAntiGreefRangeGaps(area.getWorldName());
                low.add(-gap, -gap, -gap);
                high.add(gap, gap, gap);

                CuboidArea expanded = new CuboidArea(low, high);

                collideResidence = Residence.getInstance().getResidenceManager().checkAreaCollision(expanded, this, player == null ? null : player.getUniqueId());
                cRes = Residence.getInstance().getResidenceManager().getByName(collideResidence);
                if (cRes != null) {
                    if (player != null) {
                        Residence.getInstance().msg(player, lm.Area_TooClose, gap, cRes.getName());
                        Visualizer v = new Visualizer(player);
                        v.setAreas(area);
                        v.setErrorAreas(cRes);
                        Residence.getInstance().getSelectionManager().showBounds(player, v);
                    }
                    return false;
                }
            }

        } else {
            String[] szs = getParent().listSubzones();
            for (String sz : szs) {
                ClaimedResidence res = getParent().getSubzone(sz);
                if (res != null && res != this) {
                    if (res.checkCollision(area)) {
                        if (player != null) {
                            Residence.getInstance().msg(player, lm.Area_SubzoneCollision, sz);
                        }
                        return false;
                    }
                }
            }
        }
        if (!resadmin && player != null) {
            if (!this.perms.hasResidencePermission(player, true)) {
                Residence.getInstance().msg(player, lm.General_NoPermission);
                return false;
            }
            if (getParent() != null) {
                if (!getParent().containsLoc(area.getHighLocation()) || !getParent().containsLoc(area.getLowLocation())) {
                    Residence.getInstance().msg(player, lm.Area_NotWithinParent);
                    return false;
                }
                if (!getParent().getPermissions().hasResidencePermission(player, true)
                    && !getParent().getPermissions().playerHas(player, Flags.subzone, FlagCombo.OnlyTrue)) {
                    Residence.getInstance().msg(player, lm.Residence_ParentNoPermission);
                    return false;
                }
            }

            ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);

            PermissionGroup group = rPlayer.getGroup();
            if (!this.isSubzone() && !group.canCreateResidences() && !ResPerm.create.hasPermission(player, true)
                || this.isSubzone() && !group.canCreateResidences()
                    && !ResPerm.create_subzone.hasPermission(player, true)) {
                return false;
            }

            if (areas.size() >= group.getMaxPhysicalPerResidence()) {
                Residence.getInstance().msg(player, lm.Area_MaxPhysical);
                return false;
            }

            if (!this.isSubzone() && !isSmallerThanMax(player, area, resadmin)
                || this.isSubzone() && !isSmallerThanMaxSubzone(player, area, resadmin)) {
                Residence.getInstance().msg(player, lm.Area_SizeLimit);
                return false;
            }

            if (group.getMinHeight() > area.getLowVector().getBlockY()) {
                Residence.getInstance().msg(player, lm.Area_LowLimit, String.format("%d", group.getMinHeight()));
                return false;
            }

            if (group.getMaxHeight() < area.getHighVector().getBlockY()) {
                Residence.getInstance().msg(player, lm.Area_HighLimit, String.format("%d", group.getMaxHeight()));
                return false;
            }

            if (!resadmin) {
                if (Residence.getInstance().getWorldGuard() != null && Residence.getInstance().getWorldGuardUtil().isSelectionInArea(player))
                    return false;

                if (Residence.getInstance().getKingdomsManager() != null && Residence.getInstance().getKingdomsUtil().isSelectionInArea(player))
                    return false;
            }

            if (Residence.getInstance().getConfigManager().isChargeOnAreaAdd() && chargeMoney && getParent() == null && Residence.getInstance().getConfigManager().enableEconomy() && !resadmin) {
                double chargeamount = area.getCost(group);
                if (!Residence.getInstance().getTransactionManager().chargeEconomyMoney(player, chargeamount)) {
                    return false;
                }
            }
        }

        ResidenceAreaAddEvent resevent = new ResidenceAreaAddEvent(player, NName, this, area);
        Residence.getInstance().getServ().getPluginManager().callEvent(resevent);
        if (resevent.isCancelled())
            return false;

        Residence.getInstance().getResidenceManager().removeChunkList(this.getName());
        areas.put(name, area);
        Residence.getInstance().getResidenceManager().calculateChunks(this.getName());
        return true;
    }

    public boolean replaceArea(CuboidArea neware, String name) {
        return this.replaceArea(null, neware, name, true);
    }

    public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {

        if (!areas.containsKey(name)) {
            if (player != null)
                Residence.getInstance().msg(player, lm.Area_NonExist);
            return false;
        }
        CuboidArea oldarea = areas.get(name);
        if (!newarea.getWorld().getName().equalsIgnoreCase(perms.getWorldName())) {
            if (player != null)
                Residence.getInstance().msg(player, lm.Area_DiffWorld);
            return false;
        }
        if (getParent() == null) {
            String collideResidence = Residence.getInstance().getResidenceManager().checkAreaCollision(newarea, this);
            ClaimedResidence cRes = Residence.getInstance().getResidenceManager().getByName(collideResidence);
            if (cRes != null && player != null) {
                Residence.getInstance().msg(player, lm.Area_Collision, cRes.getName());
                Visualizer v = new Visualizer(player);
                v.setAreas(this.getAreaArray());
                v.setErrorAreas(cRes.getAreaArray());
                Residence.getInstance().getSelectionManager().showBounds(player, v);
                return false;
            }

            if (Residence.getInstance().getConfigManager().getAntiGreefRangeGaps(newarea.getWorldName()) > 0) {
                Location low = newarea.getLowLocation().clone();
                Location high = newarea.getHighLocation().clone();

                int gap = Residence.getInstance().getConfigManager().getAntiGreefRangeGaps(newarea.getWorldName());
                low.add(-gap, -gap, -gap);
                high.add(gap, gap, gap);

                CuboidArea expanded = new CuboidArea(low, high);

                collideResidence = Residence.getInstance().getResidenceManager().checkAreaCollision(expanded, this, player == null ? null : player.getUniqueId());
                cRes = Residence.getInstance().getResidenceManager().getByName(collideResidence);
                if (cRes != null) {
                    if (player != null) {
                        Residence.getInstance().msg(player, lm.Area_TooClose, gap, cRes.getName());
                        Visualizer v = new Visualizer(player);
                        v.setAreas(getAreaArray());
                        v.setErrorAreas(cRes);
                        Residence.getInstance().getSelectionManager().showBounds(player, v);
                    }
                    return false;
                }
            }
        } else {
            String[] szs = getParent().listSubzones();
            for (String sz : szs) {
                ClaimedResidence res = getParent().getSubzone(sz);
                if (res != null && res != this) {
                    if (res.checkCollision(newarea)) {
                        if (player != null) {
                            Residence.getInstance().msg(player, lm.Area_SubzoneCollision, sz);
                            Visualizer v = new Visualizer(player);
                            v.setErrorAreas(res.getAreaArray());
                            Residence.getInstance().getSelectionManager().showBounds(player, v);
                        }
                        return false;
                    }
                }
            }
        }
        // Don't remove subzones that are not in the area anymore, show colliding areas
        String[] szs = listSubzones();
        for (String sz : szs) {
            ClaimedResidence res = getSubzone(sz);
            if (res == null || res == this)
                continue;
            String[] szareas = res.getAreaList();
            for (String area : szareas) {
                if (newarea.isAreaWithinArea(res.getArea(area)))
                    continue;

                boolean good = false;
                for (CuboidArea arae : getAreaArray()) {
                    if (arae != oldarea && arae.isAreaWithinArea(res.getArea(area))) {
                        good = true;
                    }
                }
                if (!good) {
                    Residence.getInstance().msg(player, lm.Area_Collision, res.getName());
                    Visualizer v = new Visualizer(player);
                    v.setAreas(this.getAreaArray());
                    v.setErrorAreas(res.getAreaArray());
                    Residence.getInstance().getSelectionManager().showBounds(player, v);
                    return false;
                }

            }
            if (res.getAreaArray().length == 0) {
                removeSubzone(sz);
            }
        }

        if (!resadmin && player != null) {

            if (!getPermissions().hasResidencePermission(player, true) && !getPermissions().playerHas(player, Flags.admin, FlagCombo.OnlyTrue)) {
                Residence.getInstance().msg(player, lm.General_NoPermission);
                return false;
            }
            if (getParent() != null) {
                if (!getParent().containsLoc(newarea.getHighLocation()) || !getParent().containsLoc(newarea.getLowLocation())) {
                    Residence.getInstance().msg(player, lm.Area_NotWithinParent);
                    return false;
                }
                if (!getParent().getPermissions().hasResidencePermission(player, true)
                    && !getParent().getPermissions().playerHas(player, Flags.subzone, FlagCombo.OnlyTrue)) {
                    Residence.getInstance().msg(player, lm.Residence_ParentNoPermission);
                    return false;
                }
            }
            ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();
            if (!group.canCreateResidences() && !ResPerm.resize.hasPermission(player, true)) {
                return false;
            }

            if (oldarea.getSize() < newarea.getSize()
                && (!this.isSubzone() && !isSmallerThanMax(player, newarea, resadmin)
                    || this.isSubzone() && !isSmallerThanMaxSubzone(player, newarea, resadmin))) {
                Residence.getInstance().msg(player, lm.Area_SizeLimit);
                return false;
            }
            if (group.getMinHeight() > newarea.getLowVector().getBlockY()) {
                Residence.getInstance().msg(player, lm.Area_LowLimit, String.format("%d", group.getMinHeight()));
                return false;
            }
            if (group.getMaxHeight() < newarea.getHighVector().getBlockY()) {
                Residence.getInstance().msg(player, lm.Area_HighLimit, String.format("%d", group.getMaxHeight()));
                return false;
            }

            if (!isBiggerThanMin(player, newarea, resadmin))
                return false;

            if (!resadmin) {
                if (Residence.getInstance().getWorldGuard() != null && Residence.getInstance().getWorldGuardUtil().isSelectionInArea(player))
                    return false;
                if (Residence.getInstance().getKingdomsManager() != null && Residence.getInstance().getKingdomsUtil().isSelectionInArea(player))
                    return false;
            }

            if (Residence.getInstance().getConfigManager().isChargeOnExpansion() && getParent() == null && Residence.getInstance().getConfigManager().enableEconomy() && !resadmin) {
                double chargeamount = newarea.getCost(group) - oldarea.getCost(group);
                if (chargeamount > 0 && !Residence.getInstance().getTransactionManager().chargeEconomyMoney(player, chargeamount)) {
                    return false;
                }
            }
        }

        ResidenceSizeChangeEvent resevent = new ResidenceSizeChangeEvent(player, this, oldarea, newarea);
        Residence.getInstance().getServ().getPluginManager().callEvent(resevent);
        if (resevent.isCancelled())
            return false;

        if ((!resadmin) && (player != null)) {
            int chargeamount = (int) Math
                .ceil((newarea.getSize() - oldarea.getSize()) * getBlockSellPrice().doubleValue());
            if ((chargeamount < 0) && (Residence.getInstance().getConfigManager().useResMoneyBack())) {
                if (!this.isServerLand())
                    Residence.getInstance().getTransactionManager().giveEconomyMoney(player, -chargeamount);
            }
        }

        Residence.getInstance().getResidenceManager().removeChunkList(this.getName());
        areas.remove(name);
        areas.put(name, newarea);
        Residence.getInstance().getResidenceManager().calculateChunks(this.getName());
        if (player != null)
            Residence.getInstance().msg(player, lm.Area_Update);
        return true;
    }

    public boolean addSubzone(String name, Location loc1, Location loc2) {
        return this.addSubzone(null, loc1, loc2, name, true);
    }

    public boolean addSubzone(Player player, Location loc1, Location loc2, String name, boolean resadmin) {
        if (player == null) {
            return this.addSubzone(null, Residence.getInstance().getServerLandName(), loc1, loc2, name, resadmin);
        }
        return this.addSubzone(player, player.getName(), loc1, loc2, name, resadmin);
    }

    public boolean isServerLand() {
        return this.getOwnerUUID().toString().equals(Residence.getInstance().getServerLandUUID());
    }

    public boolean addSubzone(Player player, String name, boolean resadmin) {
        if (Residence.getInstance().getSelectionManager().hasPlacedBoth(player)) {
            Location loc1 = Residence.getInstance().getSelectionManager().getPlayerLoc1(player);
            Location loc2 = Residence.getInstance().getSelectionManager().getPlayerLoc2(player);
            return this.addSubzone(player, player.getName(), loc1, loc2, name, resadmin);
        }
        return false;
    }

    public boolean addSubzone(Player player, String owner, Location loc1, Location loc2, String name,
        boolean resadmin) {
        if (!Residence.getInstance().validName(name)) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Invalid_NameCharacters);
            }
            return false;
        }
        if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Subzone_SelectInside);
            }
            return false;
        }

        String NName = name;
        name = name.toLowerCase();

        if (subzones.containsKey(name)) {
            if (player != null) {
                Residence.getInstance().msg(player, lm.Subzone_Exists, NName);
            }
            return false;
        }
        if (!resadmin && player != null) {
            if (!this.perms.hasResidencePermission(player, true)) {
                if (!this.perms.playerHas(player.getName(), Flags.subzone,
                    this.perms.playerHas(player, Flags.admin, false))) {
                    Residence.getInstance().msg(player, lm.General_NoPermission);
                    return false;
                }
            }

            if (this.getSubzoneList().length >= Residence.getInstance().getPlayerManager().getResidencePlayer(owner).getMaxSubzones()) {
                Residence.getInstance().msg(player, lm.Subzone_MaxAmount);
                return false;
            }

            if (this.getZoneDepth() >= Residence.getInstance().getPlayerManager().getResidencePlayer(owner).getMaxSubzoneDepth()) {
                Residence.getInstance().msg(player, lm.Subzone_MaxDepth);
                return false;
            }
        }

        CuboidArea newArea = new CuboidArea(loc1, loc2);

        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        for (Entry<String, ClaimedResidence> resEntry : set) {
            ClaimedResidence res = resEntry.getValue();
            if (res.checkCollision(newArea)) {
                if (player != null) {
                    Residence.getInstance().msg(player, lm.Subzone_Collide, resEntry.getKey());
                    Visualizer v = new Visualizer(player);
                    v.setAreas(newArea);
                    v.setErrorAreas(res);
                    Residence.getInstance().getSelectionManager().showBounds(player, v);
                }
                return false;
            }
        }

        ClaimedResidence newres;
        if (player != null) {
            newres = new ClaimedResidence(owner, perms.getWorldName(), this);
            newres.addArea(player, newArea, NName, resadmin);
        } else {
            newres = new ClaimedResidence(owner, perms.getWorldName(), this);
            newres.addArea(newArea, NName);
        }

        if (newres.getAreaCount() != 0) {
            newres.getPermissions().applyDefaultFlags();
            if (player != null) {
                ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
                PermissionGroup group = rPlayer.getGroup();
                newres.setEnterMessage(group.getDefaultEnterMessage());
                newres.setLeaveMessage(group.getDefaultLeaveMessage());
            }
            if (Residence.getInstance().getConfigManager().flagsInherit()) {
                newres.getPermissions().setParent(perms);
            }

            newres.resName = NName;

            newres.setCreateTime();

            ResidenceSubzoneCreationEvent resevent = new ResidenceSubzoneCreationEvent(player, NName, newres, newArea);
            Residence.getInstance().getServ().getPluginManager().callEvent(resevent);
            if (resevent.isCancelled())
                return false;

            subzones.put(name, newres);
            if (player != null) {
                Residence.getInstance().msg(player, lm.Area_Create, NName);
                Residence.getInstance().msg(player, lm.Subzone_Create, NName);
            }
            return true;
        }
        if (player != null) {
            Residence.getInstance().msg(player, lm.Subzone_CreateFail, NName);
        }
        return false;
    }

    public ClaimedResidence getSubzoneByLoc(Location loc) {
        for (Entry<String, ClaimedResidence> entry : subzones.entrySet()) {
            if (!entry.getValue().containsLoc(loc))
                continue;
            ClaimedResidence subrez = entry.getValue().getSubzoneByLoc(loc);
            return subrez == null ? entry.getValue() : subrez;
        }
        return null;
    }

    public ClaimedResidence getSubzone(String subzonename) {
        subzonename = subzonename.toLowerCase();

        if (!subzonename.contains(".")) {
            return subzones.get(subzonename);
        }
        String split[] = subzonename.split("\\.");
        ClaimedResidence get = subzones.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (get == null) {
                return null;
            }
            get = get.getSubzone(split[i]);
        }
        return get;
    }

    public String getSubzoneNameByRes(ClaimedResidence res) {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        for (Entry<String, ClaimedResidence> entry : set) {
            if (entry.getValue() == res) {
                return entry.getValue().getResidenceName();
            }
            String n = entry.getValue().getSubzoneNameByRes(res);
            if (n != null) {
                return entry.getValue().getResidenceName() + "." + n;
            }
        }
        return null;
    }

    public String[] getSubzoneList() {
        ArrayList<String> zones = new ArrayList<>();
        Set<String> set = subzones.keySet();
        for (String key : set) {
            if (key != null) {
                zones.add(key);
            }
        }
        return zones.toArray(new String[zones.size()]);
    }

    public boolean checkCollision(CuboidArea area) {
        for (CuboidArea checkarea : areas.values()) {
            if (checkarea != null && checkarea.checkCollision(area)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsLoc(Location loc) {
        for (CuboidArea key : areas.values()) {
            if (key.containsLoc(loc)) {
                if (getParent() != null)
                    return getParent().containsLoc(loc);
                return true;
            }
        }
        return false;
    }

    public ClaimedResidence getParent() {
        return parent;
    }

    public String getTopParentName() {
        return this.getTopParent().getName();
    }

    public ClaimedResidence getTopParent() {
        if (getParent() == null)
            return this;
        return getParent().getTopParent();
    }

    public boolean isTopArea() {
        return getParent() == null;
    }

    public boolean removeSubzone(String name) {
        return this.removeSubzone(null, name, true);
    }

    public boolean removeSubzone(Player player, String name, boolean resadmin) {
        if (name == null)
            return false;
        name = name.toLowerCase();
        ClaimedResidence res = subzones.get(name);
        if (player != null && !res.perms.hasResidencePermission(player, true) && !resadmin) {
            Residence.getInstance().msg(player, lm.General_NoPermission);
            return false;
        }
        subzones.remove(name);
        if (player != null) {
            Residence.getInstance().msg(player, lm.Subzone_Remove, name);
        }
        return true;
    }

    public long getTotalSize() {
        Collection<CuboidArea> set = areas.values();
        long size = 0;
        if (!Residence.getInstance().getConfigManager().isNoCostForYBlocks())
            for (CuboidArea entry : set) {
                size += entry.getSize();
            }
        else
            for (CuboidArea entry : set) {
                size += (entry.getXSize() * entry.getZSize());
            }
        return size;
    }

    public long getXZSize() {
        Collection<CuboidArea> set = areas.values();
        long size = 0;
        for (CuboidArea entry : set) {
            size = size + (entry.getXSize() * entry.getZSize());
        }
        return size;
    }

    public CuboidArea[] getAreaArray() {
        return areas.values().toArray(new CuboidArea[0]);
    }

    public Map<String, CuboidArea> getAreaMap() {
        return areas;
    }

    public ResidencePermissions getPermissions() {
        return perms;
    }

    public String getEnterMessage() {
        return enterMessage;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public String getShopDesc() {
        return ShopDesc;
    }

    public void setEnterMessage(String message) {
        enterMessage = message;
    }

    public void setLeaveMessage(String message) {
        leaveMessage = message;
    }

    public void setShopDesc(String message) {
        ShopDesc = message;
    }

    public void setEnterLeaveMessage(CommandSender sender, String message, boolean enter, boolean resadmin) {
        if (message != null) {
            message = message.replace("%subtitle%", "\\n");
        }

        if (sender instanceof Player) {
            ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer((Player) sender);
            PermissionGroup group = rPlayer.getGroup();
            if (!group.canSetEnterLeaveMessages() && !resadmin) {
                Residence.getInstance().msg(sender, lm.Residence_OwnerNoPermission);
                return;
            }
            if (!perms.hasResidencePermission(sender, false) && !resadmin) {
                Residence.getInstance().msg(sender, lm.General_NoPermission);
                return;
            }
        }
        if (enter) {
            this.setEnterMessage(message);
        } else {
            this.setLeaveMessage(message);
        }
        Residence.getInstance().msg(sender, lm.Residence_MessageChange);
    }

    public CuboidArea getMainArea() {
        CuboidArea area = areas.get(this.isSubzone() ? this.getResidenceName() : "main");
        if (area == null && !areas.isEmpty()) {
            return areas.entrySet().iterator().next().getValue();
        }
        return area;
    }

    public CuboidArea getAreaByLoc(Location loc) {
        for (CuboidArea thisarea : areas.values()) {
            if (thisarea.containsLoc(loc)) {
                return thisarea;
            }
        }
        return null;
    }

    public String[] listSubzones() {
        String list[] = new String[subzones.size()];
        int i = 0;
        for (String res : subzones.keySet()) {
            list[i] = res;
            i++;
        }
        return list;
    }

    public List<ClaimedResidence> getSubzones() {
        List<ClaimedResidence> list = new ArrayList<ClaimedResidence>();
        for (Entry<String, ClaimedResidence> res : subzones.entrySet()) {
            list.add(res.getValue());
        }
        return list;
    }

    public Map<String, ClaimedResidence> getSubzonesMap() {
        return subzones;
    }

    public int getSubzonesAmount(Boolean includeChild) {
        int i = 0;
        for (Entry<String, ClaimedResidence> res : subzones.entrySet()) {
            i++;
            if (includeChild)
                i += res.getValue().getSubzonesAmount(includeChild);
        }
        return i;
    }

    public void printSubzoneList(CommandSender sender, int page) {

        PageInfo pi = new PageInfo(6, subzones.size(), page);

        if (!pi.isPageOk()) {
            sender.sendMessage(ChatColor.RED + Residence.getInstance().msg(lm.Invalid_Page));
            return;
        }

        Residence.getInstance().msg(sender, lm.InformationPage_TopSingle, Residence.getInstance().msg(lm.General_Subzones));
        Residence.getInstance().msg(sender, lm.InformationPage_Page, Residence.getInstance().msg(lm.General_GenericPages, String.format("%d", page),
            pi.getTotalPages(), pi.getTotalEntries()));
        RawMessage rm = new RawMessage();
        for (int i = pi.getStart(); i <= pi.getEnd(); i++) {
            ClaimedResidence res = getSubzones().get(i);
            if (res == null)
                continue;
            rm.addText(ChatColor.GREEN + res.getResidenceName() + ChatColor.YELLOW + " - " + Residence.getInstance().msg(lm.General_Owner, res.getOwner()))
                .addHover("Teleport to " + res.getName())
                .addCommand("res tp " + res.getName());
            rm.show(sender);
            rm.clear();
        }
        pi.autoPagination(sender, "res sublist " + this.getName());
    }

    public void printAreaList(Player player, int page) {
        ArrayList<String> temp = new ArrayList<>();
        for (String area : areas.keySet()) {
            temp.add(area);
        }
        Residence.getInstance().getInfoPageManager().printInfo(player, "res area list " + this.getName(),
            Residence.getInstance().msg(lm.General_PhysicalAreas), temp, page);
    }

    public void printAdvancedAreaList(Player player, int page) {
        ArrayList<String> temp = new ArrayList<>();
        for (Entry<String, CuboidArea> entry : areas.entrySet()) {
            CuboidArea a = entry.getValue();
            Location h = a.getHighLocation();
            Location l = a.getLowLocation();
            if (this.getPermissions().has(Flags.coords, FlagCombo.OnlyFalse))
                temp.add(Residence.getInstance().msg(lm.Area_ListAll, entry.getKey(), 0, 0, 0, 0, 0, 0, a.getSize()));
            else
                temp.add(Residence.getInstance().msg(lm.Area_ListAll, entry.getKey(), h.getBlockX(), h.getBlockY(), h.getBlockZ(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), a.getSize()));
        }
        Residence.getInstance().getInfoPageManager().printInfo(player, "res area listall " + this.getName(),
            Residence.getInstance().msg(lm.General_PhysicalAreas), temp, page);
    }

    public String[] getAreaList() {
        String arealist[] = new String[areas.size()];
        int i = 0;
        for (Entry<String, CuboidArea> entry : areas.entrySet()) {
            arealist[i] = entry.getKey();
            i++;
        }
        return arealist;
    }

    public int getZoneDepth() {
        int count = 0;
        ClaimedResidence res = getParent();
        while (res != null) {
            count++;
            res = res.getParent();
        }
        return count;
    }

    @Deprecated
    public Location getTeleportLocation(Player player) {
        return getTeleportLocation(player, true);
    }

    public Location getTeleportLocation(Player player, boolean toSpawnOnFail) {

        if (tpLoc == null || this.getMainArea() != null && !this.containsLoc(new Location(this.getMainArea().getWorld(), tpLoc.getX(), tpLoc.getY(), tpLoc.getZ()))) {

            if (this.getMainArea() == null)
                return null;
            Vector low = this.getMainArea().getLowVector();
            Vector high = this.getMainArea().getHighVector();

            Location t = new Location(this.getMainArea().getWorld(), (low.getBlockX() + high.getBlockX()) / 2,
                (low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high.getBlockZ()) / 2);
            tpLoc = t.toVector();
        }

        if (tpLoc == null)
            return null;

        Location loc = tpLoc.toLocation(this.getMainArea().getLowLocation().getWorld());
        if (PitchYaw != null) {
            loc.setPitch((float) PitchYaw.getX());
            loc.setYaw((float) PitchYaw.getY());
        }
        return loc;
    }

    public CompletableFuture<Location> getTeleportLocationASYNC(Player player, boolean toSpawnOnFail) {
        return LocationUtil.getTeleportLocationASYNC(this, player, toSpawnOnFail);
    }

    public void setTpLoc(Player player, boolean resadmin) {
        if (!this.perms.hasResidencePermission(player, false) && !resadmin) {
            Residence.getInstance().msg(player, lm.General_NoPermission);
            return;
        }
        if (!this.containsLoc(player.getLocation())) {
            Residence.getInstance().msg(player, lm.Residence_NotIn);
            return;
        }

        tpLoc = player.getLocation().toVector();
        PitchYaw = new Vector(player.getLocation().getPitch(), player.getLocation().getYaw(), 0);
        Residence.getInstance().msg(player, lm.Residence_SetTeleportLocation);
    }

    public void tpToResidence(final Player reqPlayer, final Player targetPlayer, final boolean resadmin) {

        boolean isAdmin = Residence.getInstance().isResAdminOn(reqPlayer) || resadmin;

        if (this.getRaid().isRaidInitialized()) {
            if (this.getRaid().isAttacker(targetPlayer) || this.getRaid().isDefender(targetPlayer) && !ConfigManager.RaidDefenderTeleport || !isAdmin) {
                Residence.getInstance().msg(reqPlayer, lm.Raid_cantDo);
                return;
            }
        } else {
            if (!isAdmin && !ResPerm.bypass_tp.hasPermission(reqPlayer, 10000L) && !ResPerm.admin_tp.hasPermission(reqPlayer, 10000L)
                && (!this.isOwner(targetPlayer) || this.isOwner(targetPlayer) && Residence.getInstance().getConfigManager().isCanTeleportIncludeOwner())) {
                ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(reqPlayer);
                PermissionGroup group = rPlayer.getGroup();
                if (!group.hasTpAccess()) {
                    Residence.getInstance().msg(reqPlayer, lm.General_TeleportDeny);
                    return;
                }
                if (!reqPlayer.equals(targetPlayer)) {
                    Residence.getInstance().msg(reqPlayer, lm.General_NoPermission);
                    return;
                }
                if (!this.perms.playerHas(reqPlayer, Flags.tp, FlagCombo.TrueOrNone)) {
                    Residence.getInstance().msg(reqPlayer, lm.Residence_TeleportNoFlag);
                    return;
                }
                if (!this.perms.playerHas(reqPlayer, Flags.move, FlagCombo.TrueOrNone)) {
                    Residence.getInstance().msg(reqPlayer, lm.Residence_MoveDeny, this.getName());
                    return;
                }
            }
        }

        SafeLocationCache old = Residence.getInstance().getTeleportMap().get(targetPlayer.getUniqueId());

        if (Bukkit.getWorld(this.getPermissions().getWorldName()) == null)
            return;

        if (old == null || !old.getResidence().equals(this)) {
            CompletableFuture<LocationCheck> future = LocationUtil.isSafeTeleportASYNC(this, reqPlayer);
            future.thenAccept(result -> {
                validityCheck(reqPlayer, targetPlayer, resadmin, result);
                return;
            });

            return;
        }

        continueTeleport(reqPlayer, targetPlayer, resadmin);
        Residence.getInstance().getTeleportMap().remove(targetPlayer.getUniqueId());
    }

    private void validityCheck(Player reqPlayer, final Player targetPlayer, boolean resadmin, LocationCheck result) {
        SafeLocationCache safety = new SafeLocationCache(this);
        safety.setValidity(result);
        Residence.getInstance().getTeleportMap().put(reqPlayer.getUniqueId(), safety);

        if (result.getValidity().equals(LocationValidity.Valid)) {
            continueTeleport(reqPlayer, targetPlayer, resadmin);
            return;
        }

        if (result.getValidity().equals(LocationValidity.Lava))
            lm.General_TeleportConfirmLava.sendMessage(reqPlayer, result.getFallDistance());
        else if (result.getValidity().equals(LocationValidity.Void))
            lm.General_TeleportConfirmVoid.sendMessage(reqPlayer, result.getFallDistance());
        else
            lm.General_TeleportConfirm.sendMessage(reqPlayer, result.getFallDistance());
    }

    private void continueTeleport(Player reqPlayer, final Player targetPlayer, boolean isAdmin) {
        boolean bypassDelay = ResPerm.tpdelaybypass.hasPermission(targetPlayer);

        if (Residence.getInstance().getConfigManager().getTeleportDelay() > 0 && !isAdmin && !bypassDelay) {

            Residence.getInstance().msg(reqPlayer, lm.General_TeleportStarted, this.getName(),
                Residence.getInstance().getConfigManager().getTeleportDelay());
            if (Residence.getInstance().getConfigManager().isTeleportTitleMessage())
                TpTimer(reqPlayer, Residence.getInstance().getConfigManager().getTeleportDelay());
            Residence.getInstance().getTeleportDelayMap().add(reqPlayer.getName());
        }

        CompletableFuture<Location> future = this.getTeleportLocationASYNC(targetPlayer, false);

        future.thenAccept(loc -> {
            finalizeTP(loc, reqPlayer, targetPlayer, isAdmin, bypassDelay);
        });
    }

    private void finalizeTP(Location loc, Player reqPlayer, Player targetPlayer, boolean isAdmin, boolean bypassDelay) {

        if (loc == null) {
            Residence.getInstance().msg(reqPlayer, lm.Invalid_Location);
            return;
        }

        if (Math.abs(loc.getBlockX()) > 30000000 || Math.abs(loc.getBlockZ()) > 30000000) {
            Residence.getInstance().msg(reqPlayer, lm.Invalid_Area);
            return;
        }

        CMIScheduler.runTask(Residence.getInstance(), () -> {
            if (Residence.getInstance().getConfigManager().getTeleportDelay() > 0 && !isAdmin && !bypassDelay)
                performDelaydTp(loc, targetPlayer, reqPlayer, true);
            else
                performInstantTp(loc, targetPlayer, reqPlayer, true);
        });
    }

    public void TpTimer(final Player player, final int t) {
        CMITitleMessage.send(player, Residence.getInstance().msg(lm.General_TeleportTitle),
            Residence.getInstance().msg(lm.General_TeleportTitleTime, t));
        CMIScheduler.runTaskLater(Residence.getInstance(), () -> {
            if (!Residence.getInstance().getTeleportDelayMap().contains(player.getName()))
                return;
            if (t > 1)
                TpTimer(player, t - 1);
        }, 20L);
    }

    public void performDelaydTp(final Location targloc, final Player targetPlayer, Player reqPlayer,
        final boolean near) {
        ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
        Residence.getInstance().getServ().getPluginManager().callEvent(tpevent);
        if (tpevent.isCancelled())
            return;

        CMIScheduler.runAtLocationLater(Residence.getInstance(), targloc, () -> {
            if (targloc == null || targetPlayer == null || !targetPlayer.isOnline())
                return;

            if (!Residence.getInstance().getTeleportDelayMap().contains(targetPlayer.getName()) && Residence.getInstance().getConfigManager().getTeleportDelay() > 0)
                return;
            else if (Residence.getInstance().getTeleportDelayMap().contains(targetPlayer.getName()))
                Residence.getInstance().getTeleportDelayMap().remove(targetPlayer.getName());

            targetPlayer.closeInventory();
            CMITeleporter.teleportAsync(targetPlayer, targloc);
            if (near)
                Residence.getInstance().msg(targetPlayer, lm.Residence_TeleportNear);
            else
                Residence.getInstance().msg(targetPlayer, lm.General_TeleportSuccess);

        }, Residence.getInstance().getConfigManager().getTeleportDelay() * 20L);
    }

    private void performInstantTp(final Location targloc, final Player targetPlayer, Player reqPlayer,
        final boolean near) {
        ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
        Residence.getInstance().getServ().getPluginManager().callEvent(tpevent);
        if (tpevent.isCancelled())
            return;

        targetPlayer.closeInventory();

        try {
            if (!Version.isFolia())
                targloc.getChunk().load();
        } catch (Throwable e) {
        }

        if (Version.isAsyncProcessing()) {

            CompletableFuture<Boolean> future = CMITeleporter.teleportAsync(targetPlayer, targloc);
            future.thenAccept(result -> {
                if (result) {
                    if (near)
                        Residence.getInstance().msg(targetPlayer, lm.Residence_TeleportNear);
                    else
                        Residence.getInstance().msg(targetPlayer, lm.General_TeleportSuccess);
                }
            });
            return;
        }

        boolean teleported = targetPlayer.teleport(targloc);
        if (!teleported)
            return;

        if (near)
            Residence.getInstance().msg(targetPlayer, lm.Residence_TeleportNear);
        else
            Residence.getInstance().msg(targetPlayer, lm.General_TeleportSuccess);

    }

    public String getAreaIDbyLoc(Location loc) {
        for (Entry<String, CuboidArea> area : areas.entrySet()) {
            if (area.getValue().containsLoc(loc))
                return area.getKey();
        }
        return null;
    }

    public CuboidArea getCuboidAreabyName(String name) {
        for (Entry<String, CuboidArea> area : areas.entrySet()) {
            if (area.getKey().equals(name))
                return area.getValue();
        }
        return null;
    }

    public void removeArea(String id) {
        Residence.getInstance().getResidenceManager().removeChunkList(this.getName());
        areas.remove(id);
        Residence.getInstance().getResidenceManager().calculateChunks(this.getName());
    }

    public void removeArea(Player player, String id, boolean resadmin) {
        if (this.getPermissions().hasResidencePermission(player, true) || resadmin) {
            if (!areas.containsKey(id)) {
                Residence.getInstance().msg(player, lm.Area_NonExist);
                return;
            }
            if (areas.size() == 1 && !Residence.getInstance().getConfigManager().allowEmptyResidences()) {
                Residence.getInstance().msg(player, lm.Area_RemoveLast);
                return;
            }

            ResidenceAreaDeleteEvent resevent = new ResidenceAreaDeleteEvent(player, this,
                player == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
            Residence.getInstance().getServ().getPluginManager().callEvent(resevent);
            if (resevent.isCancelled())
                return;

            removeArea(id);
            if (player != null)
                Residence.getInstance().msg(player, lm.Area_Remove, id);
        } else {
            if (player != null)
                Residence.getInstance().msg(player, lm.General_NoPermission);
        }
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> areamap = new HashMap<>();

        if (mainRes)
            root.put("MainResidence", mainRes);
        if (createTime != 0L)
            root.put("CreatedOn", createTime);

        if (this.isTopArea() && raid != null && this.getRaid().isUnderRaidCooldown()) {
            root.put("LastRaid", this.getRaid().getEndsAt());
        }

        if (this.isTopArea() && raid != null && this.getRaid().isImmune()) {
            root.put("Immunity", this.getRaid().getImmunityUntil());
        }

//	if (this.getTown() != null && !this.isSubzone()) {
//	    if (this.getTown().getMainResidence().equals(this))
//		root.put("TownCap", this.getTown().getTownName());
//	    else
//		root.put("Town", this.getTown().getTownName());
//	}

        try {
            if (Residence.getInstance().getConfigManager().isNewSaveMechanic()) {
                if (enterMessage != null || leaveMessage != null) {
                    MinimizeMessages min = Residence.getInstance().getResidenceManager().addMessageToTempCache(this.getWorld(), enterMessage,
                        leaveMessage);
                    if (min == null) {
                        if (enterMessage != null)
                            root.put("EnterMessage", enterMessage);
                        if (leaveMessage != null)
                            root.put("LeaveMessage", leaveMessage);
                    } else {
                        if (min.getId() > 0)
                            root.put("Messages", min.getId());
                    }
                }
            } else {
                if (enterMessage != null)
                    root.put("EnterMessage", enterMessage);
                if (leaveMessage != null)
                    root.put("LeaveMessage", leaveMessage);
            }
        } catch (Throwable e) {
            Bukkit.getConsoleSender().sendMessage(Residence.getInstance().getPrefix() + ChatColor.RED + " Failed to save residence (" + getName() + ")!");
            e.printStackTrace();
        }

//	if (enterMessage != null)
//	    root.put("EnterMessage", enterMessage);
//
//	if (leaveMessage != null) {
//	    ResidenceManager mng = Residence.getInstance().getResidenceManager();
//	    Integer id = mng.addLeaveMessageToTempCache(leaveMessage);
//	    root.put("LeaveMessage", id);
//	}

        try {
            if (ShopDesc != null)
                root.put("ShopDescription", ShopDesc);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (bank.getStoredMoneyD() != 0)
            root.put("StoredMoney", bank.getStoredMoneyD());
        if (BlockSellPrice != 0D)
            root.put("BlockSellPrice", BlockSellPrice);

        try {
            if (!ChatPrefix.equals(""))
                root.put("ChatPrefix", ChatPrefix);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            if (!ChannelColor.getName().equalsIgnoreCase(Residence.getInstance().getConfigManager().getChatColor().getName())
                && !ChannelColor.getName().equalsIgnoreCase("WHITE")) {
                root.put("ChannelColor", ChannelColor.getName());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            Map<String, Object> map = blacklist.save();
            if (!map.isEmpty())
                root.put("BlackList", map);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            Map<String, Object> map = ignorelist.save();
            if (!map.isEmpty())
                root.put("IgnoreList", map);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (Residence.getInstance().getConfigManager().isNewSaveMechanic()) {
            for (Entry<String, CuboidArea> entry : areas.entrySet()) {
                areamap.put(entry.getKey(), entry.getValue().newSave());
            }
        } else {
            for (Entry<String, CuboidArea> entry : areas.entrySet()) {
                areamap.put(entry.getKey(), entry.getValue().save());
            }
        }

        root.put("Areas", areamap);
        Map<String, Object> subzonemap = new HashMap<>();
        for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
            subzonemap.put(sz.getValue().getResidenceName(), sz.getValue().save());
        }
        if (!subzonemap.isEmpty())
            root.put("Subzones", subzonemap);
        root.put("Permissions", perms.save(this.getWorld()));

        if (!this.cmdBlackList.isEmpty())
            root.put("cmdBlackList", this.cmdBlackList);
        if (!this.cmdWhiteList.isEmpty())
            root.put("cmdWhiteList", this.cmdWhiteList);

        try {
            if (tpLoc != null) {
                if (Residence.getInstance().getConfigManager().isNewSaveMechanic()) {
                    root.put("TPLoc",
                        convertDouble(tpLoc.getX()) + ":" + convertDouble(tpLoc.getY()) + ":"
                            + convertDouble(tpLoc.getZ()) + ":" + convertDouble(PitchYaw == null ? 0 : PitchYaw.getX()) + ":"
                            + convertDouble(PitchYaw == null ? 0 : PitchYaw.getY()));
                } else {
                    Map<String, Object> tpmap = new HashMap<String, Object>();
                    tpmap.put("X", convertDouble(this.tpLoc.getX()));
                    tpmap.put("Y", convertDouble(this.tpLoc.getY()));
                    tpmap.put("Z", convertDouble(this.tpLoc.getZ()));
                    tpmap.put("Pitch", convertDouble(PitchYaw == null ? 0 : this.PitchYaw.getX()));
                    tpmap.put("Yaw", convertDouble(PitchYaw == null ? 0 : this.PitchYaw.getY()));
                    root.put("TPLoc", tpmap);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return root;
    }

    // Converting double with comman to dots format and striping to 2 numbers after
    // dot
    private static double convertDouble(double d) {
        return convertDouble(String.valueOf(d));
    }

    private static double convertDouble(String dString) {
        DecimalFormat formatter = new DecimalFormat("#0.00");
        formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.getDefault()));
        dString = dString.replace(",", ".");
        Double d = 0D;
        try {
            d = Double.valueOf(dString);
            d = Double.valueOf(formatter.format(d));
        } catch (Exception e) {
        }
        return d;
    }

    @SuppressWarnings("unchecked")
    public static ClaimedResidence load(String worldName, Map<String, Object> root, ClaimedResidence parent,
        Residence plugin) throws Exception {
        ClaimedResidence res = new ClaimedResidence();
        if (root == null)
            throw new Exception("Null residence!");

        if (root.containsKey("CapitalizedName"))
            res.resName = ((String) root.get("CapitalizedName"));

        if (root.containsKey("CreatedOn"))
            res.createTime = ((Long) root.get("CreatedOn"));
        else
            res.createTime = System.currentTimeMillis();

        if (root.containsKey("LastRaid")) {
            res.getRaid().setEndsAt(((Long) root.get("LastRaid")));
        }

        if (root.containsKey("Immunity")) {
            res.getRaid().setImmunityUntil(((Long) root.get("Immunity")));
        }

        if (root.containsKey("ShopDescription"))
            res.setShopDesc((String) root.get("ShopDescription"));

        if (root.containsKey("StoredMoney")) {
            if (root.get("StoredMoney") instanceof Double)
                res.bank.setStoredMoney((Double) root.get("StoredMoney"));
            else
                res.bank.setStoredMoney((Integer) root.get("StoredMoney"));
        }

        if (root.containsKey("BlackList"))
            res.blacklist = ResidenceItemList.load(plugin, res, (Map<String, Object>) root.get("BlackList"));
        if (root.containsKey("IgnoreList"))
            res.ignorelist = ResidenceItemList.load(plugin, res, (Map<String, Object>) root.get("IgnoreList"));

        Map<String, Object> areamap = (Map<String, Object>) root.get("Areas");

        res.perms = ResidencePermissions.load(worldName, res, (Map<String, Object>) root.get("Permissions"));

        if (res.getPermissions().getOwnerLastKnownName() == null)
            return null;
        if (res.perms.getOwnerUUID() == null) {

            Bukkit.getConsoleSender().sendMessage("Failed to load residence: " + res.getName());

        }
//	if (root.containsKey("TownCap")) {
//	    String townName = (String) root.get("TownCap");
//	    Town t = Residence.getInstance().getTownManager().getTown(townName);
//	    if (t == null)
//		t = Residence.getInstance().getTownManager().addTown(townName, res);
//	    else
//		t.setMainResidence(res);
//	    res.setTown(t);
//	} else if (root.containsKey("Town")) {
//	    String townName = (String) root.get("Town");
//	    Town t = Residence.getInstance().getTownManager().getTown(townName);
//	    if (t == null)
//		t = Residence.getInstance().getTownManager().addTown(townName);
//	    res.setTown(t);
//	}

        if (root.containsKey("MainResidence"))
            res.mainRes = (Boolean) root.get("MainResidence");

        if (root.containsKey("BlockSellPrice"))
            res.BlockSellPrice = (Double) root.get("BlockSellPrice");
        else {
            res.BlockSellPrice = 0D;
        }

        World world = Residence.getInstance().getServ().getWorld(res.perms.getWorldName());

        if (world == null && !Residence.getInstance().getConfigManager().isLoadEveryWorld())
            throw new Exception("Cant Find World: " + res.perms.getWorldName());

        for (Entry<String, Object> map : areamap.entrySet()) {
            if (map.getValue() instanceof String) {
                // loading new same format
                res.areas.put(map.getKey(), CuboidArea.newLoad((String) map.getValue(), res.perms.getWorldName()));
            } else {
                // loading old format
                res.areas.put(map.getKey(), CuboidArea.load((Map<String, Object>) map.getValue(), res.perms.getWorldName()));
            }
        }

        if (root.containsKey("Subzones")) {
            Map<String, Object> subzonemap = (Map<String, Object>) root.get("Subzones");
            for (Entry<String, Object> map : subzonemap.entrySet()) {
                ClaimedResidence subres = ClaimedResidence.load(worldName, (Map<String, Object>) map.getValue(), res,
                    plugin);

                if (subres == null)
                    continue;

                if (subres.getResidenceName() == null)
                    subres.setName(map.getKey());

                if (Residence.getInstance().getConfigManager().flagsInherit())
                    subres.getPermissions().setParent(res.getPermissions());

                // Adding subzone owner into hies res list if parent zone owner is not same
                // person
                if (subres.getParent() != null && !subres.getOwnerUUID().equals(subres.getParent().getOwnerUUID()))
                    Residence.getInstance().getPlayerManager().addResidence(subres.getOwner(), subres);

                res.subzones.put(map.getKey().toLowerCase(), subres);
            }
        }

        if (root.containsKey("EnterMessage") && root.get("EnterMessage") instanceof String) {
            res.enterMessage = (String) root.get("EnterMessage");
        }
        if (root.containsKey("LeaveMessage") && root.get("LeaveMessage") instanceof String) {
            res.leaveMessage = (String) root.get("LeaveMessage");
        }

        if (root.containsKey("Messages") && root.get("Messages") instanceof Integer) {
            res.enterMessage = Residence.getInstance().getResidenceManager().getChacheMessageEnter(worldName, (Integer) root.get("Messages"));
            res.leaveMessage = Residence.getInstance().getResidenceManager().getChacheMessageLeave(worldName, (Integer) root.get("Messages"));
        } else {

            PermissionGroup defaultGroup = Residence.getInstance().getPermissionManager().getDefaultGroup();

            // Defaulting to first one if not present
            if (defaultGroup != null) {
                if (res.enterMessage == null) {
                    res.enterMessage = defaultGroup.getDefaultEnterMessage().replace("\n", "\\n").replace("%subtitle%", "\\n");
                }
                if (res.leaveMessage == null)
                    res.leaveMessage = defaultGroup.getDefaultLeaveMessage().replace("\n", "\\n").replace("%subtitle%", "\\n");
            }
        }

        res.parent = parent;

        if (root.get("TPLoc") instanceof String) {
            String tpLoc = (String) root.get("TPLoc");

            double pitch = 0.0;
            double yaw = 0.0;

            try {
                if (tpLoc.contains(","))
                    tpLoc = tpLoc.replace(",", ".");
                String[] split = tpLoc.split(":");
                if (split.length > 4)
                    yaw = Double.parseDouble(split[4]);
                if (split.length > 3)
                    pitch = Double.parseDouble(split[3]);

                res.tpLoc = new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
            } catch (Exception e) {
            }

            res.PitchYaw = new Vector((float) pitch, (float) yaw, 0);

        } else {
            Map<String, Object> tploc = (Map<String, Object>) root.get("TPLoc");
            if (tploc != null) {
                double pitch = 0.0;
                double yaw = 0.0;

                if (tploc.containsKey("Yaw"))
                    yaw = convertDouble(tploc.get("Yaw").toString());

                if (tploc.containsKey("Pitch"))
                    pitch = convertDouble(tploc.get("Pitch").toString());

                res.tpLoc = new Vector(convertDouble(tploc.get("X").toString()), convertDouble(tploc.get("Y").toString()), convertDouble(tploc.get("Z").toString()));
                res.PitchYaw = new Vector((float) pitch, (float) yaw, 0);
            }
        }

        if (root.containsKey("cmdBlackList"))
            res.cmdBlackList = (List<String>) root.get("cmdBlackList");
        if (root.containsKey("cmdWhiteList"))
            res.cmdWhiteList = (List<String>) root.get("cmdWhiteList");

        if (root.containsKey("ChatPrefix"))
            res.ChatPrefix = (String) root.get("ChatPrefix");

        if (root.containsKey("ChannelColor"))
            res.ChannelColor = CMIChatColor.getColor((String) root.get("ChannelColor"));
        else {
            res.ChannelColor = Residence.getInstance().getConfigManager().getChatColor();
        }

        return res;
    }

    public int getAreaCount() {
        return areas.size();
    }

    public boolean renameSubzone(String oldName, String newName) {
        return this.renameSubzone(null, oldName, newName, true);
    }

    public boolean renameSubzone(Player player, String oldName, String newName, boolean resadmin) {

        if (!Residence.getInstance().validName(newName)) {
            Residence.getInstance().msg(player, lm.Invalid_NameCharacters);
            return false;
        }
        if (oldName == null)
            return false;
        if (newName == null)
            return false;
        String newN = newName;
        oldName = oldName.toLowerCase();
        newName = newName.toLowerCase();

        ClaimedResidence res = subzones.get(oldName);
        if (res == null) {
            if (player != null)
                Residence.getInstance().msg(player, lm.Invalid_Subzone);
            return false;
        }
        if (player != null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
            Residence.getInstance().msg(player, lm.General_NoPermission);
            return false;
        }
        if (subzones.containsKey(newName)) {
            if (player != null)
                Residence.getInstance().msg(player, lm.Subzone_Exists, newName);
            return false;
        }
        res.setName(newN);
        subzones.put(newName, res);
        subzones.remove(oldName);
        if (player != null)
            Residence.getInstance().msg(player, lm.Subzone_Rename, oldName, newName);
        return true;
    }

    public boolean renameArea(String oldName, String newName) {
        return this.renameArea(null, oldName, newName, true);
    }

    public boolean renameArea(Player player, String oldName, String newName, boolean resadmin) {
        if (!Residence.getInstance().validName(newName)) {
            Residence.getInstance().msg(player, lm.Invalid_NameCharacters);
            return false;
        }

        if (this.getRaid().isRaidInitialized() && !resadmin) {
            Residence.getInstance().msg(player, lm.Raid_cantDo);
            return false;
        }

        if (player == null || perms.hasResidencePermission(player, true) || resadmin) {
            if (areas.containsKey(newName)) {
                if (player != null)
                    Residence.getInstance().msg(player, lm.Area_Exists);
                return false;
            }
            CuboidArea area = areas.get(oldName);
            if (area == null) {
                if (player != null)
                    Residence.getInstance().msg(player, lm.Area_InvalidName);
                return false;
            }
            areas.put(newName, area);
            areas.remove(oldName);
            if (player != null)
                Residence.getInstance().msg(player, lm.Area_Rename, oldName, newName);
            return true;
        }
        Residence.getInstance().msg(player, lm.General_NoPermission);
        return false;
    }

    public CuboidArea getArea(String name) {
        return areas.get(name);
    }

    public String getName() {
        String name = this.resName;
        if (this.getParent() != null)
            name = this.getParent().getName() + "." + name;
        if (name == null)
            return "Unknown";
        return name;
    }

    public void remove() {
        Residence.getInstance().getResidenceManager().removeResidence(this);
        Residence.getInstance().getResidenceManager().removeChunkList(this.getName());
        Residence.getInstance().getPlayerManager().removeResFromPlayer(this);
    }

    public ResidenceBank getBank() {
        return bank;
    }

    @Deprecated
    public String getWorld() {
        return perms.getWorldName();
    }

    @Deprecated
    public String getWorldName() {
        return perms.getWorldName();
    }

    public ResidencePlayer getRPlayer() {
        return Residence.getInstance().getPlayerManager().getResidencePlayer(this.getPermissions().getOwner());
    }

    public PermissionGroup getOwnerGroup() {
        return getRPlayer().getGroup(getPermissions().getWorldName());
    }

    public String getOwner() {
        return perms.getOwner();
    }

    public boolean isOwner(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player != null && player.getName().equalsIgnoreCase(name))
            return isOwner(player);
        return perms.getOwner().equalsIgnoreCase(name);
    }

    public boolean isOwner(UUID uuid) {
        return perms.getOwnerUUID().toString().equals(uuid.toString());
    }

    public boolean isOwner(Player p) {
        if (p == null)
            return false;
        if (Residence.getInstance().getConfigManager().isOfflineMode())
            return perms.getOwner().equalsIgnoreCase(p.getName()) || perms.getOwnerUUID().equals(p.getUniqueId());
        return perms.getOwnerUUID().equals(p.getUniqueId());
    }

    public boolean isOwner(CommandSender sender) {
        if (sender instanceof Player)
            return isOwner((Player) sender);

        if (Residence.getInstance().getConfigManager().isOfflineMode()) {
            return perms.getOwner().equalsIgnoreCase(sender.getName());
        }

        return true;
    }

    public void setChatPrefix(String ChatPrefix) {
        this.ChatPrefix = ChatPrefix;
    }

    public String getChatPrefix() {
        return this.ChatPrefix == null ? "" : this.ChatPrefix;
    }

    public void setChannelColor(CMIChatColor ChannelColor) {
        this.ChannelColor = ChannelColor;
    }

    public ChatChannel getChatChannel() {
        return Residence.getInstance().getChatManager().getChannel(this.getName());
    }

    public CMIChatColor getChannelColor() {
        return ChannelColor;
    }

    public UUID getOwnerUUID() {
        return perms.getOwnerUUID();
    }

    public ResidenceItemList getItemBlacklist() {
        return blacklist;
    }

    public ResidenceItemList getItemIgnoreList() {
        return ignorelist;
    }

    public List<String> getCmdBlackList() {
        return this.cmdBlackList;
    }

    public List<String> getCmdWhiteList() {
        return this.cmdWhiteList;
    }

    public boolean addCmdBlackList(String cmd) {
        if (cmd.contains("/"))
            cmd = cmd.replace("/", "");
        if (!this.cmdBlackList.contains(cmd.toLowerCase())) {
            this.cmdBlackList.add(cmd.toLowerCase());
            return true;
        }
        this.cmdBlackList.remove(cmd.toLowerCase());
        return false;
    }

    public boolean addCmdWhiteList(String cmd) {
        if (cmd.contains("/"))
            cmd = cmd.replace("/", "");
        if (!this.cmdWhiteList.contains(cmd.toLowerCase())) {
            this.cmdWhiteList.add(cmd.toLowerCase());
            return true;
        }
        this.cmdWhiteList.remove(cmd.toLowerCase());
        return false;
    }

    public Double getBlockSellPrice() {
        return BlockSellPrice;
    }

    public ArrayList<Player> getPlayersInResidence() {
        ArrayList<Player> within = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (this.containsLoc(player.getLocation())) {
                within.add(player);
            }
        }
        return within;
    }

    public List<ShopVote> GetShopVotes() {
        return ShopVoteList;
    }

    public void clearShopVotes() {
        ShopVoteList.clear();
    }

    public void addShopVote(List<ShopVote> ShopVotes) {
        ShopVoteList.addAll(ShopVotes);
    }

    public void addShopVote(ShopVote ShopVote) {
        ShopVoteList.add(ShopVote);
    }

    public Long getLeaseExpireTime() {
        return leaseExpireTime;
    }

    public void setLeaseExpireTime(long leaseExpireTime) {
        this.leaseExpireTime = leaseExpireTime;
    }

    public boolean kickFromResidence(Player player) {
        if (!this.containsLoc(player.getLocation()))
            return false;

        Location loc = Residence.getInstance().getConfigManager().getKickLocation();
        player.closeInventory();

        if (loc == null) {
            CompletableFuture<Location> future = LocationUtil.getOutsideFreeLocASYNC(this, player, true);

            future.thenAccept(loc1 -> {

                if (loc1 == null) {
                    LC.info_IncorrectLocation.getLocale();
                    return;
                }

                loc1.add(0, 0.4, 0);

                CMITeleporter.teleportAsync(player, loc1).thenApply(success -> {
                    if (success)
                        Residence.getInstance().msg(player, lm.Residence_Kicked);
                    else
                        Residence.getInstance().msg(player, lm.General_TeleportCanceled);
                    return null;
                });
            });
            return true;
        }

        CMITeleporter.teleportAsync(player, loc).thenApply(success -> {
            if (success)
                Residence.getInstance().msg(player, lm.Residence_Kicked);
            else
                Residence.getInstance().msg(player, lm.General_TeleportCanceled);
            return null;
        });
        return true;
    }

//    public Town getTown() {
//	return town;
//    }
//
//    public void setTown(Town town) {
//	this.town = town;
//    }

    public ResidenceRaid getRaid() {
        if (raid == null)
            raid = new ResidenceRaid(this);
        return raid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return this == obj;
    }

    public Set<Signs> getSignsInResidence() {
        return signsInResidence;
    }

    public void setSignsInResidence(Set<Signs> signsInResidence) {
        this.signsInResidence = signsInResidence;
    }

    public double getWorthByOwner() {
        return (long) ((getTotalSize() * getOwnerGroup().getCostPerBlock()) * 100L) / 100D;
    }

    public double getWorth() {
        return (long) ((getTotalSize() * getBlockSellPrice()) * 100L) / 100D;
    }

    public void showBounds(Player player, boolean showOneTime) {
        Visualizer v = new Visualizer(player);
        v.setAreas(getAreaArray());
        v.setOnce(showOneTime);
        Residence.getInstance().getSelectionManager().showBounds(player, v);
    }

    public boolean isTrusted(Player player) {
        Set<String> flags = FlagPermissions.validFlagGroups.get(padd.groupedFlag);
        if (flags == null || flags.isEmpty() || player == null)
            return false;
        boolean trusted = true;
        for (String flag : flags) {
            Flags f = Flags.getFlag(flag);
            if (f == null) {
                trusted = false;
                break;
            }
            if (f.isInGroup(padd.groupedFlag) && !this.getPermissions().playerHas(player, f, FlagCombo.OnlyTrue)) {
                trusted = false;
                break;
            }
        }
        return trusted;
    }

    @Deprecated
    public boolean isTrusted(String playerName) {
        Set<String> flags = FlagPermissions.validFlagGroups.get(padd.groupedFlag);

        if (flags == null || flags.isEmpty() || playerName == null)
            return false;
        boolean trusted = true;
        for (String flag : flags) {
            Flags f = Flags.getFlag(flag);
            if (f == null) {
                trusted = false;
                break;
            }
            if (f.isInGroup(padd.groupedFlag) && !lightWeightFlagCheck(playerName, flag)) {
                trusted = false;
                break;
            }
        }
        return trusted;
    }

    private boolean lightWeightFlagCheck(String playerName, String flag) {
        Map<String, Boolean> flags = this.getPermissions().getPlayerFlags(playerName);
        if (flags == null || flags.isEmpty() || !flags.containsKey(flag))
            return false;
        return flags.get(flag);
    }

    public Set<ResidencePlayer> getTrustedPlayers() {
        Set<ResidencePlayer> trusted = new HashSet<ResidencePlayer>();
        Iterator<Entry<String, Map<String, Boolean>>> iter = this.getPermissions().getPlayerFlags().entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, Map<String, Boolean>> entry = iter.next();
            if (isTrusted(entry.getKey())) {
                ResidencePlayer rp = ResidencePlayer.get(entry.getKey());
                if (rp != null)
                    trusted.add(rp);
            }
        }
        return trusted;
    }

    public static ClaimedResidence getByName(String landName) {
        return Residence.getInstance().getResidenceManager().getByName(landName);
    }
}
