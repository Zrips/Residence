package com.bekvon.bukkit.residence.text.help;

import com.bekvon.bukkit.cmiLib.CMIChatColor;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.ResidenceCommandListener;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.HelpLines;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class HelpEntry {
    protected static int linesPerPage = 8;
    protected String name;
    protected String desc;
    protected String[] lines;
    protected List<HelpEntry> subentrys;
    File langFile = new File(new File(Residence.getInstance().getDataLocation(), "Language"), "English.yml");

    public HelpEntry(String entryname) {
        name = entryname;
        subentrys = new ArrayList<HelpEntry>();
        lines = new String[0];
    }

    public static HelpEntry parseHelp(FileConfiguration node, String key) {
        String[] split = key.split("\\.");
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
                        entry.lines[i] = ChatColor.translateAlternateColorCodes('&', stringList.get(i));
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
                    subcommandkeys.addAll(Residence.getInstance().getCommandFiller().getCommands());
                }
                for (String subkey : subcommandkeys) {
                    entry.subentrys.add(HelpEntry.parseHelp(node, key + ".SubCommands." + subkey));
                }
            }
        }
        return entry;
    }

    public String getName() {
        if (name == null)
            return "";
        return name;
    }

    public void setName(String inname) {
        name = inname;
    }

    public String getDescription() {
        if (desc == null)
            return "";
        return desc;
    }

    public void setDescription(String description) {
        desc = description;
    }

    public void printHelp(CommandSender sender, int page, boolean resadmin, String path) {
        List<HelpLines> helplines = this.getHelpData(sender, resadmin);
        path = "/" + path.replace(".", " ") + " ";

        PageInfo pi = new PageInfo(linesPerPage, helplines.size(), page);

        if (!pi.isPageOk()) {
            Residence.getInstance().msg(sender, lm.Invalid_Help);
            return;
        }

//	String separator = Residence.getInstance().msg(lm.InformationPage_SmallSeparator);

//	sender.sendMessage(separator + " " + Residence.getInstance().msg(lm.General_HelpPageHeader, path, page, pi.getTotalPages()) + " " + separator);

        for (int i = pi.getStart(); i <= pi.getEnd(); i++) {
            if (helplines.get(i).getCommand() != null) {
                HelpEntry sub = this.getSubEntry(helplines.get(i).getCommand());

                StringBuilder desc = new StringBuilder("&6");
                int y = 0;
                for (String one : sub.lines) {
                    desc.append(one);
                    y++;
                    if (y < sub.lines.length) {
                        desc.append("\n");
                    }
                }

                if (resadmin)
                    path = path.replace("/res ", "/resadmin ");

                RawMessage rm = new RawMessage();
                rm.add(CMIChatColor.translateAlternateColorCodes("&6" + helplines.get(i).getDesc()), desc.toString(), null, path + helplines.get(i).getCommand());
                rm.show(sender);

            } else
                sender.sendMessage(CMIChatColor.translateAlternateColorCodes("&6" + helplines.get(i).getDesc()));

        }

        String baseCmd = resadmin ? "resadmin" : "res";
        String cmd = !name.equalsIgnoreCase("res") ? "/" + baseCmd + " " + name + " ? " : "/" + baseCmd + " ? ";
        Residence.getInstance().getInfoPageManager().ShowPagination(sender, pi, cmd);
    }

    public void printHelp(CommandSender sender, int page, String path, boolean resadmin) {
        HelpEntry subEntry = this.getSubEntry(path);
        if (subEntry != null) {
            subEntry.printHelp(sender, page, resadmin, path);
        } else {
            Residence.getInstance().msg(sender, lm.Invalid_Help);
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
                    unsortMap.put(entry.getName(), Residence.getInstance().msg(lm.InformationPage_FlagsList, flagName, desc));
                    continue;
                }
            }

            helplines.add(new HelpLines(entry.getName(), Residence.getInstance().msg(lm.InformationPage_GeneralList, entry.getName(), entry.getDescription())));
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


    public Set<String> getSubCommands(CommandSender sender, String[] args) {
        Set<String> subCommands = new HashSet<String>();

        if (langFile.isFile()) {
            FileConfiguration node = new YamlConfiguration();
            try {
                node.load(langFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }

            ConfigurationSection meinPath = node.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands");
            ConfigurationSection tempmeinPath = node.getConfigurationSection("CommandHelp.SubCommands.res.SubCommands");

            if (args.length == 1)
                return meinPath.getKeys(false);

            boolean ok = true;
            int i = 0;
            while (ok) {

                if (i >= args.length)
                    break;

                if (args[i].equalsIgnoreCase(""))
                    return tempmeinPath.getKeys(false);

                if (!tempmeinPath.isConfigurationSection(args[i] + ".SubCommands"))
                    break;

                tempmeinPath = tempmeinPath.getConfigurationSection(args[i] + ".SubCommands");

                i++;
            }

            int neededArgPlace = args.length - 2 - i;

            boolean subCommand = true;
            if (i < args.length && tempmeinPath.isConfigurationSection(args[i])) {
                subCommand = false;
                tempmeinPath = tempmeinPath.getConfigurationSection(args[i]);
            }

            List<String> ArgsList = new ArrayList<String>();

            int ii = 0;
            for (Entry<List<String>, List<String>> one : Residence.getInstance().getLocaleManager().CommandTab.entrySet()) {
                List<String> list = one.getKey();
                if (list.size() > ii && args.length > ii && list.get(ii).equalsIgnoreCase(args[ii])) {
                    ArgsList = one.getValue();
                }
                i++;
            }

            String NeededArg = null;
            if (neededArgPlace < ArgsList.size() && neededArgPlace >= 0)
                NeededArg = ArgsList.get(neededArgPlace);

            Player playerSender = null;
            if (sender instanceof Player)
                playerSender = (Player) sender;

            if (NeededArg != null) {

                List<String> list = new ArrayList<String>();

                if (NeededArg.contains("%%")) {
                    list.addAll(Arrays.asList(NeededArg.split("%%")));
                } else
                    list.add(NeededArg);

                for (String oneArg : list) {
                    switch (oneArg) {
                        case "[playername]":
                            for (Player one : Bukkit.getOnlinePlayers()) {
                                if (playerSender == null || one.canSee(playerSender))
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
                                ArrayList<String> resList = Residence.getInstance().getResidenceManager().getResidenceList(Residence.getInstance().getServerLandName(), true, false, false);
                                if (resList.size() > 0)
                                    subCommands.addAll(resList);
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
                        case "[residenceshop]":
                            for (ClaimedResidence one : Residence.getInstance().getResidenceManager().getShops()) {
                                subCommands.add(one.getName());
                            }
                            break;
                        case "[flag]":

                            for (String one : FlagPermissions.getAllPosibleFlags()) {

                                Flags f = Flags.getFlag(one);

                                if (f != null) {
                                    if (!f.isGlobalyEnabled())
                                        continue;
                                    subCommands.add(f.getName());
                                }
                                subCommands.add(one);

                            }
                            for (String one : FlagPermissions.getPosibleAreaFlags()) {

                                Flags f = Flags.getFlag(one);

                                if (f != null) {
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
                        default:
                            subCommands.add(oneArg);
                            break;
                    }
                }
            }

//	    String command = tempmeinPath.getCurrentPath().replace("CommandHelp.SubCommands.", "").replace(".SubCommands.", " ");
            if (subCommands.size() > 0) {
                return subCommands;
            }

            if (subCommand)
                return tempmeinPath.getKeys(false);
//	    Bukkit.dispatchCommand(sender, command + " ?");
        }
        return new HashSet<String>(Collections.singletonList("?"));
    }
}
