package com.bekvon.bukkit.residence.text.help;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.GetTime;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class InformationPager {
    Residence plugin;

    public InformationPager(Residence plugin) {
        this.plugin = plugin;
    }

    public void printInfo(CommandSender sender, String command, String title, String[] lines, int page) {
        printInfo(sender, command, title, Arrays.asList(lines), page);
    }

    public void printInfo(CommandSender sender, String command, String title, List<String> lines, int page) {

        PageInfo pi = new PageInfo(6, lines.size(), page);

        if (!pi.isPageOk()) {
            lm.Invalid_Page.sendMessage(sender);
            return;
        }
        lm.InformationPage_TopSingle.sendMessage(sender, title);

        for (int i = pi.getStart(); i <= pi.getEnd(); i++) {
            if (lines.size() > i)
                sender.sendMessage(ChatColor.GREEN + lines.get(i));
        }

        pi.autoPagination(sender, command);

    }

    public void printListInfo(CommandSender sender, UUID uuid, TreeMap<String, ClaimedResidence> ownedResidences, int page, boolean resadmin, World world) {

        int perPage = 20;
        if (sender instanceof Player)
            perPage = 10;

        String targetPlayer = null;
        if (uuid != null)
            targetPlayer = ResidencePlayer.getName(uuid);

        if (ownedResidences.isEmpty()) {
            lm.Residence_DontOwn.sendMessage(sender, targetPlayer);
            return;
        }

        PageInfo pi = new PageInfo(perPage, ownedResidences.size(), page);

        if (!(sender instanceof Player) && page == -1) {
            printListWithDelay(sender, ownedResidences, 0, resadmin);
            return;
        }
        if (!(sender instanceof Player) && page == -2) {
            printListToFile(ownedResidences, resadmin);
            return;
        }

        if (!pi.isPageOk()) {
            lm.Invalid_Page.sendMessage(sender);
            return;
        }

        if (targetPlayer != null)
            lm.InformationPage_Top.sendMessage(sender, lm.General_Residences.getMessage(), targetPlayer);

        String cmd = "res";
        if (resadmin)
            cmd = "resadmin";

        int y = -1;

        for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
            y++;
            if (y > pi.getEnd())
                break;
            if (!pi.isInRange(y))
                continue;

            ClaimedResidence res = resT.getValue();
            StringBuilder StringB = new StringBuilder();
            StringB.append(lm.General_Owner.getMessage(res.getOwner()));

            if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
                StringB.append("\n");
                CuboidArea area = res.getAreaArray()[0];
                String cord1 = lm.General_CoordsTop.getMessage(area.getHighVector().getBlockX(), area.getHighVector().getBlockY(), area.getHighVector().getBlockZ());
                String cord2 = lm.General_CoordsBottom.getMessage(area.getLowVector().getBlockX(), area.getLowVector().getBlockY(), area.getLowVector().getBlockZ());
                String worldInfo = lm.General_CoordsLiner.getMessage(cord1, cord2);
                StringB.append(worldInfo);
            }

            StringB.append("\n").append(lm.General_CreatedOn.getMessage(GetTime.getTime(res.getCreateTime())));

            String ExtraString = "";
            if (res.isForRent()) {
                if (res.isRented()) {
                    ExtraString = " " + lm.Residence_IsRented.getMessage();
                    StringB.append("\n").append(lm.Residence_RentedBy.getMessage(res.getRentedLand().getRenterName()));
                } else {
                    ExtraString = " " + lm.Residence_IsForRent.getMessage();
                }
                RentableLand rentable = res.getRentable();
                StringB.append("\n").append(lm.General_Cost.getMessage(rentable.cost, rentable.days));
                StringB.append("\n").append(lm.Rentable_AllowRenewing.getMessage(rentable.AllowRenewing));
                StringB.append("\n").append(lm.Rentable_StayInMarket.getMessage(rentable.StayInMarket));
                StringB.append("\n").append(lm.Rentable_AllowAutoPay.getMessage(rentable.AllowAutoPay));
            }

            if (res.isForSell()) {
                ExtraString = " " + lm.Residence_IsForSale.getMessage();
                StringB.append("\n " + lm.Economy_LandForSale.getMessage() + " " + res.getSellPrice());
            }

            String tpFlag = "";
            String moveFlag = "";
            String msg = lm.Residence_ResList.getMessage(y + 1, res.getName(), res.getWorldName(), tpFlag + moveFlag, ExtraString);

            if (sender instanceof Player && !res.isOwner(sender)) {
                tpFlag = res.getPermissions().playerHas((Player) sender, Flags.tp, true) ? lm.General_AllowedTeleportIcon.getMessage() : lm.General_BlockedTeleportIcon.getMessage();
                moveFlag = res.getPermissions().playerHas(sender.getName(), Flags.move, true) ? lm.General_AllowedMovementIcon.getMessage() : lm.General_BlockedMovementIcon.getMessage();

                if (res.isTrusted((Player) sender))
                    msg = lm.Residence_TrustedResList.getMessage(y + 1, res.getName(), res.getWorldName(), tpFlag + moveFlag, ExtraString);
            }

            RawMessage rm = new RawMessage();
            if (sender instanceof Player)
                rm.addText(msg).addHover(StringB.toString()).addCommand(cmd + " tp " + res.getName());
            else
                rm.addText(msg + " " + StringB.toString().replace("\n", ""));

            rm.show(sender);
        }

        String worldName = "";
        if (world != null)
            worldName = " " + world.getName();

        if (targetPlayer != null)
            pi.autoPagination(sender, cmd + " list " + targetPlayer + worldName);
        else
            pi.autoPagination(sender, cmd + " listall" + worldName);
    }

    private void printListWithDelay(final CommandSender sender, final TreeMap<String, ClaimedResidence> ownedResidences, final int start, final boolean resadmin) {

        int i = start;
        int y = 0;
        for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
            y++;
            if (y < i)
                continue;
            i++;
            if (i >= start + 100)
                break;
            if (ownedResidences.size() < i)
                break;

            ClaimedResidence res = resT.getValue();
            StringBuilder StringB = new StringBuilder();
            StringB.append(lm.General_Owner.getMessage(res.getOwner()));

            if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
                CuboidArea area = res.getAreaArray()[0];
                String cord1 = lm.General_CoordsTop.getMessage(area.getHighVector().getBlockX(), area.getHighVector().getBlockY(), area.getHighVector().getBlockZ());
                String cord2 = lm.General_CoordsBottom.getMessage(area.getLowVector().getBlockX(), area.getLowVector().getBlockY(), area.getLowVector().getBlockZ());
                String worldInfo = CMIChatColor.translate(lm.General_CoordsLiner.getMessage(cord1, cord2));
                StringB.append("\n" + worldInfo);
            }

            StringB.append("\n" + lm.General_CreatedOn.getMessage(GetTime.getTime(res.getCreateTime())));

            String ExtraString = "";
            if (res.isForRent()) {
                if (res.isRented()) {
                    ExtraString = " " + lm.Residence_IsRented.getMessage();
                    StringB.append("\n " + lm.Residence_RentedBy.getMessage(res.getRentedLand().getRenterName()));
                } else {
                    ExtraString = " " + lm.Residence_IsForRent.getMessage();
                }
                RentableLand rentable = res.getRentable();
                StringB.append("\n" + lm.General_Cost.getMessage(rentable.cost, rentable.days));
                StringB.append("\n" + lm.Rentable_AllowRenewing.getMessage(rentable.AllowRenewing));
                StringB.append("\n" + lm.Rentable_StayInMarket.getMessage(rentable.StayInMarket));
                StringB.append("\n" + lm.Rentable_AllowAutoPay.getMessage(rentable.AllowAutoPay));
            }

            if (res.isForSell()) {
                ExtraString = " " + lm.Residence_IsForSale.getMessage();
                StringB.append("\n" + lm.Economy_LandForSale.getMessage() + " " + res.getSellPrice());
            }

            String msg = lm.Residence_ResList.getMessage(i, res.getName(), res.getWorldName(), "", ExtraString);

            msg = CMIChatColor.stripColor(msg + " " + StringB.toString().replace("\n", ""));
            msg = msg.replaceAll("\\s{2}", " ");
            sender.sendMessage(msg);
        }

        if (ownedResidences.isEmpty()) {
            return;
        }

        CMIScheduler.runTaskLater(plugin, () -> printListWithDelay(sender, ownedResidences, start + 100, resadmin), 5L);

    }

    private void printListToFile(final TreeMap<String, ClaimedResidence> ownedResidences, final boolean resadmin) {

        lm.consoleMessage("Saving");
        CMIScheduler.runTaskAsynchronously(plugin, () -> {
            int y = 0;
            final StringBuilder sb = new StringBuilder();
            for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
                y++;
                if (ownedResidences.size() < y)
                    break;

                ClaimedResidence res = resT.getValue();
                StringBuilder StringB = new StringBuilder();
                StringB.append(" " + lm.General_Owner.getMessage(res.getOwner()));

                if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
                    CuboidArea area = res.getAreaArray()[0];
                    String cord1 = lm.General_CoordsTop.getMessage(area.getHighVector().getBlockX(), area.getHighVector().getBlockY(), area.getHighVector().getBlockZ());
                    String cord2 = lm.General_CoordsBottom.getMessage(area.getLowVector().getBlockX(), area.getLowVector().getBlockY(), area.getLowVector().getBlockZ());
                    String worldInfo = CMIChatColor.translate(lm.General_CoordsLiner.getMessage(cord1, cord2));
                    StringB.append("\n" + worldInfo);
                }

                StringB.append("\n " + lm.General_CreatedOn.getMessage(GetTime.getTime(res.getCreateTime())));

                String ExtraString = "";
                if (res.isForRent()) {
                    if (res.isRented()) {
                        ExtraString = " " + lm.Residence_IsRented.getMessage();
                        StringB.append("\n " + lm.Residence_RentedBy.getMessage(res.getRentedLand().getRenterName()));
                    } else {
                        ExtraString = " " + lm.Residence_IsForRent.getMessage();
                    }
                    RentableLand rentable = res.getRentable();
                    StringB.append("\n " + lm.General_Cost.getMessage(rentable.cost, rentable.days));
                    StringB.append("\n " + lm.Rentable_AllowRenewing.getMessage(rentable.AllowRenewing));
                    StringB.append("\n " + lm.Rentable_StayInMarket.getMessage(rentable.StayInMarket));
                    StringB.append("\n " + lm.Rentable_AllowAutoPay.getMessage(rentable.AllowAutoPay));
                }

                if (res.isForSell()) {
                    ExtraString = " " + lm.Residence_IsForSale.getMessage();
                    StringB.append("\n " + lm.Economy_LandForSale.getMessage() + " " + res.getSellPrice());
                }

                String msg = lm.Residence_ResList.getMessage(y, res.getName(), res.getWorldName(), "", ExtraString);

                msg = CMIChatColor.stripColor(msg + " " + StringB.toString().replace("\n", ""));
                msg = msg.replaceAll("\\s{2}", " ");

                sb.append(msg);
                sb.append(" \n");
            }

            File BackupDir = new File(Residence.getInstance().getDataLocation(), "FullLists");
            if (!BackupDir.isDirectory())
                BackupDir.mkdir();
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

            File file = new File(BackupDir, dateFormat.format(date) + ".txt");
            try (
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));) {
                writer.append(sb.toString());
                writer.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            lm.consoleMessage("Saved file to FullLists folder with " + file.getName() + " name");
        });
    }
}
