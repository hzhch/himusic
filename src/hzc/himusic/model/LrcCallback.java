package hzc.himusic.model;

import java.util.HashMap;

/**
 * �����صĻص��ӿ�
 * ��������ز�������Ϻ�  ���ýӿڷ���
 */
public interface LrcCallback {
	/**
	 * ��������ز�������Ϻ�  ���ýӿڷ���
	 * @param lrc
	 */
	public void onLrcLoaded(HashMap<String, String> lrc);
}



