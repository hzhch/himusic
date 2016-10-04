package hzc.himusic.model;

import hzc.himusic.app.MusicApplication;
import hzc.himusic.entity.Music;
import hzc.himusic.entity.SongInfo;
import hzc.himusic.entity.SongUrl;
import hzc.himusic.util.HttpUtils;
import hzc.himusic.util.JSONParser;
import hzc.himusic.util.UrlFactory;
import hzc.himusic.util.XmlParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 音乐相关的业务类
 */
public class MusicModel {

	/**
	 * 通过关键字搜索音乐
	 * @param keyword  关键字
	 * @param callback  当搜索完成后 调用该回调方法
	 */
	public void searchMusicList(final String keyword, final SearchResultCallback callback){
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			protected List<Music> doInBackground(String... params) {
				String path = UrlFactory.getSearchMusicUrl(keyword);
				try {
					InputStream is = HttpUtils.getInputStream(path);
					String json=HttpUtils.isToString(is);
					//解析json  
					JSONObject obj = new JSONObject(json);
					JSONArray ary=obj.getJSONArray("song_list");
					List<Music> musics = JSONParser.parseSearchResult(ary);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(List<Music> result) {
				callback.onResultLoaded(result);
			}
		};
		task.execute();
	}
	
	/**
	 * 加载歌词信息
	 * @param lrcPath
	 * @param callback
	 */
	public void loadLrc(final String lrcPath, final LrcCallback callback){
		AsyncTask<String, String, HashMap<String, String>> task = new AsyncTask<String, String, HashMap<String,String>>(){
			protected HashMap<String, String> doInBackground(String... params) {
				try {
					//先去缓存目录中查找 是否已经下载过
					String filename = lrcPath.substring(lrcPath.lastIndexOf("/")+1);
					File file = new File(MusicApplication.getApp().getCacheDir(), "lrc/"+filename);
					if(file.exists()){ //歌词文件已经下载完毕
						FileInputStream fis = new FileInputStream(file);
						BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
						HashMap<String, String> lrc = new HashMap<String, String>();
						String line = null;
						while((line=reader.readLine())!=null){
							//line :  空字符串   
							if(!line.contains("[")){ //没有[   不解析
								continue;
							}
							//line :  [title]渡红尘
							if(!line.contains(".")){
								continue;
							}
							//line :  [00:01.75]渡红尘
							String time=line.substring(line.indexOf("[")+1, line.indexOf("."));
							String content = line.substring(line.lastIndexOf("]")+1);
							lrc.put(time, content);
						}
						return lrc;  //不再重新发送请求
					}
					
					if(!file.getParentFile().exists()){ 
						//父目录不存在则创建父目录
						file.getParentFile().mkdirs();
					}
					InputStream is = HttpUtils.getInputStream(lrcPath);
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					PrintWriter out = new PrintWriter(file);
					HashMap<String, String> lrc = new HashMap<String, String>();
					String line = null;
					while((line=reader.readLine())!=null){
						//存入缓存文件
						out.println(line);
						out.flush();
						//line :  空字符串   
						if(!line.contains("[")){ //没有[   不解析
							continue;
						}
						//line :  [title]渡红尘
						if(!line.contains(".")){
							continue;
						}
						//line :  [00:01.75]渡红尘
						String time=line.substring(line.indexOf("[")+1, line.indexOf("."));
						String content = line.substring(line.lastIndexOf("]")+1);
						Log.i("info", "time:"+time+"  content:"+content);
						lrc.put(time, content);
					}
					out.close();
					return lrc;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			protected void onPostExecute(HashMap<String, String> lrc) {
				callback.onLrcLoaded(lrc);
			}
		};
		task.execute();
	}
	
	/**
	 * 通过songid 加载音乐的基本信息
	 * @param songId
	 * @param callback
	 */
	public void loadSongInfoBySongId(final String songId, final SongInfoCallback callback){
		AsyncTask<String, String, Music> task = new AsyncTask<String, String, Music>(){
			//发送请求 通过songid获取基本信息
			protected Music doInBackground(String... params) {
				String path = UrlFactory.getSongInfoUrl(songId);
				try {
					InputStream is = HttpUtils.getInputStream(path);
					String json = HttpUtils.isToString(is);
					//解析json
					JSONObject obj = new JSONObject(json);
					JSONArray ary = obj.getJSONObject("songurl").getJSONArray("url");
					List<SongUrl> urls = JSONParser.parseSongUrls(ary);
					JSONObject infoObj = obj.getJSONObject("songinfo");
					SongInfo info = JSONParser.parseSongInfo(infoObj);
					Music m = new Music();
					m.setUrls(urls);
					m.setInfo(info);
					return m;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return new Music();
			}
			@Override
			protected void onPostExecute(Music m) {
				callback.onSongInfoLoaded(m.getUrls(), m.getInfo());
			}
		};
		task.execute();
	}
	
	/**
	 * 异步查询新歌榜列表
	 * @param offset
	 * @param size
	 */
	public void loadNewMusicList(final int offset, final  int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/*工作线程中执行  发送http请求 */
			protected List<Music> doInBackground(String... params) {
				try {
					String url = UrlFactory.getNewMusicListUrl(offset, size);
					InputStream is=HttpUtils.getInputStream(url);
					//解析XML
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/* 当doInBackground方法执行完毕后 
			 * 将会在主线程中执行onPostExecute */
			protected void onPostExecute(List<Music> musics) {
				Log.i("info", "音乐列表:"+musics.toString());
				//调用回调方法  
				callback.onMusicListLoaded(musics);
			}
		};
		//调用execute方法后将会在工作线程中
		//自动执行doInBackground
		task.execute();
	}

	
	/**
	 * 异步查询热歌榜列表
	 * @param offset
	 * @param size
	 */
	public void loadHotMusicList(final int offset, final  int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/*工作线程中执行  发送http请求 */
			protected List<Music> doInBackground(String... params) {
				try {
					String url = UrlFactory.getHotMusicListUrl(offset, size);
					InputStream is=HttpUtils.getInputStream(url);
					//解析XML
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/* 当doInBackground方法执行完毕后 
			 * 将会在主线程中执行onPostExecute */
			protected void onPostExecute(List<Music> musics) {
				Log.i("info", "音乐列表:"+musics.toString());
				//调用回调方法  
				callback.onMusicListLoaded(musics);
			}
		};
		//调用execute方法后将会在工作线程中
		//自动执行doInBackground
		task.execute();
	}

}



