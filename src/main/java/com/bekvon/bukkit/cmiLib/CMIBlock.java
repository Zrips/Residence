package com.bekvon.bukkit.cmiLib;

import com.bekvon.bukkit.cmiLib.VersionChecker.Version;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Stairs.Shape;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CMIBlock {
    private Block block;
    private Integer data = null;
    private Object blockd = null;

    public CMIBlock(Block block) {
        this.block = block;
    }

    @Deprecated
    public blockDirection getDirection() {

        return blockDirection.getByDir(block.getData()) == null ? blockDirection.none : blockDirection.getByDir(block.getData());
    }

    public boolean isWaterlogged() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            if (block.getBlockData() instanceof org.bukkit.block.data.Waterlogged) {
                org.bukkit.block.data.Waterlogged wl = (Waterlogged) block.getBlockData();
                return wl.isWaterlogged();
            }
        }

        CMIMaterial mat = CMIMaterial.get(block);
        switch (mat) {
            case WATER:
            case LEGACY_STATIONARY_WATER:
            case BUBBLE_COLUMN:
            case KELP_PLANT:
            case KELP:
            case SEAGRASS:
            case TALL_SEAGRASS:
                return true;
            default:
                break;
        }

        return false;
    }

    public Bisect getBisect() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            org.bukkit.block.data.BlockData blockData = block.getBlockData();
            if (!(blockData instanceof org.bukkit.block.data.Bisected))
                return null;

            org.bukkit.block.data.Bisected half = (org.bukkit.block.data.Bisected) blockData;

            switch (half.getHalf()) {
                case TOP:
                    return Bisect.TOP;
                case BOTTOM:
                    return Bisect.BOTTOM;
            }
        }

        return null;
    }

    public boolean isAttached() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            org.bukkit.block.data.BlockData blockData = block.getBlockData();
            if (!(blockData instanceof org.bukkit.block.data.Attachable))
                return false;
            org.bukkit.block.data.Attachable half = (org.bukkit.block.data.Attachable) blockData;
            return half.isAttached();
        }

        return false;
    }

    public Axis getAxis() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            org.bukkit.block.data.BlockData blockData = block.getBlockData();
            if (!(blockData instanceof org.bukkit.block.data.Orientable))
                return null;
            org.bukkit.block.data.Orientable half = (org.bukkit.block.data.Orientable) blockData;
            return half.getAxis();
        }

        return null;
    }

    public BedPart getBedPart() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            org.bukkit.block.data.BlockData blockData = block.getBlockData();
            if (!(blockData instanceof org.bukkit.block.data.type.Bed))
                return null;
            org.bukkit.block.data.type.Bed half = (org.bukkit.block.data.type.Bed) blockData;
            switch (half.getPart()) {
                case FOOT:
                    return BedPart.FOOT;
                case HEAD:
                    return BedPart.HEAD;
                default:
                    break;
            }
        }

        return null;
    }

    public StairShape getStairShape() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            CMIMaterial cm = CMIMaterial.get(block);
            if (cm.isStairs()) {
                org.bukkit.block.data.BlockData blockData = block.getBlockData();
                org.bukkit.block.data.type.Stairs stair = ((org.bukkit.block.data.type.Stairs) blockData);
                org.bukkit.block.data.type.Stairs.Shape shape = stair.getShape();
                return StairShape.getByName(shape.toString());
            }
        }
        return null;
    }

    public BlockFace getFacing() {

        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            org.bukkit.block.data.BlockData blockData = block.getBlockData();
            if (blockData instanceof org.bukkit.block.data.Directional) {
                org.bukkit.block.data.Directional directional = blockd == null
                        ? (org.bukkit.block.data.Directional) blockData.clone()
                        : (org.bukkit.block.data.Directional) blockd;

                return directional.getFacing();
            }
            if (blockData instanceof org.bukkit.block.data.Rotatable) {
                org.bukkit.block.data.Rotatable directional = blockd == null ? (org.bukkit.block.data.Rotatable) blockData.clone()
                        : (org.bukkit.block.data.Rotatable) blockd;
                return directional.getRotation();
            }
        }

        try {
            CMIMaterial cm = CMIMaterial.get(block);
            switch (cm) {
                case COMPARATOR:
                    org.bukkit.material.Comparator Comparator2 = (org.bukkit.material.Comparator) block.getState().getData();
                    return Comparator2.getFacing();
                case REPEATER:
                    org.bukkit.material.Diode diode = (org.bukkit.material.Diode) block.getState().getData();
                    return diode.getFacing();
                case DISPENSER:
                    org.bukkit.material.Dispenser Dispenser = (org.bukkit.material.Dispenser) block.getState().getData();
                    return Dispenser.getFacing();
                case HOPPER:
                    org.bukkit.material.Hopper Hopper = (org.bukkit.material.Hopper) block.getState().getData();
                    return Hopper.getFacing();
                case LEVER:
                    org.bukkit.material.Lever Lever = (org.bukkit.material.Lever) block.getState().getData();
                    return Lever.getFacing();
                case TRIPWIRE_HOOK:
                    org.bukkit.material.TripwireHook TripwireHook = (org.bukkit.material.TripwireHook) block.getState().getData();
                    return TripwireHook.getFacing();
            }

            if (cm.isSign()) {
                org.bukkit.material.Sign sign = (org.bukkit.material.Sign) block.getState().getData();
                return sign.getFacing();
            }

            if (cm.isSkull()) {
                org.bukkit.material.Skull Skull = (org.bukkit.material.Skull) block.getState().getData();
                return Skull.getFacing();
            }
            if (cm.isBed()) {
                org.bukkit.material.Bed mat = (org.bukkit.material.Bed) block.getState().getData();
                return mat.getFacing();
            }

            if (cm.isDoor()) {
                org.bukkit.material.Door mat = (org.bukkit.material.Door) block.getState().getData();
                return mat.getFacing();
            }
            if (cm.isGate()) {
                org.bukkit.material.Gate mat = (org.bukkit.material.Gate) block.getState().getData();
                return mat.getFacing();
            }

            String stringname = this.block.getType().name();

            if (stringname.contains("_STAIRS")) {
                org.bukkit.material.Stairs mat = (org.bukkit.material.Stairs) block.getState().getData();

                return mat.getFacing();
            }
            if (stringname.contains("BANNER")) {
                org.bukkit.material.Banner Banner2 = (org.bukkit.material.Banner) block.getState().getData();
                return Banner2.getFacing();
            }

            if (stringname.contains("OBSERVER")) {
                org.bukkit.material.Observer Banner2 = (org.bukkit.material.Observer) block.getState().getData();
                return Banner2.getFacing();
            }

            if (stringname.contains("PISTON_BASE")) {
                org.bukkit.material.PistonBaseMaterial Banner2 = (org.bukkit.material.PistonBaseMaterial) block.getState().getData();
                return Banner2.getFacing();
            }

            if (stringname.contains("PISTON_EXTENSION")) {
                org.bukkit.material.PistonExtensionMaterial Banner2 = (org.bukkit.material.PistonExtensionMaterial) block.getState().getData();
                return Banner2.getFacing();
            }
        } catch (Exception e) {
        }

        return null;
    }

    public CMIBlock flip(FlipDirection direction, boolean angle) {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            CMIMaterial cmat = CMIMaterial.get(this.block);
            org.bukkit.block.data.BlockData blockData = block.getBlockData().clone();
            switch (direction) {
                case UP_DOWN:
                    if (blockData instanceof org.bukkit.block.data.Bisected) {
                        org.bukkit.block.data.Bisected half = blockd == null || !(blockd instanceof org.bukkit.block.data.Bisected)
                                ? (org.bukkit.block.data.Bisected) blockData.clone()
                                : (org.bukkit.block.data.Bisected) blockd;
                        if (half != null) {
                            switch (half.getHalf()) {
                                case TOP:
                                    half.setHalf(org.bukkit.block.data.Bisected.Half.BOTTOM);
                                    break;
                                case BOTTOM:
                                    half.setHalf(org.bukkit.block.data.Bisected.Half.TOP);
                                    break;
                            }
                            if (this.blockd == null)
                                this.blockd = half;
                        }
                    }
                    break;
            }

            if (blockData instanceof org.bukkit.block.data.Directional) {
                org.bukkit.block.data.Directional directional = blockd == null
                        ? (org.bukkit.block.data.Directional) blockData.clone()
                        : (org.bukkit.block.data.Directional) blockd;
                if (directional != null) {
                    org.bukkit.block.BlockFace face = directional.getFacing();
                    switch (direction) {
                        case NORTH_SOUTH:

                            if (angle) {
                                switch (face) {
                                    case NORTH:
                                        directional.setFacing(BlockFace.EAST);
                                        break;
                                    case SOUTH:
                                        directional.setFacing(BlockFace.WEST);
                                        break;
                                    case WEST:
                                        directional.setFacing(BlockFace.SOUTH);
                                        break;
                                    case EAST:
                                        directional.setFacing(BlockFace.NORTH);
                                        break;
                                }
                            } else {
                                switch (face) {
                                    case NORTH:
                                        directional.setFacing(BlockFace.SOUTH);
                                        break;
                                    case SOUTH:
                                        directional.setFacing(BlockFace.NORTH);
                                        break;
                                }
                            }

                            break;
                        case WEST_EAST:

                            if (angle) {
                                switch (face) {
                                    case WEST:
                                        directional.setFacing(BlockFace.NORTH);
                                        break;
                                    case EAST:
                                        directional.setFacing(BlockFace.SOUTH);
                                        break;
                                    case NORTH:
                                        directional.setFacing(BlockFace.WEST);
                                        break;
                                    case SOUTH:
                                        directional.setFacing(BlockFace.EAST);
                                        break;
                                }
                            } else {
                                switch (face) {
                                    case WEST:
                                        directional.setFacing(BlockFace.EAST);
                                        break;
                                    case EAST:
                                        directional.setFacing(BlockFace.WEST);
                                        break;
                                }
                            }

                            break;
                        case UP_DOWN:
                            switch (face) {
                                case UP:
                                    directional.setFacing(BlockFace.DOWN);
                                    break;
                                case DOWN:
                                    directional.setFacing(BlockFace.UP);
                                    break;
                            }
                            break;
                    }

                    if (this.blockd == null)
                        this.blockd = directional;
                }
            }

            if (cmat.isStairs()) {
                org.bukkit.block.data.type.Stairs stair = ((org.bukkit.block.data.type.Stairs) this.blockd);
                org.bukkit.block.data.type.Stairs.Shape shape = stair.getShape();
                org.bukkit.block.BlockFace face = stair.getFacing();
                switch (direction) {
                    case NORTH_SOUTH:
                    case WEST_EAST:
                        switch (shape) {
                            case INNER_LEFT:
                                stair.setShape(Shape.INNER_RIGHT);
                                break;
                            case INNER_RIGHT:
                                stair.setShape(Shape.INNER_LEFT);
                                break;
                            case OUTER_LEFT:
                                stair.setShape(Shape.OUTER_RIGHT);
                                break;
                            case OUTER_RIGHT:
                                stair.setShape(Shape.OUTER_LEFT);
                                break;
                        }
                        break;
                }
            }

            if (blockData instanceof org.bukkit.block.data.Rotatable) {
                org.bukkit.block.data.Rotatable directional = blockd == null ? (org.bukkit.block.data.Rotatable) blockData.clone()
                        : (org.bukkit.block.data.Rotatable) blockd;

                switch (directional.getRotation()) {
                    case NORTH:
                        directional.setRotation(BlockFace.SOUTH);
                        break;
                    case NORTH_NORTH_EAST:
                        directional.setRotation(BlockFace.SOUTH_SOUTH_WEST);
                        break;
                    case NORTH_EAST:
                        directional.setRotation(BlockFace.SOUTH_WEST);
                        break;
                    case EAST_NORTH_EAST:
                        directional.setRotation(BlockFace.WEST_SOUTH_WEST);
                        break;
                    case EAST:
                        directional.setRotation(BlockFace.WEST);
                        break;
                    case EAST_SOUTH_EAST:
                        directional.setRotation(BlockFace.WEST_NORTH_WEST);
                        break;
                    case SOUTH_EAST:
                        directional.setRotation(BlockFace.NORTH_WEST);
                        break;
                    case SOUTH_SOUTH_EAST:
                        directional.setRotation(BlockFace.NORTH_NORTH_WEST);
                        break;
                    case SOUTH:
                        directional.setRotation(BlockFace.NORTH);
                        break;
                    case SOUTH_SOUTH_WEST:
                        directional.setRotation(BlockFace.NORTH_NORTH_EAST);
                        break;
                    case SOUTH_WEST:
                        directional.setRotation(BlockFace.NORTH_EAST);
                        break;
                    case WEST_SOUTH_WEST:
                        directional.setRotation(BlockFace.EAST_NORTH_EAST);
                        break;
                    case WEST:
                        directional.setRotation(BlockFace.EAST);
                        break;
                    case WEST_NORTH_WEST:
                        directional.setRotation(BlockFace.EAST_SOUTH_EAST);
                        break;
                    case NORTH_WEST:
                        directional.setRotation(BlockFace.SOUTH_EAST);
                        break;
                    case NORTH_NORTH_WEST:
                        directional.setRotation(BlockFace.SOUTH_SOUTH_EAST);
                        break;
                }
                if (this.blockd == null)
                    this.blockd = directional;
            }

            if (!cmat.isWall() && !cmat.isGlassPane() && !cmat.isFence() && !cmat.equals(CMIMaterial.IRON_BARS)) {
                if (this.blockd == null)
                    this.blockd = blockData.clone();
            }

            return this;

        }

        int flipX = 0;
        int flipY = 0;
        int flipZ = 0;

        Material type = block.getType();
        this.data = this.data == null ? block.getData() : this.data;

        CMIMaterial cmat = CMIMaterial.get(block);

        try {
            switch (direction) {
                case NORTH_SOUTH:
                    flipZ = 1;
                    break;

                case WEST_EAST:
                    flipX = 1;
                    break;

                case UP_DOWN:
                    flipY = 1;
                    break;
            }

            if (cmat.isButton()) {
                switch (data & ~0x8) {
                    case 1:
                        this.data = data + flipX;
                        break;
                    case 2:
                        this.data = data - flipX;
                        break;
                    case 3:
                        this.data = data + flipZ;
                        break;
                    case 4:
                        this.data = data - flipZ;
                        break;
                    case 0:
                    case 5:
                        this.data = data ^ (flipY * 5);
                        break;
                }
            } else if (cmat.isSlab()) {
                this.data = data ^ (flipY << 3);
            } else if (cmat.isDoor()) {
                // Only bottom part
                if ((data & 0x8) == 0) {
                    switch (data & 0x3) {
                        case 0:
                            this.data = data + flipX + flipZ * 3;
                            break;
                        case 1:
                            this.data = data - flipX + flipZ;
                            break;
                        case 2:
                            this.data = data + flipX - flipZ;
                            break;
                        case 3:
                            this.data = data - flipX - flipZ * 3;
                            break;
                    }
                }
            } else if (cmat.isTrapDoor()) {
                switch (data & 0x3) {
                    case 0:
                    case 1:
                        this.data = data ^ flipZ;
                        break;
                    case 2:
                    case 3:
                        this.data = data ^ flipX;
                        break;
                }
            } else if (cmat.isBed()) {
                switch (data & 0x3) {
                    case 0:
                    case 2:
                        this.data = data ^ flipZ << 1;
                        break;
                    case 1:
                    case 3:
                        this.data = data ^ flipX << 1;
                        break;
                }
            } else if (cmat.isSign()) {
                switch (direction) {
                    case NORTH_SOUTH:
                        this.data = (16 - data) & 0xf;
                        break;
                    case WEST_EAST:
                        this.data = (8 - data) & 0xf;
                        break;
                    default:
                }
            }

            switch (CMIMaterial.get(block)) {
                case TORCH:
                case REDSTONE_TORCH:
//	    case UNLIT_REDSTONE_TORCH:
                    if (data < 1 || data > 4)
                        break;
                    switch (data) {
                        case 1:
                            this.data = data + flipX;
                            break;
                        case 2:
                            this.data = data - flipX;
                            break;
                        case 3:
                            this.data = data + flipZ;
                            break;
                        case 4:
                            this.data = data - flipZ;
                            break;
                    }
                    break;

                case LEVER:
                    switch (data & ~0x8) {
                        case 1:
                            this.data = data + flipX;
                            break;
                        case 2:
                            this.data = data - flipX;
                            break;
                        case 3:
                            this.data = data + flipZ;
                            break;
                        case 4:
                            this.data = data - flipZ;
                            break;
                        case 5:
                        case 7:
                            this.data = data ^ flipY << 1;
                            break;
                        case 6:
                        case 0:
                            this.data = data ^ flipY * 6;
                            break;
                    }
                    break;
                case RAIL:
                    switch (data) {
                        case 6:
                            this.data = data + flipX + flipZ * 3;
                            break;
                        case 7:
                            this.data = data - flipX + flipZ;
                            break;
                        case 8:
                            this.data = data + flipX - flipZ;
                            break;
                        case 9:
                            this.data = data - flipX - flipZ * 3;
                            break;
                    }

                case POWERED_RAIL:
                case DETECTOR_RAIL:
                case ACTIVATOR_RAIL:
                    switch (data & 0x7) {
                        case 0:
                        case 1:
                            this.data = data;
                            break;
                        case 2:
                        case 3:
                            this.data = data ^ flipX;
                            break;
                        case 4:
                        case 5:
                            this.data = data ^ flipZ;
                            break;
                    }
                    break;

                case LADDER:
                case WALL_SIGN:
                case CHEST:
                case FURNACE:
                case LEGACY_BURNING_FURNACE:
                case ENDER_CHEST:
                case TRAPPED_CHEST:
                case HOPPER:
                    int extra = data & 0x8;
                    int withoutFlags = data & ~0x8;
                    switch (withoutFlags) {
                        case 2:
                        case 3:
                            this.data = (data ^ flipZ) | extra;
                            break;
                        case 4:
                        case 5:
                            this.data = (data ^ flipX) | extra;
                            break;
                    }
                    break;

                case DROPPER:
                case DISPENSER:
                    int dispPower = data & 0x8;
                    switch (data & ~0x8) {
                        case 2:
                        case 3:
                            this.data = (data ^ flipZ) | dispPower;
                            break;
                        case 4:
                        case 5:
                            this.data = (data ^ flipX) | dispPower;
                            break;
                        case 0:
                        case 1:
                            this.data = (data ^ flipY) | dispPower;
                            break;
                    }
                    break;

                case PUMPKIN:
                case JACK_O_LANTERN:
                    if (data > 3)
                        break;
                case REPEATER:
                case LEGACY_DIODE_BLOCK_OFF:
                case LEGACY_DIODE_BLOCK_ON:
                case LEGACY_REDSTONE_COMPARATOR_OFF:
                case LEGACY_REDSTONE_COMPARATOR_ON:
                case COMPARATOR:
                case COCOA:
                case TRIPWIRE_HOOK:
                    switch (data & 0x3) {
                        case 0:
                        case 2:
                            this.data = data ^ (flipZ << 1);
                            break;
                        case 1:
                        case 3:
                            this.data = data ^ (flipX << 1);
                            break;
                    }
                    break;
                case PISTON_HEAD:
                case STICKY_PISTON:
                case MOVING_PISTON:
                    switch (data & ~0x8) {
                        case 0:
                        case 1:
                            this.data = data ^ flipY;
                            break;
                        case 2:
                        case 3:
                            this.data = data ^ flipZ;
                            break;
                        case 4:
                        case 5:
                            this.data = data ^ flipX;
                            break;
                    }
                    break;

                case RED_MUSHROOM:
                case BROWN_MUSHROOM:
                    switch (data) {
                        case 1:
                        case 4:
                        case 7:
                            data += flipX * 2;
                            break;
                        case 3:
                        case 6:
                        case 9:
                            data -= flipX * 2;
                            break;
                    }
                    switch (data) {
                        case 1:
                        case 2:
                        case 3:
                            this.data = data + flipZ * 6;
                            break;
                        case 7:
                        case 8:
                        case 9:
                            this.data = data - flipZ * 6;
                            break;
                    }
                    break;

                case VINE:
                    int bit1 = 0, bit2 = 0;
                    switch (direction) {
                        case NORTH_SOUTH:
                            bit1 = 0x2;
                            bit2 = 0x8;
                            break;
                        case WEST_EAST:
                            bit1 = 0x1;
                            bit2 = 0x4;
                            break;
                        default:
                            this.data = data;
                            break;
                    }
                    int newData = data & ~(bit1 | bit2);
                    if ((data & bit1) != 0)
                        newData |= bit2;
                    if ((data & bit2) != 0)
                        newData |= bit1;
                    this.data = newData;
                    break;

                case LEGACY_SKULL:
                    switch (data) {
                        case 2:
                        case 3:
                            this.data = data ^ flipZ;
                            break;
                        case 4:
                        case 5:
                            this.data = data ^ flipX;
                            break;
                    }
                    break;

                case ANVIL:
                    switch (data & 0x3) {
                        case 0:
                        case 2:
                            this.data = data ^ flipZ << 1;
                            break;
                        case 1:
                        case 3:
                            this.data = data ^ flipX << 1;
                            break;
                    }
            }

            if (cmat.isFence()) {
                switch (data & 0x3) {
                    case 0:
                    case 2:
                        this.data = data ^ flipZ << 1;
                        break;
                    case 1:
                    case 3:
                        this.data = data ^ flipX << 1;
                        break;
                }
            } else if (cmat.isStairs()) {

                data ^= flipY << 2;
                switch (data) {
                    case 0:
                    case 1:
                    case 4:
                    case 5:
                        this.data = data ^ flipX;
                        break;
                    case 2:
                    case 3:
                    case 6:
                    case 7:
                        this.data = data ^ flipZ;
                        break;
                }
            }

            if (type.name().contains("TERRACOTTA")) {
                switch (data & 0x3) {
                    case 0:
                    case 2:
                        this.data = data ^ flipZ << 1;
                        break;
                    case 1:
                    case 3:
                        this.data = data ^ flipX << 1;
                        break;
                }
            }

        } catch (Exception e) {
            this.data = data;
        }
        return this;
    }

    public CMIBlock rotate90Reverse() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {

            CMIMaterial cmat = CMIMaterial.get(this.block);
            org.bukkit.block.data.BlockData blockData = block.getBlockData();

            if (blockData instanceof org.bukkit.block.data.Directional) {
                org.bukkit.block.data.Directional directional = blockd == null ? (org.bukkit.block.data.Directional) blockData.clone()
                        : (org.bukkit.block.data.Directional) blockd;
                org.bukkit.block.BlockFace face = directional.getFacing();

                switch (directional.getFacing()) {
                    case NORTH:
                        directional.setFacing(BlockFace.WEST);
                        break;
                    case EAST:
                        directional.setFacing(BlockFace.NORTH);
                        break;
                    case SOUTH:
                        directional.setFacing(BlockFace.EAST);
                        break;
                    case WEST:
                        directional.setFacing(BlockFace.SOUTH);
                        break;

                }

                if (this.blockd == null)
                    this.blockd = directional.clone();

            }

            if (blockData instanceof org.bukkit.block.data.Rotatable) {

                org.bukkit.block.data.Rotatable directional = blockd == null ? (org.bukkit.block.data.Rotatable) blockData.clone()
                        : (org.bukkit.block.data.Rotatable) blockd;

                org.bukkit.block.BlockFace face = directional.getRotation();

                switch (face) {
                    case NORTH:
                        directional.setRotation(BlockFace.WEST);
                        break;
                    case NORTH_NORTH_EAST:
                        directional.setRotation(BlockFace.WEST_NORTH_WEST);
                        break;
                    case NORTH_EAST:
                        directional.setRotation(BlockFace.NORTH_WEST);
                        break;
                    case EAST_NORTH_EAST:
                        directional.setRotation(BlockFace.NORTH_NORTH_WEST);
                        break;
                    case EAST:
                        directional.setRotation(BlockFace.NORTH);
                        break;
                    case EAST_SOUTH_EAST:
                        directional.setRotation(BlockFace.NORTH_NORTH_EAST);
                        break;
                    case SOUTH_EAST:
                        directional.setRotation(BlockFace.NORTH_EAST);
                        break;
                    case SOUTH_SOUTH_EAST:
                        directional.setRotation(BlockFace.EAST_NORTH_EAST);
                        break;
                    case SOUTH:
                        directional.setRotation(BlockFace.EAST);
                        break;
                    case SOUTH_SOUTH_WEST:
                        directional.setRotation(BlockFace.EAST_SOUTH_EAST);
                        break;
                    case SOUTH_WEST:
                        directional.setRotation(BlockFace.SOUTH_EAST);
                        break;
                    case WEST_SOUTH_WEST:
                        directional.setRotation(BlockFace.SOUTH_SOUTH_EAST);
                        break;
                    case WEST:
                        directional.setRotation(BlockFace.SOUTH);
                        break;
                    case WEST_NORTH_WEST:
                        directional.setRotation(BlockFace.SOUTH_SOUTH_WEST);
                        break;
                    case NORTH_WEST:
                        directional.setRotation(BlockFace.SOUTH_WEST);
                        break;
                    case NORTH_NORTH_WEST:
                        directional.setRotation(BlockFace.WEST_SOUTH_WEST);
                        break;
                }
                if (this.blockd == null)
                    this.blockd = directional;
                return this;

            } else {
                if (this.blockd == null)
                    this.blockd = blockData.clone();
                return this;
            }
        }

        Material type = block.getType();
        this.data = this.data == null ? block.getData() : this.data;

        CMIMaterial cmat = CMIMaterial.get(block);

        try {

            switch (cmat) {
                case TORCH:
                case LEGACY_REDSTONE_TORCH_OFF:
                case LEGACY_REDSTONE_TORCH_ON:
                    switch (data) {
                        case 1:
                            this.data = 3;
                            break;
                        case 2:
                            this.data = 4;
                            break;
                        case 3:
                            this.data = 2;
                            break;
                        case 4:
                            this.data = 1;
                            break;
                    }
                case RAIL:
                    switch (data) {
                        case 6:
                            this.data = 7;
                            break;
                        case 7:
                            this.data = 8;
                            break;
                        case 8:
                            this.data = 9;
                            break;
                        case 9:
                            this.data = 6;
                            break;
                    }
                case POWERED_RAIL:
                case DETECTOR_RAIL:
                case ACTIVATOR_RAIL:
                    switch (data & 0x7) {
                        case 0:
                            this.data = 1 | (data & ~0x7);
                            break;
                        case 1:
                            this.data = 0 | (data & ~0x7);
                            break;
                        case 2:
                            this.data = 5 | (data & ~0x7);
                            break;
                        case 3:
                            this.data = 4 | (data & ~0x7);
                            break;
                        case 4:
                            this.data = 2 | (data & ~0x7);
                            break;
                        case 5:
                            this.data = 3 | (data & ~0x7);
                            break;
                    }
                    break;

                case STONE_BUTTON:
                case OAK_BUTTON: {
                    int thrown = data & 0x8;
                    switch (data & ~0x8) {
                        case 1:
                            this.data = 3 | thrown;
                            break;
                        case 2:
                            this.data = 4 | thrown;
                            break;
                        case 3:
                            this.data = 2 | thrown;
                            break;
                        case 4:
                            this.data = 1 | thrown;
                            break;
                        // 0 and 5 are vertical
                    }
                    break;
                }

                case LEVER: {
                    int thrown = data & 0x8;
                    switch (data & ~0x8) {
                        case 1:
                            this.data = 3 | thrown;
                            break;
                        case 2:
                            this.data = 4 | thrown;
                            break;
                        case 3:
                            this.data = 2 | thrown;
                            break;
                        case 4:
                            this.data = 1 | thrown;
                            break;
                        case 5:
                            this.data = 6 | thrown;
                            break;
                        case 6:
                            this.data = 5 | thrown;
                            break;
                        case 7:
                            this.data = 0 | thrown;
                            break;
                        case 0:
                            this.data = 7 | thrown;
                            break;
                    }
                    break;
                }

                case OAK_DOOR:
                case IRON_DOOR:
                    if ((data & 0x8) != 0) {
                        // door top halves contain no orientation information
                        break;
                    }

                    /* FALL-THROUGH */

                case COCOA:
                case TRIPWIRE_HOOK: {
                    int extra = data & ~0x3;
                    int withoutFlags = data & 0x3;
                    switch (withoutFlags) {
                        case 0:
                            this.data = 1 | extra;
                            break;
                        case 1:
                            this.data = 2 | extra;
                            break;
                        case 2:
                            this.data = 3 | extra;
                            break;
                        case 3:
                            this.data = 0 | extra;
                            break;
                    }
                    break;
                }
                case LEGACY_SIGN_POST:
                    this.data = (data + 4) % 16;
                    break;

                case LADDER:
                case WALL_SIGN:
                case CHEST:
                case FURNACE:
                case LEGACY_BURNING_FURNACE:
                case ENDER_CHEST:
                case TRAPPED_CHEST:
                case HOPPER: {
                    int extra = data & 0x8;
                    int withoutFlags = data & ~0x8;
                    switch (withoutFlags) {
                        case 2:
                            this.data = 5 | extra;
                            break;
                        case 3:
                            this.data = 4 | extra;
                            break;
                        case 4:
                            this.data = 2 | extra;
                            break;
                        case 5:
                            this.data = 3 | extra;
                            break;
                    }
                    break;
                }
                case DISPENSER:
                case DROPPER:
                    int dispPower = data & 0x8;
                    switch (data & ~0x8) {
                        case 2:
                            this.data = 5 | dispPower;
                            break;
                        case 3:
                            this.data = 4 | dispPower;
                            break;
                        case 4:
                            this.data = 2 | dispPower;
                            break;
                        case 5:
                            this.data = 3 | dispPower;
                            break;
                    }
                    break;

                case PUMPKIN:
                case JACK_O_LANTERN:
                    switch (data) {
                        case 0:
                            this.data = 1;
                            break;
                        case 1:
                            this.data = 2;
                            break;
                        case 2:
                            this.data = 3;
                            break;
                        case 3:
                            this.data = 0;
                            break;
                    }
                    break;

                case HAY_BLOCK:
                case OAK_LOG:
                case BIRCH_LOG:
                case SPRUCE_LOG:
                case JUNGLE_LOG:
                    if (data >= 4 && data <= 11)
                        data ^= 0xc;
                    break;

                case LEGACY_DIODE_BLOCK_OFF:
                case LEGACY_DIODE_BLOCK_ON:
                case LEGACY_REDSTONE_COMPARATOR_OFF:
                case LEGACY_REDSTONE_COMPARATOR_ON:
                    int dir = data & 0x03;
                    int delay = data - dir;
                    switch (dir) {
                        case 0:
                            this.data = 1 | delay;
                            break;
                        case 1:
                            this.data = 2 | delay;
                            break;
                        case 2:
                            this.data = 3 | delay;
                            break;
                        case 3:
                            this.data = 0 | delay;
                            break;
                    }
                    break;
                case OAK_TRAPDOOR:
                case IRON_TRAPDOOR:
                    int withoutOrientation = data & ~0x3;
                    int orientation = data & 0x3;
                    switch (orientation) {
                        case 0:
                            this.data = 3 | withoutOrientation;
                            break;
                        case 1:
                            this.data = 2 | withoutOrientation;
                            break;
                        case 2:
                            this.data = 0 | withoutOrientation;
                            break;
                        case 3:
                            this.data = 1 | withoutOrientation;
                            break;
                    }
                    break;

                case PISTON_HEAD:
                case STICKY_PISTON:
                case MOVING_PISTON:
                    final int rest = data & ~0x7;
                    switch (data & 0x7) {
                        case 2:
                            this.data = 5 | rest;
                            break;
                        case 3:
                            this.data = 4 | rest;
                            break;
                        case 4:
                            this.data = 2 | rest;
                            break;
                        case 5:
                            this.data = 3 | rest;
                            break;
                    }
                    break;

                case RED_MUSHROOM:
                case BROWN_MUSHROOM:
                    if (data >= 10) {
                        this.data = data;
                        break;
                    }
                    this.data = (data * 3) % 10;
                    break;

                case VINE:
                    this.data = ((data << 1) | (data >> 3)) & 0xf;
                    break;
                case ANVIL:
                    int damage = data & ~0x3;
                    switch (data & 0x3) {
                        case 0:
                            this.data = 3 | damage;
                            break;
                        case 2:
                            this.data = 1 | damage;
                            break;
                        case 1:
                            this.data = 0 | damage;
                            break;
                        case 3:
                            this.data = 2 | damage;
                            break;
                    }
                    break;
                case LEGACY_SKULL:
                    switch (data) {
                        case 2:
                            this.data = 5;
                            break;
                        case 3:
                            this.data = 4;
                            break;
                        case 4:
                            this.data = 2;
                            break;
                        case 5:
                            this.data = 3;
                            break;
                    }
            }

            if (cmat.isBed()) {
                this.data = data & ~0x3 | (data + 1) & 0x3;
            } else if (cmat.isGate()) {
                this.data = ((data + 1) & 0x3) | (data & ~0x3);
            } else if (cmat.isStairs()) {
                switch (data) {
                    case 0:
                        this.data = 2;
                        break;
                    case 1:
                        this.data = 3;
                        break;
                    case 2:
                        this.data = 1;
                        break;
                    case 3:
                        this.data = 0;
                        break;
                    case 4:
                        this.data = 6;
                        break;
                    case 5:
                        this.data = 7;
                        break;
                    case 6:
                        this.data = 5;
                        break;
                    case 7:
                        this.data = 4;
                        break;
                }
            }

            if (type.name().contains("TERRACOTTA")) {
                int extra = data & ~0x3;
                int withoutFlags = data & 0x3;
                switch (withoutFlags) {
                    case 0:
                        this.data = 1 | extra;
                        break;
                    case 1:
                        this.data = 2 | extra;
                        break;
                    case 2:
                        this.data = 3 | extra;
                        break;
                    case 3:
                        this.data = 0 | extra;
                        break;
                }

            }

        } catch (Exception e) {
            this.data = data;
        }
        return this;

    }

    public CMIBlock rotate90() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {

            CMIMaterial cmat = CMIMaterial.get(this.block);
            org.bukkit.block.data.BlockData blockData = block.getBlockData().clone();

            if (blockData instanceof org.bukkit.block.data.Directional) {

                org.bukkit.block.data.Directional directional = blockd == null ? (org.bukkit.block.data.Directional) ((org.bukkit.block.data.Directional) blockData).clone()
                        : (org.bukkit.block.data.Directional) blockd;

//	    if (cmat.isStairs()) {

                org.bukkit.block.BlockFace face = directional.getFacing();
                switch (face) {
                    case NORTH:
                        directional.setFacing(BlockFace.EAST);
                        break;
                    case EAST:
                        directional.setFacing(BlockFace.SOUTH);
                        break;
                    case SOUTH:
                        directional.setFacing(BlockFace.WEST);
                        break;
                    case WEST:
                        directional.setFacing(BlockFace.NORTH);
                        break;
                }
                if (this.blockd == null)
                    this.blockd = directional.clone();
                return this;

            }

            if (blockData instanceof org.bukkit.block.data.Rotatable) {

                org.bukkit.block.data.Rotatable directional = blockd == null ? (org.bukkit.block.data.Rotatable) blockData.clone()
                        : (org.bukkit.block.data.Rotatable) blockd;

                org.bukkit.block.BlockFace face = directional.getRotation();

                switch (face) {
                    case NORTH:
                        directional.setRotation(BlockFace.EAST);
                        break;
                    case NORTH_NORTH_EAST:
                        directional.setRotation(BlockFace.EAST_SOUTH_EAST);
                        break;
                    case NORTH_EAST:
                        directional.setRotation(BlockFace.SOUTH_EAST);
                        break;
                    case EAST_NORTH_EAST:
                        directional.setRotation(BlockFace.SOUTH_SOUTH_EAST);
                        break;
                    case EAST:
                        directional.setRotation(BlockFace.SOUTH);
                        break;
                    case EAST_SOUTH_EAST:
                        directional.setRotation(BlockFace.SOUTH_SOUTH_WEST);
                        break;
                    case SOUTH_EAST:
                        directional.setRotation(BlockFace.SOUTH_WEST);
                        break;
                    case SOUTH_SOUTH_EAST:
                        directional.setRotation(BlockFace.WEST_SOUTH_WEST);
                        break;
                    case SOUTH:
                        directional.setRotation(BlockFace.WEST);
                        break;
                    case SOUTH_SOUTH_WEST:
                        directional.setRotation(BlockFace.WEST_NORTH_WEST);
                        break;
                    case SOUTH_WEST:
                        directional.setRotation(BlockFace.NORTH_WEST);
                        break;
                    case WEST_SOUTH_WEST:
                        directional.setRotation(BlockFace.NORTH_NORTH_WEST);
                        break;
                    case WEST:
                        directional.setRotation(BlockFace.NORTH);
                        break;
                    case WEST_NORTH_WEST:
                        directional.setRotation(BlockFace.NORTH_NORTH_EAST);
                        break;
                    case NORTH_WEST:
                        directional.setRotation(BlockFace.NORTH_EAST);
                        break;
                    case NORTH_NORTH_WEST:
                        directional.setRotation(BlockFace.EAST_NORTH_EAST);
                        break;
                }

                if (this.blockd == null)
                    this.blockd = directional;
            } else {
                if (this.blockd == null)
                    this.blockd = blockData.clone();
                return this;
            }
        }

        Material type = block.getType();
        this.data = this.data == null ? block.getData() : this.data;

        CMIMaterial cmat = CMIMaterial.get(block);

        try {
            switch (cmat) {
                case TORCH:
                case LEGACY_REDSTONE_TORCH_OFF:
                case LEGACY_REDSTONE_TORCH_ON:
                    switch (data) {
                        case 3:
                            this.data = 1;
                            break;
                        case 4:
                            this.data = 2;
                            break;
                        case 2:
                            this.data = 3;
                            break;
                        case 1:
                            this.data = 4;
                            break;
                    }
                    break;

                case RAIL:
                    switch (data) {
                        case 7:
                            this.data = 6;
                            break;
                        case 8:
                            this.data = 7;
                            break;
                        case 9:
                            this.data = 8;
                            break;
                        case 6:
                            this.data = 9;
                            break;
                    }
                case POWERED_RAIL:
                case DETECTOR_RAIL:
                case ACTIVATOR_RAIL:
                    int power = data & ~0x7;
                    switch (data & 0x7) {
                        case 1:
                            this.data = 0 | power;
                            break;
                        case 0:
                            this.data = 1 | power;
                            break;
                        case 5:
                            this.data = 2 | power;
                            break;
                        case 4:
                            this.data = 3 | power;
                            break;
                        case 2:
                            this.data = 4 | power;
                            break;
                        case 3:
                            this.data = 5 | power;
                            break;
                    }
                    break;

                case STONE_BUTTON:
                case OAK_BUTTON: {
                    int thrown = data & 0x8;
                    switch (data & ~0x8) {
                        case 3:
                            this.data = 1 | thrown;
                            break;
                        case 4:
                            this.data = 2 | thrown;
                            break;
                        case 2:
                            this.data = 3 | thrown;
                            break;
                        case 1:
                            this.data = 4 | thrown;
                            break;
                    }
                    break;
                }

                case LEVER: {
                    int thrown = data & 0x8;
                    switch (data & ~0x8) {
                        case 3:
                            this.data = 1 | thrown;
                            break;
                        case 4:
                            this.data = 2 | thrown;
                            break;
                        case 2:
                            this.data = 3 | thrown;
                            break;
                        case 1:
                            this.data = 4 | thrown;
                            break;
                        case 6:
                            this.data = 5 | thrown;
                            break;
                        case 5:
                            this.data = 6 | thrown;
                            break;
                        case 0:
                            this.data = 7 | thrown;
                            break;
                        case 7:
                            this.data = 0 | thrown;
                            break;
                    }
                    break;
                }

                case OAK_DOOR:
                case IRON_DOOR:
                    if ((data & 0x8) != 0) {
                        break;
                    }
                case COCOA:
                case TRIPWIRE_HOOK: {
                    int extra = data & ~0x3;
                    int withoutFlags = data & 0x3;
                    switch (withoutFlags) {
                        case 1:
                            this.data = 0 | extra;
                            break;
                        case 2:
                            this.data = 1 | extra;
                            break;
                        case 3:
                            this.data = 2 | extra;
                            break;
                        case 0:
                            this.data = 3 | extra;
                            break;
                    }
                    break;
                }
                case SIGN:
                    this.data = (data + 12) % 16;

                    break;
                case LADDER:
                case WALL_SIGN:
                case CHEST:
                case FURNACE:
                case LEGACY_BURNING_FURNACE:
                case ENDER_CHEST:
                case TRAPPED_CHEST:
                case HOPPER: {
                    int extra = data & 0x8;
                    int withoutFlags = data & ~0x8;
                    switch (withoutFlags) {
                        case 5:
                            this.data = 2 | extra;
                            break;
                        case 4:
                            this.data = 3 | extra;
                            break;
                        case 2:
                            this.data = 4 | extra;
                            break;
                        case 3:
                            this.data = 5 | extra;
                            break;
                    }
                    break;
                }
                case DISPENSER:
                case DROPPER:
                    int dispPower = data & 0x8;
                    switch (data & ~0x8) {
                        case 5:
                            this.data = 2 | dispPower;
                            break;
                        case 4:
                            this.data = 3 | dispPower;
                            break;
                        case 2:
                            this.data = 4 | dispPower;
                            break;
                        case 3:
                            this.data = 5 | dispPower;
                            break;
                    }
                    break;
                case PUMPKIN:
                case JACK_O_LANTERN:
                    switch (data) {
                        case 1:
                            this.data = 0;
                            break;
                        case 2:
                            this.data = 1;
                            break;
                        case 3:
                            this.data = 2;
                            break;
                        case 0:
                            this.data = 3;
                            break;
                    }
                    break;

                case HAY_BLOCK:
                case OAK_LOG:
                case BIRCH_LOG:
                case SPRUCE_LOG:
                case JUNGLE_LOG:
                    if (data >= 4 && data <= 11)
                        data ^= 0xc;
                    break;

                case LEGACY_DIODE_BLOCK_OFF:
                case LEGACY_DIODE_BLOCK_ON:
                case LEGACY_REDSTONE_COMPARATOR_OFF:
                case LEGACY_REDSTONE_COMPARATOR_ON:
                    int dir = data & 0x03;
                    int delay = data - dir;
                    switch (dir) {
                        case 1:
                            this.data = 0 | delay;
                            break;
                        case 2:
                            this.data = 1 | delay;
                            break;
                        case 3:
                            this.data = 2 | delay;
                            break;
                        case 0:
                            this.data = 3 | delay;
                            break;
                    }
                    break;

                case OAK_TRAPDOOR:
                case IRON_TRAPDOOR:
                    int withoutOrientation = data & ~0x3;
                    int orientation = data & 0x3;
                    switch (orientation) {
                        case 3:
                            this.data = 0 | withoutOrientation;
                            break;
                        case 2:
                            this.data = 1 | withoutOrientation;
                            break;
                        case 0:
                            this.data = 2 | withoutOrientation;
                            break;
                        case 1:
                            this.data = 3 | withoutOrientation;
                            break;
                    }

                case PISTON_HEAD:
                case STICKY_PISTON:
                case MOVING_PISTON:
                    final int rest = data & ~0x7;
                    switch (data & 0x7) {
                        case 5:
                            this.data = 2 | rest;
                            break;
                        case 4:
                            this.data = 3 | rest;
                            break;
                        case 2:
                            this.data = 4 | rest;
                            break;
                        case 3:
                            this.data = 5 | rest;
                            break;
                    }
                    break;
                case RED_MUSHROOM:
                case BROWN_MUSHROOM:
                    if (data >= 10) {
                        this.data = data;
                        break;
                    }
                    this.data = (data * 7) % 10;
                case VINE:
                    this.data = ((data >> 1) | (data << 3)) & 0xf;
                    break;
                case ANVIL:
                    int damage = data & ~0x3;
                    switch (data & 0x3) {
                        case 0:
                            this.data = 1 | damage;
                            break;
                        case 2:
                            this.data = 3 | damage;
                            break;
                        case 1:
                            this.data = 2 | damage;
                            break;
                        case 3:
                            this.data = 0 | damage;
                            break;
                    }
                    break;
                case LEGACY_SKULL:
                    switch (data) {
                        case 2:
                            this.data = 4;
                            break;
                        case 3:
                            this.data = 5;
                            break;
                        case 4:
                            this.data = 3;
                            break;
                        case 5:
                            this.data = 2;
                            break;
                    }
            }

            if (cmat.isBed()) {
                this.data = data & ~0x3 | (data - 1) & 0x3;
            } else if (cmat.isGate()) {
                this.data = ((data + 3) & 0x3) | (data & ~0x3);
            } else if (cmat.isStairs()) {
                switch (data) {
                    case 2:
                        this.data = 0;
                        break;
                    case 3:
                        this.data = 1;
                        break;
                    case 1:
                        this.data = 2;
                        break;
                    case 0:
                        this.data = 3;
                        break;
                    case 6:
                        this.data = 4;
                        break;
                    case 7:
                        this.data = 5;
                        break;
                    case 5:
                        this.data = 6;
                        break;
                    case 4:
                        this.data = 7;
                        break;
                }
            }

            if (type.name().contains("TERRACOTTA")) {
                int extra = data & ~0x3;
                int withoutFlags = data & 0x3;
                switch (withoutFlags) {
                    case 1:
                        this.data = 0 | extra;
                        break;
                    case 2:
                        this.data = 1 | extra;
                        break;
                    case 3:
                        this.data = 2 | extra;
                        break;
                    case 0:
                        this.data = 3 | extra;
                        break;
                }
            }
        } catch (Exception e) {
            this.data = data;
        }
        return this;
    }

    public Object getData() {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            return this.blockd == null ? this.block.getBlockData().clone() : this.blockd;
        }
        if (data == null)
            return this.block.getData();
        return data.byteValue();
    }

    public CMIBlock setData(Object data) {
        if (Version.isCurrentEqualOrHigher(Version.v1_13_R1)) {
            if (data != null) {
                org.bukkit.block.data.BlockData blockData = (org.bukkit.block.data.BlockData) data;
                this.blockd = blockData.clone();
            } else
                this.blockd = data;
        } else {
            try {
                this.data = (int) data;
            } catch (ClassCastException e) {

            }
        }
        return this;
    }

    public boolean hasInventory() {
        return block.getState() instanceof InventoryHolder;
    }

    public Inventory getInventory() {
        if (block.getState() instanceof InventoryHolder) {
            InventoryHolder holder = (InventoryHolder) block.getState();
            return holder.getInventory();
        }
        return null;
    }

    public Block getSecondaryBedBlock() {

        if (block == null || !CMIMaterial.isBed(block.getType()))
            return null;

        BlockFace facing = getFacing();
        BedPart part = getBedPart();

        if (facing == null || part == null)
            return null;

        Location loc = block.getLocation().clone();

        switch (facing) {
            case WEST:
                switch (part) {
                    case FOOT:
                        return loc.add(-1, 0, 0).getBlock();
                    case HEAD:
                        return loc.add(1, 0, 0).getBlock();
                }
                break;
            case EAST:
                switch (part) {
                    case FOOT:
                        return loc.add(1, 0, 0).getBlock();
                    case HEAD:
                        return loc.add(-1, 0, 0).getBlock();
                }
                break;
            case NORTH:
                switch (part) {
                    case FOOT:
                        return loc.add(0, 0, -1).getBlock();
                    case HEAD:
                        return loc.add(0, 0, 1).getBlock();
                }
                break;
            case SOUTH:
                switch (part) {
                    case FOOT:
                        return loc.add(0, 0, 1).getBlock();
                    case HEAD:
                        return loc.add(0, 0, -1).getBlock();
                }
                break;
        }

        return null;
    }

    public Block getBedFootBlock() {

        if (block == null || !CMIMaterial.isBed(block.getType()))
            return null;

        Block sec = getSecondaryBedBlock();

        Location loc = this.block.getLocation();
        if (getBedPart() != null)
            if (getBedPart() == BedPart.FOOT) {
                return block;
            } else if (sec != null) {
                CMIBlock cbs = new CMIBlock(sec);
                if (cbs.getBedPart() != null && cbs.getBedPart() == BedPart.FOOT) {
                    return sec;
                }
            }

        return null;
    }

    public static enum blockDirection {
        none(-1), upWest(0), upEast(1), upNorth(2), upSouth(3), downWest(4), downEast(5), downNorth(6), downSouth(7);
        private int dir;

        blockDirection(int dir) {
            this.dir = dir;
        }

        public static blockDirection getByDir(int dir) {
            for (blockDirection one : blockDirection.values()) {
                if (one.getDir() == dir)
                    return one;
            }
            return null;
        }

        public int getDir() {
            return dir;
        }
    }

    public static enum FlipDirection {
        NORTH_SOUTH,
        WEST_EAST,
        UP_DOWN
    }

    public static enum Bisect {
        TOP,
        BOTTOM
    }

    public static enum BedPart {
        HEAD,
        FOOT

    }

    public static enum StairShape {
        INNER_LEFT,
        INNER_RIGHT,
        OUTER_LEFT,
        OUTER_RIGHT,
        STRAIGHT;

        public static StairShape getByName(String name) {
            for (StairShape one : StairShape.values()) {
                if (one.toString().equalsIgnoreCase(name))
                    return one;
            }
            return null;
        }
    }
}
