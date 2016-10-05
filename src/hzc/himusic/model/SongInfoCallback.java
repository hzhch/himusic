package hzc.himusic.model;

import hzc.himusic.entity.SongInfo;
import hzc.himusic.entity.SongUrl;

import java.util.List;

/**
 * ͨ��songid�������ֻ�����Ϣ�ɹ���Ļص�
 */
public interface SongInfoCallback {
	/**
	 * ��MusicModel�е�������Ϣ������Ϻ�
	 * �����߳��е��øûص������� 
	 * @param urls
	 * @param info
	 */
	void onSongInfoLoaded(List<SongUrl> urls, SongInfo info);
}



