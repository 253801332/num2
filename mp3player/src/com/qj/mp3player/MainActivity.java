package com.qj.mp3player;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{
private ArrayList<String> mp3List=new ArrayList<String>();
private Player player;
private ListView lv;
private TextView tv;
private Button bt0;
private Button bt1;
private Button bt2;
private ServiceConnection conn;
private SeekBar pb;
private MyReceiver mReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/**
		 * 动态注册广播接收者 
		 */
		mReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.qj.PROGRESS");
		registerReceiver(mReceiver, filter);
		/**
		 *seekbar拖动定点播放 
		 */
		pb = ( SeekBar)findViewById(R.id.pb);
		pb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {			
			public void onStopTrackingTouch(SeekBar seekBar) {
				player.seekBarMp3(seekBar.getProgress());				
			}			
			public void onStartTrackingTouch(SeekBar seekBar) {	
			}			
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		/**
		 * 扫描音乐文件
		 */		
		String mp3Path = Environment.getExternalStorageDirectory() + "/mp3";
		File file = new File(mp3Path);
		File[] files = file.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String filePath = files[i].getPath();
				if (filePath.endsWith(".mp3")) {
					mp3List.add(filePath);
				}
			}
		}		
		/**
		 *开启服务 		 
		 */
		Intent intent=new Intent(this,Mp3Service.class);
		intent.putStringArrayListExtra("mp3List", mp3List);
		startService(intent);				
		//绑定服务
		Intent intent1=new Intent(this,Mp3Service.class);	
		conn=new Myconn();
		bindService(intent1, conn, BIND_AUTO_CREATE);				
		//得到listview
		lv=(ListView) findViewById(R.id.lv);
		lv.setAdapter(new BaseAdapter() {			
			public View getView(int position, View convertView, ViewGroup parent) {
				View view =null;
				if (view==null) {
					view=View.inflate(MainActivity.this, R.layout.item, null);
				}else{
					view =convertView;
				}
				tv=(TextView)view.findViewById(R.id.tv);
				String []name={"月亮之上","年轻的朋友来相会","发如雪","我爱人民币"};
				//String mpath=mp3List.get(position).replace(mp3List.get(position), name[position]);
				tv.setText(mp3List.get(position).replace(mp3List.get(position), name[position]));				
				return view ;
			}			
			public long getItemId(int position) {
				return 0;
			}			
			public Object getItem(int position) {
				return null;
			}			
			public int getCount() {
				return mp3List.size();
			}
		});
		/**
		 *设置条目点击事件 
		 */
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				player.play(mp3List.get(position));
			}
		});	
		bt0=(Button) findViewById(R.id.bt0);
		bt0.setOnClickListener(this);
		bt1=(Button) findViewById(R.id.bt1);
		bt1.setOnClickListener(this);
		bt2=(Button) findViewById(R.id.bt2);
		bt2.setOnClickListener(this);		
	}
	/**
	 * 播放点击事件
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt0:
				player.play(mp3List.get(0));							
			break;
		case R.id.bt1:
			player.pause();
			break;
		case R.id.bt2:
			player.stop();
			break;
		}		
	}
	//解除绑定
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		unregisterReceiver(mReceiver);
	}
	/**
	 * 实现ServiceConnection
	 */
	public class Myconn implements ServiceConnection{
		public void onServiceConnected(ComponentName name, IBinder service) {
			player = (Player) service;
		}
		public void onServiceDisconnected(ComponentName name) {
		}	
	}	
	/**
	 * 退出提醒
	 */
	public void onBackPressed() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("提醒:");
		builder.setMessage("亲，要后台播放吗");
		builder.setPositiveButton("后台播放", new DialogInterface.OnClickListener() {		
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.setNegativeButton("停止播放", new DialogInterface.OnClickListener() {		
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(MainActivity.this,Mp3Service.class);
				stopService(intent);
				finish();
			}
		});
		builder.show();
	}
	/**
	 * 广播接受者，同步条 
	 */	
	private class MyReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			int max = intent.getIntExtra("max", 0);
			int progress = intent.getIntExtra("progress", 0);			
			if (max != 0) {
				pb.setMax(max);
				pb.setProgress(0);
			}
			if (progress != 0) {
				pb.setProgress(progress);
			}
		}
	}
}
