package hzc.himusic.util;

import android.graphics.Bitmap;

/**
 * �첽����һ��ͼƬʹ�õĻص��ӿ�
 */
public interface BitmapCallback {
	/**
	 * ��ͼƬ������ϲ�������ȡbitmap��ִ�� 
	 * ���øûص�����
	 * @param bitmap
	 */
	public void onBitmapLoaded(Bitmap bitmap);
}
