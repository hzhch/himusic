package hzc.himusic.model;

import hzc.himusic.entity.Music;

import java.util.List;

/**
 * �����б�Ļص��ӿ�
 *  �����ּ�����Ϻ�ִ�иûص�����
 */
public interface MusicListCallback {
	/**
	 * �����ּ�����Ϻ�ִ�иûص�����
	 */
	public void onMusicListLoaded(List<Music> musics);

}
