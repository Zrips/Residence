package com.bekvon.bukkit.residence.selection;

import org.bukkit.Effect;

import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayManager;
import com.bekvon.bukkit.residence.selectionVisuals.CuboidDisplayType;

import net.Zrips.CMILib.Container.CMINumber;
import net.Zrips.CMILib.Effects.CMIEffect;
import net.Zrips.CMILib.Effects.CMIEffectManager.CMIParticle;
import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.Version.Version;

public class VisualizerConfig {

    private static boolean show = true;
    private static int range = 16;
    private static int showForMs = 5000;
    private static int updateInterval = 20;

    private static double rowSpacing = 1D;
    private static double collumnSpacing = 1D;
    private static int skipBy = 2;

    private static int frameCap = 500;
    private static int sidesCap = 2000;

    private static SelectionEffect baseEffect = new SelectionEffect();
    private static SelectionEffect errorEffect = new SelectionEffect();

    private static boolean bounceAnimation;
    private static boolean enterAnimation;

    private static boolean useModernVersion = true;
    private static int gridSize = 8;
    private static int updateRateByTravel = 4;
    private static double lineThickness = 0.05;

    public static void loadConfig(ConfigReader c) {

        c.addComment("Global.Visualizer.Use", "With this enabled player will see particle effects to mark selection boundaries");
        setShow(c.get("Global.Visualizer.Use", true));

        if (Version.isCurrentEqualOrHigher(Version.v1_19_4)) {
            c.addComment("Global.Visualizer.Type", "Which type of visualization we should use. Particle or Modern.");
            setUseModernVersion(c.get("Global.Visualizer.Type", "Modern").equalsIgnoreCase("Modern"));
        } else {
            setUseModernVersion(false);
        }

        c.addComment("Global.Visualizer.Range", "Range in blocks to draw particle effects for player", "Keep it no more as 30, as player cant see more than 16 blocks");
        setRange(c.get("Global.Visualizer.Range", 16));

        c.addComment("Global.Visualizer.ShowFor", "For how long in miliseconds (5000 = 5sec) to show particle effects");
        setShowForMs(c.get("Global.Visualizer.ShowFor", 10000));

        c.addComment("Global.Visualizer.updateInterval", "How often in ticks to update particles for player");
        setUpdateInterval(c.get("Global.Visualizer.updateInterval", 5));

        c.addComment("Global.Visualizer.RowSpacing", "Spacing in blocks between particle effects for rows");
        setRowSpacing(c.get("Global.Visualizer.RowSpacing", 1D));
        if (getRowSpacing() < 1)
            setRowSpacing(1);
        c.addComment("Global.Visualizer.CollumnSpacing", "Spacing in blocks between particle effects for collums");
        setCollumnSpacing(c.get("Global.Visualizer.CollumnSpacing", 1D));
        if (getCollumnSpacing() < 1)
            setCollumnSpacing(1);

        c.addComment("Global.Visualizer.SkipBy", "Defines by how many particles we need to skip", "This will create moving particle effect and will improve overall look of selection",
                "By increasing this number, you can decrease update interval");
        setSkipBy(c.get("Global.Visualizer.SkipBy", 15));
        if (getSkipBy() < 1)
            setSkipBy(1);

        c.addComment("Global.Visualizer.FrameCap", "Maximum amount of frame particles to show for one player");
        setFrameCap(c.get("Global.Visualizer.FrameCap", 500));
        if (getFrameCap() < 1)
            setFrameCap(1);

        c.addComment("Global.Visualizer.SidesCap", "Maximum amount of sides particles to show for one player");
        setSidesCap(c.get("Global.Visualizer.SidesCap", 2000));
        if (getSidesCap() < 1)
            setSidesCap(1);

        StringBuilder effectsList = new StringBuilder();

        for (Effect one : Effect.values()) {
            if (one == null)
                continue;
            if (one.name() == null)
                continue;
            if (!effectsList.toString().isEmpty())
                effectsList.append(", ");
            effectsList.append(one.name().toLowerCase());
        }

        c.addComment("Global.Visualizer.Selected", "Particle effect names. possible: explode, largeexplode, hugeexplosion, fireworksSpark, splash, wake, crit, magicCrit",
                " smoke, largesmoke, spell, instantSpell, mobSpell, mobSpellAmbient, witchMagic, dripWater, dripLava, angryVillager, happyVillager, townaura",
                " note, portal, enchantmenttable, flame, lava, footstep, cloud, reddust, snowballpoof, snowshovel, slime, heart, barrier", " droplet, take, mobappearance", "",
                "If using spigot based server different particles can be used:", effectsList.toString());

        // Frame
        String efname = c.get("Global.Visualizer.Selected.Frame", "dust;125,150,150");
        CMIEffect selectedFrame = CMIEffect.deserialize(efname);
        if (selectedFrame == null) {
            selectedFrame = new CMIEffect(CMIParticle.DUST);
            selectedFrame.setColorFrom(org.bukkit.Color.fromRGB(125, 150, 150));
            lm.consoleMessage("Can't find effect for Selected Frame with this name, it was set to default");
        }
        getBaseEffect().setFrame(selectedFrame);

        // Sides
        efname = c.get("Global.Visualizer.Selected.Sides", "dust;150,255,200");
        CMIEffect selectedSides = CMIEffect.deserialize(efname);
        if (selectedSides == null) {
            selectedSides = new CMIEffect(CMIParticle.DUST);
            selectedFrame.setColorFrom(org.bukkit.Color.fromRGB(150, 255, 200));
            lm.consoleMessage("Can't find effect for Selected Sides with this name, it was set to default");
        }
        getBaseEffect().setSides(selectedSides);

        efname = c.get("Global.Visualizer.Overlap.Frame", "dust;255,0,255");
        CMIEffect overlapFrame = CMIEffect.deserialize(efname);
        if (overlapFrame == null) {
            overlapFrame = new CMIEffect(CMIParticle.DUST);
            selectedFrame.setColorFrom(org.bukkit.Color.fromRGB(250, 0, 255));
            lm.consoleMessage("Can't find effect for Overlap Frame with this name, it was set to default");
        }
        getErrorEffect().setFrame(overlapFrame);

        efname = c.get("Global.Visualizer.Overlap.Sides", "dust;255,100,100");
        CMIEffect overlapSides = CMIEffect.deserialize(efname);
        if (overlapSides == null) {
            overlapSides = new CMIEffect(CMIParticle.DUST);
            selectedFrame.setColorFrom(org.bukkit.Color.fromRGB(255, 100, 100));
            lm.consoleMessage("Can't find effect for Selected Sides with this name, it was set to default");
        }
        getErrorEffect().setSides(overlapSides);

        c.addComment("Global.Visualizer.EnterAnimation", "Shows particle effect when player enters residence. Only applies to main residence area");
        setEnterAnimation(c.get("Global.Visualizer.EnterAnimation", true));

        c.addComment("Global.Visualizer.BounceAnimation", "Shows particle effect when player are being pushed back");
        setBounceAnimation(c.get("Global.Visualizer.BounceAnimation", true));

        if (Version.isCurrentEqualOrHigher(Version.v1_19_4)) {

            c.addComment("Global.Visualizer.Modern.UpdateRateByTravel", "How many blocks does player need to travel before visual update is triggered",
                    "This can be set as low as 1, but its not recommended to use lower value. Default value: 4");
            setUpdateRateByTravel(c.get("Global.Visualizer.Modern.UpdateRateByTravel", 4));

            CuboidDisplayManager.updateVariables();

            c.addComment("Global.Visualizer.Modern.Materials." + CuboidDisplayType.values()[0].name() + ".Edges", "Only block type materials are accepted here.");
            c.addComment("Global.Visualizer.Modern.Materials." + CuboidDisplayType.values()[0].name() + ".Sides", "Only block type materials are accepted here.");
            c.addComment("Global.Visualizer.Modern.Materials." + CuboidDisplayType.values()[0].name() + ".Range", "Lowest value 1, highest 128", "Default: 32");
            c.addComment("Global.Visualizer.Modern.Materials." + CuboidDisplayType.values()[0].name() + ".GridSize",
                    "Lowest value 0.25, highest 64. ATENTION! Only use low values in combination with lower Range",
                    "Default: 8");
            c.addComment("Global.Visualizer.Modern.Materials." + CuboidDisplayType.values()[0].name() + ".LineThickness", "Lowest value 0.01, highest 1", "Default: 0.05");
            c.addComment("Global.Visualizer.Modern.Materials." + CuboidDisplayType.values()[0].name() + ".HideInSeconds", "Lowest value 0.1, highest 300", "Default: 30");

            for (CuboidDisplayType one : CuboidDisplayType.values()) {
                String name = c.get("Global.Visualizer.Modern.Materials." + one.name() + ".Edges", one.getEdgeMaterial().toString());
                CMIMaterial mat = CMIMaterial.get(name);

                if (mat.isBlock())
                    one.setEdgeMaterial(mat);
                else
                    CMIMessages.consoleMessage("Selection " + one.name() + " edges material not found(" + name + "), defaulting to " + one.getEdgeMaterial().toString());

                name = c.get("Global.Visualizer.Modern.Materials." + one.name() + ".Sides", one.getSideMaterial().toString());
                mat = CMIMaterial.get(name);

                if (mat.isBlock())
                    one.setSideMaterial(mat);
                else
                    CMIMessages.consoleMessage("Selection " + one.name() + " side material not found(" + name + "), defaulting to " + one.getSideMaterial().toString());

                one.setLineThickness(c.get("Global.Visualizer.Modern.Materials." + one.name() + ".LineThickness", one.getLineThickness()));
                one.setGridSize(c.get("Global.Visualizer.Modern.Materials." + one.name() + ".GridSize", one.getGridSize()));
                one.setHideInSeconds(c.get("Global.Visualizer.Modern.Materials." + one.name() + ".HideInSeconds", one.getHideInSeconds()));
                one.setRange(c.get("Global.Visualizer.Modern.Materials." + one.name() + ".Range", one.getRange()));
            }

        }

        if (Version.isTestServer())
            CuboidDisplayManager.register();
    }

    public static boolean isShow() {
        return show;
    }

    public static void setShow(boolean show) {
        VisualizerConfig.show = show;
    }

    public static int getRange() {
        return range;
    }

    public static void setRange(int range) {
        VisualizerConfig.range = range;
    }

    public static int getShowForMs() {
        return showForMs;
    }

    public static void setShowForMs(int showForMs) {
        VisualizerConfig.showForMs = showForMs;
    }

    public static int getUpdateInterval() {
        return updateInterval;
    }

    public static void setUpdateInterval(int updateInterval) {
        VisualizerConfig.updateInterval = updateInterval;
    }

    public static double getRowSpacing() {
        return rowSpacing;
    }

    public static void setRowSpacing(double rowSpacing) {
        VisualizerConfig.rowSpacing = rowSpacing;
    }

    public static double getCollumnSpacing() {
        return collumnSpacing;
    }

    public static void setCollumnSpacing(double collumnSpacing) {
        VisualizerConfig.collumnSpacing = collumnSpacing;
    }

    public static int getSkipBy() {
        return skipBy;
    }

    public static void setSkipBy(int skipBy) {
        VisualizerConfig.skipBy = skipBy;
    }

    public static int getFrameCap() {
        return frameCap;
    }

    public static void setFrameCap(int frameCap) {
        VisualizerConfig.frameCap = frameCap;
    }

    public static int getSidesCap() {
        return sidesCap;
    }

    public static void setSidesCap(int sidesCap) {
        VisualizerConfig.sidesCap = sidesCap;
    }

    public static SelectionEffect getBaseEffect() {
        return baseEffect;
    }

    public static void setBaseEffect(SelectionEffect baseEffect) {
        VisualizerConfig.baseEffect = baseEffect;
    }

    public static SelectionEffect getErrorEffect() {
        return errorEffect;
    }

    public static void setErrorEffect(SelectionEffect errorEffect) {
        VisualizerConfig.errorEffect = errorEffect;
    }

    public static boolean isBounceAnimation() {
        return bounceAnimation;
    }

    public static void setBounceAnimation(boolean bounceAnimation) {
        VisualizerConfig.bounceAnimation = bounceAnimation;
    }

    public static boolean isEnterAnimation() {
        return enterAnimation;
    }

    public static void setEnterAnimation(boolean enterAnimation) {
        VisualizerConfig.enterAnimation = enterAnimation;
    }

    public static boolean isUseModernVersion() {
        return useModernVersion;
    }

    public static void setUseModernVersion(boolean useModernVersion) {
        VisualizerConfig.useModernVersion = useModernVersion;
    }

    public static int getGridSize() {
        return gridSize;
    }

    public static void setGridSize(int gridSize) {
        VisualizerConfig.gridSize = CMINumber.clamp(gridSize, 1);
    }

    public static double getLineThickness() {
        return lineThickness;
    }

    public static void setLineThickness(double lineThickness) {
        VisualizerConfig.lineThickness = lineThickness;
    }

    public static int getUpdateRateByTravel() {
        return updateRateByTravel;
    }

    public static void setUpdateRateByTravel(int updateRateByBlock) {
        VisualizerConfig.updateRateByTravel = updateRateByBlock;
    }
}
