package com.bekvon.bukkit.residence.slimeFun;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionManager;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionModule;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;

public class SlimefunManager {

    // Fail safe to avoid infinite checks
    private final static int TRIES = 3;
    private static int times = 0;
    private static CMITask task = null;

    public static void register(Residence residence) {
        task = CMIScheduler.scheduleSyncRepeatingTask(Residence.getInstance(), () -> {
            ++times;
            if (times >= TRIES) {
                lm.consoleMessage("&cFailed to initialize Slimefun support");
                task.cancel();
            }

            ProtectionManager manager = Slimefun.getProtectionManager();

            // Wait for protectionManager to load (loaded on first server tick)
            if (manager != null) {
                try {
                    Method method = manager.getClass().getMethod("registerModule", Server.class, String.class, Function.class);
                    if (method != null) {
                        Function<Plugin, ProtectionModule> m = plugin -> new SlimeFunResidenceModule(plugin);
                        method.invoke(manager, Bukkit.getServer(), "Residence", m);
                        lm.consoleMessage("Enabled compatability with Slimefun plugin");
                        task.cancel();
                    }
                } catch (Throwable e) {
                    // Going with latest dev build supported approach
                    manager.registerModule(Bukkit.getServer().getPluginManager(), "Residence", plugin -> new SlimeFunResidenceModule(plugin));
                    lm.consoleMessage("Enabled compatability with Slimefun plugin");
                    task.cancel();
                }
            }
        }, 20, 20);
    }
}
