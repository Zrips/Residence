package com.bekvon.bukkit.residence;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.CommandStatus;

public class CommandFiller {

    public final String packagePath = "com.bekvon.bukkit.residence.commands";
    private Map<String, CommandStatus> CommandList = new TreeMap<String, CommandStatus>();

    public List<String> getCommands(boolean simple) {
        Map<String, Integer> cmd = new HashMap<String, Integer>();
        for (Entry<String, CommandStatus> one : CommandList.entrySet()) {
            if (simple && !one.getValue().getSimple() || !simple && one.getValue().getSimple())
                continue;
            cmd.put(one.getKey(), one.getValue().getPriority());
        }
        cmd = Residence.getInstance().getSortingManager().sortByValueASC(cmd);
        List<String> cmdList = new ArrayList<String>();
        for (Entry<String, Integer> one : cmd.entrySet()) {
            cmdList.add(one.getKey());
        }
        return cmdList;
    }

    public List<String> getCommands() {
        Map<String, Integer> cmd = new HashMap<String, Integer>();
        for (Entry<String, CommandStatus> one : CommandList.entrySet()) {
            cmd.put(one.getKey(), one.getValue().getPriority());
        }
        cmd = Residence.getInstance().getSortingManager().sortByValueASC(cmd);
        List<String> cmdList = new ArrayList<String>();
        for (Entry<String, Integer> one : cmd.entrySet()) {
            cmdList.add(one.getKey());
        }
        return cmdList;
    }

    public Map<String, CommandStatus> fillCommands() {
        List<String> lm = new ArrayList<String>();
        HashMap<String, Class<?>> classes = new HashMap<String, Class<?>>();
        try {
            lm = getClassesFromPackage(packagePath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (String one : lm) {
            Class<?> newclass = getClass(one);
            if (newclass != null)
                classes.put(one, newclass);
        }

        for (Entry<String, Class<?>> oneClass : classes.entrySet()) {
            boolean found = false;
            for (Method met : oneClass.getValue().getMethods()) {
                if (!met.isAnnotationPresent(CommandAnnotation.class))
                    continue;

                found = true;

                CommandAnnotation anot = met.getAnnotation(CommandAnnotation.class);

                if (anot.tab().length > 0) {
                    LocaleManager.addTabCompleteMain(oneClass.getValue().getSimpleName(), anot.tab());
                }

                CommandList.put(oneClass.getKey(), new CommandStatus(anot.simple(), anot.priority(), anot.info(), anot.usage()));
                break;
            }
            if (!found) {
                CommandList.put(oneClass.getKey(), new CommandStatus(true, 1000, "", new String[0]));
            }
        }
        return CommandList;
    }

    public static List<String> getClassesFromPackage(String pckgname) throws ClassNotFoundException {
        List<String> result = new ArrayList<String>();
        try {
            for (URL jarURL : ((URLClassLoader) Residence.class.getClassLoader()).getURLs()) {
                try {
                    result.addAll(getClassesInSamePackageFromJar(pckgname, jarURL.toURI().getPath()));
                } catch (URISyntaxException e) {
                }
            }
        } catch (NullPointerException x) {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        }

        return result;
    }

    private static List<String> getClassesInSamePackageFromJar(String packageName, String jarPath) {
        JarFile jarFile = null;
        List<String> listOfCommands = new ArrayList<String>();
        try {
            jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                String entryName = entry.getName();
                packageName = packageName.replace(".", "/");
                if (entryName != null && entryName.endsWith(".class") && entryName.startsWith(packageName)) {
                    String name = entryName.replace(packageName, "").replace(".class", "").replace("/", "");
                    if (name.contains("$"))
                        name = name.split("\\$")[0];
                    listOfCommands.add(name);
                }
            }
        } catch (Exception e) {
        } finally {
            if (jarFile != null)
                try {
                    jarFile.close();
                } catch (Exception e) {
                }
        }
        return listOfCommands;
    }

    private Class<?> getClass(String cmd) {
        Class<?> nmsClass = null;
        try {
            nmsClass = Class.forName(packagePath + "." + cmd.toLowerCase());
        } catch (Throwable e) {
        }
        return nmsClass;
    }

    public Map<String, CommandStatus> getCommandMap() {
        if (CommandList.isEmpty())
            this.fillCommands();
        return CommandList;
    }
}
