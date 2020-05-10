package com.bekvon.bukkit.cmiLib;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

public class VersionChecker {
    private static int resource = 11480;
    Residence plugin;

    public VersionChecker(Residence plugin) {
        this.plugin = plugin;
    }


    public Version getVersion() {
        return Version.getCurrent();
    }

    public Integer convertVersion(String v) {
        v = v.replaceAll("[^\\d.]", "");
        int version = 0;
        if (v.contains(".")) {
            String lVersion = "";
            for (String one : v.split("\\.")) {
                String s = one;
                if (s.length() == 1)
                    s = "0" + s;
                lVersion += s;
            }

            try {
                version = Integer.parseInt(lVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                version = Integer.parseInt(v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return version;
    }

    public void VersionCheck(final Player player) {
        if (!plugin.getConfigManager().versionCheck())
            return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                String currentVersion = plugin.getDescription().getVersion();
                String newVersion = getNewVersion();
                if (newVersion == null || newVersion.equalsIgnoreCase(currentVersion))
                    return;
                List<String> msg = Arrays.asList(
                        ChatColor.GREEN + "*********************** " + plugin.getDescription().getName() + " **************************",
                        ChatColor.GREEN + "* " + newVersion + " is now available! Your version: " + currentVersion,
                        ChatColor.GREEN + "* " + ChatColor.DARK_GREEN + plugin.getDescription().getWebsite(),
                        ChatColor.GREEN + "************************************************************");
                for (String one : msg)
                    if (player != null)
                        player.sendMessage(one);
                    else
                        Bukkit.getConsoleSender().sendMessage(one);
            }
        });
    }

    public String getNewVersion() {
        try {
            URLConnection con = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resource).openConnection();
            String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (version.length() <= 8)
                return version;
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to check for " + plugin.getDescription().getName() + " update on spigot web page.");
        }
        return null;
    }

    public enum Version {
        v1_7_R1,
        v1_7_R2,
        v1_7_R3,
        v1_7_R4,
        v1_8_R1,
        v1_8_R2,
        v1_8_R3,
        v1_9_R1,
        v1_9_R2,
        v1_10_R1,
        v1_11_R1,
        v1_12_R1,
        v1_13_R1,
        v1_13_R2,
        v1_13_R3,
        v1_14_R1,
        v1_14_R2,
        v1_15_R1,
        v1_15_R2,
        v1_16_R1,
        v1_16_R2,
        v1_17_R1,
        v1_17_R2;

        private static Version current = null;
        private Integer value;
        private String shortVersion;

        Version() {
            try {
                this.value = Integer.valueOf(this.name().replaceAll("[^\\d.]", ""));
            } catch (Exception e) {
            }
            shortVersion = this.name().substring(0, this.name().length() - 3);
        }

        public static Version getCurrent() {
            if (current != null)
                return current;
            String[] v = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
            String vv = v[v.length - 1];
            for (Version one : values()) {
                if (one.name().equalsIgnoreCase(vv)) {
                    current = one;
                    break;
                }
            }
            return current;
        }

        public static boolean isCurrentEqualOrHigher(Version v) {
            return getCurrent().getValue() >= v.getValue();
        }

        public static boolean isCurrentHigher(Version v) {
            return getCurrent().getValue() > v.getValue();
        }

        public static boolean isCurrentLower(Version v) {
            return getCurrent().getValue() < v.getValue();
        }

        public static boolean isCurrentEqualOrLower(Version v) {
            return getCurrent().getValue() <= v.getValue();
        }

        public Integer getValue() {
            return value;
        }

        public String getShortVersion() {
            return shortVersion;
        }

        public boolean isLower(Version version) {
            return getValue() < version.getValue();
        }

        public boolean isHigher(Version version) {
            return getValue() > version.getValue();
        }

        public boolean isEqualOrLower(Version version) {
            return getValue() <= version.getValue();
        }

        public boolean isEqualOrHigher(Version version) {
            return getValue() >= version.getValue();
        }
    }

}
