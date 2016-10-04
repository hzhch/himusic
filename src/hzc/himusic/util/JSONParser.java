package hzc.himusic.util;

import hzc.himusic.entity.Music;
import hzc.himusic.entity.SongInfo;
import hzc.himusic.entity.SongUrl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * json解析工具类
 */
public class JSONParser {

	/**
	 * 通过jsonArray解析songUrl集合
	 * @param ary [{},{},{},{}]
	 * @return
	 * @throws JSONException 
	 */
	public static List<SongUrl> parseSongUrls(JSONArray ary) throws JSONException {
		List<SongUrl> urls= new ArrayList<SongUrl>();
 		for(int i=0; i<ary.length(); i++){
 			JSONObject o = ary.getJSONObject(i);
			SongUrl url = new SongUrl(
					o.getString("song_file_id"), 
					o.getString("file_size"), 
					o.getString("file_duration"), 
					o.getString("file_bitrate"), 
					o.getString("show_link"), 
					o.getString("file_extension"), 
					o.getString("file_link")
			);
			urls.add(url);
		}
		return urls;
	}

	/**
	 * 通过jsonobject 解析 songInfo对象
	 * @param infoObj
	 * @return
	 * @throws JSONException 
	 */
	public static SongInfo parseSongInfo(JSONObject o) throws JSONException {
		SongInfo info = new SongInfo(
				o.getString("pic_huge"), 
				o.getString("album_1000_1000"), 
				o.getString("album_500_500"), 
				o.getString("compose"), 
				o.getString("bitrate"), 
				o.getString("artist_500_500"), 
				o.getString("album_title"), 
				o.getString("title"), 
				o.getString("hot"), 
				o.getString("language"), 
				o.getString("lrclink"), 
				o.getString("pic_big"), 
				o.getString("pic_premium"), 
				o.getString("artist_480_800"), 
				o.getString("artist_id"), 
				o.getString("album_id"), 
				o.getString("artist_1000_1000"), 
				o.getString("all_artist_id"), 
				o.getString("artist_640_1136"), 
				o.getString("songwriting"), 
				o.getString("share_url"), 
				o.getString("author"), 
				o.getString("pic_small"), 
				o.getString("song_id")
		);
		return info;
	}

	/**
	 * 解析jsonArray  搜索结果列表
	 * @param ary [{},{},{},{}]
	 * @return
	 */
	public static List<Music> parseSearchResult(JSONArray ary)throws JSONException {
		List<Music> musics = new ArrayList<Music>();
		for(int i=0; i<ary.length(); i++){
			JSONObject mo = ary.getJSONObject(i);
			Music m = new Music();
			m.setTitle(mo.getString("title"));
			m.setSong_id(mo.getString("song_id"));
			m.setAuthor(mo.getString("author"));
			m.setArtist_id(mo.getString("artist_id"));
			m.setAlbum_title(mo.getString("album_title"));
			m.setAlbum_id(mo.getString("album_id"));
			musics.add(m);
		}
		return musics;
	}

}
