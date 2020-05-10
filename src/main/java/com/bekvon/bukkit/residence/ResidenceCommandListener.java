package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResidenceCommandListener implements CommandExecutor {

    private static final String label = "res";
    private static List<String> AdminCommands = new ArrayList<String>();
    private Residence plugin;

    public ResidenceCommandListener(Residence plugin) {
        this.plugin = plugin;
    }

    public static List<String> getAdminCommands() {
        if (AdminCommands.size() == 0)
            AdminCommands = Residence.getInstance().getCommandFiller().getCommands(false);
        return AdminCommands;
    }

    private static String[] reduceArgs(String[] args) {
        if (args.length <= 1)
            return new String[0];

        return Arrays.copyOfRange(args, 1, args.length);
    }

    private static cmd getCmdClass(String[] args) {
        cmd cmdClass = null;
        try {
            Class<?> nmsClass;
            nmsClass = Class.forName("com.bekvon.bukkit.residence.commands." + args[0].toLowerCase());
            if (cmd.class.isAssignableFrom(nmsClass)) {
                cmdClass = (cmd) nmsClass.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
        }
        return cmdClass;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

//	if (args.length < 3)
//	    return true;

        ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(), args, sender);
        Bukkit.getPluginManager().callEvent(cevent);
        if (cevent.isCancelled()) {
            return true;
        }

        if (sender instanceof Player && !plugin.getPermissionManager().isResidenceAdmin(sender) && plugin.isDisabledWorldCommand(((Player) sender)
                .getWorld())) {
            plugin.msg(sender, lm.General_DisabledWorld);
            return true;
        }

        if (command.getName().equals("resreload") && args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (plugin.getPermissionManager().isResidenceAdmin(player) && ResPerm.topadmin.hasPermission(player)) {
                    plugin.reloadPlugin();
                    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded config.");
                    System.out.println("[Residence] Reloaded by " + player.getName() + ".");
                } else
                    plugin.msg(player, lm.General_NoPermission);
            } else {
                plugin.reloadPlugin();
                System.out.println("[Residence] Reloaded by console.");
            }
            return true;
        }
        if (command.getName().equals("resload")) {
            if (!(sender instanceof Player) || sender instanceof Player && plugin.getPermissionManager().isResidenceAdmin(sender) && ResPerm.topadmin.hasPermission(sender)) {
                try {
                    plugin.loadYml();
                    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded save file...");
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "[Residence] Unable to reload the save file, exception occured!");
                    sender.sendMessage(ChatColor.RED + ex.getMessage());
                    Logger.getLogger(Residence.getInstance().getClass().getName()).log(Level.SEVERE, null, ex);
                }
            } else
                plugin.msg(sender, lm.General_NoPermission);
            return true;
        } else if (command.getName().equals("rc")) {
            cmd cmdClass = getCmdClass(new String[]{"rc"});
            if (cmdClass == null) {
                sendUsage(sender, command.getName());
                return true;
            }
            boolean respond = cmdClass.perform(Residence.getInstance(), sender, reduceArgs(args), false);
            if (!respond)
                sendUsage(sender, command.getName());
            return true;
        } else if (command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
            boolean resadmin = false;
            if (sender instanceof Player) {
                if (command.getName().equals("resadmin") && plugin.getPermissionManager().isResidenceAdmin(sender)) {
                    resadmin = true;
                }
                if (command.getName().equals("resadmin") && !plugin.getPermissionManager().isResidenceAdmin(sender)) {
                    ((Player) sender).sendMessage(plugin.msg(lm.Residence_NonAdmin));
                    return true;
                }
                if (command.getName().equals("res") && plugin.getPermissionManager().isResidenceAdmin(sender) && plugin.getConfigManager().getAdminFullAccess()) {
                    resadmin = true;
                }
            } else {
                resadmin = true;
            }
            if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?") || args.length > 1 && args[args.length - 2].equals("?")) {
                return commandHelp(args, resadmin, sender, command);
            }

            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            } else {
                resadmin = true;
            }
            if (plugin.getConfigManager().allowAdminsOnly()) {
                if (!resadmin && player != null) {
                    plugin.msg(player, lm.General_AdminOnly);
                    return true;
                }
            }
            if (args.length == 0) {
                args = new String[1];
                args[0] = "?";
            }

            String cmd = args[0].toLowerCase();

            switch (cmd) {
                case "delete":
                    cmd = "remove";
                    break;
                case "sz":
                    cmd = "subzone";
                    break;
            }

            cmd cmdClass = getCmdClass(args);
            if (cmdClass == null) {
                return commandHelp(new String[]{"?"}, resadmin, sender, command);
            }

            if (!resadmin && !ResPerm.command_$1.hasPermission(sender, args[0].toLowerCase())) {
                RawMessage rm = new RawMessage();
                rm.add(plugin.msg(lm.General_NoPermission), "&7" + ResPerm.command_$1.getPermission(args[0].toLowerCase()));
                rm.show(sender);

                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                plugin.msg(console, plugin.msg(lm.General_NoPermission) + " " + ResPerm.command_$1.getPermission(args[0].toLowerCase()));
                return true;
            }

            if (!resadmin && player != null && plugin.resadminToggle.contains(player.getName())) {
                if (!plugin.getPermissionManager().isResidenceAdmin(player)) {
                    plugin.resadminToggle.remove(player.getName());
                }
            }

            String[] targ = reduceArgs(args);

            for (Method met : cmdClass.getClass().getMethods()) {
                if (!met.isAnnotationPresent(CommandAnnotation.class))
                    continue;
                CommandAnnotation cs = met.getAnnotation(CommandAnnotation.class);

                varCheck:
                if (sender instanceof Player) {
                    int[] regVar = cs.regVar();
                    List<Integer> list = new ArrayList<Integer>();
                    boolean more = true;
                    for (int one : regVar) {
                        if (one < 0)
                            more = false;
                        list.add(one);
                    }

                    int size = targ.length;

                    boolean good = true;

                    if (list.isEmpty())
                        break varCheck;

                    if (list.contains(666) || list.contains(-666)) {
                        plugin.msg(sender, lm.Invalid_FromConsole);
                        return false;
                    }

                    if (list.contains(-size))
                        good = false;

                    if (list.contains(size))
                        good = true;

                    if (list.contains(-100) && size == 0)
                        good = false;

                    if (more && !list.contains(size))
                        good = false;

                    if (!good) {
                        String[] tempArray = new String[args.length + 1];
                        System.arraycopy(args, 0, tempArray, 0, args.length);
                        tempArray[args.length] = "?";
                        args = tempArray;
                        return commandHelp(args, resadmin, sender, command);
                    }
                } else {

                    int[] consoleVar = cs.consoleVar();
                    List<Integer> list = new ArrayList<Integer>();
                    boolean more = true;
                    for (int one : consoleVar) {
                        if (one < 0)
                            more = false;
                        list.add(one);
                    }
                    int size = targ.length;
                    boolean good = true;

                    if (list.isEmpty())
                        break varCheck;

                    if (list.contains(666) || list.contains(-666)) {
                        plugin.msg(sender, lm.Invalid_Ingame);
                        return false;
                    }

                    if (list.contains(-size))
                        good = false;

                    if (list.contains(size))
                        good = true;

                    if (list.contains(-100) && size == 0)
                        good = false;

                    if (more && !list.contains(size))
                        good = false;
                    if (!good) {
                        String[] tempArray = new String[args.length + 1];
                        System.arraycopy(args, 0, tempArray, 0, args.length);
                        tempArray[args.length] = "?";
                        args = tempArray;
                        return commandHelp(args, resadmin, sender, command);
                    }
                }
            }

            Boolean respond = cmdClass.perform(Residence.getInstance(), sender, targ, resadmin);

            if (respond != null && !respond) {
                String[] tempArray = new String[args.length + 1];
                System.arraycopy(args, 0, tempArray, 0, args.length);
                tempArray[args.length] = "?";
                args = tempArray;
                return commandHelp(args, resadmin, sender, command);
            }

            return respond == null || !respond ? false : true;
        }
        return this.onCommand(sender, command, label, args);
    }

    public void sendUsage(CommandSender sender, String command) {
        plugin.msg(sender, lm.General_DefaultUsage, command);
    }

    private boolean commandHelp(String[] args, boolean resadmin, CommandSender sender, Command command) {
        if (plugin.getHelpPages() == null)
            return false;

        String helppath = getHelpPath(args);

        int page = 1;
        if (!args[args.length - 1].equalsIgnoreCase("?")) {
            try {
                page = Integer.parseInt(args[args.length - 1]);
            } catch (Exception ex) {
                plugin.msg(sender, lm.General_InvalidHelp);
            }
        }

        if (command.getName().equalsIgnoreCase("res"))
            resadmin = false;
        if (plugin.getHelpPages().containesEntry(helppath))
            plugin.getHelpPages().printHelp(sender, page, helppath, resadmin);
        return true;
    }

    private String getHelpPath(String[] args) {
        StringBuilder helppath = new StringBuilder("res");
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("?")) {
                break;
            }
            helppath.append(".").append(args[i]);
        }
        if (!plugin.getHelpPages().containesEntry(helppath.toString()) && args.length > 0)
            return getHelpPath(Arrays.copyOf(args, args.length - 1));
        return helppath.toString();
    }

}
