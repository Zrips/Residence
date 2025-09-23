package com.bekvon.bukkit.residence.bigDoors;

import java.lang.reflect.Method;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.lm;

import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;
import net.Zrips.CMILib.Version.Schedulers.CMITask;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.compatibility.IProtectionCompat;
import nl.pim16aap2.bigDoors.compatibility.ProtectionCompatManager;

public class BigDoorsManager {

    // Fail safe to avoid infinite checks
    private final static int TRIES = 3;
    private static int times = 0;
    private static CMITask task = null;

    public static void register(Residence residence) {
        task = CMIScheduler.scheduleSyncRepeatingTask(Residence.getInstance(), () -> {
            ++times;
            if (times >= TRIES) {
                lm.consoleMessage("&cFailed to initialize BigDoors support");
                task.cancel();
            }

            ProtectionCompatManager manager = BigDoors.get().getProtectionCompatManager();

            // Wait for protectionManager to load (loaded on first server tick)
            if (manager != null) {
                try {

                    manager.registerProtectionCompatDefinition(new BigDoorsDef("Residence") {
                        @Override
                        public Class<? extends IProtectionCompat> getClass(String version) {
                            return BigDoorsModule.class;
                        }
                    });

                    Method method = manager.getClass().getDeclaredMethod("addProtectionCompat", IProtectionCompat.class);
                    method.setAccessible(true);
                    method.invoke(manager, new BigDoorsModule());

                    lm.consoleMessage("Enabled compatability with BigDoors plugin");
                    task.cancel();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, 20, 20);
    }
}
