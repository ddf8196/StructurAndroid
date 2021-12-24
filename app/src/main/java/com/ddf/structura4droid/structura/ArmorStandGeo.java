package com.ddf.structura4droid.structura;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.ddf.structura4droid.structura.JSONUtil.addOrGet;

public class ArmorStandGeo {
    private static final List<String> excluded = Arrays.asList("air", "grass", "structure_block");

    private String refResourcePack;
    private JSONObject blocksDef;
    private JSONObject terrainTexture;
    private JSONObject blockRotations;
    private JSONObject blockVariants;
    private JSONObject hacks;
    private JSONObject defs;
    private JSONObject block_shapes;
    private JSONObject block_uv;

    private String name;
    private JSONObject stand;
    private double[] offsets;
    private double alpha;
    private JSONObject geometry;
    private Map<String, Integer> uv_map;
    private JSONObject blocks;
    private int[] size;


    private Image uvImage;
    private JSONArray lower_objects;
    private JSONArray slab_like_objects;


    public ArmorStandGeo(String name, double alpha, double[] offsets, int[] size, String refPack) {
        refResourcePack = refPack;
        blocksDef = loadJsonFromPack("/blocks.json");
        terrainTexture = loadJsonFromPack("/textures/terrain_texture.json");
        blockRotations = loadJson("lookups/block_rotation.json");
        blockVariants = loadJson("lookups/variants.json");
        hacks = loadJson("lookups/hacks.json");
        defs = loadJson("lookups/block_definition.json");
        block_shapes = loadJson("lookups/block_shapes.json");
        block_uv = loadJson("lookups/block_uv.json");

        this.name = name.replaceAll(" ","_").toLowerCase();
        stand = new JSONObject();
        this.offsets = offsets;
        this.alpha = alpha;

        geometry = new JSONObject();
        standInit();
        uv_map = new HashMap<>();
        blocks = new JSONObject();
        this.size = size;

        lower_objects = hacks.optJSONArray("slab_like");
        slab_like_objects = hacks.optJSONArray("trapdoor_like");
    }

    private JSONObject loadJsonFromPack(String path) {
        return loadJson(refResourcePack + path);
    }

    private JSONObject loadJson(String path) {
        try (RandomAccessFile f = FileUtil.open(path)) {
            return JSONUtil.load(f);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void export(String packFolder) {
        addBlocksToBones();
        try {
            addOrGet(geometry, "description")
                    .put("texture_height", uv_map.size());
            stand.put("minecraft:geometry", new JSONArray().put(geometry));
            String path_to_geo = packFolder + "/models/entity/armor_stand.ghost_blocks_" + name + ".geo.json";
            FileUtil.mkdirs(FileUtil.dirName(path_to_geo));
            JSONArray bones = stand.optJSONArray("minecraft:geometry").optJSONObject(0).optJSONArray("bones");
            for (int i = 0, index = 0; index < bones.length(); index++) {
                JSONObject bone = bones.optJSONObject(index);
                if (!bone.has("name")) {
                    bone.put("name", "empty_row+" + i);
                    bone.put("parent", "ghost_blocks");
                    bone.put("pivot", new JSONArray(new double[]{0.5,0.5,0.5}));
                    i++;
                }
            }

            try (RandomAccessFile jsonFile = FileUtil.open(path_to_geo, "rw")) {
                JSONUtil.dump(stand, jsonFile, 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String texture_name = packFolder + "/textures/entity/ghost_blocks_" + name + ".png";
            FileUtil.mkdirs(FileUtil.dirName(texture_name));
            saveUV(texture_name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void makeLayer(int y) {
        String layer_name = "layer_" + y;
        try {
            geometry.getJSONArray("bones")
                    .put(new JSONObject()
                            .put("name", layer_name)
                            .put("pivot", new JSONArray(new int[]{-8, 0, 8}))
                            .put("parent", "ghost_blocks"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void makeBlock(int x, int y, int z, String blockName, Object rot, boolean top, int data, boolean trapOpen, String parent, Pair<String, Object> variant) {
        if (excluded.contains(blockName)) {
            return;
        }
        String ghost_block_name = "block_" + x + "_" + y + "_" + z;
        addOrGet(blocks, ghost_block_name);
        String block_type = defs.optString(blockName);
        String shape_variant = "default";
        if (block_type.equals("hopper") && !(rot instanceof Number && ((Number) rot).intValue() == 0)) {
            shape_variant = "side";
        } else if (block_type.equals("trapdoor") && trapOpen) {
            shape_variant = "open";
        } else if (top) {
            shape_variant = "top";
        }
        if (data != 0)
            System.out.println(data);
        JSONObject block_shapes = this.block_shapes.optJSONObject(block_type).optJSONObject(shape_variant);
        JSONObject block_uv = this.block_uv.optJSONObject(block_type).optJSONObject("default");
        if (this.block_uv.optJSONObject(block_type).has(shape_variant)) {
            block_uv = this.block_uv.optJSONObject(block_type).optJSONObject(shape_variant);
        }

        if (this.block_uv.optJSONObject(block_type).has(String.valueOf(data))) {
            shape_variant = String.valueOf(data);
        }

        if (this.block_shapes.optJSONObject(block_type).has(String.valueOf(data))) {
            block_shapes = this.block_shapes.optJSONObject(block_type).optJSONObject(String.valueOf(data));
            System.out.println(block_shapes);
        }

        int[] rotation;
        if (blockRotations.has(block_type)) {
            rotation = JSONUtil.toIntArray(this.blockRotations.optJSONObject(block_type).optJSONArray(String.valueOf(rot)));
        } else {
            rotation = new int[]{0, 0, 0};
        }

        try {
            blocks.optJSONObject(ghost_block_name).put("cubes", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int uv_idx = 0;
        for (int i = 0; i < block_shapes.optJSONArray("size").length(); i++) {
            JSONObject blockUV = this.blockNameToUV(blockName, variant, shape_variant, i, 0);
            JSONObject block = new JSONObject();
            if (block_uv.optJSONObject("uv_sizes").optJSONArray("up").length() > i) {
                uv_idx = i;
            }

            double xoff = 0;
            double yoff = 0;
            double zoff = 0;
            if (block_shapes.has("offsets")) {
                xoff = block_shapes.optJSONArray("offsets").optJSONArray(i).optDouble(0);
                yoff = block_shapes.optJSONArray("offsets").optJSONArray(i).optDouble(1);
                zoff = block_shapes.optJSONArray("offsets").optJSONArray(i).optDouble(2);
            }

            try {
                block.put("origin",
                        new JSONArray()
                                .put(-1*(x + this.offsets[0]) + xoff)
                                .put(y + yoff + this.offsets[1])
                                .put(z + zoff + this.offsets[2]));
                block.put("size", block_shapes.optJSONArray("size").optJSONArray(i));
                block.put("inflate", -0.03);
                block.put("pivot",
                        new JSONArray()
                                .put(-1*(x + this.offsets[0]) + 0.5)
                                .put(y + 0.5 + this.offsets[1])
                                .put(z + 0.5 + this.offsets[2]));
                block.put("rotation", new JSONArray(rotation));

                JSONObject up = blockUV.optJSONObject("up");
                JSONObject down = blockUV.optJSONObject("down");
                JSONObject east = blockUV.optJSONObject("east");
                JSONObject west = blockUV.optJSONObject("west");
                JSONObject north = blockUV.optJSONObject("north");
                JSONObject south = blockUV.optJSONObject("south");
                JSONArray uvUp = up.optJSONArray("uv");
                uvUp.put(0, uvUp.getDouble(0) + block_uv.optJSONObject("offset").optJSONArray("up").optJSONArray(uv_idx).optDouble(0));
                uvUp.put(1, uvUp.getDouble(1) + block_uv.optJSONObject("offset").optJSONArray("up").optJSONArray(uv_idx).optDouble(1));
                JSONArray uvDown = down.optJSONArray("uv");
                uvDown.put(0, uvDown.getDouble(0) + block_uv.optJSONObject("offset").optJSONArray("down").optJSONArray(uv_idx).optDouble(0));
                uvDown.put(1, uvDown.getDouble(1) + block_uv.optJSONObject("offset").optJSONArray("down").optJSONArray(uv_idx).optDouble(1));
                JSONArray uvEast = east.optJSONArray("uv");
                uvEast.put(0, uvEast.getDouble(0) + block_uv.optJSONObject("offset").optJSONArray("east").optJSONArray(uv_idx).optDouble(0));
                uvEast.put(1, uvEast.getDouble(1) + block_uv.optJSONObject("offset").optJSONArray("east").optJSONArray(uv_idx).optDouble(1));
                JSONArray uvWest = west.optJSONArray("uv");
                uvWest.put(0, uvWest.getDouble(0) + block_uv.optJSONObject("offset").optJSONArray("west").optJSONArray(uv_idx).optDouble(0));
                uvWest.put(1, uvWest.getDouble(1) + block_uv.optJSONObject("offset").optJSONArray("west").optJSONArray(uv_idx).optDouble(1));
                JSONArray uvNorth = north.optJSONArray("uv");
                uvNorth.put(0, uvNorth.getDouble(0) + block_uv.optJSONObject("offset").optJSONArray("north").optJSONArray(uv_idx).optDouble(0));
                uvNorth.put(1, uvNorth.getDouble(1) + block_uv.optJSONObject("offset").optJSONArray("north").optJSONArray(uv_idx).optDouble(1));
                JSONArray uvSouth = south.optJSONArray("uv");
                uvSouth.put(0, uvSouth.getDouble(0) + block_uv.optJSONObject("offset").optJSONArray("south").optJSONArray(uv_idx).optDouble(0));
                uvSouth.put(1, uvSouth.getDouble(1) + block_uv.optJSONObject("offset").optJSONArray("south").optJSONArray(uv_idx).optDouble(1));
                up.put("uv_size", block_uv.optJSONObject("uv_sizes").optJSONArray("up").opt(uv_idx));
                down.put("uv_size", block_uv.optJSONObject("uv_sizes").optJSONArray("down").opt(uv_idx));
                east.put("uv_size", block_uv.optJSONObject("uv_sizes").optJSONArray("east").opt(uv_idx));
                west.put("uv_size", block_uv.optJSONObject("uv_sizes").optJSONArray("west").opt(uv_idx));
                north.put("uv_size", block_uv.optJSONObject("uv_sizes").optJSONArray("north").opt(uv_idx));
                south.put("uv_size", block_uv.optJSONObject("uv_sizes").optJSONArray("south").opt(uv_idx));

                block.put("uv", blockUV);
                this.blocks.optJSONObject(ghost_block_name).optJSONArray("cubes").put(block);
                this.blocks.optJSONObject(ghost_block_name).put("name", ghost_block_name);
                this.blocks.optJSONObject(ghost_block_name).put("parent", "layer_" + y);
                this.blocks.optJSONObject(ghost_block_name).put("pivot", block_shapes.optJSONArray("center"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void saveUV(String name) {
        if (uvImage != null)
            uvImage.save(FileUtil.getFile(name));
    }

    public void standInit() {
        try {
            stand.put("format_version", "1.12.0");
            geometry.put("description", new JSONObject().put("identifier", "geometry.armor_stand.ghost_blocks_" + name));
            geometry.optJSONObject("description").put("texture_width", 1);
            geometry.optJSONObject("description").put("visible_bounds_offset", new JSONArray(new double[]{0.0, 1.5, 0.0}));
            geometry.optJSONObject("description").put("visible_bounds_width", 5120);
            geometry.optJSONObject("description").put("visible_bounds_height", 5120);
            stand.put("minecraft:geometry", new JSONArray().put(geometry));
            geometry.put("bones",
                    new JSONArray()
                            .put(new JSONObject()
                                    .put("name", "ghost_blocks")
                                    .put("pivot", new JSONArray(new int[]{-8, 0, 8}))));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void extendUVImage(String newImageFilename) {
        Image image = new Image();
        image.load(FileUtil.getFile(newImageFilename));

        if (image.getWidth() > 16 || image.getHeight() > 16) {
            image.clip(0, 0, 16, 16);
        }
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setPixelA(x, y, (int) (image.getPixelA(x, y) * alpha));
            }
        }
        if (uvImage == null) {
            uvImage = image;
        } else {
            uvImage.draw(0, uvImage.getHeight(), image);
        }
    }

    public JSONObject blockNameToUV(String blockName, Pair<String, Object> variant, String shapeVariant, int index, int data) {
        JSONObject temp_uv = new JSONObject();

        if (excluded.contains(blockName)) {
            return temp_uv;
        }

        String block_type = defs.optString(blockName);
        Map<String, String> texture_files = getBlockTexturePaths(blockName, variant);
        JSONObject corrected_textures = null;
        if (block_uv.optJSONObject(block_type) != null) {
            if (block_uv.optJSONObject(block_type).has(shapeVariant)) {
                if (block_uv.optJSONObject(block_type).optJSONObject(shapeVariant).has("overwrite")) {
                    corrected_textures = block_uv.optJSONObject(block_type).optJSONObject(shapeVariant).optJSONObject("overwrite");
                }
            } else {
                if (block_uv.optJSONObject(block_type).optJSONObject("default").has("overwrite")) {
                    corrected_textures = block_uv.optJSONObject(block_type).optJSONObject("default").optJSONObject("overwrite");
                }
            }
        }

        if (corrected_textures == null) {
            corrected_textures = new JSONObject();
        }
        for (Iterator<String> it = corrected_textures.keys(); it.hasNext(); ) {
            String side = it.next();
            if (corrected_textures.optJSONArray(side).length() > index) {
                if (!corrected_textures.optJSONArray(side).optString(index).equals("default")) {
                    texture_files.put(side, corrected_textures.optJSONArray(side).optString(index));
                }
            }
        }
        for (String key : texture_files.keySet()) {
            if (!uv_map.containsKey(texture_files.get(key))) {
                extendUVImage(refResourcePack + "/" + texture_files.get(key) + ".png");
                uv_map.put(texture_files.get(key), uv_map.size());
            }
            try {
                temp_uv.put(key, new JSONObject()
                        .put("uv", new JSONArray(new int[]{0, uv_map.get(texture_files.get(key))}))
                        .put("uv_size", new JSONArray(new int[]{1, 1})));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return temp_uv;
    }

    public void addBlocksToBones() {
        for (Iterator<String> iterator = blocks.keys(); iterator.hasNext(); ) {
            String key = iterator.next();
            geometry.optJSONArray("bones").put(blocks.opt(key));
        }
    }

    public Map<String, String> getBlockTexturePaths(String blockName, Pair<String, Object> variant) {
        Object textureLayout = blocksDef.optJSONObject(blockName).opt("textures");
        JSONObject texturedata = terrainTexture.optJSONObject("texture_data");
        Map<String, String> textures = new HashMap<>();

        if (textureLayout instanceof JSONObject) {
            JSONObject layout = (JSONObject) textureLayout;
            if (layout.has("side")) {
                textures.put("east", layout.optString("side"));
                textures.put("west", layout.optString("side"));
                textures.put("north", layout.optString("side"));
                textures.put("south", layout.optString("side"));
            }
            if (layout.has("east")) {
                textures.put("east", layout.optString("east"));
            }
            if (layout.has("west")) {
                textures.put("west", layout.optString("west"));
            }
            if (layout.has("north")) {
                textures.put("north", layout.optString("north"));
            }
            if (layout.has("south")) {
                textures.put("south", layout.optString("south"));
            }
            if (layout.has("down")) {
                textures.put("down", layout.optString("down"));
            }
            if (layout.has("up")) {
                textures.put("up", layout.optString("up"));
            }
        } else if (textureLayout instanceof String) {
            String layout = (String) textureLayout;
            textures.put("east", layout);
            textures.put("west", layout);
            textures.put("north", layout);
            textures.put("south", layout);
            textures.put("down", layout);
            textures.put("up", layout);
        }

        for (String key : textures.keySet()) {
            Object obj = texturedata.optJSONObject(textures.get(key)).opt("textures");
            if (obj instanceof String) {
                textures.put(key, (String) obj);
            } else if (obj instanceof JSONArray) {
                int index = 0;
                if (blockVariants.has(variant.getKey())) {
                    index = blockVariants.optJSONObject(variant.getKey()).optInt(variant.getValue().toString());
                }
                textures.put(key, ((JSONArray) obj).optString(index));
            }
        }
        return textures;
    }
}
