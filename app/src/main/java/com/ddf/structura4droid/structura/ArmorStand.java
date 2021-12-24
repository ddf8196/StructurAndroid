package com.ddf.structura4droid.structura;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;

import static com.ddf.structura4droid.structura.JSONUtil.addOrGet;

public class ArmorStand {
    private static final JSONObject desc = JSONUtil.load("{\"identifier\":\"minecraft:armor_stand\",\"min_engine_version\":\"1.8.0\",\"materials\":{\"default\":\"armor_stand\",\"ghost_blocks\":\"entity_alphablend\"},\"animations\":{\"default_pose\":\"animation.armor_stand.default_pose\",\"no_pose\":\"animation.armor_stand.no_pose\",\"solemn_pose\":\"animation.armor_stand.solemn_pose\",\"athena_pose\":\"animation.armor_stand.athena_pose\",\"brandish_pose\":\"animation.armor_stand.brandish_pose\",\"honor_pose\":\"animation.armor_stand.honor_pose\",\"entertain_pose\":\"animation.armor_stand.entertain_pose\",\"salute_pose\":\"animation.armor_stand.salute_pose\",\"riposte_pose\":\"animation.armor_stand.riposte_pose\",\"zombie_pose\":\"animation.armor_stand.zombie_pose\",\"cancan_a_pose\":\"animation.armor_stand.cancan_a_pose\",\"cancan_b_pose\":\"animation.armor_stand.cancan_b_pose\",\"hero_pose\":\"animation.armor_stand.hero_pose\",\"wiggle\":\"animation.armor_stand.wiggle\",\"controller.pose\":\"controller.animation.armor_stand.pose\",\"controller.wiggling\":\"controller.animation.armor_stand.wiggle\",\"scale\":\"animation.armor_stand.ghost_blocks.scale\"},\"scripts\":{\"initialize\":[\"variable.armor_stand.pose_index = 0;\",\"variable.armor_stand.hurt_time = 0;\"],\"animate\":[\"controller.pose\",\"controller.wiggling\",\"scale\"]},\"render_controllers\":[\"controller.render.armor_stand\",\"controller.render.armor_stand.ghost_blocks\"],\"enable_attachables\":true}");
    private JSONObject stand;
    private JSONObject geos;
    private JSONObject textures;

    public ArmorStand() {
        try {
            stand = new JSONObject().put("format_version", "1.10.0");
            addOrGet(stand, "minecraft:client_entity")
                    .put("description", desc);
            geos = new JSONObject().put("default", "geometry.armor_stand.larger_render");
            textures = new JSONObject().put("default", "textures/entity/armor_stand");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addModel(String name) {
        String progName = "ghost_blocks_" + name.replaceAll(" ","_").toLowerCase();
        try {
            geos.put(progName, "geometry.armor_stand." + progName);
            textures.put(progName, "textures/entity/" + progName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void export(String packName) {
        try {
            addOrGet(addOrGet(stand, "minecraft:client_entity"), "description")
                    .put("textures", textures);
            addOrGet(addOrGet(stand, "minecraft:client_entity"), "description")
                    .put("geometry", geos);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String path = packName + "/entity/armor_stand.entity.json";
        FileUtil.mkdirs(FileUtil.dirName(path));

        try (RandomAccessFile jsonFile = FileUtil.open(path, "rw")) {
            JSONUtil.dump(stand, jsonFile, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
