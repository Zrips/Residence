package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.MinimizeFlags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;

import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.PageInfo;
import net.Zrips.CMILib.Items.CMIMC;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.Version.Version;

public class FlagPermissions {

    protected static ArrayList<String> validFlags = new ArrayList<>();
    protected static ArrayList<String> validPlayerFlags = new ArrayList<>();
    protected static ArrayList<String> validAreaFlags = new ArrayList<>();
    protected static HashMap<String, HashMap<String, FlagState>> validFlagGroups = new HashMap<>();

    private List<String> CMDWhiteList = new ArrayList<>();
    private List<String> CMDBlackList = new ArrayList<>();
    private boolean inherit = false;

    private static boolean ignoreGroupedFlagsAccess = false;

    final static Map<Material, Flags> matUseFlagList = new EnumMap<>(Material.class);

    protected Map<UUID, Map<String, Boolean>> playerFlags = new ConcurrentHashMap<UUID, Map<String, Boolean>>();
    protected Map<String, Map<String, Boolean>> playerFlagsByName = new ConcurrentHashMap<String, Map<String, Boolean>>();

    protected Map<String, Map<String, Boolean>> groupFlags = new ConcurrentHashMap<String, Map<String, Boolean>>();
    public Map<String, Boolean> cuboidFlags = new ConcurrentHashMap<String, Boolean>();
    protected FlagPermissions parent;

    public FlagPermissions() {
        cuboidFlags = new ConcurrentHashMap<String, Boolean>();
        playerFlags = new ConcurrentHashMap<UUID, Map<String, Boolean>>();
        groupFlags = new ConcurrentHashMap<String, Map<String, Boolean>>();
    }

    public static enum FlagCombo {
        OnlyTrue, OnlyFalse, TrueOrNone, FalseOrNone
    }

    public static enum FlagState {
        TRUE, FALSE, NEITHER, INVALID;

        public String getName() {
            return name().toLowerCase();
        }

        public static FlagState fromString(String flagstate) {
            if (flagstate.equalsIgnoreCase("true") || flagstate.equalsIgnoreCase("t")) {
                return FlagState.TRUE;
            } else if (flagstate.equalsIgnoreCase("false") || flagstate.equalsIgnoreCase("f")) {
                return FlagState.FALSE;
            } else if (flagstate.equalsIgnoreCase("remove") || flagstate.equalsIgnoreCase("r") || flagstate.equalsIgnoreCase("neither")) {
                return FlagState.NEITHER;
            } else {
                return FlagState.INVALID;
            }
        }

    }

    public static void addMaterialToUseFlag(Material mat, Flags flag) {
        if (mat == null)
            return;
        matUseFlagList.put(mat, flag);
    }

    public static void removeMaterialFromUseFlag(Material mat) {
        if (mat == null)
            return;
        matUseFlagList.remove(mat);
    }

    public static EnumMap<Material, Flags> getMaterialUseFlagList() {
        return (EnumMap<Material, Flags>) matUseFlagList;
    }

    public static void addFlag(Flags flag) {
        addFlag(flag.name());
    }

    public static void addFlag(String flag) {
        if (Residence.getInstance() == null) {
            lm.consoleMessage("Can't add flags (" + flag + ") to residence plugin before it was initialized");
            return;
        }
        flag = flag.toLowerCase();
        if (!validFlags.contains(flag)) {
            validFlags.add(flag);
        }
        if (validFlagGroups.containsKey(flag)) {
            validFlagGroups.remove(flag);
        }

        // Checking custom flag
        Flags f = Flags.getFlag(flag);
        if (f == null) {
            Residence.getInstance().getPermissionManager().getAllFlags().setFlag(flag, FlagState.TRUE);
        }
    }

    public static void addPlayerOrGroupOnlyFlag(Flags flag) {
        addPlayerOrGroupOnlyFlag(flag.name());
    }

    public static void addPlayerOrGroupOnlyFlag(String flag) {
        if (Residence.getInstance() == null) {
            lm.consoleMessage("Can't add flags (" + flag + ") to residence plugin before it was initialized");
            return;
        }
        flag = flag.toLowerCase();
        if (!validPlayerFlags.contains(flag)) {
            validPlayerFlags.add(flag);
        }
        if (validFlagGroups.containsKey(flag)) {
            validFlagGroups.remove(flag);
        }

        // Checking custom flag
        Flags f = Flags.getFlag(flag);
        if (f == null) {
            Residence.getInstance().getPermissionManager().getAllFlags().setFlag(flag, FlagState.TRUE);
        }
    }

    public static void addResidenceOnlyFlag(Flags flag) {
        addResidenceOnlyFlag(flag.name());
    }

    public static void addResidenceOnlyFlag(String flag) {
        if (Residence.getInstance() == null) {
            lm.consoleMessage("Can't add flags (" + flag + ") to residence plugin before it was initialized");
            return;
        }
        flag = flag.toLowerCase();
        if (!validAreaFlags.contains(flag)) {
            validAreaFlags.add(flag);
        }
        if (validFlagGroups.containsKey(flag)) {
            validFlagGroups.remove(flag);
        }
        // Checking custom flag
        Flags f = Flags.getFlag(flag);
        if (f == null) {
            Residence.getInstance().getPermissionManager().getAllFlags().setFlag(flag, FlagState.TRUE);
        }
    }

    public static void addFlagToFlagGroup(String group, String flag) {
        
		group = group.toLowerCase();
		
        FlagState state = FlagState.TRUE;

        if (flag.contains("-")) {
            FlagState temp = FlagState.fromString(flag.split("-")[1]);
            if (!temp.equals(FlagState.INVALID))
                state = temp;
            flag = flag.split("-")[0];
        }

        Flags f = Flags.getFlag(flag);
        if (f != null && !f.isGlobalyEnabled()) {
            return;
        }
        
        if (!FlagPermissions.validFlags.contains(group) && !FlagPermissions.validAreaFlags.contains(group) && !FlagPermissions.validPlayerFlags.contains(group)) {
            validFlagGroups.computeIfAbsent(group, k -> new HashMap<String, FlagState>()).put(flag, state);
        }
    }

    public static void removeFlagFromFlagGroup(String group, String flag) {
        
        group = group.toLowerCase();
        
        if (validFlagGroups.containsKey(group)) {
            HashMap<String, FlagState> flags = validFlagGroups.get(group);
            flags.remove(flag);
            if (flags.isEmpty()) {
                validFlagGroups.remove(group);
            }
        }
    }

    public static boolean flagGroupExists(String group) {
        return validFlagGroups.containsKey(group.toLowerCase());
    }

    public static void initValidFlags() {
        validAreaFlags.clear();
        validPlayerFlags.clear();
        validFlags.clear();
        validFlagGroups.clear();

        for (Flags flag : Flags.values()) {
            switch (flag.getFlagMode()) {
            case Both:
                addFlag(flag);
                break;
            case Player:
                addPlayerOrGroupOnlyFlag(flag);
                break;
            case Residence:
                addResidenceOnlyFlag(flag);
                break;
            default:
                break;
            }
        }

        Residence.getInstance().getConfigManager().UpdateGroupedFlagsFile();

        for (CMIMaterial one : CMIMaterial.values()) {
            if (one.getMaterial() == null)
                continue;

            if (one.containsCriteria(CMIMC.BED))
                addMaterialToUseFlag(one.getMaterial(), Flags.bed);

            if (one.containsCriteria(CMIMC.BUTTON))
                addMaterialToUseFlag(one.getMaterial(), Flags.button);

            if (one.containsCriteria(CMIMC.CAKE))
                addMaterialToUseFlag(one.getMaterial(), Flags.cake);

            if (one.containsCriteria(CMIMC.SHULKERBOX))
                addMaterialToUseFlag(one.getMaterial(), Flags.container);

            if (one.containsCriteria(CMIMC.DOOR))
                addMaterialToUseFlag(one.getMaterial(), Flags.door);

            if (one.containsCriteria(CMIMC.FENCEGATE))
                addMaterialToUseFlag(one.getMaterial(), Flags.door);

            if (one.containsCriteria(CMIMC.TRAPDOOR))
                addMaterialToUseFlag(one.getMaterial(), Flags.door);

            if (one.containsCriteria(CMIMC.POTTED))
                addMaterialToUseFlag(one.getMaterial(), Flags.flowerpot);

            if (one.containsCriteria(CMIMC.PRESSUREPLATE))
                addMaterialToUseFlag(one.getMaterial(), Flags.pressure);

            if (Version.isCurrentEqualOrHigher(Version.v1_17_R1)) {
                if (one.containsCriteria(CMIMC.CANDLE))
                    addMaterialToUseFlag(one.getMaterial(), Flags.use);

                if (one.containsCriteria(CMIMC.CANDLECAKE))
                    addMaterialToUseFlag(one.getMaterial(), Flags.cake);
            }

            if (Version.isCurrentEqualOrHigher(Version.v1_21_R6)) {
                if (one.containsCriteria(CMIMC.COPPERCHEST))
                    addMaterialToUseFlag(one.getMaterial(), Flags.container);

                if (one.containsCriteria(CMIMC.SHELF))
                    addMaterialToUseFlag(one.getMaterial(), Flags.container);
            }

        }

        addMaterialToUseFlag(CMIMaterial.ANVIL.getMaterial(), Flags.anvil);
        addMaterialToUseFlag(CMIMaterial.CHIPPED_ANVIL.getMaterial(), Flags.anvil);
        addMaterialToUseFlag(CMIMaterial.DAMAGED_ANVIL.getMaterial(), Flags.anvil);

        addMaterialToUseFlag(CMIMaterial.BEACON.getMaterial(), Flags.beacon);

        addMaterialToUseFlag(CMIMaterial.BREWING_STAND.getMaterial(), Flags.brew);

        addMaterialToUseFlag(CMIMaterial.CHAIN_COMMAND_BLOCK.getMaterial(), Flags.commandblock);
        addMaterialToUseFlag(CMIMaterial.COMMAND_BLOCK.getMaterial(), Flags.commandblock);
        addMaterialToUseFlag(CMIMaterial.REPEATING_COMMAND_BLOCK.getMaterial(), Flags.commandblock);

        addMaterialToUseFlag(CMIMaterial.CHEST.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.DISPENSER.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.DROPPER.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.FURNACE.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.HOPPER.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.JUKEBOX.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.LEGACY_BURNING_FURNACE.getMaterial(), Flags.container);
        addMaterialToUseFlag(CMIMaterial.TRAPPED_CHEST.getMaterial(), Flags.container);

        addMaterialToUseFlag(CMIMaterial.COMPARATOR.getMaterial(), Flags.diode);
        addMaterialToUseFlag(CMIMaterial.DAYLIGHT_DETECTOR.getMaterial(), Flags.diode);
        addMaterialToUseFlag(CMIMaterial.REPEATER.getMaterial(), Flags.diode);

        addMaterialToUseFlag(CMIMaterial.DRAGON_EGG.getMaterial(), Flags.egg);

        addMaterialToUseFlag(CMIMaterial.ENCHANTING_TABLE.getMaterial(), Flags.enchant);

        addMaterialToUseFlag(CMIMaterial.FLOWER_POT.getMaterial(), Flags.flowerpot);

        addMaterialToUseFlag(CMIMaterial.LEVER.getMaterial(), Flags.lever);

        addMaterialToUseFlag(CMIMaterial.NOTE_BLOCK.getMaterial(), Flags.note);

        addMaterialToUseFlag(CMIMaterial.CRAFTING_TABLE.getMaterial(), Flags.table);

        if (Version.isCurrentEqualOrHigher(Version.v1_14_R1)) {
            addMaterialToUseFlag(CMIMaterial.BARREL.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.BLAST_FURNACE.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.CARTOGRAPHY_TABLE.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.COMPOSTER.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.FLETCHING_TABLE.getMaterial(), Flags.container);

            addMaterialToUseFlag(CMIMaterial.GRINDSTONE.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.LOOM.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.SMITHING_TABLE.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.SMOKER.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.STONECUTTER.getMaterial(), Flags.container);

            addMaterialToUseFlag(CMIMaterial.BELL.getMaterial(), Flags.use);
            addMaterialToUseFlag(CMIMaterial.CAMPFIRE.getMaterial(), Flags.use);
            addMaterialToUseFlag(CMIMaterial.LECTERN.getMaterial(), Flags.use);
        }

        if (Version.isCurrentEqualOrHigher(Version.v1_16_R1)) {
            addMaterialToUseFlag(CMIMaterial.SOUL_CAMPFIRE.getMaterial(), Flags.use);
        }

        if (Version.isCurrentEqualOrHigher(Version.v1_20_R1)) {
            addMaterialToUseFlag(CMIMaterial.CHISELED_BOOKSHELF.getMaterial(), Flags.container);
            addMaterialToUseFlag(CMIMaterial.DECORATED_POT.getMaterial(), Flags.container);
        }

        if (Version.isCurrentEqualOrHigher(Version.v1_21_R1)) {
            addMaterialToUseFlag(CMIMaterial.CRAFTER.getMaterial(), Flags.table);
        }

    }

    public void parseCommandLimits(ConfigurationSection node) {
        if (node.isList("WhiteList")) {
            CMDWhiteList = node.getStringList("WhiteList").stream().map(str -> str.replace(" ", "_").replaceAll("^/+", "")).collect(Collectors.toList());
        }
        if (node.isList("BlackList")) {
            CMDBlackList = node.getStringList("BlackList").stream().map(str -> str.replace(" ", "_").replaceAll("^/+", "")).collect(Collectors.toList());
        }
        if (node.isBoolean("Inherit")) {
            inherit = node.getBoolean("Inherit");
        }
    }

    public static FlagPermissions parseFromConfigNode(String name, ConfigurationSection node) {
        FlagPermissions list = new FlagPermissions();

        if (!node.isConfigurationSection(name))
            return list;

        Set<String> keys = node.getConfigurationSection(name).getKeys(false);
        if (keys == null)
            return list;

        for (String key : keys) {
            boolean state = node.getBoolean(name + "." + key, false);
            key = key.toLowerCase();
            Flags f = Flags.getFlag(key);
            if (f != null)
                f.setEnabled(state);
            if (state) {
                list.setFlag(key, FlagState.TRUE);
            } else {
                list.setFlag(key, FlagState.FALSE);
            }
        }
        return list;
    }

    public static FlagPermissions parseFromConfigNodeAsList(String node, String stage) {
        FlagPermissions list = new FlagPermissions();
        if (node.equalsIgnoreCase("true")) {
            list.setFlag(node, FlagState.valueOf(stage));
        } else {
            list.setFlag(node, FlagState.FALSE);
        }

        return list;
    }

    protected Map<String, Boolean> getPlayerFlags(Player player, boolean allowCreate) {

        if (player == null || player.getName() == null)
            return null;

        return getPlayerFlags(player.getUniqueId(), allowCreate);
    }

    public Map<String, Boolean> getPlayerFlags(UUID uuid) {
        return getPlayerFlags(uuid, false);
    }

    protected Map<String, Boolean> getPlayerFlags(UUID uuid, boolean allowCreate) {

        if (uuid == null)
            return null;

        Map<String, Boolean> flags = getPlayerFlags().get(uuid);

        if (flags == null && allowCreate) {
            flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
            getPlayerFlags().put(uuid, flags);
        }

        flags = getFlagsByPlayerName(flags, uuid);

        return flags;
    }

    // To get outdated named flag records, can be removed in future
    private Map<String, Boolean> getFlagsByPlayerName(Map<String, Boolean> flags, UUID uuid) {

        if (playerFlagsByName.isEmpty())
            return flags;

        String name = ResidencePlayer.getName(uuid);

        if (name == null)
            return flags;

        Map<String, Boolean> namedFlags = playerFlagsByName.get(name);

        if (namedFlags == null)
            return flags;

        if (flags == null)
            flags = Collections.synchronizedMap(new HashMap<String, Boolean>());

        flags.putAll(namedFlags);

        getPlayerFlags().computeIfAbsent(uuid, k -> new HashMap<String, Boolean>()).putAll(namedFlags);
        playerFlagsByName.remove(name);

        return flags;
    }

    @Deprecated
    protected Map<String, Boolean> getPlayerFlags(String player, boolean allowCreate)// this function works with uuid in string format as well, instead of player
                                                                                     // name
    {

        Map<String, Boolean> flags = null;
        UUID uuid = ResidencePlayer.getUUID(player);

        String name = uuid != null ? ResidencePlayer.getName(uuid) : ResidencePlayer.getName(player);
        name = name == null ? player : name;

        if (uuid == null)
            uuid = ResidencePlayer.getUUID(name);

        if (uuid != null)
            flags = getPlayerFlags().get(uuid);

        if (flags == null) {
            flags = playerFlagsByName.get(name);
            if (uuid != null && !PlayerManager.isTempUUID(uuid) && flags != null) {
                flags = playerFlagsByName.remove(name);
                getPlayerFlags().put(uuid, flags);
            }
        }

        if (flags == null && allowCreate) {
            flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
            if (uuid != null) {
                getPlayerFlags().put(uuid, flags);
            } else {
                playerFlagsByName.put(name, flags);
            }
        }
        return flags;
    }

    @Deprecated
    public boolean setPlayerFlag(String player, String flag, FlagState state) {
        return setPlayerFlag(ResidencePlayer.getUUID(player), flag, state);
    }

    public boolean setPlayerFlag(UUID uuid, String flag, FlagState state) {

        if (uuid == null)
            return false;

        Map<String, Boolean> map = this.getPlayerFlags(uuid, state != FlagState.NEITHER);
        if (map != null) {
            if (state == FlagState.FALSE) {
                map.put(flag, false);
            } else if (state == FlagState.TRUE) {
                map.put(flag, true);
            } else if (state == FlagState.NEITHER) {
                map.remove(flag);
            }
            if (map.isEmpty())
                this.removeAllPlayerFlags(uuid);
        }

        return true;
    }

    @Deprecated
    public void removeAllPlayerFlags(String player) {// this function works with uuid in string format as well, instead of player
                                                     // name
        removeAllPlayerFlags(ResidencePlayer.getUUID(player));
    }

    public void removeAllPlayerFlags(UUID uuid) {
        if (uuid == null)
            return;
        getPlayerFlags().remove(uuid);
    }

    public void removeAllGroupFlags(String group) {
        groupFlags.remove(group);
    }

    public boolean setGroupFlag(String group, String flag, FlagState state) {
        group = group.toLowerCase();
                
        if (!groupFlags.containsKey(group)) {
            groupFlags.put(group, Collections.synchronizedMap(new HashMap<String, Boolean>()));
        }
        Map<String, Boolean> map = groupFlags.get(group);
        if (state == FlagState.FALSE) {
            map.put(flag, false);
        } else if (state == FlagState.TRUE) {
            map.put(flag, true);
        } else if (state == FlagState.NEITHER) {
            map.remove(flag);
        }
        if (map.isEmpty()) {
            groupFlags.remove(group);
        }
        return true;
    }

    public boolean setFlag(String flag, FlagState state) {
        if (state == FlagState.FALSE) {
            cuboidFlags.put(flag, false);
        } else if (state == FlagState.TRUE) {
            cuboidFlags.put(flag, true);
        } else if (state == FlagState.NEITHER) {
            cuboidFlags.remove(flag);
        }
        return true;
    }

    /**
     * @deprecated Use {@link FlagState#fromString(String)} directly.
     */
    @Deprecated
    public static FlagState stringToFlagState(String flagstate) {
        return FlagState.fromString(flagstate);
    }

    public boolean playerHas(ResidencePlayer resPlayer, Flags flag, boolean def) {
        if (resPlayer == null)
            return false;
        return this.playerCheck(resPlayer.getPlayer(), flag.toString(), this.groupCheck(resPlayer.getGroup(), flag.toString(), this.has(flag, def)));
    }

    public boolean playerHas(Player player, Flags flag, FlagCombo f) {
        switch (f) {
        case FalseOrNone:
            return !playerHas(player, flag, false);
        case OnlyFalse:
            return !playerHas(player, flag, true);
        case OnlyTrue:
            return playerHas(player, flag, false);
        case TrueOrNone:
            return playerHas(player, flag, true);
        default:
            return false;
        }

    }

    public boolean playerHas(Player player, Flags flag, boolean def) {
        if (player == null)
            return false;

        PermissionGroup group = PermissionGroup.getGroup(player);
        if (group == null)
            return false;

        return this.playerCheck(player, flag.toString(), this.groupCheck(group, flag.toString(), this.has(flag, def)));
    }

    public boolean playerHas(Player player, String world, Flags flag, boolean def) {
        if (player == null)
            return false;
        return playerHas(player.getUniqueId(), world, flag, def);
    }

    public boolean playerHas(UUID uuid, String world, Flags flag, boolean def) {
        if (uuid == null)
            return false;

        if (!flag.isGlobalyEnabled())
            return true;

        PermissionGroup group = PermissionGroup.getGroup(uuid, world);
        if (group == null)
            return false;

        return this.playerCheck(uuid, flag.toString(), this.groupCheck(group, flag.toString(), this.has(flag, def)));
    }

    public boolean playerHas(UUID uuid, String world, String flag, boolean def) {
        if (uuid == null)
            return false;

        PermissionGroup group = PermissionGroup.getGroup(uuid, world);
        if (group == null)
            return false;

        return this.playerCheck(uuid, flag, this.groupCheck(group, flag, this.has(flag, def)));
    }

    @Deprecated
    public boolean playerHas(String player, String world, String flag, boolean def) {
        return this.playerHas(ResidencePlayer.getUUID(player), world, flag, def);
    }

    public boolean groupHas(String group, String flag, boolean def) {
        return this.groupCheck(group, flag, this.has(flag, def));
    }

    private boolean playerCheck(Player player, String flag, boolean def) {
        if (player == null)
            return false;

        return playerCheck(player.getUniqueId(), flag, def);
    }

    private boolean playerCheck(UUID uuid, String flag, boolean def) {
        Map<String, Boolean> pmap = this.getPlayerFlags(uuid, false);
        if (pmap != null) {
            if (pmap.containsKey(flag)) {
                return pmap.get(flag);
            }
        }
        if (parent != null) {
            return parent.playerCheck(uuid, flag, def);
        }
        return def;
    }

    private boolean groupCheck(PermissionGroup group, String flag, boolean def) {
        if (group == null)
            return def;
        return groupCheck(group.getGroupName(), flag, def);
    }

    private boolean groupCheck(String group, String flag, boolean def) {
        if (groupFlags.containsKey(group)) {
            Map<String, Boolean> gmap = groupFlags.get(group);
            if (gmap.containsKey(flag)) {
                return gmap.get(flag);
            }
        }
        if (parent != null) {
            return parent.groupCheck(group, flag, def);
        }
        return def;
    }

    public boolean has(Flags flag, FlagCombo f) {
        switch (f) {
        case FalseOrNone:
            return !has(flag, false);
        case OnlyFalse:
            return !has(flag, true);
        case OnlyTrue:
            return has(flag, false);
        case TrueOrNone:
            return has(flag, true);
        default:
            return false;
        }
    }

    public boolean has(Flags flag, boolean def) {
        return has(flag, def, true);
    }

    public boolean has(Flags flag, boolean def, boolean checkParent) {

        Boolean cubFlag = cuboidFlags.get(flag.toString());

        if (cubFlag != null)
            return cubFlag;

        if (checkParent && parent != null)
            return parent.has(flag, def);

        return def;
    }

    @Deprecated
    public boolean has(String flag, boolean def) {
        return has(flag, def, true);
    }

    @Deprecated
    public boolean has(String flag, boolean def, boolean checkParent) {
        if (cuboidFlags.containsKey(flag)) {
            return cuboidFlags.get(flag);
        }
        if (checkParent && parent != null) {
            return parent.has(flag, def);
        }
        return def;
    }

    @Deprecated
    public boolean isPlayerSet(String player, String flag) {
        return isPlayerSet(ResidencePlayer.getUUID(player), flag);
    }

    public boolean isPlayerSet(UUID uuid, String flag) {
        Map<String, Boolean> flags = this.getPlayerFlags(uuid, false);
        if (flags == null)
            return false;
        return flags.containsKey(flag);
    }

    @Deprecated
    public boolean inheritanceIsPlayerSet(String player, String flag) {
        return inheritanceIsPlayerSet(ResidencePlayer.getUUID(player), flag);
    }

    public boolean inheritanceIsPlayerSet(UUID uuid, String flag) {
        Map<String, Boolean> flags = this.getPlayerFlags(uuid, false);
        if (flags == null) {
            return parent == null ? false : parent.inheritanceIsPlayerSet(uuid, flag);
        }
        return flags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsPlayerSet(uuid, flag);
    }

    public boolean isGroupSet(String group, String flag) {
        group = group.toLowerCase();
        Map<String, Boolean> flags = groupFlags.get(group);
        if (flags == null) {
            return false;
        }
        return flags.containsKey(flag);
    }

    public boolean inheritanceIsGroupSet(String group, String flag) {
        group = group.toLowerCase();
        Map<String, Boolean> flags = groupFlags.get(group);
        if (flags == null) {
            return parent == null ? false : parent.inheritanceIsGroupSet(group, flag);
        }
        return flags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsGroupSet(group, flag);
    }

    public boolean isSet(String flag) {
        return cuboidFlags.containsKey(flag);
    }

    public boolean inheritanceIsSet(String flag) {
        return cuboidFlags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsSet(flag);
    }

    public boolean checkValidFlag(String flag, boolean globalflag) {
        if (validFlags.contains(flag)) {
            return true;
        }
        if (globalflag) {
            if (validAreaFlags.contains(flag)) {
                return true;
            }
        } else {
            if (validPlayerFlags.contains(flag)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> save(String world) {
        Map<String, Object> root = new LinkedHashMap<>();

        // Putting uuid's to main cache for later save

        Map<String, Object> playerFlagsClone = new HashMap<String, Object>();
        for (Entry<UUID, Map<String, Boolean>> one : getPlayerFlags().entrySet()) {
            MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, one.getValue());
            playerFlagsClone.put(one.getKey().toString(), min.getId());
        }

        for (Entry<String, Map<String, Boolean>> one : playerFlagsByName.entrySet()) {
            MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, one.getValue());
            playerFlagsClone.put(one.getKey(), min.getId());
        }

        root.put("PlayerFlags", playerFlagsClone);

        if (!groupFlags.isEmpty()) {
            Map<String, Object> GroupFlagsClone = new HashMap<String, Object>();
            for (Entry<String, Map<String, Boolean>> one : groupFlags.entrySet()) {
                MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, one.getValue());
                GroupFlagsClone.put(one.getKey(), min.getId());
            }
            root.put("GroupFlags", GroupFlagsClone);
        }

        MinimizeFlags min = Residence.getInstance().getResidenceManager().addFlagsTempCache(world, cuboidFlags);
        if (min == null) {
            // Cloning map to fix issue for yml anchors being created
            root.put("AreaFlags", new HashMap<String, Boolean>(cuboidFlags));
        } else {
            root.put("AreaFlags", min.getId());
        }

        return root;
    }

    public static FlagPermissions load(Map<String, Object> root) throws Exception {
        FlagPermissions newperms = new FlagPermissions();
        return FlagPermissions.load(root, newperms);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static FlagPermissions load(Map<String, Object> root, FlagPermissions newperms) throws Exception {

        if (root.containsKey("PlayerFlags")) {
            boolean old = true;
            for (Entry<String, Object> one : ((HashMap<String, Object>) root.get("PlayerFlags")).entrySet()) {
                if (one.getValue() instanceof Integer)
                    old = false;
                break;
            }

            Map<UUID, Map<String, Boolean>> byUUID = new HashMap<UUID, Map<String, Boolean>>();
            Map<String, Map<String, Boolean>> byName = new HashMap<String, Map<String, Boolean>>();

            if (old) {
                Map<String, Map<String, Boolean>> temp = (Map) root.get("PlayerFlags");

                for (Entry<String, Map<String, Boolean>> one : temp.entrySet()) {

                    if (one.getKey().length() == 36 && one.getKey().contains("-")) {
                        try {
                            UUID uuid = UUID.fromString(one.getKey());
                            byUUID.put(uuid, one.getValue());
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    byName.put(one.getKey(), one.getValue());
                    newperms.getPlayerFlagsByName().put(one.getKey(), new HashMap<String, Boolean>(one.getValue()));
                }

            } else if (newperms instanceof ResidencePermissions) {
                Map<String, Boolean> ft = new HashMap<String, Boolean>();
                for (Entry<String, Integer> one : ((HashMap<String, Integer>) root.get("PlayerFlags")).entrySet()) {
                    ft = Residence.getInstance().getResidenceManager().getChacheFlags(((ResidencePermissions) newperms).getWorldName(), one.getValue());
                    if (ft == null || ft.isEmpty())
                        continue;

                    if (one.getKey().length() == 36 && one.getKey().contains("-")) {
                        try {
                            UUID uuid = UUID.fromString(one.getKey());
                            byUUID.put(uuid, new HashMap<String, Boolean>(ft));
                            continue;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    byName.put(one.getKey(), new HashMap<String, Boolean>(ft));
                }
            }

            if (!byUUID.isEmpty())
                newperms.playerFlags = byUUID;
            if (!byName.isEmpty())
                newperms.playerFlagsByName = byName;
        }

        if (root.containsKey("GroupFlags")) {
            boolean old = true;
            for (Entry<String, Object> one : ((HashMap<String, Object>) root.get("GroupFlags")).entrySet()) {
                if (one.getValue() instanceof Integer)
                    old = false;
                break;
            }
            if (old)
                newperms.groupFlags = (Map) root.get("GroupFlags");
            else {
                if (newperms instanceof ResidencePermissions) {
                    Map<String, Map<String, Boolean>> t = new HashMap<String, Map<String, Boolean>>();
                    Map<String, Boolean> ft = new HashMap<String, Boolean>();
                    for (Entry<String, Integer> one : ((HashMap<String, Integer>) root.get("GroupFlags")).entrySet()) {
                        ft = Residence.getInstance().getResidenceManager().getChacheFlags(((ResidencePermissions) newperms).getWorldName(), one.getValue());
                        if (ft != null && !ft.isEmpty())
                            t.put(one.getKey(), new HashMap<String, Boolean>(ft));
                    }
                    if (!t.isEmpty()) {
                        newperms.groupFlags = t;
                    }
                }
            }
        }

        if (root.containsKey("AreaFlags")) {
            boolean old = true;
            if (root.get("AreaFlags") instanceof Integer)
                old = false;
            if (old)
                newperms.cuboidFlags = (Map) root.get("AreaFlags");
            else {
                if (newperms instanceof ResidencePermissions) {
                    Map<String, Boolean> ft = new HashMap<String, Boolean>();
                    ft = Residence.getInstance().getResidenceManager().getChacheFlags(((ResidencePermissions) newperms).getWorldName(), (Integer) root.get("AreaFlags"));
                    if (ft != null && !ft.isEmpty())
                        newperms.cuboidFlags = new HashMap<String, Boolean>(ft);
                }
            }
        } else
            newperms.cuboidFlags = Residence.getInstance().getConfigManager().getGlobalResidenceDefaultFlags().getFlags();

        String ownerName = null;
        UUID uuid = null;

        if (root.containsKey("OwnerLastKnownName")) {
            ownerName = (String) root.get("OwnerLastKnownName");
            if (root.containsKey("OwnerUUID"))
                uuid = UUID.fromString((String) root.get("OwnerUUID"));
            else
                uuid = PlayerManager.createTempUUID(ownerName);
        }

        newperms.convertPlayerNamesToUUIDs(ownerName, uuid);

        return newperms;
    }

    private void convertPlayerNamesToUUIDs(String ownerName, UUID ownerUUID) {

        HashMap<String, UUID> converts = new HashMap<>();

        for (String keyset : playerFlagsByName.keySet()) {
            if (keyset.length() != 36) {
                UUID uuid = null;
                if (ownerName == null || !ownerName.equals(keyset) || PlayerManager.isTempUUID(ownerUUID))
                    uuid = ResidencePlayer.getUUID(keyset);

                if (uuid != null && !PlayerManager.isTempUUID(uuid))
                    converts.put(keyset, uuid);
            }
        }

        for (Entry<String, UUID> convert : converts.entrySet()) {
            playerFlags.computeIfAbsent(convert.getValue(), k -> new HashMap<String, Boolean>()).putAll(playerFlagsByName.remove(convert.getKey()));
        }
    }

    public String listFlags() {
        return listFlags(0, 0);
    }

    public String listFlags(Integer split) {
        return listFlags(split, 0);
    }

    public String listFlags(Integer split, Integer totalShow) {
        StringBuilder sbuild = new StringBuilder();
        Set<Entry<String, Boolean>> set = cuboidFlags.entrySet();

        FlagPermissions gRD = Residence.getInstance().getConfigManager().getGlobalResidenceDefaultFlags();

        synchronized (set) {
            Iterator<Entry<String, Boolean>> it = set.iterator();
            int i = -1;
            int t = 0;

            String haveColor = Residence.getInstance().getLM().getMessage(lm.Flag_haveColor);
            String denyColor = Residence.getInstance().getLM().getMessage(lm.Flag_lackColor);
            String havePrefix = Residence.getInstance().getLM().getMessage(lm.Flag_havePrefix);
            String denyPrefix = Residence.getInstance().getLM().getMessage(lm.Flag_lackPrefix);

            while (it.hasNext()) {
                Entry<String, Boolean> next = it.next();

                if (Residence.getInstance().getConfigManager().isInfoExcludeDFlags() && gRD.cuboidFlags.get(next.getKey()) != null && gRD.cuboidFlags.get(next.getKey()) == next.getValue())
                    continue;

                String fname = next.getKey();

                Flags flag = Flags.getFlag(fname);

                if (flag != null && !flag.isGlobalyEnabled())
                    continue;
                if (flag != null)
                    fname = flag.getName();

                i++;
                t++;

                if (totalShow > 0 && t > totalShow) {
                    break;
                }

                if (split > 0 && i >= split) {
                    i = 0;
                    sbuild.append("\n");
                }

                if (next.getValue()) {
                    sbuild.append(haveColor).append(havePrefix).append(fname);
                    if (it.hasNext()) {
                        sbuild.append(" ");
                    }
                } else {
                    sbuild.append(denyColor).append(denyPrefix).append(fname);
                    if (it.hasNext()) {
                        sbuild.append(" ");
                    }
                }

            }
        }
        if (sbuild.length() == 0) {
            sbuild.append("none");
        }
        return CMIChatColor.translate(sbuild.toString());
    }

    public Map<String, Boolean> getFlags() {
        return cuboidFlags;
    }

    @Deprecated
    public Map<String, Boolean> getPlayerFlags(String player) {
        return this.getPlayerFlags(player, false);
    }

    @Deprecated
    public Set<String> getposibleFlags() {
        return getAllPossibleFlags();
    }

    public static Set<String> getAllPossibleFlags() {
        Set<String> t = new HashSet<String>();
        t.addAll(FlagPermissions.validFlags);
        t.addAll(FlagPermissions.validPlayerFlags);
        return t;
    }

    @Deprecated
    public static ArrayList<String> getPosibleAreaFlags() {
        return getPossibleAreaFlags();
    }

    public static ArrayList<String> getPossibleAreaFlags() {
        return FlagPermissions.validAreaFlags;
    }

    @Deprecated
    public List<String> getPosibleFlags(Player player, boolean residence, boolean resadmin) {
        return getPossibleFlags(player, residence, resadmin);
    }

    public List<String> getPossibleFlags(Player player, boolean residence, boolean resadmin) {
        Set<String> flags = new HashSet<String>();
        for (Entry<String, Boolean> one : Residence.getInstance().getPermissionManager().getAllFlags().getFlags().entrySet()) {
            if (!one.getValue() && !resadmin && !ResPerm.flag_$1.hasSetPermission(player, one.getKey().toLowerCase()))
                continue;

            if (!residence && !getAllPossibleFlags().contains(one.getKey()))
                continue;

            String fname = one.getKey();

            Flags flag = Flags.getFlag(fname);

            if (flag != null && !flag.isGlobalyEnabled())
                continue;

            flags.add(one.getKey());
        }

        return new ArrayList<String>(flags);
    }

    @Deprecated
    public String listPlayerFlags(String player) {
        return listPlayerFlags(ResidencePlayer.getUUID(player));
    }

    public String listPlayerFlags(UUID uuid) {
        Map<String, Boolean> flags = this.getPlayerFlags(uuid, false);
        if (flags != null) {
            return this.printPlayerFlags(flags);
        }
        return "none";
    }

    protected String printPlayerFlags(Map<String, Boolean> flags) {
        StringBuilder sbuild = new StringBuilder();
        if (flags == null)
            return "none";
        Set<Entry<String, Boolean>> set = flags.entrySet();

        String haveColor = Residence.getInstance().getLM().getMessage(lm.Flag_haveColor);
        String denyColor = Residence.getInstance().getLM().getMessage(lm.Flag_lackColor);
        String havePrefix = Residence.getInstance().getLM().getMessage(lm.Flag_havePrefix);
        String denyPrefix = Residence.getInstance().getLM().getMessage(lm.Flag_lackPrefix);

        synchronized (set) {
            Iterator<Entry<String, Boolean>> it = set.iterator();

            while (it.hasNext()) {
                Entry<String, Boolean> next = it.next();

                String fname = next.getKey();

                Flags flag = Flags.getFlag(next.getKey());
                if (flag != null && !flag.isGlobalyEnabled())
                    continue;
                if (flag != null)
                    fname = flag.getName();

                if (next.getValue()) {
                    sbuild.append(haveColor).append(havePrefix).append(fname);
                    if (it.hasNext()) {
                        sbuild.append(" ");
                    }
                } else {
                    sbuild.append(denyColor).append(denyPrefix).append(fname);
                    if (it.hasNext()) {
                        sbuild.append(" ");
                    }
                }
            }
        }
        if (sbuild.length() == 0) {
            sbuild.append("none");
        }
        return CMIChatColor.translate(sbuild.toString());
    }

    @Deprecated
    public String listOtherPlayersFlags(String player) {
        return listOtherPlayersFlags(ResidencePlayer.getUUID(player));
    }

    public String listOtherPlayersFlags(UUID uuid) {
        if (uuid == null)
            return "";

        StringBuilder sbuild = new StringBuilder();
        Set<Entry<UUID, Map<String, Boolean>>> set = getPlayerFlags().entrySet();
        synchronized (set) {
            Iterator<Entry<UUID, Map<String, Boolean>>> it = set.iterator();
            while (it.hasNext()) {
                Entry<UUID, Map<String, Boolean>> nextEnt = it.next();
                UUID pUUID = nextEnt.getKey();
                if (pUUID.equals(uuid))
                    continue;

                String perms = printPlayerFlags(nextEnt.getValue());

                String pName = ResidencePlayer.getName(pUUID);

                if (!perms.equals("none")) {
                    sbuild.append(pName).append(CMIChatColor.WHITE).append("[").append(perms).append(CMIChatColor.WHITE).append("] ");
                }
            }
        }

        if (playerFlagsByName.isEmpty())
            return sbuild.toString();

        Set<Entry<String, Map<String, Boolean>>> setByName = playerFlagsByName.entrySet();
        synchronized (setByName) {
            Iterator<Entry<String, Map<String, Boolean>>> it = setByName.iterator();
            while (it.hasNext()) {
                Entry<String, Map<String, Boolean>> nextEnt = it.next();

                String pName = nextEnt.getKey();

                UUID pUUID = ResidencePlayer.getUUID(pName);

                if (uuid.equals(pUUID))
                    continue;

                String perms = printPlayerFlags(nextEnt.getValue());

                if (!perms.equals("none")) {
                    sbuild.append(pName).append(CMIChatColor.WHITE).append("[").append(perms).append(CMIChatColor.WHITE).append("] ");
                }
            }
        }

        return sbuild.toString();
    }

    public String listPlayersFlags() {
        StringBuilder sbuild = new StringBuilder();
        Set<Entry<UUID, Map<String, Boolean>>> set = getPlayerFlags().entrySet();
        synchronized (set) {
            Iterator<Entry<UUID, Map<String, Boolean>>> it = set.iterator();
            while (it.hasNext()) {
                Entry<UUID, Map<String, Boolean>> nextEnt = it.next();
                UUID pUUID = nextEnt.getKey();

                String pName = ResidencePlayer.getName(pUUID);

                String perms = printPlayerFlags(nextEnt.getValue());

                if (Residence.getInstance().getServerLandName().equalsIgnoreCase(pName))
                    continue;

                if (pName != null && !perms.equals("none")) {
                    sbuild.append(pName).append(CMIChatColor.WHITE).append("[").append(perms).append(CMIChatColor.WHITE).append("] ");
                }
            }
        }

        Set<Entry<String, Map<String, Boolean>>> setByName = playerFlagsByName.entrySet();
        synchronized (setByName) {
            Iterator<Entry<String, Map<String, Boolean>>> it = setByName.iterator();
            while (it.hasNext()) {
                Entry<String, Map<String, Boolean>> nextEnt = it.next();

                String pName = nextEnt.getKey();

                if (pName.equalsIgnoreCase(Residence.getInstance().getServerLandName()))
                    continue;

                String perms = printPlayerFlags(nextEnt.getValue());

                if (!perms.equals("none")) {
                    sbuild.append(pName).append(CMIChatColor.WHITE).append("[").append(perms).append(CMIChatColor.WHITE).append("] ");
                }
            }
        }
        return sbuild.toString();
    }

    String p1Color = null;
    String p2Color = null;

    @Deprecated
    public RawMessage listPlayersFlagsRaw(String player, String text) {
        return listPlayersFlagsRaw(ResidencePlayer.getUUID(player), text);
    }

    public RawMessage listPlayersFlagsRaw(UUID uuid, String text) {

        if (p1Color == null) {
            p1Color = lm.Flag_p1Color.getMessage();
            p2Color = lm.Flag_p2Color.getMessage();
        }

        RawMessage rm = new RawMessage();
        rm.addText(text);

        int playersToShow = 5;
        int i = 0;

        ResidencePlayer rplayer = ResidencePlayer.get(uuid);

        Set<Entry<UUID, Map<String, Boolean>>> set = getPlayerFlags().entrySet();
        synchronized (set) {
            Iterator<Entry<UUID, Map<String, Boolean>>> it = set.iterator();
            boolean random = true;

            UUID addedOwn = null;
            if (rplayer != null) {
                Map<String, Boolean> own = getPlayerFlags().get(rplayer.getUniqueId());
                if (own != null) {
                    addedOwn = uuid;
                    random = addPlayerFlagToRM(rm, uuid, own, random);
                    i++;
                }
            }

            StringBuilder hover = new StringBuilder();
            while (it.hasNext()) {
                Entry<UUID, Map<String, Boolean>> nextEnt = it.next();

                if (addedOwn != null && nextEnt.getKey().equals(addedOwn))
                    continue;

                i++;

                String playerName = ResidencePlayer.getName(nextEnt.getKey());

                if (playerName == null)
                    playerName = "UNKNOWN";

                if (i <= playersToShow)
                    random = addPlayerFlagToRM(rm, playerName, nextEnt.getValue(), random);
                if (i == playersToShow) {
                    rm.addText(lm.Flag_others.getMessage(set.size() - i));
                }

                if (i > playersToShow) {
                    if (random)
                        playerName = p2Color + playerName;
                    else
                        playerName = p1Color + playerName;
                    random = !random;
                    hover.append(playerName).append(" ");
                }
            }

            if (!hover.toString().isEmpty()) {
                rm.addHover(splitBy(5, hover.toString()));
            }
        }

        if (playerFlagsByName.isEmpty())
            return rm;

        Set<Entry<String, Map<String, Boolean>>> setByName = playerFlagsByName.entrySet();
        synchronized (setByName) {
            Iterator<Entry<String, Map<String, Boolean>>> it = setByName.iterator();
            boolean random = true;

            String ownerName = rplayer != null ? rplayer.getName() : "";

            String addedOwn = null;
            if (rplayer != null) {
                Map<String, Boolean> own = playerFlagsByName.get(ownerName);
                if (own != null) {
                    addedOwn = ownerName;
                    random = addPlayerFlagToRM(rm, uuid, own, random);
                    i++;
                }
            }

            StringBuilder hover = new StringBuilder();
            while (it.hasNext()) {
                Entry<String, Map<String, Boolean>> nextEnt = it.next();

                if (addedOwn != null && ownerName.equals(nextEnt.getKey()))
                    continue;

                i++;

                String playerName = ResidencePlayer.getName(nextEnt.getKey());

                if (i <= playersToShow)
                    random = addPlayerFlagToRM(rm, playerName, nextEnt.getValue(), random);
                if (i == playersToShow) {
                    rm.addText(lm.Flag_others.getMessage(set.size() - i));
                }

                if (i > playersToShow) {
                    if (random)
                        playerName = p2Color + playerName;
                    else
                        playerName = p1Color + playerName;
                    random = !random;
                    hover.append(playerName).append(" ");
                }
            }

            if (!hover.toString().isEmpty()) {
                rm.addHover(splitBy(5, hover.toString()));
            }
        }
        return rm;
    }

    private boolean addPlayerFlagToRM(RawMessage rm, UUID uuid, Map<String, Boolean> permMap, boolean random) {

        return addPlayerFlagToRM(rm, ResidencePlayer.getName(uuid), permMap, random);
    }

    private boolean addPlayerFlagToRM(RawMessage rm, String playerName, Map<String, Boolean> permMap, boolean random) {
        String perms = printPlayerFlags(permMap);

        if (playerName.equalsIgnoreCase(Residence.getInstance().getServerLandName()))
            return random;

        if (perms.equals("none"))
            return random;

        if (random)
            playerName = p2Color + playerName;
        else
            playerName = p1Color + playerName;
        random = !random;

        rm.addText(playerName + "&r").addHover(splitBy(5, perms));
        rm.addText(" ");
        return random;
    }

    public void listPlayers(CommandSender sender, String text, int page) {

        if (p1Color == null) {
            p1Color = lm.Flag_p1Color.getMessage();
            p2Color = lm.Flag_p2Color.getMessage();
        }

        RawMessage rm = new RawMessage();
        if (text != null)
            rm.addText(text);

        PageInfo pi = new PageInfo(10, getPlayerFlags().size() + playerFlagsByName.size(), page);

        Set<Entry<UUID, Map<String, Boolean>>> set = getPlayerFlags().entrySet();
        synchronized (set) {
            Iterator<Entry<UUID, Map<String, Boolean>>> it = set.iterator();
            UUID addedOwn = null;

            ResidencePlayer rplayer = ResidencePlayer.get(sender);
            if (rplayer != null) {
                Map<String, Boolean> own = getPlayerFlags().get(rplayer.getUniqueId());
                if (own != null) {
                    addedOwn = rplayer.getUniqueId();
                    if (!pi.isContinue()) {
                        rm = new RawMessage();
                        addPlayerFlags(rm, PlayerManager.getSenderUUID(sender), own, pi.getPositionForOutput());
                        rm.show(sender);
                    }
                }
            }

            while (it.hasNext()) {
                if (pi.isContinue()) {
                    it.next();
                    continue;
                }
                if (pi.isBreak())
                    break;
                Entry<UUID, Map<String, Boolean>> nextEnt = it.next();

                if (addedOwn != null && (addedOwn.equals(nextEnt.getKey())))
                    continue;
                rm = new RawMessage();
                addPlayerFlags(rm, nextEnt.getKey(), nextEnt.getValue(), pi.getPositionForOutput());
                rm.show(sender);
            }
        }

        Set<Entry<String, Map<String, Boolean>>> setByName = playerFlagsByName.entrySet();
        synchronized (setByName) {
            Iterator<Entry<String, Map<String, Boolean>>> it = setByName.iterator();
            String addedOwn = null;

            ResidencePlayer rplayer = ResidencePlayer.get(sender);
            if (rplayer != null) {
                Map<String, Boolean> own = playerFlagsByName.get(rplayer.getName());
                if (own != null) {
                    addedOwn = rplayer.getName();
                    if (!pi.isContinue()) {
                        rm = new RawMessage();
                        addPlayerFlags(rm, PlayerManager.getSenderUUID(sender), own, pi.getPositionForOutput());
                        rm.show(sender);
                    }
                }
            }

            while (it.hasNext()) {
                if (pi.isContinue()) {
                    it.next();
                    continue;
                }
                if (pi.isBreak())
                    break;
                Entry<String, Map<String, Boolean>> nextEnt = it.next();
                if (addedOwn != null && (sender.getName().equals(nextEnt.getKey()) || addedOwn.equals(nextEnt.getKey())))
                    continue;
                rm = new RawMessage();
                addPlayerFlags(rm, nextEnt.getKey(), nextEnt.getValue(), pi.getPositionForOutput());
                rm.show(sender);
            }
        }
    }

    private void addPlayerFlags(RawMessage rm, String next, Map<String, Boolean> permMap, int position) {
        addPlayerFlags(rm, ResidencePlayer.getUUID(next), permMap, position);
    }

    private void addPlayerFlags(RawMessage rm, UUID uuid, Map<String, Boolean> permMap, int position) {

        String perms = printPlayerFlags(permMap);

        String next = ResidencePlayer.getName(uuid);

        if (next.equalsIgnoreCase(Residence.getInstance().getServerLandName()))
            return;

        if (perms.equals("none"))
            return;

        rm.addText(lm.Residence_ResList.getMessage(position, next, limitTo(5, perms), "", ""));
        rm.addCommand("res pset " + next);
        rm.addHover(splitBy(6, perms));

    }

    protected String limitTo(int to, String perms) {
        if (!perms.contains(" "))
            return perms;
        int i = 0;
        StringBuilder str = new StringBuilder();
        for (String one : perms.split(" ")) {
            i++;
            if (!str.toString().isEmpty())
                str.append(LC.info_ListSpliter.getLocale());
            str.append(one);
            if (i >= to) {
                str.append("...");
                break;
            }
        }

        return str.toString();
    }

    protected String splitBy(int by, String perms) {
        if (!perms.contains(" "))
            return perms;

        String[] splited = perms.split(" ");
        int i = 0;
        perms = "";
        StringBuilder str = new StringBuilder();
        for (String one : splited) {
            i++;
            if (!str.toString().isEmpty())
                str.append(" ");
            str.append(one);
            if (i % by == 0 && i < splited.length) {
                str.append("\n");
            }
        }

        return str.toString();
    }

    public String listGroupFlags() {
        StringBuilder sbuild = new StringBuilder();
        Set<String> set = groupFlags.keySet();
        synchronized (set) {
            Iterator<String> it = set.iterator();
            while (it.hasNext()) {
                String next = it.next();
                String perms = listGroupFlags(next);
                if (!perms.equals("none")) {
                    sbuild
                            .append(next)
                            .append(CMIChatColor.WHITE)
                            .append("[")
                            .append(CMIChatColor.DARK_AQUA)
                            .append(perms)
                            .append(CMIChatColor.WHITE)
                            .append("] ");
                }
            }
        }
        return sbuild.toString();
    }

    public String listGroupFlags(String group) {
        group = group.toLowerCase();
        if (groupFlags.containsKey(group)) {
            StringBuilder sbuild = new StringBuilder();
            Map<String, Boolean> get = groupFlags.get(group);
            Set<Entry<String, Boolean>> set = get.entrySet();

            String haveColor = Residence.getInstance().getLM().getMessage(lm.Flag_haveColor);
            String denyColor = Residence.getInstance().getLM().getMessage(lm.Flag_lackColor);
            String havePrefix = Residence.getInstance().getLM().getMessage(lm.Flag_havePrefix);
            String denyPrefix = Residence.getInstance().getLM().getMessage(lm.Flag_lackPrefix);

            synchronized (get) {
                Iterator<Entry<String, Boolean>> it = set.iterator();
                while (it.hasNext()) {
                    Entry<String, Boolean> next = it.next();
                    if (next.getValue()) {
                        sbuild.append(haveColor).append(havePrefix).append(next.getKey());
                        if (it.hasNext()) {
                            sbuild.append(" ");
                        }
                    } else {
                        sbuild.append(denyColor).append(denyPrefix).append(next.getKey());
                        if (it.hasNext()) {
                            sbuild.append(" ");
                        }
                    }
                }
            }
            if (sbuild.length() == 0) {
                groupFlags.remove(group);
                sbuild.append("none");
            }
            return CMIChatColor.translate(sbuild.toString());
        }
        return "none";
    }

    public void clearFlags() {
        groupFlags.clear();
        playerFlags.clear();
        cuboidFlags.clear();
    }

    public void printFlags(Player player) {
        lm.General_ResidenceFlags.sendMessage(player, listFlags());
        lm.General_PlayersFlags.sendMessage(player, listPlayerFlags(player.getUniqueId()));
        lm.General_GroupFlags.sendMessage(player, listGroupFlags());
        lm.General_OthersFlags.sendMessage(player, listOtherPlayersFlags(player.getUniqueId()));
    }

    @Deprecated
    public void copyUserPermissions(String fromUser, String toUser) {
        this.copyUserPermissions(ResidencePlayer.getUUID(fromUser), ResidencePlayer.getUUID(toUser));
    }

    public void copyUserPermissions(UUID fromUser, UUID toUser) {
        Map<String, Boolean> get = this.getPlayerFlags(fromUser, false);
        if (get != null) {
            Map<String, Boolean> targ = this.getPlayerFlags(toUser, true);
            for (Entry<String, Boolean> entry : get.entrySet()) {
                targ.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Deprecated
    public void clearPlayersFlags(String user) {
        this.removeAllPlayerFlags(user);
    }

    public void clearPlayersFlags(UUID uuid) {
        this.removeAllPlayerFlags(uuid);
    }

    public void setParent(FlagPermissions p) {
        parent = p;
    }

    public FlagPermissions getParent() {
        return parent;
    }

    public Map<UUID, Map<String, Boolean>> getPlayerFlags() {
        return playerFlags;
    }

    public Map<String, Map<String, Boolean>> getPlayerFlagsByName() {
        return playerFlagsByName;
    }

    public List<String> getCMDWhiteList() {
        return CMDWhiteList;
    }

    public List<String> getCMDBlackList() {
        return CMDBlackList;
    }

    public boolean isInheritCMDLimits() {
        return inherit;
    }

    public static boolean has(Location loc, Flags flag, FlagCombo combo) {
        return getPerms(loc).has(flag, combo);
    }

    public static boolean has(Location loc, Flags flag, boolean state) {
        return getPerms(loc).has(flag, state);
    }

    public static boolean has(Location loc, Player player, Flags flag, FlagCombo combo) {
        return FlagPermissions.getPerms(loc, player).playerHas(player, flag, combo);
    }

    public static boolean has(Location loc, Player player, Flags flag, boolean state) {
        return FlagPermissions.getPerms(loc, player).playerHas(player, flag, state);
    }

    public static FlagPermissions getPerms(Location loc) {
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        if (res != null)
            return res.getPermissions();
        return Residence.getInstance().getWorldFlags().getPerms(loc.getWorld().getName());
    }

    public static FlagPermissions getPerms(Player player) {
        return getPerms(player.getLocation(), player);
    }

    public static FlagPermissions getPerms(Location loc, Player player) {
        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
        if (res != null)
            return res.getPermissions();
        if (player != null)
            return Residence.getInstance().getWorldFlags().getPerms(player);
        return Residence.getInstance().getWorldFlags().getPerms(loc.getWorld().getName());
    }

    public static FlagPermissions getPerms(World world) {
        return Residence.getInstance().getWorldFlags().getPerms(world);
    }

    public static boolean isIgnoreGroupedFlagsAccess() {
        return ignoreGroupedFlagsAccess;
    }

    public static void setIgnoreGroupedFlagsAccess(boolean flagsAccess) {
        ignoreGroupedFlagsAccess = flagsAccess;
    }
}
