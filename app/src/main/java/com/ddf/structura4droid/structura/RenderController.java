package com.ddf.structura4droid.structura;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;

import static com.ddf.structura4droid.structura.JSONUtil.addOrGet;

public class RenderController {
    private JSONObject rc;
    private String rcname;
    private String geometry = "%s";
    private String textures = "%s";

    public RenderController() {
        try {
            rc = new JSONObject()
                    .put("format_version", "1.8.0")
                    .put("render_controllers", new JSONObject());
            rcname = "controller.render.armor_stand.ghost_blocks";
            addOrGet(addOrGet(rc, "render_controllers"), rcname)
                    .put("materials", new JSONArray()
                                    .put(0, new JSONObject()
                                                    .put("*", "Material.ghost_blocks")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addModel(String rawName) {
        String name = rawName.replaceAll(" ","_").toLowerCase();
        String newGeo = String.format("query.get_name == '%s' ? Geometry.ghost_blocks_%s : (%s)", rawName,name,"%s");
        geometry = String.format(geometry, newGeo);
        String newTexture = String.format("query.get_name == '%s' ? Texture.ghost_blocks_%s : (%s)", rawName,name,"%s");
        textures = String.format(textures, newTexture);
    }

    public void export(String packName) {
        geometry = String.format(geometry, "Geometry.default");
        textures = String.format(textures, "Texture.default");
        try {
            addOrGet(addOrGet(rc, "render_controllers"), rcname)
                    .put("geometry", geometry);
            addOrGet(addOrGet(rc, "render_controllers"), rcname)
                    .put("textures", new JSONArray()
                            .put(0, textures));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String rc = "armor_stand.ghost_blocks.render_controllers.json";
        String rcpath = packName + "/render_controllers/" + rc;
        FileUtil.mkdirs(FileUtil.dirName(rcpath));

        try (RandomAccessFile jsonFile = FileUtil.open(rcpath, "rw")) {
            JSONUtil.dump(this.rc, jsonFile, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
