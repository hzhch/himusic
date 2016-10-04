package hzc.himusic.model;

import java.util.HashMap;

/**
 * 歌词相关的回调接口
 * 当歌词下载并解析完毕后  调用接口方法
 */
public interface LrcCallback {
	/**
	 * 当歌词下载并解析完毕后  调用接口方法
	 * @param lrc
	 */
	public void onLrcLoaded(HashMap<String, String> lrc);
}



