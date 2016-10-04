package hzc.himusic.util;

import hzc.himusic.app.MusicApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;

/**
 * 图片操作的工具类
 */
public class BitmapUtils {
	
	/**
	 * 异步模糊化处理图片
	 * @param bitmap  源图片
	 * @param raduis  模糊半径
	 * @param callback  处理完毕后执行的回调
	 */
	public static void loadBlurBitmap(final Bitmap bitmap, final int raduis, final  BitmapCallback callback){
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			protected Bitmap doInBackground(String... params) {
				Bitmap b = createBlurBitmap(bitmap, raduis);
				return b;
			}
			protected void onPostExecute(Bitmap b) {
				callback.onBitmapLoaded(b);
			}
		};
		task.execute();
	}
	
	/**
	 * 传递bitmap 传递模糊半径 返回一个被模糊的bitmap
	 * 比较耗时
	 * @param sentBitmap
	 * @param radius
	 * @return
	 */
	public static Bitmap createBlurBitmap(Bitmap sentBitmap, int radius) {
		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
		if (radius < 1) {
			return (null);
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);
		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];
		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);

		}
		yw = yi = 0;
		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;
		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}

			}
			stackpointer = radius;
			for (x = 0; x < w; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);

				}
				p = pix[yw + vmin[x]];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi++;

			}
			yw += w;

		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				sir = stack[i + radius];
				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];
				rbs = r1 - Math.abs(i);
				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

				}
				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;
				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];
				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];
				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;

				}
				p = x + vmin[y];
				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];
				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];
				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;
				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];
				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];
				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];
				yi += w;
			}
		}
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

	/**
	 * 通过路径加载图片  通过比例进行压缩
	 * @param path
	 * @param scale
	 * @param callback
	 */
	public static void loadBitmap(final String path, final int scale, final BitmapCallback callback){
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//在工作线程中执行
			protected Bitmap doInBackground(String... params) {
				Bitmap b = null;
				try {
					String filename = path.substring(path.lastIndexOf("/")+1);
					File file = new File(MusicApplication.getApp().getCacheDir(), "images/"+filename);
					//先去文件缓存中寻找 是否已经下载过
					Options opts= new Options();
					opts.inSampleSize = scale;
					if(file.exists()){
						b = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
						return b;
					}
					//下载图片
					InputStream is = HttpUtils.getInputStream(path);
					b=BitmapFactory.decodeStream(is);
					//把图片存入文件缓存中 供下次使用
					save(b, file);
					//再从文件中按照压缩比例读取一次
					b = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
					return b;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			//主线程中执行
			protected void onPostExecute(Bitmap b) {
				callback.onBitmapLoaded(b);
			}
		};
		task.execute();

	}
	
	/**
	 * 通过路径加载图片 
	 * @param path http://开头的网络路径
	 */
	public static void loadBitmap(final String path, final BitmapCallback callback){
		AsyncTask<String, String, Bitmap> task = new AsyncTask<String, String, Bitmap>(){
			//在工作线程中执行
			protected Bitmap doInBackground(String... params) {
				try {
					String filename = path.substring(path.lastIndexOf("/")+1);
					File file = new File(MusicApplication.getApp().getCacheDir(), "images/"+filename);
					//先去文件缓存中寻找 是否已经下载过
					Bitmap b=loadBitmap(file);
					if(b!=null){ //文件中有
						return b;
					}
					InputStream is = HttpUtils.getInputStream(path);
					b=BitmapFactory.decodeStream(is);
					//把图片存入文件缓存中 供下次使用
					save(b, file);
					return b;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			//主线程中执行
			protected void onPostExecute(Bitmap b) {
				callback.onBitmapLoaded(b);
			}
		};
		task.execute();
	}
	
	/**
	 * 通过文件获取Bitmap
	 * @param file
	 * @return
	 */
	public static Bitmap loadBitmap(File file){
		if(!file.exists()){
			return null; //文件不存在
		}
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}
	
	/**
	 * 保存图片
	 * @param bitmap  原图片
	 * @param file  目标目录
	 * @throws FileNotFoundException 
	 */
	public static void save(Bitmap bitmap, File file) throws FileNotFoundException{
		if(!file.getParentFile().exists()){ //父目录不存在
			file.getParentFile().mkdirs();
		}
		FileOutputStream fos = new FileOutputStream(file);
		//压缩并输出
		bitmap.compress(CompressFormat.JPEG, 100, fos);
		
	}
	

	/**
	 * 通过输入流读取图片  在读取的过程中执行压缩图片
	 * @param is 输入流
	 * @param width  图片的目标宽度
	 * @param height  图片的目标高度
	 * @return
	 * @throws IOException 
	 */
	public static Bitmap loadBitmap(InputStream is, int width, int height) throws IOException {
		//先把输入流中的内容读取成byte[]
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024*5];
		int length =0;
		while((length=is.read(buffer)) != -1){
			os.write(buffer, 0, length);
			os.flush();
		}
		byte[] bytes = os.toByteArray();
		os.close();
		//通过byte数组使用BitmapFactory解析图片的原始尺寸
		Options opts = new Options();
		//仅仅加载边界属性  (边界属性包含图片原始的宽度和高度)
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
		int w = opts.outWidth / width;
		int h = opts.outHeight / height;
		//通过原始尺寸计算出压缩比例
		int scale = w > h ? h : w;
		//再次解析byte[]  需要给出Options.inSampleSize
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = scale;
		//获取压缩过后的图片
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
	}

}
