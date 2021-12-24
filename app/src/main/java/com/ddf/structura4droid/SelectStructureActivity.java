package com.ddf.structura4droid;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.FragmentActivity;

import com.ddf.structura4droid.structura.FileUtil;
import com.ddf.structura4droid.structura.Structura;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;

public class SelectStructureActivity extends FragmentActivity {
	private final List<File> structureFiles = new ArrayList<>();
	
	private LinearLayout loadingLinearLayout;
	private ListView structureListView;
	private StructuresListAdapter adapter;
	private LoadStructuresThread task;
	private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        filePath = intent.getStringExtra(filePath);

        setContentView(R.layout.select_structure);
		loadingLinearLayout = findViewById(R.id.loading_linear_layout);
		structureListView = findViewById(R.id.structure_list_view);
		adapter = new StructuresListAdapter();
		structureListView.setAdapter(adapter);
		structureListView.setOnItemClickListener(new StructuresListItemClickListener());
    }

    @SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		PermissionX.init(this)
				.permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
				.explainReasonBeforeRequest()
				.onExplainRequestReason(new ExplainReasonCallback() {
					@Override
					public void onExplainReason(ExplainScope scope, List<String> deniedList) {
						scope.showRequestReasonDialog(deniedList, "需要申请以下权限以加载已导出的结构", "确定");
					}
				})
				.onForwardToSettings(new ForwardToSettingsCallback() {
					@Override
					public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
						scope.showForwardToSettingsDialog(deniedList, "请到设置中手动授予相关权限", "确定");
					}
				})
				.request(new RequestCallback() {
					@Override
					public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
						if (allGranted) {
							loadStructures(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/games/com.mojang/structures"));
						} else {
						    StringBuilder msg = new StringBuilder("以下权限未授予, 无法加载结构: ");
                            for (String denied : deniedList) {
                                msg.append(denied).append(" ");
                            }
						    showToast(msg.toString());
						}
					}
				});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		task.cancel();
	}

	Handler handler = new Handler(Looper.myLooper()) {
		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					structureFiles.clear();
					structureFiles.addAll((List<File>) msg.obj);
					Collections.sort(structureFiles);
					loadingLinearLayout.setVisibility(View.GONE);
					structureListView.setVisibility(View.VISIBLE);
					adapter.notifyDataSetChanged();
					break;
				case 1:
					structureFiles.clear();
					adapter.notifyDataSetChanged();
					break;
			}

		}
	};

	private void loadStructures(File... dirs) {
		structureListView.setVisibility(View.GONE);
		loadingLinearLayout.setVisibility(View.VISIBLE);
		if (task != null) {
			task.cancel();
		}
		task = new LoadStructuresThread(handler, dirs);
		task.start();
	}

	private void showToast(int id) {
		Toast.makeText(this, id, Toast.LENGTH_LONG).show();
	}

	private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	static class LoadStructuresThread extends Thread {
		private final Handler handler;
		private final File[] dirs;
		private volatile boolean canceled = false;

		LoadStructuresThread(Handler handler, File... dirs) {
			this.handler = handler;
			this.dirs = dirs;
		}

		@Override
		public void run() {
			if (canceled) return;
			List<File> list = new ArrayList<>();
			for (File dir : dirs) {
				if (canceled) return;
				File[] structureFiles = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
						return !file.isDirectory() && file.getName().endsWith(".mcstructure");
					}
				});
				if (structureFiles == null) {
					handler.sendEmptyMessage(1);
					return;
				}
				for (File file : structureFiles) {
					list.add(file);
				}
			}

			Message message = new Message();
			message.what = 0;
			message.obj = list;
			if (canceled) return;
			handler.sendMessage(message);
		}

		public void cancel() {
			canceled = true;
			handler.sendEmptyMessage(1);
		}
	}
	
	class StructuresListAdapter extends BaseAdapter {
		private final LayoutInflater layoutInflater = LayoutInflater.from(SelectStructureActivity.this);
		
		@Override
		public int getCount() {
			return structureFiles.size();
		}

		@Override
		public File getItem(int position) {
			return structureFiles.get(position);
		}

		@Override
		public long getItemId(int position) {
			 return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null);
			}
			TextView text1 = convertView.findViewById(android.R.id.text1);
			TextView text2 = convertView.findViewById(android.R.id.text2);
			text1.setText(getItem(position).getName());
			text2.setText(getItem(position).getAbsolutePath());
			return convertView;
		}
	}
	
	class StructuresListItemClickListener implements AdapterView.OnItemClickListener {
		@SuppressWarnings("deprecation")
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			//Intent intent = new Intent(SelectStructureActivity.this, ExportStructureActivity.class);
			//intent.putExtra("world", structureFiles.get(position));
			//SelectStructureActivity.this.startActivity(intent);
			Map<String, String> models = new HashMap<>();
			models.put("", structureFiles.get(position).getAbsolutePath());
			Map<String, double[]> offsets = new HashMap<>();
			offsets.put("", new double[]{8, 0, 7});
			String name = structureFiles.get(position).getName();
			name = name.substring(0, name.lastIndexOf('.'));
			File pack = Structura.generatePack(name, 0.8f, true, models, offsets, false);
			File dir = new File(Environment.getExternalStorageDirectory(), "Structura4Droid");
			dir.mkdirs();
			File des = new File(dir, pack.getName());
			FileUtil.copyFile(pack.getAbsolutePath(), true, des.getPath(), true);
			pack.delete();
			showToast(getString(R.string.pack_saved_to, des.getAbsolutePath()));
		}
	}
	
}
