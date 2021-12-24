package com.ddf.structura4droid.structura;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class FileUtil {
    private static String workDirectory;

    public static void setWorkDirectory(String workDirectory) {
        FileUtil.workDirectory = workDirectory;
    }

    public static void copyFile(String srcPath, String desPath) {
        copyFile(srcPath, false, desPath, false);
    }

    public static void copyFile(String srcPath, boolean isSrcAbsolute, String desPath, boolean isDesAbsolute) {
        if (!isSrcAbsolute) {
            srcPath = processPath(srcPath);
        }
        if (!isDesAbsolute) {
            desPath = processPath(desPath);
        }
        try (FileChannel input = new FileInputStream(srcPath).getChannel();
             FileChannel output = new FileOutputStream(desPath).getChannel()
        ) {
            output.transferFrom(input, 0, input.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean exists(String path) {
        path = processPath(path);
        return new File(path).exists();
    }

    public static void writeFile(InputStream input, RandomAccessFile file) {
        byte[] buf = new byte[1024];
        int n;
        try {
            while ((n = input.read(buf)) >= 0) {
                file.write(buf, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean delete(String path) {
        path = processPath(path);
        return new File(path).delete();
    }

    public static boolean rmdir(String path) {
        path = processPath(path);
        File dir = new File(path);
        deleteDir(dir);
        return true;
    }

    private static void deleteDir(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDir(f);
                }
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static boolean mkdirs(String path) {
        path = processPath(path);
        return new File(path).mkdirs();
    }

    public static RandomAccessFile open(String path) {
        return open(path, "r");
    }
    public static RandomAccessFile open(String path, String mode) {
        path = processPath(path);
        try {
            return new RandomAccessFile(path, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static OutputStream openOutputStream(String path) {
        path = processPath(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            return fileOutputStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getFile(String path) {
        return new File(processPath(path));
    }

    public static String dirName(String path) {
        File parent = new File(path).getParentFile();
        if (parent == null) {
            return "/";
        }
        return parent.getPath();
    }

    private static String processPath(String relativePath) {
        return workDirectory + File.separator + relativePath;
    }
}
