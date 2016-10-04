package hzc.himusic.util;

import hzc.himusic.entity.Music;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 解析XML文档的工具类
 */
public class XmlParser {

	/**
	 * 解析音乐列表
	 * @param is  输入流
	 * @return  List<Music>
	 * @throws XmlPullParserException 
	 */
	public static List<Music> parseMusicList(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");
		int type = parser.getEventType();
		List<Music> musics = new ArrayList<Music>();
		Music m = null;
		while(type != XmlPullParser.END_DOCUMENT){
			switch (type) {
			case XmlPullParser.START_TAG: //碰到了开始标签
				String tagName = parser.getName();
				if("song".equals(tagName)){
					m = new Music();
					musics.add(m);
				}else if("artist_id".equals(tagName)){
					m.setArtist_id(parser.nextText());
				}else if("pic_big".equals(tagName)){
					m.setPic_big(parser.nextText());
				}else if("pic_small".equals(tagName)){
					m.setPic_small(parser.nextText());
				}else if("publishtime".equals(tagName)){
					m.setPublishtime(parser.nextText());
				}else if("lrclink".equals(tagName)){
					m.setLrclink(parser.nextText());
				}else if("style".equals(tagName)){
					m.setStyle(parser.nextText());
				}else if("song_id".equals(tagName)){
					m.setSong_id(parser.nextText());
				}else if("title".equals(tagName)){
					m.setTitle(parser.nextText());
				}else if("ting_uid".equals(tagName)){
					m.setTing_uid(parser.nextText());
				}else if("author".equals(tagName)){
					m.setAuthor(parser.nextText());
				}else if("album_id".equals(tagName)){
					m.setAlbum_id(parser.nextText());
				}else if("album_title".equals(tagName)){
					m.setAlbum_title(parser.nextText());
				}else if("artist_name".equals(tagName)){
					m.setArtist_name(parser.nextText());
				}
				break;
			}
			//继续驱动事件
			type = parser.next();
		}
		return musics;
	}
	
}




