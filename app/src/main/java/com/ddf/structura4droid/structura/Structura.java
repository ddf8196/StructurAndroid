package com.ddf.structura4droid.structura;

import com.nukkitx.nbt.NbtMap;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static com.ddf.structura4droid.structura.FileUtil.copyFile;

public class Structura {
    public static File generatePack(
            String packName,
            double opacity,
            boolean simple,
            Map<String, String> models,
            Map<String, double[]> offsets,
            boolean makeMaterialsList
    ) {
        String visualName = packName;
        //makes a render controller class that we will use to hide models
        RenderController rc = new RenderController();
        //makes a armor stand entity class that we will use to add models
        ArmorStand armorStandEntity = new ArmorStand();
        //manifest is mostly hard coded in this function.
        Manifest.export(visualName);

        //repeat for each structure after you get it to work
        //create a base animation controller for us to put pose changes into
        Animations animation = new Animations("Vanilla_Resource_Pack");
        int longestY = 0;
        boolean updateAnimation = true;

        for (String modelName : models.keySet()) {
            double[] offest = offsets.get(modelName);
            rc.addModel(modelName);
            armorStandEntity.addModel(modelName);
            copyFile(models.get(modelName), true, packName + "/" + modelName + ".mcstructure", false);

            ProcessStructure struct2make = new ProcessStructure(models.get(modelName));
            ArmorStandGeo armorStand = new ArmorStandGeo(modelName, opacity, offest, new int[]{64, 64, 64}, "Vanilla_Resource_Pack");

            int[] size = struct2make.getSize();
            int xlen = size[0], ylen = size[1], zlen = size[2];
            if (ylen > longestY) {
                updateAnimation = true;
                longestY = ylen;
            } else {
                updateAnimation = false;
            }

            for (int y = 0; y < ylen; y++) {
                armorStand.makeLayer(y);
                if (updateAnimation) {
                    animation.insertLayer(y);
                }
                for (int x = 0; x < xlen; x++) {
                    for (int z = 0; z < zlen; z++) {
                        NbtMap block = struct2make.getBlock(x, y, z);
                        String blockName = block.getString("name").replaceAll("minecraft:", "");
                        BlockProp blockProp = processBlock(block);
                        try {
                            armorStand.makeBlock(x, y, z, blockName, blockProp.rot, blockProp.top, blockProp.data, blockProp.openBit, null, blockProp.variant);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("There is an unsuported block in this world and it was skipped");
                            System.out.printf("x:%s Y:%s Z:%s, Block:%s, Variant: %s%n", x, y, z, block.getString("name"), blockProp.variant);
                        }
                    }
                }
            }
            Map<String, Integer> allBlocks = struct2make.getBlockList();

            // call export fuctions
            armorStand.export(packName);
            animation.export(packName);

            //export the armorstand class
            armorStandEntity.export(packName);
            FileUtil.delete(packName + "/" + modelName + ".mcstructure");
        }
        // Copy my icons in
        copyFile("lookups/pack_icon.png", packName + "/pack_icon.png");
        // Adds to zip file a modified armor stand geometry to enlarge the render area of the entity
        String largerRender = "lookups/armor_stand.larger_render.geo.json";
        String largerRenderPath = packName + "/models/entity/" + "armor_stand.larger_render.geo.json";
        copyFile(largerRender, largerRenderPath);
        // the base render controller is hard coded and just copied in
        rc.export(packName);

        ZipFile zip = new ZipFile(FileUtil.getFile(packName + ".mcpack"));
        zip.setCharset(StandardCharsets.UTF_8);
        zip.setRunInThread(false);
        try {
            // add all files to the mcpack file
            zip.addFolder(FileUtil.getFile(packName));
        } catch (ZipException e) {
            e.printStackTrace();
        }

        // delete all the extra files.
        FileUtil.rmdir(packName);

        return zip.getFile();
    }

    private static BlockProp processBlock(NbtMap block) {
        //everything below is handling the garbage mapping and naming in NBT
        //probably should be cleaned up into a helper function/library. for now it works-ish
        BlockProp blockProp = new BlockProp(0, false, false, 0);
        blockProp.setVariant(null, null);
        if (block.getCompound("states").containsKey("wall_block_type")) {
            blockProp.setVariant("wall_block_type", block.getCompound("states").get("wall_block_type"));
        }
        if (block.getCompound("states").containsKey("wood_type")) {
            blockProp.setVariant("wood_type",block.getCompound("states").get("wood_type"));
            if (block.getString("name").equals("minecraft:wood")) {
                Object keys = block.getCompound("states").get("wood_type");
                if (((Number) block.getCompound("states").get("stripped_bit")).byteValue() != 0) {
                    keys += "_stripped";
                }
                blockProp.setVariant("wood", keys);
            }
        }

        if (block.getCompound("states").containsKey("old_log_type")) {
            blockProp.setVariant("old_log_type", block.getCompound("states").get("old_log_type"));
        }
        if (block.getCompound("states").containsKey("new_log_type")) {
            blockProp.setVariant("new_log_type", block.getCompound("states").get("new_log_type"));
        }
        if (block.getCompound("states").containsKey("stone_type")) {
            blockProp.setVariant("stone_type", block.getCompound("states").get("stone_type"));
        }
        if (block.getCompound("states").containsKey("prismarine_block_type")) {
            blockProp.setVariant("prismarine_block_type", block.getCompound("states").get("prismarine_block_type"));
        }
        if (block.getCompound("states").containsKey("stone_brick_type")) {
            blockProp.setVariant("stone_brick_type", block.getCompound("states").get("stone_brick_type"));
        }
        if (block.getCompound("states").containsKey("color")) {
            blockProp.setVariant("color", block.getCompound("states").get("color"));
        }
        if (block.getCompound("states").containsKey("sand_stone_type")) {
            blockProp.setVariant("sand_stone_type", block.getCompound("states").get("sand_stone_type"));
        }
        if (block.getCompound("states").containsKey("stone_slab_type")) {
            blockProp.setVariant("stone_slab_type", block.getCompound("states").get("stone_slab_type"));
        }
        if (block.getCompound("states").containsKey("stone_slab_type_2")) {
            blockProp.setVariant("stone_slab_type_2", block.getCompound("states").get("stone_slab_type_2"));
        }
        if (block.getCompound("states").containsKey("stone_slab_type_3")) {
            blockProp.setVariant("stone_slab_type_3", block.getCompound("states").get("stone_slab_type_3"));
        }
        if (block.getCompound("states").containsKey("stone_slab_type_4")) {
            blockProp.setVariant("stone_slab_type_4", block.getCompound("states").get("stone_slab_type_4"));
        }

        if (block.getCompound("states").containsKey("facing_direction")) {
            blockProp.rot = block.getCompound("states").get("facing_direction");
        }
        if (block.getCompound("states").containsKey("direction")) {
            blockProp.rot = block.getCompound("states").get("direction");
        }
        if (block.getCompound("states").containsKey("torch_facing_direction")) {
            blockProp.rot = block.getCompound("states").get("torch_facing_direction");
        }
        if (block.getCompound("states").containsKey("weirdo_direction")) {
            blockProp.rot = block.getCompound("states").get("weirdo_direction");
        }
        if (block.getCompound("states").containsKey("upside_down_bit")) {
            blockProp.top = Byte.parseByte(block.getCompound("states").get("upside_down_bit").toString()) != 0;
        }
        if (block.getCompound("states").containsKey("top_slot_bit")) {
            blockProp.top = Byte.parseByte(block.getCompound("states").get("top_slot_bit").toString()) != 0;
        }
        if (block.getCompound("states").containsKey("open_bit")) {
            blockProp.openBit = Byte.parseByte(block.getCompound("states").get("open_bit").toString()) != 0;
        }
        if (block.getCompound("states").containsKey("repeater_delay")) {
            blockProp.data = Integer.parseInt(block.getCompound("states").get("repeater_delay").toString());
        }
        if (block.getCompound("states").containsKey("output_subtract_bit")) {
            blockProp.data = Integer.parseInt(block.getCompound("states").get("output_subtract_bit").toString());
        }
        return blockProp;
    }

    private static class BlockProp {
        Object rot;
        boolean top;
        Pair<String, Object> variant;
        boolean openBit;
        int data;

        BlockProp(Object rot, boolean top, boolean openBit, int data) {
            this.rot = rot;
            this.top = top;
            this.openBit = openBit;
            this.data = data;
        }

        void setVariant(String key, Object value) {
            variant = new Pair<>(key, value);
        }
    }
}
