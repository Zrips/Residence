package com.bekvon.bukkit.residence.protection;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ActionBarTitleMessages;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.Siege.ResidenceSiege;
import com.bekvon.bukkit.residence.chat.ChatChannel;
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
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.text.help.PageInfo;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.Utils;

public class ClaimedResidence {

    private String resName = null;
    protected ClaimedResidence parent;
    protected Map<String, CuboidArea> areas;
    protected Map<String, ClaimedResidence> subzones;
    protected ResidencePermissions perms;
    protected ResidenceBank bank;
    protected Double BlockSellPrice = 0.0;
    protected Location tpLoc;
    protected String enterMessage;
    protected String leaveMessage;
    protected String ShopDesc = null;
    protected String ChatPrefix = "";
    protected ChatColor ChannelColor = ChatColor.WHITE;
    protected ResidenceItemList ignorelist;
    protected ResidenceItemList blacklist;
    protected boolean mainRes = false;
    protected long createTime = 0L;

    private Long leaseExpireTime = null;

    protected List<String> cmdWhiteList = new ArrayList<String>();
    protected List<String> cmdBlackList = new ArrayList<String>();

    List<ShopVote> ShopVoteList = new ArrayList<ShopVote>();

    protected RentableLand rentableland = null;
    protected RentedLand rentedland = null;

    protected Integer sellPrice = -1;

    private Residence plugin;

    private ResidenceSiege siege;

    private Set<Signs> signsInResidence = new HashSet<Signs>();

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
	return plugin.getTransactionManager().isForSale(this.getName());
    }

    public boolean isForRent() {
	return plugin.getRentManager().isForRent(this);
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
	return plugin.getRentManager().isRented(this);
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

    public ClaimedResidence(String creationWorld, Residence plugin) {
	this(plugin.getServerLandName(), creationWorld, plugin);
    }

    public ClaimedResidence(String creator, String creationWorld, Residence plugin) {
	this(plugin);
	perms = new ResidencePermissions(this, creator, creationWorld);
    }

    public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence, Residence plugin) {
	this(creator, creationWorld, plugin);
	parent = parentResidence;
    }

    public ClaimedResidence(Residence plugin) {
	subzones = new HashMap<>();
	areas = new HashMap<>();
	bank = new ResidenceBank(this);
	blacklist = new ResidenceItemList(plugin, this, ListType.BLACKLIST);
	ignorelist = new ResidenceItemList(plugin, this, ListType.IGNORELIST);
	this.plugin = plugin;
    }

    public boolean isMainResidence() {
	return mainRes;
    }

    public void setMainResidence(boolean state) {
	mainRes = state;
    }

    public boolean isSubzone() {
	return parent == null ? false : true;
    }

    public int getSubzoneDeep() {
	return getSubzoneDeep(0);
    }

    public int getSubzoneDeep(int deep) {
	deep++;
	if (parent != null) {
	    return parent.getSubzoneDeep(deep);
	}
	return deep;
    }

    public boolean isBiggerThanMin(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() < group.getMinX()) {
	    plugin.msg(player, lm.Area_ToSmallX, area.getXSize(), group.getMinX());
	    return false;
	}
	if (area.getYSize() < group.getMinY()) {
	    plugin.msg(player, lm.Area_ToSmallY, area.getYSize(), group.getMinY());
	    return false;
	}
	if (area.getZSize() < group.getMinZ()) {
	    plugin.msg(player, lm.Area_ToSmallZ, area.getZSize(), group.getMinZ());
	    return false;
	}
	return true;
    }

    public boolean isBiggerThanMinSubzone(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() < group.getSubzoneMinX()) {
	    plugin.msg(player, lm.Area_ToSmallX, area.getXSize(), group.getSubzoneMinX());
	    return false;
	}
	if (area.getYSize() < group.getSubzoneMinY()) {
	    plugin.msg(player, lm.Area_ToSmallY, area.getYSize(), group.getSubzoneMinY());
	    return false;
	}
	if (area.getZSize() < group.getSubzoneMinZ()) {
	    plugin.msg(player, lm.Area_ToSmallZ, area.getZSize(), group.getSubzoneMinZ());
	    return false;
	}
	return true;
    }

    public boolean isSmallerThanMax(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() > group.getMaxX()) {
	    plugin.msg(player, lm.Area_ToBigX, area.getXSize(), group.getMaxX());
	    return false;
	}
	if (area.getYSize() > group.getMaxY()) {
	    plugin.msg(player, lm.Area_ToBigY, area.getYSize(), group.getMaxY());
	    return false;
	}
	if (area.getZSize() > group.getMaxZ()) {
	    plugin.msg(player, lm.Area_ToBigZ, area.getZSize(), group.getMaxZ());
	    return false;
	}
	return true;
    }

    public boolean isSmallerThanMaxSubzone(Player player, CuboidArea area, boolean resadmin) {
	if (resadmin)
	    return true;
	ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	if (area.getXSize() > group.getSubzoneMaxX()) {
	    plugin.msg(player, lm.Area_ToBigX, area.getXSize(), group.getSubzoneMaxX());
	    return false;
	}
	if (area.getYSize() > group.getSubzoneMaxY()) {
	    plugin.msg(player, lm.Area_ToBigY, area.getYSize(), group.getSubzoneMaxY());
	    return false;
	}
	if (area.getZSize() > group.getSubzoneMaxZ()) {
	    plugin.msg(player, lm.Area_ToBigZ, area.getZSize(), group.getSubzoneMaxZ());
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
	if (!plugin.validName(name)) {
	    if (player != null) {
		plugin.msg(player, lm.Invalid_NameCharacters);
	    }
	    return false;
	}

	String NName = name;
	name = name.toLowerCase();

	if (areas.containsKey(NName)) {
	    if (player != null) {
		plugin.msg(player, lm.Area_Exists);
	    }
	    return false;
	}

	if (this.isSubzone() && !isBiggerThanMinSubzone(player, area, resadmin)
	    || !this.isSubzone() && !isBiggerThanMin(player, area, resadmin))
	    return false;

	if (!resadmin && plugin.getConfigManager().getEnforceAreaInsideArea() && this.getParent() == null) {
	    boolean inside = false;
	    for (CuboidArea are : areas.values()) {
		if (are.isAreaWithinArea(area)) {
		    inside = true;
		}
	    }
	    if (!inside) {
		return false;
	    }
	}
	if (!area.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
	    if (player != null) {
		plugin.msg(player, lm.Area_DiffWorld);
	    }
	    return false;
	}
	if (parent == null) {
	    String collideResidence = plugin.getResidenceManager().checkAreaCollision(area, this);
	    ClaimedResidence cRes = plugin.getResidenceManager().getByName(collideResidence);
	    if (cRes != null) {
		if (player != null) {
		    plugin.msg(player, lm.Area_Collision, cRes.getName());
		    Visualizer v = new Visualizer(player);
		    v.setAreas(area);
		    v.setErrorAreas(cRes);
		    plugin.getSelectionManager().showBounds(player, v);
		}
		return false;
	    }
	} else {
	    String[] szs = parent.listSubzones();
	    for (String sz : szs) {
		ClaimedResidence res = parent.getSubzone(sz);
		if (res != null && res != this) {
		    if (res.checkCollision(area)) {
			if (player != null) {
			    plugin.msg(player, lm.Area_SubzoneCollision, sz);
			}
			return false;
		    }
		}
	    }
	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		plugin.msg(player, lm.General_NoPermission);
		return false;
	    }
	    if (parent != null) {
		if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
		    plugin.msg(player, lm.Area_NotWithinParent);
		    return false;
		}
		if (!parent.getPermissions().hasResidencePermission(player, true)
		    && !parent.getPermissions().playerHas(player, Flags.subzone, FlagCombo.OnlyTrue)) {
		    plugin.msg(player, lm.Residence_ParentNoPermission);
		    return false;
		}
	    }

	    ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

	    PermissionGroup group = rPlayer.getGroup();
	    if (!this.isSubzone() && !group.canCreateResidences() && !ResPerm.create.hasPermission(player, true)
		|| this.isSubzone() && !group.canCreateResidences()
		    && !ResPerm.create_subzone.hasPermission(player, true)) {
		return false;
	    }

	    if (areas.size() >= group.getMaxPhysicalPerResidence()) {
		plugin.msg(player, lm.Area_MaxPhysical);
		return false;
	    }
	    if (!this.isSubzone() && !isSmallerThanMax(player, area, resadmin)
		|| this.isSubzone() && !isSmallerThanMaxSubzone(player, area, resadmin)) {
		plugin.msg(player, lm.Area_SizeLimit);
		return false;
	    }

	    if (group.getMinHeight() > area.getLowLoc().getBlockY()) {
		plugin.msg(player, lm.Area_LowLimit, String.format("%d", group.getMinHeight()));
		return false;
	    }

	    if (group.getMaxHeight() < area.getHighLoc().getBlockY()) {
		plugin.msg(player, lm.Area_HighLimit, String.format("%d", group.getMaxHeight()));
		return false;
	    }

	    if (!resadmin) {
		if (plugin.getWorldGuard() != null && plugin.getWorldGuardUtil().isSelectionInArea(player))
		    return false;

		if (plugin.getKingdomsManager() != null && plugin.getKingdomsUtil().isSelectionInArea(player))
		    return false;
	    }

	    if (chargeMoney && parent == null && plugin.getConfigManager().enableEconomy() && !resadmin) {
		double chargeamount = area.getCost(group);
		if (!plugin.getTransactionManager().chargeEconomyMoney(player, chargeamount)) {
		    return false;
		}
	    }
	}

	ResidenceAreaAddEvent resevent = new ResidenceAreaAddEvent(player, NName, this, area);
	plugin.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return false;

	plugin.getResidenceManager().removeChunkList(this.getName());
	areas.put(name, area);
	plugin.getResidenceManager().calculateChunks(this.getName());
	return true;
    }

    public boolean replaceArea(CuboidArea neware, String name) {
	return this.replaceArea(null, neware, name, true);
    }

    public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {
	if (!areas.containsKey(name)) {
	    if (player != null)
		plugin.msg(player, lm.Area_NonExist);
	    return false;
	}
	CuboidArea oldarea = areas.get(name);
	if (!newarea.getWorld().getName().equalsIgnoreCase(perms.getWorld())) {
	    if (player != null)
		plugin.msg(player, lm.Area_DiffWorld);
	    return false;
	}
	if (parent == null) {
	    String collideResidence = plugin.getResidenceManager().checkAreaCollision(newarea, this);
	    ClaimedResidence cRes = plugin.getResidenceManager().getByName(collideResidence);
	    if (cRes != null && player != null) {
		plugin.msg(player, lm.Area_Collision, cRes.getName());
		Visualizer v = new Visualizer(player);
		v.setAreas(this.getAreaArray());
		v.setErrorAreas(cRes.getAreaArray());
		plugin.getSelectionManager().showBounds(player, v);
		return false;
	    }
	} else {
	    String[] szs = parent.listSubzones();
	    for (String sz : szs) {
		ClaimedResidence res = parent.getSubzone(sz);
		if (res != null && res != this) {
		    if (res.checkCollision(newarea)) {
			if (player != null) {
			    plugin.msg(player, lm.Area_SubzoneCollision, sz);
			    Visualizer v = new Visualizer(player);
			    v.setErrorAreas(res.getAreaArray());
			    plugin.getSelectionManager().showBounds(player, v);
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
		    plugin.msg(player, lm.Area_Collision, res.getName());
		    Visualizer v = new Visualizer(player);
		    v.setAreas(this.getAreaArray());
		    v.setErrorAreas(res.getAreaArray());
		    plugin.getSelectionManager().showBounds(player, v);
		    return false;
		}

	    }
	    if (res.getAreaArray().length == 0) {
		removeSubzone(sz);
	    }
	}

	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		plugin.msg(player, lm.General_NoPermission);
		return false;
	    }
	    if (parent != null) {
		if (!parent.containsLoc(newarea.getHighLoc()) || !parent.containsLoc(newarea.getLowLoc())) {
		    plugin.msg(player, lm.Area_NotWithinParent);
		    return false;
		}
		if (!parent.getPermissions().hasResidencePermission(player, true)
		    && !parent.getPermissions().playerHas(player, Flags.subzone, FlagCombo.OnlyTrue)) {
		    plugin.msg(player, lm.Residence_ParentNoPermission);
		    return false;
		}
	    }
	    ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	    PermissionGroup group = rPlayer.getGroup();
	    if (!group.canCreateResidences() && !ResPerm.resize.hasPermission(player, true)) {
		return false;
	    }

	    if (oldarea.getSize() < newarea.getSize()
		&& (!this.isSubzone() && !isSmallerThanMax(player, newarea, resadmin)
		    || this.isSubzone() && !isSmallerThanMaxSubzone(player, newarea, resadmin))) {
		plugin.msg(player, lm.Area_SizeLimit);
		return false;
	    }
	    if (group.getMinHeight() > newarea.getLowLoc().getBlockY()) {
		plugin.msg(player, lm.Area_LowLimit, String.format("%d", group.getMinHeight()));
		return false;
	    }
	    if (group.getMaxHeight() < newarea.getHighLoc().getBlockY()) {
		plugin.msg(player, lm.Area_HighLimit, String.format("%d", group.getMaxHeight()));
		return false;
	    }

	    if (!isBiggerThanMin(player, newarea, resadmin))
		return false;

	    if (!resadmin) {
		if (plugin.getWorldGuard() != null && plugin.getWorldGuardUtil().isSelectionInArea(player))
		    return false;
		if (plugin.getKingdomsManager() != null && plugin.getKingdomsUtil().isSelectionInArea(player))
		    return false;
	    }

	    if (parent == null && plugin.getConfigManager().enableEconomy() && !resadmin) {
		double chargeamount = newarea.getCost(group) - oldarea.getCost(group);
		if (chargeamount > 0) {
		    if (!plugin.getTransactionManager().chargeEconomyMoney(player, chargeamount)) {
			return false;
		    }
		}
	    }
	}

	ResidenceSizeChangeEvent resevent = new ResidenceSizeChangeEvent(player, this, oldarea, newarea);
	plugin.getServ().getPluginManager().callEvent(resevent);
	if (resevent.isCancelled())
	    return false;

	if ((!resadmin) && (player != null)) {
	    int chargeamount = (int) Math
		.ceil((newarea.getSize() - oldarea.getSize()) * getBlockSellPrice().doubleValue());
	    if ((chargeamount < 0) && (this.plugin.getConfigManager().useResMoneyBack())) {
		if (!this.isServerLand())
		    this.plugin.getTransactionManager().giveEconomyMoney(player, -chargeamount);
	    }
	}

	plugin.getResidenceManager().removeChunkList(this.getName());
	areas.remove(name);
	areas.put(name, newarea);
	plugin.getResidenceManager().calculateChunks(this.getName());
	if (player != null)
	    plugin.msg(player, lm.Area_Update);
	return true;
    }

    public boolean addSubzone(String name, Location loc1, Location loc2) {
	return this.addSubzone(null, loc1, loc2, name, true);
    }

    public boolean addSubzone(Player player, Location loc1, Location loc2, String name, boolean resadmin) {
	if (player == null) {
	    return this.addSubzone(null, plugin.getServerLandName(), loc1, loc2, name, resadmin);
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
	if (!plugin.validName(name)) {
	    if (player != null) {
		plugin.msg(player, lm.Invalid_NameCharacters);
	    }
	    return false;
	}
	if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
	    if (player != null) {
		plugin.msg(player, lm.Subzone_SelectInside);
	    }
	    return false;
	}

	String NName = name;
	name = name.toLowerCase();

	if (subzones.containsKey(name)) {
	    if (player != null) {
		plugin.msg(player, lm.Subzone_Exists, NName);
	    }
	    return false;
	}
	if (!resadmin && player != null) {
	    if (!this.perms.hasResidencePermission(player, true)) {
		if (!this.perms.playerHas(player.getName(), Flags.subzone,
		    this.perms.playerHas(player, Flags.admin, false))) {
		    plugin.msg(player, lm.General_NoPermission);
		    return false;
		}
	    }

	    if (this.getSubzoneList().length >= plugin.getPlayerManager().getResidencePlayer(owner).getMaxSubzones()) {
		plugin.msg(player, lm.Subzone_MaxAmount);
		return false;
	    }

	    if (this.getZoneDepth() >= plugin.getPlayerManager().getResidencePlayer(owner).getMaxSubzoneDepth()) {
		plugin.msg(player, lm.Subzone_MaxDepth);
		return false;
	    }
	}

	CuboidArea newArea = new CuboidArea(loc1, loc2);

	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	for (Entry<String, ClaimedResidence> resEntry : set) {
	    ClaimedResidence res = resEntry.getValue();
	    if (res.checkCollision(newArea)) {
		if (player != null) {
		    plugin.msg(player, lm.Subzone_Collide, resEntry.getKey());
		    Visualizer v = new Visualizer(player);
		    v.setAreas(newArea);
		    v.setErrorAreas(res);
		    plugin.getSelectionManager().showBounds(player, v);
		}
		return false;
	    }
	}
	ClaimedResidence newres;
	if (player != null) {
	    newres = new ClaimedResidence(owner, perms.getWorld(), this, plugin);
	    newres.addArea(player, newArea, NName, resadmin);
	} else {
	    newres = new ClaimedResidence(owner, perms.getWorld(), this, plugin);
	    newres.addArea(newArea, NName);
	}

	if (newres.getAreaCount() != 0) {
	    newres.getPermissions().applyDefaultFlags();
	    if (player != null) {
		ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
		PermissionGroup group = rPlayer.getGroup();
		newres.setEnterMessage(group.getDefaultEnterMessage());
		newres.setLeaveMessage(group.getDefaultLeaveMessage());
	    }
	    if (plugin.getConfigManager().flagsInherit()) {
		newres.getPermissions().setParent(perms);
	    }

	    newres.resName = NName;

	    newres.setCreateTime();

	    ResidenceSubzoneCreationEvent resevent = new ResidenceSubzoneCreationEvent(player, NName, newres, newArea);
	    plugin.getServ().getPluginManager().callEvent(resevent);
	    if (resevent.isCancelled())
		return false;

	    subzones.put(name, newres);
	    if (player != null) {
		plugin.msg(player, lm.Area_Create, NName);
		plugin.msg(player, lm.Subzone_Create, NName);
	    }
	    return true;
	}
	if (player != null) {
	    plugin.msg(player, lm.Subzone_CreateFail, NName);
	}
	return false;
    }

    public ClaimedResidence getSubzoneByLoc(Location loc) {
	Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
	ClaimedResidence res = null;
	for (Entry<String, ClaimedResidence> entry : set) {
	    if (entry.getValue().containsLoc(loc)) {
		res = entry.getValue();
		break;
	    }
	}
	if (res == null)
	    return null;
	ClaimedResidence subrez = res.getSubzoneByLoc(loc);
	if (subrez == null) {
	    return res;
	}
	return subrez;
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
	Set<String> set = areas.keySet();
	for (String key : set) {
	    CuboidArea checkarea = areas.get(key);
	    if (checkarea != null) {
		if (checkarea.checkCollision(area)) {
		    return true;
		}
	    }
	}
	return false;
    }

    public boolean containsLoc(Location loc) {
	Collection<CuboidArea> keys = areas.values();
	for (CuboidArea key : keys) {
	    if (key.containsLoc(loc)) {
		if (parent != null)
		    return parent.containsLoc(loc);
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
	if (parent == null)
	    return this;
	return parent.getTopParent();
    }

    public boolean isTopArea() {
	return parent == null;
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
	    plugin.msg(player, lm.General_NoPermission);
	    return false;
	}
	subzones.remove(name);
	if (player != null) {
	    plugin.msg(player, lm.Subzone_Remove, name);
	}
	return true;
    }

    public long getTotalSize() {
	Collection<CuboidArea> set = areas.values();
	long size = 0;
	if (!plugin.getConfigManager().isNoCostForYBlocks())
	    for (CuboidArea entry : set) {
		size = size + entry.getSize();
	    }
	else
	    for (CuboidArea entry : set) {
		size = size + (entry.getXSize() * entry.getZSize());
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
	CuboidArea[] temp = new CuboidArea[areas.size()];
	int i = 0;
	for (CuboidArea area : areas.values()) {
	    temp[i] = area;
	    i++;
	}
	return temp;
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
	    if (message.equals("")) {
		message = null;
	    }
	}
	if (sender instanceof Player) {
	    ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer((Player) sender);
	    PermissionGroup group = rPlayer.getGroup();
	    if (!group.canSetEnterLeaveMessages() && !resadmin) {
		plugin.msg(sender, lm.Residence_OwnerNoPermission);
		return;
	    }
	    if (!perms.hasResidencePermission(sender, false) && !resadmin) {
		plugin.msg(sender, lm.General_NoPermission);
		return;
	    }
	}
	if (enter) {
	    this.setEnterMessage(message);
	} else {
	    this.setLeaveMessage(message);
	}
	plugin.msg(sender, lm.Residence_MessageChange);
    }

    public Location getMiddleFreeLoc(Location insideLoc, Player player) {
	CuboidArea area = this.getAreaByLoc(insideLoc);
	if (area == null) {
	    return insideLoc;
	}

	int y = area.getHighLoc().getBlockY();
	int lowY = area.getLowLoc().getBlockY();

	int x = area.getLowLoc().getBlockX() + area.getXSize() / 2;
	int z = area.getLowLoc().getBlockZ() + area.getZSize() / 2;

	Location newLoc = new Location(area.getWorld(), x + 0.5, y, z + 0.5);
	boolean found = false;
	int it = 1;
	int maxIt = newLoc.getBlockY() + 1;
	while (it < maxIt) {
	    it++;

	    if (newLoc.getBlockY() < lowY)
		break;

	    newLoc.add(0, -1, 0);

	    Block block = newLoc.getBlock();
	    Block block2 = newLoc.clone().add(0, 1, 0).getBlock();
	    Block block3 = newLoc.clone().add(0, -1, 0).getBlock();
	    if (ResidencePlayerListener.isEmptyBlock(block) && ResidencePlayerListener.isEmptyBlock(block2)
		&& !ResidencePlayerListener.isEmptyBlock(block3)) {
		found = true;
		break;
	    }
	}
	if (found) {
	    if (player != null) {
		newLoc.setPitch(player.getLocation().getPitch());
		newLoc.setYaw(player.getLocation().getYaw());
	    }
	    return newLoc;
	}
	return getOutsideFreeLoc(insideLoc, player);
    }

    public Location getOutsideFreeLoc(Location insideLoc, Player player) {
	CuboidArea area = this.getAreaByLoc(insideLoc);
	if (area == null) {
	    return insideLoc;
	}

	List<RandomLoc> randomLocList = new ArrayList<RandomLoc>();

	for (int z = -1; z < area.getZSize() + 1; z++) {
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX(), 0, area.getLowLoc().getZ() + z));
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX() + area.getXSize(), 0, area.getLowLoc().getZ() + z));
	}

	for (int x = -1; x < area.getXSize() + 1; x++) {
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX() + x, 0, area.getLowLoc().getZ()));
	    randomLocList.add(new RandomLoc(area.getLowLoc().getX() + x, 0, area.getLowLoc().getZ() + area.getZSize()));
	}

	Location loc = insideLoc.clone();

	boolean found = false;
	int it = 0;
	int maxIt = 30;
	while (!found && it < maxIt) {
	    it++;

	    Random ran = new Random(System.currentTimeMillis());
	    if (randomLocList.isEmpty())
		break;
	    int check = ran.nextInt(randomLocList.size());
	    RandomLoc place = randomLocList.get(check);
	    randomLocList.remove(check);
	    double x = place.getX();
	    double z = place.getZ();

	    loc.setX(x);
	    loc.setZ(z);
	    loc.setY(area.getHighLoc().getBlockY());

	    int max = area.getHighLoc().getBlockY();
	    max = loc.getWorld().getEnvironment() == Environment.NETHER ? 100 : max;

	    for (int i = max; i > area.getLowLoc().getY(); i--) {
		loc.setY(i);
		Block block = loc.getBlock();
		Block block2 = loc.clone().add(0, 1, 0).getBlock();
		Block block3 = loc.clone().add(0, -1, 0).getBlock();
		if (!ResidencePlayerListener.isEmptyBlock(block3) && ResidencePlayerListener.isEmptyBlock(block)
		    && ResidencePlayerListener.isEmptyBlock(block2)) {
		    break;
		}
	    }

	    if (!ResidencePlayerListener.isEmptyBlock(loc.getBlock()))
		continue;

	    if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.LAVA)
		continue;

	    if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.WATER)
		continue;

	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);
	    if (res != null && player != null && !res.getPermissions().playerHas(player, Flags.tp, FlagCombo.TrueOrNone)
		&& !ResPerm.admin_tp.hasPermission(player))
		continue;

	    found = true;
	    loc.add(0.5, 0.1, 0.5);
	    break;
	}

	if (!found && plugin.getConfigManager().getKickLocation() != null)
	    return plugin.getConfigManager().getKickLocation();
	if (player != null) {
	    loc.setPitch(player.getLocation().getPitch());
	    loc.setYaw(player.getLocation().getYaw());
	}
	return loc;
    }

    public CuboidArea getMainArea() {
	CuboidArea area = areas.get(this.isSubzone() ? this.getResidenceName() : "main");
	if (area == null && !areas.isEmpty())
	    for (Entry<String, CuboidArea> one : areas.entrySet()) {
		area = one.getValue();
		break;
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
	    sender.sendMessage(ChatColor.RED + plugin.msg(lm.Invalid_Page));
	    return;
	}

	plugin.msg(sender, lm.InformationPage_TopLine, plugin.msg(lm.General_Subzones));
	plugin.msg(sender, lm.InformationPage_Page, plugin.msg(lm.General_GenericPages, String.format("%d", page),
	    pi.getTotalPages(), pi.getTotalEntries()));
	RawMessage rm = new RawMessage();
	for (int i = pi.getStart(); i <= pi.getEnd(); i++) {
	    ClaimedResidence res = getSubzones().get(i);
	    if (res == null)
		continue;
	    rm.add(ChatColor.GREEN + res.getResidenceName() + ChatColor.YELLOW + " - "
		+ plugin.msg(lm.General_Owner, res.getOwner()), "Teleport to " + res.getName(),
		"res tp " + res.getName());
	    rm.show(sender);
	    rm.clear();
	}

	plugin.getInfoPageManager().ShowPagination(sender, pi.getTotalPages(), page, "res sublist " + this.getName());
    }

    public void printAreaList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (String area : areas.keySet()) {
	    temp.add(area);
	}
	plugin.getInfoPageManager().printInfo(player, "res area list " + this.getName(),
	    plugin.msg(lm.General_PhysicalAreas), temp, page);
    }

    public void printAdvancedAreaList(Player player, int page) {
	ArrayList<String> temp = new ArrayList<>();
	for (Entry<String, CuboidArea> entry : areas.entrySet()) {
	    CuboidArea a = entry.getValue();
	    Location h = a.getHighLoc();
	    Location l = a.getLowLoc();
	    temp.add(ChatColor.GREEN + "{" + ChatColor.YELLOW + "ID:" + ChatColor.RED + entry.getKey() + " "
		+ ChatColor.YELLOW + "P1:" + ChatColor.RED + "(" + h.getBlockX() + "," + h.getBlockY() + ","
		+ h.getBlockZ() + ") " + ChatColor.YELLOW + "P2:" + ChatColor.RED + "(" + l.getBlockX() + ","
		+ l.getBlockY() + "," + l.getBlockZ() + ") " + ChatColor.YELLOW + "(Size:" + ChatColor.RED
		+ a.getSize() + ChatColor.YELLOW + ")" + ChatColor.GREEN + "} ");
	}
	plugin.getInfoPageManager().printInfo(player, "res area listall " + this.getName(),
	    plugin.msg(lm.General_PhysicalAreas), temp, page);
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
	ClaimedResidence res = parent;
	while (res != null) {
	    count++;
	    res = res.getParent();
	}
	return count;
    }

//    public Location getTeleportLocation() {
//	return getTeleportLocation(null);
//    }

    public Location getTeleportLocation(Player player) {
	if (tpLoc == null) {
	    if (this.getMainArea() == null)
		return null;
	    Location low = this.getMainArea().getLowLoc();
	    Location high = this.getMainArea().getHighLoc();
	    Location t = new Location(low.getWorld(), (low.getBlockX() + high.getBlockX()) / 2,
		(low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high.getBlockZ()) / 2);
	    return this.getMiddleFreeLoc(t, player);
	}
	return tpLoc;
    }

    public void setTpLoc(Player player, boolean resadmin) {
	if (!this.perms.hasResidencePermission(player, false) && !resadmin) {
	    plugin.msg(player, lm.General_NoPermission);
	    return;
	}
	if (!this.containsLoc(player.getLocation())) {
	    plugin.msg(player, lm.Residence_NotIn);
	    return;
	}
	tpLoc = player.getLocation();
	plugin.msg(player, lm.Residence_SetTeleportLocation);
    }

    public int isSafeTp(Player player) {
	if (player.getAllowFlight())
	    return 0;

	if (player.getGameMode() == GameMode.CREATIVE)
	    return 0;

	if (plugin.getNms().isSpectator(player.getGameMode()))
	    return 0;

	if (tpLoc == null)
	    return 0;

	Location tempLoc = new Location(tpLoc.getWorld(), tpLoc.getX(), tpLoc.getY(), tpLoc.getZ());

	int from = (int) tempLoc.getY();

	int fallDistance = 0;
	for (int i = 0; i < 255; i++) {
	    tempLoc.setY(from - i);
	    Block block = tempLoc.getBlock();
	    if (ResidencePlayerListener.isEmptyBlock(block)) {
		fallDistance++;
	    } else {
		break;
	    }
	}
	return fallDistance;
    }

    public void tpToResidence(Player reqPlayer, final Player targetPlayer, boolean resadmin) {

	boolean isAdmin = plugin.isResAdminOn(reqPlayer);
	boolean bypassDelay = ResPerm.tpdelaybypass.hasPermission(targetPlayer);

	if (!resadmin && !isAdmin && !ResPerm.bypass_tp.hasPermission(reqPlayer)
	    && (!this.isOwner(targetPlayer) || this.isOwner(targetPlayer)
		&& Residence.getInstance().getConfigManager().isCanTeleportIncludeOwner())) {
	    ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(reqPlayer);
	    PermissionGroup group = rPlayer.getGroup();
	    if (!group.hasTpAccess()) {
		plugin.msg(reqPlayer, lm.General_TeleportDeny);
		return;
	    }
	    if (!reqPlayer.equals(targetPlayer)) {
		plugin.msg(reqPlayer, lm.General_NoPermission);
		return;
	    }
	    if (!this.perms.playerHas(reqPlayer, Flags.tp, FlagCombo.TrueOrNone)) {
		plugin.msg(reqPlayer, lm.Residence_TeleportNoFlag);
		return;
	    }
	    if (!this.perms.playerHas(reqPlayer, Flags.move, FlagCombo.TrueOrNone)) {
		plugin.msg(reqPlayer, lm.Residence_MoveDeny, this.getName());
		return;
	    }
	}

	if (!plugin.getTeleportMap().containsKey(targetPlayer.getName()) && !isAdmin) {
	    int distance = isSafeTp(reqPlayer);
	    if (distance > 6) {
		plugin.msg(reqPlayer, lm.General_TeleportConfirm, distance);
		plugin.getTeleportMap().put(reqPlayer.getName(), this);
		return;
	    }
	}

	if (plugin.getConfigManager().getTeleportDelay() > 0 && !isAdmin && !resadmin && !bypassDelay) {
	    plugin.msg(reqPlayer, lm.General_TeleportStarted, this.getName(),
		plugin.getConfigManager().getTeleportDelay());
	    if (plugin.getConfigManager().isTeleportTitleMessage())
		TpTimer(reqPlayer, plugin.getConfigManager().getTeleportDelay());
	    plugin.getTeleportDelayMap().add(reqPlayer.getName());
	}

	Location loc = this.getTeleportLocation(targetPlayer);

	if (plugin.getConfigManager().getTeleportDelay() > 0 && !isAdmin && !bypassDelay)
	    performDelaydTp(loc, targetPlayer, reqPlayer, true);
	else
	    performInstantTp(loc, targetPlayer, reqPlayer, true);
    }

    public void TpTimer(final Player player, final int t) {
	ActionBarTitleMessages.sendTitle(player, plugin.msg(lm.General_TeleportTitle),
	    plugin.msg(lm.General_TeleportTitleTime, t));
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		if (!plugin.getTeleportDelayMap().contains(player.getName()))
		    return;
		if (t > 1)
		    TpTimer(player, t - 1);
	    }
	}, 20L);
    }

    public void performDelaydTp(final Location targloc, final Player targetPlayer, Player reqPlayer,
	final boolean near) {
	ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
	plugin.getServ().getPluginManager().callEvent(tpevent);
	if (tpevent.isCancelled())
	    return;

	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		if (targloc == null || targetPlayer == null || !targetPlayer.isOnline())
		    return;
		if (!plugin.getTeleportDelayMap().contains(targetPlayer.getName())
		    && plugin.getConfigManager().getTeleportDelay() > 0)
		    return;
		else if (plugin.getTeleportDelayMap().contains(targetPlayer.getName()))
		    plugin.getTeleportDelayMap().remove(targetPlayer.getName());
		targetPlayer.closeInventory();
		targetPlayer.teleport(targloc);
		if (near)
		    plugin.msg(targetPlayer, lm.Residence_TeleportNear);
		else
		    plugin.msg(targetPlayer, lm.General_TeleportSuccess);
		return;
	    }
	}, plugin.getConfigManager().getTeleportDelay() * 20L);
    }

    private void performInstantTp(final Location targloc, final Player targetPlayer, Player reqPlayer,
	final boolean near) {
	ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
	plugin.getServ().getPluginManager().callEvent(tpevent);
	if (!tpevent.isCancelled()) {
	    targetPlayer.closeInventory();
	    boolean teleported = targetPlayer.teleport(targloc);

	    if (teleported) {
		if (near)
		    plugin.msg(targetPlayer, lm.Residence_TeleportNear);
		else
		    plugin.msg(targetPlayer, lm.General_TeleportSuccess);
	    }
	}
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
	plugin.getResidenceManager().removeChunkList(this.getName());
	areas.remove(id);
	plugin.getResidenceManager().calculateChunks(this.getName());
    }

    public void removeArea(Player player, String id, boolean resadmin) {
	if (this.getPermissions().hasResidencePermission(player, true) || resadmin) {
	    if (!areas.containsKey(id)) {
		plugin.msg(player, lm.Area_NonExist);
		return;
	    }
	    if (areas.size() == 1 && !plugin.getConfigManager().allowEmptyResidences()) {
		plugin.msg(player, lm.Area_RemoveLast);
		return;
	    }

	    ResidenceAreaDeleteEvent resevent = new ResidenceAreaDeleteEvent(player, this,
		player == null ? DeleteCause.OTHER : DeleteCause.PLAYER_DELETE);
	    plugin.getServ().getPluginManager().callEvent(resevent);
	    if (resevent.isCancelled())
		return;

	    removeArea(id);
	    if (player != null)
		plugin.msg(player, lm.Area_Remove);
	} else {
	    if (player != null)
		plugin.msg(player, lm.General_NoPermission);
	}
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new HashMap<>();
	Map<String, Object> areamap = new HashMap<>();

	if (mainRes)
	    root.put("MainResidence", mainRes);
	if (createTime != 0L)
	    root.put("CreatedOn", createTime);

//	if (this.getTown() != null && !this.isSubzone()) {
//	    if (this.getTown().getMainResidence().equals(this))
//		root.put("TownCap", this.getTown().getTownName());
//	    else
//		root.put("Town", this.getTown().getTownName());
//	}

	if (plugin.getConfigManager().isNewSaveMechanic()) {
	    if (enterMessage != null && leaveMessage != null) {
		MinimizeMessages min = plugin.getResidenceManager().addMessageToTempCache(this.getWorld(), enterMessage,
		    leaveMessage);
		if (min == null) {
		    if (enterMessage != null)
			root.put("EnterMessage", enterMessage);
		    if (leaveMessage != null)
			root.put("LeaveMessage", leaveMessage);
		} else {
		    root.put("Messages", min.getId());
		}
	    }
	} else {
	    if (enterMessage != null)
		root.put("EnterMessage", enterMessage);
	    if (leaveMessage != null)
		root.put("LeaveMessage", leaveMessage);
	}

//	if (enterMessage != null)
//	    root.put("EnterMessage", enterMessage);
//
//	if (leaveMessage != null) {
//	    ResidenceManager mng = plugin.getResidenceManager();
//	    Integer id = mng.addLeaveMessageToTempCache(leaveMessage);
//	    root.put("LeaveMessage", id);
//	}

	if (ShopDesc != null)
	    root.put("ShopDescription", ShopDesc);
	if (bank.getStoredMoneyD() != 0)
	    root.put("StoredMoney", bank.getStoredMoneyD());
	if (BlockSellPrice != 0D)
	    root.put("BlockSellPrice", BlockSellPrice);

	if (!ChatPrefix.equals(""))
	    root.put("ChatPrefix", ChatPrefix);
	if (!ChannelColor.name().equals(plugin.getConfigManager().getChatColor().name())
	    && !ChannelColor.name().equals("WHITE")) {
	    root.put("ChannelColor", ChannelColor.name());
	}

	Map<String, Object> map = blacklist.save();
	if (!map.isEmpty())
	    root.put("BlackList", map);
	map = ignorelist.save();
	if (!map.isEmpty())
	    root.put("IgnoreList", map);

	if (plugin.getConfigManager().isNewSaveMechanic()) {
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

	if (tpLoc != null) {
	    if (plugin.getConfigManager().isNewSaveMechanic()) {
		root.put("TPLoc",
		    convertDouble(tpLoc.getX()) + ":" + convertDouble(tpLoc.getY()) + ":"
			+ convertDouble(tpLoc.getZ()) + ":" + convertDouble(tpLoc.getPitch()) + ":"
			+ convertDouble(tpLoc.getYaw()));
	    } else {
		Map<String, Object> tpmap = new HashMap<String, Object>();
		tpmap.put("X", convertDouble(this.tpLoc.getX()));
		tpmap.put("Y", convertDouble(this.tpLoc.getY()));
		tpmap.put("Z", convertDouble(this.tpLoc.getZ()));
		tpmap.put("Pitch", convertDouble(this.tpLoc.getPitch()));
		tpmap.put("Yaw", convertDouble(this.tpLoc.getYaw()));
		root.put("TPLoc", tpmap);
	    }
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
	ClaimedResidence res = new ClaimedResidence(plugin);
	if (root == null)
	    throw new Exception("Null residence!");

	if (root.containsKey("CapitalizedName"))
	    res.resName = ((String) root.get("CapitalizedName"));

	if (root.containsKey("CreatedOn"))
	    res.createTime = ((Long) root.get("CreatedOn"));
	else
	    res.createTime = System.currentTimeMillis();

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

//	if (root.containsKey("TownCap")) {
//	    String townName = (String) root.get("TownCap");
//	    Town t = plugin.getTownManager().getTown(townName);
//	    if (t == null)
//		t = plugin.getTownManager().addTown(townName, res);
//	    else
//		t.setMainResidence(res);
//	    res.setTown(t);
//	} else if (root.containsKey("Town")) {
//	    String townName = (String) root.get("Town");
//	    Town t = plugin.getTownManager().getTown(townName);
//	    if (t == null)
//		t = plugin.getTownManager().addTown(townName);
//	    res.setTown(t);
//	}

	if (root.containsKey("MainResidence"))
	    res.mainRes = (Boolean) root.get("MainResidence");

	if (root.containsKey("BlockSellPrice"))
	    res.BlockSellPrice = (Double) root.get("BlockSellPrice");
	else {
	    res.BlockSellPrice = 0D;
	}

	World world = plugin.getServ().getWorld(res.perms.getWorld());
	if (world == null)
	    throw new Exception("Cant Find World: " + res.perms.getWorld());
	for (Entry<String, Object> map : areamap.entrySet()) {
	    if (map.getValue() instanceof String) {
		// loading new same format
		res.areas.put(map.getKey(), CuboidArea.newLoad((String) map.getValue(), world));
	    } else {
		// loading old format
		res.areas.put(map.getKey(), CuboidArea.load((Map<String, Object>) map.getValue(), world));
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

		if (plugin.getConfigManager().flagsInherit())
		    subres.getPermissions().setParent(res.getPermissions());

		// Adding subzone owner into hies res list if parent zone owner is not same
		// person
		if (subres.getParent() != null && !subres.getOwnerUUID().equals(subres.getParent().getOwnerUUID()))
		    plugin.getPlayerManager().addResidence(subres.getOwner(), subres);

		res.subzones.put(map.getKey().toLowerCase(), subres);
	    }
	}

	if (root.containsKey("EnterMessage") && root.get("EnterMessage") instanceof String)
	    res.enterMessage = (String) root.get("EnterMessage");
	if (root.containsKey("LeaveMessage") && root.get("LeaveMessage") instanceof String)
	    res.leaveMessage = (String) root.get("LeaveMessage");

	if (root.containsKey("Messages") && root.get("Messages") instanceof Integer) {
	    res.enterMessage = plugin.getResidenceManager().getChacheMessageEnter(worldName,
		(Integer) root.get("Messages"));
	    res.leaveMessage = plugin.getResidenceManager().getChacheMessageLeave(worldName,
		(Integer) root.get("Messages"));
	}

	res.parent = parent;

	if (root.get("TPLoc") instanceof String) {
	    String tpLoc = (String) root.get("TPLoc");

	    double pitch = 0.0;
	    double yaw = 0.0;

	    try {
		String[] split = tpLoc.split(":");
		if (split.length > 4)
		    yaw = convertDouble(split[4]);
		if (split.length > 3)
		    pitch = convertDouble(split[3]);
		res.tpLoc = new Location(world, convertDouble(split[0]), convertDouble(split[1]),
		    convertDouble(split[2]));
	    } catch (Exception e) {
	    }

	    res.tpLoc.setPitch((float) pitch);
	    res.tpLoc.setYaw((float) yaw);

	} else {
	    Map<String, Object> tploc = (Map<String, Object>) root.get("TPLoc");
	    if (tploc != null) {
		double pitch = 0.0;
		double yaw = 0.0;

		if (tploc.containsKey("Yaw"))
		    yaw = convertDouble(tploc.get("Yaw").toString());

		if (tploc.containsKey("Pitch"))
		    pitch = convertDouble(tploc.get("Pitch").toString());

		res.tpLoc = new Location(world, convertDouble(tploc.get("X").toString()),
		    convertDouble(tploc.get("Y").toString()), convertDouble(tploc.get("Z").toString()));
		res.tpLoc.setPitch((float) pitch);
		res.tpLoc.setYaw((float) yaw);
	    }
	}

	if (root.containsKey("cmdBlackList"))
	    res.cmdBlackList = (List<String>) root.get("cmdBlackList");
	if (root.containsKey("cmdWhiteList"))
	    res.cmdWhiteList = (List<String>) root.get("cmdWhiteList");

	if (root.containsKey("ChatPrefix"))
	    res.ChatPrefix = (String) root.get("ChatPrefix");

	if (root.containsKey("ChannelColor"))
	    res.ChannelColor = ChatColor.valueOf((String) root.get("ChannelColor"));
	else {
	    res.ChannelColor = plugin.getConfigManager().getChatColor();
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
	if (!plugin.validName(newName)) {
	    plugin.msg(player, lm.Invalid_NameCharacters);
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
		plugin.msg(player, lm.Invalid_Subzone);
	    return false;
	}
	if (player != null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
	    plugin.msg(player, lm.General_NoPermission);
	    return false;
	}
	if (subzones.containsKey(newName)) {
	    if (player != null)
		plugin.msg(player, lm.Subzone_Exists, newName);
	    return false;
	}
	res.setName(newN);
	subzones.put(newName, res);
	subzones.remove(oldName);
	if (player != null)
	    plugin.msg(player, lm.Subzone_Rename, oldName, newName);
	return true;
    }

    public boolean renameArea(String oldName, String newName) {
	return this.renameArea(null, oldName, newName, true);
    }

    public boolean renameArea(Player player, String oldName, String newName, boolean resadmin) {
	if (!plugin.validName(newName)) {
	    plugin.msg(player, lm.Invalid_NameCharacters);
	    return false;
	}
	if (player == null || perms.hasResidencePermission(player, true) || resadmin) {
	    if (areas.containsKey(newName)) {
		if (player != null)
		    plugin.msg(player, lm.Area_Exists);
		return false;
	    }
	    CuboidArea area = areas.get(oldName);
	    if (area == null) {
		if (player != null)
		    plugin.msg(player, lm.Area_InvalidName);
		return false;
	    }
	    areas.put(newName, area);
	    areas.remove(oldName);
	    if (player != null)
		plugin.msg(player, lm.Area_Rename, oldName, newName);
	    return true;
	}
	plugin.msg(player, lm.General_NoPermission);
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
	plugin.getResidenceManager().removeResidence(this);
	plugin.getResidenceManager().removeChunkList(this.getName());
	plugin.getPlayerManager().removeResFromPlayer(this);
    }

    public ResidenceBank getBank() {
	return bank;
    }

    public String getWorld() {
	return perms.getWorld();
    }

    public ResidencePlayer getRPlayer() {
	return plugin.getPlayerManager().getResidencePlayer(this.getPermissions().getOwner());
    }

    public PermissionGroup getOwnerGroup() {
	return getRPlayer().getGroup(getPermissions().getWorld());
    }

    public String getOwner() {
	return perms.getOwner();
    }

    public boolean isOwner(String name) {
	Player player = Bukkit.getPlayer(name);
	if (player != null)
	    return isOwner(player);
	return perms.getOwner().equalsIgnoreCase(name);
    }

    public boolean isOwner(Player p) {
	if (p == null)
	    return false;
	if (plugin.getConfigManager().isOfflineMode())
	    return perms.getOwner().equals(p.getName());
	return perms.getOwnerUUID().equals(p.getUniqueId());
    }

    public boolean isOwner(CommandSender sender) {
	if (plugin.getConfigManager().isOfflineMode()) {
	    return perms.getOwner().equals(sender.getName());
	}
	if (sender instanceof Player) {
	    return perms.getOwnerUUID().equals(((Player) sender).getUniqueId());
	}
	return true;
    }

    public void setChatPrefix(String ChatPrefix) {
	this.ChatPrefix = ChatPrefix;
    }

    public String getChatPrefix() {
	return this.ChatPrefix == null ? "" : this.ChatPrefix;
    }

    public void setChannelColor(ChatColor ChannelColor) {
	this.ChannelColor = ChannelColor;
    }

    public ChatChannel getChatChannel() {
	return plugin.getChatManager().getChannel(this.getName());
    }

    public ChatColor getChannelColor() {
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

    public void setLeaseExpireTime(Long leaseExpireTime) {
	this.leaseExpireTime = leaseExpireTime;
    }

//    public Town getTown() {
//	return town;
//    }
//
//    public void setTown(Town town) {
//	this.town = town;
//    }

    public boolean isUnderSiege() {
	return getSiege().getEndsAt() > System.currentTimeMillis()
	    && getSiege().getStartsAt() < System.currentTimeMillis();
    }

    public boolean canSiege() {
	return !isUnderSiege() && this.getSiege().getCooldownEnd() < System.currentTimeMillis();
    }

    public ResidenceSiege getSiege() {
	if (siege == null)
	    siege = new ResidenceSiege();
	return siege;
    }

    public boolean isUnderSiegeCooldown() {
	return this.getSiege().getCooldownEnd() > System.currentTimeMillis();
    }

    int preSiegeDuration = 5;
    int siegeDuration = 5;

    public boolean startSiege(Player attacker) {

	if (isUnderSiege())
	    return false;

	if (this.getSiege().getCooldownEnd() > System.currentTimeMillis())
	    return false;

	getSiege().addAttacker(attacker);
	getSiege().addDefender(this.getRPlayer().getPlayer());
	getSiege().setStartsAt(System.currentTimeMillis() + (preSiegeDuration * 1000));
	getSiege().setEndsAt(getSiege().getStartsAt() + (siegeDuration * 1000));

	return true;
    }

    public void endSiege() {
	getSiege().setEndsAt(System.currentTimeMillis());
	if (getSiege().getSchedId() > 0) {
	    Bukkit.getScheduler().cancelTask(getSiege().getSchedId());
	    getSiege().setSchedId(-1);
	}

	getSiege().setStartsAt(0L);

	for (Player one : this.getSiege().getAttackers()) {
	    Location outside = this.getOutsideFreeLoc(one.getLocation(), one);
	    if (outside != null)
		one.teleport(outside);
	}
	this.getSiege().getAttackers().clear();
	this.getSiege().getDefenders().clear();
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
	return (int) ((getTotalSize() * getOwnerGroup().getCostPerBlock()) * 100) / 100D;
    }

    public double getWorth() {
	return (int) ((getTotalSize() * getBlockSellPrice()) * 100) / 100.0;
    }

    public void showBounds(Player player, boolean showOneTime) {
	Visualizer v = new Visualizer(player);
	v.setAreas(getAreaArray());
	v.setOnce(showOneTime);
	plugin.getSelectionManager().showBounds(player, v);
    }
}
