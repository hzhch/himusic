package hzc.himusic.util;

import android.graphics.Bitmap;

/**
 * 异步加载一张图片使用的回调接口
 */
public interface BitmapCallback {
	/**
	 * 当图片加载完毕并解析获取bitmap后执行 
	 * 调用该回调方法
	 * @param bitmap
	 */
	public void onBitmapLoaded(Bitmap bitmap);
}
