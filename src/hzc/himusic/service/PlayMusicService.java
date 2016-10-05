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
 * �����������Service����
 */
public class PlayMusicService extends Service{
	private MediaPlayer mediaPlayer;
	private boolean isLoop = true;
	
	/** ����Serviceʱִ��1�� */
	public void onCreate() {
		mediaPlayer = new MediaPlayer();
		//ע��mediaPlayer�ļ��� 
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				//׼�����  ��ʼ����
				mediaPlayer.start();
				//���͹㲥 -> �����Ѿ���ʼ����
				Intent intent = new Intent(GlobalConsts.ACTION_MUSIC_STARTED);
				sendBroadcast(intent);
			}
		});
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				//next() ֪ͨActivity������
			}
		});
		//�����������ֽ��ȵ��߳�
		new UpdateProgressThread().start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new MusicBinder();
	}
	
	
	@Override
	public void onDestroy() {
		mediaPlayer.release();  //�ͷ���Դ
		isLoop = false;
		super.onDestroy();
	}
	
	public class MusicBinder extends Binder{
		
		/**
		 * ���Ż���ͣ
		 */
		public void startOrPause(){
			if(mediaPlayer.isPlaying()){
				mediaPlayer.pause();
			}else{
				mediaPlayer.start();
			}
		}
		
		/**
		 * ��λ��ĳ��λ�� �������š���ͣ
		 * @param position
		 */
		public void seekTo(int position){
			mediaPlayer.seekTo(position);
		}
		
		/**
		 * ���ͻ��˵��õĽӿڷ���
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
	 * ���½���
	 * ÿ1S����һ�ι㲥
	 */
	class UpdateProgressThread extends Thread{
		public void run() {
			while(isLoop){
				try {
					Thread.sleep(1000);
					if(mediaPlayer.isPlaying()){
						//���͹㲥
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



