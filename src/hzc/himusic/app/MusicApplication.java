package hzc.himusic.app;

import hzc.himusic.entity.Music;

import java.util.List;

import android.app.Application;

public class MusicApplication extends Application{
	private List<Music> musics;
	private int position;
	private static MusicApplication app;
	
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
	}
	
	public static MusicApplication getApp(){
		return app;
	}

	public void setMusicList(List<Music> musics){
		this.musics = musics;
	}
	
	public void setPosition(int position){
		this.position = position;
	}
	
	/**
	 * 获取当前正在播放的音乐
	 * @return
	 */
	public Music getCurrentMusic(){
		return musics.get(position);
	}
	
	/**
	 * 切换到上一首 并且返回歌曲对象
	 * @return
	 */
	public Music preMusic(){
		position = position ==0 ?  0 : position - 1;
		return getCurrentMusic();
	}
	
	/**
	 * 切换到下一首  并且返回当前需要播放的Music
	 * @return
	 */
	public Music nextMusic(){
		position = position == musics.size()-1 ? 0 : position+1;
		return getCurrentMusic();
	}
	
}


