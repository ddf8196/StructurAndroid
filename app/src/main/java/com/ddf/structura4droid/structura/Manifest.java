package com.ddf.structura4droid.structura;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class Manifest {
    public static void export(String packName) {
        JSONObject manifest = JSONUtil.load("{\"format_version\":2,\"header\":{\"name\":\"\",\"description\":\"Structura block overlay pack, created by \\u00a7o\\u00a75DrAv0011\\u00a7r, \\u00a7o\\u00a79FondUnicycle\\u00a7r and \\u00a7o\\u00a75RavinMaddHatter\\u00a7r\",\"uuid\":\"\",\"version\":[0,0,1],\"min_engine_version\":[1,16,0]},\"modules\":[{\"type\":\"resources\",\"uuid\":\"\",\"version\":[0,0,1]}]}");
        try {
            manifest.optJSONObject("header").put("name", packName);
            manifest.optJSONObject("header").put("uuid", UUID.randomUUID().toString());
            manifest.optJSONArray("modules").optJSONObject(0).put("uuid", UUID.randomUUID().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String path = packName + "/manifest.json";
        FileUtil.mkdirs(FileUtil.dirName(path));
        try (RandomAccessFile jsonFile = FileUtil.open(path, "rw")) {
            JSONUtil.dump(manifest, jsonFile, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
