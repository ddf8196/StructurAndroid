package com.ddf.structura4droid;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.ddf.structura4droid.structura.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();
		File filesDir = getFilesDir();
		FileUtil.setWorkDirectory(filesDir.getAbsolutePath());
		extractAssets(getAssets(), "lookups");
		extractAssets(getAssets(), "Vanilla_Resource_Pack");
	}

	private static void extractAssets(AssetManager assets, String path) {
		try {
			String[] list = assets.list(path);
			if (list.length > 0) {
				for (String name : list) {
					if (path != null && path.endsWith("/")) {
						path = path.substring(0, path.length() - 1);
					}
					if (!TextUtils.isEmpty(path)) {
						name = path + "/" + name;
					}
					String[] list1 = assets.list(name);
					if (list1.length > 0) {
						extractAssets(assets, name);
					} else {
						if (FileUtil.exists(name)) {
							continue;
						}
						FileUtil.mkdirs(FileUtil.dirName(name));
						try (InputStream is = assets.open(name); RandomAccessFile file = FileUtil.open(name, "rw")) {
							FileUtil.writeFile(is, file);
						}
					}
				}
			} else {
				if (FileUtil.exists(path)) {
					return;
				}
				FileUtil.mkdirs(FileUtil.dirName(path));
				try (InputStream is = assets.open(path); RandomAccessFile file = FileUtil.open(path, "rw")) {
					FileUtil.writeFile(is, file);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
