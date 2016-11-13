package com.qj.mp3player;
import java.util.ArrayList;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class Mp3Service extends Service {
	private ArrayList<String> mp3List=new ArrayList<String>();
	private MediaPlayer mp;
	private int currentPosition;
	private boolean hasStart;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if (mp != null) {
				Intent intent = new Intent();
				intent.setAction("com.qj.PROGRESS");
				intent.putExtra("progress", mp.getCurrentPosition());
				sendBroadcast(intent);				
				mHandler.sendEmpt yMessageDelayed(0, 200);
			}
		}
	};
	private class MyBinder extends Binder implements Player{
		public void play(String position) {			
			playMp3(position);
		}
		public void pause() {			
			pauseMp3();
		}
		public void stop() {
			stopMp3();
		}	
		public void seekBarMp3(int position){
			seekBar(position);
		}
	}	
	public IBinder onBind(Intent intent) {		
		return new MyBinder();
	}
	/**
	 * 得到MP3列表
	 */

	public int onStartCommand(Intent intent, int flags, int startId) {
		mp3List=intent.getStringArrayListExtra("mp3List");		
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 播放方法
	 */
	public void playMp3(final String position){
			if (mp!=null) {
				mp.release();
				mp=null;
			}	
			try {
				mp=new MediaPlayer();
				mp.setDataSource(position);
				mp.setOnPreparedListener(new OnPreparedListener() {				
					public void onPrepared(MediaPlayer mp1) {  
						mp.start();	
						hasStart = true;
						int max = mp.getDuration();
						/**
						 *广播发送 
						 */
						Intent intent = new Intent();
						intent.setAction("com.qj.PROGRESS");
						intent.putExtra("max", max);
						sendBroadcast(intent);			
						mHandler.sendEmptyMessage(1);
					}
				});				
				mp.setOnErrorListener(new OnErrorListener() {		//播放失败		
					public boolean onError(MediaPlayer mp1, int what, int extra) {				
						return true;
					}
				});
				mp.setOnCompletionListener(new OnCompletionListener() {	//播放下一首			
					public void onCompletion(MediaPlayer mp1) {
						currentPosition=mp3List.indexOf(position);
						currentPosition+=1;
						if (currentPosition>=0&&currentPosition<mp3List.size()) {
							String newPosition=mp3List.get(currentPosition);
							playMp3(newPosition);
						}else {
							currentPosition=0;
							String newPosition=mp3List.get(currentPosition);
							playMp3(newPosition);
						}					
					}
				});
				mp.prepareAsync();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	/**
	 * 暂停/播放
	 */
	public void pauseMp3(){
		if (mp != null && mp.isPlaying()) {
		mp.pause();
		}else if (mp!= null) {
		mp.start();
		}
	}
	/**
	 * 停止播放 
	 */
	public void stopMp3(){
		if (mp != null) {
			mp.stop();
			mp.release();
			mp= null;
		}
	}
	private void seekBar(int position){
		if (mp!=null) {
			mp.seekTo(position);
		}		
	}	
	@Override
	public void onDestroy() {
		if (mp != null) {
			mp.release();
			mp = null;
		}
		super.onDestroy();
	}
}
