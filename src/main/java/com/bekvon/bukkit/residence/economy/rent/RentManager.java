package com.bekvon.bukkit.residence.economy.rent;

import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.MarketRentInterface;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent.RentEventType;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;
import com.bekvon.bukkit.residence.text.help.PageInfo;
import com.bekvon.bukkit.residence.utils.GetTime;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

public class RentManager implements MarketRentInterface {
    private Set<ClaimedResidence> rentedLand;
    private Set<ClaimedResidence> rentableLand;
    private Residence plugin;

    public RentManager(Residence plugin) {
        this.plugin = plugin;
        rentedLand = new HashSet<ClaimedResidence>();
        rentableLand = new HashSet<ClaimedResidence>();
    }

    private static long daysToMs(int days) {
//	return (((long) days) * 1000L);
        return ((days) * 24L * 60L * 60L * 1000L);
    }

    private static int msToDays(long ms) {
        return (int) Math.ceil((((ms / 1000D) / 60D) / 60D) / 24D);
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

    private static RentedLand loadRented(Map<String, Object> map) {
        RentedLand newland = new RentedLand();
        newland.player = (String) map.get("Player");
        newland.startTime = (Long) map.get("StartTime");
        newland.endTime = (Long) map.get("EndTime");
        newland.AutoPay = (Boolean) map.get("AutoRefresh");
        return newland;
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
    public List<String> getRentedLands(String playername) {
        return getRentedLands(playername, false);
    }

    public List<String> getRentedLands(String playername, boolean onlyHidden) {
        List<String> rentedLands = new ArrayList<String>();
        if (playername == null)
            return rentedLands;
        for (ClaimedResidence res : rentedLand) {
            if (res == null)
                continue;

            if (!res.isRented())
                continue;

            if (!res.getRentedLand().player.equals(playername))
                continue;

            String world = " ";
            ClaimedResidence topres = res.getTopParent();
            world = topres.getWorld();

            boolean hidden = topres.getPermissions().has("hidden", false);

            if (onlyHidden && !hidden)
                continue;

            rentedLands.add(plugin.msg(lm.Residence_List, "", res.getName(), world)
                    + plugin.msg(lm.Rent_Rented));
        }
        return rentedLands;
    }

    public List<ClaimedResidence> getRents(String playername) {
        return getRents(playername, false);
    }

    public List<ClaimedResidence> getRents(String playername, boolean onlyHidden) {
        return getRents(playername, onlyHidden, null);
    }

    public List<ClaimedResidence> getRents(String playername, boolean onlyHidden, World world) {
        List<ClaimedResidence> rentedLands = new ArrayList<ClaimedResidence>();
        for (ClaimedResidence res : rentedLand) {
            if (res == null)
                continue;

            if (!res.isRented())
                continue;

            if (!res.getRentedLand().player.equalsIgnoreCase(playername))
                continue;

            ClaimedResidence topres = res.getTopParent();

            boolean hidden = topres.getPermissions().has("hidden", false);

            if (onlyHidden && !hidden)
                continue;

            if (world != null && !world.getName().equalsIgnoreCase(res.getWorld()))
                continue;
            rentedLands.add(res);
        }
        return rentedLands;
    }

    public TreeMap<String, ClaimedResidence> getRentsMap(String playername, boolean onlyHidden, World world) {
        TreeMap<String, ClaimedResidence> rentedLands = new TreeMap<String, ClaimedResidence>();
        for (ClaimedResidence res : rentedLand) {
            if (res == null)
                continue;

            if (!res.isRented())
                continue;

            if (!res.getRentedLand().player.equalsIgnoreCase(playername))
                continue;

            ClaimedResidence topres = res.getTopParent();

            boolean hidden = topres.getPermissions().has("hidden", false);

            if (onlyHidden && !hidden)
                continue;

            if (world != null && !world.getName().equalsIgnoreCase(res.getWorld()))
                continue;
            rentedLands.put(res.getName(), res);
        }
        return rentedLands;
    }

    public List<String> getRentedLandsList(Player player) {
        return getRentedLandsList(player.getName());
    }

    public List<String> getRentedLandsList(String playername) {
        List<String> rentedLands = new ArrayList<String>();
        for (ClaimedResidence res : rentedLand) {
            if (res == null)
                continue;
            if (!res.isRented())
                continue;
            if (!res.getRentedLand().player.equalsIgnoreCase(playername))
                continue;
            rentedLands.add(res.getName());
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

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        if (res.isRaidInitialized() && !resadmin) {
            plugin.msg(player, lm.Raid_cantDo);
            return;
        }

        if (!plugin.getConfigManager().enabledRentSystem()) {
            plugin.msg(player, lm.Economy_MarketDisabled);
            return;
        }

        if (res.isForSell() && !resadmin) {
            plugin.msg(player, lm.Economy_SellRentFail);
            return;
        }

        if (res.isParentForSell() && !resadmin) {
            plugin.msg(player, lm.Economy_ParentSellRentFail);
            return;
        }

        if (!resadmin) {
            if (!res.getPermissions().hasResidencePermission(player, true)) {
                plugin.msg(player, lm.General_NoPermission);
                return;
            }
            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();

            days = group.getMaxRentDays() < days ? group.getMaxRentDays() : days;

            if (this.getRentableCount(player.getName()) >= group.getMaxRentables()) {
                plugin.msg(player, lm.Residence_MaxRent);
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

            plugin.getSignUtil().CheckSign(res);

            plugin.msg(player, lm.Residence_ForRentSuccess, res.getResidenceName(), amount, days);
        } else {
            plugin.msg(player, lm.Residence_AlreadyRent);
        }
    }

    @Override
    public void rent(Player player, String landName, boolean AutoPay, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        rent(player, res, AutoPay, resadmin);
    }

    @SuppressWarnings("deprecation")
    public void rent(Player player, ClaimedResidence res, boolean AutoPay, boolean resadmin) {

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        if (res.isRaidInitialized() && !resadmin) {
            plugin.msg(player, lm.Raid_cantDo);
            return;
        }

        if (!plugin.getConfigManager().enabledRentSystem()) {
            plugin.msg(player, lm.Rent_Disabled);
            return;
        }

        if (res.isOwner(player)) {
            plugin.msg(player, lm.Economy_OwnerRentFail);
            return;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        if (!resadmin && this.getRentCount(player.getName()) >= rPlayer.getMaxRents()) {
            plugin.msg(player, lm.Residence_MaxRent);
            return;
        }
        if (!res.isForRent()) {
            plugin.msg(player, lm.Residence_NotForRent);
            return;
        }
        if (res.isRented()) {
            printRentInfo(player, res.getName());
            return;
        }

        RentableLand land = res.getRentable();

        if (plugin.getEconomyManager().canAfford(player.getName(), land.cost)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENT);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;

            if (!land.AllowAutoPay && AutoPay) {
                plugin.msg(player, lm.Residence_CantAutoPay);
                AutoPay = false;
            }

            if (plugin.getEconomyManager().transfer(player.getName(), res.getPermissions().getOwner(), land.cost)) {
                RentedLand newrent = new RentedLand();
                newrent.player = player.getName();
                newrent.startTime = System.currentTimeMillis();
                newrent.endTime = System.currentTimeMillis() + daysToMs(land.days);
                newrent.AutoPay = AutoPay;
                res.setRented(newrent);
                rentedLand.add(res);

                plugin.getSignUtil().CheckSign(res);

                Visualizer v = new Visualizer(player);
                v.setAreas(res);
                plugin.getSelectionManager().showBounds(player, v);

                res.getPermissions().copyUserPermissions(res.getPermissions().getOwner(), player.getName());
                res.getPermissions().clearPlayersFlags(res.getPermissions().getOwner());
                res.getPermissions().applyDefaultRentedFlags();
                plugin.msg(player, lm.Residence_RentSuccess, res.getName(), land.days);

                if (plugin.getSchematicManager() != null &&
                        plugin.getConfigManager().RestoreAfterRentEnds &&
                        !plugin.getConfigManager().SchematicsSaveOnFlagChange &&
                        res.getPermissions().has("backup", true)) {
                    plugin.getSchematicManager().save(res);
                }

            } else {
                player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
            }
        } else {
            plugin.msg(player, lm.Economy_NotEnoughMoney);
        }
    }

    public void payRent(Player player, String landName, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        payRent(player, res, resadmin);
    }

    public void payRent(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }
        if (!plugin.getConfigManager().enabledRentSystem()) {
            plugin.msg(player, lm.Rent_Disabled);
            return;
        }

        if (!res.isForRent()) {
            plugin.msg(player, lm.Residence_NotForRent);
            return;
        }

        if (res.isRented() && !getRentingPlayer(res).equals(player.getName()) && !resadmin) {
            plugin.msg(player, lm.Rent_NotByYou);
            return;
        }

        RentableLand land = res.getRentable();
        RentedLand rentedLand = res.getRentedLand();

        if (rentedLand == null) {
            plugin.msg(player, lm.Residence_NotRented);
            return;
        }

        if (!land.AllowRenewing) {
            plugin.msg(player, lm.Rent_OneTime);
            return;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (!resadmin && group.getMaxRentDays() != -1 &&
                msToDays((rentedLand.endTime - System.currentTimeMillis()) + daysToMs(land.days)) >= group.getMaxRentDays()) {
            plugin.msg(player, lm.Rent_MaxRentDays, group.getMaxRentDays());
            return;
        }

        if (plugin.getEconomyManager().canAfford(player.getName(), land.cost)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.RENT);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;
            if (plugin.getEconomyManager().transfer(player.getName(), res.getPermissions().getOwner(), land.cost)) {
                rentedLand.endTime = rentedLand.endTime + daysToMs(land.days);
                plugin.getSignUtil().CheckSign(res);

                Visualizer v = new Visualizer(player);
                v.setAreas(res);
                plugin.getSelectionManager().showBounds(player, v);

                plugin.msg(player, lm.Rent_Extended, land.days, res.getName());
                plugin.msg(player, lm.Rent_Expire, GetTime.getTime(rentedLand.endTime));
            } else {
                player.sendMessage(ChatColor.RED + "Error, unable to transfer money...");
            }
        } else {
            plugin.msg(player, lm.Economy_NotEnoughMoney);
        }
    }

    @Override
    public void unrent(Player player, String landName, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        unrent(player, res, resadmin);
    }

    public void unrent(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        if (res.isRaidInitialized() && !resadmin) {
            plugin.msg(player, lm.Raid_cantDo);
            return;
        }

        RentedLand rent = res.getRentedLand();
        if (rent == null) {
            plugin.msg(player, lm.Residence_NotRented);
            return;
        }

        if (resadmin || rent.player.equals(player.getName()) || res.isOwner(player) && ResPerm.market_evict.hasPermission(player)) {
            ResidenceRentEvent revent = new ResidenceRentEvent(res, player, RentEventType.UNRENTABLE);
            plugin.getServ().getPluginManager().callEvent(revent);
            if (revent.isCancelled())
                return;

            rentedLand.remove(res);
            res.setRented(null);
            if (!res.getRentable().AllowRenewing && !res.getRentable().StayInMarket) {
                rentableLand.remove(res);
                res.setRentable(null);
            }

            boolean backup = res.getPermissions().has("backup", false);

            res.getPermissions().applyDefaultFlags();

            if (plugin.getSchematicManager() != null && plugin.getConfigManager().RestoreAfterRentEnds && backup) {
                plugin.getSchematicManager().load(res);
                // set true if its already exists
                res.getPermissions().setFlag("backup", FlagState.TRUE);
            }
            plugin.getSignUtil().CheckSign(res);

            plugin.msg(player, lm.Residence_Unrent, res.getName());
        } else {
            plugin.msg(player, lm.General_NoPermission);
        }
    }

    @Override
    public void removeFromForRent(Player player, String landName, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        removeFromForRent(player, res, resadmin);
    }

    public void removeFromForRent(Player player, ClaimedResidence res, boolean resadmin) {
        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        if (!res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
            plugin.msg(player, lm.General_NoPermission);
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
            plugin.getSignUtil().CheckSign(res);
            plugin.msg(player, lm.Residence_RemoveRentable, res.getResidenceName());
        } else {
            plugin.msg(player, lm.Residence_NotForRent);
        }
    }

    @Override
    public void removeFromRent(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        removeFromRent(res);
    }

    public void removeFromRent(ClaimedResidence res) {
        rentedLand.remove(res);
    }

    @Override
    public void removeRentable(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        removeRentable(res);
    }

    public void removeRentable(ClaimedResidence res) {
        if (res == null)
            return;
        removeFromRent(res);
        rentableLand.remove(res);
        plugin.getSignUtil().removeSign(res.getName());
    }

    @Override
    public boolean isForRent(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return isForRent(res);
    }

    public boolean isForRent(ClaimedResidence res) {
        if (res == null)
            return false;
        return rentableLand.contains(res);
    }

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
    public String getRentingPlayer(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentingPlayer(res);
    }

    public String getRentingPlayer(ClaimedResidence res) {
        if (res == null)
            return null;
        return res.isRented() ? res.getRentedLand().player : null;
    }

    @Override
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
    public boolean getRentedAutoRepeats(String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        return getRentedAutoRepeats(res);
    }

    public boolean getRentedAutoRepeats(ClaimedResidence res) {
        if (res == null)
            return false;
        return getRentableRepeatable(res) ? (rentedLand.contains(res) ? res.getRentedLand().AutoPay : false) : false;
    }

    @Override
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
                System.out.println("Rent Check: " + res.getName());

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
                rentedLand.remove(res);
                res.setRented(null);
                res.getPermissions().applyDefaultFlags();
                plugin.getSignUtil().CheckSign(res);
                continue;
            }
            if (land.AutoPay && rentable.AllowAutoPay) {

                Double money = 0D;
                if (plugin.getConfigManager().isDeductFromBankThenPlayer()) {
                    money += res.getBank().getStoredMoneyD();
                    money += plugin.getEconomyManager().getBalance(land.player);
                } else if (plugin.getConfigManager().isDeductFromBank()) {
                    money += res.getBank().getStoredMoneyD();
                } else {
                    money += plugin.getEconomyManager().getBalance(land.player);
                }

                if (money < rentable.cost) {
                    if (!rentable.StayInMarket) {
                        rentableLand.remove(res);
                        res.setRentable(null);
                    }
                    rentedLand.remove(res);
                    res.setRented(null);
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

                        if (plugin.getEconomyManager().getBalance(land.player) < deductFromPlayer) {
                            updatedTime = false;
                        } else {
                            if (deductFromPlayer == 0D || plugin.getEconomyManager().subtract(land.player, deductFromPlayer)) {
                                plugin.getEconomyManager().add(res.getPermissions().getOwner(), rentable.cost);
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
                            plugin.getEconomyManager().add(res.getPermissions().getOwner(), rentable.cost);
                            updatedTime = true;
                        }
                    } else {
                        updatedTime = plugin.getEconomyManager().transfer(land.player, res.getPermissions().getOwner(), rentable.cost);
                    }

                    if (!updatedTime) {
                        if (!rentable.StayInMarket) {
                            rentableLand.remove(res);
                            res.setRentable(null);
                        }
                        rentedLand.remove(res);
                        res.setRented(null);
                        res.getPermissions().applyDefaultFlags();
                    } else {
                        land.endTime = System.currentTimeMillis() + daysToMs(rentable.days);
                    }
                }

                plugin.getSignUtil().CheckSign(res);
                continue;
            }
            if (!rentable.StayInMarket) {
                rentableLand.remove(res);
                res.setRentable(null);
            }
            rentedLand.remove(res);
            res.setRented(null);

            boolean backup = res.getPermissions().has("backup", false);

            res.getPermissions().applyDefaultFlags();

            if (plugin.getSchematicManager() != null && plugin.getConfigManager().RestoreAfterRentEnds && backup) {
                plugin.getSchematicManager().load(res);
                plugin.getSignUtil().CheckSign(res);
                // set true if its already exists
                res.getPermissions().setFlag("backup", FlagState.TRUE);
                // To avoid lag spikes on multiple residence restores at once, will limit to one residence at time
                break;
            }
            plugin.getSignUtil().CheckSign(res);
        }
    }

    @Override
    public void setRentRepeatable(Player player, String landName, boolean value, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        setRentRepeatable(player, res, value, resadmin);
    }

    public void setRentRepeatable(Player player, ClaimedResidence res, boolean value, boolean resadmin) {

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        RentableLand land = res.getRentable();

        if (!res.isOwner(player) && !resadmin) {
            plugin.msg(player, lm.Residence_NotOwner);
            return;
        }

        if (land == null || !res.isOwner(player) && !resadmin) {
            plugin.msg(player, lm.Residence_NotOwner);
            return;
        }

        land.AllowRenewing = value;
        if (!value && this.isRented(res))
            res.getRentedLand().AutoPay = false;

        if (value)
            plugin.msg(player, lm.Rentable_EnableRenew, res.getResidenceName());
        else
            plugin.msg(player, lm.Rentable_DisableRenew, res.getResidenceName());

    }

    @Override
    public void setRentedRepeatable(Player player, String landName, boolean value, boolean resadmin) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        setRentedRepeatable(player, res, value, resadmin);
    }

    public void setRentedRepeatable(Player player, ClaimedResidence res, boolean value, boolean resadmin) {
        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        RentedLand land = res.getRentedLand();

        if (land == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        if (!res.getRentable().AllowAutoPay && value) {
            plugin.msg(player, lm.Residence_CantAutoPay);
            return;
        }

        if (!land.player.equals(player.getName()) && !resadmin) {
            plugin.msg(player, lm.Residence_NotOwner);
            return;
        }

        if (!land.player.equals(player.getName()) && !resadmin) {
            plugin.msg(player, lm.Residence_NotOwner);
            return;
        }

        land.AutoPay = value;
        if (value)
            plugin.msg(player, lm.Rent_EnableRenew, res.getResidenceName());
        else
            plugin.msg(player, lm.Rent_DisableRenew, res.getResidenceName());

        plugin.getSignUtil().CheckSign(res);
    }

    public void printRentInfo(Player player, String landName) {
        ClaimedResidence res = plugin.getResidenceManager().getByName(landName);
        printRentInfo(player, res);
    }

    public void printRentInfo(Player player, ClaimedResidence res) {

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return;
        }

        RentableLand rentable = res.getRentable();
        RentedLand rented = res.getRentedLand();
        if (rentable != null) {
            plugin.msg(player, lm.General_Separator);
            plugin.msg(player, lm.General_Land, res.getName());
            plugin.msg(player, lm.General_Cost, rentable.cost, rentable.days);
            plugin.msg(player, lm.Rentable_AllowRenewing, rentable.AllowRenewing);
            plugin.msg(player, lm.Rentable_StayInMarket, rentable.StayInMarket);
            plugin.msg(player, lm.Rentable_AllowAutoPay, rentable.AllowAutoPay);
            if (rented != null) {
                plugin.msg(player, lm.Residence_RentedBy, rented.player);

                if (rented.player.equals(player.getName()) || res.isOwner(player) || plugin.isResAdminOn(player))
                    player.sendMessage((rented.AutoPay ? plugin.msg(lm.Rent_AutoPayTurnedOn) : plugin.msg(lm.Rent_AutoPayTurnedOff))
                            + "\n");
                plugin.msg(player, lm.Rent_Expire, GetTime.getTime(rented.endTime));
            } else {
                plugin.msg(player, lm.General_Status, plugin.msg(lm.General_Available));
            }
            plugin.msg(player, lm.General_Separator);
        } else {
            plugin.msg(player, lm.General_Separator);
            plugin.msg(player, lm.Residence_NotForRent);
            plugin.msg(player, lm.General_Separator);
        }
    }

    public void printRentableResidences(Player player, int page) {
        plugin.msg(player, lm.Rentable_Land);
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
                rentedBy = plugin.msg(lm.Residence_RentedBy, rent.player);
                hover = GetTime.getTime(rent.endTime);
            }

            String msg = plugin.msg(lm.Rent_RentList, pi.getPositionForOutput(position), res.getName(), res.getRentable().cost, res.getRentable().days, res.getRentable().AllowRenewing,
                    res.getOwner(), rentedBy);

            RawMessage rm = new RawMessage();
            rm.add(msg, "§2" + hover);

            if (!hover.equalsIgnoreCase("")) {
                rm.show(player);
            } else {
                player.sendMessage(msg);
            }
        }

        plugin.getInfoPageManager().ShowPagination(player, pi, "/res market list rent");

    }

    @Override
    public int getRentCount(String player) {
        int count = 0;
        for (ClaimedResidence res : rentedLand) {
            if (res.getRentedLand().player.equalsIgnoreCase(player))
                count++;
        }
        return count;
    }

    @Override
    public int getRentableCount(String player) {
        int count = 0;
        for (ClaimedResidence res : rentableLand) {
            if (res != null)
                if (res.isOwner(player))
                    count++;
        }
        return count;
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
            RentedLand one = loadRented((Map<String, Object>) rent.getValue());
            ClaimedResidence res = plugin.getResidenceManager().getByName(rent.getKey());
            if (res != null) {
                res.setRented(one);
                this.rentedLand.add(res);
            }
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

}
