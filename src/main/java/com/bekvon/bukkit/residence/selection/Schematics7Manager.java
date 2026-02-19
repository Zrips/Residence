package com.bekvon.bukkit.residence.selection;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import net.Zrips.CMILib.Messages.CMIMessages;

public class Schematics7Manager implements WESchematicManager {
    private Residence plugin;

    private boolean FAWE = false;

    public Schematics7Manager(Residence residence) {
        this.plugin = residence;
        FAWE = Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

    @Override
    public boolean save(ClaimedResidence residence) {
        if (plugin.getWorldEdit() == null || residence == null)
            return false;

        CuboidArea area = residence.getAreaArray()[0];
        BlockVector3 min = BlockVector3.at(
                area.getLowVector().getBlockX(),
                area.getLowVector().getBlockY(),
                area.getLowVector().getBlockZ());
        BlockVector3 max = BlockVector3.at(
                area.getHighVector().getBlockX(),
                area.getHighVector().getBlockY(),
                area.getHighVector().getBlockZ());
        BlockVector3 origin = min;

        org.bukkit.@Nullable World bukkitWorld = Bukkit.getWorld(residence.getWorldName());
        if (bukkitWorld == null)
            return false;

        World weWorld = BukkitAdapter.adapt(bukkitWorld);
        EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld);

        CuboidRegion region = new CuboidRegion(weWorld, min, max);
        ClipboardFormat format = ClipboardFormats.findByAlias("schem");
        if (format == null) {
            editSession.close();
            return false;
        }

        try {
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            clipboard.setOrigin(origin);

            ForwardExtentCopy copy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(true);
            Operations.complete(copy);

            File schematicsDir = new File(plugin.getDataLocation(), "Schematics" + File.separator + residence.getWorldName());
            if (!schematicsDir.exists() && !schematicsDir.mkdirs()) {
                editSession.close();
                return false;
            }

            String baseName = residence.getName();
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            File schematicFile = new File(schematicsDir, baseName + "_" + timestamp + ".schem");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(schematicFile);
                    ClipboardWriter writer = format.getWriter(fos)) {
                writer.write(clipboard);
            }

            // Keep only 3 newest versions
            File[] files = schematicsDir.listFiles((dir, name) -> name.startsWith(baseName + "_") && name.endsWith(".schem"));
            if (files != null && files.length > 3) {
                Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                for (int i = 3; i < files.length; i++) {
                    files[i].delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            editSession.close();
            return false;
        }
        editSession.close();
        return true;
    }

    @Override
    public boolean load(ClaimedResidence residence) {
        return load(residence, 0);
    }

    public boolean load(ClaimedResidence residence, int versionIndex) {
        if (plugin.getWorldEdit() == null || residence == null)
            return false;

        org.bukkit.World bukkitWorld = org.bukkit.Bukkit.getWorld(residence.getWorldName());
        if (bukkitWorld == null)
            return false;

        File schematicsDir = new File(plugin.getDataLocation(), "Schematics" + File.separator + residence.getWorldName());

        String baseName = residence.getName();
        File[] files = schematicsDir.listFiles((dir, name) -> name.startsWith(baseName + "_") && name.endsWith(".schem"));

        if (files == null || files.length == 0 || versionIndex < 1 || versionIndex > files.length)
            return false;

        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        File schematicFile = files[versionIndex - 1];

        World weWorld = BukkitAdapter.adapt(bukkitWorld);

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

        if (format == null)
            return false;

        try (java.io.FileInputStream fis = new java.io.FileInputStream(schematicFile);
                ClipboardReader reader = format.getReader(fis)) {

            Clipboard clipboard = reader.read();

            CuboidArea area = residence.getAreaArray()[0];
            BlockVector3 targetMin = BlockVector3.at(
                    area.getLowVector().getBlockX(),
                    area.getLowVector().getBlockY(),
                    area.getLowVector().getBlockZ());

            EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld);
            try {
                ClipboardHolder holder = new ClipboardHolder(clipboard);

                BlockVector3 pastePosition = isFAWE() ? targetMin.subtract(clipboard.getOrigin()) : clipboard.getRegion().getMinimumPoint();

                Operation operation = holder.createPaste(editSession)
                        .to(pastePosition)
                        .ignoreAirBlocks(false)
                        .copyEntities(true)
                        .build();

                Operations.complete(operation);

                long blockCount = clipboard.getRegion().getVolume();
                BlockVector3 origin = isFAWE() ? pastePosition : clipboard.getOrigin();
                CMIMessages.consoleMessage("[Residence] Pasted schematic '" + schematicFile.getName() + "' at " + origin + " with " + blockCount + " blocks");

            } finally {
                editSession.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean isFAWE() {
        return FAWE;
    }

    @Override
    public boolean delete(ClaimedResidence res) {
        if (plugin.getWorldEdit() == null)
            return false;
        if (res == null)
            return false;

        File schematicsDir = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorldName());
        if (!schematicsDir.exists() || !schematicsDir.isDirectory())
            return false;

        String baseName = res.getName();
        File[] files = schematicsDir.listFiles((dir, name) -> name.startsWith(baseName + "_") && name.endsWith(".schem"));

        boolean deletedAny = false;
        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    deletedAny = true;
                }
            }
        }
        return deletedAny;
    }

    @Override
    public boolean rename(ClaimedResidence res, String newName) {
        if (plugin.getWorldEdit() == null)
            return false;
        if (res == null)
            return false;

        File schematicsDir = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorldName());
        if (!schematicsDir.exists() || !schematicsDir.isDirectory())
            return false;

        String baseName = res.getName();
        File[] files = schematicsDir.listFiles((dir, name) -> name.startsWith(baseName + "_") && name.endsWith(".schem"));
        if (files == null || files.length == 0)
            return false;

        boolean renamedAny = false;
        for (File file : files) {
            String name = file.getName();
            String newFileName = newName + name.substring(baseName.length());
            File newFile = new File(schematicsDir, newFileName);
            if (file.renameTo(newFile)) {
                renamedAny = true;
            }
        }
        return renamedAny;
    }

    @Override
    public List<String> getList(ClaimedResidence res) {

        if (plugin.getWorldEdit() == null)
            return java.util.Collections.emptyList();
        if (res == null)
            return java.util.Collections.emptyList();

        File schematicsDir = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorldName());
        if (!schematicsDir.exists() || !schematicsDir.isDirectory())
            return java.util.Collections.emptyList();

        String baseName = res.getName();
        File[] files = schematicsDir.listFiles((dir, name) -> name.startsWith(baseName + "_") && name.endsWith(".schem"));

        if (files == null || files.length == 0)
            return java.util.Collections.emptyList();

        java.util.Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        List<String> versions = new java.util.ArrayList<>();
        for (File file : files) {
            String name = file.getName();
            String timestampPart = name.substring(baseName.length() + 1, name.length() - 6);

            // Convert to date format for better readability
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
                java.util.Date date = sdf.parse(timestampPart);
                timestampPart = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            } catch (Exception e) {
            }

            versions.add(timestampPart);
        }
        return versions;
    }
}
