package com.bekvon.bukkit.residence.commands;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.shopStuff.Board;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.shopStuff.Vote;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.RawMessages.RawMessage;

public class shop implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1700)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        int page = 1;
        try {
            if (args.length > 0) {
                page = Integer.parseInt(args[args.length - 1]);
            }
        } catch (Exception ex) {
        }

        if ((args.length == 1 || args.length == 2 || args.length == 3) && (args[0].equalsIgnoreCase("votes") || args[0].equalsIgnoreCase("likes"))) {

            int VotePage = 1;

            ClaimedResidence res = null;
            if (args.length == 1) {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
                if (res == null) {
                    lm.Residence_NotIn.sendMessage(sender);
                    return true;
                }
            } else if (args.length == 2) {
                res = plugin.getResidenceManager().getByName(args[1]);
                if (res == null) {
                    try {
                        VotePage = Integer.parseInt(args[1]);
                        res = plugin.getResidenceManager().getByLoc(player.getLocation());
                        if (res == null) {
                            lm.Residence_NotIn.sendMessage(sender);
                            return true;
                        }
                    } catch (Exception ex) {
                        lm.General_UseNumbers.sendMessage(sender);
                        return true;
                    }
                }

            } else if (args.length == 3) {
                res = plugin.getResidenceManager().getByName(args[1]);
                if (res == null) {
                    lm.Residence_NotIn.sendMessage(sender);
                    return true;
                }
                try {
                    VotePage = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    lm.General_UseNumbers.sendMessage(sender);
                    return true;
                }
            }

            if (res == null) {
                lm.Residence_NotIn.sendMessage(sender);
                return true;
            }

            List<ShopVote> VoteList = res.getAllShopVotes();

            String separator = lm.InformationPage_SmallSeparator.getMessage();

            PageInfo pi = new PageInfo(10, VoteList.size(), page);

            if (!pi.isPageOk()) {
                lm.Shop_NoVotes.sendMessage(sender);
                return true;
            }

            lm.Shop_VotesTopLine.sendMessage(sender, separator, res.getName(), VotePage, pi.getTotalPages(), separator);

            int position = -1;
            for (ShopVote one : VoteList) {
                position++;
                if (position > pi.getEnd())
                    break;
                if (!pi.isInRange(position))
                    continue;

                Date dNow = new Date(one.getTime());
                SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                ft.setTimeZone(TimeZone.getTimeZone(plugin.getConfigManager().getTimeZone()));
                String timeString = ft.format(dNow);

                String message = lm.Shop_VotesList.getMessage(pi.getStart() + position + 1, one.getName(), (plugin.getConfigManager().isOnlyLike()
                        ? ""
                        : one.getVote()), timeString);
                player.sendMessage(message);
            }

            pi.autoPagination(sender, "/res shop votes " + res.getName());

            return true;
        }
        if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("list")) {

            int Shoppage = 1;

            if (args.length == 2) {
                try {
                    Shoppage = Integer.parseInt(args[1]);
                } catch (Exception ex) {
                    lm.General_UseNumbers.sendMessage(sender);
                    return true;
                }
            }

            Map<String, Double> ShopList = plugin.getShopSignUtilManager().getSortedShopList();

            String separator = lm.InformationPage_SmallSeparator.getMessage();

            PageInfo pi = new PageInfo(10, ShopList.size(), page);

            if (!pi.isPageOk()) {
                lm.Shop_NoVotes.sendMessage(sender);
                return true;
            }

            lm.Shop_ListTopLine.sendMessage(sender, separator, Shoppage, pi.getTotalPages(), separator);

            for (Entry<String, Double> one : ShopList.entrySet()) {
                if (!pi.isEntryOk())
                    continue;
                if (pi.isBreak())
                    break;
                ClaimedResidence res = plugin.getResidenceManager().getByName(one.getKey());
                if (res == null)
                    continue;

                Vote vote = plugin.getShopSignUtilManager().getAverageVote(one.getKey());
                String votestat = "";

                if (plugin.getConfigManager().isOnlyLike()) {
                    votestat = vote.getAmount() == 0 ? "" : lm.Shop_ListLiked.getMessage(plugin.getShopSignUtilManager().getLikes(one.getKey()));
                } else
                    votestat = vote.getAmount() == 0 ? "" : lm.Shop_ListVoted.getMessage(vote.getVote(), vote.getAmount());

                String owner = res.getOwner();
                String message = lm.Shop_List.getMessage(pi.getPositionForOutput(), one.getKey(), owner, votestat);

                String desc = res.getShopDesc() == null ? lm.Shop_NoDesc.getMessage() : lm.Shop_Desc.getMessage(CMIChatColor.translate(res.getShopDesc().replace("/n", "\n")));

                RawMessage rm = new RawMessage();
                rm.addText(" " + message).addHover(desc).addCommand("/res tp " + one.getKey());
                rm.show(sender);
            }

            pi.autoPagination(sender, "/res shop list");

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("deleteboard")) {

            if (!resadmin) {
                lm.General_AdminOnly.sendMessage(sender);
                return true;
            }

            ShopListener.Delete.add(player.getName());
            lm.Shop_DeleteBoard.sendMessage(sender);
            return true;
        }
        if (args.length > 1 && args[0].equalsIgnoreCase("setdesc")) {

            ClaimedResidence res = null;

            String desc = "";
            if (args.length >= 1) {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
                if (res == null) {
                    lm.Residence_NotIn.sendMessage(sender);
                    return true;
                }
                for (int i = 2; i < args.length; i++) {
                    desc += args[i];
                    if (i < args.length - 1)
                        desc += " ";
                }
            }

            if (res == null)
                return true;

            if (!res.isOwner(player) && !resadmin) {
                lm.Residence_NonAdmin.sendMessage(sender);
                return true;
            }

            res.setShopDesc(desc);
            lm.Shop_DescChange.sendMessage(sender, CMIChatColor.translate(desc));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("createboard")) {

            if (!resadmin) {
                lm.General_AdminOnly.sendMessage(sender);
                return true;
            }

            if (!plugin.getSelectionManager().hasPlacedBoth(player)) {
                lm.Select_Points.sendMessage(sender);
                return true;
            }

            int place = 1;
            try {
                place = Integer.parseInt(args[1]);
            } catch (Exception ex) {
                lm.General_UseNumbers.sendMessage(sender);
                return true;
            }

            if (place < 1)
                place = 1;

            CuboidArea cuboid = plugin.getSelectionManager().getSelectionCuboid(player);

            if (cuboid.getXSize() > 16 || cuboid.getYSize() > 16 || cuboid.getZSize() > 16) {
                lm.Shop_ToBigSelection.sendMessage(sender);
                return true;
            }

            if (cuboid.getXSize() != 1 && cuboid.getZSize() != 1) {
                lm.Shop_ToDeapSelection.sendMessage(sender);
                return true;
            }

            Location loc1 = plugin.getSelectionManager().getSelection(player).getBaseArea().getHighLocation();
            Location loc2 = plugin.getSelectionManager().getSelection(player).getBaseArea().getLowLocation();

            if (loc1.getBlockY() < loc2.getBlockY()) {
                lm.Shop_InvalidSelection.sendMessage(sender);
                return true;
            }

            Board newTemp = new Board();
            newTemp.setStartPlace(place);
            newTemp.setTopLoc(loc1);
            newTemp.setBottomLoc(loc2);

            if (plugin.getShopSignUtilManager().exist(newTemp)) {
                lm.Shop_BoardExist.sendMessage(sender);
                return true;
            }

            plugin.getShopSignUtilManager().addBoard(newTemp);
            lm.Shop_NewBoard.sendMessage(sender);

            plugin.getShopSignUtilManager().boardUpdate();
            plugin.getShopSignUtilManager().saveSigns();

            return true;

        }
        if ((args.length == 1 || args.length == 2 || args.length == 3) && (args[0].equalsIgnoreCase("vote") || args[0].equalsIgnoreCase("like"))) {
            String resName = "";
            int vote = 5;
            ClaimedResidence res = null;
            if (args.length == 2) {

                if (plugin.getConfigManager().isOnlyLike()) {

                    res = plugin.getResidenceManager().getByName(args[1]);
                    if (res == null) {
                        lm.Invalid_Residence.sendMessage(sender);
                        return true;
                    }
                    vote = plugin.getConfigManager().getVoteRangeTo();

                } else {
                    res = plugin.getResidenceManager().getByLoc(player.getLocation());
                    if (res == null) {
                        lm.Residence_NotIn.sendMessage(sender);
                        return true;
                    }

                    try {
                        vote = Integer.parseInt(args[1]);
                    } catch (Exception ex) {
                        lm.General_UseNumbers.sendMessage(sender);
                        return true;
                    }
                }
            } else if (args.length == 1 && plugin.getConfigManager().isOnlyLike()) {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
                if (res == null) {
                    lm.Residence_NotIn.sendMessage(sender);
                    return true;
                }
                vote = plugin.getConfigManager().getVoteRangeTo();
            } else if (args.length == 3 && !plugin.getConfigManager().isOnlyLike()) {
                res = plugin.getResidenceManager().getByName(args[1]);
                if (res == null) {
                    lm.Invalid_Residence.sendMessage(sender);
                    return true;
                }
                try {
                    vote = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    lm.General_UseNumbers.sendMessage(sender);
                    return true;
                }
            } else if (args.length == 2 && !plugin.getConfigManager().isOnlyLike()) {
                res = plugin.getResidenceManager().getByLoc(player.getLocation());
                if (res == null) {
                    lm.Invalid_Residence.sendMessage(sender);
                    return true;
                }
                try {
                    vote = Integer.parseInt(args[2]);
                } catch (Exception ex) {
                    lm.General_UseNumbers.sendMessage(sender);
                    return true;
                }
            } else {
                return false;
            }

            resName = res.getName();

            if (!res.getPermissions().has("shop", false)) {
                lm.Shop_CantVote.sendMessage(sender);
                return true;
            }

            if (vote < plugin.getConfigManager().getVoteRangeFrom() || vote > plugin.getConfigManager().getVoteRangeTo()) {
                lm.Shop_VotedRange.sendMessage(sender, plugin.getConfigManager().getVoteRangeFrom(), plugin.getConfigManager().getVoteRangeTo());
                return true;
            }

//	    ConcurrentHashMap<String, List<ShopVote>> VoteList = plugin.getShopSignUtilManager().GetAllVoteList();

            if (!res.getAllShopVotes().isEmpty()) {
                List<ShopVote> list = res.getAllShopVotes();
                boolean found = false;
                for (ShopVote OneVote : list) {
                    if (OneVote.getName().equalsIgnoreCase(player.getName()) || OneVote.getUuid() != null && OneVote.getUuid() == player.getUniqueId()) {
                        if (plugin.getConfigManager().isOnlyLike()) {
                            lm.Shop_AlreadyLiked.sendMessage(sender, resName);
                            return true;
                        }
                        lm.Shop_VoteChanged.sendMessage(sender, OneVote.getVote(), vote, resName);
                        OneVote.setVote(vote);
                        OneVote.setTime(System.currentTimeMillis());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ShopVote newVote = new ShopVote(player.getUniqueId(), vote, System.currentTimeMillis());
                    list.add(newVote);
                    if (plugin.getConfigManager().isOnlyLike())
                        lm.Shop_Liked.sendMessage(sender, resName);
                    else
                        lm.Shop_Voted.sendMessage(sender, vote, resName);
                }
            } else {
                ShopVote newVote = new ShopVote(player.getUniqueId(), vote, System.currentTimeMillis());
                res.addShopVote(newVote);
                if (plugin.getConfigManager().isOnlyLike())
                    lm.Shop_Liked.sendMessage(sender, resName);
                else
                    lm.Shop_Voted.sendMessage(sender, vote, resName);
            }
            plugin.getShopSignUtilManager().saveShopVotes(true);
            plugin.getShopSignUtilManager().boardUpdate();
            return true;
        }
        return false;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Manage residence shop");
        c.get("Info", Arrays.asList("Manages residence shop feature"));

        // Sub commands
        c.setFullPath(c.getPath() + "SubCommands.");
        c.get("list.Description", "Shows list of res shops");
        c.get("list.Info", Arrays.asList("&eUsage: &6/res shop list", "Shows full list of all residences with shop flag"));
        LocaleManager.addTabCompleteSub(this, "list");

        c.get("vote.Description", "Vote for residence shop");
        c.get("vote.Info", Arrays.asList("&eUsage: &6/res shop vote <residence> [amount]", "Votes for current or defined residence"));
        LocaleManager.addTabCompleteSub(this, "vote", "[residence]", "10");

        c.get("like.Description", "Give like for residence shop");
        c.get("like.Info", Arrays.asList("&eUsage: &6/res shop like <residence>", "Gives like for residence shop"));
        LocaleManager.addTabCompleteSub(this, "like", "[residenceshop]");

        c.get("votes.Description", "Shows res shop votes");
        c.get("votes.Info", Arrays.asList("&eUsage: &6/res shop votes <residence> <page>", "Shows full vote list of current or defined residence shop"));
        LocaleManager.addTabCompleteSub(this, "votes", "[residenceshop]");

        c.get("likes.Description", "Shows res shop likes");
        c.get("likes.Info", Arrays.asList("&eUsage: &6/res shop likes <residence> <page>", "Shows full like list of current or defined residence shop"));
        LocaleManager.addTabCompleteSub(this, "likes", "[residenceshop]");

        c.get("setdesc.Description", "Sets residence shop description");
        c.get("setdesc.Info", Arrays.asList("&eUsage: &6/res shop setdesc [text]", "Sets residence shop description. Color code supported. For new line use /n"));

        c.get("createboard.Description", "Create res shop board");
        c.get("createboard.Info", Arrays.asList("&eUsage: &6/res shop createboard [place]",
                "Creates res shop board from selected area. Place - position from which to start filling board"));
        LocaleManager.addTabCompleteSub(this, "createboard", "1");

        c.get("deleteboard.Description", "Deletes res shop board");
        c.get("deleteboard.Info", Arrays.asList("&eUsage: &6/res shop deleteboard", "Deletes res shop board bi right clicking on one of signs"));
        LocaleManager.addTabCompleteSub(this, "deleteboard", "1");
    }
}
