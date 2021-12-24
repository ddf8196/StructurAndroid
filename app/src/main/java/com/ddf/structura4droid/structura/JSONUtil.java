package com.ddf.structura4droid.structura;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class JSONUtil {
    public static JSONObject addOrGet(JSONObject self, String name) {
        JSONObject obj = self.optJSONObject(name);
        if (obj == null) {
            obj = new JSONObject();
            try {
                self.put(name, obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> keySet(JSONObject self) {
        try {
            Method method = JSONObject.class.getMethod("keySet");
            return (Set<String>) method.invoke(self);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int[] toIntArray(JSONArray self) {
        int[] result = new int[self.length()];
        for (int i = 0; i < self.length(); i++) {
            result[i] = self.optInt(i);
        }
        return result;
    }
    public static JSONObject load(RandomAccessFile file) {
        try {
            byte[] bytes = new byte[(int) file.length()];
            file.readFully(bytes);
            return load(new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject load(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void dump(JSONObject json, DataOutput output, int indentSpaces) {
        try {
            output.write(json.toString(indentSpaces).getBytes(StandardCharsets.UTF_8));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
