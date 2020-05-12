package com.residence.zip;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipLibrary {
    private Residence plugin;
    private File BackupDir = new File(Residence.getInstance().getDataLocation(), "Backup");

    public ZipLibrary(Residence residence) {
        this.plugin = residence;
    }

    private static void packZip(File output, List<File> sources) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
        zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);

        for (File source : sources) {
            if (source.isDirectory()) {
                zipDir(zipOut, "", source);
            } else {
                zipFile(zipOut, "", source);
            }
        }

        zipOut.flush();
        zipOut.close();
    }

    private static String buildPath(String path, String file) {
        if (path == null || path.isEmpty()) {
            return file;
        }

        return path + File.separator + file;
    }

    private static void zipDir(ZipOutputStream zos, String path, File dir) throws IOException {
        if (!dir.canRead()) {
            return;
        }

        File[] files = dir.listFiles();
        path = buildPath(path, dir.getName());

        for (File source : files) {
            if (source.isDirectory()) {
                zipDir(zos, path, source);
            } else {
                zipFile(zos, path, source);
            }
        }
    }

    private static void zipFile(ZipOutputStream zos, String path, File file) throws IOException {
        if (!file.canRead()) {
            return;
        }

        zos.putNextEntry(new ZipEntry(buildPath(path, file.getName())));

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4092];
        int byteCount = 0;

        while ((byteCount = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, byteCount);
        }

        fis.close();
        zos.closeEntry();
    }

    private void cleanFiles() {
        long x = plugin.getConfigManager().BackupAutoCleanUpDays() * 60L * 1000L * 24L * 60L;
        long time = System.currentTimeMillis();
        for (File file : BackupDir.listFiles()) {
            long diff = time - file.lastModified();
            if (diff > x) {
                file.delete();
            }
        }
    }

    public void backup() throws IOException {
        if (!BackupDir.isDirectory())
            try {
                BackupDir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        if (plugin.getConfigManager().BackupAutoCleanUpUse())
            cleanFiles();
        // Generate the proper date for the backup filename
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        File fileZip = new File(BackupDir, dateFormat.format(date) + ".zip");

        // Create the Source List, and add directories/etc to the file.
        List<File> sources = new ArrayList<File>();

        File saveFolder = new File(plugin.getDataLocation(), "Save");
        File worldFolder = new File(saveFolder, "Worlds");

        if (!saveFolder.isDirectory()) {
            return;
        }

        File saveFile;
        if (plugin.getConfigManager().BackupWorldFiles())
            for (World world : plugin.getServ().getWorlds()) {
                saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
                if (saveFile.isFile()) {
                    sources.add(saveFile);
                }
            }

        if (plugin.getConfigManager().BackupforsaleFile())
            sources.add(new File(saveFolder, "forsale.yml"));
        if (plugin.getConfigManager().BackupleasesFile())
            sources.add(new File(saveFolder, "leases.yml"));
        if (plugin.getConfigManager().BackuppermlistsFile())
            sources.add(new File(saveFolder, "permlists.yml"));
        if (plugin.getConfigManager().BackuprentFile())
            sources.add(new File(saveFolder, "rent.yml"));

        if (plugin.getConfigManager().BackupflagsFile())
            sources.add(new File(plugin.getDataLocation(), "flags.yml"));
        if (plugin.getConfigManager().BackupgroupsFile())
            sources.add(new File(plugin.getDataLocation(), "groups.yml"));
        if (plugin.getConfigManager().BackupconfigFile())
            sources.add(new File(plugin.getDataLocation(), "config.yml"));

        if (plugin.getConfigManager().UseZipBackup())
            packZip(fileZip, sources);
    }
}
