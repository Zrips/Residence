package com.bekvon.bukkit.residence.allNms;

import com.bekvon.bukkit.cmiLib.CMIMaterial;
import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

public class v1_13Events implements Listener {

    Residence plugin;

    public v1_13Events(Residence plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryFade(BlockFadeEvent event) {
        if (Version.isCurrentLower(Version.v1_13_R1))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.dryup.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;

        CMIMaterial mat = CMIMaterial.get(event.getBlock());
        if (!mat.equals(CMIMaterial.FARMLAND))
            return;

        FlagPermissions perms = plugin.getPermsByLoc(event.getNewState().getLocation());
        if (perms.has(Flags.dryup, FlagCombo.OnlyFalse)) {
            Block b = event.getBlock();
            try {
                BlockData data = b.getBlockData();
                Farmland farm = (Farmland) data;
                farm.setMoisture(7);
                b.setBlockData(farm);
            } catch (NoClassDefFoundError e) {
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {
        if (Version.isCurrentLower(Version.v1_13_R1))
            return;
        // Disabling listener if flag disabled globally
        if (!Flags.dryup.isGlobalyEnabled())
            return;
        // disabling event on world
        if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
            return;
        try {

            if (!event.getChangedType().toString().equalsIgnoreCase("FARMLAND"))
                return;

            FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
            if (perms.has(Flags.dryup, FlagCombo.OnlyFalse)) {
                Block b = event.getBlock();
                try {
                    BlockData data = b.getBlockData();
                    Farmland farm = (Farmland) data;
                    farm.setMoisture(7);
                    b.setBlockData(farm);
                } catch (NoClassDefFoundError e) {
                }
                event.setCancelled(true);
            }
        } catch (Exception | Error e) {

        }
    }
}
