package hzc.himusic.service;

import hzc.himusic.util.GlobalConsts;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
/**
 * 播放音乐相关Service服务
 */
public class PlayMusicService extends Service{
	private MediaPlayer mediaPlayer;
	private boolean isLoop = true;
	
	/** 创建Service时执行1次 */
	public void onCreate() {
		mediaPlayer = new MediaPlayer();
		//注册mediaPlayer的监听 
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				//准备完成  开始播放
				mediaPlayer.start();
				//发送广播 -> 音乐已经开始播放
				Intent intent = new Intent(GlobalConsts.ACTION_MUSIC_STARTED);
				sendBroadcast(intent);
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				//next() 通知Activity播完了
			}
		});
		//启动更新音乐进度的线程
		new UpdateProgressThread().start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new MusicBinder();
	}
	
	
	@Override
	public void onDestroy() {
		mediaPlayer.release();  //释放资源
		isLoop = false;
		super.onDestroy();
	}
	
	public class MusicBinder extends Binder{
		
		/**
		 * 播放或暂停
		 */
		public void startOrPause(){
			if(mediaPlayer.isPlaying()){
				mediaPlayer.pause();
			}else{
				mediaPlayer.start();
			}
		}
		
		/**
		 * 定位到某个位置 继续播放、暂停
		 * @param position
		 */
		public void seekTo(int position){
			mediaPlayer.seekTo(position);
		}
		
		/**
		 * 供客户端调用的接口方法
		 * @param url
		 */
		public void playMusic(String url){
			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(url);
				mediaPlayer.prepareAsync();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 更新进度
	 * 每1S发送一次广播
	 */
	class UpdateProgressThread extends Thread{
		public void run() {
			while(isLoop){
				try {
					Thread.sleep(1000);
					if(mediaPlayer.isPlaying()){
						//发送广播
						Intent intent = new Intent(GlobalConsts.ACTION_UPDATE_PROGRESS);
						int current=mediaPlayer.getCurrentPosition();
						int total=mediaPlayer.getDuration();
						intent.putExtra("current", current);
						intent.putExtra("total", total);
						sendBroadcast(intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
}



