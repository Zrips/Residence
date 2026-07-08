package com.bekvon.bukkit.residence.containers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Farmland;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.Zrips.CMILib.Items.CMIMaterial;

public class ResidenceBlockData {

    private static int MAX_ENTRIES = 50;
    public static LinkedHashMap<String, BlockData> powder_snow = new LinkedHashMap<String, BlockData>(MAX_ENTRIES + 1, .75F, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BlockData> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public static void updatePowderedSnow(Block block) {

        BlockData data = powder_snow.remove(block.getLocation().toString());
        if (data == null)
            return;

        Block blockUnder = block.getLocation().clone().add(0, -1, 0).getBlock();

        if (data.getMaterial().equals(blockUnder.getType())) {
            blockUnder.setBlockData(data);
        }
    }
    
    public static void addPowderedSnow(Block sourceBlock, Block block) {

        powder_snow.put(sourceBlock.getLocation().toString(), block.getBlockData().clone());

    }
    
    public static void updateAnvilFacing(@NotNull Block block) {

        if (block == null || !CMIMaterial.isAnvil(block.getType()))
            return;

        BlockFace face = ((Directional) block.getBlockData()).getFacing();
        block.setType(CMIMaterial.ANVIL.getMaterial());
        Directional directional = (Directional) block.getBlockData();
        directional.setFacing(face);
        block.setBlockData(directional);
    }

    public static @Nullable Location getRelative(@NotNull Block block) {
        if (block == null)
            return null;
        return block.getRelative(((Dispenser) block.getBlockData()).getFacing()).getLocation();
    }

    public static void updateFarmLand(@NotNull Block block) {
        BlockData data = block.getBlockData();
        Farmland farm = (Farmland) data;
        if (farm.getMoisture() < 2) {
            farm.setMoisture(7);
            block.setBlockData(farm);
        }
    }

}
