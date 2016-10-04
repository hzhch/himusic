package hzc.himusic.util;

import hzc.himusic.R;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;
import android.widget.ImageView;

/**
 * 图片加载相关操作的工具类
 * 自动实现：
 * 图片异步批量加载业务
 * 图片的内存缓存
 * 图片的文件缓存
 */
public class ImageLoader {
	private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	private Context context;
	// 准备任务集合
	private List<ImageLoadTask> tasks = new ArrayList<ImageLoadTask>();
	// 声明轮循任务集合的工作线程
	private Thread workThread;
	private boolean isLoop = true;
	private AbsListView listView;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_LOAD_IMAGE_SUCCESS: //图片下载成功
				ImageLoadTask task = (ImageLoadTask) msg.obj;
				Bitmap b = task.bitmap;
				ImageView imageView=(ImageView) listView.findViewWithTag(task.path);
				if(imageView!=null){
					if(b!=null){
						imageView.setImageBitmap(b);
					}else{
						imageView.setImageResource(R.drawable.ic_launcher);
					}
				}
				break;
			}
		}
	};
	public static final int HANDLER_LOAD_IMAGE_SUCCESS=1;

	
	public ImageLoader(Context context, AbsListView listView) {
		this.context = context;
		this.listView = listView;
		// 初始化工作线程并且启动
		workThread = new Thread() {
			// 不断的轮循任务集合 查看集合中是否有任务对象
			// 如果有，则获取任务对象发送图片请求。
			// 然后继续轮循集合查看下一个任务对象。
			public void run() {
				while (isLoop) {
					if (!tasks.isEmpty()) {// 集合中有任务对象
						ImageLoadTask task = tasks.remove(0);
						Bitmap bitmap = loadBitmap(task.path);
						task.bitmap = bitmap;
						// 想办法设置ImageView（主线程）
						Message msg = new Message();
						msg.what = HANDLER_LOAD_IMAGE_SUCCESS;
						msg.obj = task;
						handler.sendMessage(msg);
					} else { // 集合中已经没有任务了
						try {
							synchronized (workThread) {
								workThread.wait();// 线程等待
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} 

					}
				}
			}
		};
		workThread.start();
		
	}
	
	/**
	 * 发送http请求 通过path下载bitmap
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap loadBitmap(String path) {
		try {
			InputStream is = HttpUtils.getInputStream(path);
			Bitmap b = BitmapUtils.loadBitmap(is, 50, 50);
			//把bitmap 存入内存缓存中
			cache.put(path, new SoftReference<Bitmap>(b));
			//把bitmap 存入文件缓存
			String filename = path.substring(path.lastIndexOf("/")+1);
			File file = new File(context.getCacheDir(), "images/"+filename);
			BitmapUtils.save(b, file);
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 显示图片
	 * @param pic_small  图片路径
	 * @param ivAlbum  需要显示该图片的ImageView
	 */
	public void displayImage(String picPath, ImageView ivAlbum) {
		// 设置图片 ivAlbum
		ivAlbum.setTag(picPath);
		//先去内存缓存中查询  是否已经下载过该图片
		SoftReference<Bitmap> ref=cache.get(picPath);
		if(ref != null){//以前存过该图片
			Bitmap b = ref.get();  
			if(b!=null){  //以前存的图片还没有被GC销毁
				ivAlbum.setImageBitmap(b);
				return;
			}
		}
		
		//内存缓存中没有 则去文件缓存中查询
		String filename = picPath.substring(picPath.lastIndexOf("/")+1);
		File file = new File(context.getCacheDir(), "images/"+filename);
		Bitmap b = BitmapUtils.loadBitmap(file);
		if(b!=null){  //文件中有缓存图片
			ivAlbum.setImageBitmap(b);
			//向内存缓存中再存一次
			cache.put(picPath, new SoftReference<Bitmap>(b));
			return;
		}

		// new Thread(){}.start(); 线程数过多将会卡顿
		// 向任务集合中添加一个图片下载任务
		ImageLoadTask task = new ImageLoadTask();
		task.path = picPath;
		tasks.add(task);
		// 唤醒工作线程 赶紧起来干活
		synchronized (workThread) {
			workThread.notify();
		}

		
	}

	/**
	 * 停止工作线程
	 */
	public void stopThread(){
		isLoop = false;
		synchronized (workThread) {
			workThread.notify();
		}
	}
	
	/**
	 * 描述一个图片下载任务对象
	 */
	class ImageLoadTask {
		String path;
		Bitmap bitmap;
	}

	
}
