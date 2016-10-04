package hzc.himusic.model;

import hzc.himusic.entity.Music;

import java.util.List;

/**
 * 当获取到搜索结果后执行该回调方法
 */
public interface SearchResultCallback {
	
	public void onResultLoaded(List<Music> musics);
}

