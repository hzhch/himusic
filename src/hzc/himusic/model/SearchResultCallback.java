package hzc.himusic.model;

import hzc.himusic.entity.Music;

import java.util.List;

/**
 * ����ȡ�����������ִ�иûص�����
 */
public interface SearchResultCallback {
	
	public void onResultLoaded(List<Music> musics);
}

