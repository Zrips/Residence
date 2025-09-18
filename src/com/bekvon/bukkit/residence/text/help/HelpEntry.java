package com.bekvon.bukkit.residence.text.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.Placeholders.Placeholder.CMIPlaceHolders;
import com.bekvon.bukkit.residence.commands.pset;
import com.bekvon.bukkit.residence.commands.set;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.Flags.FlagMode;
import com.bekvon.bukkit.residence.containers.HelpLines;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.CMIList;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.Version.Version;

public class HelpEntry {
    protected String name;
    protected String desc;
    protected String[] lines;
    protected List<HelpEntry> subentrys;
    protected static int linesPerPage = 8;

    public HelpEntry(String entryname) {
        name = entryname;
        subentrys = new ArrayList<HelpEntry>();
        lines = new String[0];
    }

    public String getName() {
        if (name == null)
            return "";
        return name;
    }

    public void setName(String inname) {
        name = inname;
    }

    public void setDescription(String description) {
        desc = description;
    }

    public String getDescription() {
        if (desc == null)
            return "";
        return desc;
    }

    public void printHelp(CommandSender sender, int page, boolean resadmin, String path) {
        List<HelpLines> helplines = this.getHelpData(sender, resadmin);
        path = "/" + path.replace(".", " ") + " ";

        PageInfo pi = new PageInfo(linesPerPage, helplines.size(), page);
        if (!pi.isPageOk()) {
            lm.Invalid_Help.sendMessage(sender);
            return;
        }

//	String separator = Residence.getInstance().msg(lm.InformationPage_SmallSeparator);

//	sender.sendMessage(separator + " " + Residence.getInstance().msg(lm.General_HelpPageHeader, path, page, pi.getTotalPages()) + " " + separator);

        for (int i = pi.getStart(); i <= pi.getEnd(); i++) {
            if (helplines.get(i).getCommand() != null) {
                HelpEntry sub = this.getSubEntry(helplines.get(i).getCommand());

                String desc = "&6";
                int y = 0;
                for (String one : sub.lines) {
                    desc += one;
                    y++;
                    if (y < sub.lines.length) {
                        desc += "\n";
                    }
                }

                if (resadmin)
                    path = path.replace("/res ", "/resadmin ");

                RawMessage rm = new RawMessage();
                rm.addText(CMIChatColor.translate("&6" + helplines.get(i).getDesc())).addHover(desc).addSuggestion(path + helplines.get(i).getCommand());
                rm.show(sender);

            } else
                sender.sendMessage(CMIChatColor.translate("&6" + helplines.get(i).getDesc()));
        }

        String baseCmd = resadmin ? "resadmin" : "res";
        String cmd = !name.equalsIgnoreCase("res") ? "/" + baseCmd + " " + name + " ?" : "/" + baseCmd + " ?";
        pi.autoPagination(sender, cmd);
    }

    public void printHelp(CommandSender sender, int page, String path, boolean resadmin) {
        HelpEntry subEntry = this.getSubEntry(path);
        if (subEntry != null) {
            subEntry.printHelp(sender, page, resadmin, path);
        } else {
            lm.Invalid_Help.sendMessage(sender);
        }
    }

    private List<HelpLines> getHelpData(CommandSender sender, boolean resadmin) {
        List<HelpLines> helplines = new ArrayList<HelpLines>();

        for (String one : lines) {
            helplines.add(new HelpLines(null, one));
        }

        FlagPermissions GlobalFlags = Residence.getInstance().getPermissionManager().getAllFlags();

        Map<String, String> unsortMap = new HashMap<String, String>();

        for (HelpEntry entry : subentrys) {

            if (!name.equalsIgnoreCase("flags")) {
                if (ResidenceCommandListener.getAdminCommands().contains(entry.getName().toLowerCase()) && !resadmin)
                    continue;

                if (!ResidenceCommandListener.getAdminCommands().contains(entry.getName().toLowerCase()) && resadmin)
                    continue;

            } else {
                if (GlobalFlags.getFlags().containsKey(entry.getName().toLowerCase())) {
                    Boolean state = GlobalFlags.getFlags().get(entry.getName().toLowerCase());
                    if (!state && !resadmin && !ResPerm.flag_$1.hasPermission(sender, entry.getName().toLowerCase())) {
                        continue;
                    }

                    String flagName = entry.getName();
                    Flags flag = Flags.getFlag(entry.getName());
                    if (flag != null)
                        flagName = flag.getName();

                    String desc = entry.getDescription();

                    switch (entry.getName().toLowerCase()) {
                    case "wspeed1":
                        desc = desc.replace("%1", Residence.getInstance().getConfigManager().getWalkSpeed1() + "");
                        break;
                    case "wspeed2":
                        desc = desc.replace("%1", Residence.getInstance().getConfigManager().getWalkSpeed2() + "");
                        break;
                    }

                    // adding flag name and description for later sorting

                    unsortMap.put(entry.getName(), lm.InformationPage_FlagsList.getMessage(flagName, desc));
                    continue;
                }
            }

            helplines.add(new HelpLines(entry.getName(), lm.InformationPage_GeneralList.getMessage(entry.getName(), entry.getDescription())));
        }

        if (!unsortMap.isEmpty()) {
            // Sorting flags help page by alphabet
            unsortMap = Residence.getInstance().getSortingManager().sortStringByKeyASC(unsortMap);
            // Converting HashMap to helplines
            for (Entry<String, String> one : unsortMap.entrySet()) {
                helplines.add(new HelpLines(one.getKey(), one.getValue()));
            }
        }

        return helplines;
    }

    public boolean containesEntry(String name) {
        return this.getSubEntry(name) != null;
    }

    public HelpEntry getSubEntry(String name) {
        String[] split = name.split("\\.");
        HelpEntry entry = this;
        for (String entryname : split) {
            entry = entry.findSubEntry(entryname);
            if (entry == null)
                return null;
        }
        return entry;
    }

    private HelpEntry findSubEntry(String name) {
        for (HelpEntry entry : subentrys) {
            if (entry.getName().equalsIgnoreCase(name))
                return entry;
        }
        return null;
    }

    public void addSubEntry(HelpEntry entry) {
        if (!subentrys.contains(entry)) {
            subentrys.add(entry);
        }
    }

    public void removeSubEntry(HelpEntry entry) {
        if (subentrys.contains(entry)) {
            subentrys.remove(entry);
        }
    }

    public int getSubEntryCount() {
        return subentrys.size();
    }

    public static HelpEntry parseHelp(FileConfiguration node, String key) {
        String split[] = key.split("\\.");
        String thisname = split[split.length - 1];
        HelpEntry entry = new HelpEntry(thisname);
        ConfigurationSection keysnode = node.getConfigurationSection(key);

        Set<String> keys = null;
        if (keysnode != null)
            keys = keysnode.getKeys(false);
        if (keys != null) {
            if (keys.contains("Info")) {
                List<String> stringList = node.getStringList(key + ".Info");
                if (stringList != null) {
                    entry.lines = new String[stringList.size()];
                    for (int i = 0; i < stringList.size(); i++) {
                        entry.lines[i] = CMIChatColor.translate(stringList.get(i));
                    }
                }
            }
            if (keys.contains("Description")) {
                entry.desc = node.getString(key + ".Description");
            }
            if (keys.contains("SubCommands")) {
                Set<String> subcommandkeys = node.getConfigurationSection(key + ".SubCommands").getKeys(false);
                if (key.equalsIgnoreCase("CommandHelp.SubCommands.res")) {
                    subcommandkeys.clear();
                    for (String one : Residence.getInstance().getCommandFiller().getCommands()) {
                        subcommandkeys.add(one);
                    }
                }
                for (String subkey : subcommandkeys) {
                    entry.subentrys.add(HelpEntry.parseHelp(node, key + ".SubCommands." + subkey));
                }
            }
        }
        return entry;
    }

    private static String getMp(HashMap<String, List<String>> mp) {
        StringBuilder st = new StringBuilder();
        for (String one : mp.keySet()) {
            if (!st.toString().isEmpty())
                st.append("%%");
            st.append(one);
        }
        return st.toString();
    }

    private static String getMpEntry(HashMap<String, List<String>> mp) {
        StringBuilder st = new StringBuilder();
        for (Entry<String, List<String>> one : mp.entrySet()) {
            if (!st.toString().isEmpty())
                st.append("%%");
            if (one.getKey().equalsIgnoreCase("") && !one.getValue().isEmpty())
                st.append(one.getValue().get(0));
            else
                st.append(one.getKey());
        }
        return st.toString();
    }

    @SuppressWarnings("deprecation")
    public Set<String> getSubCommands(CommandSender sender, String[] args) {
        Set<String> subCommands = new HashSet<String>();
        int neededArgPlace = args.length - 2;

        if (neededArgPlace < 0)
            neededArgPlace = 0;

        List<String> ArgsList = new ArrayList<String>();

        if (args.length > 0) {
            HashMap<String, List<String>> mp = new HashMap<String, List<String>>();
            List<String> base = new ArrayList<String>();
            for (Entry<String, HashMap<String, List<String>>> one : Residence.getInstance().getLocaleManager().CommandTab.entrySet()) {
                if (one.getKey().startsWith(args[0].toLowerCase())) {
                    mp.putAll(one.getValue());
                    base.add(one.getKey());
                }
            }

            if (!mp.isEmpty()) {
                if (args.length > 1) {
                    if (args[args.length - 1].isEmpty()) {
                        List<String> ls = mp.get(args[1].toLowerCase());
                        if (ls != null) {
                            neededArgPlace--;
                            if (args.length == 2) {
                                ArgsList.add(getMpEntry(mp));
                            } else
                                ArgsList = ls;
                        } else {
                            ls = mp.get("");
                            if (ls != null) {
                                ArgsList = ls;
                            } else {
                                ArgsList.add(getMp(mp));
                            }
                        }
                    } else {
                        List<String> main = mp.get("");
                        if (main != null) {
                            if (args.length == 2) {
                                ArgsList.add(getMpEntry(mp));
                            } else
                                ArgsList = main;
                        } else {
                            ArgsList.add(getMp(mp));
                        }
                    }
                } else {
                    ArgsList.add(CMIList.listToString(base, "%%"));
                    ArgsList.add(getMp(mp));
                }
            } else {
                for (String one : Residence.getInstance().getLocaleManager().CommandTab.keySet()) {
                    if (ResPerm.command_$1.hasPermission(sender, one))
                        subCommands.add(one);
                }
                return subCommands;
            }
        }

        String NeededArg = null;

        if (neededArgPlace < 0)
            neededArgPlace = 0;
        if (neededArgPlace < ArgsList.size() && neededArgPlace >= 0) {
            NeededArg = ArgsList.get(neededArgPlace);
        }

        Player playerSender = null;
        if (sender instanceof Player)
            playerSender = (Player) sender;

        if (NeededArg != null) {

            List<String> list = new ArrayList<String>();

            if (NeededArg.contains("%%")) {
                list.addAll(Arrays.asList(NeededArg.split("%%")));
            } else {
                list.add(NeededArg);
            }

            for (String oneArg : list) {
                switch (oneArg) {
                case "[playername]":
                    for (Player one : Bukkit.getOnlinePlayers()) {
                        if (playerSender == null || playerSender.canSee(one))
                            subCommands.add(one.getName());
                    }
                    break;
                case "[residence]":
                    if (sender instanceof Player) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(((Player) sender).getLocation());
                        if (res != null) {
                            String resName = res.getName();
                            if (resName != null)
                                subCommands.add(resName);
                        }
                        List<ClaimedResidence> resList = Residence.getInstance().getPlayerManager().getResidencePlayer(((Player) sender)).getResList();
                        for (ClaimedResidence oneRes : resList) {
                            subCommands.add(oneRes.getName());
                        }
                    } else {
                        subCommands.addAll(Residence.getInstance().getResidenceManager().getResidenceList(Residence.getInstance().getServerUUID(), true, false, false));
                    }
                    break;
                case "[cresidence]":
                    if (sender instanceof Player) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(((Player) sender).getLocation());
                        if (res != null) {
                            String resName = res.getName();
                            if (resName != null)
                                subCommands.add(resName);
                        }
                    }
                    break;
                case "[carea]":
                    if (sender instanceof Player) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(((Player) sender).getLocation());
                        if (res != null) {
                            String resName = res.getAreaIDbyLoc(((Player) sender).getLocation());
                            if (resName != null)
                                subCommands.add(resName);
                        }
                    }
                    break;
                case "[enter]":
                    if (sender instanceof Player) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(((Player) sender).getLocation());
                        if (res != null) {
                            String resName = res.getEnterMessage();
                            if (resName != null)
                                subCommands.add(CMIChatColor.deColorize(resName));
                        }
                    }
                    break;
                case "[leave]":
                    if (sender instanceof Player) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(((Player) sender).getLocation());
                        if (res != null) {
                            String resName = res.getLeaveMessage();
                            if (resName != null)
                                subCommands.add(CMIChatColor.deColorize(resName));
                        }
                    }
                    break;
                case "[residenceshop]":
                    for (ClaimedResidence one : Residence.getInstance().getResidenceManager().getShops()) {
                        subCommands.add(one.getName());
                    }
                    break;
                case "[placeholder]":
                    for (CMIPlaceHolders one : CMIPlaceHolders.values()) {
                        subCommands.add(one.getFull());
                    }
                    break;
                case "[flag]":

                    FlagMode mode = FlagMode.Both;
                    if (args.length > 0) {
                        if (args[0].equalsIgnoreCase(set.class.getSimpleName()))
                            mode = FlagMode.Residence;
                        else if (args[0].equalsIgnoreCase(pset.class.getSimpleName()))
                            mode = FlagMode.Player;
                    }

                    if (args.length > 1 && Flags.getFlag(args[args.length - 2]) != null) {
                        continue;
                    }

                    for (String one : FlagPermissions.getAllPossibleFlags()) {
                        Flags f = Flags.getFlag(one);

                        if (f != null) {

                            if (f.getFlagMode() != FlagMode.Both && f.getFlagMode() != mode)
                                continue;

                            if (!f.isGlobalyEnabled())
                                continue;
                            subCommands.add(f.getName());
                        }
                        subCommands.add(one);
                    }

                    for (String one : FlagPermissions.getPossibleAreaFlags()) {
                        Flags f = Flags.getFlag(one);
                        if (f != null) {

                            if (f.getFlagMode() != FlagMode.Both && f.getFlagMode() != mode)
                                continue;

                            if (!f.isGlobalyEnabled())
                                continue;
                            subCommands.add(f.getName());
                        }
                        subCommands.add(one);
                    }
                    break;
                case "[material]":
                    for (Material one : Material.values()) {
                        subCommands.add(one.name().toLowerCase());
                    }
                    break;
                case "[materialId]":
                    if (Version.isCurrentEqualOrLower(Version.v1_13_R2))
                        for (Material one : Material.values()) {
                            subCommands.add(String.valueOf(one.getId()));
                        }
                    break;
                case "[worldname]":
                    for (World one : Bukkit.getWorlds()) {
                        subCommands.add(one.getName());
                    }
                    break;
                case "[x]":
                    if (playerSender != null)
                        subCommands.add(String.valueOf(playerSender.getLocation().getBlockX()));
                    break;
                case "[y]":
                    if (playerSender != null)
                        subCommands.add(String.valueOf(playerSender.getLocation().getBlockY()));
                    break;
                case "[z]":
                    if (playerSender != null)
                        subCommands.add(String.valueOf(playerSender.getLocation().getBlockZ()));
                    break;
                default:
                    subCommands.add(oneArg);
                    break;
                }
            }
        }

        if (!subCommands.isEmpty()) {
            return subCommands;
        }

        return new HashSet<String>(Arrays.asList("?"));
    }
}
