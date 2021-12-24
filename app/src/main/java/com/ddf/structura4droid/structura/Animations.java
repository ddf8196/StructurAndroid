package com.ddf.structura4droid.structura;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;

import static com.ddf.structura4droid.structura.JSONUtil.addOrGet;

public class Animations {
    private static final String[] poses = new String[] {
            "animation.armor_stand.default_pose",
            "animation.armor_stand.no_pose",
            "animation.armor_stand.solemn_pose",
            "animation.armor_stand.athena_pose",
            "animation.armor_stand.brandish_pose",

            "animation.armor_stand.honor_pose",
            "animation.armor_stand.entertain_pose",
            "animation.armor_stand.salute_pose",
            "animation.armor_stand.riposte_pose",
            "animation.armor_stand.zombie_pose",

            "animation.armor_stand.cancan_a_pose",
            "animation.armor_stand.cancan_b_pose",
            "animation.armor_stand.hero_pose"
    };
    private static final JSONObject defaultSize = JSONUtil.load("{\"format_version\": \"1.8.0\",\"animations\": {\"animation.armor_stand.ghost_blocks.scale\": {\"loop\": true,\"bones\": {\"ghost_blocks\": {\"scale\": 16.0}}}}}");

    private JSONObject sizing;

    public Animations(String pathToDefault) {
         sizing = JSONUtil.load(FileUtil.open(pathToDefault + "/animations/armor_stand.animation.json"));
    }

    public void insertLayer(int y) {
        String name = "layer_" + y;
        //# self.sizing["animations"][self.poses[0]]["bones"][name]={"scale":16}
        for (int i = 0; i < 12; i++) {
            if (y % (12) != i) {
                //# self.sizing["animations"][self.poses[i+1]]["bones"][name]={"scale":16}
                try {
                    addOrGet(addOrGet(addOrGet(addOrGet(sizing, "animations"), poses[i + 1]), "bones"), name)
                            .put("scale", 0.08);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void export(String packName) {
        String pathToAni = packName + "/animations/armor_stand.animation.json";
        FileUtil.mkdirs(FileUtil.dirName(pathToAni));
        try (RandomAccessFile jsonFile = FileUtil.open(pathToAni, "rw")) {
            JSONUtil.dump(sizing, jsonFile, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String pathToRc = packName + "/animations/armor_stand.ghost_blocks.scale.animation.json";
        FileUtil.mkdirs(FileUtil.dirName(pathToRc));
        try (RandomAccessFile jsonFile = FileUtil.open(pathToRc, "rw")) {
            JSONUtil.dump(defaultSize, jsonFile, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
