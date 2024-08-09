package com.bekvon.bukkit.residence.slimeFun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;

import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionModule;

public class SlimeFunResidenceModule implements ProtectionModule {

    private final Residence residence;

    public SlimeFunResidenceModule(Plugin plugin) {
        this.residence = (Residence) plugin;
    }

    @Override
    public Plugin getPlugin() {
        return this.residence;
    }

    @Override
    public void load() {
    }

    @Override
    public boolean hasPermission(OfflinePlayer op, Location loc, Interaction action) {

        if (op == null)
            return false;

        switch (action) {
        case INTERACT_BLOCK:
            ClaimedResidence res = residence.getResidenceManager().getByLoc(loc);
            if (res != null) {
                boolean allow = res.getPermissions().playerHas(ResidencePlayer.get(op.getUniqueId()), Flags.container, false);
                if (!allow)
                    residence.msg(op.getPlayer(), lm.Flag_Deny, Flags.container);
                return allow;
            }
            break;
        case BREAK_BLOCK:
            Player player = Bukkit.getPlayer(op.getUniqueId());
            if (player == null)
                return false;
            ResidencePermissions.setEventCallsSuspended(true);
            boolean result = ResidenceBlockListener.canBreakBlock(player, loc, true);
            ResidencePermissions.setEventCallsSuspended(false);
            return result;
        default:
            break;
        }

        return true;
    }

}
