package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

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
                    plugin.msg(player, lm.Invalid_Residence);
                    return true;
                }

                if (res.isRented()) {
                    if (resadmin || plugin.isResAdminOn(player) || ResPerm.market_evict.hasPermission(player)) {
                        plugin.UnrentConfirm.put(player.getName(), res.getName());
                        plugin.msg(sender, lm.Rent_EvictConfirm, res.getName());
                    } else if (plugin.getRentManager().getRentingPlayer(res).equalsIgnoreCase(sender.getName())) {
                        plugin.UnrentConfirm.put(player.getName(), res.getName());
                        plugin.msg(sender, lm.Rent_UnrentConfirm, res.getName());
                    } else
                        plugin.getRentManager().printRentInfo(player, res);
                } else {
                    plugin.UnrentConfirm.put(player.getName(), res.getName());
                    plugin.msg(sender, lm.Rent_ReleaseConfirm, res.getName());
                }

                return true;

            case "confirm":
                if (!plugin.UnrentConfirm.containsKey(player.getName())) {
                    plugin.msg(player, lm.Invalid_Residence);
                    return false;
                }
                String area = plugin.UnrentConfirm.remove(player.getName());
                res = plugin.getResidenceManager().getByName(area);
                if (res == null) {
                    plugin.msg(player, lm.Invalid_Residence);
                    return true;
                }

                if (!res.isRented()) {
                    plugin.getRentManager().removeFromForRent(player, res, resadmin);
                    ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
                    if (rPlayer != null && rPlayer.getMainResidence() == res) {
                        rPlayer.setMainResidence(null);
                    }
                } else
                    plugin.getRentManager().unrent(player, area, resadmin);
                return true;
            case "sign":
                if (args.length != 2) {
                    return false;
                }
                Block block = Utils.getTargetBlock(player, 10);

                if (!(block.getState() instanceof Sign)) {
                    plugin.msg(player, lm.Sign_LookAt);
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
                    plugin.msg(player, lm.Residence_NotOwner);
                    return true;
                }

                res = plugin.getResidenceManager().getByName(args[2]);

                if (res == null) {
                    plugin.msg(player, lm.Invalid_Residence);
                    return true;
                }

                boolean ForSale = res.isForSell();
                boolean ForRent = res.isForRent();

                if (ForSale || ForRent) {
                    signInfo.setResidence(res);
                    signInfo.setLocation(loc);
                    plugin.getSignUtil().getSigns().addSign(signInfo);
                    plugin.getSignUtil().saveSigns();
                } else {
                    plugin.msg(player, lm.Residence_NotForRentOrSell);
                    return true;
                }

                plugin.getSignUtil().CheckSign(res, 5);

                return true;

            case "info":
                res = null;
                if (args.length == 1)
                    res = plugin.getResidenceManager().getByLoc(player.getLocation());
                else if (args.length == 2)
                    res = plugin.getResidenceManager().getByName(args[1]);
                if (res == null) {
                    plugin.msg(player, lm.Invalid_Residence);
                    return true;
                }
                boolean sell = plugin.getTransactionManager().viewSaleInfo(res, player);
                if (plugin.getConfigManager().enabledRentSystem() && res.isForRent()) {
                    plugin.getRentManager().printRentInfo(player, res);
                } else if (!sell) {
                    plugin.msg(sender, lm.Residence_NotForRentOrSell);
                }
                return true;
            case "buy":
                res = null;
                if (args.length == 1)
                    res = plugin.getResidenceManager().getByLoc(player.getLocation());
                else if (args.length == 2)
                    res = plugin.getResidenceManager().getByName(args[1]);

                if (res == null) {
                    plugin.msg(player, lm.Invalid_Residence);
                    return true;
                }

                sell = plugin.getTransactionManager().viewSaleInfo(res, player);
                if (sell) {
                    plugin.getTransactionManager().buyPlot(res, player, resadmin);
                } else {
                    plugin.msg(sender, lm.Residence_NotForRentOrSell);
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
                    plugin.msg(player, lm.Invalid_Amount);
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
                plugin.msg(player, lm.Invalid_Boolean);
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
            plugin.msg(player, lm.Invalid_Residence);

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
            plugin.msg(player, lm.Invalid_Residence);
        return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player) {
        if (args.length < 4 || args.length > 7) {
            return false;
        }
        if (!plugin.getConfigManager().enabledRentSystem()) {
            plugin.msg(player, lm.Rent_Disabled);
            return true;
        }
        int days;
        int cost;
        try {
            cost = Integer.parseInt(args[2]);
        } catch (Exception ex) {
            plugin.msg(player, lm.Invalid_Cost);
            return true;
        }
        try {
            days = Integer.parseInt(args[3]);
        } catch (Exception ex) {
            plugin.msg(player, lm.Invalid_Days);
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
                plugin.msg(player, lm.Invalid_Boolean);
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
                plugin.msg(player, lm.Invalid_Boolean);
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
                plugin.msg(player, lm.Invalid_Boolean);
                return true;
            }
        }

        plugin.getRentManager().setForRent(player, args[1], cost, days, AllowRenewing, StayInMarket, AllowAutoPay, resadmin);
        return true;
    }

    private boolean commandResMarketAutoPay(String[] args, boolean resadmin, Player player) {
        if (!plugin.getConfigManager().enableEconomy()) {
            plugin.msg(player, lm.Economy_MarketDisabled);
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
            plugin.msg(player, lm.Invalid_Boolean);
            return true;
        }

        if (res == null) {
            plugin.msg(player, lm.Invalid_Residence);
            return true;
        }

        if (res.isRented() && res.getRentedLand().player.equalsIgnoreCase(player.getName())) {
            plugin.getRentManager().setRentedRepeatable(player, res.getName(), value, resadmin);
        } else if (res.isForRent()) {
            plugin.getRentManager().setRentRepeatable(player, res.getName(), value, resadmin);
        } else {
            plugin.msg(player, lm.Economy_RentReleaseInvalid, ChatColor.YELLOW + res.getName() + ChatColor.RED);
        }
        return true;
    }

    private boolean commandResMarketList(String[] args, Player player, int page) {
        if (!plugin.getConfigManager().enableEconomy()) {
            plugin.msg(player, lm.Economy_MarketDisabled);
            return true;
        }
        plugin.msg(player, lm.General_MarketList);
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

        c.setP(c.getPath() + "SubCommands.");

        c.get("Info.Description", "Get economy Info on residence");
        c.get("Info.Info", Arrays.asList("&eUsage: &6/res market Info [residence]", "Shows if the Residence is for sale or for rent, and the cost."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "Info"), Arrays.asList("[residence]"));

        c.get("list.Description", "Lists rentable and for sale residences.");
        c.get("list.Info", Arrays.asList("&eUsage: &6/res market list [rent/sell]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "list"), Arrays.asList("rent%%sell"));

        c.get("list.SubCommands.rent.Description", "Lists rentable residences.");
        c.get("list.SubCommands.rent.Info", Arrays.asList("&eUsage: &6/res market list rent"));

        c.get("list.SubCommands.sell.Description", "Lists for sale residences.");
        c.get("list.SubCommands.sell.Info", Arrays.asList("&eUsage: &6/res market list sell"));

        c.get("sell.Description", "Sell a residence");
        c.get("sell.Info", Arrays.asList("&eUsage: &6/res market sell [residence] [amount]", "Puts a residence for sale for [amount] of money.",
                "Another player can buy the residence with /res market buy"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "sell"), Arrays.asList("[residence]"));

        c.get("sign.Description", "Set market sign");
        c.get("sign.Info", Arrays.asList("&eUsage: &6/res market sign [residence]", "Sets market sign you are looking at."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "sign"), Arrays.asList("[residence]"));

        c.get("buy.Description", "Buy a residence");
        c.get("buy.Info", Arrays.asList("&eUsage: &6/res market buy [residence]", "Buys a Residence if its for sale."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "buy"), Arrays.asList("[residence]"));

        c.get("unsell.Description", "Stops selling a residence");
        c.get("unsell.Info", Arrays.asList("&eUsage: &6/res market unsell [residence]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "unsell"), Arrays.asList("[residence]"));

        c.get("rent.Description", "ent a residence");
        c.get("rent.Info", Arrays.asList("&eUsage: &6/res market rent [residence] <AutoPay>",
                "Rents a residence.  Autorenew can be either true or false.  If true, the residence will be automatically re-rented upon expire if the residence owner has allowed it."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "rent"), Arrays.asList("[cresidence]", "true%%false"));

        c.get("rentable.Description", "Make a residence rentable.");
        c.get("rentable.Info", Arrays.asList("&eUsage: &6/res market rentable [residence] [cost] [days] <AllowRenewing> <StayInMarket> <AllowAutoPay>",
                "Makes a residence rentable for [cost] money for every [days] number of days.",
                "If <AllowRenewing> is true, the residence will be able to be rented again before rent expires.",
                "If <StayInMarket> is true, the residence will stay in market after last renter will be removed.",
                "If <AllowAutoPay> is true, money for rent will be automaticaly taken from players balance if he chosen that option when renting"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "rentable"), Arrays.asList("[residence]", "1000", "7", "true", "true",
                "true"));

        c.get("autopay.Description", "Sets residence AutoPay to given value");
        c.get("autopay.Info", Arrays.asList("&eUsage: &6/res market autopay <residence> [true/false]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "autopay"), Arrays.asList("[residence]%%true%%false", "true%%false"));

        c.get("payrent.Description", "Pays rent for defined residence");
        c.get("payrent.Info", Arrays.asList("&eUsage: &6/res market payrent <residence>"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "payrent"), Arrays.asList("[residence]"));

        c.get("confirm.Description", "Confirms residence unrent/release action");
        c.get("confirm.Info", Arrays.asList("&eUsage: &6/res market confirm"));

        c.get("unrent.Description", "Remove a residence from rent or rentable.");
        c.get("unrent.Info", Arrays.asList("&eUsage: &6/res market unrent [residence]",
                "If you are the renter, this command releases the rent on the house for you.",
                "If you are the owner, this command makes the residence not for rent anymore."));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "release"), Arrays.asList("[residence]"));
        Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "unrent"), Arrays.asList("[residence]"));
    }

}
