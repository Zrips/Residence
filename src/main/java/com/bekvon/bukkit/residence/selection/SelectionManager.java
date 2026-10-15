package com.bekvon.bukkit.residence.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.SelectionSides;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceSelectionVisualizationEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager.ResPerm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayManager;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayType;

import net.Zrips.CMILib.ActionBar.CMIActionBar;
import net.Zrips.CMILib.Colors.CMIChatColor;
import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Container.CMIVectorInt3D;
import net.Zrips.CMILib.Container.CMIWorld;
import net.Zrips.CMILib.Cuboids.CMIBlockWorldArea;
import net.Zrips.CMILib.Effects.CMIEffect;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Version.Schedulers.CMIScheduler;

public class SelectionManager {

    protected Map<UUID, Selection> selections;
    private HashMap<UUID, Visualizer> vMap = new HashMap<UUID, Visualizer>();

    protected Residence plugin;
    protected Server server;

    Permission ignoreyPermission = new Permission(ResPerm.bypass_ignorey.getPermission(), PermissionDefault.FALSE);
    Permission ignoreyinsubzonePermission = new Permission(ResPerm.bypass_ignoreyinsubzone.getPermission(), PermissionDefault.FALSE);

    private static CMIMaterial tool = CMIMaterial.WOODEN_HOE;
    private static CMIMaterial infoTool = CMIMaterial.STRING;
    private static boolean smartSelect = false;
    private static boolean ignoreY = false;
    private static boolean ignoreYInSubzone = false;
    private static int netherHeight = 128;
    private static boolean noCostForYBlocks = false;
    private static boolean worldEditIntegration = false;

    public static void loadConfig(ConfigReader c) {

        if (c == null)
            return;

        c.addComment("Global.Selection.ToolId", "Wooden Hoe is the default selection tool for Residence.");
        setTool(CMIMaterial.get(c.get("Global.Selection.ToolId", c.getC().getString("Global.SelectionToolId", CMIMaterial.WOODEN_HOE.name()))));

        c.addComment("Global.Selection.SmartSelect",
                "When enabled player will not need to select two oposite corners with left and right mouse clicks",
                "instead player can select any block and selection will be expanded/contracted based on closest corner/face");
        setSmartSelect(c.get("Global.Selection.SmartSelect", true));

        c.addComment("Global.Selection.IgnoreY", "By setting this to true, all selections will be made from bedrock to sky ignoring Y coordinates");
        setIgnoreY(c.get("Global.Selection.IgnoreY", false));

        c.addComment("Global.Selection.IgnoreYInSubzone",
                "When this set to true, selections inside existing residence will be from bottom to top of that residence",
                "When this set to false, selections inside existing residence will be exactly as they are");
        setIgnoreYInSubzone(c.get("Global.Selection.IgnoreYInSubzone", false));

        c.addComment("Global.Selection.netherHeight",
                "Defines height of nether when creating residences. This mostly applies when performing commands like /res select vert or /res auto which will expand residence to defined height",
                "This cant be higher than 319 or lower than 1");
        setNetherHeight(CMINumber.clamp(c.get("Global.Selection.netherHeight", 128), 1, 319));

        c.addComment("Global.Selection.NoCostForYBlocks", "By setting this to true, player will only pay for x*z blocks ignoring height",
                "This will lower residence price by up to 319 times, so adjust block price BEFORE enabling this");
        setNoCostForYBlocks(c.get("Global.Selection.NoCostForYBlocks", false));

        c.addComment("Global.Selection.WorldEditIntegration", "Enable or disable world edit integration into Residence plugin");
        setWorldEditIntegration(c.get("Global.Selection.WorldEditIntegration", true));

        c.addComment("Global.Selection.InfoToolId", "This determins which tool you can use to see info on residences, default is String.",
                "Simply equip this tool and hit a location inside the residence and it will display the info for it.");
        setInfoTool(CMIMaterial.get(c.get("Global.Selection.InfoToolId", c.getString("Global.InfoToolId", Material.STRING.toString()))));

    }

    public enum selectionType {
        noLimits, ignoreY, residenceBounds;
    }

    public class Selection {
        private Player player;
        private Location loc1;
        private Location loc2;
        private CMIBlockWorldArea area = new CMIBlockWorldArea();

        public Selection(Player player) {
            this.player = player;
        }

        public Location getBaseLoc1() {
            return loc1 == null ? null : loc1.clone();
        }

        public void setBaseLoc1(Location loc1) {
            this.loc1 = loc1.clone();

            area.setLowLocation(loc1);
            area.setHighLocation(loc2);
        }

        public World getWorld() {
            if (loc1 != null)
                return loc1.getWorld();
            if (loc2 != null)
                return loc2.getWorld();
            return player.getWorld();
        }

        public Location getBaseLoc2() {
            return loc2 == null ? null : loc2.clone();
        }

        public void setBaseLoc2(Location loc2) {
            this.loc2 = loc2.clone();

            area.setLowLocation(loc1);
            area.setHighLocation(loc2);
        }

        public selectionType getSelectionRestrictions() {
            if (inSameResidence()) {
                if (isIgnoreYInSubzone()) {
                    if (hasPlacedBoth() && !player.hasPermission(ignoreyinsubzonePermission)) {
                        return selectionType.residenceBounds;
                    }
                }
            } else {
                if (isIgnoreY()) {
                    if (hasPlacedBoth() && !player.hasPermission(ignoreyPermission)) {
                        return selectionType.ignoreY;
                    }
                }
            }
            return selectionType.noLimits;
        }

        public int getMaxYAllowed() {
            switch (getSelectionRestrictions()) {
            case ignoreY:
            case noLimits:
            default:
                return getMaxWorldHeight(getWorld());
            case residenceBounds:
                ClaimedResidence res1 = plugin.getResidenceManager().getByLoc(this.getBaseLoc2());
                if (res1 != null) {
                    CuboidArea area = res1.getAreaByLoc(this.getBaseLoc2());
                    if (area != null) {
                        return area.getHighVector().getBlockY();
                    }
                }
                break;
            }
            return getMaxWorldHeight(getWorld());
        }

        public int getMinYAllowed() {
            switch (getSelectionRestrictions()) {
            case ignoreY:
            case noLimits:
            default:
                try {
                    return CMIWorld.getMinHeight(this.getBaseLoc1().getWorld());
                } catch (Throwable e) {
                    return 0;
                }
            case residenceBounds:
                ClaimedResidence res1 = plugin.getResidenceManager().getByLoc(this.getBaseLoc1());
                if (res1 != null) {
                    CuboidArea area = res1.getAreaByLoc(getBaseLoc1());
                    if (area != null)
                        return area.getLowVector().getBlockY();
                }
                break;
            }
            try {
                return CMIWorld.getMinHeight(this.getBaseLoc1().getWorld());
            } catch (Throwable e) {
                return 0;
            }
        }

        private boolean inSameResidence() {
            if (!hasPlacedBoth())
                return false;

            ClaimedResidence res1 = plugin.getResidenceManager().getByLoc(this.getBaseLoc1());

            if (res1 == null)
                return false;

            ClaimedResidence res2 = plugin.getResidenceManager().getByLoc(this.getBaseLoc2());

            if (res2 == null)
                return false;
            return res1.getName().equals(res2.getName()) || res1.getTopParent().equals(res2.getTopParent());
        }

        public void vert(boolean resadmin) {
            if (hasPlacedBoth()) {
                sky(resadmin);
                bedrock(resadmin);
            } else {
                lm.Select_Points.sendMessage(player);
            }
        }

        private void shadowSky(CMIBlockWorldArea area) {
            if (!hasPlacedBoth())
                return;
            area.getHighPoint().setY(this.getMaxYAllowed());
        }

        private void shadowBedrock(CMIBlockWorldArea area) {
            if (!hasPlacedBoth())
                return;
            area.getLowPoint().setY(this.getMinYAllowed());
        }

        @Deprecated
        private void shadowSky(CuboidArea area) {
            if (!hasPlacedBoth())
                return;
            area.setHighLocation(this.getBaseArea().getHighLocation());
            area.getHighVector().setY(this.getMaxYAllowed());
        }

        @Deprecated
        private void shadowBedrock(CuboidArea area) {
            if (!hasPlacedBoth())
                return;
            area.setLowLocation(this.getBaseArea().getLowLocation());
            area.getLowVector().setY(this.getMinYAllowed());
        }

        public void sky(boolean resadmin) {
            if (hasPlacedBoth()) {
                ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(this.getPlayer());
                PermissionGroup group = rPlayer.getGroup();

                CuboidArea base = this.getBaseArea();

                int selectedLowY = base.getLowVector().getBlockY();

                int newy = this.getMaxYAllowed();

                if (!resadmin) {
                    if (group.getHighestYAllowed() < newy)
                        newy = group.getHighestYAllowed();

                    if (Math.abs(newy - selectedLowY) > (group.getMaxYSize() - 1))
                        newy = selectedLowY + (group.getMaxYSize() - 1);
                }

                this.setBaseLoc1(base.getLowLocation());
                Location l2 = base.getHighLocation();
                l2.setY(newy);
                this.setBaseLoc2(l2);

                lm.Select_Sky.sendMessage(player);
            } else {
                lm.Select_Points.sendMessage(player);
            }
        }

        public void bedrock(boolean resadmin) {
            if (hasPlacedBoth()) {
                ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(this.getPlayer());
                PermissionGroup group = rPlayer.getGroup();

                CuboidArea base = this.getBaseArea();

                int selectedHighY = base.getHighVector().getBlockY();

                int newy = this.getMinYAllowed();
                if (!resadmin) {

                    if (newy < group.getLowestYAllowed())
                        newy = group.getLowestYAllowed();

                    if (Math.abs(selectedHighY - newy) > (group.getMaxYSize() - 1)) {
                        newy = selectedHighY - (group.getMaxYSize() - 1);
                    }
                }

                Location l1 = base.getLowLocation();
                l1.setY(newy);
                this.setBaseLoc1(l1);
                this.setBaseLoc2(base.getHighLocation());

                lm.Select_Bedrock.sendMessage(player);
            } else {
                lm.Select_Points.sendMessage(player);
            }
        }

        public void selectChunk() {
            Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
            int xcoord = chunk.getX() * 16;
            int zcoord = chunk.getZ() * 16;
            int xmax = xcoord + 15;
            int zmax = zcoord + 15;

            this.setBaseLoc1(new Location(player.getWorld(), xcoord, this.getMinYAllowed(), zcoord));
            this.setBaseLoc2(new Location(player.getWorld(), xmax, this.getMaxYAllowed(), zmax));
            lm.Select_Success.sendMessage(player);
        }

        public boolean hasPlacedBoth() {
            return this.getBaseLoc1() != null && this.getBaseLoc2() != null;
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        public CMIBlockWorldArea getOriginalArea() {
            if (!this.hasPlacedBoth())
                return null;

            if (area == null) {
                area = new CMIBlockWorldArea(this.loc1, this.loc2);
            }

            return area;
        }

        @Deprecated
        public CuboidArea getBaseArea() {
            if (!this.hasPlacedBoth())
                return null;

            return new CuboidArea(this.loc1, this.loc2);
        }

        @Deprecated
        public CuboidArea getResizedArea() {

            CuboidArea area = this.getBaseArea();

            switch (getSelectionRestrictions()) {
            case noLimits:
                break;
            case residenceBounds:
            case ignoreY:
                shadowSky(area);
                shadowBedrock(area);
                break;
            default:
                break;
            }

            return area;
        }

        public CMIBlockWorldArea getUpdatedArea() {

            CMIBlockWorldArea area = this.getOriginalArea();

            switch (getSelectionRestrictions()) {
            case noLimits:
                break;
            case residenceBounds:
            case ignoreY:
                shadowSky(area);
                shadowBedrock(area);

                break;
            default:
                break;
            }

            return area;
        }
    }

    private int getMaxWorldHeight(World world) {
        if (world == null)
            return 319;
        switch (world.getEnvironment()) {
        case NETHER:
            return SelectionManager.getNetherHeight();
        case NORMAL:
        case THE_END:

            try {
                return CMIWorld.getMaxHeight(world) - 1;
            } catch (Throwable e) {
                return 319;
            }
        default:
            break;
        }

        try {
            return CMIWorld.getMaxHeight(world) - 1;
        } catch (Throwable e) {
            return 319;
        }
    }

    public enum Direction {
        UP, DOWN, PLUSX, PLUSZ, MINUSX, MINUSZ;

        public Direction getOpposite() {
            switch (this) {
            case DOWN:
                return UP;
            case MINUSX:
                return PLUSX;
            case MINUSZ:
                return PLUSZ;
            case PLUSX:
                return MINUSX;
            case PLUSZ:
                return MINUSZ;
            case UP:
                return DOWN;
            default:
                break;
            }
            return this;
        }
    }

    public SelectionManager(Server server, Residence plugin) {
        this.plugin = plugin;
        this.server = server;
        selections = Collections.synchronizedMap(new HashMap<UUID, Selection>());
    }

    public Selection getSelection(Player player) {
        Selection s = selections.get(player.getUniqueId());
        if (s == null) {
            s = new Selection(player);
            selections.put(player.getUniqueId(), s);
        }
        return s;
    }

    public Selection clearSelection(Player player) {
        Selection s = selections.remove(player.getUniqueId());
        if (s != null) {
            Visualizer visuals = vMap.remove(player.getUniqueId());
            if (visuals != null) {
                visuals.clearRecords();
            }
        }

        CuboidDisplayManager.removeAll(player);
        return s;
    }

    public void updateLocations(Player player) {
        Selection s = selections.get(player.getUniqueId());
        if (s != null) {
            updateLocations(player, this.getSelection(player).getBaseLoc1(), this.getSelection(player).getBaseLoc2(), true);
        }
    }

    public void updateSelection(Player player, Location loc, boolean shiftFace) {
        CMIBlockWorldArea area = getSelectionArea(player);

        if (area == null) {
            plugin.getSelectionManager().placeLoc(player, loc, loc, true);
            return;
        }

        CMIVectorInt3D vLow = area.getLowPoint().clone();
        CMIVectorInt3D vHigh = area.getHighPoint().clone();
        CMIVectorInt3D vBlock = new CMIVectorInt3D(loc);

        int x = vBlock.getX();
        int y = vBlock.getY();
        int z = vBlock.getZ();

        int lowX = vLow.getX();
        int lowY = vLow.getY();
        int lowZ = vLow.getZ();

        int highX = vHigh.getX();
        int highY = vHigh.getY();
        int highZ = vHigh.getZ();

        boolean outsideX = x < lowX || x > highX;
        boolean outsideY = y < lowY || y > highY;
        boolean outsideZ = z < lowZ || z > highZ;

        if (shiftFace) {
            if (outsideX || outsideY || outsideZ) {
                // Outside: expand the cuboid to include the location.
                vLow.setX(Math.min(lowX, x));
                vLow.setY(Math.min(lowY, y));
                vLow.setZ(Math.min(lowZ, z));

                vHigh.setX(Math.max(highX, x));
                vHigh.setY(Math.max(highY, y));
                vHigh.setZ(Math.max(highZ, z));
            } else {
                // Distances from the target to all six faces.
                int[] distances = {
                        x - lowX, // 0: low X
                        highX - x, // 1: high X
                        y - lowY, // 2: low Y
                        highY - y, // 3: high Y
                        z - lowZ, // 4: low Z
                        highZ - z // 5: high Z
                };

                // Find the closest face that the target is not already touching.
                // Find the closest eligible face, preferring side faces on ties.
                int selectedFace = -1;
                int minDistance = Integer.MAX_VALUE;

                for (int i = 0; i < distances.length; i++) {
                    int distance = distances[i];

                    // Ignore faces the target is already touching.
                    if (distance <= 0) {
                        continue;
                    }

                    boolean isSideFace = i == 0 || i == 1 || i == 4 || i == 5;
                    boolean selectedIsSideFace = selectedFace == 0 || selectedFace == 1
                            || selectedFace == 4 || selectedFace == 5;

                    if (distance < minDistance
                            || (distance == minDistance
                                    && isSideFace
                                    && !selectedIsSideFace)) {
                        minDistance = distance;
                        selectedFace = i;
                    }
                }
                // Move exactly one face.
                switch (selectedFace) {
                case 0:
                    vLow.setX(x);
                    break;
                case 1:
                    vHigh.setX(x);
                    break;
                case 2:
                    vLow.setY(y);
                    break;
                case 3:
                    vHigh.setY(y);
                    break;
                case 4:
                    vLow.setZ(z);
                    break;
                case 5:
                    vHigh.setZ(z);
                    break;
                default:
                    // No eligible face.
                    break;
                }
            }
        } else {
            if (outsideX || outsideY || outsideZ) {
                // Outside: expand only the bounds necessary to include the location.
                if (x < lowX) {
                    vLow.setX(x);
                } else if (x > highX) {
                    vHigh.setX(x);
                }

                if (y < lowY) {
                    vLow.setY(y);
                } else if (y > highY) {
                    vHigh.setY(y);
                }

                if (z < lowZ) {
                    vLow.setZ(z);
                } else if (z > highZ) {
                    vHigh.setZ(z);
                }
            } else {
                // Inside: move every face tied for the furthest distance.
                int[] distances = {
                        x - lowX,
                        highX - x,
                        y - lowY,
                        highY - y,
                        z - lowZ,
                        highZ - z
                };

                int maxDistance = distances[0];

                for (int i = 1; i < distances.length; i++) {
                    if (distances[i] > maxDistance) {
                        maxDistance = distances[i];
                    }
                }

                if (distances[0] == maxDistance) {
                    vLow.setX(x);
                }

                if (distances[1] == maxDistance) {
                    vHigh.setX(x);
                }

                if (distances[2] == maxDistance) {
                    vLow.setY(y);
                }

                if (distances[3] == maxDistance) {
                    vHigh.setY(y);
                }

                if (distances[4] == maxDistance) {
                    vLow.setZ(z);
                }

                if (distances[5] == maxDistance) {
                    vHigh.setZ(z);
                }
            }
        }

        @NotNull
        selectorOptions combo = plugin.getComboSelectionManager().getOrCreate(player);

        if (combo.isGroupLimit()) {
            CMIBlockWorldArea updated = ComboSelection.constrainSelection(area, new CMIBlockWorldArea(vLow.toLocation(player.getWorld()), vHigh.toLocation(player.getWorld())), ResidencePlayer.get(player));
            vLow = updated.getLowPoint();
            vHigh = updated.getHighPoint();
        }

        updateLocations(
                player,
                vLow.toLocation(player.getWorld()),
                vHigh.toLocation(player.getWorld()));
    }

    public void updateLocations(Player player, Location loc1, Location loc2) {
        updateLocations(player, loc1, loc2, false);
    }

    public void updateLocations(Player player, Location loc1, Location loc2, boolean force) {
        Selection selection = getSelection(player);
        if (loc1 != null)
            selection.setBaseLoc1(loc1);
        if (loc2 != null)
            selection.setBaseLoc2(loc2);
        this.afterSelectionUpdate(player, force);
    }

    public void placeLoc(Player player, Location loc1, Location loc2, boolean show) {
        placeLoc1(player, loc1, false);
        placeLoc2(player, loc2, show);
    }

    public void placeLoc1(Player player, Location loc) {
        placeLoc1(player, loc, false);
    }

    public void placeLoc1(Player player, Location loc, boolean show) {
        if (loc == null)
            return;

        getSelection(player).setBaseLoc1(loc);
        if (show) {
            this.afterSelectionUpdate(player);
        }
    }

    public void placeLoc2(Player player, Location loc) {
        placeLoc2(player, loc, false);
    }

    public void placeLoc2(Player player, Location loc, boolean show) {
        if (loc == null)
            return;

        getSelection(player).setBaseLoc2(loc);
        if (show) {
            this.afterSelectionUpdate(player);
        }
    }

    public void afterSelectionUpdate(Player player) {
        afterSelectionUpdate(player, false);
    }

    public void afterSelectionUpdate(Player player, boolean force) {
        if (!this.hasPlacedBoth(player))
            return;

        Visualizer v = vMap.get(player.getUniqueId());
        if (v == null) {
            v = new Visualizer(player);
            vMap.put(player.getUniqueId(), v);
        }
        v.setStart(System.currentTimeMillis());

        v.cancelAll();
        if (force)
            v.setLoc(null);
        v.setAreas(CuboidDisplayType.SELECTION, this.getSelectionArea(player));
        v.setOnce(false);

        this.showBounds(player, v);
    }

    public Location getPlayerLoc1(Player player) {
        if (player == null)
            return null;
        Selection sel = getSelection(player);
        if (sel == null)
            return null;
        CuboidArea area = sel.getResizedArea();
        if (area == null)
            return null;
        return area.getLowLocation();
    }

    @Deprecated
    public Location getPlayerLoc1(String player) {
        return getPlayerLoc1(Bukkit.getPlayer(player));
    }

    public Location getPlayerLoc2(Player player) {
        if (player == null)
            return null;
        Selection sel = getSelection(player);
        if (sel == null)
            return null;
        CuboidArea area = sel.getResizedArea();
        if (area == null)
            return null;
        return area.getHighLocation();
    }

    @Deprecated
    public Location getPlayerLoc2(String player) {
        return getPlayerLoc2(Bukkit.getPlayer(player));
    }

    @Deprecated
    public CuboidArea getSelectionCuboid(Player player) {
        if (player == null)
            return null;
        return getSelection(player).getResizedArea();
    }

    public CMIBlockWorldArea getSelectionArea(Player player) {
        if (player == null)
            return null;
        return getSelection(player).getUpdatedArea();
    }

    @Deprecated
    public CuboidArea getSelectionCuboid(String player) {
        if (!hasPlacedBoth(player))
            return null;
        return getSelectionCuboid(Bukkit.getPlayer(player));
    }

    public boolean hasPlacedBoth(Player player) {
        if (player == null)
            return false;
        return getSelection(player).hasPlacedBoth();
    }

    @Deprecated
    public boolean hasPlacedBoth(String player) {
        return hasPlacedBoth(Bukkit.getPlayer(player));
    }

    public void showSelectionInfoInActionBar(Player player) {

        if (!plugin.getConfigManager().useActionBarOnSelection())
            return;

        CMIBlockWorldArea cuboidArea = this.getSelectionArea(player);

        String message = lm.Select_TotalSize.getMessage(cuboidArea.getSize());

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (plugin.getConfigManager().enableEconomy())
            message += " " + lm.General_LandCost.getMessage(plugin.getEconomyManager().format(group.getCost(cuboidArea.getSize())));
        CMIActionBar.send(player, message);
    }

    public void showSelectionInfo(Player player) {
        if (hasPlacedBoth(player)) {
            lm.General_Separator.sendMessage(player);
            CMIBlockWorldArea cuboidArea = this.getSelectionArea(player);
            lm.Select_TotalSize.sendMessage(player, cuboidArea.getSize());

            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();

            if (plugin.getConfigManager().enableEconomy())
                lm.General_LandCost.sendMessage(player, plugin.getEconomyManager().format(group.getCost(cuboidArea.getSize())));
            lm.showMessage(player, CMIChatColor.YELLOW + "X" + lm.General_Size.getMessage(cuboidArea.getXSize()));
            lm.showMessage(player, CMIChatColor.YELLOW + "Y" + lm.General_Size.getMessage(cuboidArea.getYSize()));
            lm.showMessage(player, CMIChatColor.YELLOW + "Z" + lm.General_Size.getMessage(cuboidArea.getZSize()));
            lm.General_Separator.sendMessage(player);
            Visualizer v = new Visualizer(player);
            v.setAreas(CuboidDisplayType.SELECTION, this.getSelectionArea(player));
            this.showBounds(player, v);
        } else
            lm.Select_Points.sendMessage(player);
    }

    public void showBounds(final Player player, final Visualizer v) {

        if (!VisualizerConfig.isShow())
            return;

        CMIScheduler.runTask(Residence.getInstance(), () -> {

            // Only firing selection event if its selection and not one time showing. Check
            // can be removed on later builds and plugins can check if this is short
            // visualization
            if (!v.isOnce()) {
                ResidenceSelectionVisualizationEvent ev = new ResidenceSelectionVisualizationEvent(player, v.getAreas(), v.getErrorAreas(), v.isOnce());
                Bukkit.getPluginManager().callEvent(ev);

                if (ev.isCancelled()) {
                    return;
                }
            }

            if (VisualizerConfig.isUseModernVersion()) {
                if (v.isOnce()) {

                    List<CMIBlockWorldArea> selection = v.getAreas(CuboidDisplayType.SELECTION);

                    CuboidDisplayManager.add(player,
                            new ArrayList<>(selection.isEmpty() ? v.getAreas(CuboidDisplayType.ERROR) : selection),
                            selection.isEmpty() ? CuboidDisplayType.BOUNCE : CuboidDisplayType.ENTER_EXIT);
                } else {
                    CuboidDisplayManager.show(player, v.getAreas(CuboidDisplayType.SELECTION), v.getAreas(CuboidDisplayType.ERROR));
                }
                return;
            }

            Visualizer tv = vMap.get(player.getUniqueId());
            if (tv != null) {
                tv.cancelAll();
            }

            vMap.put(player.getUniqueId(), v);
            if (!plugin.isEnabled())
                return;

            v.setScheduler(CMIScheduler.runTimerAsync(Residence.getInstance(), () -> {

                boolean result = true;
                if (!v.getAreas(CuboidDisplayType.SELECTION).isEmpty())
                    result = makeBorders(player, false);

                if (!v.getAreas(CuboidDisplayType.ERROR).isEmpty())
                    result = makeBorders(player, true) && result ? true : result;

                if (v.getStart() + VisualizerConfig.getShowForMs() < System.currentTimeMillis() || !result || v.isOnce()) {
                    v.cancelAll();
                }

            }, 0, 1L)

            );

        });
    }

    private List<Location> getLocations(Location lowLoc, Location loc, Double TX, Double TY, Double TZ, Double Range, boolean StartFromZero) {

        double eachCollumn = VisualizerConfig.getCollumnSpacing();
        double eachRow = VisualizerConfig.getRowSpacing();

        if (TX == 0D)
            TX = eachCollumn + eachCollumn * 0.1;
        if (TY == 0D)
            TY = eachRow + eachRow * 0.1;
        if (TZ == 0D)
            TZ = eachCollumn + eachCollumn * 0.1;

        double CollumnStart = eachCollumn;
        double RowStart = eachRow;

        if (StartFromZero) {
            CollumnStart = 0;
            RowStart = 0;
        }

        List<Location> locList = new ArrayList<Location>();

        if (lowLoc.getWorld() != loc.getWorld())
            return locList;

        for (double x = CollumnStart; x < TX; x += eachCollumn) {
            Location CurrentX = lowLoc.clone();
            if (TX > eachCollumn + eachCollumn * 0.1)
                CurrentX.add(x, 0, 0);
            for (double y = RowStart; y < TY; y += eachRow) {
                Location CurrentY = CurrentX.clone();
                if (TY > eachRow + eachRow * 0.1)
                    CurrentY.add(0, y, 0);
                for (double z = CollumnStart; z < TZ; z += eachCollumn) {
                    Location CurrentZ = CurrentY.clone();
                    if (TZ > eachCollumn + eachCollumn * 0.1)
                        CurrentZ.add(0, 0, z);
                    double dist = loc.distance(CurrentZ);
                    if (dist < Range)
                        locList.add(CurrentZ.clone());
                }
            }
        }

        return locList;
    }

    private List<Location> getLocationsWallsByData(Location loc, Double TX, Double TY, Double TZ, Location lowLoc, SelectionSides Sides, double Range) {
        List<Location> locList = new ArrayList<Location>();

        // North wall
        if (Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, TY, 0D, Range, false));

        // South wall
        if (Sides.ShowSouthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), TX, TY, 0D, Range, false));

        // West wall
        if (Sides.ShowWestSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), 0D, TY, TZ, Range, false));

        // East wall
        if (Sides.ShowEastSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, TY, TZ, Range, false));

        // Roof wall
        if (Sides.ShowTopSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), TX, 0D, TZ, Range, false));

        // Ground wall
        if (Sides.ShowBottomSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, 0D, TZ, Range, false));

        return locList;
    }

    private List<Location> getLocationsCornersByData(Location loc, Double TX, Double TY, Double TZ, Location lowLoc, SelectionSides Sides, double Range) {
        List<Location> locList = new ArrayList<Location>();

        // North bottom line
        if (Sides.ShowBottomSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, 0D, 0D, Range, true));

        // North top line
        if (Sides.ShowTopSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), TX, 0D, 0D, Range, true));

        // South bottom line
        if (Sides.ShowBottomSide() && Sides.ShowSouthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), TX, 0D, 0D, Range, true));

        // South top line
        if (Sides.ShowTopSide() && Sides.ShowSouthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, TZ), loc.clone(), TX, 0D, 0D, Range, true));

        // North - West corner
        if (Sides.ShowWestSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, 0), loc.clone(), 0D, TY, 0D, Range, true));

        // North - East corner
        if (Sides.ShowEastSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, TY, 0D, Range, true));

        // South - West corner
        if (Sides.ShowSouthSide() && Sides.ShowWestSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), 0D, TY, 0D, Range, true));

        // South - East corner
        if (Sides.ShowSouthSide() && Sides.ShowEastSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, TZ), loc.clone(), 0D, TY + 1, 0D, Range, true));

        // West bottom corner
        if (Sides.ShowWestSide() && Sides.ShowBottomSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        // East bottom corner
        if (Sides.ShowEastSide() && Sides.ShowBottomSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        // West top corner
        if (Sides.ShowWestSide() && Sides.ShowTopSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        // East top corner
        if (Sides.ShowEastSide() && Sides.ShowTopSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, TY, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        return locList;
    }

    private boolean makeBorders(final Player player, final boolean error) {

        final Visualizer v = vMap.get(player.getUniqueId());

        if (v == null)
            return false;

        List<CMIBlockWorldArea> areas = null;

        if (!error)
            areas = v.getAreas(CuboidDisplayType.SELECTION);
        else
            areas = v.getAreas(CuboidDisplayType.ERROR);

        Location loc = player.getLocation();
        int range = VisualizerConfig.getRange();

        final List<Location> locList = new ArrayList<Location>();
        final List<Location> errorLocList = new ArrayList<Location>();

        final boolean same = v.isSameLoc();
        if (!same) {
            for (CMIBlockWorldArea area : areas) {
                if (area == null)
                    continue;

                CMIBlockWorldArea cuboidArea = area.clone();
                cuboidArea.getHighPoint().add(new CMIVectorInt3D(1, 1, 1));

                SelectionSides Sides = new SelectionSides();

                int PLLX = loc.getBlockX() - range;
                int PLLZ = loc.getBlockZ() - range;
                int PLLY = loc.getBlockY() - range;
                int PLHX = loc.getBlockX() + range;
                int PLHZ = loc.getBlockZ() + range;
                int PLHY = loc.getBlockY() + range;

                if (cuboidArea.getLowPoint().getX() < PLLX) {
                    cuboidArea.getLowPoint().setX(PLLX);
                    Sides.setWestSide(false);
                }

                if (cuboidArea.getHighPoint().getX() > PLHX) {
                    cuboidArea.getHighPoint().setX(PLHX);
                    Sides.setEastSide(false);
                }

                if (cuboidArea.getLowPoint().getZ() < PLLZ) {
                    cuboidArea.getLowPoint().setZ(PLLZ);
                    Sides.setNorthSide(false);
                }

                if (cuboidArea.getHighPoint().getZ() > PLHZ) {
                    cuboidArea.getHighPoint().setZ(PLHZ);
                    Sides.setSouthSide(false);
                }

                if (cuboidArea.getLowPoint().getY() < PLLY) {
                    cuboidArea.getLowPoint().setY(PLLY);
                    Sides.setBottomSide(false);
                }

                if (cuboidArea.getHighPoint().getY() > PLHY) {
                    cuboidArea.getHighPoint().setY(PLHY);
                    Sides.setTopSide(false);
                }

                double TX = cuboidArea.getXSize() - 1;
                double TY = cuboidArea.getYSize() - 1;
                double TZ = cuboidArea.getZSize() - 1;

                locList.addAll(getLocationsWallsByData(loc, TX, TY, TZ, cuboidArea.getLowPoint().toLocation(cuboidArea.getWorld().getWorld()).clone(), Sides, range));
                errorLocList.addAll(getLocationsCornersByData(loc, TX, TY, TZ, cuboidArea.getLowPoint().toLocation(cuboidArea.getWorld().getWorld()).clone(), Sides, range));
            }
            v.setLoc(player.getLocation());
        } else {
            if (error) {
                locList.addAll(v.getPrimaryLocations(CuboidDisplayType.ERROR));
                errorLocList.addAll(v.getSecondaryLocations(CuboidDisplayType.ERROR));
            } else {
                locList.addAll(v.getPrimaryLocations(CuboidDisplayType.SELECTION));
                errorLocList.addAll(v.getSecondaryLocations(CuboidDisplayType.SELECTION));
            }
        }

        if (!plugin.isEnabled())
            return false;

        int size = locList.size();
        int errorSize = errorLocList.size();

        int timesMore = 1;
        int errorTimesMore = 1;

        if (size > VisualizerConfig.getSidesCap()) {
            timesMore = size / VisualizerConfig.getSidesCap() + 1;
        }
        if (errorSize > VisualizerConfig.getFrameCap()) {
            errorTimesMore = errorSize / VisualizerConfig.getFrameCap() + 1;
        }

        v.addCurrentSkip();
        if (v.getCurrentSkip() > VisualizerConfig.getSkipBy())
            v.setCurrentSkip(1);

        try {
            showParticles(locList, player, timesMore, error, true, v);
            showParticles(errorLocList, player, errorTimesMore, error, false, v);
        } catch (Exception e) {
            return false;
        }

        if (error) {
            v.setPrimaryLocations(CuboidDisplayType.ERROR, locList);
            v.setSecondaryLocations(CuboidDisplayType.ERROR, errorLocList);
        } else {
            v.setPrimaryLocations(CuboidDisplayType.SELECTION, locList);
            v.setSecondaryLocations(CuboidDisplayType.SELECTION, errorLocList);
        }

        return true;
    }

    private void showParticles(List<Location> locList, Player player, int timesMore, boolean error, boolean sides, Visualizer v) {
        int s = 0;

        for (int i = 0; i < locList.size(); i += timesMore) {

            if (!v.isOnce()) {
                s++;

                if (s > VisualizerConfig.getSkipBy())
                    s = 1;

                if (s != v.getCurrentSkip())
                    continue;
            }

            Location l = locList.get(i);

            CMIEffect effect = null;
            if (sides) {
                effect = error ? VisualizerConfig.getErrorEffect().getSides() : VisualizerConfig.getBaseEffect().getSides();
            } else {
                effect = error ? VisualizerConfig.getErrorEffect().getFrame() : VisualizerConfig.getBaseEffect().getFrame();
            }

            effect.show(player, l);
        }
    }

    public void vert(Player player, boolean resadmin) {
        if (hasPlacedBoth(player)) {
            this.sky(player, resadmin);
            this.bedrock(player, resadmin);
        } else {
            lm.Select_Points.sendMessage(player);
        }
    }

    public void sky(Player player, boolean resadmin) {
        Selection selection = this.getSelection(player);
        selection.sky(resadmin);
    }

    public void bedrock(Player player, boolean resadmin) {
        Selection selection = this.getSelection(player);
        selection.bedrock(resadmin);
    }

    @Deprecated
    public void selectChunk(Player player) {
        Selection selection = this.getSelection(player);
        selection.selectChunk();
    }

    public boolean worldEdit(Player player) {
        lm.General_WorldEditNotFound.sendMessage(player);
        return false;
    }

    public boolean worldEditUpdate(Player player) {
        lm.General_WorldEditNotFound.sendMessage(player);
        return false;
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
        Location myloc = player.getLocation();
        Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
        Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);

        CuboidArea area = new CuboidArea(loc1, loc2);

        placeLoc1(player, loc1, false);
        placeLoc2(player, loc2, false);

        Selection selection = this.getSelection(player);

        if (selection.getMaxYAllowed() < area.getHighVector().getBlockY())
            selection.getBaseLoc2().setY(selection.getMaxYAllowed());

        if (selection.getMinYAllowed() > area.getLowVector().getBlockY())
            selection.getBaseLoc1().setY(selection.getMinYAllowed());

//	selection.updateBaseArea();
//	selection.updateShadowLocations();
//	selection.updateShadowArea();

        this.afterSelectionUpdate(player);
        lm.Select_Success.sendMessage(player);
        showSelectionInfo(player);
    }

    @Deprecated
    public void modify(Player player, boolean shift, double amount) {
        modify(player, shift, (int) amount);
    }

    public void modify(Player player, boolean shift, int amount) {
        if (!hasPlacedBoth(player)) {
            lm.Select_Points.sendMessage(player);
            return;
        }
        Direction d = getDirection(player);
        if (d == null) {
            lm.Invalid_Direction.sendMessage(player);
            return;
        }
        CMIBlockWorldArea area = this.getSelectionArea(player);

        int MIN_HEIGHT = CMIWorld.getMinHeight(area.getWorld());

        switch (d) {
        case DOWN:
            int oldy = area.getLowPoint().getY() - amount;

            if (oldy < MIN_HEIGHT) {
                lm.Select_TooLow.sendMessage(player);
                oldy = MIN_HEIGHT;
            }
            area.getLowPoint().setY(oldy);
            if (shift) {
                area.getHighPoint().setY(area.getHighPoint().getY() - amount);
                lm.Shifting_Down.sendMessage(player, amount);
            } else
                lm.Expanding_Down.sendMessage(player, amount);
            break;
        case MINUSX:
            area.getLowPoint().setX(area.getLowPoint().getX() - amount);
            if (shift) {
                int oldx2 = area.getHighPoint().getX();
                oldx2 = oldx2 - amount;
                area.getHighPoint().setX(oldx2);
                lm.Shifting_West.sendMessage(player, amount);
            } else
                lm.Expanding_West.sendMessage(player, amount);
            break;
        case MINUSZ:
            area.getLowPoint().setZ(area.getLowPoint().getZ() - amount);
            if (shift) {
                area.getHighPoint().setZ(area.getHighPoint().getZ() - amount);
                lm.Shifting_North.sendMessage(player, amount);
            } else
                lm.Expanding_North.sendMessage(player, amount);
            break;
        case PLUSX:
            area.getHighPoint().setX(area.getHighPoint().getX() + amount);
            if (shift) {
                area.getLowPoint().setX(area.getLowPoint().getX() + amount);
                lm.Shifting_East.sendMessage(player, amount);
            } else
                lm.Expanding_East.sendMessage(player, amount);
            break;
        case PLUSZ:
            area.getHighPoint().setZ(area.getHighPoint().getZ() + amount);
            if (shift) {
                area.getLowPoint().setZ(area.getLowPoint().getZ() + amount);
                lm.Shifting_South.sendMessage(player, amount);
            } else
                lm.Expanding_South.sendMessage(player, amount);
            break;
        case UP:
            oldy = area.getHighPoint().getY() + amount;
            if (oldy > getMaxWorldHeight(player.getLocation().getWorld())) {
                lm.Select_TooHigh.sendMessage(player);
                oldy = getMaxWorldHeight(player.getLocation().getWorld());
            }
            area.getHighPoint().setY(oldy);
            if (shift) {
                area.getLowPoint().setY(area.getLowPoint().getY() + amount);
                lm.Shifting_Up.sendMessage(player, amount);
            } else
                lm.Expanding_Up.sendMessage(player, amount);
            break;
        default:
            break;
        }

        updateLocations(player, area.getHighPoint().toLocation(player.getWorld()), area.getLowPoint().toLocation(player.getWorld()), true);
    }

    public CMIBlockWorldArea resize(CMIBlockWorldArea area, Player player, boolean shift, int amount) {
        if (!hasPlacedBoth(player)) {
            return area;
        }
        Direction d = getDirection(player);
        if (d == null) {
            return area;
        }

        int MIN_HEIGHT = CMIWorld.getMinHeight(area.getWorld());

        switch (d) {
        case DOWN:
            int oldy = area.getLowPoint().getY() - amount;

            if (oldy < MIN_HEIGHT) {
                oldy = MIN_HEIGHT;
            }
            area.getLowPoint().setY(oldy);
            if (shift) {
                area.getHighPoint().setY(area.getHighPoint().getY() - amount);
            }
            break;
        case MINUSX:
            area.getLowPoint().setX(area.getLowPoint().getX() - amount);
            if (shift) {
                int oldx2 = area.getHighPoint().getX();
                oldx2 = oldx2 - amount;
                area.getHighPoint().setX(oldx2);
            }
            break;
        case MINUSZ:
            area.getLowPoint().setZ(area.getLowPoint().getZ() - amount);
            if (shift) {
                area.getHighPoint().setZ(area.getHighPoint().getZ() - amount);
            }
            break;
        case PLUSX:
            area.getHighPoint().setX(area.getHighPoint().getX() + amount);
            if (shift) {
                area.getLowPoint().setX(area.getLowPoint().getX() + amount);
            }
            break;
        case PLUSZ:
            area.getHighPoint().setZ(area.getHighPoint().getZ() + amount);
            if (shift) {
                area.getLowPoint().setZ(area.getLowPoint().getZ() + amount);
            }
            break;
        case UP:
            oldy = area.getHighPoint().getY() + amount;
            if (oldy > getMaxWorldHeight(player.getLocation().getWorld())) {
                oldy = getMaxWorldHeight(player.getLocation().getWorld());
            }
            area.getHighPoint().setY(oldy);
            if (shift) {
                area.getLowPoint().setY(area.getLowPoint().getY() + amount);
            }
            break;
        default:
            break;
        }

        return area;
    }

    @Deprecated
    public boolean contract(Player player, double amount) {
        return contract(player, amount, false);
    }

    @Deprecated
    public boolean contract(Player player, double amount, @SuppressWarnings("unused") @Deprecated boolean resadmin) {
        return contract(player, (int) amount);
    }

    public boolean contract(Player player, int amount) {
        if (!hasPlacedBoth(player)) {
            lm.Select_Points.sendMessage(player);
            return false;
        }
        Direction d = getDirection(player);
        if (d == null) {
            lm.Invalid_Direction.sendMessage(player);
            return false;
        }
        d = d.getOpposite();

        CuboidArea area = this.getSelectionCuboid(player);
        switch (d) {
        case UP:
            double oldy = area.getHighVector().getBlockY() - amount;
            if (oldy > getMaxWorldHeight(player.getLocation().getWorld())) {
                lm.Select_TooHigh.sendMessage(player);
                oldy = getMaxWorldHeight(player.getLocation().getWorld());
            }
            area.getHighVector().setY(oldy);
            lm.Contracting_Down.sendMessage(player, amount);
            break;
        case PLUSX:
            area.getHighVector().setX(area.getHighVector().getBlockX() - amount);
            lm.Contracting_West.sendMessage(player, amount);
            break;
        case PLUSZ:
            area.getHighVector().setZ(area.getHighVector().getBlockZ() - amount);
            lm.Contracting_North.sendMessage(player, amount);
            break;
        case MINUSX:
            area.getLowVector().setX(area.getLowVector().getBlockX() + amount);
            lm.Contracting_East.sendMessage(player, amount);
            break;
        case MINUSZ:
            area.getLowVector().setZ(area.getLowVector().getBlockZ() + amount);
            lm.Contracting_South.sendMessage(player, amount);
            break;
        case DOWN:

            int MIN_HEIGHT = 0;
            try {
                MIN_HEIGHT = CMIWorld.getMinHeight(area.getWorld());
            } catch (Throwable e) {
            }
            oldy = area.getLowVector().getBlockY() + amount;

            if (oldy < MIN_HEIGHT) {
                lm.Select_TooLow.sendMessage(player);
                oldy = MIN_HEIGHT;
            }
            area.getLowVector().setY(oldy);
            lm.Contracting_Up.sendMessage(player, amount);
            break;
        default:
            break;
        }

//	if (!ClaimedResidence.isBiggerThanMinSubzone(player, area, resadmin))
//	    return false;

        updateLocations(player, area.getHighLocation(), area.getLowLocation(), true);
        return true;
    }

    private static Direction getDirection(Player player) {

        int yaw = (int) player.getLocation().getYaw();

        if (yaw < 0)
            yaw += 360;

        yaw += 45;
        yaw %= 360;

        int facing = yaw / 90;

        float pitch = player.getLocation().getPitch();
        if (pitch < -50)
            return Direction.UP;
        if (pitch > 50)
            return Direction.DOWN;
        if (facing == 1) // east
            return Direction.MINUSX;
        if (facing == 3) // west
            return Direction.PLUSX;
        if (facing == 2) // north
            return Direction.MINUSZ;
        if (facing == 0) // south
            return Direction.PLUSZ;
        return null;
    }

    public void regenerate(CuboidArea area) {
    }

    public void onDisable() {
        for (Entry<UUID, Visualizer> one : vMap.entrySet()) {
            one.getValue().cancelAll();
        }
        vMap.clear();
    }

    public static CMIMaterial getTool() {
        return tool;
    }

    public static void setTool(CMIMaterial tool) {
        SelectionManager.tool = tool;
    }

    public static boolean isIgnoreY() {
        return ignoreY;
    }

    public static void setIgnoreY(boolean ignoreY) {
        SelectionManager.ignoreY = ignoreY;
    }

    public static boolean isIgnoreYInSubzone() {
        return ignoreYInSubzone;
    }

    public static void setIgnoreYInSubzone(boolean ignoreYInSubzone) {
        SelectionManager.ignoreYInSubzone = ignoreYInSubzone;
    }

    public static int getNetherHeight() {
        return netherHeight;
    }

    public static void setNetherHeight(int netherHeight) {
        SelectionManager.netherHeight = netherHeight;
    }

    public static boolean isNoCostForYBlocks() {
        return noCostForYBlocks;
    }

    public static void setNoCostForYBlocks(boolean noCostForYBlocks) {
        SelectionManager.noCostForYBlocks = noCostForYBlocks;
    }

    public static boolean isWorldEditIntegration() {
        return worldEditIntegration;
    }

    public static void setWorldEditIntegration(boolean worldEditIntegration) {
        SelectionManager.worldEditIntegration = worldEditIntegration;
    }

    public static CMIMaterial getInfoTool() {
        return infoTool;
    }

    public static void setInfoTool(CMIMaterial infoTool) {
        SelectionManager.infoTool = infoTool;
    }

    public static boolean isSmartSelect() {
        return smartSelect;
    }

    public static void setSmartSelect(boolean smartSelect) {
        SelectionManager.smartSelect = smartSelect;
    }
}
