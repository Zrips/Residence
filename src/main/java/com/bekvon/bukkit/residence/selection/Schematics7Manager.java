package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.io.File;

@SuppressWarnings("deprecation")
public class Schematics7Manager implements WESchematicManager {
    private Residence plugin;

    public Schematics7Manager(Residence residence) {
        this.plugin = residence;
    }

    @Override
    public boolean save(ClaimedResidence res) {
//	if (plugin.getWorldEdit() == null)
//	    return false;
//	if (res == null)
//	    return false;
//
//	CuboidArea area = res.getAreaArray()[0];
//
//	Vector bvmin = new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
//	Vector bvmax = new Vector(area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
//	Vector origin = new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
//
//	World bworld = Bukkit.getWorld(res.getWorld());
//
//	if (bworld == null)
//	    return false;
//
//	EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(bworld), -1);
//	editSession.enableQueue();
//	
//	Clipboard clipboard = new Clipboard(bvmax.subtract(bvmin).add(new Vector(1, 1, 1)), origin);
//	clipboard.setOrigin(origin);
//	clipboard.copy(editSession);
//
//	File dir = new File(plugin.getDataLocation(), "Schematics");
//	if (!dir.exists())
//	    try {
//		dir.mkdir();
//	    } catch (SecurityException se) {
//	    }
//	dir = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld());
//	if (!dir.exists())
//	    try {
//		dir.mkdir();
//	    } catch (SecurityException se) {
//	    }
//
//	File file = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");
//	try {
//	    com.sk89q.worldedit.schematic.SchematicFormat.MCEDIT.save(clipboard, file);
//	} catch (Exception e) {
//	    return false;
//	}
//
//	editSession.flushQueue();
        return true;
    }

    @Override
    public boolean load(ClaimedResidence res) {
//	if (plugin.getWorldEdit() == null)
//	    return false;
//
//	if (res == null)
//	    return false;
//
//	World bworld = Bukkit.getWorld(res.getWorld());
//
//	if (bworld == null)
//	    return false;
//
//	EditSession es = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(bworld), -1);
//	File file = new File(plugin.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");
//
//	if (!file.exists())
//	    return false;
//
//	com.sk89q.worldedit.CuboidClipboard cc = null;
//	try {
//	    cc = CuboidClipboard.loadSchematic(file);
//	} catch (Exception e1) {
//	    e1.printStackTrace();
//	    return false;
//	}
//
//	if (cc == null)
//	    return false;
//	Vector or = cc.getOrigin();
//	ClaimedResidence r1 = plugin.getResidenceManager().getByLoc(new Location(bworld, or.getBlockX(), or.getBlockY(), or.getBlockZ()));
//	ClaimedResidence r2 = plugin.getResidenceManager().getByLoc(new Location(bworld, or.getBlockX() + cc.getWidth() - 1, or.getBlockY() + cc.getHeight() - 1, or
//	    .getBlockZ() + cc.getLength() - 1));
//	if (r1 == null || r2 == null)
//	    return false;
//
//	if (!r1.getName().equalsIgnoreCase(r2.getName()))
//	    return false;
//
////	int totalBlocks = cc.getHeight() * cc.getLength() * cc.getWidth();
//
//	try {
//	    cc.paste(es, cc.getOrigin(), false);
//	} catch (MaxChangedBlocksException e) {
//	    e.printStackTrace();
//	    return false;
//	}
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
