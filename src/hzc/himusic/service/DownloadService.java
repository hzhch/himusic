package hzc.himusic.service;

import hzc.himusic.R;
import hzc.himusic.util.HttpUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

/**
 * ���ط���  IntentService 
 * �������ڣ�
 * 1>����һ������IntentServiceʱ��Android����
 *    ���ᴴ��IntentService����
 * 2>IntentService�����ڹ����߳�����ѭ��Ϣ���У�
 *    ִ��ÿ����Ϣ�����е�ҵ���߼���
 * 3>�����Ϣ��������Ȼ����Ϣ�������ִ�У�
 *    �����Ϣ�����е���Ϣ�Ѿ�ִ����ϣ�
 *    IntentService�����Զ����٣�ִ��onDestroy������
 *
 */
public class DownloadService extends IntentService{

	private static final int NOTIFICATION_ID = 100;

	public DownloadService(){
		super("download");
	}
	
	public DownloadService(String name) {
		super(name);
	}

	/**
	 * �÷����еĴ��뽫���ڹ����߳���ִ��
	 * ÿ������startService����IntentService��
	 * IntentService�����OnHandlerIntent�е�
	 * ҵ���߼�������Ϣ���еȴ�ִ�С�
	 * �������߳���ѭ������Ϣ����ʱ������
	 * ִ�и÷�����
	 */
	protected void onHandleIntent(Intent intent) {
		//����Http���� ִ������ҵ��
		//1.  ��ȡ���ֵ�·��
		String url=intent.getStringExtra("url");
		String bit=intent.getStringExtra("bit");
		String title=intent.getStringExtra("title");
		//2.  ����File�������ڱ��������ļ�
		//     /mnt/sdcard/Music/_64/����.mp3
		File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),"_"+bit+"/"+title+".mp3" );                  
		if(targetFile.exists()){
			Log.i("info", "�����Ѵ���");
			return;
		}
		if(!targetFile.getParentFile().exists()){
			targetFile.getParentFile().mkdirs();
		}
		try {
			sendNotification("���ֿ�ʼ����", "���ֿ�ʼ����");
			//3.  ����Http���󣬻�ȡInputStream
			InputStream is = HttpUtils.getInputStream(url);
			//4.  �߶�ȡ�߱��浽File������
			FileOutputStream fos = new FileOutputStream(targetFile);
			byte[] buffer = new byte[1024*100];
			int length=0;
			int current = 0;
			int total = Integer.parseInt(intent.getStringExtra("total"));
			while((length=is.read(buffer)) != -1){
				fos.write(buffer, 0, length);
				fos.flush();
				current += length;
				//֪ͨ���صĽ���
				double progress = Math.floor(1000.0*current/total)/10;
				sendNotification("���ֿ�ʼ����", "���ؽ��ȣ�"+progress+"%");
			}
			//5.  �ļ��������
			fos.close();
			cancelNotification(); //���³��ֹ�����Ϣ
			sendNotification("�����������", "�����������");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��֪ͨ
	 */
	public void sendNotification(String ticker, String text){
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("��������")
			.setContentText(text)
			.setTicker(ticker);
		Notification n = builder.build();
		manager.notify(NOTIFICATION_ID, n);
	}
	
	/**
	 * ȡ��֪ͨ
	 */
	public void cancelNotification(){
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}
	
	
}








