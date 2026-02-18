package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResAdmin;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.utils.Utils;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.FileHandler.ConfigReader;

public class market implements cmd {
    Residence plugin;

    @Override
    @CommandAnnotation(simple = true, priority = 2600)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        this.plugin = plugin;
        if (!(sender instanceof Player))
            return false;

        if (args.length == 0) {
            return false;
        }

        Player player = (Player) sender;
        int page = 1;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }

        switch (args[0].toLowerCase()) {

        case "list":
            return commandResMarketList(args, player, page);
        case "autopay":
            return commandResMarketAutoPay(args, resadmin, player);
        case "payrent":
            return commandResMarketPayRent(args, resadmin, player);
        case "rentable":
            return commandResMarketRentable(args, resadmin, player);
        case "rent":
            return commandResMarketRent(args, resadmin, player);
        case "release":
        case "unrent":
            if (args.length != 2 && args.length != 1)
                return false;

            ClaimedResidence res = null;

            if (args.length == 1)
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
            else
                res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }

            if (res.isRented()) {
                if (resadmin || ResAdmin.isResAdmin(player) || ResPerm.market_evict.hasPermission(player)) {
                    plugin.unrentConfirm.put(player.getUniqueId(), res);
                    lm.Rent_EvictConfirm.sendMessage(sender, res.getName());
                } else if (plugin.getRentManager().getRentingPlayer(res).equalsIgnoreCase(sender.getName())) {
                    plugin.unrentConfirm.put(player.getUniqueId(), res);
                    lm.Rent_UnrentConfirm.sendMessage(sender, res.getName());
                } else
                    plugin.getRentManager().printRentInfo(player, res);
            } else {
                plugin.unrentConfirm.put(player.getUniqueId(), res);
                lm.Rent_ReleaseConfirm.sendMessage(sender, res.getName());
            }

            return true;

        case "confirm":
            if (!plugin.unrentConfirm.containsKey(player.getUniqueId())) {
                lm.Invalid_Residence.sendMessage(sender);
                return false;
            }
            res = plugin.unrentConfirm.remove(player.getUniqueId());
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }

            if (!res.isRented()) {
                plugin.getRentManager().removeFromForRent(player, res, resadmin);
                ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
                if (rPlayer != null && rPlayer.getMainResidence() == res) {
                    rPlayer.setMainResidence(null);
                }
            } else
                plugin.getRentManager().unrent(player, res, resadmin);
            return true;
        case "sign":
            if (args.length != 2) {
                return false;
            }
            Block block = Utils.getTargetBlock(player, 10);

            if (!(block.getState() instanceof Sign)) {
                lm.Sign_LookAt.sendMessage(sender);
                return true;
            }

            Sign sign = (Sign) block.getState();

            Signs signInfo = new Signs();

            Signs oldSign = plugin.getSignUtil().getSignFromLoc(sign.getLocation());

            if (oldSign != null)
                signInfo = oldSign;

            Location loc = sign.getLocation();

            ClaimedResidence CurrentRes = plugin.getResidenceManager().getByLoc(sign.getLocation());

            if (CurrentRes != null && !CurrentRes.isOwner(player) && !resadmin) {
                lm.Residence_NotOwner.sendMessage(sender);
                return true;
            }

            res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }

            boolean ForSale = res.isForSell();
            boolean ForRent = res.isForRent();

            if (ForSale || ForRent) {

                if (res.getSignsInResidence().size() >= plugin.getConfigManager().getSignsMaxPerResidence()) {
                    lm.Sign_TooMany.sendMessage(player);
                    return true;
                }

                signInfo.setResidence(res);
                signInfo.setLocation(loc);
                plugin.getSignUtil().getSigns().addSign(signInfo);
                plugin.getSignUtil().saveSigns();
            } else {
                lm.Residence_NotForRentOrSell.sendMessage(sender);
                return true;
            }

            plugin.getSignUtil().checkSign(res, 5);

            return true;

        case "info":
            res = null;
            if (args.length == 1)
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
            else if (args.length == 2)
                res = plugin.getResidenceManager().getByName(args[1]);
            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }
            boolean sell = plugin.getTransactionManager().viewSaleInfo(res, player);
            if (plugin.getConfigManager().enabledRentSystem() && res.isForRent()) {
                plugin.getRentManager().printRentInfo(player, res);
            } else if (!sell) {
                lm.Residence_NotForRentOrSell.sendMessage(sender);
            }
            return true;
        case "buy":
            res = null;
            if (args.length == 1)
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
            else if (args.length == 2)
                res = plugin.getResidenceManager().getByName(args[1]);

            if (res == null) {
                lm.Invalid_Residence.sendMessage(sender);
                return true;
            }

            sell = plugin.getTransactionManager().viewSaleInfo(res, player);
            if (sell) {
                plugin.getTransactionManager().buyPlot(res, player, resadmin);
            } else {
                lm.Residence_NotForRentOrSell.sendMessage(sender);
            }
            return true;
        case "unsell":
            if (args.length != 2)
                return false;

            plugin.getTransactionManager().removeFromSale(player, args[1], resadmin);
            return true;

        case "sell":
            if (args.length != 3)
                return false;

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (Exception ex) {
                lm.Invalid_Amount.sendMessage(sender);
                return true;
            }
            plugin.getTransactionManager().putForSale(args[1], player, amount, resadmin);
            return true;
        default:
            return false;
        }
    }

    private boolean commandResMarketRent(String[] args, boolean resadmin, Player player) {
        if (args.length < 1 || args.length > 3) {
            return false;
        }
        boolean repeat = plugin.getConfigManager().isRentPlayerAutoPay();

        ClaimedResidence res = null;

        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("t") || args[2].equalsIgnoreCase("true")) {
                repeat = true;
            } else if (args[2].equalsIgnoreCase("f") || args[2].equalsIgnoreCase("false")) {
                repeat = false;
            } else {
                lm.Invalid_Boolean.sendMessage(player);
                return true;
            }
        }

        if (args.length == 1)
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        else if (args.length > 1)
            res = plugin.getResidenceManager().getByName(args[1]);

        if (res != null) {
            plugin.getRentManager().rent(player, res, repeat, resadmin);
        } else
            lm.Invalid_Residence.sendMessage(player);

        return true;
    }

    private boolean commandResMarketPayRent(String[] args, boolean resadmin, Player player) {
        if (args.length != 1 && args.length != 2) {
            return false;
        }

        ClaimedResidence res = null;

        if (args.length == 1)
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        else
            res = plugin.getResidenceManager().getByName(args[1]);

        if (res != null)
            plugin.getRentManager().payRent(player, res, resadmin);
        else
            lm.Invalid_Residence.sendMessage(player);
        return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player) {
        if (args.length < 4 || args.length > 7) {
            return false;
        }
        if (!plugin.getConfigManager().enabledRentSystem()) {
            lm.Rent_Disabled.sendMessage(player);
            return true;
        }
        int days;
        int cost;
        try {
            cost = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            lm.Invalid_Cost.sendMessage(player);
            return true;
        }
        if (cost <= 0) {
            lm.Invalid_Cost.sendMessage(player);
            return true;
        }
        try {
            days = Integer.parseInt(args[3]);
        } catch (Exception ex) {
            lm.Invalid_Days.sendMessage(player);
            return true;
        }
        if (days <= 0) {
            lm.Invalid_Days.sendMessage(player);
            return true;
        }
        boolean AllowRenewing = plugin.getConfigManager().isRentAllowRenewing();
        if (args.length >= 5) {
            String ag = args[4];
            if (ag.equalsIgnoreCase("t") || ag.equalsIgnoreCase("true")) {
                AllowRenewing = true;
            } else if (ag.equalsIgnoreCase("f") || ag.equalsIgnoreCase("false")) {
                AllowRenewing = false;
            } else {
                lm.Invalid_Boolean.sendMessage(player);
                return true;
            }
        }

        boolean StayInMarket = plugin.getConfigManager().isRentStayInMarket();
        if (args.length >= 6) {
            String ag = args[5];
            if (ag.equalsIgnoreCase("t") || ag.equalsIgnoreCase("true")) {
                StayInMarket = true;
            } else if (ag.equalsIgnoreCase("f") || ag.equalsIgnoreCase("false")) {
                StayInMarket = false;
            } else {
                lm.Invalid_Boolean.sendMessage(player);
                return true;
            }
        }

        boolean AllowAutoPay = plugin.getConfigManager().isRentAllowAutoPay();
        if (args.length >= 7) {
            String ag = args[6];
            if (ag.equalsIgnoreCase("t") || ag.equalsIgnoreCase("true")) {
                AllowAutoPay = true;
            } else if (ag.equalsIgnoreCase("f") || ag.equalsIgnoreCase("false")) {
                AllowAutoPay = false;
            } else {
                lm.Invalid_Boolean.sendMessage(player);
                return true;
            }
        }

        plugin.getRentManager().setForRent(player, args[1], cost, days, AllowRenewing, StayInMarket, AllowAutoPay, resadmin);
        return true;
    }

    private boolean commandResMarketAutoPay(String[] args, boolean resadmin, Player player) {
        if (!plugin.getConfigManager().enableEconomy()) {
            lm.Economy_MarketDisabled.sendMessage(player);
            return true;
        }
        if (args.length != 2 && args.length != 3) {
            return false;
        }

        boolean value;

        String barg = "";
        ClaimedResidence res = null;
        if (args.length == 2) {
            barg = args[1];
            res = plugin.getResidenceManager().getByLoc(player.getLocation());
        } else {
            barg = args[2];
            res = plugin.getResidenceManager().getByName(args[1]);
        }

        if (barg.equalsIgnoreCase("true") || barg.equalsIgnoreCase("t")) {
            value = true;
        } else if (barg.equalsIgnoreCase("false") || barg.equalsIgnoreCase("f")) {
            value = false;
        } else {
            lm.Invalid_Boolean.sendMessage(player);
            return true;
        }

        if (res == null) {
            lm.Invalid_Residence.sendMessage(player);
            return true;
        }

        if (res.isRented() && res.getRentedLand().isRenter(player)) {
            plugin.getRentManager().setRentedRepeatable(player, res.getName(), value, resadmin);
        } else if (res.isForRent()) {
            plugin.getRentManager().setRentRepeatable(player, res.getName(), value, resadmin);
        } else {
            lm.Economy_RentReleaseInvalid.sendMessage(player, CMIChatColor.YELLOW + res.getName() + CMIChatColor.RED);
        }
        return true;
    }

    private boolean commandResMarketList(String[] args, Player player, int page) {
        if (!plugin.getConfigManager().enableEconomy()) {
            lm.Economy_MarketDisabled.sendMessage(player);
            return true;
        }
        lm.General_MarketList.sendMessage(player);
        if (args.length < 2)
            return false;

        if (args[1].equalsIgnoreCase("sell")) {
            plugin.getTransactionManager().printForSaleResidences(player, page);
            return true;
        }
        if (args[1].equalsIgnoreCase("rent")) {
            if (plugin.getConfigManager().enabledRentSystem()) {
                plugin.getRentManager().printRentableResidences(player, page);
            }
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();

        c.get("Description", "Buy, Sell, or Rent Residences");
        c.get("Info", Arrays.asList("&eUsage: &6/res market ? for more Info"));

        c.setFullPath(c.getPath() + "SubCommands.");

        c.get("Info.Description", "Get economy Info on residence");
        c.get("Info.Info", Arrays.asList("&eUsage: &6/res market Info [residence]", "Shows if the Residence is for sale or for rent, and the cost."));
        LocaleManager.addTabCompleteSub(this, "Info", "[residence]");

        c.get("list.Description", "Lists rentable and for sale residences.");
        c.get("list.Info", Arrays.asList("&eUsage: &6/res market list [rent/sell]"));
        LocaleManager.addTabCompleteSub(this, "list", "rent%%sell");

        c.get("list.SubCommands.rent.Description", "Lists rentable residences.");
        c.get("list.SubCommands.rent.Info", Arrays.asList("&eUsage: &6/res market list rent"));

        c.get("list.SubCommands.sell.Description", "Lists for sale residences.");
        c.get("list.SubCommands.sell.Info", Arrays.asList("&eUsage: &6/res market list sell"));

        c.get("sell.Description", "Sell a residence");
        c.get("sell.Info", Arrays.asList("&eUsage: &6/res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
                "Another player can buy the residence with /res market buy"));
        LocaleManager.addTabCompleteSub(this, "sell", "[residence]");

        c.get("sign.Description", "Set market sign");
        c.get("sign.Info", Arrays.asList("&eUsage: &6/res market sign [residence]", "Sets market sign you are looking at."));
        LocaleManager.addTabCompleteSub(this, "sign", "[residence]");

        c.get("buy.Description", "Buy a residence");
        c.get("buy.Info", Arrays.asList("&eUsage: &6/res market buy [residence]", "Buys a Residence if its for sale."));
        LocaleManager.addTabCompleteSub(this, "buy", "[residence]");

        c.get("unsell.Description", "Stops selling a residence");
        c.get("unsell.Info", Arrays.asList("&eUsage: &6/res market unsell [residence]"));
        LocaleManager.addTabCompleteSub(this, "unsell", "[residence]");

        c.get("rent.Description", "ent a residence");
        c.get("rent.Info", Arrays.asList("&eUsage: &6/res market rent [residence] <AutoPay>",
                "Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."));
        LocaleManager.addTabCompleteSub(this, "rent", "[cresidence]", "true%%false");

        c.get("rentable.Description", "Make a residence rentable.");
        c.get("rentable.Info", Arrays.asList("&eUsage: &6/res market rentable [residence] [cost] [days] <AllowRenewing> <StayInMarket> <AllowAutoPay>",
                "Makes a residence rentable for [cost] money for every [days] number of days.",
                "If <AllowRenewing> is true, the residence will be able to be rented again before rent expires.",
                "If <StayInMarket> is true, the residence will stay in market after last renter will be removed.",
                "If <AllowAutoPay> is true, money for rent will be automaticaly taken from players balance if he chosen that option when renting"));
        LocaleManager.addTabCompleteSub(this, "rentable", "[residence]", "1000", "7", "true", "true", "true");

        c.get("autopay.Description", "Sets residence AutoPay to given value");
        c.get("autopay.Info", Arrays.asList("&eUsage: &6/res market autopay [residence] [true/false]"));
        LocaleManager.addTabCompleteSub(this, "autopay", "[residence]%%true%%false", "true%%false");

        c.get("payrent.Description", "Pays rent for defined residence");
        c.get("payrent.Info", Arrays.asList("&eUsage: &6/res market payrent [residence]"));
        LocaleManager.addTabCompleteSub(this, "payrent", "[residence]");

        c.get("confirm.Description", "Confirms residence unrent/release action");
        c.get("confirm.Info", Arrays.asList("&eUsage: &6/res market confirm"));
        LocaleManager.addTabCompleteSub(this, "confirm");

        c.get("unrent.Description", "Remove a residence from rent or rentable.");
        c.get("unrent.Info", Arrays.asList("&eUsage: &6/res market unrent [residence]",
                "If you are the renter, this command releases the rent on the house for you.",
                "If you are the owner, this command makes the residence not for rent anymore."));
        LocaleManager.addTabCompleteSub(this, "release", "[residence]");
        LocaleManager.addTabCompleteSub(this, "unrent", "[residence]");
    }

}
