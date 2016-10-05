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
 * ������ص�ҵ����
 */
public class MusicModel {

	/**
	 * ͨ���ؼ�����������
	 * @param keyword  �ؼ���
	 * @param callback  ��������ɺ� ���øûص�����
	 */
	public void searchMusicList(final String keyword, final SearchResultCallback callback){
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			protected List<Music> doInBackground(String... params) {
				String path = UrlFactory.getSearchMusicUrl(keyword);
				try {
					InputStream is = HttpUtils.getInputStream(path);
					String json=HttpUtils.isToString(is);
					//����json  
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
	 * ���ظ����Ϣ
	 * @param lrcPath
	 * @param callback
	 */
	public void loadLrc(final String lrcPath, final LrcCallback callback){
		AsyncTask<String, String, HashMap<String, String>> task = new AsyncTask<String, String, HashMap<String,String>>(){
			protected HashMap<String, String> doInBackground(String... params) {
				try {
					//��ȥ����Ŀ¼�в��� �Ƿ��Ѿ����ع�
					String filename = lrcPath.substring(lrcPath.lastIndexOf("/")+1);
					File file = new File(MusicApplication.getApp().getCacheDir(), "lrc/"+filename);
					if(file.exists()){ //����ļ��Ѿ��������
						FileInputStream fis = new FileInputStream(file);
						BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
						HashMap<String, String> lrc = new HashMap<String, String>();
						String line = null;
						while((line=reader.readLine())!=null){
							//line :  ���ַ���   
							if(!line.contains("[")){ //û��[   ������
								continue;
							}
							//line :  [title]�ɺ쳾
							if(!line.contains(".")){
								continue;
							}
							//line :  [00:01.75]�ɺ쳾
							String time=line.substring(line.indexOf("[")+1, line.indexOf("."));
							String content = line.substring(line.lastIndexOf("]")+1);
							lrc.put(time, content);
						}
						return lrc;  //�������·�������
					}
					
					if(!file.getParentFile().exists()){ 
						//��Ŀ¼�������򴴽���Ŀ¼
						file.getParentFile().mkdirs();
					}
					InputStream is = HttpUtils.getInputStream(lrcPath);
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					PrintWriter out = new PrintWriter(file);
					HashMap<String, String> lrc = new HashMap<String, String>();
					String line = null;
					while((line=reader.readLine())!=null){
						//���뻺���ļ�
						out.println(line);
						out.flush();
						//line :  ���ַ���   
						if(!line.contains("[")){ //û��[   ������
							continue;
						}
						//line :  [title]�ɺ쳾
						if(!line.contains(".")){
							continue;
						}
						//line :  [00:01.75]�ɺ쳾
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
	 * ͨ��songid �������ֵĻ�����Ϣ
	 * @param songId
	 * @param callback
	 */
	public void loadSongInfoBySongId(final String songId, final SongInfoCallback callback){
		AsyncTask<String, String, Music> task = new AsyncTask<String, String, Music>(){
			//�������� ͨ��songid��ȡ������Ϣ
			protected Music doInBackground(String... params) {
				String path = UrlFactory.getSongInfoUrl(songId);
				try {
					InputStream is = HttpUtils.getInputStream(path);
					String json = HttpUtils.isToString(is);
					//����json
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
	 * �첽��ѯ�¸���б�
	 * @param offset
	 * @param size
	 */
	public void loadNewMusicList(final int offset, final  int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/*�����߳���ִ��  ����http���� */
			protected List<Music> doInBackground(String... params) {
				try {
					String url = UrlFactory.getNewMusicListUrl(offset, size);
					InputStream is=HttpUtils.getInputStream(url);
					//����XML
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/* ��doInBackground����ִ����Ϻ� 
			 * ���������߳���ִ��onPostExecute */
			protected void onPostExecute(List<Music> musics) {
				Log.i("info", "�����б�:"+musics.toString());
				//���ûص�����  
				callback.onMusicListLoaded(musics);
			}
		};
		//����execute�����󽫻��ڹ����߳���
		//�Զ�ִ��doInBackground
		task.execute();
	}

	
	/**
	 * �첽��ѯ�ȸ���б�
	 * @param offset
	 * @param size
	 */
	public void loadHotMusicList(final int offset, final  int size, final MusicListCallback callback) {
		AsyncTask<String, String, List<Music>> task = new AsyncTask<String, String, List<Music>>(){
			/*�����߳���ִ��  ����http���� */
			protected List<Music> doInBackground(String... params) {
				try {
					String url = UrlFactory.getHotMusicListUrl(offset, size);
					InputStream is=HttpUtils.getInputStream(url);
					//����XML
					List<Music> musics = XmlParser.parseMusicList(is);
					return musics;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			/* ��doInBackground����ִ����Ϻ� 
			 * ���������߳���ִ��onPostExecute */
			protected void onPostExecute(List<Music> musics) {
				Log.i("info", "�����б�:"+musics.toString());
				//���ûص�����  
				callback.onMusicListLoaded(musics);
			}
		};
		//����execute�����󽫻��ڹ����߳���
		//�Զ�ִ��doInBackground
		task.execute();
	}

}



