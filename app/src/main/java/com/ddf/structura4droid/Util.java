package com.ddf.structura4droid;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

public class Util {
    public static byte[] readAll(String path) {
        return readAll(new File(path));
    }

    public static byte[] readAll(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            byte[] array = new byte[fileInputStream.available()];
            fileInputStream.read(array);
            return array;
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static String readAsString(String path) {
        return new String(readAll(path), StandardCharsets.UTF_8);
    }
}
