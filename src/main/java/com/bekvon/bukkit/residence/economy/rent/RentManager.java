package com.bekvon.bukkit.residence.economy.rent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketRentInterface;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent.RentEventType;
import com.bekvon.bukkit.residence.listeners.ResidenceLWCListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.utils.GetTime;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class RentManager implements MarketRentInterface {
    private Set<ClaimedResidence> rentedLand;
    private Set<ClaimedResidence> rentableLand;

    private static Map<UUID, List<ClaimedResidence>> playerRentedLands = new ConcurrentHashMap<>();
    private static Map<String, List<ClaimedResidence>> byPlayerNameRentedLands = new ConcurrentHashMap<>();

    private Residence plugin;

    public RentManager(Residence plugin) {
        this.plugin = plugin;
        rentedLand = new HashSet<ClaimedResidence>();
        rentableLand = new HashSet<ClaimedResidence>();
    }

    public static void updateUUID(UUID from, UUID to) {
        List<ClaimedResidence> list = playerRentedLands.remove(from);
        if (list != null)
            playerRentedLands.put(to, list);
    }

    private void addRented(ClaimedResidence residence) {
        if (residence != null && residence.getRentedLand() != null)
            addRented(residence.getRentedLand().getUniqueId(), residence.getRentedLand().getRenterName(), residence);
        else
            addRented(null, null, residence);
    }

    private void addRented(Player player, ClaimedResidence residence) {
        addRented(player == null ? null : player.getUniqueId(), player == null ? null : player.getName(), residence);
    }

    private void addRented(UUID uuid, String playerName, ClaimedResidence residence) {
        if (residence == null)
            return;
        rentedLand.add(residence);
        if (uuid != null)
            playerRentedLands.computeIfAbsent(uuid, k -> new ArrayList<>()).add(residence);

        // Only cache by player name if uuid is null
        if (playerName != null && uuid == null)
            byPlayerNameRentedLands.computeIfAbsent(playerName, k -> new ArrayList<>()).add(residence);
    }

    private void removeRented(ClaimedResidence residence) {
        if (residence != null && residence.getRentedLand() != null)
            removeRented(residence.getRentedLand().getUniqueId(), residence.getRentedLand().getRenterName(), residence);
        else
            removeRented(null, null, residence);
    }

    private void removeRented(Player player, ClaimedResidence residence) {
        removeRented(player == null ? null : player.getUniqueId(), player == null ? null : player.getName(), residence);
    }

    private void removeRented(UUID uuid, String playerName, ClaimedResidence residence) {
        if (residence == null)
            return;

        rentedLand.remove(residence);
        if (uuid != null)
            playerRentedLands.computeIfAbsent(uuid, k -> new ArrayList<>()).remove(residence);
        if (playerName != null)
            byPlayerNameRentedLands.computeIfAbsent(playerName, k -> new ArrayList<>()).remove(residence);

        residence.setRented(null);
    }

    @Override
    public RentedLand getRentedLand(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentedLand(res);
    }

    public RentedLand getRentedLand(ClaimedResidence res) {
        if (res == null)
            return null;
        return res.isRented() ? res.getRentedLand() : null;
    }

    @Override
    @Deprecated
    public List<String> getRentedLands(String playername) {
        return getRentedLands(playername, false);
    }

    @Deprecated
    public List<String> getRentedLands(String playername, boolean onlyHidden) {
        List<String> rentedLands = new ArrayList<String>();
        if (playername == null)
            return rentedLands;
        for (ClaimedResidence res : rentedLand) {
            if (res == null)
                continue;

            if (!res.isRented())
                continue;

            if (!res.getRentedLand().isRenter(playername))
                continue;

            String world = " ";
            ClaimedResidence topres = res.getTopParent();
            world = topres.getWorld();

            boolean hidden = topres.getPermissions().has("hidden", false);

            if (onlyHidden && !hidden)
                continue;

            rentedLands.add(lm.Residence_List.getMessage("", res.getName(), world) + lm.Rent_Rented.getMessage());
        }
        return rentedLands;
    }

    public List<ClaimedResidence> getRentedLands(UUID uuid) {
        return getRentedLands(uuid, false, null);
    }

    public List<ClaimedResidence> getRentedLands(UUID uuid, boolean onlyHidden) {
        return getRentedLands(uuid, onlyHidden, null);
    }

    public List<ClaimedResidence> getRentedLands(UUID uuid, boolean onlyHidden, World world) {
        List<ClaimedResidence> rentedLands = new ArrayList<ClaimedResidence>();
        if (uuid == null)
            return rentedLands;

        rentedLands.addAll(parseLands(uuid, onlyHidden, world, playerRentedLands.getOrDefault(uuid, new ArrayList<>())));

        if (!byPlayerNameRentedLands.isEmpty()) {
            String playerName = ResidencePlayer.getName(uuid);
            rentedLands.addAll(parseLands(uuid, onlyHidden, world, byPlayerNameRentedLands.getOrDefault(playerName, new ArrayList<>())));
        }

        return rentedLands;
    }

    private static List<ClaimedResidence> parseLands(UUID uuid, boolean onlyHidden, World world, List<ClaimedResidence> lands) {
        List<ClaimedResidence> rentedLands = new ArrayList<ClaimedResidence>();

        for (ClaimedResidence res : lands) {
            if (res == null)
                continue;

            if (!res.isRented())
                continue;

            if (!res.getRentedLand().isRenter(uuid))
                continue;

            ClaimedResidence topres = res.getTopParent();

            if (world != null && !world.getName().equalsIgnoreCase(res.getWorldName()))
                continue;

            if (onlyHidden && !topres.getPermissions().has("hidden", false))
                continue;

            rentedLands.add(res);
        }
        return rentedLands;

    }

    @Deprecated
    public List<ClaimedResidence> getRents(String playername) {
        return getRents(playername, false);
    }

    @Deprecated
    public List<ClaimedResidence> getRents(String playername, boolean onlyHidden) {
        return getRents(playername, onlyHidden, null);
    }

    @Deprecated
    public List<ClaimedResidence> getRents(String playername, boolean onlyHidden, World world) {
        return getRentedLands(ResidencePlayer.getUUID(playername), onlyHidden, world);
    }

    @Deprecated
    public TreeMap<String, ClaimedResidence> getRentsMap(String playername, boolean onlyHidden, World world) {
        return getRentsMap(ResidencePlayer.getUUID(playername), onlyHidden, world);
    }

    public TreeMap<String, ClaimedResidence> getRentsMap(UUID uuid, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> rentedLands = new TreeMap<String, ClaimedResidence>();
        for (ClaimedResidence res : getRentedLands(uuid, onlyHidden, world)) {
            if (res == null)
                continue;

            if (!res.isRented())
                continue;

            if (!res.getRentedLand().isRenter(uuid))
                continue;

            rentedLands.put(res.getName(), res);
        }
        return rentedLands;
    }

    @Deprecated
    public List<String> getRentedLandsList(Player player) {
        return getRentedLandsList(player.getName());
    }

    @Deprecated
    public List<String> getRentedLandsList(String playername) {

        List<String> rentedLands = new ArrayList<String>();
        List<ClaimedResidence> list = getRentedLandsList(ResidencePlayer.getUUID(playername));

        for (ClaimedResidence res : list) {
            if (res == null)
                continue;
            rentedLands.add(res.getName());
        }

        return rentedLands;
    }

    public List<ClaimedResidence> getRentedLandsList(UUID uuid) {

        List<ClaimedResidence> rentedLands = new ArrayList<ClaimedResidence>();

        if (uuid == null)
            return rentedLands;

        for (ClaimedResidence res : getRentedLands(uuid, false)) {
            if (res == null)
                continue;
            if (!res.isRented())
                continue;
            if (!res.getRentedLand().isRenter(uuid))
                continue;
            rentedLands.add(res);
        }
        return rentedLands;
    }

    @Override
    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean resadmin) {
        setForRent(player, landName, amount, days, AllowRenewing, plugin.getConfigManager().isRentStayInMarket(), resadmin);
    }

    @Override
    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean resadmin) {
        setForRent(player, landName, amount, days, AllowRenewing, StayInMarket, plugin.getConfigManager().isRentAllowAutoPay(), resadmin);
    }

    @Override
    public void setForRent(Player player, String landName, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean AllowAutoPay, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        setForRent(player, res, amount, days, AllowRenewing, StayInMarket, AllowAutoPay, resadmin);
    }

    public void setForRent(Player player, ClaimedResidence res, int amount, int days, boolean AllowRenewing, boolean StayInMarket, boolean AllowAutoPay,
            boolean resadmin) {
        if (amount < 0)
            return;

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (res.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_cantDo.sendMessage(player);
            return;
        }

        if (!plugin.getConfigManager().enabledRentSystem()) {
            lm.Economy_MarketDisabled.sendMessage(player);
            return;
        }

        if (res.isForSell() && !resadmin) {
            lm.Economy_SellRentFail.sendMessage(player);
            return;
        }

        if (res.isParentForSell() && !resadmin) {
            lm.Economy_ParentSellRentFail.sendMessage(player);
            return;
        }

        if (!resadmin) {
            if (!res.getPermissions().hasResidencePermission(player, true)) {
                lm.General_NoPermission.sendMessage(player);
                return;
            }
            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();

            days = group.getMaxRentDays() < days ? group.getMaxRentDays() : days;

            if (this.getRentableCount(player.getUniqueId()) >= group.getMaxRentables()) {
                lm.Residence_MaxRent.sendMessage(player);
                return;
            }
        }

        if (!rentableLand.contains(res)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENTABLE);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;
            RentableLand newrent = new RentableLand();
            newrent.days = days;
            newrent.cost = amount;
            newrent.AllowRenewing = AllowRenewing;
            newrent.StayInMarket = StayInMarket;
            newrent.AllowAutoPay = AllowAutoPay;
            res.setRentable(newrent);
            rentableLand.add(res);

            plugin.getSignUtil().checkSign(res);

            lm.Residence_ForRentSuccess.sendMessage(player, res.getResidenceName(), amount, days);
        } else {
            lm.Residence_AlreadyRent.sendMessage(player);
        }
    }

    @Override
    public void rent(Player player, String landName, boolean AutoPay, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        rent(player, res, AutoPay, resadmin);
    }

    public void rent(Player player, ClaimedResidence res, boolean AutoPay, boolean resadmin) {

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (res.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_cantDo.sendMessage(player);
            return;
        }

        if (!plugin.getConfigManager().enabledRentSystem()) {
            lm.Rent_Disabled.sendMessage(player);
            return;
        }

        if (res.isOwner(player)) {
            lm.Economy_OwnerRentFail.sendMessage(player);
            return;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        rPlayer.forceUpdateGroup();
        if (!resadmin && this.getRentCount(player.getUniqueId()) >= rPlayer.getMaxRents()) {
            lm.Residence_MaxRent.sendMessage(player);
            return;
        }
        if (!res.isForRent()) {
            lm.Residence_NotForRent.sendMessage(player);
            return;
        }
        if (res.isRented()) {
            printRentInfo(player, res.getName());
            return;
        }

        RentableLand land = res.getRentable();

        if (plugin.getEconomyManager().canAfford(player, land.cost)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENT);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;

            if (!land.AllowAutoPay && AutoPay) {
                lm.Residence_CantAutoPay.sendMessage(player);
                AutoPay = false;
            }

            if (plugin.getEconomyManager().transfer(player.getUniqueId(), res.getPermissions().getOwnerUUID(), land.cost)) {
                RentedLand newrent = new RentedLand();
//                newrent.player = player.getName();
                newrent.setUniqueId(player.getUniqueId());
                newrent.startTime = System.currentTimeMillis();
                newrent.endTime = System.currentTimeMillis() + daysToMs(land.days);
                newrent.AutoPay = AutoPay;
                res.setRented(newrent);

                addRented(player, res);

                plugin.getSignUtil().checkSign(res);

                Visualizer v = new Visualizer(player);
                v.setAreas(res);
                plugin.getSelectionManager().showBounds(player, v);

                res.getPermissions().copyUserPermissions(res.getPermissions().getOwnerUUID(), player.getUniqueId());
                res.getPermissions().clearPlayersFlags(res.getPermissions().getOwnerUUID());
                res.getPermissions().applyDefaultRentedFlags();
                lm.Residence_RentSuccess.sendMessage(player, res.getName(), land.days);

                if (plugin.getSchematicManager() != null &&
                        plugin.getConfigManager().RestoreAfterRentEnds &&
                        !plugin.getConfigManager().SchematicsSaveOnFlagChange &&
                        res.getPermissions().has("backup", true)) {
                    plugin.getSchematicManager().save(res);
                }

            } else {
                player.sendMessage(CMIChatColor.RED + "Error, unable to transfer money...");
            }
        } else {
            lm.Economy_NotEnoughMoney.sendMessage(player);
        }
    }

    public void payRent(Player player, String landName, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        payRent(player, res, resadmin);
    }

    public void payRent(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }
        if (!plugin.getConfigManager().enabledRentSystem()) {
            lm.Rent_Disabled.sendMessage(player);
            return;
        }

        if (!res.isForRent()) {
            lm.Residence_NotForRent.sendMessage(player);
            return;
        }

        if (res.isRented() && !getRentingPlayer(res).equals(player.getName()) && !resadmin) {
            lm.Rent_NotByYou.sendMessage(player);
            return;
        }

        RentableLand land = res.getRentable();
        RentedLand rentedLand = res.getRentedLand();

        if (rentedLand == null) {
            lm.Residence_NotRented.sendMessage(player);
            return;
        }

        if (!land.AllowRenewing) {
            lm.Rent_OneTime.sendMessage(player);
            return;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (!resadmin && group.getMaxRentDays() != -1 &&
                msToDays((rentedLand.endTime - System.currentTimeMillis()) + daysToMs(land.days)) >= group.getMaxRentDays()) {
            lm.Rent_MaxRentDays.sendMessage(player, group.getMaxRentDays());
            return;
        }

        if (plugin.getEconomyManager().canAfford(player, land.cost)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENT);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;
            if (plugin.getEconomyManager().transfer(player.getUniqueId(), res.getPermissions().getOwnerUUID(), land.cost)) {
                rentedLand.endTime = rentedLand.endTime + daysToMs(land.days);
                plugin.getSignUtil().checkSign(res);

                Visualizer v = new Visualizer(player);
                v.setAreas(res);
                plugin.getSelectionManager().showBounds(player, v);

                lm.Rent_Extended.sendMessage(player, land.days, res.getName());
                lm.Rent_Expire.sendMessage(player, GetTime.getTime(rentedLand.endTime));
            } else {
                player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
            }
        } else {
            lm.Economy_NotEnoughMoney.sendMessage(player);
        }
    }

    @Override
    public void unrent(Player player, String landName, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        unrent(player, res, resadmin);
    }

    public void unrent(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (res.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_cantDo.sendMessage(player);
            return;
        }

        RentedLand rent = res.getRentedLand();
        if (rent == null) {
            lm.Residence_NotRented.sendMessage(player);
            return;
        }

        if (resadmin || rent.isRenter(player) || res.isOwner(player) && ResPerm.market_evict.hasPermission(player)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.UNRENTABLE);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;

            removeRented(player, res);

            res.setRented(null);
            if (!res.getRentable().AllowRenewing && !res.getRentable().StayInMarket) {
                rentableLand.remove(res);
                res.setRentable(null);
            }

            boolean backup = res.getPermissions().has("backup", false);

            if (plugin.getConfigManager().isRemoveLwcOnUnrent() && plugin.isLwcPresent())
                ResidenceLWCListener.removeLwcFromResidence(player, res);

            res.getPermissions().applyDefaultFlags();

            if (plugin.getSchematicManager() != null && plugin.getConfigManager().RestoreAfterRentEnds && backup) {
                plugin.getSchematicManager().load(res);
                // set true if its already exists
                res.getPermissions().setFlag("backup", FlagState.TRUE);
            }
            plugin.getSignUtil().checkSign(res);

            lm.Residence_Unrent.sendMessage(player, res.getName());
        } else {
            lm.General_NoPermission.sendMessage(player);
        }
    }

    private static long daysToMs(int days) {
        return ((days) * 24L * 60L * 60L * 1000L);
    }

    private static int msToDays(long ms) {
        return (int) Math.ceil((((ms / 1000D) / 60D) / 60D) / 24D);
    }

    @Override
    public void removeFromForRent(Player player, String landName, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        removeFromForRent(player, res, resadmin);
    }

    public void removeFromForRent(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
            lm.General_NoPermission.sendMessage(player);
            return;
        }

        if (rentableLand.contains(res)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.UNRENT);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;
            rentableLand.remove(res);
            res.setRentable(null);
            res.getPermissions().applyDefaultFlags();
            plugin.getSignUtil().checkSign(res);
            lm.Residence_RemoveRentable.sendMessage(player, res.getResidenceName());
        } else {
            lm.Residence_NotForRent.sendMessage(player);
        }
    }

    @Override
    @Deprecated
    public void removeFromRent(String landName) {
        removeRented(ClaimedResidence.getByName(landName));
    }

    public void removeFromRent(ClaimedResidence res) {
        removeRented(res);
    }

    @Override
    @Deprecated
    public void removeRentable(String landName) {
        removeRentable(ClaimedResidence.getByName(landName));
    }

    public void removeRentable(ClaimedResidence res) {
        removeRentable(res, true);
    }

    public void removeRentable(ClaimedResidence res, boolean removeSigns) {
        if (res == null)
            return;
        removeRented(res);
        rentableLand.remove(res);
        if (removeSigns)
            plugin.getSignUtil().removeSign(res);
    }

    @Override
    @Deprecated
    public boolean isForRent(String landName) {
        return isForRent(ClaimedResidence.getByName(landName));
    }

    public boolean isForRent(ClaimedResidence res) {
        if (res == null)
            return false;
        return rentableLand.contains(res);
    }

    @Deprecated
    public RentableLand getRentableLand(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentableLand(res);
    }

    public RentableLand getRentableLand(ClaimedResidence res) {
        if (res == null)
            return null;
        if (res.isForRent())
            return res.getRentable();
        return null;
    }

    @Override
    @Deprecated
    public boolean isRented(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return isRented(res);
    }

    public boolean isRented(ClaimedResidence res) {
        if (res == null)
            return false;
        return rentedLand.contains(res);
    }

    @Override
    @Deprecated
    public String getRentingPlayer(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentingPlayer(res);
    }

    public String getRentingPlayer(ClaimedResidence res) {
        if (res == null)
            return null;
        return res.isRented() ? res.getRentedLand().getRenterName() : null;
    }

    @Override
    @Deprecated
    public int getCostOfRent(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getCostOfRent(res);
    }

    public int getCostOfRent(ClaimedResidence res) {
        if (res == null)
            return 0;
        return res.isForRent() ? res.getRentable().cost : 0;
    }

    @Override
    @Deprecated
    public boolean getRentableRepeatable(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentableRepeatable(res);
    }

    public boolean getRentableRepeatable(ClaimedResidence res) {
        if (res == null)
            return false;
        return res.isForRent() ? res.getRentable().AllowRenewing : false;
    }

    @Override
    @Deprecated
    public boolean getRentedAutoRepeats(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentedAutoRepeats(res);
    }

    public boolean getRentedAutoRepeats(ClaimedResidence res) {
        if (res == null)
            return false;
        return getRentableRepeatable(res) ? (isRented(res) ? res.getRentedLand().AutoPay : false) : false;
    }

    @Override
    @Deprecated
    public int getRentDays(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentDays(res);
    }

    public int getRentDays(ClaimedResidence res) {
        if (res == null)
            return 0;
        return res.isForRent() ? res.getRentable().days : 0;
    }

    @Override
    public void checkCurrentRents() {
        Set<ClaimedResidence> t = new HashSet<ClaimedResidence>();
        t.addAll(rentedLand);
        for (ClaimedResidence res : t) {

            if (res == null)
                continue;

            RentedLand land = res.getRentedLand();
            if (land == null)
                continue;

            if (land.endTime > System.currentTimeMillis())
                continue;

            if (plugin.getConfigManager().debugEnabled())
                lm.consoleMessage("Rent Check: " + res.getName());

            ResidenceRentEvent revent = new ResidenceRentEvent(res, null, RentEventType.RENT_EXPIRE);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                continue;

            RentableLand rentable = res.getRentable();
            if (!rentable.AllowRenewing) {
                if (!rentable.StayInMarket) {
                    rentableLand.remove(res);
                    res.setRentable(null);
                }

                removeRented(res);

                res.getPermissions().applyDefaultFlags();
                plugin.getSignUtil().checkSign(res);
                continue;
            }

            UUID renterUUID = land.getUniqueId();

            if (renterUUID != null && land.AutoPay && rentable.AllowAutoPay) {

                Double money = 0D;
                if (plugin.getConfigManager().isDeductFromBankThenPlayer()) {
                    money += res.getBank().getStoredMoneyD();
                    money += plugin.getEconomyManager().getBalance(renterUUID);
                } else if (plugin.getConfigManager().isDeductFromBank()) {
                    money += res.getBank().getStoredMoneyD();
                } else {
                    money += plugin.getEconomyManager().getBalance(renterUUID);
                }

                if (money < rentable.cost) {
                    if (!rentable.StayInMarket) {
                        rentableLand.remove(res);
                        res.setRentable(null);
                    }
                    removeRented(res);
                    res.getPermissions().applyDefaultFlags();
                } else {

                    boolean updatedTime = true;
                    if (plugin.getConfigManager().isDeductFromBankThenPlayer()) {
                        double deductFromPlayer = rentable.cost;
                        double leftInBank = res.getBank().getStoredMoneyD();
                        if (leftInBank < deductFromPlayer) {
                            deductFromPlayer = deductFromPlayer - leftInBank;
                            leftInBank = 0D;
                        } else {
                            leftInBank = leftInBank - deductFromPlayer;
                            deductFromPlayer = 0D;
                        }
                        leftInBank = leftInBank < 0 ? 0 : leftInBank;

                        if (plugin.getEconomyManager().getBalance(renterUUID) < deductFromPlayer) {
                            updatedTime = false;
                        } else {
                            if (deductFromPlayer == 0D || plugin.getEconomyManager().subtract(renterUUID, deductFromPlayer)) {
                                plugin.getEconomyManager().add(res.getPermissions().getOwnerUUID(), rentable.cost);
                                res.getBank().setStoredMoney(leftInBank);
                                updatedTime = true;
                            }
                        }
                    } else if (plugin.getConfigManager().isDeductFromBank()) {
                        double deductFromPlayer = rentable.cost;
                        double leftInBank = res.getBank().getStoredMoneyD();
                        if (leftInBank < deductFromPlayer) {
                            updatedTime = false;
                        } else {
                            res.getBank().setStoredMoney(leftInBank - deductFromPlayer);
                            plugin.getEconomyManager().add(res.getPermissions().getOwnerUUID(), rentable.cost);
                            updatedTime = true;
                        }
                    } else {
                        updatedTime = plugin.getEconomyManager().transfer(renterUUID, res.getPermissions().getOwnerUUID(), rentable.cost);
                    }

                    if (!updatedTime) {
                        if (!rentable.StayInMarket) {
                            rentableLand.remove(res);
                            res.setRentable(null);
                        }
                        removeRented(res);
                        res.getPermissions().applyDefaultFlags();
                    } else {
                        land.endTime = System.currentTimeMillis() + daysToMs(rentable.days);
                    }
                }

                plugin.getSignUtil().checkSign(res);
                continue;
            }
            if (!rentable.StayInMarket) {
                rentableLand.remove(res);
                res.setRentable(null);
            }

            removeRented(res);

            boolean backup = res.getPermissions().has("backup", false);

            res.getPermissions().applyDefaultFlags();

            if (plugin.getSchematicManager() != null && plugin.getConfigManager().RestoreAfterRentEnds && backup) {
                plugin.getSchematicManager().load(res);
                plugin.getSignUtil().checkSign(res);
                // set true if its already exists
                res.getPermissions().setFlag("backup", FlagState.TRUE);
                // To avoid lag spikes on multiple residence restores at once, will limit to one
                // residence at time
                break;
            }
            plugin.getSignUtil().checkSign(res);
        }
    }

    @Override
    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        setRentRepeatable(player, res, value, resadmin);
    }

    public void setRentRepeatable(Player player, ClaimedResidence res, boolean value, boolean resadmin) {

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        RentableLand land = res.getRentable();

        if (!res.isOwner(player) && !resadmin) {
            lm.Residence_NotOwner.sendMessage(player);
            return;
        }

        if (land == null || !res.isOwner(player) && !resadmin) {
            lm.Residence_NotOwner.sendMessage(player);
            return;
        }

        land.AllowRenewing = value;
        if (!value && this.isRented(res))
            res.getRentedLand().AutoPay = false;

        if (value)
            lm.Rentable_EnableRenew.sendMessage(player, res.getResidenceName());
        else
            lm.Rentable_DisableRenew.sendMessage(player, res.getResidenceName());

    }

    @Override
    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        setRentedRepeatable(player, res, value, resadmin);
    }

    public void setRentedRepeatable(Player player, ClaimedResidence res, boolean value, boolean resadmin) {
        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        RentedLand land = res.getRentedLand();

        if (land == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        if (!res.getRentable().AllowAutoPay && value) {
            lm.Residence_CantAutoPay.sendMessage(player);
            return;
        }

        if (!land.isRenter(player) && !resadmin) {
            lm.Residence_NotOwner.sendMessage(player);
            return;
        }

        if (!land.isRenter(player) && !resadmin) {
            lm.Residence_NotOwner.sendMessage(player);
            return;
        }

        land.AutoPay = value;
        if (value)
            lm.Rent_EnableRenew.sendMessage(player, res.getResidenceName());
        else
            lm.Rent_DisableRenew.sendMessage(player, res.getResidenceName());

        plugin.getSignUtil().checkSign(res);
    }

    public void printRentInfo(Player player, String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        printRentInfo(player, res);
    }

    public void printRentInfo(Player player, ClaimedResidence res) {

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return;
        }

        RentableLand rentable = res.getRentable();
        RentedLand rented = res.getRentedLand();
        if (rentable != null) {
            lm.General_Separator.sendMessage(player);
            lm.General_Land.sendMessage(player, res.getName());
            lm.General_Cost.sendMessage(player, rentable.cost, rentable.days);
            lm.Rentable_AllowRenewing.sendMessage(player, rentable.AllowRenewing);
            lm.Rentable_StayInMarket.sendMessage(player, rentable.StayInMarket);
            lm.Rentable_AllowAutoPay.sendMessage(player, rentable.AllowAutoPay);
            if (rented != null) {
                lm.Residence_RentedBy.sendMessage(player, rented.getRenterName());

                if (rented.isRenter(player) || res.isOwner(player) || ResAdmin.isResAdmin(player)) {
                    if (rented.AutoPay)
                        lm.Rent_AutoPayTurnedOn.sendMessage(player);
                    else
                        lm.Rent_AutoPayTurnedOff.sendMessage(player);
                }
                lm.Rent_Expire.sendMessage(player, GetTime.getTime(rented.endTime));
            } else {
                lm.General_Status.sendMessage(player, lm.General_Available.getMessage());
            }
            lm.General_Separator.sendMessage(player);
        } else {
            lm.General_Separator.sendMessage(player);
            lm.Residence_NotForRent.sendMessage(player);
            lm.General_Separator.sendMessage(player);
        }
    }

    public void printRentableResidences(Player player, int page) {
        lm.Rentable_Land.sendMessage(player);
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(ChatColor.GREEN);

        PageInfo pi = new PageInfo(10, rentableLand.size(), page);
        int position = -1;
        for (ClaimedResidence res : rentableLand) {
            if (res == null)
                continue;

            position++;

            if (position > pi.getEnd())
                break;
            if (!pi.isInRange(position))
                continue;
            boolean rented = res.isRented();

            if (!res.getRentable().AllowRenewing && rented)
                continue;

            String rentedBy = "";
            String hover = "";
            if (rented) {
                RentedLand rent = res.getRentedLand();
                rentedBy = lm.Residence_RentedBy.getMessage(rent.getRenterName());
                hover = GetTime.getTime(rent.endTime);
            }

            String msg = lm.Rent_RentList.getMessage(pi.getPositionForOutput(position), res.getName(), res.getRentable().cost, res.getRentable().days, res.getRentable().AllowRenewing,
                    res.getOwner(), rentedBy);

            RawMessage rm = new RawMessage();
            rm.addText(msg).addHover("&2" + hover);

            if (!hover.equalsIgnoreCase("")) {
                rm.show(player);
            } else {
                player.sendMessage(msg);
            }
        }
        pi.autoPagination(player, "/res market list rent");

    }

    @Override
    public int getRentCount(UUID playerUUID) {
        int count = playerRentedLands.getOrDefault(playerUUID, new ArrayList<>()).size();

        if (!byPlayerNameRentedLands.isEmpty()) {
            ResidencePlayer rp = plugin.getPlayerManager().getResidencePlayer(playerUUID);
            if (rp != null) {
                count += byPlayerNameRentedLands.getOrDefault(rp.getName(), new ArrayList<>()).size();
            }
        }

        return count;
    }

    @Override
    @Deprecated
    public int getRentCount(String player) {
        return getRentCount(ResidencePlayer.getUUID(player));
    }

    @Override
    public int getRentableCount(UUID playerUUID) {
        if (playerUUID == null)
            return 0;

        int count = 0;
        for (ClaimedResidence res : rentableLand) {
            if (res != null && res.isOwner(playerUUID))
                count++;
        }
        return count;
    }

    @Override
    @Deprecated
    public int getRentableCount(String player) {
        return getRentableCount(ResidencePlayer.getUUID(player));
    }

    @Override
    public Set<ClaimedResidence> getRentableResidences() {
        return rentableLand;
    }

    @Override
    public Set<ClaimedResidence> getCurrentlyRentedResidences() {
        return rentedLand;
    }

    @SuppressWarnings("unchecked")
    public void load(Map<String, Object> root) {
        if (root == null)
            return;
        this.rentableLand.clear();

        Map<String, Object> rentables = (Map<String, Object>) root.get("Rentables");
        for (Entry<String, Object> rent : rentables.entrySet()) {
            RentableLand one = loadRentable((Map<String, Object>) rent.getValue());
            ClaimedResidence res = plugin.getResidenceManager().getByName(rent.getKey());
            if (res != null) {
                res.setRentable(one);
                this.rentableLand.add(res);
            }
        }
        Map<String, Object> rented = (Map<String, Object>) root.get("Rented");
        for (Entry<String, Object> rent : rented.entrySet()) {
            RentedLand one = RentedLand.loadRented((Map<String, Object>) rent.getValue());
            ClaimedResidence res = plugin.getResidenceManager().getByName(rent.getKey());

            if (res == null)
                continue;

            res.setRented(one);
            addRented(res);
        }
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new HashMap<String, Object>();
        Map<String, Object> rentables = new HashMap<String, Object>();
        for (ClaimedResidence res : rentableLand) {
            if (res == null || res.getRentable() == null)
                continue;
            rentables.put(res.getName(), res.getRentable().save());
        }
        Map<String, Object> rented = new HashMap<String, Object>();
        for (ClaimedResidence res : rentedLand) {
            if (res == null || res.getRentedLand() == null)
                continue;
            rented.put(res.getName(), res.getRentedLand().save());
        }
        root.put("Rentables", rentables);
        root.put("Rented", rented);
        return root;
    }

    private static RentableLand loadRentable(Map<String, Object> map) {
        RentableLand newland = new RentableLand();
        newland.cost = (Integer) map.get("Cost");
        newland.days = (Integer) map.get("Days");
        newland.AllowRenewing = (Boolean) map.get("Repeatable");
        if (map.containsKey("StayInMarket"))
            newland.StayInMarket = (Boolean) map.get("StayInMarket");
        if (map.containsKey("AllowAutoPay"))
            newland.AllowAutoPay = (Boolean) map.get("AllowAutoPay");
        return newland;
    }

}
