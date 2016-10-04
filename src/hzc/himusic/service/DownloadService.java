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
 * 下载服务  IntentService 
 * 生命周期：
 * 1>当第一次启动IntentService时，Android容器
 *    将会创建IntentService对象。
 * 2>IntentService将会在工作线程中轮循消息队列，
 *    执行每个消息对象中的业务逻辑。
 * 3>如果消息队列中依然有消息，则继续执行，
 *    如果消息队列中的消息已经执行完毕，
 *    IntentService将会自动销毁，执行onDestroy方法。
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
	 * 该方法中的代码将会在工作线程中执行
	 * 每当调用startService启动IntentService后，
	 * IntentService将会把OnHandlerIntent中的
	 * 业务逻辑放入消息队列等待执行。
	 * 当工作线程轮循到该消息对象时，将会
	 * 执行该方法。
	 */
	protected void onHandleIntent(Intent intent) {
		//发送Http请求 执行下载业务
		//1.  获取音乐的路径
		String url=intent.getStringExtra("url");
		String bit=intent.getStringExtra("bit");
		String title=intent.getStringExtra("title");
		//2.  构建File对象，用于保存音乐文件
		//     /mnt/sdcard/Music/_64/歌名.mp3
		File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),"_"+bit+"/"+title+".mp3" );                  
		if(targetFile.exists()){
			Log.i("info", "音乐已存在");
			return;
		}
		if(!targetFile.getParentFile().exists()){
			targetFile.getParentFile().mkdirs();
		}
		try {
			sendNotification("音乐开始下载", "音乐开始下载");
			//3.  发送Http请求，获取InputStream
			InputStream is = HttpUtils.getInputStream(url);
			//4.  边读取边保存到File对象中
			FileOutputStream fos = new FileOutputStream(targetFile);
			byte[] buffer = new byte[1024*100];
			int length=0;
			int current = 0;
			int total = Integer.parseInt(intent.getStringExtra("total"));
			while((length=is.read(buffer)) != -1){
				fos.write(buffer, 0, length);
				fos.flush();
				current += length;
				//通知下载的进度
				double progress = Math.floor(1000.0*current/total)/10;
				sendNotification("音乐开始下载", "下载进度："+progress+"%");
			}
			//5.  文件下载完成
			fos.close();
			cancelNotification(); //重新出现滚动消息
			sendNotification("音乐下载完成", "音乐下载完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发通知
	 */
	public void sendNotification(String ticker, String text){
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("音乐下载")
			.setContentText(text)
			.setTicker(ticker);
		Notification n = builder.build();
		manager.notify(NOTIFICATION_ID, n);
	}
	
	/**
	 * 取消通知
	 */
	public void cancelNotification(){
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
	}
	
	
}








