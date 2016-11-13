package com.qj.mp3player;

import android.content.Intent;
import android.os.IBinder;

public interface Player {
	public void play(String position);
	public void pause();
	public void stop();
	public void seekBarMp3(int position);

}