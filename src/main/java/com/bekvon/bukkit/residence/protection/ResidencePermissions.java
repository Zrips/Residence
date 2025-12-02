package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.commands.padd;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.Flags.FlagMode;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagCheckEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagEvent.FlagType;
import com.bekvon.bukkit.residence.event.ResidenceOwnerChangeEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;

import net.Zrips.CMILib.Locale.LC;

public class ResidencePermissions extends FlagPermissions {

    private UUID ownerUUID;
    private String ownerLastKnownName;
    private String world;
    private ClaimedResidence residence;

    private static boolean suspendEventCalls = false;

    private ResidencePermissions(ClaimedResidence res) {
        super();
        residence = res;
    }

    public ResidencePermissions(ClaimedResidence res, String creator, UUID creatorUUID, String inworld) {
        this(res);
        if (creatorUUID == null)
            ownerUUID = ResidencePlayer.getUUID(creator);
        else
            ownerUUID = creatorUUID;
        if (ownerUUID == null)
            ownerUUID = PlayerManager.createTempUUID(creator);
        this.ownerLastKnownName = creator;
        world = inworld;
    }

    @Deprecated
    public ResidencePermissions(ClaimedResidence res, String creator, String inworld) {
        this(res);
        ownerUUID = ResidencePlayer.getUUID(creator);
        if (ownerUUID == null)
            ownerUUID = PlayerManager.createTempUUID(creator);
        this.ownerLastKnownName = creator;
        world = inworld;
    }

    public boolean playerHas(CommandSender sender, Flags flag, boolean def) {
        if (sender instanceof Player)
            return playerHas((Player) sender, flag, def);
        return true;
    }

    @Override
    public boolean playerHas(Player player, Flags flag, boolean def) {
        return playerHas(player, world, flag, def);
    }

    @Deprecated
    public boolean playerHas(Player player, String flag, boolean def) {
        return this.playerHas(player.getName(), world, flag, def);
    }

    public boolean playerHas(String player, Flags flag, boolean def) {
        return playerHas(player, flag.toString(), def);
    }

    @Deprecated
    public boolean playerHas(String player, String flag, boolean def) {
        return this.playerHas(player, world, flag, def);
    }

    @Override
    public boolean playerHas(Player player, Flags flag, FlagCombo f) {
        return playerHas(player.getUniqueId(), flag, f);
    }

    public boolean playerHas(ResidencePlayer player, Flags flag, FlagCombo f) {
        return playerHas(player.getUniqueId(), flag, f);
    }

    public boolean playerHas(UUID uuid, Flags flag, FlagCombo f) {
        switch (f) {
        case FalseOrNone:
            return !this.playerHas(uuid, world, flag, false);
        case OnlyFalse:
            return !this.playerHas(uuid, world, flag, true);
        case OnlyTrue:
            return this.playerHas(uuid, world, flag, false);
        case TrueOrNone:
            return this.playerHas(uuid, world, flag, true);
        default:
            return false;
        }
    }

    public boolean playerHas(UUID uuid, String flag, FlagCombo f) {
        switch (f) {
        case FalseOrNone:
            return !this.playerHas(uuid, world, flag, false);
        case OnlyFalse:
            return !this.playerHas(uuid, world, flag, true);
        case OnlyTrue:
            return this.playerHas(uuid, world, flag, false);
        case TrueOrNone:
            return this.playerHas(uuid, world, flag, true);
        default:
            return false;
        }
    }

    @Override
    public boolean playerHas(ResidencePlayer player, Flags flag, boolean def) {
        if (player == null)
            return false;
        return playerHas(player.getUniqueId(), this.world, flag, def);
    }

    @Override
    public boolean playerHas(UUID uuid, String world, Flags flag, boolean def) {
        if (uuid == null)
            return false;

        world = world == null ? this.world : world;

//        if (!isEventCallsSuspended() && Residence.getInstance().isFullyLoaded()) {
//            ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag.toString(), FlagType.PLAYER, ResidencePlayer.getName(uuid), def);
//            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
//            if (fc.isOverriden())
//                return fc.getOverrideValue();
//        }
        return super.playerHas(uuid, world, flag, def);
    }

    @Override
    public boolean playerHas(UUID uuid, String world, String flag, boolean def) {
        if (uuid == null)
            return false;

        world = world == null ? this.world : world;

//        if (!isEventCallsSuspended() && Residence.getInstance().isFullyLoaded()) {
//            ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag.toString(), FlagType.PLAYER, ResidencePlayer.getName(uuid), def);
//            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
//            if (fc.isOverriden())
//                return fc.getOverrideValue();
//        }
        return super.playerHas(uuid, world, flag, def);
    }

    @Deprecated
    public boolean playerHas(String player, String flag, FlagCombo f) {
        return playerHas(ResidencePlayer.getUUID(player), flag, f);
    }

    @Override
    public boolean playerHas(Player player, String world, Flags flag, boolean def) {
        return playerHas(player.getUniqueId(), world, flag, def);
    }

    @Override
    @Deprecated
    public boolean playerHas(String player, String world, String flag, boolean def) {
        return playerHas(ResidencePlayer.getUUID(player), world, flag, def);
    }

    @Override
    public boolean groupHas(String group, String flag, boolean def) {
//        if (!isEventCallsSuspended() && Residence.getInstance().isFullyLoaded()) {
//            ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag, FlagType.GROUP, group, def);
//            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
//            if (fc.isOverriden())
//                return fc.getOverrideValue();
//        }
        return super.groupHas(group, flag, def);
    }

    @Override
    public boolean has(Flags flag, FlagCombo f) {
        return has(flag, f, true);
    }

    public boolean has(Flags flag, FlagCombo f, boolean checkParent) {
        switch (f) {
        case FalseOrNone:
            return !has(flag, false, checkParent);
        case OnlyFalse:
            return !has(flag, true, checkParent);
        case OnlyTrue:
            return has(flag, false, checkParent);
        case TrueOrNone:
            return has(flag, true, checkParent);
        default:
            return false;
        }
    }

    @Deprecated
    public boolean has(String flag, FlagCombo f) {
        return has(flag, f, true);
    }

    @Deprecated
    public boolean has(String flag, FlagCombo f, boolean checkParent) {
        switch (f) {
        case FalseOrNone:
            return !has(flag, false, checkParent);
        case OnlyFalse:
            return !has(flag, true, checkParent);
        case OnlyTrue:
            return has(flag, false, checkParent);
        case TrueOrNone:
            return has(flag, true, checkParent);
        default:
            return false;
        }
    }

    @Override
    public boolean has(Flags flag, boolean def) {
        return has(flag.toString(), def, true);
    }

    @Override
    public boolean has(String flag, boolean def) {
        return has(flag, def, true);
    }

    @Override
    public boolean has(String flag, boolean def, boolean checkParent) {
//        if (!isEventCallsSuspended() && Residence.getInstance().isFullyLoaded()) {
//            ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag, FlagType.RESIDENCE, null, def);
//            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
//            if (fc.isOverriden())
//                return fc.getOverrideValue();
//        }
        return super.has(flag, def, checkParent);
    }

    @Deprecated
    public boolean hasApplicableFlag(String player, String flag) {
        return hasApplicableFlag(ResidencePlayer.getUUID(player), flag);
    }

    public boolean hasApplicableFlag(UUID uuid, String flag) {
        return super.inheritanceIsPlayerSet(uuid, flag) ||
                super.inheritanceIsGroupSet(ResidencePlayer.get(uuid).getGroup(world).getGroupName(), flag) ||
                super.inheritanceIsSet(flag);
    }

    public void applyTemplate(Player player, FlagPermissions list, boolean resadmin) {
        if (player != null) {
            if (!resadmin && !player.getUniqueId().toString().equals(ownerUUID.toString())) {
                lm.General_NoPermission.sendMessage(player);
                return;
            }
        } else {
            resadmin = true;
        }
        ResidencePlayer rPlayer = ResidencePlayer.get(this.getOwner());
        PermissionGroup group = rPlayer.getGroup(world);

        for (Entry<String, Boolean> flag : list.cuboidFlags.entrySet()) {
            if (group.hasFlagAccess(flag.getKey()) || resadmin) {
                this.cuboidFlags.put(flag.getKey(), flag.getValue());
            } else {
                lm.Flag_SetDeny.sendMessage(player, flag.getKey());
            }
        }

        for (Entry<UUID, Map<String, Boolean>> plists : list.getPlayerFlags().entrySet()) {
            Map<String, Boolean> map = this.getPlayerFlags(plists.getKey(), true);
            for (Entry<String, Boolean> flag : plists.getValue().entrySet()) {
                if (group.hasFlagAccess(flag.getKey()) || resadmin) {
                    map.put(flag.getKey(), flag.getValue());
                } else {
                    lm.Flag_SetDeny.sendMessage(player, flag.getKey());
                }
            }
        }

        for (Entry<String, Map<String, Boolean>> plists : list.getPlayerFlagsByName().entrySet()) {
            Map<String, Boolean> map = this.getPlayerFlags(plists.getKey(), true);
            for (Entry<String, Boolean> flag : plists.getValue().entrySet()) {
                if (group.hasFlagAccess(flag.getKey()) || resadmin) {
                    map.put(flag.getKey(), flag.getValue());
                } else {
                    lm.Flag_SetDeny.sendMessage(player, flag.getKey());
                }
            }
        }

        for (Entry<String, Map<String, Boolean>> glists : list.groupFlags.entrySet()) {
            for (Entry<String, Boolean> flag : glists.getValue().entrySet()) {
                if (group.hasFlagAccess(flag.getKey()) || resadmin) {
                    if (!this.groupFlags.containsKey(glists.getKey()))
                        this.groupFlags.put(glists.getKey(), Collections.synchronizedMap(new HashMap<String, Boolean>()));
                    this.groupFlags.get(glists.getKey()).put(flag.getKey(), flag.getValue());
                } else {
                    lm.Flag_SetDeny.sendMessage(player, flag.getKey());
                }
            }
        }

        lm.Residence_PermissionsApply.sendMessage(player);
    }

    public boolean hasResidencePermission(CommandSender sender, boolean requireOwner) {
        if (!(sender instanceof Player))
            return true;

        ClaimedResidence par = this.residence.getParent();
        Player player = (Player) sender;

        if (par != null && par.getPermissions().playerHas(player, Flags.admin, FlagCombo.OnlyTrue))
            return true;

        if (Residence.getInstance().getConfigManager().enabledRentSystem()) {
            String resname = residence.getName();
            if (Residence.getInstance().getRentManager().isRented(resname)) {
                if (requireOwner) {
                    return false;
                }
                String renter = Residence.getInstance().getRentManager().getRentingPlayer(resname);
                if (sender.getName().equals(renter)) {
                    return true;
                }
                return (playerHas(player, Flags.admin, FlagCombo.OnlyTrue));
            }
        }

        if (requireOwner) {
            return (this.getOwner().equals(sender.getName()));
        }

        return (playerHas(player, Flags.admin, FlagCombo.OnlyTrue) || this.getOwner().equals(sender.getName()));
    }

    private boolean checkCanSetFlag(CommandSender sender, String flag, FlagState state, boolean globalflag, boolean resadmin) {
        Flags f = Flags.getFlag(flag);
        if (f != null)
            flag = f.toString();
        if (!checkValidFlag(flag, globalflag)) {
            if (f != null)
                lm.Invalid_FlagType_Fail.sendMessage(sender, f.getFlagMode() == FlagMode.Residence ? Residence.getInstance().getLM().getMessage(lm.Invalid_FlagType_Residence)
                        : Residence
                                .getInstance().getLM().getMessage(lm.Invalid_FlagType_Player));
            else
                lm.Invalid_Flag.sendMessage(sender);
            return false;
        }
        if (state == FlagState.INVALID) {
            lm.Invalid_FlagState.sendMessage(sender);
            return false;
        }
        if (!resadmin) {
            if (!this.hasResidencePermission(sender, false)) {
                lm.General_NoPermission.sendMessage(sender);
                return false;
            }
            if (!hasFlagAccess(this.getOwnerUUID(), flag) && !ResPerm.flag_$1.hasPermission(sender, 10000L, flag.toLowerCase())) {
                lm.Flag_SetFailed.sendMessage(sender, flag);
                return false;
            }
        }
        return true;
    }

    @Deprecated
    private boolean hasFlagAccess(String player, String flag) {
        return hasFlagAccess(ResidencePlayer.getUUID(player), flag);
    }

    private boolean hasFlagAccess(UUID uuid, String flag) {
        ResidencePlayer rPlayer = ResidencePlayer.get(uuid);
        PermissionGroup group = rPlayer.getGroup(world);
        return group.hasFlagAccess(flag);
    }

    @Deprecated
    public boolean setPlayerFlag(CommandSender sender, String targetPlayer, String flag, String flagstate, boolean resadmin, boolean show) {
        return this.setPlayerFlag(sender, ResidencePlayer.getUUID(targetPlayer), flag, flagstate, resadmin, show);
    }

    public boolean setPlayerFlag(CommandSender sender, UUID targetPlayerUUID, String flag, String flagstate, boolean resadmin, boolean show) {

        if (targetPlayerUUID == null) {
            sender.sendMessage("No player by this name");
            return false;
        }

        if (this.residence.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_noFlagChange.sendMessage(sender);
            return false;
        }

//        String targetPlayer = ResidencePlayer.getName(uuid);

        Flags f = Flags.getFlag(flag);
        if (f != null)
            flag = f.toString();
        if (validFlagGroups.containsKey(flag)) {
            return this.setFlagGroupOnPlayer(sender, targetPlayerUUID, flag, flagstate, resadmin);
        }

        FlagState state = FlagPermissions.stringToFlagState(flagstate);
        if (checkCanSetFlag(sender, flag, state, false, resadmin)) {

            if (!isEventCallsSuspended()) {
                ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, sender instanceof Player ? (Player) sender : null, flag, ResidenceFlagChangeEvent.FlagType.PLAYER, state,
                        ResidencePlayer.getName(targetPlayerUUID));
                Residence.getInstance().getServ().getPluginManager().callEvent(fc);
                if (fc.isCancelled())
                    return false;
            }

            if (super.setPlayerFlag(targetPlayerUUID, flag, state)) {
                if (show) {
                    switch (state) {
                    case NEITHER:
                        lm.Flag_PRemoved.sendMessage(sender, flag, residence.getName(), ResidencePlayer.getName(targetPlayerUUID));
                        break;
                    case FALSE:
                    case TRUE:
                        lm.Flag_PSet.sendMessage(sender, flag, residence.getName(), flagstate, ResidencePlayer.getName(targetPlayerUUID));
                        break;
                    default:
                        break;
                    }
                }

                if ((f == null || f.isInGroup(padd.groupedFlag))) {
                    boolean trusted = this.residence.isTrusted(targetPlayerUUID);
                    if (!state.equals(FlagState.TRUE) && !trusted) {
                        ResidencePlayer rplayer = ResidencePlayer.get(targetPlayerUUID);
                        if (rplayer != null) {
                            rplayer.removeTrustedResidence(this.residence);
                        }
                    } else if (state.equals(FlagState.TRUE) && trusted) {
                        ResidencePlayer rplayer = ResidencePlayer.get(targetPlayerUUID);
                        if (rplayer != null) {
                            rplayer.addTrustedResidence(this.residence);
                        }
                    }
                }

                return true;
            }
        }
        return false;
    }

    public boolean setGroupFlag(Player player, String group, String flag, String flagstate, boolean resadmin) {
        Flags f = Flags.getFlag(flag);
        FlagState state = FlagPermissions.stringToFlagState(flagstate);
        return setGroupFlag(player, group, f, state, resadmin);
    }

    public boolean setGroupFlag(Player player, String group, Flags f, FlagState flagstate, boolean resadmin) {

        String flag = null;
        if (f != null)
            flag = f.toString();

        if (flag == null) {
            lm.Invalid_Flag.sendMessage(player);
            return false;
        }

        if (flagstate.equals(FlagState.INVALID)) {
            lm.Invalid_FlagState.sendMessage(player);
            return false;
        }

        if (this.residence.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_noFlagChange.sendMessage(player);
            return false;
        }

        group = group.toLowerCase();
        if (validFlagGroups.containsKey(flag)) {
            return this.setFlagGroupOnGroup(player, flag, group, flagstate.toString(), resadmin);
        }

        if (checkCanSetFlag(player, flag, flagstate, false, resadmin)) {
            if (Residence.getInstance().getPermissionManager().hasGroup(group)) {
                if (!isEventCallsSuspended()) {
                    ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.GROUP, flagstate, group);
                    Residence.getInstance().getServ().getPluginManager().callEvent(fc);
                    if (fc.isCancelled())
                        return false;
                }
                if (super.setGroupFlag(group, flag, flagstate)) {
                    lm.Flag_Set.sendMessage(player, flag, residence.getName(), flagstate);
                    return true;
                }
            } else {
                lm.Invalid_Group.sendMessage(player);
                return false;
            }
        }
        return false;
    }

    @Deprecated
    public boolean setFlag(CommandSender sender, String flag, String flagstate, boolean resadmin) {
        return setFlag(sender, flag, FlagPermissions.stringToFlagState(flagstate), resadmin);
    }

    public boolean setFlag(CommandSender sender, String flag, FlagState state, boolean resadmin) {
        return setFlag(sender, flag, state, resadmin, true);
    }

    public boolean setFlag(CommandSender sender, String flag, FlagState state, boolean resadmin, boolean inform) {

        Flags f = Flags.getFlag(flag);
        if (f != null)
            flag = f.toString();

        if (this.residence.getRaid().isRaidInitialized() && !resadmin) {
            lm.Raid_noFlagChange.sendMessage(sender);
            return false;
        }

        if (validFlagGroups.containsKey(flag))
            return this.setFlagGroup(sender, flag, state, resadmin);

        if (Residence.getInstance().getConfigManager().isPvPFlagPrevent()) {
            for (String oneFlag : Residence.getInstance().getConfigManager().getProtectedFlagsList()) {
                if (!flag.equalsIgnoreCase(oneFlag))
                    continue;

                ArrayList<Player> players = this.residence.getPlayersInResidence();
                if (!resadmin && (players.size() > 1 || players.size() == 1 && !players.get(0).getName().equals(this.getOwner()))) {
                    int size = 0;
                    for (Player one : players) {
                        if (!one.getName().equals(this.getOwner()))
                            size++;
                    }
                    if (inform)
                        lm.Flag_ChangeDeny.sendMessage(sender, flag, size);
                    return false;
                }
            }
        }

        if (checkCanSetFlag(sender, flag, state, true, resadmin)) {
            if (!isEventCallsSuspended()) {
                ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(this.residence, sender instanceof Player ? (Player) sender : null, flag,
                        ResidenceFlagChangeEvent.FlagType.RESIDENCE, state, null);
                Residence.getInstance().getServ().getPluginManager().callEvent(fc);
                if (fc.isCancelled())
                    return false;
            }
            if (super.setFlag(flag, state)) {
                if (inform) {
                    switch (state) {
                    case NEITHER:
                        lm.Flag_Removed.sendMessage(sender, flag, this.residence.getName(), state.getName());
                        break;
                    case FALSE:
                    case TRUE:
                        lm.Flag_Set.sendMessage(sender, flag, this.residence.getName(), state.getName());
                        break;
                    default:
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean removeAllPlayerFlags(CommandSender sender, String targetPlayer, boolean resadmin) {
        return this.removeAllPlayerFlags(sender, ResidencePlayer.getUUID(targetPlayer), resadmin);
    }

    public boolean removeAllPlayerFlags(CommandSender sender, UUID targetPlayerUUID, boolean resadmin) {
        if (this.hasResidencePermission(sender, false) || resadmin) {

            if (!isEventCallsSuspended()) {
                ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(this.residence, sender instanceof Player ? (Player) sender : null, "ALL",
                        ResidenceFlagChangeEvent.FlagType.RESIDENCE, FlagState.NEITHER, null);
                Residence.getInstance().getServ().getPluginManager().callEvent(fc);
                if (fc.isCancelled()) {
                    return false;
                }
            }
            super.removeAllPlayerFlags(targetPlayerUUID);
            lm.Flag_RemovedAll.sendMessage(sender, ResidencePlayer.getName(targetPlayerUUID), this.residence.getName());
            return true;
        }
        return false;
    }

    public boolean removeAllGroupFlags(Player player, String group, boolean resadmin) {
        if (this.hasResidencePermission(player, false) || resadmin) {
            if (!isEventCallsSuspended()) {
                ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, "ALL", ResidenceFlagChangeEvent.FlagType.GROUP, FlagState.NEITHER, null);
                Residence.getInstance().getServ().getPluginManager().callEvent(fc);
                if (fc.isCancelled()) {
                    return false;
                }
            }
            super.removeAllGroupFlags(group);
            lm.Flag_RemovedGroup.sendMessage(player, group, this.residence.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean setFlag(String flag, FlagState state) {
        if (!isEventCallsSuspended()) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null, flag, ResidenceFlagChangeEvent.FlagType.RESIDENCE, state, null);
            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
            if (fc.isCancelled())
                return false;
        }
        return super.setFlag(flag, state);
    }

    @Override
    public boolean setGroupFlag(String group, String flag, FlagState state) {
        if (!isEventCallsSuspended()) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null, flag, ResidenceFlagChangeEvent.FlagType.GROUP, state, group);
            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
            if (fc.isCancelled())
                return false;
        }
        return super.setGroupFlag(group, flag, state);
    }

    @Override
    @Deprecated
    public boolean setPlayerFlag(String player, String flag, FlagState state) {
        return setPlayerFlag(ResidencePlayer.getUUID(player), flag, state);
    }

    @Override
    public boolean setPlayerFlag(UUID uuid, String flag, FlagState state) {
        if (!isEventCallsSuspended()) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null, flag, ResidenceFlagChangeEvent.FlagType.PLAYER, state, ResidencePlayer.getName(uuid));
            Residence.getInstance().getServ().getPluginManager().callEvent(fc);
            if (fc.isCancelled())
                return false;
        }
        return super.setPlayerFlag(uuid, flag, state);
    }

    public void applyDefaultFlags(Player player, boolean resadmin) {
        if (this.hasResidencePermission(player, true) || resadmin) {
            this.applyDefaultFlags();
            lm.Flag_Default.sendMessage(player);
        } else
            lm.General_NoPermission.sendMessage(player);
    }

    public void applyDefaultFlags() {
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(this.getOwner());
        PermissionGroup group = rPlayer.getGroup(world);
        Set<Entry<String, Boolean>> dflags = group.getDefaultResidenceFlags();
//	Set<Entry<String, Boolean>> dcflags = group.getDefaultCreatorFlags();
        Set<Entry<String, Map<String, Boolean>>> dgflags = group.getDefaultGroupFlags();
        this.applyGlobalDefaults();

        for (Entry<String, Boolean> next : dflags) {
            if (this.checkValidFlag(next.getKey(), true)) {
                this.setFlag(next.getKey(), next.getValue() ? FlagState.TRUE : FlagState.FALSE);
            }
        }

//	for (Entry<String, Boolean> next : dcflags) {
//	    if (this.checkValidFlag(next.getKey(), false)) {
//		if (next.getValue()) {
//		    this.setPlayerFlag(this.getOwner(), next.getKey(), FlagState.TRUE);
//		} else {
//		    this.setPlayerFlag(this.getOwner(), next.getKey(), FlagState.FALSE);
//		}
//	    }
//	}
        for (Entry<String, Map<String, Boolean>> entry : dgflags) {
            Map<String, Boolean> value = entry.getValue();
            for (Entry<String, Boolean> flag : value.entrySet()) {
                this.setGroupFlag(entry.getKey(), flag.getKey(), flag.getValue() ? FlagState.TRUE : FlagState.FALSE);
            }
        }
    }

    public void applyDefaultRentedFlags() {
        if (!this.residence.isRented())
            return;
        FlagPermissions dflags = Residence.getInstance().getConfigManager().getGlobalRentedDefaultFlags();
        Map<String, Boolean> dgflags = dflags.getFlags();
        if (this.residence.rentedland == null || this.residence.rentedland.getRenterName() == null)
            return;
        UUID player = this.residence.rentedland.getUniqueId();
        this.removeAllPlayerFlags(player);
        for (Entry<String, Boolean> entry : dgflags.entrySet()) {
            this.setPlayerFlag(player, entry.getKey(), entry.getValue() ? FlagState.TRUE : FlagState.FALSE);
        }
    }

    public boolean setOwner(Player player, boolean resetFlags) {
        return setOwner(player.getUniqueId(), resetFlags);
    }

    public boolean setOwner(UUID uuid, boolean resetFlags) {

        String name = ResidencePlayer.getName(uuid);

        ResidenceOwnerChangeEvent ownerchange = new ResidenceOwnerChangeEvent(residence, name, uuid);
        Residence.getInstance().getServ().getPluginManager().callEvent(ownerchange);

        // Dont change owner if event is canceled
        if (ownerchange.isCancelled())
            return false;

        Residence.getInstance().getPlayerManager().removeResFromPlayer(residence.getOwnerUUID(), residence);
        Residence.getInstance().getPlayerManager().addResidence(uuid, residence);

        ownerLastKnownName = name;
        ownerUUID = uuid;

        if (resetFlags)
            this.applyDefaultFlags();

        return true;
    }

    public void setOwner(String newOwner, boolean resetFlags) {

        ResidenceOwnerChangeEvent ownerchange = new ResidenceOwnerChangeEvent(residence, newOwner);
        Residence.getInstance().getServ().getPluginManager().callEvent(ownerchange);

        // Dont change owner if event is canceled
        if (ownerchange.isCancelled())
            return;

        Residence.getInstance().getPlayerManager().removeResFromPlayer(residence.getOwnerUUID(), residence);

        ownerLastKnownName = newOwner;
        ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(newOwner);
        if (rPlayer != null) {
            this.ownerUUID = rPlayer.getUniqueId();
            if (rPlayer.getName() != null)
                ownerLastKnownName = rPlayer.getName();
        }

        if (ownerLastKnownName.equalsIgnoreCase("Server Land") || ownerLastKnownName.equalsIgnoreCase(Residence.getInstance().getServerLandName())) {
            ownerUUID = Residence.getInstance().getServerUUID();// the UUID for server owned land
        } else {
            UUID playerUUID = ResidencePlayer.getUUID(ownerLastKnownName);
            if (playerUUID != null)
                ownerUUID = playerUUID;
            else
                ownerUUID = PlayerManager.createTempUUID(ownerLastKnownName);// the fake UUID used when unable to find the real one, will be updated with
                                                                             // players real UUID when its possible to find it
        }

        Residence.getInstance().getPlayerManager().addResidence(ownerUUID, residence);
        if (resetFlags)
            this.applyDefaultFlags();
    }

    public String getOwner() {

        if (ownerUUID != null && ownerUUID.equals(Residence.getInstance().getServerUUID())) // check for server land
            return Residence.getInstance().getServerLandName();
        String name = ResidencePlayer.getName(ownerUUID);// try to find the owner's name
        if (name == null)
            return ownerLastKnownName == null ? "Unknown" : ownerLastKnownName;// return last known if we cannot find it
        ownerLastKnownName = name;// update last known if we did find it
        return name;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        if (ownerUUID != null)
            this.ownerUUID = ownerUUID;
    }

    @Deprecated
    public String getWorld() {
        return world;
    }

    public String getWorldName() {
        return world;
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(world);
    }

    @Override
    public Map<String, Object> save(String world) {
        Map<String, Object> root = super.save(this.world);
        if (!PlayerManager.isTempUUID(ownerUUID))
            root.put("OwnerUUID", ownerUUID.toString());
        root.put("OwnerLastKnownName", ownerLastKnownName);
        return root;
    }

    public static ResidencePermissions load(String worldName, ClaimedResidence res, Map<String, Object> root) throws Exception {

        ResidencePermissions newperms = new ResidencePermissions(res);

        if (root.containsKey("OwnerUUID") || root.containsKey("OwnerLastKnownName")) {
            newperms.ownerLastKnownName = (String) root.get("OwnerLastKnownName");// otherwise load last known name from file

            if (!root.containsKey("OwnerUUID"))
                newperms.ownerUUID = PlayerManager.createTempUUID(newperms.ownerLastKnownName);// get empty owner UUID
            else
                newperms.ownerUUID = UUID.fromString((String) root.get("OwnerUUID"));// get owner UUID

            if (newperms.ownerLastKnownName == null)
                newperms.ownerLastKnownName = ResidencePlayer.getName(newperms.ownerUUID);

            if (newperms.ownerLastKnownName != null) {

                if (newperms.ownerLastKnownName.equalsIgnoreCase("Server land") || newperms.ownerLastKnownName.equalsIgnoreCase(Residence.getInstance().getServerLandName())) {
                    newperms.ownerUUID = Residence.getInstance().getServerUUID();// UUID for server land
                    newperms.ownerLastKnownName = Residence.getInstance().getServerLandName();
                } else if (PlayerManager.isTempUUID(newperms.ownerUUID)) // check for fake UUID
                {
                    UUID realUUID = ResidencePlayer.getUUID(newperms.ownerLastKnownName);// try to find the real UUID of the player if possible now
                    if (realUUID != null)
                        newperms.ownerUUID = realUUID;
                }
            }
        } else {
            newperms.ownerUUID = Residence.getInstance().getServerUUID();// cant determine owner name or UUID... setting zero UUID which is server land
            newperms.ownerLastKnownName = Residence.getInstance().getServerLandName();
        }

        Residence.getInstance().getPlayerManager().addPlayer(newperms.ownerLastKnownName, newperms.ownerUUID);

        newperms.world = worldName;

        FlagPermissions.load(root, newperms);

        if (newperms.getOwner() == null || newperms.world == null || newperms.playerFlags == null || newperms.groupFlags == null || newperms.cuboidFlags == null)
            throw new Exception("Invalid Residence Permissions...");

        return newperms;
    }

    public void applyGlobalDefaults() {
        this.clearFlags();
        FlagPermissions gRD = Residence.getInstance().getConfigManager().getGlobalResidenceDefaultFlags();
        FlagPermissions gCD = Residence.getInstance().getConfigManager().getGlobalCreatorDefaultFlags();
        Map<String, FlagPermissions> gGD = Residence.getInstance().getConfigManager().getGlobalGroupDefaultFlags();
        for (Entry<String, Boolean> entry : gRD.cuboidFlags.entrySet()) {
            this.setFlag(entry.getKey(), entry.getValue() ? FlagState.TRUE : FlagState.FALSE);
        }
        for (Entry<String, Boolean> entry : gCD.cuboidFlags.entrySet()) {
            if (entry.getValue())
                this.setPlayerFlag(this.getOwnerUUID(), entry.getKey(), FlagState.TRUE);
            else
                this.setPlayerFlag(this.getOwnerUUID(), entry.getKey(), FlagState.FALSE);
        }
        for (Entry<String, FlagPermissions> entry : gGD.entrySet()) {
            for (Entry<String, Boolean> flag : entry.getValue().cuboidFlags.entrySet()) {
                if (flag.getValue())
                    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
                else
                    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
            }
        }
    }

    @Deprecated
    public boolean setFlagGroup(CommandSender sender, String flaggroup, String state, boolean resadmin) {
        return setFlagGroup(sender, flaggroup, FlagPermissions.stringToFlagState(state), resadmin);
    }

    public boolean setFlagGroup(CommandSender sender, String flaggroup, FlagState state, boolean resadmin) {
        if (FlagPermissions.validFlagGroups.containsKey(flaggroup)) {
            Set<String> flags = FlagPermissions.validFlagGroups.get(flaggroup);
            boolean changed = false;

            for (String flag : flags) {
                if (this.setFlag(sender, flag, state, resadmin)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean setFlagGroupOnGroup(Player player, String flaggroup, String group, String state, boolean resadmin) {
        if (FlagPermissions.validFlagGroups.containsKey(flaggroup)) {
            Set<String> flags = FlagPermissions.validFlagGroups.get(flaggroup);
            boolean changed = false;
            for (String flag : flags) {
                if (this.setGroupFlag(player, group, flag, state, resadmin)) {
                    changed = true;
                }
            }
            return changed;
        }
        return false;
    }

    public boolean setFlagGroupOnPlayer(CommandSender sender, String target, String flaggroup, String state, boolean resadmin) {
        return setFlagGroupOnPlayer(sender, ResidencePlayer.getUUID(target), flaggroup, state, resadmin);
    }

    public boolean setFlagGroupOnPlayer(CommandSender sender, UUID uuid, String flaggroup, String state, boolean resadmin) {
        if (!FlagPermissions.validFlagGroups.containsKey(flaggroup))
            return false;

        Set<String> flags = FlagPermissions.validFlagGroups.get(flaggroup);
        boolean changed = false;
        boolean changedAll = true;
        StringBuilder flagString = new StringBuilder();

        for (String flag : flags) {
            if (this.setPlayerFlag(sender, uuid, flag, state, resadmin, false)) {
                changed = true;
                if (!flagString.toString().isEmpty())
                    flagString.append(LC.info_ListSpliter.getLocale());
                flagString.append(flag);
            } else
                changedAll = false;
        }
        if (changedAll) {
            ResidencePlayer rplayer = ResidencePlayer.get(uuid);
            if (rplayer != null) {
                rplayer.addTrustedResidence(this.residence);
            }
        }
        if (!flagString.toString().isEmpty())
            lm.Flag_Set.sendMessage(sender, flagString, ResidencePlayer.getName(uuid), state);
        return changed;
    }

    public String getOwnerLastKnownName() {
        return ownerLastKnownName;
    }

    public void setOwnerLastKnownName(String ownerLastKnownName) {
        this.ownerLastKnownName = ownerLastKnownName;
    }

    public static boolean isEventCallsSuspended() {
        return suspendEventCalls;
    }

    public static void setEventCallsSuspended(boolean suspendEventCalls) {
        ResidencePermissions.suspendEventCalls = suspendEventCalls;
    }
}
