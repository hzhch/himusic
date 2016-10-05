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
 * ͼƬ������ز����Ĺ�����
 * �Զ�ʵ�֣�
 * ͼƬ�첽��������ҵ��
 * ͼƬ���ڴ滺��
 * ͼƬ���ļ�����
 */
public class ImageLoader {
	private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
	private Context context;
	// ׼�����񼯺�
	private List<ImageLoadTask> tasks = new ArrayList<ImageLoadTask>();
	// ������ѭ���񼯺ϵĹ����߳�
	private Thread workThread;
	private boolean isLoop = true;
	private AbsListView listView;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case HANDLER_LOAD_IMAGE_SUCCESS: //ͼƬ���سɹ�
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
		// ��ʼ�������̲߳�������
		workThread = new Thread() {
			// ���ϵ���ѭ���񼯺� �鿴�������Ƿ����������
			// ����У����ȡ���������ͼƬ����
			// Ȼ�������ѭ���ϲ鿴��һ���������
			public void run() {
				while (isLoop) {
					if (!tasks.isEmpty()) {// ���������������
						ImageLoadTask task = tasks.remove(0);
						Bitmap bitmap = loadBitmap(task.path);
						task.bitmap = bitmap;
						// ��취����ImageView�����̣߳�
						Message msg = new Message();
						msg.what = HANDLER_LOAD_IMAGE_SUCCESS;
						msg.obj = task;
						handler.sendMessage(msg);
					} else { // �������Ѿ�û��������
						try {
							synchronized (workThread) {
								workThread.wait();// �̵߳ȴ�
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
	 * ����http���� ͨ��path����bitmap
	 * 
	 * @param path
	 * @return
	 */
	public Bitmap loadBitmap(String path) {
		try {
			InputStream is = HttpUtils.getInputStream(path);
			Bitmap b = BitmapUtils.loadBitmap(is, 50, 50);
			//��bitmap �����ڴ滺����
			cache.put(path, new SoftReference<Bitmap>(b));
			//��bitmap �����ļ�����
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
	 * ��ʾͼƬ
	 * @param pic_small  ͼƬ·��
	 * @param ivAlbum  ��Ҫ��ʾ��ͼƬ��ImageView
	 */
	public void displayImage(String picPath, ImageView ivAlbum) {
		// ����ͼƬ ivAlbum
		ivAlbum.setTag(picPath);
		//��ȥ�ڴ滺���в�ѯ  �Ƿ��Ѿ����ع���ͼƬ
		SoftReference<Bitmap> ref=cache.get(picPath);
		if(ref != null){//��ǰ�����ͼƬ
			Bitmap b = ref.get();  
			if(b!=null){  //��ǰ���ͼƬ��û�б�GC����
				ivAlbum.setImageBitmap(b);
				return;
			}
		}
		
		//�ڴ滺����û�� ��ȥ�ļ������в�ѯ
		String filename = picPath.substring(picPath.lastIndexOf("/")+1);
		File file = new File(context.getCacheDir(), "images/"+filename);
		Bitmap b = BitmapUtils.loadBitmap(file);
		if(b!=null){  //�ļ����л���ͼƬ
			ivAlbum.setImageBitmap(b);
			//���ڴ滺�����ٴ�һ��
			cache.put(picPath, new SoftReference<Bitmap>(b));
			return;
		}

		// new Thread(){}.start(); �߳������ཫ�Ῠ��
		// �����񼯺������һ��ͼƬ��������
		ImageLoadTask task = new ImageLoadTask();
		task.path = picPath;
		tasks.add(task);
		// ���ѹ����߳� �Ͻ������ɻ�
		synchronized (workThread) {
			workThread.notify();
		}

		
	}

	/**
	 * ֹͣ�����߳�
	 */
	public void stopThread(){
		isLoop = false;
		synchronized (workThread) {
			workThread.notify();
		}
	}
	
	/**
	 * ����һ��ͼƬ�����������
	 */
	class ImageLoadTask {
		String path;
		Bitmap bitmap;
	}

	
}
