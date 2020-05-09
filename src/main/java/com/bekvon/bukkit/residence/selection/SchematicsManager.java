package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("deprecation")
public class SchematicsManager implements WESchematicManager {
    private Residence plugin;

    public SchematicsManager(Residence residence) {
        this.plugin = residence;
    }

    @Override
    public boolean save(ClaimedResidence res) {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
            return false;
        if (plugin.getWorldEdit() == null)
            return false;
        if (res == null)
            return false;

        CuboidArea area = res.getAreaArray()[0];

        Vector bvmin = new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
        Vector bvmax = new Vector(area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
        Vector origin = new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());

        World bworld = Bukkit.getWorld(res.getWorld());

        if (bworld == null)
            return false;
        try {
            EditSession editSession = EditSession.class.getConstructor(BukkitWorld.class, Integer.class).newInstance(new BukkitWorld(bworld), Integer.MAX_VALUE);
//	    EditSession editSession = new EditSession(new BukkitWorld(bworld), Integer.MAX_VALUE);
            editSession.enableQueue();
            com.sk89q.worldedit.CuboidClipboard clipboard = new com.sk89q.worldedit.CuboidClipboard(bvmax.subtract(bvmin).add(new Vector(1, 1, 1)), origin);
            clipboard.setOrigin(origin);
            clipboard.copy(editSession);

            File dir = new File(plugin.getDataLocation(), "Schematics");
            if (!dir.exists())
                try {
                    dir.mkdir();
                } catch (SecurityException se) {
                }
            dir = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld());
            if (!dir.exists())
                try {
                    dir.mkdir();
                } catch (SecurityException se) {
                }

            File file = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");
            try {
                com.sk89q.worldedit.schematic.SchematicFormat.MCEDIT.save(clipboard, file);
            } catch (Exception e) {

                if (plugin.getWorldGuardVersion() >= 7) {
                    editSession.flushSession();
                } else {
                    editSession.getClass().getMethod("flushQueue").invoke(editSession);
                }
                return false;
            }
            if (plugin.getWorldGuardVersion() >= 7) {
                editSession.flushSession();
            } else {
                editSession.getClass().getMethod("flushQueue").invoke(editSession);
            }

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }
//	
        return true;
    }

    @Override
    public boolean load(ClaimedResidence res) {

        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1))
            return false;
        if (plugin.getWorldEdit() == null)
            return false;

        if (res == null)
            return false;

        World bworld = Bukkit.getWorld(res.getWorld());

        if (bworld == null)
            return false;
        try {
            EditSession es = EditSession.class.getConstructor(BukkitWorld.class, Integer.class).newInstance(new BukkitWorld(bworld), Integer.MAX_VALUE);
//	    EditSession es = new EditSession(new BukkitWorld(bworld), Integer.MAX_VALUE);
            File file = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");

            if (!file.exists())
                return false;

            com.sk89q.worldedit.CuboidClipboard cc = null;
            try {
                cc = CuboidClipboard.loadSchematic(file);
            } catch (Exception e1) {
                e1.printStackTrace();
                return false;
            }

            if (cc == null)
                return false;
            Vector or = cc.getOrigin();
            ClaimedResidence r1 = plugin.getResidenceManager().getByLoc(new Location(bworld, or.getBlockX(), or.getBlockY(), or.getBlockZ()));
            ClaimedResidence r2 = plugin.getResidenceManager().getByLoc(new Location(bworld, or.getBlockX() + cc.getWidth() - 1, or.getBlockY() + cc.getHeight() - 1, or
                    .getBlockZ() + cc.getLength() - 1));
            if (r1 == null || r2 == null)
                return false;

            if (!r1.getName().equalsIgnoreCase(r2.getName()))
                return false;

//		int totalBlocks = cc.getHeight() * cc.getLength() * cc.getWidth();

            try {
                cc.paste(es, cc.getOrigin(), false);
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
                return false;
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
            e1.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean delete(ClaimedResidence res) {
        if (plugin.getWorldEdit() == null)
            return false;
        if (res == null)
            return false;

        File file = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");
        if (!file.exists())
            return false;

        return file.delete();
    }

    @Override
    public boolean rename(ClaimedResidence res, String newName) {
        if (plugin.getWorldEdit() == null)
            return false;
        if (res == null)
            return false;

        File oldFile = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");

        if (!oldFile.exists())
            return false;

        File newFile = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + newName + ".schematic");
        return oldFile.renameTo(newFile);
    }
}
